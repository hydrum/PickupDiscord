package de.gost0r.pickupbot.discord;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;
import de.gost0r.pickupbot.discord.web.DiscordGateway;
import de.gost0r.pickupbot.discord.web.DiscordGatewayEvent;
import de.gost0r.pickupbot.discord.web.WsClientEndPoint;

public class DiscordBot  {
	
	private static String token = "";
	private static String guildID = "117622053061787657";
		
	private DiscordGateway gateway;
	private WsClientEndPoint endpoint;
	
	public DiscordBot() {
	}
	
	public void init() {
		try {
			
			endpoint = new WsClientEndPoint(new URI("wss://gateway.discord.gg/?encoding=json&v=6"));
			gateway = new DiscordGateway(this, endpoint);
			
			endpoint.addMessageHandler(gateway);
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void handleEvent(DiscordGatewayEvent event, JSONObject obj) {
		try {
			switch(event) {
				case MESSAGE_CREATE: 		recvMessage(DiscordUser.getUser(obj.getJSONObject("author")),
														DiscordChannel.findChannel(obj.getString("channel_id")),
														obj.getString("content"));
											break;
				default: break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void recvMessage(DiscordUser user, DiscordChannel channel, String msg) {
		
	}
	
	public void sendMsg(DiscordChannel channel, String msg) {
		DiscordAPI.sendMessage(channel, msg);
	}
	
	public void sendMsg(DiscordUser user, String msg) {
		sendMsg(user.getDMChannel(), msg);
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		DiscordBot.token = token;
	}

	public static String getGuildID() {
		return guildID;
	}

	public static void setGuildID(String guildID) {
		DiscordBot.guildID = guildID;
	}

	public DiscordUser parseMention(String string) {
		string = string.replaceAll("[^\\d]", "" );
		return DiscordUser.getUser(string);
	}

}
