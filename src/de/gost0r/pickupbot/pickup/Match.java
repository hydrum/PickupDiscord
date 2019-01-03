package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.pickup.MatchStats.Status;
import de.gost0r.pickupbot.pickup.server.Server;
import de.gost0r.pickupbot.pickup.server.ServerMonitor.ServerState;

public class Match implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Gametype gametype;
	private MatchState state;
	private int id;

	private Map<String, List<Player>> teamList;
	private Map<GameMap, Integer> mapVotes;
	private Map<Player, MatchStats> playerStats = new HashMap<Player, MatchStats>();
	
	private Server server;
	private GameMap map;
	private int[] elo = new int[2];	
	private int[] score = new int[2];
	
	private int[] surrender;
	
	private long startTime;
	
	private PickupLogic logic;
	
	private Match() {
		teamList = new HashMap<String, List<Player>>();
		teamList.put("red", new ArrayList<Player>());
		teamList.put("blue", new ArrayList<Player>());
		mapVotes = new HashMap<GameMap, Integer>();
	}
	
	public Match(PickupLogic logic, Gametype gametype, List<GameMap> maplist) {
		this();
		this.logic = logic;
		this.gametype = gametype;

		for (GameMap m : maplist) {
			mapVotes.put(m, 0);
		}
		state = MatchState.Signup;

		surrender = new int[] { gametype.getTeamSize() - 1, gametype.getTeamSize() - 1};
	}

	public Match(int id, long startTime, GameMap map, int[] score, int[] elo,
			Map<String, List<Player>> teamList, MatchState state, Gametype gametype, Server server,
			Map<Player, MatchStats> playerStats) {
		this();
		this.id = id;
		this.startTime = startTime;
		this.map = map;
		this.score = score;
		this.elo = elo;
		this.teamList = teamList;
		this.state = state;
		this.gametype = gametype;
		this.server = server;
		this.playerStats = playerStats;
		
		if (!isOver()) {
			server.startMonitoring(this);
		}

		surrender = new int[] { gametype.getTeamSize() - 1, gametype.getTeamSize() - 1};
	}

	public void reset() {
		if (state == MatchState.Signup) {
			resetSignup();
		} else if (state == MatchState.AwaitingServer) {
			resetAwaitingServer();
		} else if (state == MatchState.Live) {
			resetLive();
		} else if (isOver()) {
			// do nothing
		}
	}
	
	private void resetSignup() {
		// reset mapvote
		for(Player p : playerStats.keySet()) {
			p.resetVotes();
		}
		for (GameMap m : mapVotes.keySet()) {
			mapVotes.put(m, 0);
		}
		
		playerStats.clear();
	}
	
	private void resetAwaitingServer() {
		logic.cancelRequestServer(this);
		resetSignup();
		state = MatchState.Signup;
	}
	
	private void resetLive() {
		abort();
	}

	public void addPlayer(Player player) {
		if (state == MatchState.Signup && !isInMatch(player)) {
			playerStats.put(player, new MatchStats());
			checkReadyState(player);
		}
	}

	public void removePlayer(Player player, boolean shouldSpam) {
		if ((state == MatchState.Signup || state == MatchState.AwaitingServer) && isInMatch(player)) {
			GameMap map = player.getVotedMap(gametype);
			if (map != null) {
				mapVotes.put(map, mapVotes.get(map).intValue() - 1);
				player.voteMap(gametype, null);
			}
			playerStats.remove(player);
			checkServerState();
			logic.cmdStatus(this, player, shouldSpam);
		}
	}

	public void voteMap(Player player, GameMap map) {
		if ((state == MatchState.Signup || state == MatchState.AwaitingServer) && isInMatch(player)) {
			GameMap oldMap = player.getVotedMap(gametype);
			if (oldMap != null) {
				mapVotes.put(oldMap, mapVotes.get(oldMap).intValue() - 1);
				player.voteMap(gametype, null);
			}
			mapVotes.put(map, mapVotes.get(map).intValue() + 1);
			player.voteMap(gametype, map);
			String msg = Config.pkup_map;
			msg = msg.replace(".map.", map.name);
			logic.bot.sendNotice(player.getDiscordUser(), msg);
		} else {
			logic.bot.sendNotice(player.getDiscordUser(), Config.map_cannot_vote);
		}
	}
	
	public void voteSurrender(Player player) {
		long timeUntilSurrender = (startTime + 600000L) - System.currentTimeMillis(); // 10min in milliseconds
		if (timeUntilSurrender < 0) {
			if (!player.hasVotedSurrender()) {
				player.voteSurrender();
				int teamnum = getTeam(player).equals("red") ? 0 : 1;
				surrender[teamnum]--;
				checkSurrender();
				if (!isOver()) {
					String msg = Config.pkup_surrender_cast;
					msg = msg.replace(".num.", String.valueOf(surrender[teamnum]));
					msg = msg.replace(".s.", surrender[teamnum] > 1 ? "s" : "");
					logic.bot.sendNotice(player.getDiscordUser(), msg);
				}
			} else {
				logic.bot.sendNotice(player.getDiscordUser(), Config.player_already_surrender);
			}
		} else {
			timeUntilSurrender /= 1000d;
			String min = String.valueOf((int) (Math.floor(timeUntilSurrender / 60d)));
			String sec = String.valueOf((int) (Math.floor(timeUntilSurrender % 60d)));
			min = min.length() == 1 ? "0" + min : min;
			sec = sec.length() == 1 ? "0" + sec : sec;
			String msg = Config.pkup_surrender_time;
			msg = msg.replace(".time.", min + ":" + sec);
			logic.bot.sendNotice(player.getDiscordUser(), msg);
		}
	}
	
	public void checkSurrender() {
		for (int i = 0; i < 2; ++i) {
			if (surrender[i] <= 0) {
				state = MatchState.Surrender;
				try {
					server.getServerMonitor().surrender(i);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
				}
				cleanUp();
				sendAftermath();
				logic.matchRemove(this);
				logic.db.saveMatch(this);
			}
		}
	}
	
	public void checkReadyState(Player player) {
		if (playerStats.keySet().size() == gametype.getTeamSize() * 2) {
			state = MatchState.AwaitingServer;
			logic.cmdStatus(this, null, true);
			logic.requestServer(this);
		} else {
			logic.cmdStatus(this, player, true);
		}
	}
	
	public void checkServerState() {
		if (state == MatchState.AwaitingServer && playerStats.keySet().size() != gametype.getTeamSize() * 2) {
			state = MatchState.Signup;
			logic.cancelRequestServer(this);
		}
	}

	public void abort() {
		state = MatchState.Abort;
		cleanUp();
		logic.db.saveMatch(this);
	}

	public void abandon(Status status, List<Player> involvedPlayers) {
		state = MatchState.Abandon;
		cleanUp();
		sendAftermath(status, involvedPlayers);
		logic.matchRemove(this);
		logic.db.saveMatch(this);
	}

	public void end() {
		state = MatchState.Done;
		cleanUp();
		sendAftermath();
		logic.matchRemove(this);
		logic.db.saveMatch(this);
	}

	public void cancelStart() {
		state = MatchState.Abort;
		cleanUp();
		logic.matchStarted(this);
		logic.matchRemove(this);
	}
	
	private void cleanUp() {
		for(Player p : playerStats.keySet()) {
			p.resetVotes();
		}
		server.free();

		logic.matchEnded(this);
	}
	
	private void sendAftermath() {
		String fullmsg = Config.pkup_aftermath_head;
		fullmsg = fullmsg.replace(".gametype.", gametype.getName());
		fullmsg = fullmsg.replace(".gamenumber.", String.valueOf(id));
		fullmsg = fullmsg.replace(".map.", map.name);
		for (int i = 0; i < 2; ++i) {
    		int team = i;
    		int opp = (team + 1) % 2;
    		String teamname = (team == 0) ? "Red" : "Blue";
			String msg = Config.pkup_aftermath_result;
			msg = msg.replace(".team.", teamname);
			if (score[team] > score[opp]) {
				msg = msg.replace(".result.", "won");
			} else if (score[team] < score[opp]) {
				msg = msg.replace(".result.", "lost");
			} else {
				msg = msg.replace(".result.", "draw");
			} 
			msg = msg.replace(".score.", score[team] + "-" + score[opp]);
			
			for (Player p : teamList.get(teamname.toLowerCase())) {
				String msgelo = Config.pkup_aftermath_player;
				msgelo = msgelo.replace(".player.", p.getDiscordUser().getMentionString());
				String elochange = ((p.getEloChange() >= 0) ? "+" : "") + String.valueOf(p.getEloChange());
				msgelo = msgelo.replace(".elochange.", elochange);
				msg += " " + msgelo;
			}
			fullmsg += "\n" + msg;
		}
		for (Player player : getPlayerList()) {
			if (player.didChangeRank()) {
				String msg = Config.pkup_aftermath_rank;
				msg = msg.replace(".player.", player.getDiscordUser().getMentionString());
				msg = msg.replace(".updown.", player.getEloChange() > 0 ? "up" : "down");
				msg = msg.replace(".rank.", player.getRank().getEmoji());
				fullmsg += "\n" + msg;
			}
		}
		
		logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), fullmsg);
	}
	
	private void sendAftermath(Status status, List<Player> involvedPlayers) {
		String fullmsg = Config.pkup_aftermath_head;
		fullmsg = fullmsg.replace(".gametype.", gametype.getName());
		fullmsg = fullmsg.replace(".gamenumber.", String.valueOf(id));
		fullmsg = fullmsg.replace(".map.", map.name);

		String msg = Config.pkup_aftermath_abandon_1;
		msg = msg.replace(".reason.", status.name());
		fullmsg += "\n" + msg;
		
		if (!involvedPlayers.isEmpty()) { // only if we have people who left	
			String playerlist = "";
			for (Player player : involvedPlayers) {
				if (!playerlist.isEmpty()) {
					playerlist += " ";
				}
				playerlist += player.getUrtauth();
			}
			String be = involvedPlayers.size() == 1 ? "was" : "were";
			
			msg = Config.pkup_aftermath_abandon_2;
			msg = msg.replace(".players.", playerlist);
			msg = msg.replace(".be.", be);
			fullmsg += "\n" + msg;
		}
		
		logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), fullmsg);
	}
	
	public void launch(Server server) {

		if (!isOver() && state != MatchState.Live) {
			
			this.server = server;
			server.take();
			
			state = MatchState.Live;
			
			for (Player player : getPlayerList()) {				
				for (Match m : logic.playerInMatch(player)) {
					if (m == this) continue;
					m.removePlayer(player, false);
				}
			}
			
			logic.matchStarted(this);
			
			logic.cmdStatus();
			
			// do important changes that affect possibly other matches/servers/playerlists outside the thread!
			
			new Thread(this).start();
			
		}
	}
	

	// DONT CALL THIS OUTSIDE OF launch() !!!
	public void run() {			
		startTime = System.currentTimeMillis();
		
		Random rand = new Random();
		int password = rand.nextInt((999999-100000) + 1) + 100000;
		server.password = String.valueOf(password);
		LOGGER.info("Password: " + server.password);

		// Get most voted map
		List<GameMap> mapList = getMostMapVotes();
		if (mapList.size() == 0) {
			logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), "ERROR: NO MAP FOR GAMETYPE");
			cancelStart();
			return;
		}
		this.map = mapList.size() == 1 ? mapList.get(0) : mapList.get(rand.nextInt(mapList.size()-1));
		LOGGER.info("Map: " + this.map.name);

		// Sort players by elo
		List<Player> playerList = new ArrayList<Player>();
		for (Player p : playerStats.keySet()) {
			playerList.add(p);
		}
		List<Player> sortPlayers = new ArrayList<Player>();
		sortPlayers.add(playerList.get(0));
		for (Player player : playerList) {
			for (Player sortplayer : sortPlayers) {
				if (player.equals(sortplayer)) continue;
				else if (player.getElo() >= sortplayer.getElo()) {
					sortPlayers.add(sortPlayers.indexOf(sortplayer), player);
					break;
				}
			}
			if (!sortPlayers.contains(player)) {
				sortPlayers.add(player);
			}
		}

		// 5: i=0,1 => red: (0, 9) (2, 7) -> blue: (1, 8) (3, 6)   if cond: (4, 5)
		// 4: i=0,1 => red: (0, 7) (2, 5) -> blue: (1, 7) (3, 4)   if cond: none
		// 3: i=0   => red: (0, 5)        -> blue: (1, 4)          if cond: (2, 3)
		// 2: i=0   => red: (0, 3)        -> blue: (1, 2)          if cond: none
		// 1: i=-   => red: none          -> blue: none            if cond: (0, 1)
		for (int i = 0; i < Math.floor(gametype.getTeamSize() / 2); ++i) {
			teamList.get("red").add(sortPlayers.get(i*2));
			teamList.get("red").add(sortPlayers.get(((gametype.getTeamSize()*2)-1)-i*2));

			teamList.get("blue").add(sortPlayers.get(i*2+1));
			teamList.get("blue").add(sortPlayers.get(((gametype.getTeamSize()*2)-1)-i*2-1));
		}
		
		if ((gametype.getTeamSize() % 2) == 1) {
			int better = gametype.getTeamSize() - 1;
			int worse = gametype.getTeamSize();
			
			// compute avg elo up till now
			elo = new int[] {0, 0};
			for (Player p : teamList.get("red")) elo[0] += p.getElo();
			for (Player p : teamList.get("blue")) elo[1] += p.getElo();
			elo[0] /= Math.max(1, gametype.getTeamSize() - 1);
			elo[1] /= Math.max(1, gametype.getTeamSize() - 1);

			if (elo[0] > elo[1]) {
				teamList.get("red").add(sortPlayers.get(worse));
				teamList.get("blue").add(sortPlayers.get(better));
			} else {
				teamList.get("red").add(sortPlayers.get(better));
				teamList.get("blue").add(sortPlayers.get(worse));
			}
		}
		
		// avg elo
		elo = new int[] {0, 0};
		for (Player p : teamList.get("red")) elo[0] += p.getElo();
		for (Player p : teamList.get("blue")) elo[1] += p.getElo();
		elo[0] /= gametype.getTeamSize();
		elo[1] /= gametype.getTeamSize();

		LOGGER.info("Team Red: " + elo[0] + " " + Arrays.toString(teamList.get("red").toArray()));
		LOGGER.info("Team Blue: " + elo[1] + " " + Arrays.toString(teamList.get("blue").toArray()));
		
		id = logic.db.createMatch(this);
		
		// MESSAGE HYPE
		
		
//			String msg = Config.pkup_go_admin.replace(".elored.", String.valueOf(elo[0]));
//			msg = msg.replace(".eloblue.", String.valueOf(elo[1]));
//			msg = msg.replace(".gamenumber.", String.valueOf(id));
//			msg = msg.replace(".password.", server.password);
//			msg = msg.replace(".map.", this.map.name);
//			logic.bot.sendMsg(logic.bot.adminchan, msg);
		
		String fullmsg = "";
		
		String msg = Config.pkup_go_pub_head;
		msg = msg.replace(".elo.", String.valueOf((elo[0] + elo[1])/2));
		msg = msg.replace(".gamenumber.", String.valueOf(id));
		msg = msg.replace(".gametype.", gametype.getName());
		fullmsg = msg;
		
		msg = Config.pkup_map_list;
		msg = msg.replace(".gametype.", gametype.getName());			
		if (getMostMapVotes().size() == mapVotes.keySet().size()) {
			msg = msg.replace(".maplist.", "NO VOTES - RANDOM!");
		}
		else {
			msg = msg.replace(".maplist.", getMapVotes(true));
		}
		fullmsg += "\n" + msg;
		
		String[] teamname = {"Red", "Blue"};
		for (String team : teamname) {
			String playernames = "";
			for (Player p : teamList.get(team.toLowerCase())) {
				if (!playernames.equals("")) {
					playernames += " ";
				}
				playernames += p.getDiscordUser().getMentionString();
			}
			msg = Config.pkup_go_pub_team;
			msg = msg.replace(".team.", team);
			msg = msg.replace(".gametype.", gametype.getName());
			msg = msg.replace(".playerlist.", playernames);
			fullmsg += "\n" + msg;
		}
		
		msg = Config.pkup_go_pub_map;
		msg = msg.replace(".map.", this.map.name);
		msg = msg.replace(".gametype.", gametype.getName());
		fullmsg += "\n" + msg;

		msg = Config.pkup_go_pub_calm;
		msg = msg.replace(".gametype.", gametype.getName());
		fullmsg += "\n" + msg;
		
		logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), fullmsg);

		msg = Config.pkup_go_player;
		msg = msg.replace(".server.", server.getAddress());
		msg = msg.replace(".password.", server.password);
		for (String team : teamList.keySet()) {
			for (Player player : teamList.get(team)) {
				String msg_t = msg.replace(".team.", team.toUpperCase());
				logic.bot.sendMsg(player.getDiscordUser(), msg_t);
			}
		}
		
		msg = Config.pkup_go_pub_sent;
		msg = msg.replace(".gametype.", gametype.getName());
		logic.bot.sendMsg(logic.getChannelByType(PickupChannelType.PUBLIC), msg);

		// set server data
		for (String s : this.gametype.getConfig()) {
			server.sendRcon(s);
		}
		server.sendRcon("g_password " + server.password);
		server.sendRcon("map " + this.map.name);
		server.sendRcon("g_warmup 10");
		
		server.startMonitoring(this);;
	}
	
	List<GameMap> getMostMapVotes() {
		List<GameMap> mapList = new ArrayList<GameMap>();
		int currentVotes = -1;
		for (GameMap map : mapVotes.keySet()) {
			if (currentVotes == -1) {
				mapList.add(map);
				currentVotes = mapVotes.get(map);
				continue;
			}

			if (mapVotes.get(map) > currentVotes) {
				mapList.clear();
				mapList.add(map);
				currentVotes = mapVotes.get(map);
			} else if (mapVotes.get(map) == currentVotes) {
				mapList.add(map);
			}
		}
		return mapList;
	}
	
	public String getMapVotes(boolean skipNull) {
		
		List<GameMap> mostMapVotes = getMostMapVotes();
		String msg = "None";
		for (GameMap map : mapVotes.keySet()) {
			if (skipNull && mapVotes.get(map) == 0) continue;
			if (msg.equals("None")) {
				msg = "";				
			} else {
				msg += " || ";
			}
			String mapString = map.name + ": " + String.valueOf(mapVotes.get(map));
			if (mostMapVotes.size() < mapVotes.keySet().size() && mostMapVotes.contains(map)) {
				mapString = "**" + mapString + "**";
			}
			msg += mapString;
		}
		return msg;
	}
	
	public boolean isOver() {
		return state == MatchState.Done || state == MatchState.Abort || state == MatchState.Abandon || state == MatchState.Surrender;
	}
	
	public MatchState getMatchState() {
		return state;
	}
	
	public Gametype getGametype() {
		return gametype;
	}
	
	public boolean isInMatch(Player player) {
		return playerStats.containsKey(player);
	}

	public Set<GameMap> getMapList() {
		return mapVotes.keySet();
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public void setLogic(PickupLogic logic) {
		this.logic = logic;
	}

	public PickupLogic getLogic() {
		return logic;
	}

	public Server getServer() {
		return server;
	}

	public long getStartTime() {
		return startTime;
	}

	public GameMap getMap() {
		return map;
	}
	
	public int[] getElo() {
		return elo;
	}

	public int getEloRed() {
		return elo[0];
	}

	public int getEloBlue() {
		return elo[1];
	}
	
	public void setScore(int[] score) {
		this.score = score;
	}

	public List<Player> getTeamRed() {
		return teamList.get("red");
	}

	public List<Player> getTeamBlue() {
		return teamList.get("blue");
	}

	public List<Player> getPlayerList() {
		List<Player> list = new ArrayList<Player>();
		for (Player p : playerStats.keySet()) {
			list.add(p);
		}
		return list;
	}

	public String getTeam(Player player) {
		for(String team : teamList.keySet()) {
			if (teamList.get(team).contains(player)) {
				return team;
			}
		}
		return null;
	}

	public int getScoreRed() {
		return score[0];
	}
	
	public int getScoreBlue() {
		return score[1];
	}

	public MatchStats getStats(Player player) {
		return playerStats.get(player);
	}

	public int getPlayerCount() {
		return playerStats.keySet().size();
	}
	
	public String getIngameInfo() {
		String info;
		if (state == MatchState.Live) {
			ServerState serverState = server.getServerMonitor().getState();
			info = serverState.name();
			if (serverState == ServerState.LIVE) {
				info += " - " + server.getServerMonitor().getGameTime();
			}
			info += " (" + server.getServerMonitor().getScore() + ")";
			return info;
		} else {
			info = state.name();
			info += " (" + score[0] + "-" + score[1] + ")";
		}
		return info;
	}
	
	public String getMatchInfo() {
		
		String redplayers = "None";
		for (Player p : teamList.get("red")) {
			if (redplayers.equals("None")) {
				redplayers = p.getUrtauth();
			} else {
				redplayers += " " + p.getUrtauth();
			}
		}
		String blueplayers = "None";
		for (Player p : teamList.get("blue")) {
			if (blueplayers.equals("None")) {
				blueplayers = p.getUrtauth();
			} else {
				blueplayers += " " + p.getUrtauth();
			}
		}

		String msg = Config.pkup_match_print_info;
		msg = msg.replace(".gamenumber.", String.valueOf(id));
		msg = msg.replace(".gametype.", gametype.getName());
		msg = msg.replace(".map.", map != null ? map.name : "null");
		msg = msg.replace(".redteam.", redplayers);
		msg = msg.replace(".blueteam.", blueplayers);
		

		msg = msg.replace(".ingame.", getIngameInfo());
		
		return msg;
	}
	
	@Override
	public String toString() {
		String msg = "";
		switch(state) {
		case Signup: msg = Config.pkup_match_print_signup; break;
		case AwaitingServer: msg = Config.pkup_match_print_server; break;
		case Live: msg = Config.pkup_match_print_live; break;
		case Done: msg = Config.pkup_match_print_done; break;
		case Abort: msg = Config.pkup_match_print_abort; break;
		case Abandon: msg = Config.pkup_match_print_abandon; break;
		case Surrender: msg = Config.pkup_match_print_sur; break;
		default: break;
		}
		
		String playernames = "None";
		for (Player p : playerStats.keySet()) {
			if (playernames.equals("None")) {
				playernames = p.getDiscordUser().getMentionString();
			} else {
				playernames += " " + p.getDiscordUser().getMentionString();
			}
		}
		
		msg = msg.replace(".gamenumber.", String.valueOf(id));
		msg = msg.replace(".gametype.", gametype.getName());
		msg = msg.replace(".map.", map != null ? map.name : "null");
		msg = msg.replace(".elored.", String.valueOf(elo[0]));
		msg = msg.replace(".eloblue.", String.valueOf(elo[1]));
		msg = msg.replace(".playernumber.", String.valueOf(getPlayerCount()));
		msg = msg.replace(".maxplayer.", String.valueOf(gametype.getTeamSize() * 2));
		msg = msg.replace(".playerlist.", playernames);
		msg = msg.replace(".score.", String.valueOf(score[0]) + " " + String.valueOf(score[1]));
		return msg;
	}
}
