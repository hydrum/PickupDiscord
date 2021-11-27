package de.gost0r.pickupbot.discord;

import de.gost0r.pickupbot.discord.api.DiscordAPI;

public class DiscordInteraction {
	public String id;
	public String token;
	public DiscordUser user;
	public String custom_id;
	public DiscordMessage message;
	
	public DiscordInteraction(String id, String token, String custom_id, DiscordUser user, DiscordMessage message) {
		this.id = id;
		this.token = token;
		this.user = user;
		this.custom_id = custom_id;
		this.message = message;
	}
	
	public void respond(String content) {
		DiscordAPI.interactionRespond(id, token, content);
	}
}