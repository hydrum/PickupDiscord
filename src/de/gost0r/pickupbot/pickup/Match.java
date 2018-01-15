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
			// TODO
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
	
	private void resetLive() {
		abort();
	}

	public void addPlayer(Player player) {
		if (state == MatchState.Signup && !isInMatch(player)) {
			playerStats.put(player, new MatchStats());
			checkReadyState();
		}
	}

	public void removePlayer(Player player) {
		if (state == MatchState.Signup && isInMatch(player)) {
			GameMap map = player.getVotedMap();
			if (map != null) {
				mapVotes.put(map, mapVotes.get(map).intValue() - 1);
				player.resetMap();
			}
			playerStats.remove(player);
			logic.cmdStatus(this);
		}
	}

	public void voteMap(Player player, GameMap map) {
		if (state == MatchState.Signup && isInMatch(player) && player.getVotedMap() == null) {
			mapVotes.put(map, mapVotes.get(map).intValue() + 1);
			player.vote(map);
			System.out.println(logic == null);
			System.out.println(logic.bot == null);
			logic.bot.sendNotice(player.getDiscordUser(), Config.pkup_map);
		} else {
			logic.bot.sendNotice(player.getDiscordUser(), Config.map_already_voted);
		}
	}
	
	public void checkReadyState() {
		if (playerStats.keySet().size() == 10) {
			state = MatchState.AwaitingServer;
			logic.cmdStatus(this);
			logic.requestServer(this);
		} else {
			logic.cmdStatus(this);
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
	}
	
	private void cleanUp() {
		for(Player p : playerStats.keySet()) {
			p.resetMap();
		}
		logic.db.saveMatch(this);
		server.free();

		logic.matchEnded(this);
	}
	
	private void sendAftermath() {
		for (int i = 0; i < 2; ++i) {
    		int team = i;
    		int opp = (team + 1) % 2;
    		String teamname = (team == 0) ? "Red" : "Blue";
			String msg = Config.pkup_aftermath;
			msg = msg.replace(".team.", teamname);
			if (score[team] > score[opp]) {
				msg = msg.replace(".result.", "won");
			} else if (score[team] < score[opp]) {
				msg = msg.replace(".result.", "lost");
			} else {
				msg = msg.replace(".result.", "draw");
			} 
			msg = msg.replace(".score.", score[team] + "-" + score[opp]);
	
			for (int j = 0; j < teamList.get(teamname).size(); ++j) {
				Player p = teamList.get(teamname).get(j);
				msg = msg.replace(".player"    + (j + 1) + ".", p.getDiscordUser().getMentionString());
				msg = msg.replace(".elochange" + (j + 1) + ".", String.valueOf(p.getEloChange()));
				logic.bot.sendMsg(logic.bot.getPubchan(), msg);
			}
		}
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
			this.map = tmp.get(rand.nextInt(tmp.size()-1));
			System.out.println("Map: " + this.map.name);

			// Sort players by elo
			Player[] playerList = (Player[]) playerStats.keySet().toArray();
			List<Player> sortPlayers = new ArrayList<Player>();
			sortPlayers.add(playerList[0]);
			for (Player player : playerList) {
				for (Player sortplayer : sortPlayers) {
					if (player.equals(sortplayer)) continue;
					else if (player.getElo() <= sortplayer.getElo()) {
						sortPlayers.add(sortPlayers.indexOf(sortplayer), player); 
						break;
					}				
				}
			}
			
			teamList.get("red").add(sortPlayers.get(0));
			teamList.get("red").add(sortPlayers.get(2));
			teamList.get("red").add(sortPlayers.get(7));
			teamList.get("red").add(sortPlayers.get(9));
			for (Player p : teamList.get("red")) elo[0] += p.getElo();

			teamList.get("blue").add(sortPlayers.get(1));
			teamList.get("blue").add(sortPlayers.get(3));
			teamList.get("blue").add(sortPlayers.get(6));
			teamList.get("blue").add(sortPlayers.get(8));
			for (Player p : teamList.get("blue")) elo[1] += p.getElo();
			
			if (elo[0] > elo[1]) {
				teamList.get("red").add(sortPlayers.get(5));
				teamList.get("blue").add(sortPlayers.get(4));
				elo[0] += sortPlayers.get(5).getElo();
				elo[1] += sortPlayers.get(4).getElo();
			} else {
				teamList.get("red").add(sortPlayers.get(4));
				teamList.get("blue").add(sortPlayers.get(5));
				elo[0] += sortPlayers.get(4).getElo();
				elo[1] += sortPlayers.get(5).getElo();
			}
			
			// avg elo
			elo[0] /= 5;
			elo[1] /= 5;
	
			System.out.println("Team Red: " + elo[0] + " " + Arrays.toString(teamList.get("red").toArray()));
			System.out.println("Team Blue: " + elo[1] + " " + Arrays.toString(teamList.get("blue").toArray()));
			
			id = logic.db.createMatch(this);
			
			server.startObservation(this);
			
			server.sendRcon("exec " + this.gametype.getConfig());
			server.sendRcon("map " + this.map.name);
			server.sendRcon("g_password " + server.password);
			
			// MESSAGE HYPE
			
			
//			String msg = Config.pkup_go_admin.replace(".elored.", String.valueOf(elo[0]));
//			msg = msg.replace(".eloblue.", String.valueOf(elo[1]));
//			msg = msg.replace(".gamenumber.", String.valueOf(id));
//			msg = msg.replace(".password.", server.password);
//			msg = msg.replace(".map.", this.map.name);
//			logic.bot.sendMsg(logic.bot.adminchan, msg);
			
			String msg = Config.pkup_go_pub_head;
			msg = msg.replace(".elo.", String.valueOf((elo[0] + elo[1])/2));
			msg = msg.replace(".gamenumber.", String.valueOf(id));
			msg = msg.replace(".gametype.", gametype.getName());
			logic.bot.sendMsg(logic.bot.getPubchan(), msg);
			
			msg = Config.pkup_map_list;
			msg = msg.replace(".gametype.", gametype.getName());
			msg = msg.replace(".maplist.", getMapVotes());
			logic.bot.sendMsg(logic.bot.getPubchan(), msg);
			
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
				logic.bot.sendMsg(logic.bot.getPubchan(), msg);
			}
			
			msg = Config.pkup_go_pub_map;
			msg = msg.replace(".map.", this.map.name);
			msg = msg.replace(".gametype.", gametype.getName());
			logic.bot.sendMsg(logic.bot.getPubchan(), msg);

			msg = Config.pkup_go_pub_calm;
			msg = msg.replace(".gametype.", gametype.getName());
			logic.bot.sendMsg(logic.bot.getPubchan(), msg);

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
			if (!playernames.equals("None")) {
				playernames += " ";
			}
			playernames += p.getDiscordUser().getMentionString();
		}
		
		msg = msg.replace(".gamenumber.", String.valueOf(id));
		msg = msg.replace(".gametype.", gametype.getName());
		msg = msg.replace(".map.", map != null ? map.name : "null");
		msg = msg.replace(".elored.", String.valueOf(elo[0]));
		msg = msg.replace(".eloblue.", String.valueOf(elo[1]));
		msg = msg.replace(".playernumber.", String.valueOf(getPlayerCount()));
		msg = msg.replace(".playerlist.", playernames);
		return msg;
	}
}
