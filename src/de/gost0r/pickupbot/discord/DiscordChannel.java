package de.gost0r.pickupbot.discord;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;

public class DiscordChannel {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public String id;
	public DiscordChannelType type;
	public String name;
	public String topic;
	
	public DiscordChannel(JSONObject channel) {
		try {
			this.id = channel.getString("id");
			this.type = DiscordChannelType.values()[channel.getInt("type")];
			this.name = channel.has("name") ? channel.getString("name") : null;
			this.topic = channel.has("topic") ? channel.getString("topic") : null;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}


	public static Map<String, DiscordChannel> channelList = new HashMap<String, DiscordChannel>();
	public static DiscordChannel findChannel(String channelID) {
		if (channelID.matches("[0-9]+")) {
			if (channelList.containsKey(channelID)) {
				return channelList.get(channelID);
			}
			JSONObject reply = DiscordAPI.requestChannel(channelID);
			if (reply != null) {
				DiscordChannel newChannel = new DiscordChannel(reply);
				channelList.put(channelID, newChannel);
				return newChannel;
			}
		}
		LOGGER.info("Unable to find channel for: " + channelID);
		return null;
	}
	

	@Override
	public boolean equals(Object o) {
		if (o instanceof DiscordChannel) {
			DiscordChannel chan = (DiscordChannel) o;
			return chan.id == this.id;
		}
		return false;
	}
}
