package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.server.Server;

public class PickupLogic {
	
	public PickupBot bot;
	public Database db;
	
	private List<Server> serverList;
	private List<GameMap> mapList;

	private List<String> adminRoles;
	
	private List<Match> ongoingMatches; // ongoing matches (live)
	
	private Queue<Match> awaitingServer;
	
	private Map<Gametype, Match> curMatch;
	
	private boolean locked;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		db = new Database(this);
		Player.db = db;		
		// handle db stuff
		serverList = db.loadServers();
		adminRoles = db.loadAdminRoles();
		
		curMatch = new HashMap<Gametype, Match>();
		for (Gametype gt : db.loadGametypes()) {
			if (gt.getActive()) {
				curMatch.put(gt, null);
			}
		}
		mapList = db.loadMaps(); // needs current gamemode list
		ongoingMatches = db.loadOngoingMatches(); // need maps, servers and gamemodes

		createCurrentMatches();
		
		awaitingServer = new LinkedList<Match>();
		
		adminRoles.add("401821506205646858"); // pickupadmin
		adminRoles.add("309752235611389953"); // owner
	}
	
	public void cmdAddPlayer(Player player, String mode) {
		Gametype gt = getGametypeByString(mode);
		if (gt != null && curMatch.keySet().contains(gt)) {
			if (!locked) {
				if (curMatch.get(gt).getMatchState() == MatchState.Signup) {
					if (!player.isBanned()) {
						if (playerInMatch(player) == null) {
							curMatch.get(gt).addPlayer(player);
						} else bot.sendNotice(player.getDiscordUser(), Config.player_already_added);
					} else bot.sendNotice(player.getDiscordUser(), Config.is_banned);
				} else bot.sendNotice(player.getDiscordUser(), Config.pkup_match_unavi);
			} else bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
		} else bot.sendNotice(player.getDiscordUser(), Config.pkup_match_invalid_gt);
	}
	
	public void cmdRemovePlayer(Player player) {
		if (!locked) {
			Match m = playerInMatch(player);
			if (m != null) {
				if (m.getMatchState() == MatchState.Signup || m.getMatchState() == MatchState.AwaitingServer) {
					m.removePlayer(player);
				} else bot.sendNotice(player.getDiscordUser(), Config.player_cannot_remove);
			} else bot.sendNotice(player.getDiscordUser(), Config.player_already_removed);
		} else bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
	}
	
	public boolean cmdLock() {
		locked = true;
		bot.sendMsg(bot.getPubchan(), Config.lock_enable);
		return true;
	}
	
	public boolean cmdUnlock() {
		locked = false;
		bot.sendMsg(bot.getPubchan(), Config.lock_disable);
		return true;
	}
	
	public void cmdRegisterPlayer(DiscordUser user, String urtauth) {
		// check whether the user and the urtauth aren't taken
		if (Player.get(urtauth) == null && Player.get(user) == null) {
			if (urtauth.matches("^[a-z0-9]*$")) {
				Player p = new Player(user, urtauth);
				db.createPlayer(p);
				bot.sendNotice(user, Config.auth_success);
			} else {
				bot.sendNotice(user, Config.auth_invalid);
			}
		} else {
			bot.sendNotice(user, Config.auth_taken);
		}
	}
	
	public boolean cmdUnregisterPlayer(Player player) {
		Match m = playerInMatch(player);
		if (m != null) {
			if (m.getMatchState() == MatchState.Signup) {
				m.removePlayer(player);
			}
		}
		db.removePlayer(player);
		Player.remove(player);
		return true;
	}

	public void cmdTopElo(int number) {
		String msg = Config.pkup_top5_header;
		
		List<Player> players = db.getTopPlayers(number);
		if (players.isEmpty()) {
			bot.sendMsg(bot.getPubchan(), msg + "  None");
		} else {
			for (Player p : players) {
				msg += "\n" + cmdGetElo(p, false);
			}
			bot.sendMsg(bot.getPubchan(), msg);
		}
	}

	public String cmdGetElo(Player p) {
		return cmdGetElo(p, true);
	}

	public String cmdGetElo(Player p, boolean sendMsg) {
		String msg = Config.pkup_getelo;
		msg = msg.replace(".urtauth.", p.getUrtauth());
		msg = msg.replace(".elo.", String.valueOf(p.getElo()));
		String elochange = "";
		if (p.getEloChange() >= 0) {
			elochange = "+" + String.valueOf(p.getEloChange());
		}
		elochange += String.valueOf(p.getEloChange());
		msg = msg.replace(".elochange.", elochange);
		msg = msg.replace(".rank.", String.valueOf(db.getRankForPlayer(p)));
		if (sendMsg) {
			bot.sendMsg(bot.getPubchan(), msg);
		}
		return msg;
	}
	
	public void cmdGetMaps() {
		for (Gametype gametype : curMatch.keySet()) {
			String msg = Config.pkup_map_list;
			msg = msg.replace(".gametype.", gametype.getName());
			msg = msg.replace(".maplist.", curMatch.get(gametype).getMapVotes());
			bot.sendMsg(bot.getPubchan(), msg);
		}
	}


	public void cmdMapVote(Player player, String mapname) {
		Match m = playerInMatch(player);
		if (m != null && m.getMatchState() == MatchState.Signup) {
			int counter = 0;
			GameMap map = null;
			for (GameMap xmap : m.getMapList()) {
				if (xmap.name.contains(mapname)) {
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
		bot.sendNotice(player.getDiscordUser(), Config.player_already_removed);
	}
	
	public void cmdStatus() {
		if (curMatch.isEmpty()) {
			bot.sendMsg(bot.getPubchan(), Config.pkup_match_unavi);
			return;
		}
		for (Match m : curMatch.values()) {
			cmdStatus(m, null);
		}
	}
	
	public void cmdStatus(Match match, Player player) {
		String msg = "";
		int playerCount = match.getPlayerCount();
		if (playerCount == 0) {
			msg = Config.pkup_status_noone;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
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
			}
			
			msg = msg.replace(".playerlist.", playernames);
			
		} else if (match.getMatchState() == MatchState.AwaitingServer){
			msg = Config.pkup_status_server;
			msg = msg.replace(".gametype.", match.getGametype().getName().toUpperCase());
		}
		bot.sendMsg(bot.getPubchan(), msg);
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
			bot.sendMsg(bot.getPubchan(), Config.pkup_reset_all);
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
			bot.sendMsg(bot.getPubchan(), Config.pkup_reset_cur);
			return true;
		} else {
			Gametype gt = getGametypeByString(cmd);
			if (gt != null) {
				curMatch.get(gt).reset();
				createMatch(gt);
				bot.sendMsg(bot.getPubchan(), Config.pkup_reset_type.replace(".gametype.", gt.getName()));
			} else {
				try {
					int idx = Integer.valueOf(cmd);
					Iterator<Match> iter = ongoingMatches.iterator();
					while (iter.hasNext()) {
						Match match = iter.next();
						if (match.getID() == idx) {
							match.reset();
							ongoingMatches.remove(match);
							bot.sendMsg(bot.getPubchan(), Config.pkup_reset_id.replace(".id.", cmd));
							return true;
						}
					}
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public boolean cmdGetData(DiscordUser user, String id) {
		String msg = "Match not found.";
		try {
			int i_id = Integer.valueOf(id);
			for (Match match : ongoingMatches) {
				if (match.getID() == i_id) {
					msg = Config.pkup_pw;
					msg = msg.replace(".server.", match.getServer().getAddress());
					msg = msg.replace(".password.", match.getServer().password);
					bot.sendMsg(user, msg);
					return true;
				}
			}
		} catch (NumberFormatException e) {
			
		}
		bot.sendMsg(user, msg);
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
				map.setGametype(gt, true);
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
			e.printStackTrace();
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
		
		db.updateGametype(gt);
		return true;
	}
	
	public boolean cmdRemoveGameConfig(String gametype, String command) {
		Gametype gt = getGametypeByString(gametype);
		if (gt == null) return false;
		
		gt.removeConfig(command);
		
		db.updateGametype(gt);
		return true;
	}
	
	public boolean cmdListGameConfig(DiscordUser user, String gametype) {
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
		bot.sendMsg(user, msg);
		
		return true;
//		return !configlist.isEmpty(); // we sent the info anyways, so its fine
	}
	
	public boolean cmdAddServer(String serveraddr, String rcon) {
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
			Server server = new Server(-1, ip, port, rcon, "???", true);
			db.createServer(server);
			serverList.add(server);
			checkServer();
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean cmdServerActivation(String id, boolean active) {
		try {
			int idx = Integer.valueOf(id);
			System.out.println(idx);
			for (Server server : serverList) {
				if (server.id == idx && !server.isTaken() && server.active != active) {
					server.active = active;
					db.updateServer(server);
					checkServer();
					return true;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean cmdServerList(DiscordUser user) {
		String msg = "None";
		for (Server server : serverList) {
			if (msg.equals("None")) {
				msg = server.toString();
			} else {
				msg += "\n" + server.toString();
			}
		}
		bot.sendMsg(user, msg);
		return true;
	}
	
	public boolean cmdMatchList(DiscordUser user) {
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
		bot.sendMsg(user, msg);
		return true;
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
		awaitingServer.add(match);
		checkServer();
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
		for (Server server : serverList) {
			if (server.active && !server.isTaken() && !awaitingServer.isEmpty()) {
				Match m = awaitingServer.poll();
				if (m != null) {
					m.start(server);
				}
			}
		}
	}
	
	// HELPER

	public Gametype getGametypeByString(String mode) {
		for (Gametype gt : curMatch.keySet()) {
			if (gt.getName().equalsIgnoreCase(mode)) {
				return gt;
			}
		}
		return null;
	}
	
	public Match playerInMatch(Player player) {
		for (Match m : curMatch.values()) {
			if (m.isInMatch(player)) {
				return m;
			}
		}
		for (Match m : ongoingMatches) {
			if (m.isInMatch(player)) {
				return m;
			}
		}
		return null;
	}
	
	public boolean isLocked() {
		return locked;
	}

	public List<String> getAdminList() {
		return adminRoles;
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
