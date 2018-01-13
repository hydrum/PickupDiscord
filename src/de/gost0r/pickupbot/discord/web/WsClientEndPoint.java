package de.gost0r.pickupbot.discord.web;

import java.io.IOException;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class WsClientEndPoint {
	
	Session userSession = null;
	private MessageHandler messageHandler;
	
    public WsClientEndPoint(URI endpointURI) {
        try {
        		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
				container.connectToServer(this, endpointURI);
			} catch (DeploymentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }
    
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket: " + reason.getReasonPhrase());
        this.userSession = null;
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
    		System.out.println("Unable to send msg, session is closed.");
    	}
    }
    
    public boolean isConnected() {
    	return this.userSession != null && this.userSession.isOpen();
    }

    public static interface MessageHandler {

        public void handleMessage(String message);
    }
    
}
