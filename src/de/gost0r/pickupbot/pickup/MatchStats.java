package de.gost0r.pickupbot.pickup;

public class MatchStats {
	
	public enum STATUS {
		PLAYING,
		TIMEOUT,
		LEFT,
		NOSHOW,
		RINGER,
		RINGER_RAGEQUIT
	}
	
	private String ip;	
	private STATUS status = STATUS.NOSHOW;
	
	public Score[] score = new Score[2];
	
	public MatchStats() {
	}
	
	public MatchStats(Score score1, Score score2, String ip, String status) {
		this();
		score[0] = score1;
		score[1] = score2;
		this.ip = ip;
		this.status = STATUS.valueOf(status);
	}
	
	public void updateStatus(STATUS status) {
		this.status = status;
	}
	
	public STATUS getStatus() {
		return status;
	}
	
	public void updateIP(String ip) {
		this.ip = ip;
	}

	public String getIP() {
		return ip;
	}
	
}
