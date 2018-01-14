package de.gost0r.pickupbot.discord.web;

import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.web.WsClientEndPoint.MessageHandler;

public class DiscordGateway implements MessageHandler{
	
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
			System.out.println("SendMsg: " + msg);
			
			clientEP.sendMessage(msg);
		} else {
			heartbeatTimer.cancel();
		}
	}

	@Override
	public void handleMessage(String message) {
		try {
			JSONObject jsonObj = new JSONObject(message);
			System.out.println("INC " + jsonObj);
			
			// Parse to packet
			int op = jsonObj.getInt("op");
			JSONObject d = jsonObj.isNull("d") ? null : new JSONObject(jsonObj.getString("d"));
			int s = jsonObj.isNull("s") ? -1 : jsonObj.getInt("s") ;
			String t = jsonObj.isNull("t") ? null : jsonObj.getString("t");
			DiscordPacket incPak = new DiscordPacket(DiscordGatewayOpcode.values()[op], d, s, t);

//			System.out.println("IncPacket: " + incPak);
			handlePacket(incPak);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void handlePacket(DiscordPacket p) {
		latestPacket = p;
		switch(p.op) {
			case Hello: handlePacketHello(p); break;
			case Dispatch: bot.handleEvent(DiscordGatewayEvent.valueOf(p.t), p.d);
			default: break;
		}
	}

	private void handlePacketHello(DiscordPacket p) {
		// setting heartbeat timer
		try {
			heartbeatTask = new HeartbeatTask(this);			
			long interval = p.d.getLong("heartbeat_interval");
			heartbeatTimer.scheduleAtFixedRate(heartbeatTask, interval/2, interval);			
		} catch (JSONException e) {
			e.printStackTrace();
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
			presence.put("status", "invisible");
			presence.put("afk", false);
			msg.put("presence", presence);
			
			DiscordPacket outP = new DiscordPacket(DiscordGatewayOpcode.Identify, msg, -1, "__null__");
			
			sendMessage(outP.toSend());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public int getLatestSeq() {
		if (latestPacket == null) {
			return -1;
		}
		return latestPacket.s;
	}
}
