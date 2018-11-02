package de.gost0r.pickupbot.pickup;

import de.gost0r.pickupbot.discord.DiscordUser;

public class PlayerBan {
	
	public enum BanReason {
		NOSHOW,
		RAGEQUIT,
		INSULT,
		TROLL,
		AFK
	}
	
	public Player player;
	public long startTime;
	public long endTime;
	
	public BanReason reason;
	
	public DiscordUser pardon = null;

}
