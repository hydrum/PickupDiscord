package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		// TODO
		// simply abort() ?
	}

	public void addPlayer(Player player) {
		if (state == MatchState.Signup && !isInMatch(player)) {
			// TODO: check for starting condition
			playerStats.put(player, new MatchStats());
			logic.cmdStatus(this);
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
		logic.db.saveMatch(this);
		server.free();

		// TODO: tell server to remove this match from ongoing
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
	

//	public void start() {
//		if (!done) {
//			logic.bot.sendMode(logic.bot.pubchan, "", "+m");
//			
//			server.take();
//			
//			starttime = System.currentTimeMillis();
//			
//			Random rand = new Random();
//			int password = rand.nextInt((999999-100000) + 1) + 100000;
//			Debug.Log(TAG.MATCH, "Password: " + String.valueOf(password));
//			server.setPassword(String.valueOf(password));
//	
//			// TODO: Randomize map if no votes.
//			for (Map map : maplist) {
//				if (this.map == null) this.map = map;
//				else if (map.getVotes() > this.map.getVotes()) {
//					this.map = map;
//				}
//			}		
//			Debug.Log(TAG.MATCH, "Map: " + this.map.getName());
//	
//			ArrayList<Player> sortplayers = new ArrayList<Player>();
//			sortplayers.add(playerlist.get(0));
//			for (Player player : playerlist) {
//				for (Player sortplayer : sortplayers) {
//					if (player.equals(sortplayer)) continue;
//					else if (player.getElo() <= sortplayer.getElo()) {
//						sortplayers.add(sortplayers.indexOf(sortplayer), player); 
//						break;
//					}				
//				}
//			}
//			teamred = new Player[5];
//			teamred[0] = sortplayers.get(0);
//			teamred[1] = sortplayers.get(2);
//			teamred[3] = sortplayers.get(7);
//			teamred[4] = sortplayers.get(9);
//			elored = teamred[0].getElo() + teamred[1].getElo() + teamred[3].getElo() + teamred[4].getElo();		
//	
//			teamblue = new Player[5];
//			teamblue[0] = sortplayers.get(1);
//			teamblue[1] = sortplayers.get(3);
//			teamblue[3] = sortplayers.get(6);
//			teamblue[4] = sortplayers.get(8);		
//			eloblue = teamblue[0].getElo() + teamblue[1].getElo() + teamblue[3].getElo() + teamblue[4].getElo();
//			
//			if (eloblue <= elored) {
//				teamblue[2] = sortplayers.get(4);
//				teamred[2] = sortplayers.get(5);
//			} else {
//				teamred[2] = sortplayers.get(4);
//				teamblue[2] = sortplayers.get(5);
//			}
//			eloblue += teamblue[2].getElo();
//			elored += teamred[2].getElo();
//			
//			eloblue /= 5;
//			elored /= 5;
//			
//			addMatchHistory();
//	
//			Debug.Log(TAG.MATCH, "Team red:" + elored + " " + teamred.toString());
//			Debug.Log(TAG.MATCH, "Team blue:" + eloblue + " " + teamblue.toString());
//			
//			id = logic.db.createMatch(this);
//			
//			server.startObservation(this);
//			
//			server.sendRcon("map " + this.map.getName());
//			server.sendRcon("g_password " + server.getPassword());
//
//			String defmapstring = "No votes for any map - RANDOM MAP!";
//			String mapstring = defmapstring;
//			for (Map map : maplist) {
//				if (map.getVotes() > 0) {
//					String chosenMap = this.map.getName().equals(map.getName()) ? "" : "";
//					if (mapstring.equals(defmapstring)) {
//						mapstring = chosenMap + map.getName() + ": " + String.valueOf(map.getVotes()) + chosenMap;
//					} else {
//						mapstring += " || " + chosenMap + map.getName() + ": " + String.valueOf(map.getVotes()) + chosenMap;
//					}
//				}
//			}
//			
//			String msg = Config.pkup_go_admin.replace(".elored.", String.valueOf(elored));
//			msg = msg.replace(".eloblue.", String.valueOf(eloblue));
//			msg = msg.replace(".gamenumber.", String.valueOf(id));
//			msg = msg.replace(".password.", server.getPassword());
//			msg = msg.replace(".map.", this.map.getName());
//			logic.bot.sendMsg(logic.bot.adminchan, msg);
//			
//			msg = Config.pkup_go_pub_head.replace(".elo.", String.valueOf((elored + eloblue)/2));
//			msg = msg.replace(".gamenumber.", String.valueOf(id));
//			logic.bot.sendMsg(logic.bot.pubchan, msg);
//			
//			logic.bot.sendMsg(logic.bot.pubchan, mapstring);
//			
//			msg = Config.pkup_go_pub_red.replace(".playerlist.", User.get(teamred[0].getQauth()).getNick() + " " + User.get(teamred[1].getQauth()).getNick()  + " " + User.get(teamred[2].getQauth()).getNick()  + " " + User.get(teamred[3].getQauth()).getNick()  + " " + User.get(teamred[4].getQauth()).getNick());
//			logic.bot.sendMsg(logic.bot.pubchan, msg);
//			msg = Config.pkup_go_pub_blue.replace(".playerlist.", User.get(teamblue[0].getQauth()).getNick() + " " + User.get(teamblue[1].getQauth()).getNick()  + " " + User.get(teamblue[2].getQauth()).getNick()  + " " + User.get(teamblue[3].getQauth()).getNick()  + " " + User.get(teamblue[4].getQauth()).getNick());
//			logic.bot.sendMsg(logic.bot.pubchan, msg);
//			
//			msg = Config.pkup_go_pub_map.replace(".map.", this.map.getName());
//			logic.bot.sendMsg(logic.bot.pubchan, msg);
//			
//			logic.bot.sendMsg(logic.bot.pubchan, Config.pkup_go_pub_calm);
//			
//			String msg_b = Config.pkup_go_player.replace(".team.", "blue");
//			msg_b = msg_b.replace(".server.", server.getAddress());
//			msg_b = msg_b.replace(".password.", server.getPassword());
//	
//			String msg_r = Config.pkup_go_player.replace(".team.", "red");
//			msg_r = msg_r.replace(".server.", server.getAddress());
//			msg_r = msg_r.replace(".password.", server.getPassword());
//
//			for (int i = 0; i < 5; i++) {
//				logic.bot.sendMsg(User.get(teamred[i].getQauth()).getNick(), msg_r);
//			}
//			for (int i = 0; i < 5; i++) {
//				logic.bot.sendMsg(User.get(teamblue[i].getQauth()).getNick(), msg_b);
//			}
//			
//			logic.bot.sendMsg(logic.bot.pubchan, Config.pkup_go_pub_lostpw);
//		}
//	}
	
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
}
