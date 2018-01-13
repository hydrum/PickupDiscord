package de.gost0r.pickupbot.discord;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;

public class DiscordChannel {

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
			e.printStackTrace();
		}
	}


	public static Map<String, DiscordChannel> channelList = new HashMap<String, DiscordChannel>();
	public static DiscordChannel findChannel(String channelID) {
		if (channelList.containsKey(channelID)) {
			return channelList.get(channelID);
		}
		JSONObject reply = DiscordAPI.requestChannel(channelID);
		if (reply != null) {
			DiscordChannel newChannel = new DiscordChannel(reply);
			channelList.put(channelID, newChannel);
			return newChannel;
		}
		return null;
	}
}
