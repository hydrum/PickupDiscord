package de.gost0r.pickupbot.discord.web;

import io.sentry.Sentry;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatTask extends TimerTask {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private DiscordGateway dg;

    public HeartbeatTask(DiscordGateway dg) {
        this.dg = dg;
    }

    @Override
    public void run() {
        try {
            sendHeatbeat();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HeartbeatTask failed: ", e);
            Sentry.captureException(e);
        }
    }

    private void sendHeatbeat() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("op", DiscordGatewayOpcode.Heatbeat.ordinal());
            msg.put("d", dg.getLatestSeq() < 0 ? "__null__" : dg.getLatestSeq()); // last sequence number
            dg.sendMessage(msg.toString());
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "Exception: ", e);
            Sentry.captureException(e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected exception in heartbeat: ", e);
            Sentry.captureException(e);
        }
    }

}