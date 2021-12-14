package de.gost0r.pickupbot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.pickup.Country;
import de.gost0r.pickupbot.pickup.PickupBot;

import io.sentry.Sentry;

public class PickupBotDiscordMain {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "EN"));
		try {
		
			setupLogger();
			
			Country.initCountryCodes();

			String config = new String(Files.readAllBytes(Paths.get("config.json")));
			JSONObject cfg = new JSONObject(config);
			DiscordBot.setToken(cfg.getString("token"));
			
			PickupBot bot = new PickupBot();
			bot.init();

//			while (true) {
//				Thread.sleep(5000);
//			}
		} catch (IOException | JSONException | SecurityException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
			Sentry.capture(e);
		}
	}
	
	public static void setupLogger() throws SecurityException, IOException {

		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s - %2$s(): %5$s%6$s%n");
		Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.WARNING);

		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.WARNING);
		logger.addHandler(handler);

		FileHandler logfile = new FileHandler("bot.log");
		logfile.setFormatter(new SimpleFormatter());
		logfile.setLevel(Level.WARNING);
		logger.addHandler(logfile);

		Sentry.init("https://444a1a8bd3044cfa8f7d84f2703a500c@sentry.ftwgl.net/28");

		LOGGER.severe("Bot started.");
		Sentry.capture("Bot started");
	}
}
