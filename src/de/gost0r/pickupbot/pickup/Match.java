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
	private List<Player> playerList;
	private Map<GameMap, Integer> mapVotes;
	
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
		playerList = new ArrayList<Player>();
		mapVotes = new HashMap<GameMap, Integer>();
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
		for(Player p : playerList) {
			p.resetMap();
		}
		for (GameMap m : mapVotes.keySet()) {
			mapVotes.put(m, 0);
		}
		
		playerList.clear();
	}
	
	private void resetLive() {
		// TODO
	}

	public void addPlayer(Player player) {
		// TODO send msg
		if (state == MatchState.Signup && !isInMatch(player)) {
			playerList.add(player);
		}
	}

	public void removePlayer(Player player) {
		// TODO send msg
		if (state == MatchState.Signup && isInMatch(player)) {
			GameMap map = player.getVotedMap();
			if (map != null) {
				mapVotes.put(map, mapVotes.get(map).intValue() - 1);
			}
			playerList.remove(player);
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
		return playerList.contains(player);
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

	public List<Player> getPlayerList() {
		return playerList;
	}

	public String getTeam(Player player) {
		for(String team : teamList.keySet()) {
			if (teamList.get(team).contains(player)) {
				return team;
			}
		}
		return "null";
	}
}
