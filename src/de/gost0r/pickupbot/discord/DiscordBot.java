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
				case MESSAGE_CREATE: recvMessage(DiscordUser.findUser(obj.getJSONObject("author")),
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
		//DiscordAPI.sendMessage(channel, msg);
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		DiscordBot.token = token;
	}

}
