package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.gost0r.pickupbot.pickup.server.Server;

public class Match {
	
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
	}

	public Match(int id, long startTime, GameMap map, int[] score, int[] elo,
			Map<String, List<Player>> teamList, MatchState state, Gametype gametype, Server server,
			Map<Player, MatchStats> playerStats) {
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
	}

	public void reset() {
		if (state == MatchState.Signup) {
			resetSignup();
		} else if (state == MatchState.AwaitingServer) {
			resetAwaitingServer();
		} else if (state == MatchState.Live) {
			resetLive();
		} else if (state == MatchState.Done || state == MatchState.Abort){
			// do nothing
		}
	}
	
	private void resetSignup() {
		// reset mapvote
		for(Player p : playerStats.keySet()) {
			p.resetMap();
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

	public void removePlayer(Player player) {
		if ((state == MatchState.Signup || state == MatchState.AwaitingServer) && isInMatch(player)) {
			GameMap map = player.getVotedMap();
			if (map != null) {
				mapVotes.put(map, mapVotes.get(map).intValue() - 1);
				player.resetMap();
			}
			playerStats.remove(player);
			checkServerState();
			logic.cmdStatus(this, player);
		}
	}

	public void voteMap(Player player, GameMap map) {
		if ((state == MatchState.Signup || state == MatchState.AwaitingServer) && isInMatch(player) && player.getVotedMap() == null) {
			mapVotes.put(map, mapVotes.get(map).intValue() + 1);
			player.vote(map);
			logic.bot.sendNotice(player.getDiscordUser(), Config.pkup_map);
		} else {
			logic.bot.sendNotice(player.getDiscordUser(), Config.map_already_voted);
		}
	}
	
	public void checkReadyState(Player player) {
		if (playerStats.keySet().size() == gametype.getTeamSize() * 2) {
			state = MatchState.AwaitingServer;
			logic.cmdStatus(this, null);
			logic.requestServer(this);
		} else {
			logic.cmdStatus(this, player);
		}
	}
	
	public void checkServerState() {
		if (state == MatchState.AwaitingServer && playerStats.keySet().size() != gametype.getTeamSize() * 2) {
			state = MatchState.Signup;
		}
	}

	public void abort() {
		state = MatchState.Abort;
		cleanUp();
	}

	public void end() {
		state = MatchState.Done;
		cleanUp();
		sendAftermath();
		logic.matchRemove(this);
	}
	
	private void cleanUp() {
		for(Player p : playerStats.keySet()) {
			p.resetMap();
		}
		System.out.println(logic == null);
		logic.db.saveMatch(this);
		server.free();

		logic.matchEnded(this);
	}
	
	private void sendAftermath() {		
		String fullmsg = Config.pkup_aftermath_head;
		fullmsg = fullmsg.replace(".gametype.", gametype.getName());
		fullmsg = fullmsg.replace(".gamenumber.", String.valueOf(id));
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
				msgelo = msgelo.replace(".elochange.", String.valueOf(p.getEloChange()));
				msg += " " + msgelo;
			}
			fullmsg += "\n" + msg;
		}
		logic.bot.sendMsg(logic.bot.getPubchan(), fullmsg);
	}
	

	public void start(Server server) {
		if (!(state == MatchState.Abort || state == MatchState.Done)) {
			
			this.server = server;
			server.take();
			
			state = MatchState.Live;
			
			startTime = System.currentTimeMillis();
			
			Random rand = new Random();
			int password = rand.nextInt((999999-100000) + 1) + 100000;
			server.password = String.valueOf(password);
			System.out.println("Password: " + server.password);

			// Get most voted map
			List<GameMap> tmp = new ArrayList<GameMap>();
			int currentVotes = -1;
			for (GameMap map : mapVotes.keySet()) {
				if (currentVotes == -1) {
					tmp.add(map);
					currentVotes = mapVotes.get(map);
					continue;
				}

				if (mapVotes.get(map) > currentVotes) {
					tmp.clear();
					tmp.add(map);
					currentVotes = mapVotes.get(map);
				} else if (mapVotes.get(map) == currentVotes) {
					tmp.add(map);
				}
			}
			System.out.println(tmp.size());
			System.out.println(Arrays.toString(tmp.toArray()));
			this.map = tmp.size() == 1 ? tmp.get(0) : tmp.get(rand.nextInt(tmp.size()-1));
			System.out.println("Map: " + this.map.name);

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

				if (elo[0] > elo[1]) {
					teamList.get("red").add(sortPlayers.get(worse));
					teamList.get("blue").add(sortPlayers.get(better));
				} else {
					teamList.get("red").add(sortPlayers.get(better));
					teamList.get("blue").add(sortPlayers.get(worse));
				}
			}
			
			// avg elo
			for (Player p : teamList.get("red")) elo[0] += p.getElo();
			for (Player p : teamList.get("blue")) elo[1] += p.getElo();
			elo[0] /= gametype.getTeamSize();
			elo[1] /= gametype.getTeamSize();
	
			System.out.println("Team Red: " + elo[0] + " " + Arrays.toString(teamList.get("red").toArray()));
			System.out.println("Team Blue: " + elo[1] + " " + Arrays.toString(teamList.get("blue").toArray()));
			
			id = logic.db.createMatch(this);
			
			server.startObservation(this);
			
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
			msg = msg.replace(".maplist.", getMapVotes());
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
			
			logic.bot.sendMsg(logic.bot.getPubchan(), fullmsg);

			msg = Config.pkup_go_player;
			msg = msg.replace(".server.", server.getAddress());
			msg = msg.replace(".password.", server.password);
			for (String team : teamList.keySet()) {
				for (Player player : teamList.get(team)) {
					String msg_t = msg.replace(".team.", team);
					logic.bot.sendMsg(player.getDiscordUser(), msg_t);
				}
			}
			
			msg = Config.pkup_go_pub_sent;
			msg = msg.replace(".gametype.", gametype.getName());
			logic.bot.sendMsg(logic.bot.getPubchan(), msg);

			// set server data
			for (String s : this.gametype.getConfig()) {
				server.sendRcon(s);
			}
			server.sendRcon("g_password " + server.password);
			server.sendRcon("map " + this.map.name);
			
			logic.matchStarted(this);
		}
	}
	
	public String getMapVotes() {

		String msg = "None";
		for (GameMap map : mapVotes.keySet()) {
			if (msg.equals("None"))
				msg = map.name + ": " + String.valueOf(mapVotes.get(map));
			else
				msg += " || " + map.name + ": " + String.valueOf(mapVotes.get(map));
		}
		return msg;
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
	
	@Override
	public String toString() {
		String msg = "";
		switch(state) {
		case Signup: msg = Config.pkup_match_print_signup; break;
		case AwaitingServer: msg = Config.pkup_match_print_server; break;
		case Live: msg = Config.pkup_match_print_live; break;
		case Done: msg = Config.pkup_match_print_done; break;
		case Abort: msg = Config.pkup_match_print_abort; break;
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
		return msg;
	}
}
