package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.List;

import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.server.Server;

public class PickupLogic {
	
	public PickupBot bot;
	
	private List<Server> serverList;
	private List<GameMap> mapList;
	
	private List<Match> curMatches; // ongoing matches (live)
	
	private Match curMatch;
	
	public boolean locked;
	
	public PickupLogic(PickupBot bot) {
		this.bot = bot;
		
		// handle db stuff
		curMatches = new ArrayList<Match>();// db.loadCurMatches
		mapList = new ArrayList<GameMap>();// db.loadMapList
		serverList = new ArrayList<Server>();// db.loadServerList
	}
	
	public void cmdAddPlayer(Player player) {
		curMatch.addPlayer(player);
	}
	
	public void removePlayer(Player player) {
		curMatch.removePlayer(player);
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

	public void gameGetElo(String urtauth) {
		Player player = Player.get(urtauth);		
		if (player != null) {
			String msg = Config.pkup_getelo;
			msg = msg.replace(".urtauth.", urtauth);
			msg = msg.replace(".elo.", String.valueOf(player.getElo()));
			String elochange = String.valueOf(player.getEloChange());
			if (player.getEloChange() >= 0) {
				elochange = "+" + elochange;
			} else {
				elochange = "-" + elochange;
			}
			msg.replace(".elochange.", elochange);
			bot.sendMsg(bot.getPubchan(), msg);
			
		} else {
			bot.sendMsg(bot.getPubchan(), Config.player_not_found);
		}
	}
	
	public void gameGetMaps() {
		bot.sendMsg(bot.getPubchan(), curMatch.getMapVotes());
	}


	public void gameMapVote(Player player, String mapname) {
		if (curMatch.getMatchState() == MatchState.Signup
				&& curMatch.isInMatch(player)) {
			int counter = 0;
			GameMap map = null;
			for (GameMap xmap : curMatch.getMapList()) {
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
				curMatch.voteMap(player, map);
			}
		}
	}

	public void gameReset(String cmd) {
		if (cmd.equals("all")) {
			for (Match match : curMatches) {
				match.reset();
			}
			curMatch.reset();
			
		} else if (cmd.equals("cur")) {
			curMatch.reset();
		} else {
			try {
				int idx = Integer.valueOf(cmd);
				for (Match match : curMatches) {
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
	
	public boolean isLocked() {
		return locked;
	}

}
