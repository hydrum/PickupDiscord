package de.gost0r.pickupbot.discord.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.http.*;

import io.sentry.Sentry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordComponent;
import de.gost0r.pickupbot.discord.DiscordEmbed;
import de.gost0r.pickupbot.discord.DiscordMessage;

public class DiscordAPI {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static final String api_url = "https://discordapp.com/api/";
	public static final String api_version = "v9";
	
	public static boolean sendMessage(DiscordChannel channel, String msg) {
		try {
			String reply = sendPostRequest("/channels/"+ channel.id + "/messages", new JSONObject().put("content", msg));
			
			if (reply == null) {
				return false;
			}
			
			JSONObject obj = new JSONObject(reply);
			return !obj.has("code");
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}
	
	public static boolean sendMessage(DiscordChannel channel, String msg, DiscordEmbed embed) {
		try {
			List<JSONObject> embedList = new ArrayList<JSONObject>();
			embedList.add(embed.getJSON());
			String reply = sendPostRequest("/channels/"+ channel.id + "/messages", new JSONObject().put("content", msg).put("embeds", embedList));

			if (reply == null) {
				return false;
			}

			JSONObject obj = new JSONObject(reply);
			return !obj.has("code");
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}
	
	public static DiscordMessage sendMessageToEdit(DiscordChannel channel, String msg, DiscordEmbed embed, List<DiscordComponent> components) {
		try {
			List<JSONObject> embedList = new ArrayList<JSONObject>();
			if (embed != null) {
				embedList.add(embed.getJSON());
			}
			
			List<List<JSONObject>> componentListGlob = new ArrayList<List<JSONObject>>();
			List<JSONObject> componentList = new ArrayList<JSONObject>();
			if(components != null) {
				for (DiscordComponent component : components) {
					componentList.add(component.getJSON());
					
					if (componentList.size() == 5) {
						componentListGlob.add(new ArrayList<JSONObject>(componentList));
						componentList.clear();
					}
				}
				
				if (!componentList.isEmpty()) {
					componentListGlob.add(componentList);
				}
			}
			
			JSONObject content =  new JSONObject().put("content", msg);
			
			if (!embedList.isEmpty()) {
				content.put("embeds", embedList);
			}
			if(!componentListGlob.isEmpty()) {
				List<JSONObject> componentObjList = new ArrayList<JSONObject>();
				for (List<JSONObject> componentListElem : componentListGlob) {
					JSONObject componentObj = new JSONObject();
					componentObj.put("type", 1);
					componentObj.put("components", componentListElem);
					componentObjList.add(componentObj);
				}
				content.put("components", componentObjList);
			}
					
			String reply = sendPostRequest("/channels/"+ channel.id + "/messages", content);

			if (reply == null) {
				return null;
			}

			JSONObject obj = new JSONObject(reply);

			return new DiscordMessage(obj.getString("id"), null, channel, msg);
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}
	
	public static boolean deleteMessage(DiscordChannel channel, String msgid) {
		try {
			String reply = sendDeleteRequest("/channels/"+ channel.id + "/messages/" + msgid);
			JSONObject obj = null;
			if (reply != null) {
				if (reply.isEmpty()) {
					// a successful deletemsg will return a 204 error
					return true;
				}
				obj = new JSONObject(reply);
			}
			return obj != null && !obj.has("code");
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}

	public static boolean deleteChannel(DiscordChannel channel) {
		try {
			String reply = sendDeleteRequest("/channels/"+ channel.id);
			JSONObject obj = null;
			if (reply != null) {
				if (reply.isEmpty()) {
					return true;
				}
				obj = new JSONObject(reply);
			}
			return obj != null && !obj.has("code");
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}
	
	public static boolean editMessage(DiscordMessage msg, String content, DiscordEmbed embed) {
		try {
			List<JSONObject> embedList = new ArrayList<JSONObject>();
			embedList.add(embed.getJSON());
			String reply = sendPatchRequest("/channels/"+ msg.channel.id + "/messages/" + msg.id, new JSONObject().put("content", content).put("embeds", embedList));
			return reply != null;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}
	
	public static DiscordChannel createThread(DiscordChannel channel, String name) {
		try {
			String reply = sendPostRequest("/channels/"+ channel.id + "/threads", new JSONObject().put("name", name).put("type", 11));

			if (reply == null) {
				return null;
			}

			JSONObject obj = new JSONObject(reply);
			return new DiscordChannel(obj);
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}
	
	public static boolean archiveThread(DiscordChannel channel) {
		try {
			String reply = sendPatchRequest("/channels/"+ channel.id, new JSONObject().put("archived", true));
			//JSONObject obj = new JSONObject(reply);
			return true;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return false;
	}
	
	public static void interactionRespond(String interaction_id, String interaction_token, String content) {
		if (content == null) {
			sendPostRequest("/interactions/"+ interaction_id + "/" + interaction_token + "/callback", new JSONObject().put("type", 6));
		}
		else {
			JSONObject data = new JSONObject();
			data.put("content", content);
			data.put("flags", 64);
			sendPostRequest("/interactions/"+ interaction_id + "/" + interaction_token + "/callback", new JSONObject().put("type", 4).put("data", data));
		}
	}
	
	private static synchronized String sendPostRequest(String request, JSONObject content) {
//		Thread.dumpStack();
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
			
			if (c.getResponseCode() != 200 && c.getResponseCode() != 201 && c.getResponseCode() != 204) {
				LOGGER.warning("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString() + " - Loadout: " + content.toString());
				Sentry.capture("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString() + " - Loadout: " + content.toString());
				if (c.getResponseCode() == 429 || c.getResponseCode() == 502) {
					try {
						Thread.sleep(1000);
						return sendPostRequest(request, content);
					} catch (InterruptedException e) {
						LOGGER.log(Level.WARNING, "Exception: ", e);
						Sentry.capture(e);
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
				} catch (ClassCastException | NullPointerException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
					Sentry.capture(e);
				}
			}
			c.disconnect();
			
			LOGGER.fine("API call complete for " + request + ": " + fullmsg);
			return fullmsg;
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}
	
	private static synchronized String sendPatchRequest(String request, JSONObject content) {
//		Thread.dumpStack();
		try {
			
			URL url = new URL(api_url + api_version + request);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			
			HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .build();
			
			HttpRequest patchRequest = HttpRequest.newBuilder()
					.uri(URI.create(api_url + api_version + request))
					.method("PATCH", HttpRequest.BodyPublishers.ofString(content.toString()))
					.header("User-Agent", "Bot")
					.header("Authorization", "Bot " + DiscordBot.getToken())
					.header("Content-Type", "application/json")
					.build();
			
			HttpResponse response = httpClient.send(patchRequest,HttpResponse.BodyHandlers.ofString());
			
			return response.toString();
			
		} catch (InterruptedException | IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}
	
	private static synchronized String sendGetRequest(String request) {
//		Thread.dumpStack();
		try {
			URL url = new URL(api_url + api_version + request);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("User-Agent", "Bot");
			c.setRequestProperty("Authorization", "Bot " + DiscordBot.getToken());
			
			if (c.getResponseCode() != 200 && c.getResponseCode() != 201 && c.getResponseCode() != 204) {
				LOGGER.warning("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString());
				Sentry.capture("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString());
				if (c.getResponseCode() == 429 || c.getResponseCode() == 502) {
					try {
						Thread.sleep(1000);
						return sendGetRequest(request);
					} catch (InterruptedException e) {
						LOGGER.log(Level.WARNING, "Exception: ", e);
						Sentry.capture(e);
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
				} catch (ClassCastException | NullPointerException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
					Sentry.capture(e);
				}
			}
			c.disconnect();

			LOGGER.fine("API call complete for " + request + ": " + fullmsg);
			return fullmsg;
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}

	
	private static synchronized String sendDeleteRequest(String request) {
//		Thread.dumpStack();
		try {
			URL url = new URL(api_url + api_version + request);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("DELETE");
			c.setRequestProperty("User-Agent", "Bot");
			c.setRequestProperty("Authorization", "Bot " + DiscordBot.getToken());
			
			if (c.getResponseCode() != 200 && c.getResponseCode() != 201 && c.getResponseCode() != 204) {
				LOGGER.warning("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString());
				Sentry.capture("API call failed: (" + c.getResponseCode() + ") " + c.getResponseMessage() + " for " + url.toString());
				if (c.getResponseCode() == 429 || c.getResponseCode() == 502) {
					try {
						Thread.sleep(1000);
						return sendGetRequest(request);
					} catch (InterruptedException e) {
						LOGGER.log(Level.WARNING, "Exception: ", e);
						Sentry.capture(e);
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
				} catch (ClassCastException | NullPointerException e) {
					LOGGER.log(Level.WARNING, "Exception: ", e);
					Sentry.capture(e);
				}
			}
			c.disconnect();

			LOGGER.fine("API call complete for " + request + ": " + fullmsg);
			return fullmsg;
			
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}
	
	
	public static JSONObject createDM(String userid) {
		try {
			String reply = sendPostRequest("/users/@me/channels", new JSONObject().put("recipient_id", userid));
			JSONObject obj = new JSONObject(reply);
			return obj;
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
		return null;
	}

	public static JSONArray requestDM() {
		String reply = sendGetRequest("/users/@me/channels");
		if (reply != null && !reply.isEmpty()) {
			try {
				return new JSONArray(reply);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
				Sentry.capture(e);
			}
		}
		return null;
	}

	public static JSONObject requestUser(String userID) {
		String reply = sendGetRequest("/users/" + userID);
		if (reply != null && !reply.isEmpty()) {
			try {
				return new JSONObject(reply);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
				Sentry.capture(e);
			}
		}
		return null;
	}

	public static JSONObject requestChannel(String channelID) {
		String reply = sendGetRequest("/channels/" + channelID);
		if (reply != null && !reply.isEmpty()) {
			try {
				return new JSONObject(reply);
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
				Sentry.capture(e);
			}
		}
		return null;
	}
	
	public static JSONArray requestUserGuildRoles(String guild, String userID) {
		String reply = sendGetRequest("/guilds/" + guild + "/members/" + userID);
		if (reply != null && !reply.isEmpty()) {
			try {
				return new JSONObject(reply).getJSONArray("roles");
			} catch (JSONException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
				Sentry.capture("Unable to send msg, session is closed.");
			}
		}
		return null;
	}

}
