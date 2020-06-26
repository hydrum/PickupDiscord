package de.gost0r.pickupbot.pickup.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.pickup.Match;
import de.gost0r.pickupbot.pickup.Region;

public class Server {
	
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public int id;
	
	public String IP;
	public int port;
	public String rconpassword;
	public String password;
	public boolean active;
	public Region region;
		
	private boolean taken = false;
	
	private DatagramSocket socket;
	
	private ServerMonitor monitor;
	private Thread monitorThread;

	public Server(int id, String ip, int port, String rconpassword, String password, boolean active, Region region) {
		this.id = id;
		this.IP = ip;
		this.port = port;
		this.rconpassword = rconpassword;
		this.password = password;
		this.active = active;
		this.region = region;
		
		connect();
		monitor = null;
	}

	public void connect() {
		try {
			this.socket = new DatagramSocket();
			this.socket.setSoTimeout(1000);
		} catch (SocketException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}


	public synchronized String sendRcon(String rconString) {
		try {
			if (this.socket.isClosed()) {
				LOGGER.severe("SOCKET IS CLOSED");
				connect();
			}
			String rcon = "xxxxrcon " + rconpassword + " " + rconString;
			
			byte[] recvBuffer = new byte[2048];
			byte[] sendBuffer = rcon.getBytes();
			
			sendBuffer[0] = (byte) 0xff;
			sendBuffer[1] = (byte) 0xff;
			sendBuffer[2] = (byte) 0xff;
			sendBuffer[3] = (byte) 0xff;

			LOGGER.fine(rcon);

			DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, getInetIP(), port);
			DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
			this.socket.send(sendPacket);
			
			String string = "";
	        while (true) {
	        	try {
	        		this.socket.receive(recvPacket);
	        		String newString = new String(recvPacket.getData());
	        		
	        		newString = newString.substring(4); // remove the goddamn first 4 chars
	        		
	        		string += newString;

	    			recvBuffer = new byte[2048]; // empty buffer
	        		recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
	        	} catch (SocketTimeoutException e) {
	        		break;
	        	}       
	        }

	        string = string.replace("" + (char) 0, "");
	        
	        // Thread.sleep(100);
	        return string;
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
        return null;
	}
	
	
	public synchronized String pushRcon(String rconString) {
		try {
			if (this.socket.isClosed()) {
				LOGGER.severe("SOCKET IS CLOSED");
				connect();
			}
			String rcon = "xxxxrcon " + rconpassword + " " + rconString;
			
			byte[] sendBuffer = rcon.getBytes();
			
			sendBuffer[0] = (byte) 0xff;
			sendBuffer[1] = (byte) 0xff;
			sendBuffer[2] = (byte) 0xff;
			sendBuffer[3] = (byte) 0xff;

			LOGGER.fine(rcon);

			DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, getInetIP(), port);
			this.socket.send(sendPacket);

		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
        return null;
	}
	
	
	public void startMonitoring(Match match) {
		if (this.monitor == null) {
			this.monitor = new ServerMonitor(this, match);
			monitorThread = new Thread(this.monitor);
			monitorThread.start();
		}
	}
	
	public void stopMonitoring() {
		if (monitor != null) {
			this.monitor.stop();
			this.monitor = null;
		}
	}

	public void take() {
		taken = true;
	}
	
	public void free() {
		taken = false;
		stopMonitoring();
	}

	public ServerMonitor getServerMonitor() {
		return monitor;
	}

	public InetAddress getInetIP() {
		try {
			return InetAddress.getByName(IP);
		} catch (UnknownHostException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return null;
	}

	public boolean isTaken() {
		return taken;
	}
	
	@Override
	public String toString() {
		String isActive = this.active ? "active" : "inactive";
		return "#" + id + " " + IP + ":" + port + " " + isActive + " " + region + " " + isReachable() ;
	}
	
	public String isReachable() {
		String status = ":red_circle: (Host Timeout)";
		
		try {
			InetAddress.getByName(IP).isReachable(1000);
		} catch (UnknownHostException e) {
			return status;
		} catch (IOException e) {
			return status;
		}
		
		String rconStatusAck = sendRcon("status");
		
		if(rconStatusAck.contains("num score ping name"))
		{
			//rcon is correct and server is up
			status = ":green_circle:";
		}
		else if (rconStatusAck.contains("Bad rconpassword"))
		{
			// server is up bud rcon is wrong
			status = ":yellow_circle: (bad rconpassword)";
		}
		else if (rconStatusAck.contains("No rconpassword set on the server."))
		{
			// server is up bud rcon is wrong
			status = ":yellow_circle: (no rconpassword set on the server)";
		}
		else
		{
			// urban terror server is down
			status = ":red_circle: (server down)";
		}
		
		return status;
	}

	public String getAddress() {
		return IP + ":" + port;
	}

}
