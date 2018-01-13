package de.gost0r.pickupbot.pickup;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Match {
	
	private MatchState state;
	private int id;
	
	private List<Player> playerList;
	private Map<GameMap, Integer> mapVotes;
	
	private PickupLogic logic;

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
}
