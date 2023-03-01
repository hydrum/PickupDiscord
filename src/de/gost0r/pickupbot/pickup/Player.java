package de.gost0r.pickupbot.pickup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordGuild;
import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.stats.WinDrawLoss;

import javax.swing.*;

public class Player {
	
	public static Database db;
	public static PickupLogic logic;
	
	private DiscordUser user;
	private String urtauth;
	
	private Map<Gametype, GameMap> votedMap = new HashMap<Gametype, GameMap>();
	private int elo = 1000;
	private int eloChange = 0;
	private int elorank = 0;
	
	private float kdr = 0.0f;
	
	public PlayerStats stats;
	
	private List<PlayerBan> bans = new ArrayList<PlayerBan>();
		
	private boolean active = true;
	private boolean enforceAC = false;
	
	private boolean surrender = false;
	
	private long lastMessage = -1L;
	private boolean afkReminderSent = false;
	private DiscordChannel lastPublicChannel;
	
	private String country = "NOT_DEFINED";

	private int coins;
	private long eloBoost;
	private int additionalMapVotes;
	private int mapBans;

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

	public ArrayList<PlayerBan> getPlayerBanListSince(long time) {
		ArrayList<PlayerBan> banList= new ArrayList<PlayerBan>();
		for (PlayerBan ban : bans) {
			if (ban.startTime >= time) {
				banList.add(ban);
			}
		}
		return banList;
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
		if (elorank <= 5){
			return PlayerRank.LEET;
		} else if (elo >= 1600) {
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

		// Update roles
		// TODO make it work for different servers
		if (currentRank != previousRank){
			logic.bot.removeUserRole(getDiscordUser(), previousRank.getRole());
		}
		if (getDiscordUser().hasRole(new DiscordGuild("117622053061787657"), PlayerRank.LEET.getRole()) && elorank > 5){
			logic.bot.removeUserRole(getDiscordUser(), PlayerRank.LEET.getRole());
		}
		if (!getDiscordUser().hasRole(new DiscordGuild("117622053061787657"), currentRank.getRole())){
			logic.bot.addUserRole(getDiscordUser(), currentRank.getRole());
		}

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

	public DiscordChannel getLastPublicChannel() { return lastPublicChannel; }

	public void setLastPublicChannel(DiscordChannel channel){
		lastPublicChannel = channel;
	}

	public boolean getEnforceAC() { return this.enforceAC; }

	public void setEnforceAC(boolean enforceAC) { this.enforceAC = enforceAC; }

	public float getCaptainScore(Gametype gt){
		WinDrawLoss wdl = stats.ts_wdl;
		float kdr = stats.kdr;
		if (gt.getName().equals("CTF")){
			wdl = stats.ctf_wdl;
			kdr = stats.ctf_rating;
		}
		if (wdl.getTotal() < 5){
			return (float) elo;
		}
		return (float) (elo + (kdr * 500 + wdl.calcWinRatio() * 500.0) / 4);
	}

	public void setRank(int rank){
		this.elorank = rank;
	}
	public int getEloRank(){
		return db.getRankForPlayer(this);
	}

	public int getCoins() {return coins;}
	public void setCoins(int coins) {this.coins = coins ;}

	public void addCoins(int amount) {
		coins += amount ;
	}
	public void spendCoins(int amount) {
		coins -= amount ;
	}
	public void saveWallet(){
		db.updatePlayerCoins(this);
	}

	public long getEloBoost() {return eloBoost;}
	public void setEloBoost(long eloBoost) {
		this.eloBoost = eloBoost ;
		db.updatePlayerBoost(this);
	}

	public boolean hasBoostActive(){
		return eloBoost >= System.currentTimeMillis();
	}

	public int getAdditionalMapVotes() {return additionalMapVotes;}
	public void setAdditionalMapVotes(int mapVotes) {
		this.additionalMapVotes = mapVotes ;
		db.updatePlayerBoost(this);
	}

	public int getMapBans() {return mapBans;}
	public void setMapBans(int mapBans) {
		this.mapBans = mapBans ;
		db.updatePlayerBoost(this);
	}
}
