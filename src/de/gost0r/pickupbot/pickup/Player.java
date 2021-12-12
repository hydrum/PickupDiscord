package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gost0r.pickupbot.discord.DiscordUser;

public class Player {
	
	public static Database db;
	
	private DiscordUser user;
	private String urtauth;
	
	private Map<Gametype, GameMap> votedMap = new HashMap<Gametype, GameMap>();
	private int elo = 1000;
	private int eloChange = 0;
	
	private float kdr = 0.0f;
	
	public PlayerStats stats;
	
	private List<PlayerBan> bans = new ArrayList<PlayerBan>();
		
	private boolean active = true;
	
	private boolean surrender = false;
	
	private long lastMessage = -1L;
	private boolean afkReminderSent = false;
	
	private String country = "NOT_DEFINED";

	public Player(DiscordUser user, String urtauth) {
		this.user = user;
		this.setUrtauth(urtauth);
		playerList.add(this);
	}

	public void voteMap(Gametype gametype, GameMap map) {
		votedMap.put(gametype, map);
	}

	public void voteSurrender() {
		surrender = true;
	}

	public void resetVotes() {
		for (Gametype gt : votedMap.keySet()) {
			votedMap.put(gt, null);
		}
		surrender = false;
	}	

	public void addElo(int elochange) {
		this.elo += elochange;
		this.eloChange = elochange;
		
		// db update done by servermonitor
	}

	public void afkCheck() {
		lastMessage = System.currentTimeMillis();
		afkReminderSent = false;
	}

	public long getLastMessage() {
		return lastMessage;
	}
	
	public void setLastMessage(long lastMessage) {
		this.lastMessage = lastMessage;
	}

	public boolean getAfkReminderSent() {
		return afkReminderSent;
	}

	public void setAfkReminderSent(boolean value) {
		afkReminderSent = value;
	}

	public GameMap getVotedMap(Gametype gametype) {
		if (votedMap.containsKey(gametype)) {
			return votedMap.get(gametype);
		}
		return null;
	}

	public boolean hasVotedSurrender() {
		return surrender;
	}

	public DiscordUser getDiscordUser() {
		return user;
	}

	public int getElo() {
		return elo;
	}

	public void setElo(int elo) {
		if (elo <= 0) {
			this.elo = 1000;
		} else {
			this.elo = elo;
		}
	}
	
	public float getKdr() {
		return kdr;
	}

	public void setKdr(float kdr) {
		this.kdr = kdr;
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

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void addBan(PlayerBan ban) {
		bans.add(ban);
	}

	public void forgiveBan() {
		for (PlayerBan ban : bans) {
			ban.forgiven = true;
		}
		
		db.forgiveBan(this);
	}

	public PlayerBan getLatestBan() {
		PlayerBan current = null;
		for (PlayerBan ban : bans) {
			if (current == null) {
				current = ban;
				continue;
			}
			if ((ban.startTime > current.startTime) && !ban.forgiven) {
				current = ban;
			}
		}
		return current;
	}

	public int getPlayerBanCountSince(long time) {
		int i = 0;
		for (PlayerBan ban : bans) {
			if (ban.startTime >= time) {
				++i;
			}
		}
		return i;
	}

	public boolean isBanned() {
		for (PlayerBan ban : bans) {
			if (!ban.forgiven && ban.endTime > System.currentTimeMillis()) {
				return true;
			}
		}
		return false;
	}

	public PlayerRank getRank() {
		return getRank(elo);
	}

	private PlayerRank getRank(int elo) {
		if (elo >= 1600) {
			return PlayerRank.DIAMOND;
		} else if (elo >= 1400) {
			return PlayerRank.PLATINUM;
		} else if (elo >= 1200) {
			return PlayerRank.GOLD;
		} else if (elo >= 1000) {
			return PlayerRank.SILVER;
		} else if (elo >= 800) {
			return PlayerRank.BRONZE;
		} else {
			return PlayerRank.WOOD;
		}
	}

	public boolean didChangeRank() {
		PlayerRank currentRank = getRank(elo);
		PlayerRank previousRank = getRank(elo-eloChange);
		return currentRank != previousRank;
	}

	private static List<Player> playerList = new ArrayList<Player>();
	public static Player get(String urtauth) {
		for (Player player : playerList) {
			if (player.getUrtauth().equals(urtauth) && player.getActive())
				return player;
		}
		Player p = db.loadPlayer(urtauth); // can be valid or null
		return p;
	}

	public static Player get(DiscordUser user) {
		for (Player player : playerList) {
			if (player.getDiscordUser().equals(user) && player.getActive())
				return player;
		}
		Player p = db.loadPlayer(user); // can be valid or null
		return p; 
	}

	public static Player get(DiscordUser user, String urtauth) {
		for (Player player : playerList) {
			if (player.getUrtauth().equals(urtauth) && player.getDiscordUser().equals(user))
				return player;
		}
		Player p = db.loadPlayer(user, urtauth, false); // can be valid or null
		return p;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Player) {
			Player player = (Player) o;
			return player.getDiscordUser().equals(this.getDiscordUser()) && player.urtauth == this.urtauth;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.urtauth;
	}

	public Region getRegion() {
		if(this.country.equalsIgnoreCase("NOT_DEFINED")) {
			return Region.WORLD;
		}
		else {
			String continent = Country.getContinent(this.country);
			
			return Region.valueOf(continent);
		}
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public static void remove(Player player) {
		if (playerList.contains(player)) {
			playerList.remove(player);
		}
	}
}
