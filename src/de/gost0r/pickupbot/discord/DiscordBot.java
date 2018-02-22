package de.gost0r.pickupbot.discord;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;
import de.gost0r.pickupbot.discord.web.DiscordGateway;
import de.gost0r.pickupbot.discord.web.DiscordGatewayEvent;
import de.gost0r.pickupbot.discord.web.WsClientEndPoint;

public class DiscordBot  {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private static String token = "";
	private static String guildID = "117622053061787657";
	
	protected DiscordUser self = null;
		
	private DiscordGateway gateway = null;
	private WsClientEndPoint endpoint = null;
	
	public DiscordBot() {
	}
	
	public void init() {
		reconnect();
		self = DiscordUser.getUser("@me");
	}
	
	public void reconnect() {
		try {
			if (endpoint == null) {
				endpoint = new WsClientEndPoint(this, new URI("wss://gateway.discord.gg/?encoding=json&v=6"));
			} else {
				endpoint.reconnect();
			}
			if (gateway == null) {
				gateway = new DiscordGateway(this, endpoint);
			} else {
				gateway.reconnect();
			}
			endpoint.addMessageHandler(gateway);
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (Exception e) {
			// we can trigger a handshake exception for endpoint. if that's the case, simply try again.
			// TODO: need to check that this is really working
			LOGGER.log(Level.SEVERE, "Exception: ", e);
			init();
		}
	}
	
	public void handleEvent(DiscordGatewayEvent event, JSONObject obj) {
		try {
			if (event == DiscordGatewayEvent.MESSAGE_CREATE) {
				DiscordMessage msg = new DiscordMessage(obj.getString("id"),
								DiscordUser.getUser(obj.getJSONObject("author")),
								DiscordChannel.findChannel(obj.getString("channel_id")),
								obj.getString("content"));
				recvMessage(msg);
			}
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}

	protected void recvMessage(DiscordMessage msg) {
		
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
