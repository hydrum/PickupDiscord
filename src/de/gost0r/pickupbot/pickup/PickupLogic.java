package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordEmbed;
import de.gost0r.pickupbot.discord.DiscordRole;
import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.discord.DiscordUserStatus;
import de.gost0r.pickupbot.discord.api.DiscordAPI;
import de.gost0r.pickupbot.pickup.PlayerBan.BanReason;
import de.gost0r.pickupbot.pickup.server.Server;

public class PickupLogic {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public PickupBot bot;
	public Database db;
	
	private List<Server> serverList;
	private List<GameMap> mapList;

	private Map<PickupRoleType, List<DiscordRole>> roles;
	private Map<PickupChannelType, List<DiscordChannel>> channels;
	
	private List<Match> ongoingMatches; // ongoing matches (live)
	
	private Queue<Match> awaitingServer;
	
	private Map<Gametype, Match> curMatch;
	
	private boolean locked;
	
	private Map<BanReason, String[]> banDuration;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		db = new Database(this);
		Player.db = db;
		// handle db stuff
		
//		db.resetStats();
		
		serverList = db.loadServers();
		roles = db.loadRoles();
		channels = db.loadChannels();
		
		curMatch = new HashMap<Gametype, Match>();
		for (Gametype gt : db.loadGametypes()) {
			if (gt.getActive()) {
				curMatch.put(gt, null);
			}
		}
		mapList = db.loadMaps(); // needs current gamemode list
		ongoingMatches = db.loadOngoingMatches(); // need maps, servers and gamemodes

		createCurrentMatches();
		
		banDuration = new HashMap<BanReason, String[]>();
		banDuration.put(BanReason.NOSHOW, new String[] {"10m", "30m", "1h", "2h", "6h", "12h", "3d", "1w", "2w", "1M"});
		banDuration.put(BanReason.RAGEQUIT, new String[] {"30m", "1h", "2h", "6h", "12h", "3d", "1w", "2w", "1M", "3M"});
		
		awaitingServer = new LinkedList<Match>();
	}
	
	public void cmdAddPlayer(Player player, List<Gametype> modes) {

		if (locked) {
			bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
			return;
		}
		if (player.isBanned()) {
			bot.sendMsg(bot.getLatestMessageChannel(), printBanInfo(player));
			return;
		}
		if (playerInActiveMatch(player) != null) {
			bot.sendNotice(player.getDiscordUser(), Config.player_already_match);
			return;
		}

		String defmsg = "You are already in queue for:";
		String msg = defmsg;
		for (Gametype gt : modes) {
			if (gt != null && curMatch.keySet().contains(gt)) {
				Match m = curMatch.get(gt);
				if (m.getMatchState() != MatchState.Signup || m.isInMatch(player) || playerInActiveMatch(player) != null) {
					msg += " " + gt.getName();
				} else {
					m.addPlayer(player);
				}
			}
		}
		
		if (!msg.equals(defmsg)) {
			bot.sendNotice(player.getDiscordUser(), msg);
		}
	}
	
	public void cmdRemovePlayer(Player player, List<Gametype> modes) {
		if (locked) {
			bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
			return;
		}
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
			if (gt != null && curMatch.keySet().contains(gt)) {
				curMatch.get(gt).removePlayer(player, true); // conditions checked within function
			}
		}
	}
	
	public void cmdPick(Player player, int pick) {
		for (Match match : ongoingMatches) {
			if (match.isCaptainTurn(player)) {
				match.pick(player, pick - 1);
				return;
			}
		}
		bot.sendNotice(player.getDiscordUser(), Config.player_not_captain);
	}
	
	public boolean cmdLock() {
		locked = true;
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.lock_enable);
		return true;
	}
	
	public boolean cmdUnlock() {
		locked = false;
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.lock_disable);
		return true;
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
						bot.sendNotice(user, "Unknown county code. Look yours up here: <https://datahub.io/core/country-list/r/0.html>");
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
			bot.sendMsg(getChannelByType(PickupChannelType.ADMIN), "Unknown county code. Look yours up here: <https://datahub.io/core/country-list/r/0.html>");
		}	
}
	
	public void cmdTopElo(int number) {
		// String msg = Config.pkup_top10_header;
		String embed_rank = "";
		String embed_player = "";
		String embed_elo = "";
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 players";
		embed.color = 7056881;
		int rank = 1;
		
		List<Player> players = db.getTopPlayers(number);
		if (players.isEmpty()) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), "None");
		} else {
			for (Player p : players) {
				// msg += "\n" + cmdGetElo(p, false);
				String country = "";
				if( p.getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  "<:puma:849287183474884628>";
				}
				else {
					country = ":flag_" + p.getCountry().toLowerCase() + ":";
				}
				embed_rank += "**" + String.valueOf(rank) + "**\n";
				embed_player += country + " \u200b \u200b  " +  p.getUrtauth() + '\n';
				embed_elo += p.getRank().getEmoji() + " \u200b \u200b  " + String.valueOf(p.getElo()) + "\n";
				rank++;
			}
			// bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
			embed.addField("\u200b", embed_rank, true);
			embed.addField("Player", embed_player, true);
			embed.addField("Elo", embed_elo, true);
			
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), null, embed);
		}
	}
	
	public void cmdTopWDL(int number) {
		String embed_rank = "";
		String embed_player = "";
		String embed_wdl = "";
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 win rate";
		embed.color = 7056881;
		
		
		Map<Player, Float> topwdl = db.getTopWDL(number);
		if (topwdl.isEmpty()) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), "None");
		} else {
			int rank = 1;
			for (Map.Entry<Player, Float> entry : topwdl.entrySet()) {
				String country = "";
				if( entry.getKey().getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  ":puma:";
				}
				else {
					country = ":flag_" + entry.getKey().getCountry().toLowerCase() + ":";
				}
				embed_rank += "**" + String.valueOf(rank) + "**\n";
				embed_player += country + " \u200b \u200b  " +  entry.getKey().getUrtauth() + '\n';
				embed_wdl += String.valueOf(Math.round(entry.getValue() * 100d)) + " %\n";
				rank++;
			}
			embed.addField("\u200b", embed_rank, true);
			embed.addField("Player", embed_player, true);
			embed.addField("Win rate", embed_wdl, true);
			
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), null, embed);
		}
	}
	
	public void cmdTopKDR(int number) {
		String embed_rank = "";
		String embed_player = "";
		String embed_wdl = "";
		DiscordEmbed embed = new DiscordEmbed();
		embed.title = "Top 10 kill death ratio";
		embed.color = 7056881;
		
		
		Map<Player, Float> topkdr = db.getTopKDR(number);
		if (topkdr.isEmpty()) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), "None");
		} else {
			int rank = 1;
			for (Map.Entry<Player, Float> entry : topkdr.entrySet()) {
				String country = "";
				if( entry.getKey().getCountry().equalsIgnoreCase("NOT_DEFINED")) {
					country =  "<:puma:849287183474884628>";
				}
				else {
					country = ":flag_" + entry.getKey().getCountry().toLowerCase() + ":";
				}
				embed_rank += "**" + String.valueOf(rank) + "**\n";
				embed_player += country + " \u200b \u200b  " +  entry.getKey().getUrtauth() + '\n';
				embed_wdl += String.valueOf(String.format("%.02f", entry.getValue())) + "\n";
				rank++;
			}
			embed.addField("\u200b", embed_rank, true);
			embed.addField("Player", embed_player, true);
			embed.addField("KDR", embed_wdl, true);
			
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), null, embed);
		}
	}

	public String cmdGetElo(Player p) {
		return cmdGetElo(p, true);
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
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
		}
		return msg;
	}
	
	public void cmdTopCountries(int number) {
		String msg = Config.pkup_top5_header;
		
		ArrayList<CountryRank> countries = db.getTopCountries(number);
		if (countries.isEmpty()) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg + "  None");
		} else {
			
			for(int i = 0; i < countries.size(); i++) {
				String ranking = Config.pkup_getelo_country;
				
				ranking = ranking.replace(".position.", Integer.toString(i + 1));
				ranking = ranking.replace(".country.", Country.getCountryFlag(countries.get(i).country));
				ranking = ranking.replace(".elo.", countries.get(i).elo.toString());
				
				msg = msg + "\n" + ranking;
			}
			
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
		}
	}
	
	public void cmdGetMaps() {
		String msg = "None";
		for (Gametype gametype : curMatch.keySet()) {
			if (msg.equals("None")) {
				msg = "";
			} else {
				msg += "\n";
			}
			String mapString = Config.pkup_map_list;
			mapString = mapString.replace(".gametype.", gametype.getName());
			mapString = mapString.replace(".maplist.", curMatch.get(gametype).getMapVotes(false));
			msg += mapString;
		}
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
	}


	public void cmdMapVote(Player player, Gametype gametype, String mapname) {
		if (gametype == null) {
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
		if (curMatch.containsKey(gametype)) {
			Match m = curMatch.get(gametype);
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
				} else {
					m.voteMap(player, map); // handles sending a msg itself
				}
			}
		}
	}
	
	public void cmdStatus() {
		if (curMatch.isEmpty()) {
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_match_unavi);
			return;
		}
		String msg = "None";
		for (Match m : curMatch.values()) {
			if (msg.equals("None")) {
				msg = "";
			} else {
				msg += "\n";
			}
			msg += cmdStatus(m, null, false);
		}
		bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), msg);
	}
	
	public String cmdStatus(Match match, Player player, boolean shouldSend) {
		String msg = "";
		int playerCount = match.getPlayerCount();
		if (playerCount == 0 && player == null) {
			msg = Config.pkup_status_noone;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
			msg = msg.replace("<gametype>", match.getGametype().getName().toLowerCase());
		} else if (match.getMatchState() == MatchState.Signup){
			msg = Config.pkup_status_signup;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
			msg = msg.replace(".playernumber.", String.valueOf(playerCount));
			msg = msg.replace(".maxplayer.", String.valueOf(match.getGametype().getTeamSize() * 2));

			String playernames = "None";
			if (player == null) {
				for (Player p : match.getPlayerList()) {
					if (playernames.equals("None")) {
						playernames = p.getUrtauth();
					} else {
						playernames += " " + p.getUrtauth();
					}
				}
			} else {
				playernames = player.getDiscordUser().getMentionString();
				playernames += (match.isInMatch(player)) ? " added." : " removed.";
			}
			
			msg = msg.replace(".playerlist.", playernames);
			
		} else if (match.getMatchState() == MatchState.AwaitingServer){
			msg = Config.pkup_status_server;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
		}
		if (shouldSend) {
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

	public boolean cmdReset(String cmd) {
		return cmdReset(cmd, null);
	}

	public boolean cmdReset(String cmd, String mode) {
		if (cmd.equals("all")) {
			Iterator<Match> iter = ongoingMatches.iterator();
			List<Match> toRemove = new ArrayList<Match>();
			while (iter.hasNext()) {
				Match match = iter.next();
				match.reset();
				toRemove.add(match);
			}
			ongoingMatches.removeAll(toRemove);
			for (Match m : curMatch.values()) {
				m.reset();
				createCurrentMatches();
			}
			bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_all);
			return true;
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
			return true;
		} else {
			Gametype gt = getGametypeByString(cmd);
			if (gt != null) {
				curMatch.get(gt).reset();
				createMatch(gt);
				bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_type.replace(".gametype.", gt.getName()));
			} else {
				try {
					int idx = Integer.valueOf(cmd);
					Iterator<Match> iter = ongoingMatches.iterator();
					while (iter.hasNext()) {
						Match match = iter.next();
						if (match.getID() == idx) {
							match.reset();
							ongoingMatches.remove(match);
							bot.sendMsg(getChannelByType(PickupChannelType.PUBLIC), Config.pkup_reset_id.replace(".id.", cmd));
							return true;
						}
					}
					
				} catch (NumberFormatException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
				}
			}
		}
		return false;
	}
	
	public boolean cmdGetData(DiscordUser user, String id, DiscordChannel channel) {
		String msg = "Match not found.";
		try {
			int i_id = Integer.valueOf(id);
			for (Match match : ongoingMatches) {
				if (match.getID() == i_id) {
					msg = Config.pkup_pw;
					msg = msg.replace(".server.", match.getServer().getAddress());
					msg = msg.replace(".password.", match.getServer().password);
					bot.sendMsg(channel, msg);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		bot.sendMsg(channel, msg);
		return false;
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
			int i_teamSize = Integer.valueOf(teamSize);
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
		
		String configlist = "";
		for (String config : gt.getConfig()) {
			if (!configlist.isEmpty()) {
				configlist += "\n";
			}
			configlist += config;
		}
		
		String msg = Config.pkup_config_list;
		msg = msg.replace(".gametype.", gt.getName());
		msg = msg.replace(".configlist.", configlist);
		bot.sendMsg(channel, msg);
		
		return true;
//		return !configlist.isEmpty(); // we sent the info anyways, so its fine
	}
	
	public boolean cmdAddServer(String serveraddr, String rcon, String str_region) {
		try {
			String ip = serveraddr;
			int port = 27960;
			if (serveraddr.contains(":")) {
				String[] servers = serveraddr.split(":");
				ip = servers[0];
				port = Integer.valueOf(servers[1]);
			}
			for (Server s : serverList) {
				if (s.IP.equals(ip) && s.port == port) {
					return false;
				}
			}
			
			Region region = Region.valueOf(str_region);
			
			if(region != null) {
				Server server = new Server(-1, ip, port, rcon, "???", true, region);
				
				db.createServer(server);
				serverList.add(server);
				checkServer();
				return true;
			}
			else {
				return false;
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			return false;
		} catch (IllegalArgumentException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			return false;
		}
	}

	public boolean cmdServerActivation(String id, boolean active) {
		try {
			int idx = Integer.valueOf(id);
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
		}
		return false;
	}

	public boolean cmdServerChangeRcon(String id, String rcon) {
		try {
			int idx = Integer.valueOf(id);
			for (Server server : serverList) {
				if (server.id == idx) {
					server.rconpassword = rcon;
					db.updateServer(server);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return false;
	}

	public boolean cmdServerSendRcon(String id, String rconString) {
		try {
			int idx = Integer.valueOf(id);
			for (Server server : serverList) {
				if (server.id == idx) {
					server.sendRcon(rconString);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return false;
	}
	
	public boolean cmdServerList(DiscordChannel channel) {
		String msg = "None";
		for (Server server : serverList) {
			if (msg.equals("None")) {
				msg = server.toString();
			} else {
				msg += "\n" + server.toString();
			}
		}
		bot.sendMsg(channel, msg);
		return true;
	}
	
	public boolean cmdMatchList(DiscordChannel channel) {
		String msg = "None";
		for (Match match : curMatch.values()) {
			if (msg.equals("None")) {
				msg = match.toString();
			} else {
				msg += "\n" + match.toString();
			}
		}
		for (Match match : ongoingMatches) {
			if (msg.equals("None")) {
				msg = match.toString();
			} else {
				msg += "\n" + match.toString();
			}
		}
		bot.sendMsg(channel, msg);
		return true;
	}
	
	public boolean cmdLive() {
		String msg = "No live matches found.";
		for (Match match : ongoingMatches) {
			if (msg.equals("No live matches found.")) {
				msg = match.getMatchInfo();
			} else {
				msg += "\n" + match.getMatchInfo();
			}
		}
		bot.sendMsg(bot.getLatestMessageChannel(), msg);
		return true;
	}
	
	public boolean cmdDisplayMatch(String matchid) {
		try {
			int idx = Integer.valueOf(matchid);
			for (Match match : ongoingMatches) {
				if (match.getID() == idx) {
					bot.sendMsg(bot.getLatestMessageChannel(), match.getMatchInfo());
					return true;
				}
			}
			
			Match match = db.loadMatch(idx); // TODO: cache?
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return true;
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
		return false;
	}
	
	public boolean cmdDisplayLastMatch() {
		try {
			Match match = db.loadLastMatch(); 
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return true;
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
		return false;
	}
	
	public boolean cmdDisplayLastMatchPlayer(Player p) {
		try {
			Match match = db.loadLastMatchPlayer(p); 
			if (match != null) {
				bot.sendMsg(bot.getLatestMessageChannel(), null, match.getMatchEmbed());
				return true;
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		bot.sendMsg(bot.getLatestMessageChannel(), "Match not found.");
		return false;
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
		if (awaitingServer.contains(match)) {
			awaitingServer.remove(match);
		}
	}

	public void matchStarted(Match match) {
		createMatch(match.getGametype());
		ongoingMatches.add(match);
	}

	public void matchEnded(Match match) {
//		matchRemove(match); // dont remove as this can be called while in a loop
		checkServer();
	}
	
	public void matchRemove(Match match) {
		if (ongoingMatches.contains(match)) {
			ongoingMatches.remove(match);
		}
	}

	private void checkServer() {
		if(!awaitingServer.isEmpty())
		{
			Match m = awaitingServer.poll();
			
			if(m != null)
			{
				Server bs = getBestServer(m.getPreferredServerRegion());
				
				if(bs != null && bs.active && !bs.isTaken() && m.getMatchState() == MatchState.AwaitingServer)
				{
					m.launch(bs);
				}
				else
				{
					for (Server server : serverList) {
						if (server.active && !server.isTaken() && m.getMatchState() == MatchState.AwaitingServer) {	
								m.launch(server);
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
		long endTime = -1L;
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
				PlayerBan ban = new PlayerBan();
				ban.player = player;
				
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
		
		String curDuration = "";
		for (int i = 0; i < string.length(); ++i) {
			if (Character.isDigit(string.charAt(i))) {
				curDuration += String.valueOf(string.charAt(i));
			} else {
				long duration = Long.valueOf(curDuration);
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
				curDuration = "";
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
		if ((curAmount = duration / year) > 0 && acc > 0) {
			string += curAmount + "y";
			duration = duration % year;
			--acc;
		}		
		if ((curAmount = duration / month) > 0 && acc > 0) {
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
			duration = duration % second;
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
	
	public boolean isLocked() {
		return locked;
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

}
