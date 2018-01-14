package de.gost0r.pickupbot.pickup.server;

import de.gost0r.pickupbot.pickup.Player;

public class ServerPlayer {
	
	public enum ServerPlayerState {
		Connected,
		Disconnected,
		Reconnected
	}

	public ServerPlayerState state;
	public String id;
	public String name;
	public String team;
	public String ping;
	public String ip;
	public String auth;
	public Player player = null;
	public CTF_Stats ctfstats = new CTF_Stats();
	
	public ServerPlayer() {
		
	}
}
