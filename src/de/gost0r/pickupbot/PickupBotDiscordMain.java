package de.gost0r.pickupbot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.pickup.PickupBot;

public class PickupBotDiscordMain {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "EN"));
		try {
		
			setupLogger();

			String config = new String(Files.readAllBytes(Paths.get("config.json")));
			JSONObject cfg = new JSONObject(config);
			DiscordBot.setToken(cfg.getString("token"));
			
			DiscordBot bot = new PickupBot();
			bot.init();
			
			while (true) {
					Thread.sleep(5000);
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		} catch (SecurityException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		
//		eloTest();
	}
	
	public static void setupLogger() throws SecurityException, IOException {

        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s - %2$s(): %5$s%6$s%n");
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.ALL);
        
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
		
        FileHandler logfile = new FileHandler("bot.log");
        logfile.setFormatter(new SimpleFormatter());
        logfile.setLevel(Level.ALL);
        logger.addHandler(logfile);
        
        LOGGER.severe("Bot started.");
	}
	
	public static void eloTest() {
		int eloSelf = 1000;
		int eloOpp = 1000;

		double w = 1; // 1 win, 0.5 draw, 0 loss
		
		double tSelf = Math.pow(10d, eloSelf/400d);
		double tOpp = Math.pow(10d, eloOpp/400d);
		double e = tSelf / (tSelf + tOpp);
		
		double resultSelf = 32d * (w - e);
		int elochange = (int) Math.floor(resultSelf);
		System.out.println(elochange);
	}

}
