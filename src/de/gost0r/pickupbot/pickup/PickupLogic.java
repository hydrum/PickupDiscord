package de.gost0r.pickupbot.pickup;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.PickupBotDiscordMain;
import de.gost0r.pickupbot.discord.*;
import de.gost0r.pickupbot.discord.api.DiscordAPI;
import de.gost0r.pickupbot.pickup.PlayerBan.BanReason;
import de.gost0r.pickupbot.pickup.server.Server;
import io.sentry.Sentry;
import org.json.JSONObject;

public class PickupLogic {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public PickupBot bot;
	public Database db;

	private List<Server> serverList;
	private List<Server> gtvServerList;
	private List<GameMap> mapList;

	private Map<PickupRoleType, List<DiscordRole>> roles;
	private Map<PickupChannelType, List<DiscordChannel>> channels;
	
	private List<Match> ongoingMatches; // ongoing matches (live)
	
	private Queue<Match> awaitingServer;
	
	private Map<Gametype, Match> curMatch;
	
	private boolean locked;
	
	private Map<BanReason, String[]> banDuration;
	private Map<Gametype, GameMap> lastMapPlayed;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		db = new Database(this);
		Player.db = db;
		Player.logic = this;
		
		serverList = db.loadServers();
		roles = db.loadRoles();
		channels = db.loadChannels();
		
		curMatch = new HashMap<Gametype, Match>();
		lastMapPlayed = new HashMap<Gametype, GameMap>();
		for (Gametype gt : db.loadGametypes()) {
			if (gt.getActive()) {
				curMatch.put(gt, null);
				lastMapPlayed.put(getGametypeByString(gt.getName()), new GameMap("null"));
			}
		}
		mapList = db.loadMaps(); // needs current gamemode list
		ongoingMatches = db.loadOngoingMatches(); // need maps, servers and gamemodes
		
		createCurrentMatches();
		
		banDuration = new HashMap<BanReason, String[]>();
		banDuration.put(BanReason.NOSHOW, new String[] {"10m", "30m", "1h", "2h", "6h", "12h", "3d", "1w", "2w", "1M"});
		banDuration.put(BanReason.RAGEQUIT, new String[] {"30m", "1h", "2h", "6h", "12h", "3d", "1w", "2w", "1M", "3M"});
		
		awaitingServer = new LinkedList<Match>();
		
		Server testGTV = new Server(0, "gtv.b00bs-clan.com", 709, "arkon4bmn", "SevenAndJehar", true, null);
		gtvServerList = new ArrayList<Server>();
		gtvServerList.add(testGTV);
	}

	public void cmdAddPlayer(Player player, List<Gametype> modes, boolean forced) {
		for (Gametype gt : modes) {
			cmdAddPlayer(player, gt, forced);
		}
	}

	public void cmdAddPlayer(Player player, Gametype gt, boolean forced) {
		
		if (locked && !forced) {
			bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
			return;
		}
		if (player.isBanned() && !forced) {
			bot.sendMsg(bot.getLatestMessageChannel(), printBanInfo(player));
			return;
		}
		if (playerInActiveMatch(player) != null) {
			bot.sendNotice(player.getDiscordUser(), Config.player_already_match);
			return;
		}
		
		int minEloDiv1 = 1400; //=Platinum players and higher.
		if (gt.getName().equalsIgnoreCase("div1") && player.getElo() < minEloDiv1){
			bot.sendNotice(player.getDiscordUser(), Config.player_notdiv1.replace(".minelo.", String.valueOf(minEloDiv1)));
			return;
		}
		if (forced) {
			player.setLastMessage(System.currentTimeMillis());
		}
		
		String defmsg = "You are already in queue for:";
		StringBuilder msg = new StringBuilder(defmsg);
		
		if (curMatch.containsKey(gt)) {
			Match m = curMatch.get(gt);
			if (m.getMatchState() != MatchState.Signup || m.isInMatch(player) || playerInActiveMatch(player) != null) {
				msg.append(" ").append(gt.getName());
			} else {
				m.addPlayer(player);
			}
		}
		
		if (!msg.toString().equals(defmsg)) {
			bot.sendNotice(player.getDiscordUser(), msg.toString());
		}
	}

	public void cmdRemovePlayer(Player player, List<Gametype> modes) {
		
		if (playerInActiveMatch(player) != null) {
			bot.sendNotice(player.getDiscordUser(), Config.player_already_match);
			return;
		}
		
		// remove from all if null
		if (modes == null) {
			for (Match match : curMatch.values()) {
				match.removePlayer(player, true);
			}
			return;
		}
		
		for (Gametype gt : modes) {
			if (gt != null && curMatch.containsKey(gt)) {
				curMatch.get(gt).removePlayer(player, true); // conditions checked within function
			}
		}
	}

	public void cmdPick(DiscordInteraction interaction, Player player, int pick) {
		for (Match match : ongoingMatches) {
			if (match.isCaptainTurn(player)) {
				interaction.respond(null);
				match.pick(player, pick);
				return;
			}
		}
		interaction.respond(Config.player_not_captain);
	}

	public void cmdLock() {
		locked = true;
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.lock_enable);
	}

	public void cmdUnlock() {
		locked = false;
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.lock_disable);
	}

	public void cmdRegisterPlayer(DiscordUser user, String urtauth, String msgid) {
		// check whether the user and the urtauth aren't taken
		if (Player.get(user) == null) {
			if (Player.get(urtauth) == null) {
				if (urtauth.matches("^[a-z0-9]*$")) {
					if (urtauth.length() != 32) {
						Player p = new Player(user, urtauth);
						p.setElo(db.getAvgElo());
						db.createPlayer(p);
						bot.sendNotice(user, Config.auth_success);
					} else {
						DiscordAPI.deleteMessage(bot.getLatestMessageChannel(), msgid);
						bot.sendNotice(user, Config.auth_sent_key);
					}
				} else {
					bot.sendNotice(user, Config.auth_invalid);
				}
			} else {
				bot.sendNotice(user, Config.auth_taken_urtauth);
			}
		} else {
			bot.sendNotice(user, Config.auth_taken_user);
		}
	}

	public boolean cmdUnregisterPlayer(Player player) {
		List<Match> matches = playerInMatch(player);
		for (Match m : matches) {
			m.removePlayer(player, true);
		}
		db.removePlayer(player);
		Player.remove(player);
		return true;
	}

	public void cmdSetPlayerCountry(DiscordUser user, String str_country) {
		
			// check if user is already registered
			if (Player.get(user) != null) {
				Player p = db.loadPlayer(user);
				
				if(p.getCountry().equalsIgnoreCase("NOT_DEFINED") || user.hasAdminRights()) {
					
					if(Country.isValid(str_country))
					{
						db.updatePlayerCountry(p, str_country);
						Player.get(user).setCountry(str_country);
						bot.sendNotice(user, Config.country_added);
					}
					else
					{
						bot.sendNotice(user, "Unknown county code. Look yours up: <https://datahub.io/core/country-list/r/0.html>");
					}
				}
				else {
					// Region has been set by user, need an admin 
					bot.sendNotice(user, "Your country code is already set. Corresponding region: " + p.getRegion().toString());
				}
			} 
			else {
				// send register first notice
				bot.sendNotice(user, Config.user_not_registered);
			}
		
	}

	public void cmdChangePlayerCountry(Player p, String str_country) {
		
		if(Country.isValid(str_country))
		{
			db.updatePlayerCountry(p, str_country);
			p.setCountry(str_country);
			bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), "Country code updated to " + str_country);
		}
		else
		{
			bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), "Unknown county code. Look yours up: <https://datahub.io/core/country-list/r/0.html>");
		}	
	}

	public void cmdTopElo(int number) {
		StringBuilder embed_rank = new StringBuilder();
		StringBuilder embed_player = new StringBuilder();
		StringBuilder embed_elo = new StringBuilder();
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 players";
		embed.color = 7056881;
		int rank = 1;
		
		List<Player> players = db.getTopPlayers(number);
		if (players.isEmpty()) {
			bot.sendMsg(bot.getLatestMessageChannel(), "None");
		} else {
			for (Player p : players) {
				String country;
				if( p.getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  "<:puma:849287183474884628>";
				}
				else {
					country = ":flag_" + p.getCountry().toLowerCase() + ":";
				}
				embed_rank.append("**").append(rank).append("**\n");
				embed_player.append(country).append(" \u200b \u200b  ").append(p.getUrtauth()).append('\n');
				embed_elo.append(p.getRank().getEmoji()).append(" \u200b \u200b  ").append(p.getElo()).append("\n");
				rank++;
			}
			embed.addField("\u200b", embed_rank.toString(), true);
			embed.addField("Player", embed_player.toString(), true);
			embed.addField("Elo", embed_elo.toString(), true);
			
			bot.sendMsg(bot.getLatestMessageChannel(), null, embed);
		}
	}

	public void cmdTopWDL(int number) {
		StringBuilder embed_rank = new StringBuilder();
		StringBuilder embed_player = new StringBuilder();
		StringBuilder embed_wdl = new StringBuilder();
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 win rate";
		embed.color = 7056881;
		
		
		Map<Player, Float> topwdl = db.getTopWDL(number);
		if (topwdl.isEmpty()) {
			bot.sendMsg(bot.getLatestMessageChannel(), "None");
		} else {
			int rank = 1;
			for (Map.Entry<Player, Float> entry : topwdl.entrySet()) {
				String country;
				if( entry.getKey().getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  ":puma:";
				}
				else {
					country = ":flag_" + entry.getKey().getCountry().toLowerCase() + ":";
				}
				embed_rank.append("**").append(rank).append("**\n");
				embed_player.append(country).append(" \u200b \u200b  ").append(entry.getKey().getUrtauth()).append('\n');
				embed_wdl.append(Math.round(entry.getValue() * 100d)).append(" %\n");
				rank++;
			}
			embed.addField("\u200b", embed_rank.toString(), true);
			embed.addField("Player", embed_player.toString(), true);
			embed.addField("Win rate", embed_wdl.toString(), true);
			
			bot.sendMsg(bot.getLatestMessageChannel(), null, embed);
		}
	}

	public void cmdTopKDR(int number) {
		StringBuilder embed_rank = new StringBuilder();
		StringBuilder embed_player = new StringBuilder();
		StringBuilder embed_wdl = new StringBuilder();
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 kill death ratio";
		embed.color = 7056881;
		
		
		Map<Player, Float> topkdr = db.getTopKDR(number);
		if (topkdr.isEmpty()) {
			bot.sendMsg(bot.getLatestMessageChannel(), "None");
		} else {
			int rank = 1;
			for (Map.Entry<Player, Float> entry : topkdr.entrySet()) {
				String country;
				if( entry.getKey().getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  "<:puma:849287183474884628>";
				}
				else {
					country = ":flag_" + entry.getKey().getCountry().toLowerCase() + ":";
				}
				embed_rank.append("**").append(rank).append("**\n");
				embed_player.append(country).append(" \u200b \u200b  ").append(entry.getKey().getUrtauth()).append('\n');
				embed_wdl.append(String.format("%.02f", entry.getValue())).append("\n");
				rank++;
			}
			embed.addField("\u200b", embed_rank.toString(), true);
			embed.addField("Player", embed_player.toString(), true);
			embed.addField("KDR", embed_wdl.toString(), true);
			
			bot.sendMsg(bot.getLatestMessageChannel(), null, embed);
		}
	}

	public String cmdGetElo(Player p, boolean sendMsg) {
		if (p == null) {
			return "";
		}
		String msg = Config.pkup_getelo;
		msg = msg.replace(".urtauth.", p.getUrtauth());
		msg = msg.replace(".elo.", String.valueOf(p.getElo()));
		msg = msg.replace(".wdl.", String.valueOf(Math.round(db.getWDLForPlayer(p).calcWinRatio() * 100d)));
		msg = msg.replace(".position.", String.valueOf(db.getRankForPlayer(p)));
		msg = msg.replace(".rank.", p.getRank().getEmoji());
		msg = msg.replace(".kdr.", String.format("%.02f", p.getKdr()));
		
		if( p.getCountry().equalsIgnoreCase("NOT_DEFINED")) {
			msg = msg.replace(".country.", "<:puma:849287183474884628>");
		}
		else {
			msg = msg.replace(".country.", ":flag_" + p.getCountry().toLowerCase() + ":");
		}
		
		if (sendMsg) {
			bot.sendMsg(bot.getLatestMessageChannel(), msg);
		}

		return msg;
	}

	public void cmdGetStats(Player p) {
		if (p == null) {
			return;
		}
		
		p.stats = db.getPlayerStats(p);
		
		String country = "<:puma:849287183474884628>";
		if(!p.getCountry().equalsIgnoreCase("NOT_DEFINED")) {
			country = ":flag_" + p.getCountry().toLowerCase() + ":";
		}
			
		DiscordEmbed statsEmbed = new DiscordEmbed();
		statsEmbed.color = 7056881;
		statsEmbed.title = country + " \u200b \u200b  " +  p.getUrtauth();
		statsEmbed.thumbnail = p.getDiscordUser().getAvatarUrl();
		statsEmbed.description = p.getRank().getEmoji() + " \u200b \u200b  **" + p.getElo() + "**  #" + db.getRankForPlayer(p);
		
		statsEmbed.addField("Kills / Assists", p.stats.kills + "/" + p.stats.assists, true);
		statsEmbed.addField("Deaths", String.valueOf(p.stats.deaths), true);
		if (p.stats.kdrRank == -1) {
			statsEmbed.addField("KDR", String.format("%.02f", p.getKdr()), true);
			
		} else {
			statsEmbed.addField("KDR", String.format("%.02f", p.getKdr()) + " (#" + p.stats.kdrRank + ")", true);
		}
		
		statsEmbed.addField("Wins / Draws", p.stats.wins + "/" + p.stats.draws, true);
		statsEmbed.addField("Defeats", String.valueOf(p.stats.losses), true);
		if (p.stats.wdlRank == -1) {
			statsEmbed.addField("Win rate", Math.round(db.getWDLForPlayer(p).calcWinRatio() * 100d) + "%", true);
			
		} else {
			statsEmbed.addField("Win rate", Math.round(db.getWDLForPlayer(p).calcWinRatio() * 100d) + "% (#" + p.stats.wdlRank + ")", true);
		}
		
		bot.sendMsg(bot.getLatestMessageChannel(), null, statsEmbed);
	}

	public void cmdTopCountries(int number) {
		StringBuilder msg = new StringBuilder(Config.pkup_top5_header);
		
		ArrayList<CountryRank> countries = db.getTopCountries(number);
		if (countries.isEmpty()) {
			bot.sendMsg(bot.getLatestMessageChannel(), msg + "  None");
		} else {
			
			for(int i = 0; i < countries.size(); i++) {
				String ranking = Config.pkup_getelo_country;
				
				ranking = ranking.replace(".position.", Integer.toString(i + 1));
				ranking = ranking.replace(".country.", Country.getCountryFlag(countries.get(i).country));
				ranking = ranking.replace(".elo.", countries.get(i).elo.toString());
				
				msg.append("\n").append(ranking);
			}
			
			bot.sendMsg(bot.getLatestMessageChannel(), msg.toString());
		}
	}

	public void cmdGetMaps(boolean showZeroVote) {
		StringBuilder msg = new StringBuilder("None");
		for (Gametype gametype : curMatch.keySet()) {
			if (msg.toString().equals("None")) {
				msg = new StringBuilder();
			} else {
				msg.append("\n");
			}
			String mapString = Config.pkup_map_list;
			mapString = mapString.replace(".gametype.", gametype.getName());
			mapString = mapString.replace(".maplist.", curMatch.get(gametype).getMapVotes(!showZeroVote));
			msg.append(mapString);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), msg.toString());
	}

	public void cmdMapVote(Player player, Gametype gametype, String mapname) {
		Match activeMatch = playerInActiveMatch(player);
		Match m = null;
		
		if (activeMatch != null) {
			m = activeMatch;
			gametype = m.getGametype();
		}
		
		else if (gametype == null) {
			List<Match> matches = playerInMatch(player);
			if (matches.size() == 1) {
				gametype = matches.get(0).getGametype();
			} else if (matches.size() == 0) {
				bot.sendNotice(player.getDiscordUser(), Config.player_not_in_match);
				return;
			} else {
				bot.sendNotice(player.getDiscordUser(), Config.map_specify_gametype);
				return;
			}
		}
		if (curMatch.containsKey(gametype) || m != null) {
			if (m == null) {
				m = curMatch.get(gametype);
			}
			
			if (m.getMatchState() == MatchState.Signup || m.getMatchState() == MatchState.AwaitingServer) {
				int counter = 0;
				GameMap map = null;
				for (GameMap xmap : m.getMapList()) {
					if (xmap.name.toLowerCase().contains(mapname.toLowerCase())) {
						counter++;
						map = xmap;
					}
				}
				if (counter > 1) {
					bot.sendNotice(player.getDiscordUser(), Config.map_not_unique);
				} else if (counter == 0) {
					bot.sendNotice(player.getDiscordUser(), Config.map_not_found);
				} else if (lastMapPlayed.get(gametype).name.equals(map.name)) {
					bot.sendNotice(player.getDiscordUser(), Config.map_played_last_game);
				} else {
					m.voteMap(player, map); // handles sending a msg itself
				}
			}
		}
	}

	public void cmdStatus() {
		if (curMatch.isEmpty()) {
			bot.sendMsg(bot.getLatestMessageChannel(), Config.pkup_match_unavi);
			return;
		}
		StringBuilder msg = new StringBuilder("None");
		for (Match m : curMatch.values()) {
			if (msg.toString().equals("None")) {
				msg = new StringBuilder();
			} else {
				msg.append("\n");
			}
			msg.append(cmdStatus(m, null, false));
		}
		bot.sendMsg(bot.getLatestMessageChannel(), msg.toString());
	}

	public String cmdStatus(Match match, Player player, boolean shouldSend) {
		String msg = "";
		int playerCount = match.getPlayerCount();
		if (playerCount == 0 && player == null) {
			msg = Config.pkup_status_noone;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
			msg = msg.replace("<gametype>", match.getGametype().getName().toLowerCase());
		} else if (match.getMatchState() == MatchState.Signup || match.getMatchState() == MatchState.AwaitingServer){
			msg = Config.pkup_status_signup;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
			msg = msg.replace(".playernumber.", String.valueOf(playerCount));
			msg = msg.replace(".maxplayer.", String.valueOf(match.getGametype().getTeamSize() * 2));

			StringBuilder playernames = new StringBuilder("None");
			if (player == null) {
				for (Player p : match.getPlayerList()) {
					if (playernames.toString().equals("None")) {
						playernames = new StringBuilder(p.getUrtauth());
					} else {
						playernames.append(" ").append(p.getUrtauth());
					}
				}
			} else {
				playernames = new StringBuilder(player.getDiscordUser().getMentionString());
				playernames.append((match.isInMatch(player)) ? " added." : " removed.");
			}
			
			msg = msg.replace(".playerlist.", playernames.toString());
			
		} 
		if (shouldSend) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
		}
		if (match.getMatchState() == MatchState.AwaitingServer && shouldSend && match.getGametype().getTeamSize() > 1) {
			msg = Config.pkup_status_server;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
			msg = msg.replace(".votes.", match.getMapVotes(true));
			
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
		}
		return msg;
	}

	public void cmdSurrender(Player player) {
		Match match = playerInActiveMatch(player);
		if (match != null && match.getMatchState() == MatchState.Live) {
			match.voteSurrender(player);
		}
		else bot.sendNotice(player.getDiscordUser(), Config.player_not_in_match);
	}

	public void cmdReset(String cmd) {
		cmdReset(cmd, null);
	}

	public void cmdReset(String cmd, String mode) {
		if (cmd.equals("all")) {
			List<Match> toRemove = new ArrayList<Match>();
			for (Match match : ongoingMatches) {
				match.reset();
				toRemove.add(match);
			}
			ongoingMatches.removeAll(toRemove);
			for (Match m : curMatch.values()) {
				m.reset();
				createCurrentMatches();
			}
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_all);
		} else if (cmd.equals("cur")) {
			Gametype gt = getGametypeByString(mode);
			if (gt != null) {
				curMatch.get(gt).reset();
				createMatch(gt);
			} else {
				for (Match m : curMatch.values()) {
					m.reset();
				}
				createCurrentMatches();
			}
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_cur);
		} else {
			Gametype gt = getGametypeByString(cmd);
			if (gt != null) {
				curMatch.get(gt).reset();
				createMatch(gt);
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_type.replace(".gametype.", gt.getName()));
			} else {
				try {
					int idx = Integer.parseInt(cmd);
					for (Match match : ongoingMatches) {
						if (match.getID() == idx) {
							match.reset();
							ongoingMatches.remove(match);
							bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_id.replace(".id.", cmd));
						}
					}
				} catch (NumberFormatException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
					Sentry.capture(e);
				}
			}
		}
	}

	public void cmdGetData(String id, DiscordChannel channel) {
		String msg = "Match not found.";
		try {
			int i_id = Integer.parseInt(id);
			for (Match match : ongoingMatches) {
				if (match.getID() == i_id) {
					msg = Config.pkup_pw;
					msg = msg.replace(".server.", match.getServer().getAddress());
					msg = msg.replace(".password.", match.getServer().password);
					bot.sendMsg(channel, msg);
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		bot.sendMsg(channel, msg);
	}

	public boolean cmdEnableMap(String mapname, String gametype) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		GameMap map = null;
		for (GameMap xmap : mapList) {
			if (xmap.name.equals(mapname)) {
				map = xmap;
				break;
			}
		}
		if (map == null) {
			map = new GameMap(mapname);
			map.setGametype(gt, true);
			db.createMap(map, gt);
			mapList.add(map);
		} else {
			map.setGametype(gt, true);
			db.updateMap(map, gt);
		}
		return true;
	}

	public boolean cmdDisableMap(String mapname, String gametype) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		for (GameMap map : mapList) {
			if (map.name.equals(mapname)) {
				map.setGametype(gt, false);
				db.updateMap(map, gt);
				return true;
			}
		}
		return false;
	}

	public boolean cmdEnableGametype(String gametype, String teamSize) {
		try {
			int i_teamSize = Integer.parseInt(teamSize);
			Gametype gt = getGametypeByString(gametype);
			if (gt == null) {
				gt = new Gametype(gametype.toUpperCase(), i_teamSize, true);
			}
			gt.setTeamSize(i_teamSize);
			gt.setActive(true);
			db.updateGametype(gt);
			// checking whether this was active before
			Gametype tmp = getGametypeByString(gametype);
			if (tmp != null) {
				curMatch.get(tmp).reset();
			}
			createMatch(gt);
			return true;
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
			return false;
		}
	}

	public boolean cmdDisableGametype(String gametype) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
				
		gt.setActive(false);
		db.updateGametype(gt);
		curMatch.get(gt).reset();
		curMatch.remove(gt);
		return true;
	}

	public boolean cmdAddGameConfig(String gametype, String command) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		gt.addConfig(command);

		return true;
	}

	public boolean cmdRemoveGameConfig(String gametype, String command) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		gt.removeConfig(command);
		
		db.updateGametype(gt);
		return true;
	}

	public boolean cmdListGameConfig(DiscordChannel channel, String gametype) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		StringBuilder configlist = new StringBuilder();
		for (String config : gt.getConfig()) {
			if (configlist.length() > 0) {
				configlist.append("\n");
			}
			configlist.append(config);
		}
		
		String msg = Config.pkup_config_list;
		msg = msg.replace(".gametype.", gt.getName());
		msg = msg.replace(".configlist.", configlist.toString());
		bot.sendMsg(channel, msg);
		
		return true;
	}

	public boolean cmdAddServer(String serveraddr, String rcon, String str_region) {
		try {
			String ip = serveraddr;
			int port = 27960;
			if (serveraddr.contains(":")) {
				String[] servers = serveraddr.split(":");
				ip = servers[0];
				port = Integer.parseInt(servers[1]);
			}
			for (Server s : serverList) {
				if (s.IP.equals(ip) && s.port == port) {
					return false;
				}
			}
			
			Region region = Region.valueOf(str_region);
			Server server = new Server(-1, ip, port, rcon, "???", true, region);

			db.createServer(server);
			serverList.add(server);
			checkServer();
			return true;
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
			return false;
		}
	}

	public boolean cmdServerActivation(String id, boolean active) {
		try {
			int idx = Integer.parseInt(id);
			for (Server server : serverList) {
				if (server.id == idx && !server.isTaken() && server.active != active) {
					server.active = active;
					db.updateServer(server);
					checkServer();
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}

	public boolean cmdServerChangeRcon(String id, String rcon) {
		try {
			int idx = Integer.parseInt(id);
			for (Server server : serverList) {
				if (server.id == idx) {
					server.rconpassword = rcon;
					db.updateServer(server);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}

	public boolean cmdServerSendRcon(String id, String rconString) {
		try {
			int idx = Integer.parseInt(id);
			for (Server server : serverList) {
				if (server.id == idx) {
					server.sendRcon(rconString);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}

	public void cmdServerList(DiscordChannel channel) {
		StringBuilder msg = new StringBuilder("None");
		for (Server server : serverList) {
			if (msg.toString().equals("None")) {
				msg = new StringBuilder(server.toString());
			} else {
				msg.append("\n").append(server.toString());
			}
		}
		bot.sendMsg(channel, msg.toString());
	}

	public void cmdMatchList(DiscordChannel channel) {
		StringBuilder msg = new StringBuilder("None");
		for (Match match : curMatch.values()) {
			if (msg.toString().equals("None")) {
				msg = new StringBuilder(match.toString());
			} else {
				msg.append("\n").append(match.toString());
			}
		}
		for (Match match : ongoingMatches) {
			if (msg.toString().equals("None")) {
				msg = new StringBuilder(match.toString());
			} else {
				msg.append("\n").append(match.toString());
			}
		}
		bot.sendMsg(channel, msg.toString());
	}

	public void cmdLive(DiscordChannel channel) {
		String msg = "No live matches found.";
		DiscordEmbed scoreBoardLinkEmbed = new DiscordEmbed();
		for (Match match : ongoingMatches) {
			msg = match.getMatchInfo();
			if (match.getMatchState() == MatchState.AwaitingServer || match.getServer().getServerMonitor() == null || match.liveScoreMsgs.isEmpty()) {
				bot.sendMsg(bot.getLatestMessageChannel(), msg);
			} 
			else {
				String scoreBoardLink = "";
				for (DiscordMessage liveScoreMsg : match.liveScoreMsgs){
					if(liveScoreMsg.channel.parent_id.equals(channel.id)){
						scoreBoardLink = "[Live scoreboard](https://discord.com/channels/" + liveScoreMsg.channel.guild_id + "/" + liveScoreMsg.channel.id + "/" + liveScoreMsg.id + ")";
						break;
					}
				}
				StringBuilder embedDescription = new StringBuilder(scoreBoardLink);

				scoreBoardLinkEmbed.color = 7056881;
				
				if (match.getGtvServer() != null) {
					embedDescription.append("\n").append(Config.pkup_go_pub_calm);
				}

				scoreBoardLinkEmbed.description = embedDescription.toString();
				bot.sendMsg(bot.getLatestMessageChannel(), msg, scoreBoardLinkEmbed);
			}
		}
		
		if (msg.equals("No live matches found.")) {
			bot.sendMsg(bot.getLatestMessageChannel(), msg);
		}
	}

	public void cmdDisplayMatch(String matchid) {
		try {
			int idx = Integer.parseInt(matchid);
			for (Match match : ongoingMatches) {
				if (match.getID() == idx) {
					bot.sendMsg(bot.getLatestMessageChannel(), match.getMatchInfo());
					return;
				}
			}
			
			Match match = db.loadMatch(idx); // TODO: cache?
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return;
			}			
		
		} catch (NumberFormatException e) {
			bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
	}

	public void cmdDisplayLastMatch() {
		try {
			Match match = db.loadLastMatch(); 
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return;
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
	}

	public void cmdDisplayLastMatchPlayer(Player p) {
		try {
			Match match = db.loadLastMatchPlayer(p); 
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return;
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
	}

	public void cmdResetElo(){
		db.resetElo();
	}

	// Matchcreation

	private void createCurrentMatches() {
		for (Gametype gametype : curMatch.keySet()) {
			createMatch(gametype);
		}
	}

	private void createMatch(Gametype gametype) {
		List<GameMap> gametypeMapList = new ArrayList<GameMap>();
		for (GameMap map : mapList) {
			if (map.isActiveForGametype(gametype)) {
				gametypeMapList.add(map);
			}
		}
		Match match = new Match(this, gametype, gametypeMapList);
		
		curMatch.put(gametype, match);
	}

	public void requestServer(Match match) {
		if (!awaitingServer.contains(match)) {
			awaitingServer.add(match);
			checkServer();
		}
	}

	public void cancelRequestServer(Match match) {
		awaitingServer.remove(match);
	}

	public void matchStarted(Match match) {
		createMatch(match.getGametype());
		ongoingMatches.add(match);
	}

	public void matchEnded() {
//		matchRemove(match); // dont remove as this can be called while in a loop
		checkServer();
	}
	
	public void matchRemove(Match match) {
		ongoingMatches.remove(match);
	}

	private void checkServer() {
		if(!awaitingServer.isEmpty())
		{
			Match m = awaitingServer.poll();
			
			if(m != null)
			{
				Server bs = getBestServer(m.getPreferredServerRegion());
				
				if(bs != null && bs.active && !bs.isTaken() && bs.isOnline() && m.getMatchState() == MatchState.AwaitingServer)
				{
					m.launch(bs);
				}
				else
				{
					for (Server server : serverList) { // Use NAE server by default when the best region is not avi
						if (server.active && !server.isTaken() && server.isOnline() && m.getMatchState() == MatchState.AwaitingServer && server.region == Region.NAE) {
								m.launch(server);
								return;
						}
					}
					for (Server server : serverList) { // If no NAE servers are avi take the first avi
						if (server.active && !server.isTaken() && server.isOnline() && m.getMatchState() == MatchState.AwaitingServer) {
							m.launch(server);
							return;
						}
					}
				}
			}
		}
	}

	private Server getBestServer(Region r)
	{
		Server bestServer = null;
		
		for (Server server : serverList) {
			if (server.region == r && server.active && !server.isTaken()) {	
				bestServer = server;
				break;
			}
		}
		
		return bestServer;
	}


	// ROLES & CHANNEL

	public boolean addRole(PickupRoleType type, DiscordRole role) {
		if (!roles.containsKey(type)) {
			roles.put(type, new ArrayList<DiscordRole>());
		}
		if (!roles.get(type).contains(role)) {
			roles.get(type).add(role);
			db.updateRole(role, type);
			return true;
		}
		return false;
	}

	public boolean removeRole(PickupRoleType type, DiscordRole role) {
		if (roles.containsKey(type)) {
			roles.get(type).remove(role);
			db.updateRole(role, PickupRoleType.NONE);
			return true;
		}
		return false;
	}

	public boolean addChannel(PickupChannelType type, DiscordChannel channel) {
		if (!channels.containsKey(type)) {
			channels.put(type, new ArrayList<DiscordChannel>());
		}
		if (!channels.get(type).contains(channel)) {
			channels.get(type).add(channel);
			db.updateChannel(channel, type);
			return true;
		}
		return false;
	}

	public boolean removeChannel(PickupChannelType type, DiscordChannel channel) {
		if (channels.containsKey(type)) {
			channels.get(type).remove(channel);
			db.updateChannel(channel, PickupChannelType.NONE);
			return true;
		}
		return false;
	}
	
	// AFK CHECK

	public void afkCheck() {
		Set<Player> playerList = new HashSet<Player>();
		for (Match m : curMatch.values()) {			
			playerList.addAll(m.getPlayerList());
		}
		
		for (Player p : playerList) {
			//long latestAFKmsg = p.getDiscordUser().statusChangeTime > p.getLastMessage() ? p.getDiscordUser().statusChangeTime : p.getLastMessage();
			long latestAFKmsg = p.getLastMessage();
			long afkKickTime = latestAFKmsg + 20 * 60 * 1000;
			long afkReminderTime = latestAFKmsg + 17 * 60 * 1000;
			
			if (afkKickTime < System.currentTimeMillis()) {
				LOGGER.info("AFK: REMOVE - " + p.getUrtauth() + ": " + afkKickTime + " > " + System.currentTimeMillis());
				cmdRemovePlayer(p, null);
			} else if (afkReminderTime < System.currentTimeMillis() && !p.getAfkReminderSent()) {
				p.setAfkReminderSent(true);
				LOGGER.info("AFK: REMINDER - " + p.getUrtauth() + ": " + afkKickTime + " > " + System.currentTimeMillis());
				String msg = Config.afk_reminder;
				msg = msg.replace(".user.", p.getDiscordUser().getMentionString());
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
			}
		}
		
		for (Match m :ongoingMatches) {
			if (m.getMatchState() != MatchState.AwaitingServer) {
				continue;
			}
			
			long lastPickTime = m.getTimeLastPick();
			long pickKickTime = lastPickTime + 3 * 60 * 1000;
			long pickReminderTime = lastPickTime + 2 * 60 * 1000;
			
			if (pickKickTime < System.currentTimeMillis()) {
				String msg = Config.pick_reset;
				msg = msg.replace(".matchid.", String.valueOf(db.getLastMatchID() + 1));
				msg = msg.replace(".user.", m.getCaptainsTurn().getDiscordUser().getMentionString());
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
				autoBanPlayer(m.getCaptainsTurn(), BanReason.NOSHOW);
				m.reset();
			} else if (pickReminderTime < System.currentTimeMillis() && !m.getPickReminderSent()) {
				m.setPickReminderSent(true);
				LOGGER.info("PICK: REMINDER - " + m.getCaptainsTurn().getUrtauth() + ": " + pickKickTime + " > " + System.currentTimeMillis());
				String msg = Config.pick_reminder;
				msg = msg.replace(".user.", m.getCaptainsTurn().getDiscordUser().getMentionString());
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
			}
		}
	}


	public void autoBanPlayer(Player player, BanReason reason) {
		String[] durationString = banDuration.get(reason);
		PlayerBan latestBan = player.getLatestBan();
		
		int strength = 0;
		if (latestBan != null) {
			long latestBanDuration = latestBan.endTime - latestBan.startTime;
			latestBanDuration = Math.max(latestBanDuration * 2, parseDurationFromString("1w"));
			strength = player.getPlayerBanCountSince(System.currentTimeMillis() - latestBanDuration);
			strength = Math.min(strength, durationString.length - 1);
		}
		long duration = parseDurationFromString(durationString[strength]);
		
		banPlayer(player, reason, duration);
	}

	public void banPlayer(Player player, BanReason reason, long duration) {
		
		// add reminaing bantime to the new ban
		long endTime;
		if (player.isBanned()) {
			endTime = player.getLatestBan().endTime + duration;
		} else {
			endTime = System.currentTimeMillis() + duration;
		}
		PlayerBan ban = new PlayerBan();
		ban.player = player;
		ban.startTime = System.currentTimeMillis();
		ban.endTime = endTime;
		ban.reason = reason;
		
		player.addBan(ban);
		db.createBan(ban);
		
		bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), printBanInfo(player));
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), printBanInfo(player));
		
		for (Match match : playerInMatch(player)) {
			match.removePlayer(player, true);
		}
	}

	public void UnbanPlayer(Player player) {
			
			if (player.isBanned()) {
				
				player.forgiveBan();
				
				bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), printUnbanInfo(player));
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), printUnbanInfo(player));
			} else {
				// Player is not banned 
				bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), printPlayerNotBannedInfo(player));
			}
	}


	public String printBanInfo(Player player) {
		PlayerBan ban = player.getLatestBan();
		
		if (ban == null || ban.endTime <= System.currentTimeMillis() || ban.forgiven) {
			String msg = Config.not_banned;
			msg = msg.replace(".user.", player.getDiscordUser().getMentionString());
			msg = msg.replace(".urtauth.", player.getUrtauth());
			return msg;
		}
		
		String time = parseStringFromDuration(ban.endTime - System.currentTimeMillis());
		
		String msg = Config.is_banned;
		msg = msg.replace(".user.", player.getDiscordUser().getMentionString());
		msg = msg.replace(".urtauth.", player.getUrtauth());
		msg = msg.replace(".reason.", ban.reason.name());
		msg = msg.replace(".time.", time);
		return msg;
	}

	public String printUnbanInfo(Player player) {
		String msg = Config.is_unbanned;
		msg = msg.replace(".user.", player.getDiscordUser().getMentionString());
		msg = msg.replace(".urtauth.", player.getUrtauth());
		return msg;
	}

	public String printPlayerNotBannedInfo(Player player) {
		String msg = Config.is_notbanned;
		msg = msg.replace(".user.", player.getDiscordUser().getMentionString());
		msg = msg.replace(".urtauth.", player.getUrtauth());
		return msg;
	}
	
	// HELPER

	public static long parseDurationFromString(String string) {
		long total = 0;
		
		StringBuilder curDuration = new StringBuilder();
		for (int i = 0; i < string.length(); ++i) {
			if (Character.isDigit(string.charAt(i))) {
				curDuration.append(string.charAt(i));
			} else {
				long duration = Long.parseLong(curDuration.toString());
				switch (string.charAt(i)) {
				case 'y': duration *= 12;
				case 'M': duration *= 4;
				case 'w': duration *= 7;
				case 'd': duration *= 24;
				case 'h': duration *= 60;
				case 'm': duration *= 60;
				case 's': duration *= 1000;
				}
				total += duration;
				curDuration = new StringBuilder();
			}
		}
		return total;
	}

	public static String parseStringFromDuration(long duration) {
		String string = "";
		
		int acc = 2;
		
		// TODO REFACTOR
		long second = 1000;
		long minute = second * 60;
		long hour = minute * 60;
		long day = hour * 24;
		long week = day * 7;
		long month = week * 4;
		long year = month * 12;
		
		long curAmount;
		if ((curAmount = duration / year) > 0) {
			string += curAmount + "y";
			duration = duration % year;
			--acc;
		}		
		if ((curAmount = duration / month) > 0) {
			string += curAmount + "M";
			duration = duration % month;
			--acc;
		}
		if ((curAmount = duration / week) > 0 && acc > 0) {
			string += curAmount + "w";
			duration = duration % week;
			--acc;
		}
		if ((curAmount = duration / day) > 0 && acc > 0) {
			string += curAmount + "d";
			duration = duration % day;
			--acc;
		}
		if ((curAmount = duration / hour) > 0 && acc > 0) {
			string += curAmount + "h";
			duration = duration % hour;
			--acc;
		}
		if ((curAmount = duration / minute) > 0 && acc > 0) {
			string += curAmount + "m";
			duration = duration % minute;
			--acc;
		}
		if ((curAmount = duration / second) > 0 && acc > 0) {
			string += curAmount + "s";
			--acc;
		}
		
		return string;
	}

	public Gametype getGametypeByString(String mode) {
		for (Gametype gt : curMatch.keySet()) {
			if (gt.getName().equalsIgnoreCase(mode)) {
				return gt;
			}
		}
		return null;
	}

	public List<Match> playerInMatch(Player player) {
		List<Match> matchlist = new ArrayList<Match>();
		for (Gametype gt : curMatch.keySet()) {
			Match m = playerInMatch(gt, player);
			if (m != null) {
				matchlist.add(m);
			}
		}
		return matchlist;
	}

	public Match playerInActiveMatch(Player player) {
		for (Match m : ongoingMatches) {
			if (m.isInMatch(player)) {
				return m;
			}
		}
		return null;
	}

	public Match playerInMatch(Gametype gametype, Player player) {
		if (curMatch.containsKey(gametype)) {
			if (curMatch.get(gametype).isInMatch(player)) {
				return curMatch.get(gametype);
			}
		}
		return null;
	}

	public List<DiscordRole> getAdminList() {
		List<DiscordRole> list = new ArrayList<DiscordRole>();
		if (roles.containsKey(PickupRoleType.ADMIN)) {
			list.addAll(roles.get(PickupRoleType.ADMIN));
		}
		if (roles.containsKey(PickupRoleType.SUPERADMIN)) {
			list.addAll(roles.get(PickupRoleType.SUPERADMIN));
		}
		return list;
	}

	public List<DiscordRole> getSuperAdminList() {
		List<DiscordRole> list = new ArrayList<DiscordRole>();
		if (roles.containsKey(PickupRoleType.SUPERADMIN)) {
			list.addAll(roles.get(PickupRoleType.SUPERADMIN));
		}
		return list;
	}

	public List<DiscordRole> getRoleByType(PickupRoleType type) {
		if (roles.containsKey(type)) {
			return roles.get(type);
		}
		return new ArrayList<DiscordRole>();
	}

	public List<DiscordChannel> getChannelByType(PickupChannelType type) {
		if (channels.containsKey(type)) {
			return channels.get(type);
		}
		return new ArrayList<DiscordChannel>();
	}

	public Set<PickupRoleType> getRoleTypes() {
		return roles.keySet();
	}

	public Set<PickupChannelType> getChannelTypes() {
		return channels.keySet();
	}

	public Server getServerByID(int id) {
		for (Server s : serverList) {
			if (s.id == id) {
				return s;
			}
		}
		return null;
	}

	public GameMap getMapByName(String name) {
		for (GameMap m : mapList) {
			if (m.name.equals(name)) {
				return m;
			}
		}
		return null;
	}

	public void setLastMapPlayed(Gametype gt, GameMap map) {
		lastMapPlayed.remove(gt);
		lastMapPlayed.put(gt, map);
	}

	public GameMap getLastMapPlayed(Gametype gt) {
		return lastMapPlayed.get(gt);
	}

	public Server setupGTV() {
		for (Server gtv : gtvServerList) {
			if (!gtv.isTaken()) {
				gtv.take();
				return gtv;
			}
		}
		return null;
	}

	public void restartApplication() throws URISyntaxException, IOException
	{
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(PickupBotDiscordMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		/* is it a jar file? */
		if(!currentJar.getName().endsWith(".jar"))
		return;

		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());

		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}

	public void cmdLaunchAC(DiscordInteraction interaction, Player p, int matchId, String ip, String password){
		for (Match match : ongoingMatches) {
			if (match.getID() == matchId) {
				List<Player> playersInMatch = match.getPlayerList();
				for (Player playerInMatch : playersInMatch){
					if (playerInMatch.getDiscordUser().id.equals(p.getDiscordUser().id)){

						JSONObject requestObj = new JSONObject()
								.put("discord_id", Long.parseLong(p.getDiscordUser().id))
								.put("address", ip)
								.put("password", password);

						sendFTWPostRequest(requestObj);
						String response = sendFTWPostRequest(requestObj);
						interaction.respond(response);
						return;
					}
				}
				interaction.respond(Config.ftw_playernotinmatch);
				return;
			}
		}

		interaction.respond(Config.ftw_matchnotfound);
	}

	private synchronized String sendFTWPostRequest(JSONObject content) {
		try {
			byte[] postData       = content.toString().getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;

			URL url = new URL(bot.ftwAPIUrl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("POST");
			c.setRequestProperty("Authorization", bot.ftwAPIkey);
			c.setRequestProperty("charset", "utf-8");
			c.setRequestProperty("Content-Type", "application/json");
			c.setRequestProperty("User-Agent", "Bot");
			c.setDoOutput(true);
			c.setUseCaches(false);
			c.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			try (DataOutputStream wr = new DataOutputStream( c.getOutputStream())) {
				wr.write(postData);
			}
			if (c.getResponseCode() == 200){
				c.disconnect();
				return Config.ftw_success;
			}
			c.disconnect();
			return Config.ftw_notconnected;

		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return Config.ftw_error;
	}
}
