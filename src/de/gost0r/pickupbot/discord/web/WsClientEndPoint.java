package de.gost0r.pickupbot.discord.web;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import de.gost0r.pickupbot.discord.DiscordBot;
import io.sentry.Sentry;

@ClientEndpoint
public class WsClientEndPoint {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	Session userSession = null;
	private MessageHandler messageHandler;
	
	DiscordBot bot;
	
	URI endpointURI = null;
	
    public WsClientEndPoint(DiscordBot bot, URI endpointURI) {
    	this.bot = bot;
    	this.endpointURI = endpointURI;
    	connect();
    }
    
    private void connect() {
        try {
    		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (DeploymentException | IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
            Sentry.capture(e);
		}
    }
    
    public void reconnect() {
		try {
	    	if (userSession != null && userSession.isOpen()) {
				userSession.close();
	    	}
    		connect();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
            Sentry.capture(e);
		}
    }
    
    @OnOpen
    public void onOpen(Session userSession) {
		LOGGER.warning("WebSocket opened. ");
        Sentry.capture("WebSocket opened. ");
        this.userSession = userSession;
    }
    
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
		LOGGER.warning("WebSocket closed. " + reason.toString());
        Sentry.capture("WebSocket closed. " + reason.toString());
        this.userSession = null;
        bot.reconnect();
    }

    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendMessage(String message) {
    	if (this.userSession != null && this.userSession.isOpen()) {
    		this.userSession.getAsyncRemote().sendText(message);
    	} else {
    		LOGGER.warning("Unable to send msg, session is closed.");
            Sentry.capture("Unable to send msg, session is closed.");
    	}
    }
    
    public boolean isConnected() {
    	return this.userSession != null && this.userSession.isOpen();
    }

    public static interface MessageHandler {
        public void handleMessage(String message);
    }
    
}
