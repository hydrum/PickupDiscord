package de.gost0r.pickupbot.pickup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.server.Server;

public class PickupLogic {
	
	public PickupBot bot;
	public Database db;
	
	private List<Server> serverList;
	private List<GameMap> mapList;

	private List<String> adminRoles;
	
	private List<Match> ongoingMatches; // ongoing matches (live)
	
	private Map<Gametype, Match> curMatch;
	
	private boolean locked;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		db = new Database(this);
		Player.db = db;
		
		// handle db stuff
		ongoingMatches = db.loadOngoingMatches();
		mapList = db.loadMaps();
		serverList = db.loadServers();
		adminRoles = db.loadAdminRoles();
		
		curMatch = new HashMap<Gametype, Match>();
		
		adminRoles.add("401822611694419968"); // pickupadmin
		adminRoles.add("401834352872521739"); // owner
	}
	
	public void cmdAddPlayer(Player player, String mode) {
		Gametype gt = getGametypeByString(mode);
		if (gt != null && curMatch.keySet().contains(gt)) {
			if (!locked) {
				if (curMatch.get(gt).getMatchState() == MatchState.Signup) {
					if (player.isBanned()) {
						if (playerInMatch(player) == null) {
							curMatch.get(gt).addPlayer(player);
						}
					} else bot.sendNotice(player.getDiscordUser(), Config.is_banned);
				} else bot.sendNotice(player.getDiscordUser(), Config.pkup_no_match_avi);
			} else bot.sendNotice(player.getDiscordUser(), Config.pkup_lock);
		} else bot.sendNotice(player.getDiscordUser(), Config.pkup_no_match_avi);
	}
	
	public void cmdRemovePlayer(Player player) {
		if (!locked) {
			Match m = playerInMatch(player);
			if (m != null && m.getMatchState() == MatchState.Signup) {
				m.removePlayer(player);
			} else bot.sendNotice(player.getDiscordUser(), Config.pkup_no_match_avi);
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

	public void cmdGetElo(Player p) {
		String msg = Config.pkup_getelo;
		msg = msg.replace(".urtauth.", p.getUrtauth());
		msg = msg.replace(".elo.", String.valueOf(p.getElo()));
		String elochange = String.valueOf(p.getEloChange());
		if (p.getEloChange() >= 0) {
			elochange = "+" + elochange;
		} else {
			elochange = "-" + elochange;
		}
		msg.replace(".elochange.", elochange);
		bot.sendMsg(bot.getPubchan(), msg);
	}
	
	public void cmdGetMaps() {
		for (Gametype gametype : curMatch.keySet()) {
			bot.sendMsg(bot.getPubchan(), "*" + gametype + "*:" + curMatch.get(gametype).getMapVotes());
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
	}
	
	public void cmdStatus() {
		// TODO
	}
	
	public boolean cmdReset(String cmd) {
		return cmdReset(cmd, null);
	}

	public boolean cmdReset(String cmd, String mode) {
		Gametype gt = getGametypeByString(mode);
		if (cmd.equals("all")) {
			for (Match match : ongoingMatches) {
				match.reset();
			}
			for (Match m : curMatch.values()) {
				m.reset();
			}
			bot.sendMsg(bot.getPubchan(), Config.pkup_reset_all);
			return true;
		} else if (cmd.equals("cur")) {
			if (gt != null && curMatch.keySet().contains(gt)) {
				curMatch.get(mode).reset();
			} else {
				for (Match m : curMatch.values()) {
					m.reset();
				}
			}
			bot.sendMsg(bot.getPubchan(), Config.pkup_reset_cur);
			return true;
		} else {
			try {
				int idx = Integer.valueOf(cmd);
				for (Match match : ongoingMatches) {
					if (match.getID() == idx) {
						match.reset();
						bot.sendMsg(bot.getPubchan(), Config.pkup_reset_id.replace(".id.", cmd));
						return true;
					}
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean cmdEnableMap(String mapname) {
		GameMap map = null;
		for (GameMap xmap : mapList) {
			if (xmap.toString().equals(mapname)) {
				map = xmap;
				break;
			}
		}
		if (map == null) {
			map = new GameMap(mapname, true);
			db.createMap(map);
			mapList.add(map);
		} else {
			map.active = true;
			db.updateMap(map);
		}
		return true;
	}
	
	public boolean cmdDisableMap(String mapname) {
		for (GameMap map : mapList) {
			if (map.toString().equals(mapname)) {
				map.active = false;
				db.updateMap(map);
				return true;
			}
		}
		return false;
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
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
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
	
	
	// HELPER
	
	public Gametype getGametypeByString(String mode) {
		for (Gametype gt : curMatch.keySet()) {
			if (gt.getName().equals(mode)) {
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
