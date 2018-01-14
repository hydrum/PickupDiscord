package de.gost0r.pickupbot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.pickup.PickupBot;

public class PickupBotDiscordMain {
	

	public static void main(String[] args) {
		
		try {

			String config = new String(Files.readAllBytes(Paths.get("config.json")));
			JSONObject cfg = new JSONObject(config);
			DiscordBot.setToken(cfg.getString("token"));
			
			DiscordBot bot = new PickupBot();
			bot.init();
			
			while (true) {
					Thread.sleep(5000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
