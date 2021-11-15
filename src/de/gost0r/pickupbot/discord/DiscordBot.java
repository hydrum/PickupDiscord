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
	private static DiscordGuild guild;
	
	protected DiscordUser self = null;
		
	private DiscordGateway gateway = null;
	private WsClientEndPoint endpoint = null;
	
	public DiscordBot() {
	}
	
	public void init() {
		reconnect();
		self = DiscordUser.getUser("@me");
		guild = DiscordGuild.getGuild("117622053061787657");
	}
	
	public void reconnect() {
		try {
			if (endpoint == null) {
				endpoint = new WsClientEndPoint(this, new URI("wss://gateway.discord.gg/?encoding=json&v=9"));
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
			DiscordUser user = null;
			switch(event){
			case MESSAGE_CREATE:
				DiscordMessage msg = new DiscordMessage(obj.getString("id"),
								DiscordUser.getUser(obj.getJSONObject("author")),
								DiscordChannel.findChannel(obj.getString("channel_id")),
								obj.getString("content"));
				recvMessage(msg);
				break;
			case GUILD_MEMBER_UPDATE:
				user = DiscordUser.findUser(obj.getJSONObject("user").getString("id"));
				if (user != null) {
					// only update roles of users that we already know
					user.setRoles(DiscordGuild.getGuild("guild_id"), obj.getJSONArray("roles"));
				}
				break;
			case PRESENCE_UPDATE:
				user = DiscordUser.findUser(obj.getJSONObject("user").getString("id"));
				if (user != null) {
					DiscordUserStatus userStatus = DiscordUserStatus.valueOf(obj.getString("status"));
					user.setStatus(userStatus);
				}
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		tick();
	}
	
	protected void tick() {
		
	}

	protected void recvMessage(DiscordMessage msg) {
		
	}
	
	public void sendMsg(DiscordChannel channel, String msg) {
		DiscordAPI.sendMessage(channel, msg);
	}
	
	public void sendMsg(DiscordChannel channel, String msg, DiscordEmbed embed) {
		DiscordAPI.sendMessage(channel, msg, embed);
	}
	
	
	public void sendMsg(DiscordUser user, String msg) {
		sendMsg(user.getDMChannel(), msg);
	}
	
	public void sendMsg(DiscordUser user, String msg, DiscordEmbed embed) {
		sendMsg(user.getDMChannel(), msg, embed);
	}
	
	public DiscordMessage sendMsgToEdit(DiscordChannel channel, String msg, DiscordEmbed embed) {
		return DiscordAPI.sendMessageToEdit(channel, msg, embed);
	}
	
	public DiscordChannel createThread(DiscordChannel channel, String name) {
		return DiscordAPI.createThread(channel, name);
	}

	public static String getToken() {
		return token;
	}

	public static void setToken(String token) {
		DiscordBot.token = token;
	}

	public static DiscordGuild getGuild() {
		return guild;
	}

	public static void setGuild(DiscordGuild guild) {
		DiscordBot.guild = guild;
	}

	public DiscordUser parseMention(String string) {
		string = string.replaceAll("[^\\d]", "" );
		return DiscordUser.getUser(string);
	}

}
