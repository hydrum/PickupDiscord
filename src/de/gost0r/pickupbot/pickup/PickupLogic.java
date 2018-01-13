package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.server.Server;

public class PickupLogic {
	
	public PickupBot bot;
	
	private List<Server> serverList;
	private List<GameMap> mapList;

	private List<String> adminRoles;
	
	private List<Match> ongoingMatches; // ongoing matches (live)
	
	private Map<Gametype, Match> curMatch;
	
	public boolean locked;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		// handle db stuff
		ongoingMatches = new ArrayList<Match>();// db.loadOngoingMatches
		mapList = new ArrayList<GameMap>();// db.loadMapList
		serverList = new ArrayList<Server>();// db.loadServerList
		curMatch = new HashMap<Gametype, Match>(); // db.loadcurmatch
		adminRoles = new ArrayList<String>(); // db.loadadmins
		
		adminRoles.add("401822611694419968"); // pickupadmin
		adminRoles.add("401834352872521739"); // owner
	}
	
	public void gameAddPlayer(Player player, String mode) {
		Gametype gt = getGametypeByString(mode);
		if (gt != null && curMatch.keySet().contains(gt)) {
//			curMatch.addPlayer(player);
		}
	}
	
	public void gameRemovePlayer(Player player) {
//		curMatch.removePlayer(player);
	}
	
	public void cmdLock() {
		locked = true;
		bot.sendMsg(bot.getPubchan(), Config.lock_enable);
	}
	
	public void cmdUnlock() {
		locked = false;
		bot.sendMsg(bot.getPubchan(), Config.lock_disable);
	}
	
	public void cmdRegisterPlayer(DiscordUser user, String urtauth) {
		// check whether the user and the urtauth aren't taken
		if (Player.get(urtauth) == null) {
			if (urtauth.matches("^[a-z0-9]*$")) {
				Player p = new Player(user, urtauth);
				// TODO: store p in db
				bot.sendNotice(user, Config.auth_success);
			} else {
				bot.sendNotice(user, Config.auth_invalid);
			}
		} else {
			bot.sendNotice(user, Config.auth_taken);
		}
	}

	public void gameGetElo(Player p) {
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
	
	public void gameGetMaps() {
		for (Gametype gametype : curMatch.keySet()) {
			bot.sendMsg(bot.getPubchan(), "*" + gametype + "*:" + curMatch.get(gametype).getMapVotes());
		}
	}


	public void gameMapVote(Player player, String mapname) {
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
				// handles sending a msg itself
				m.voteMap(player, map);
			}
		}
	}
	
	public void gameReset(String cmd) {
		gameReset(cmd, null);
	}

	public void gameReset(String cmd, String mode) {
		Gametype gt = getGametypeByString(mode);
		if (cmd.equals("all")) {
			for (Match match : ongoingMatches) {
				match.reset();
			}
			for (Match m : curMatch.values()) {
				m.reset();
			}
			
		} else if (cmd.equals("cur")) {
			if (gt != null && curMatch.keySet().contains(gt)) {
				curMatch.get(mode).reset();
			} else {
				for (Match m : curMatch.values()) {
					m.reset();
				}
			}
		} else {
			try {
				int idx = Integer.valueOf(cmd);
				for (Match match : ongoingMatches) {
					if (match.getID() == idx) {
						match.reset();
						break;
					}
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	
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
		return null;
	}
	
	public boolean isLocked() {
		return locked;
	}

	public List<String> getAdminList() {
		return adminRoles;
	}

}
