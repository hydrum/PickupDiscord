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
	private int eloRed;
	private int eloBlue;
	private Player[] teamRed;
	private Player[] teamBlue;
	
	private int scoreRed;
	private int scoreBlue;
	
	private long startTime;
	
	private PickupLogic logic;
	
	public Match() {
		teamList = new HashMap<String, List<Player>>();
		teamList.put("red", new ArrayList<Player>());
		teamList.put("blue", new ArrayList<Player>());
		mapVotes = new HashMap<GameMap, Integer>();
	}

	public Match(int id, long startTime, GameMap map, int scoreRed, int scoreBlue, int eloRed, int eloBlue,
			Map<String, List<Player>> teamList, MatchState state, Gametype gametype, Server server,
			Map<Player, MatchStats> playerStats) {
		this.id = id;
		this.startTime = startTime;
		this.map = map;
		this.scoreRed = scoreRed;
		this.scoreBlue = scoreBlue;
		this.eloRed = eloRed;
		this.eloBlue = eloBlue;
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
		} else if (state == MatchState.Done){
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
	}

	public void addPlayer(Player player) {
		// TODO send msg
		if (state == MatchState.Signup && !isInMatch(player)) {
			playerStats.put(player, new MatchStats());
		}
	}

	public void removePlayer(Player player) {
		// TODO send msg
		if (state == MatchState.Signup && isInMatch(player)) {
			GameMap map = player.getVotedMap();
			if (map != null) {
				mapVotes.put(map, mapVotes.get(map).intValue() - 1);
			}
			playerStats.remove(player);
		}
	}

	public void voteMap(Player player, GameMap map) {
		if (state == MatchState.Signup && isInMatch(player) && player.getVotedMap() == null) {
			mapVotes.put(map, mapVotes.get(map).intValue() + 1);
			player.vote(map);
			logic.bot.sendNotice(player.getDiscordUser(), Config.pkup_map);
		} else {
			logic.bot.sendNotice(player.getDiscordUser(), Config.map_already_voted);
		}
	}
	
	public String getMapVotes() {

		String msg = "";
		for (GameMap map : mapVotes.keySet()) {
			if (msg.equals(""))
				msg += map.name + ": " + String.valueOf(mapVotes.get(map));
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

	public Server getServer() {
		return server;
	}

	public long getStartTime() {
		return startTime;
	}

	public GameMap getMap() {
		return map;
	}

	public int getEloRed() {
		return eloRed;
	}

	public int getEloBlue() {
		return eloBlue;
	}

	public Player[] getTeamRed() {
		return teamRed;
	}

	public Player[] getTeamBlue() {
		return teamBlue;
	}

	public Player[] getPlayerList() {
		return (Player[]) playerStats.keySet().toArray();
	}

	public String getTeam(Player player) {
		for(String team : teamList.keySet()) {
			if (teamList.get(team).contains(player)) {
				return team;
			}
		}
		return "null";
	}

	public int getScoreRed() {
		return scoreRed;
	}
	
	public int getScoreBlue() {
		return scoreBlue;
	}

	public MatchStats getStats(Player player) {
		return playerStats.get(player);
	}
}
