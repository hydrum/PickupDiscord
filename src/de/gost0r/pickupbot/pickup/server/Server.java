package de.gost0r.pickupbot.pickup.server;

public class Server {

	public int id;
	
	public String IP;
	public int port;
	public String rconpassword;
	public String password;
	public boolean active;
	
	
	private boolean taken;
	

	public Server(int id, String ip, int port, String rconpassword, String password, boolean active) {
		this.id = id;
		this.IP = ip;
		this.port = port;
		this.rconpassword = rconpassword;
		this.password = password;
		this.active = active;
	}


	public void sendRcon(String rconString) {
		// TODO 
		
	}

	public boolean isTaken() {
		return taken;
	}

}
