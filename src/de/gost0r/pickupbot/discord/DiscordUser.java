package de.gost0r.pickupbot.discord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.api.DiscordAPI;
import de.gost0r.pickupbot.pickup.Gametype;

public class DiscordUser {
	
	public String id;
	public String username;
	public String discriminator;
	public String avatar;
	
	private Map<String, List<String>> roles = new HashMap<String, List<String>>();
	
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
	
	public List<String> getRoles(String guild) {
//		if (roles.containsKey(guild)) {
//			return roles.get(guild);
//		} else
		// DON'T SAVE THEM FOR NOW, NEED GUILD_MEMBER_UPDATE EVENTS TO TAKE CARE OF IT INSTEAD (later)
		{
			JSONArray ar = DiscordAPI.requestUserGuildRoles(guild, id);
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < ar.length(); ++i) {
				try {
					list.add(ar.getString(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			roles.put(guild, list);
			return list;
		}
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
			e.printStackTrace();
		}
		return null;
	}
	public static DiscordUser getUser(String id) {
		if (userList.containsKey(id)) {
			return userList.get(id);
		}
		DiscordUser newUser = new DiscordUser(DiscordAPI.requestUser(id));
		userList.put(id, newUser);
		return newUser;
	}
	
	public static DiscordUser findUser(String id) {
		if (userList.containsKey(id)) {
			return userList.get(id);
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
