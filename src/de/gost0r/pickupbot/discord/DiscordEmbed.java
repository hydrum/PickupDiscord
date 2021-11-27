package de.gost0r.pickupbot.discord;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DiscordEmbed {
	
	public String title;
	public String description;
	public String thumbnail;
	public int color;
	public List<JSONObject> fields;
	
	public DiscordEmbed() {
		fields = new ArrayList<JSONObject>();
	}
	
	public void addField(String name, String value, boolean inline) {
		JSONObject field = new JSONObject();
		field.put("name", name);
		field.put("value", value);
		field.put("inline", inline);
		
		fields.add(field);
	}
	
	public JSONObject getJSON() {
		JSONObject embedJSON = new JSONObject();
		
		if (title != null) {
			embedJSON.put("title", title);
		} 
		if (description != null) {
			embedJSON.put("description", description);
		}
		if (thumbnail != null) {
			embedJSON.put("thumbnail", new JSONObject().put("url", thumbnail));
		}
		if (color != 0) {
			embedJSON.put("color", color);
		} 
		if (fields != null) {
			embedJSON.put("fields", fields);
		} 
		
		return embedJSON;		
	}
}