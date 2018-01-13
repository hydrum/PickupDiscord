package de.gost0r.pickupbot.discord;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;

public class DiscordUser {
	
	public String id;
	public String username;
	public String discriminator;
	public String avatar;
	
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
			e.printStackTrace();
		}
	}

	public static Map<String, DiscordUser> userList = new HashMap<String, DiscordUser>();
	public static DiscordUser findUser(JSONObject obj) {
		try {
			String userID = obj.getString("id");
			if (userList.containsKey(userID)) {
				return userList.get(userID);
			}
			DiscordUser newUser = new DiscordUser(obj);
			userList.put(userID, newUser);
			return newUser;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
