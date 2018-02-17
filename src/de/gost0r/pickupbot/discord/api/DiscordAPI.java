package de.gost0r.pickupbot.discord.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;

public class DiscordAPI {
	
	public static final String api_url = "https://discordapp.com/api/";
	public static final String api_version = "v6";
	
	public static boolean sendMessage(DiscordChannel channel, String msg) {
		try {
			String reply = sendPostRequest("/channels/"+ channel.id + "/messages", new JSONObject().put("content", msg));
			JSONObject obj = new JSONObject(reply);
			return !obj.has("code");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static String sendPostRequest(String request, JSONObject content) {
		try {
			byte[] postData       = content.toString().getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			
			URL url = new URL(api_url + api_version + request);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("POST");
			c.setRequestProperty("Authorization", "Bot " + DiscordBot.getToken());
			c.setRequestProperty("charset", "utf-8");
			c.setRequestProperty("Content-Type", "application/json"); 
			c.setRequestProperty("User-Agent", "Bot");
			c.setDoOutput(true);
			c.setUseCaches(false);
			c.setRequestProperty("Content-Length", Integer.toString(postDataLength));	
			try (DataOutputStream wr = new DataOutputStream( c.getOutputStream())) {
				wr.write(postData);
			}
			
			if (c.getResponseCode() != 200) {
				System.out.println("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + request);
				if (c.getResponseCode() == 429 || c.getResponseCode() == 502) {
					try {
						Thread.sleep(1000);
						return sendPostRequest(request, content);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((c.getInputStream())));
			String fullmsg = "";
			String output = "";
			while ((output = br.readLine()) != null) {
				try {
					fullmsg += output;
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			c.disconnect();
			
			return fullmsg;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String sendGetRequest(String request) {
		try {
			URL url = new URL(api_url + api_version + request);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("User-Agent", "Bot");
			c.setRequestProperty("Authorization", "Bot " + DiscordBot.getToken());
			
			if (c.getResponseCode() != 200) {
				System.out.println("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + request);
				if (c.getResponseCode() == 429 || c.getResponseCode() == 502) {
					try {
						Thread.sleep(1000);
						return sendGetRequest(request);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((c.getInputStream())));
			String fullmsg = "";
			String output = "";
			while ((output = br.readLine()) != null) {
				try {
					fullmsg += output;
				} catch (ClassCastException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
			c.disconnect();
			
			return fullmsg;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static JSONObject createDM(String userid) {
		try {
			String reply = sendPostRequest("/users/@me/channels", new JSONObject().put("recipient_id", userid));
			JSONObject obj = new JSONObject(reply);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray requestDM() {
		String reply = sendGetRequest("/users/@me/channels");
		if (!reply.isEmpty()) {
			try {
				return new JSONArray(reply);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static JSONObject requestUser(String userID) {
		String reply = sendGetRequest("/users/" + userID);
		if (!reply.isEmpty()) {
			try {
				return new JSONObject(reply);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static JSONObject requestChannel(String channelID) {
		String reply = sendGetRequest("/channels/" + channelID);
		if (!reply.isEmpty()) {
			try {
				return new JSONObject(reply);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static JSONArray requestUserGuildRoles(String guild, String userID) {
		String reply = sendGetRequest("/guilds/" + guild + "/members/" + userID);
		if (!reply.isEmpty()) {
			try {
				return new JSONObject(reply).getJSONArray("roles");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
