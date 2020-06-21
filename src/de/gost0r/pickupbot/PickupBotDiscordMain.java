package de.gost0r.pickupbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
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
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.pickup.Country;
import de.gost0r.pickupbot.pickup.PickupBot;

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
			
			// TEST: make admin chan to pub chan.
		//	DiscordChannel targetChannel = DiscordChannel.findChannel("687958620755066938");
		//	bot.sendMsg(targetChannel, "test");
			
//			if (!bot.logic.getChannelByType(PickupChannelType.PUBLIC).contains(targetChannel)) 
//			{
//				bot.logic.addChannel(PickupChannelType.PUBLIC, targetChannel);
//			}
//			targetChannel = DiscordChannel.findChannel("402541587164561419");
//			if (bot.logic.getChannelByType(PickupChannelType.PUBLIC).contains(targetChannel)) 
//			{
//				bot.logic.removeChannel(PickupChannelType.PUBLIC, targetChannel);
//			}
			
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
		
//		serverTest();
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
	
	public static void serverTest() {
		
		try {
			DatagramChannel channel = DatagramChannel.open();
			channel.configureBlocking(true);
			channel.connect(new InetSocketAddress("sd.biddle.cf", 27960));
			
			String rconpassword = "HereWeGo";
			String rcon = "xxxxrcon " + rconpassword + " players";
			
			byte[] sendBuffer = rcon.getBytes();
			
			sendBuffer[0] = (byte) 0xff;
			sendBuffer[1] = (byte) 0xff;
			sendBuffer[2] = (byte) 0xff;
			sendBuffer[3] = (byte) 0xff;
			
			ByteBuffer buf = ByteBuffer.allocate(2048);
			buf.clear();
			buf.put(sendBuffer);
			buf.flip();
			//int bytesWritten = channel.write(buf);
			
			buf = ByteBuffer.allocate(2000);
			buf.clear();
			
			int bytesRead = 0;
			while ((bytesRead = channel.read(buf)) > 0)
			{
				System.out.println(bytesRead);
	    		String newString = new String(buf.array());
	    		
				buf = ByteBuffer.allocate(2000);
	    		buf.clear();
	    		
	    		System.out.println(newString);
			}
			System.out.println("exit");
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
