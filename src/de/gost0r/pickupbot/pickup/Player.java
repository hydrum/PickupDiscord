package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.List;

import de.gost0r.pickupbot.discord.DiscordUser;

public class Player {
	
	private DiscordUser user;
	private String urtauth;
	
	private GameMap votedMap;
	private int elo;
	private int eloChange;
	
	private boolean banned;
	
	public Player(DiscordUser user, String urtauth) {
		this.user = user;
		this.setUrtauth(urtauth);
		playerList.add(this);
	}
	
	public void vote(GameMap map) {
		if (votedMap == null) {
			votedMap = map;
		}
	}
	
	public void resetMap() {
		votedMap = null;
	}
	
	public GameMap getVotedMap() {
		return votedMap;
	}

	public DiscordUser getDiscordUser() {
		return user;
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		this.elo = elo;
	}

	public int getEloChange() {
		return eloChange;
	}

	public void setEloChange(int eloChange) {
		this.eloChange = eloChange;
	}
	
	public String getUrtauth() {
		return urtauth;
	}

	public void setUrtauth(String urtauth) {
		this.urtauth = urtauth;
	}
	

	private static List<Player> playerList = new ArrayList<Player>();
	public static Player get(String urtauth) {
		// TODO: check db
		for (Player player : playerList) {
			if (player.getUrtauth().equals(urtauth))
				return player;
		}
		return null;
	}

	public static Player get(DiscordUser user) {
		// TODO: check db
		for (Player player : playerList) {
			if (player.getDiscordUser().equals(user))
				return player;
		}
		return null;
	}
}
