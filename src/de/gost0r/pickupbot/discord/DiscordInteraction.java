package de.gost0r.pickupbot.discord;

import de.gost0r.pickupbot.discord.api.DiscordAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DiscordInteraction {
	public String id;
	public String token;
	public DiscordUser user;
	public String custom_id;
	public DiscordMessage message;
	public ArrayList<String> values;
	
	public DiscordInteraction(String id, String token, String custom_id, DiscordUser user, DiscordMessage message, JSONArray values) {
		this.id = id;
		this.token = token;
		this.user = user;
		this.custom_id = custom_id;
		this.message = message;

		ArrayList<String> strValues = new ArrayList<String>();
		if (values != null){
			for (int i = 0; i < values.length(); i++){
				strValues.add(values.getString(i));
			}
		}
		this.values = strValues;
	}
	
	public void respond(String content) {
		DiscordAPI.interactionRespond(id, token, content, null, null);
	}

	public void respond(String content, DiscordEmbed embed) {
		DiscordAPI.interactionRespond(id, token, content, embed, null);
	}

	public void respond(String content, DiscordEmbed embed, ArrayList<DiscordComponent> components) {
		DiscordAPI.interactionRespond(id, token, content, embed, components);
	}
}