package de.gost0r.pickupbot.discord.web;

import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

public class HeartbeatTask extends TimerTask {

	private DiscordGateway dg;
	public HeartbeatTask(DiscordGateway dg) {
		this.dg = dg;
	}
	
	@Override
	public void run() {
		sendHeatbeat();
	}

	private void sendHeatbeat() {
		System.out.println("Sending Heartbeat...");
		try {
			JSONObject msg = new JSONObject();
			msg.put("op", DiscordGatewayOpcode.Heatbeat.ordinal());
			msg.put("d", dg.getLatestSeq() < 0 ? "__null__" : dg.getLatestSeq()); // last sequence number
			dg.sendMessage(msg.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}