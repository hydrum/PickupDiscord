package de.gost0r.pickupbot.discord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;

public class DiscordUser {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public String id;
	public String username;
	public String discriminator;
	public String avatar;
	
	public DiscordChannel channel = null;
	
	private Map<DiscordGuild, List<DiscordRole>> roles = new HashMap<DiscordGuild, List<DiscordRole>>();
	
	public DiscordUser(String id, String username, String discriminator, String avatar) {
		this.id = id;
		this.username = username;
		this.discriminator = discriminator;
		this.avatar = avatar;
	}
	
	public DiscordUser(JSONObject user) {
		try {
			this.id = user.getString("id");
			this.username = user.getString("username");
			this.discriminator = user.getString("discriminator");
			this.avatar = user.getString("avatar");
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	
	public String getMentionString() {
		return "<@" + id + ">";
	}
	
	public List<DiscordRole> getRoles(DiscordGuild guild) {
		if (roles.containsKey(guild)) {
			return roles.get(guild);
		} else {
			JSONArray ar = DiscordAPI.requestUserGuildRoles(guild.id, this.id);
			List<DiscordRole> list = new ArrayList<DiscordRole>();
			for (int i = 0; i < ar.length(); ++i) {
				try {
					DiscordRole role = DiscordRole.getRole(ar.getString(i));
					list.add(role);
				} catch (JSONException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
				}
			}
			roles.put(guild, list);
			return list;
		}
	}
	
	public DiscordChannel getDMChannel() {
		try {
			if (channel == null) {
				String channelID = DiscordAPI.createDM(id).getString("id");
				DiscordChannel channel = DiscordChannel.findChannel(channelID);
				this.channel = channel;
			}
			return channel;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (NullPointerException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return null;
	}

	public static Map<String, DiscordUser> userList = new HashMap<String, DiscordUser>();
	public static DiscordUser getUser(JSONObject obj) {
		try {
			String userID = obj.getString("id");
			if (userList.containsKey(userID)) {
				return userList.get(userID);
			}
			DiscordUser newUser = new DiscordUser(obj);
			userList.put(userID, newUser);
			return newUser;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return null;
	}
	public static DiscordUser getUser(String id) {
		if (id.matches("[0-9]+")) {
			if (userList.containsKey(id)) {
				return userList.get(id);
			}
			JSONObject user = DiscordAPI.requestUser(id);
			if (user != null) {
				DiscordUser newUser = new DiscordUser(user);
				userList.put(newUser.id, newUser);
				return newUser;
			}
		}
		return null;
	}
	
	public static DiscordUser findUser(String id) {
		if (id.matches("[0-9]+")) {
			if (userList.containsKey(id)) {
				return userList.get(id);
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DiscordUser) {
			DiscordUser user = (DiscordUser) o;
			return user.id == this.id;
		}
		return false;
	}
}
