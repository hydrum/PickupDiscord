package de.gost0r.pickupbot.discord.web;

import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.web.WsClientEndPoint.MessageHandler;

public class DiscordGateway implements MessageHandler {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Timer heartbeatTimer = new Timer();
	private HeartbeatTask heartbeatTask;
	
	private WsClientEndPoint clientEP;
	private DiscordPacket latestPacket;
	
	private DiscordBot bot;
	
	public DiscordGateway(DiscordBot bot, WsClientEndPoint clientEP) {
		this.bot = bot;
		this.clientEP = clientEP;
	}
	
	public void sendMessage(String msg) {		
		if (clientEP.isConnected()) {
			
			msg = msg.replaceAll("\"__null__\"", "null");

			LOGGER.finer("OUT DiscordPacket: " + msg);
			clientEP.sendMessage(msg);
		} else {
			LOGGER.info("Cancel heartbeatTimer");
			heartbeatTimer.cancel();
		}
	}

	@Override
	public void handleMessage(String message) {
		try {
			JSONObject jsonObj = new JSONObject(message);
			
			// Parse to packet
			int op = jsonObj.getInt("op");
			JSONObject d = jsonObj.isNull("d") ? null : jsonObj.getString("d");
			int s = jsonObj.isNull("s") ? -1 : jsonObj.getInt("s") ;
			String t = jsonObj.isNull("t") ? null : jsonObj.getString("t");
			DiscordPacket incPak = new DiscordPacket(DiscordGatewayOpcode.values()[op], d, s, t);
			
			// DON'T LOG THIS EVENT [spamming all user info of the server]
			if (t == null || !t.equals(DiscordGatewayEvent.GUILD_CREATE.name())) {
				LOGGER.finer("INC DiscordPacket: " + incPak.toString());
			}
			handlePacket(incPak);
			
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception for '" + message + "': ", e);
		}
	}
	
	private void handlePacket(DiscordPacket p) {
		latestPacket = p;
		switch(p.op) {
			case Hello: handlePacketHello(p); break;
			case Dispatch: bot.handleEvent(DiscordGatewayEvent.valueOf(p.t), p.d); break;
			default: break;
		}
	}

	private void handlePacketHello(DiscordPacket p) {
		// setting heartbeat timer
		try {
			if (heartbeatTask != null) {
				heartbeatTask.cancel();
			}
			heartbeatTask = new HeartbeatTask(this);			
			long interval = p.d.getLong("heartbeat_interval");
			heartbeatTimer.scheduleAtFixedRate(heartbeatTask, interval/2, interval);			
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		
		// sending identify
		try {
			JSONObject msg = new JSONObject();
			msg.put("token", DiscordBot.getToken());
			
			JSONObject prop = new JSONObject();
			prop.put("$os", "linux");
			prop.put("$browser", "urtpickupbot");
			prop.put("$device", "urtpickupbot");
			msg.put("properties", prop);

			msg.put("compress", false);
			msg.put("large_threshold", 50);
			JSONArray ar = new JSONArray("[0, 1]");
			msg.put("shard", ar);

			JSONObject presence = new JSONObject();
			presence.put("since", "__null__");
			presence.put("game", "__null__");
			presence.put("status", "online");
			presence.put("afk", false);
			msg.put("presence", presence);
			
			DiscordPacket outP = new DiscordPacket(DiscordGatewayOpcode.Identify, msg, -1, "__null__");
			
			sendMessage(outP.toSend());
			
		} catch (JSONException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		
	}
	
	public void setClientEP(WsClientEndPoint clientEP) {
		this.clientEP = clientEP;
	}
	
	public void reconnect() {
		// do nothing for now.
	}
	
	public int getLatestSeq() {
		if (latestPacket == null) {
			return -1;
		}
		return latestPacket.s;
	}
}
