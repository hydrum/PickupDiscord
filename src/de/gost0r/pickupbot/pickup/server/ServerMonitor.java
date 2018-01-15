package de.gost0r.pickupbot.pickup.server;

import java.util.ArrayList;
import java.util.List;

import de.gost0r.pickupbot.pickup.Match;
import de.gost0r.pickupbot.pickup.MatchStats;
import de.gost0r.pickupbot.pickup.Player;
import de.gost0r.pickupbot.pickup.server.ServerPlayer.ServerPlayerState;

public class ServerMonitor implements Runnable {
	
	public static enum ServerState {
		WELCOME,
		WARMUP,
		LIVE,
		SCORE
	}
	
	private Server server;
	private Match match;
	
	private boolean stopped;
	
	int score[][] = new int[2][2]; 
	
	
	private List<ServerPlayer> players;
	private String gameTime;
	private ServerState state;
	private boolean firstHalf;
	private boolean swapRoles;

	public ServerMonitor(Server server, Match match) {
		this.server = server;
		this.match = match;
		
		this.stopped = false;
		state = ServerState.WELCOME;
		
		players = new ArrayList<ServerPlayer>();
	}

	@Override
	public void run() {
		while (!stopped) {
			observe();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop() {
		stopped = true;
	}
	
	private void observe() {
		RconPlayersParsed rpp = parseRconPlayers();		
		gameTime = rpp.gametime;
		swapRoles = getSwapRoles();
		
		updatePlayers(rpp);		
		evaluateState(rpp);
		
		forceplayers();
		
		if (state == ServerState.WELCOME)
		{
			if (firstHalf) {
				checkMatchStart();
			}
		}
		else if (state == ServerState.WARMUP)
		{
			// Do nothing
		}
		else if (state == ServerState.LIVE)
		{
			
		}
		else if (state == ServerState.SCORE)
		{
			if (swapRoles && firstHalf) {
				firstHalf = false;
			}
		}		
	}

	private void checkMatchStart() {

//		if (players.size() >= 10 && (!rpp.matchready[0] || !rpp.matchready[1])) {
//			server.sendRcon("forceready");		
//			
//		}
		if (players.size() < 10) {
			int[] time = new int[3];
			time[0] = Integer.valueOf(gameTime.split(":")[0]); // hours
			time[1] = Integer.valueOf(gameTime.split(":")[1]); // mins
			time[2] = Integer.valueOf(gameTime.split(":")[2]); // secs
			if (time[1] < 5) {
				// ABORT GAME
				server.sendRcon("bigtext \"ABORT MATCH DUE TO NOSHOW.");
				System.out.println("MATCH ABORT");
				match.abort();
				stop();
			}
		}
	}

	private void saveStats(RconPlayersParsed rpp) {
		int half = firstHalf ? 0 : 1;
		
		score[half] = rpp.scores;

		// save playerscores
		for (ServerPlayer player : players) {
			try {
				match.getStats(player.player).score[half].score = Integer.valueOf(player.ctfstats.score);
				match.getStats(player.player).score[half].deaths = Integer.valueOf(player.ctfstats.deaths);
				match.getStats(player.player).score[half].assists = Integer.valueOf(player.ctfstats.assists);
				match.getStats(player.player).score[half].caps = Integer.valueOf(player.ctfstats.caps);
				match.getStats(player.player).score[half].returns = Integer.valueOf(player.ctfstats.returns);
				match.getStats(player.player).score[half].fc_kills = Integer.valueOf(player.ctfstats.fc_kills);
				match.getStats(player.player).score[half].stop_caps = Integer.valueOf(player.ctfstats.stop_caps);
				match.getStats(player.player).score[half].protect_flag = Integer.valueOf(player.ctfstats.protect_flag);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void forceplayers() {
		
		for (ServerPlayer sp : players) {
			if (sp.state == ServerPlayerState.Connected || sp.state == ServerPlayerState.Reconnected) {
				Player player = Player.get(sp.auth);				
				if (player != null) {
					sp.player = player;
					match.getStats(player).updateStatus(MatchStats.Status.PLAYING);
					match.getStats(player).updateIP(sp.ip);
				} else { // if player not authed or player unknown -> kick
					System.out.println("Didn't find " + sp.auth + " in the registered player list. -> kick");
					server.sendRcon("kick " + sp.id);
					continue;
				}
				
				String team = match.getTeam(player);
				if (team == null) { // player not in match
					System.out.println("Player " + sp.auth + " is not listed as match player. -> kick");
					server.sendRcon("KICK " + sp.id);
				}
				
				if (team != null && state != ServerState.SCORE) {
						String oppTeam = team.equalsIgnoreCase("red") ? "blue" : "red";
						if (!sp.team.equalsIgnoreCase(team) && firstHalf)
						{
							System.out.println("Player " + sp.auth + " is in the wrong team.");
							server.sendRcon("force " + sp.id + " " + team.toUpperCase());
						}
						else if (sp.team.equalsIgnoreCase(team) && !firstHalf)
						{
							System.out.println("Player " + sp.auth + " is in the wrong team.");
							server.sendRcon("force " + sp.id + " " + oppTeam.toUpperCase());
						}
				}
			
			} else { // not active
				if (sp.player != null && match.getStats(sp.player).getStatus() != MatchStats.Status.LEFT) { 
					match.getStats(sp.player).updateIP(sp.ip);
					match.getStats(sp.player).updateStatus(MatchStats.Status.LEFT);
				}
			}
		}
	}
	


	private void evaluateState(RconPlayersParsed rpp) {
		if (state == ServerState.WELCOME) {
			if (rpp.matchready[0] && rpp.matchready[1] && rpp.warmupphase) {
				state = ServerState.WARMUP;
			}
		} else if (state == ServerState.WARMUP) {
			if (rpp.matchready[0] && rpp.matchready[1] && !rpp.warmupphase) {
				state = ServerState.LIVE;
			}
		} else if (state == ServerState.LIVE && rpp.gametime.equals("00:00:00")) {
			if (!rpp.matchready[0] && !rpp.matchready[1]) {
				state = ServerState.SCORE;
				saveStats(rpp);
				if (!swapRoles || (swapRoles && !firstHalf)) {
					endGame();
				}
			}
		} else if (state == ServerState.SCORE && !rpp.gametime.equals("00:00:00")) {
			if (!rpp.matchready[0] && !rpp.matchready[1] && !rpp.warmupphase) {
				state = ServerState.WELCOME;
			} else if (rpp.matchready[0] && rpp.matchready[1] && rpp.warmupphase) {
				state = ServerState.WARMUP;
			}
		}
	}

	private void updatePlayers(RconPlayersParsed rpp) {
		List<ServerPlayer> oldPlayers = new ArrayList<ServerPlayer>(players);
		List<ServerPlayer> newPlayers = new ArrayList<ServerPlayer>();
		
		for (ServerPlayer player : rpp.players) {
			
			// find player in serverplayerlist
			ServerPlayer found = null;
			for (ServerPlayer player_x : players) {
				if (player_x.auth.equalsIgnoreCase(player.auth)) {
					found = player_x;
					break;
				}
			}
			
			if (found != null) {
				if (found.state == ServerPlayerState.Disconnected) {
					System.out.println("Player " + found.auth + " reconnected.");
					found.state = ServerPlayerState.Reconnected;
				}
				oldPlayers.remove(found);
			} else {
				System.out.println("Player " + player.auth + " connected.");
				newPlayers.add(player);
			}
		}

		for (ServerPlayer player : oldPlayers) {
			if (player.state != ServerPlayerState.Disconnected) {
				player.state = ServerPlayerState.Disconnected;
			}
			System.out.println("Player " + player.auth + " disconnected.");
		}
		
		for (ServerPlayer player : newPlayers) {
			players.add(player);
		}
	}
	
	private boolean getSwapRoles() {
		String swaproles = server.sendRcon("g_swaproles");
		String[] split = swaproles.split("\"");
		if (split.length > 4) {
			return split[3].equals("1");
		}
		return false;
	}

	private RconPlayersParsed parseRconPlayers() {

		RconPlayersParsed rpp = new RconPlayersParsed();
		
		String playersString = server.sendRcon("players");
		String[] stripped = playersString.split("\n");
		
		for (int i = 0; i < stripped.length; i++) {
			
			if (stripped[i].startsWith("Map:"))
			{
				rpp.map = stripped[i].split(" ")[1];
			}
			else if (stripped[i].startsWith("Players"))
			{
				rpp.playercount = Integer.valueOf(stripped[i].split(" ")[1]);				
			}
			else if (stripped[i].startsWith("GameType"))
			{
				rpp.gametype = stripped[i].split(" ")[1];
			}
			else if (stripped[i].startsWith("Scores"))
			{
				rpp.scores[0] = Integer.valueOf(stripped[i].split(" ")[1].split(":")[1]);
				rpp.scores[1] = Integer.valueOf(stripped[i].split(" ")[2].split(":")[1]);
			}
			else if (stripped[i].startsWith("MatchMode"))
			{
				rpp.matchmode = stripped[i].split(" ")[1].equals("ON") ? true : false;
			}
			else if (stripped[i].startsWith("MatchReady"))
			{
				rpp.matchready[0] = stripped[i].split(" ")[1].split(":")[1].equals("YES") ? true : false;
				rpp.matchready[1] = stripped[i].split(" ")[2].split(":")[1].equals("YES") ? true : false;
			}
			else if (stripped[i].startsWith("WarmupPhase"))
			{
				rpp.warmupphase = stripped[i].split(" ")[1].equals("YES") ? true : false;
			}
			else if (stripped[i].startsWith("GameTime"))
			{
				rpp.gametime = stripped[i].split(" ")[1];
			}
			else
			{
				
				String[] line = stripped[i].split(" ");
				
				if (line[0].equals("[connecting]")) continue;
				
				if (line[0].equals("CTF:")) {
					// ctfstats
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).caps = line[1].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).returns = line[2].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).fc_kills = line[3].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).stop_caps = line[4].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).protect_flag = line[5].split(":")[1];
				}
				else if (rpp.players.size() < rpp.playercount) // TODO: use something else
				{
					ServerPlayer sp = new ServerPlayer();
					sp.state = ServerPlayerState.Connected;
					sp.id = line[0].split(":")[0];
					sp.name = line[0].split(":")[1];
					sp.team = line[1].split(":")[1];
					sp.ctfstats.score = line[2].split(":")[1];
					sp.ctfstats.deaths = line[3].split(":")[1];
					sp.ctfstats.assists = line[4].split(":")[1];
					sp.ping = line[5].split(":")[1];
					sp.auth = line[6].split(":")[1];
					sp.ip = line[7].split(":")[1];
					
					rpp.players.add(sp);
				}
				
			}
		}
//		System.out.println(rpp.toString());
		return rpp;
	}
	
	private void endGame() {

		// PROCEED WITH STATS/ELO GENERATING

        int redscore = score[0][0] + score[1][1]; //score_red_first + score_blue_second;
        int bluescore = score[0][1] + score[1][0]; //score_blue_first + score_red_second;
        int[] finalscore = { redscore, bluescore };
        for (ServerPlayer player : players) {
        	if (player.player != null) {
        		int team = match.getTeam(player.player).equalsIgnoreCase("red") ? 0 : 1;
        		int opp = (team + 1) % 2;
        		
    			int e = (int) (1 / (1 + Math.pow((double) 10, (double)((match.getElo()[opp] - player.player.getElo()) / 400))));
    			double d = 1;
    			int w = 1;
    			if (finalscore[team] > finalscore[opp]) { // victory
    				w = 1;
    			} else if (finalscore[team] < finalscore[opp]) { // loss
    				w = 0;
    			} else { // draw
    				w = 1;
    				d = 0.5;
    			}
    			int elochange = (int) (32 * (w-e) * d);
    			int newelo = player.player.getElo() + elochange;
    			System.out.println("ELO player: " + player.auth + " old ELO: " + player.player.getElo() + " new ELO: " + newelo + " (" + (!String.valueOf(elochange).startsWith("-") ? "+" : "" + elochange));
    			player.player.addElo(elochange);
        	}
        }
        match.setScore(finalscore);
        match.end();
		stop();
	}
}
