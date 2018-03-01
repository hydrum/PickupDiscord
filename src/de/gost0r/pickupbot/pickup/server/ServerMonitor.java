package de.gost0r.pickupbot.pickup.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.pickup.Match;
import de.gost0r.pickupbot.pickup.MatchStats;
import de.gost0r.pickupbot.pickup.Player;
import de.gost0r.pickupbot.pickup.server.ServerPlayer.ServerPlayerState;

public class ServerMonitor implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
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
	
	private RconPlayersParsed prevRPP;

	public ServerMonitor(Server server, Match match) {
		this.server = server;
		this.match = match;
		
		this.stopped = false;
		state = ServerState.WELCOME;
		
		firstHalf = true;
		
		players = new ArrayList<ServerPlayer>();
	}

	@Override
	public void run() {
		LOGGER.severe("run() started");
		while (!stopped) {
			observe();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
			}
		}
		LOGGER.severe("run() ended");
	}
	
	public void stop() {
		stopped = true;
		LOGGER.severe("stop() called");
	}
	
	private void observe() {
		RconPlayersParsed rpp = parseRconPlayers();		
		gameTime = rpp.gametime;
		
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
			
		}		
	}

	private void checkMatchStart() {

//		if (players.size() >= 10 && (!rpp.matchready[0] || !rpp.matchready[1])) {
//			server.sendRcon("forceready");		
//			
//		}
//		if (players.size() < match.getGametype().getTeamSize()*2) {
//			int[] time = new int[3];
//			time[0] = Integer.valueOf(gameTime.split(":")[0]); // hours
//			time[1] = Integer.valueOf(gameTime.split(":")[1]); // mins
//			time[2] = Integer.valueOf(gameTime.split(":")[2]); // secs
//			if (time[1] < 5) {
//				// ABORT GAME
//				server.sendRcon("bigtext \"ABORT MATCH DUE TO NOSHOW.");
//				System.out.println("MATCH ABORT");
//				match.abort();
//				stop();
//			}
//		}
	}

	private void saveStats(int[] scorex) {
		int half = firstHalf ? 0 : 1;
		
		score[half] = scorex;
		
		// reset matchstats to previous
		for (ServerPlayer sp : prevRPP.players) {
			for (ServerPlayer player : players) {
				if (sp.equals(player)) {
					player.copy(sp);
					continue;
				}
			}
		}

		// save playerscores
		for (ServerPlayer player : players) {
			try {
				if (player.player != null && match.isInMatch(player.player)) {
					match.getStats(player.player).score[half].score = Integer.valueOf(player.ctfstats.score);
					match.getStats(player.player).score[half].deaths = Integer.valueOf(player.ctfstats.deaths);
					match.getStats(player.player).score[half].assists = Integer.valueOf(player.ctfstats.assists);
					match.getStats(player.player).score[half].caps = Integer.valueOf(player.ctfstats.caps);
					match.getStats(player.player).score[half].returns = Integer.valueOf(player.ctfstats.returns);
					match.getStats(player.player).score[half].fc_kills = Integer.valueOf(player.ctfstats.fc_kills);
					match.getStats(player.player).score[half].stop_caps = Integer.valueOf(player.ctfstats.stop_caps);
					match.getStats(player.player).score[half].protect_flag = Integer.valueOf(player.ctfstats.protect_flag);
				}
			} catch (NumberFormatException e) {
				LOGGER.log(Level.WARNING, "Exception: ", e);
			}
		}
		
	}

	private void forceplayers() {
		
		for (ServerPlayer sp : players) {
			if (sp.state == ServerPlayerState.Connected || sp.state == ServerPlayerState.Reconnected) {
				Player player = Player.get(sp.auth);				
				if (player != null && match.isInMatch(player)) {
					sp.player = player;
					match.getStats(player).updateStatus(MatchStats.Status.PLAYING);
					match.getStats(player).updateIP(sp.ip);
				} else if (player != null && match.getLogic().bot.hasAdminRights(player.getDiscordUser())) {
					// PLAYER IS AN ADMIN, DONT FORCE/KICK HIM
					continue;
				} else { // if player not authed, auth not registered or not playing in this match -> kick
					LOGGER.info("Didn't find " + sp.name + " (" + sp.auth + ") signed up for this match  -> kick");
					server.sendRcon("kick " + sp.id);
					continue;
				}
				
				String team = match.getTeam(player);
				if (team != null && state != ServerState.SCORE)
				{
					String oppTeam = team.equalsIgnoreCase("red") ? "blue" : "red";
					if (!sp.team.equalsIgnoreCase(team) && firstHalf)
					{
						LOGGER.info("Player " + sp.name + " (" + sp.auth + ") is in the wrong team. Supposed to be: " + team.toUpperCase() + " but currently " + sp.team);
						server.sendRcon("forceteam " + sp.id + " " + team.toUpperCase());
					}
					else if (!sp.team.equalsIgnoreCase(oppTeam) && !firstHalf) // we should have switched teams -.-
					{
						LOGGER.info("Player " + sp.name + " (" + sp.auth + ") is in the wrong team. Supposed to be: " + oppTeam.toUpperCase() + " but currently " + sp.team);
						server.sendRcon("forceteam " + sp.id + " " + oppTeam.toUpperCase());
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
		if (state == ServerState.WELCOME)
		{
			if (rpp.matchready[0] && rpp.matchready[1] && rpp.warmupphase)
			{
				state = ServerState.WARMUP;
				LOGGER.info("SWITCHED WELCOME -> WARMUP");
			}
			else if (rpp.matchready[0] && rpp.matchready[1] && !rpp.warmupphase)
			{
				state = ServerState.LIVE;
				LOGGER.info("SWITCHED WELCOME -> LIVE");
			}
		}
		else if (state == ServerState.WARMUP)
		{
			if (rpp.matchready[0] && rpp.matchready[1] && !rpp.warmupphase)
			{
				state = ServerState.LIVE;
				LOGGER.info("SWITCHED WARMUP -> LIVE");
			}
		}
		else if (state == ServerState.LIVE)
		{
			if (rpp.gametime.equals("00:00:00"))
			{
				state = ServerState.SCORE;
				LOGGER.info("SWITCHED LIVE -> SCORE");
			}
		}
		else if (state == ServerState.SCORE)
		{
			if (rpp.warmupphase) {
				if (rpp.matchready[0] && rpp.matchready[1])
				{
					state = ServerState.WARMUP;
					LOGGER.info("SWITCHED SCORE -> WARMUP");
				} else {
					state = ServerState.WELCOME;
					LOGGER.info("SWITCHED SCORE -> WELCOME");
				}
				handleScoreTransition();
			} else {
				if (getPlayerCount("red") == 0 || getPlayerCount("blue") == 0) {
					state = ServerState.WELCOME;
					LOGGER.info("SWITCHED SCORE -> WELCOME");
					handleScoreTransition();
				}
			}
		}
		prevRPP = rpp;
	}

	private int getPlayerCount(String team) {
		int count = 0;
		for (ServerPlayer player : players) {
			if (player.state == ServerPlayer.ServerPlayerState.Connected || player.state == ServerPlayer.ServerPlayerState.Reconnected) {
				if (player.team.equalsIgnoreCase(team)) {
					count++;
				}
			}
		}
		return count;
	}

	private void handleScoreTransition() {		
		swapRoles = getSwapRoles();
		
		saveStats(prevRPP.scores);
		if (!swapRoles || (swapRoles && !firstHalf)) {
			endGame();
		} else {
			firstHalf = false;
		}
	}

	private void updatePlayers(RconPlayersParsed rpp) {
		List<ServerPlayer> oldPlayers = new ArrayList<ServerPlayer>(players);
		List<ServerPlayer> newPlayers = new ArrayList<ServerPlayer>();
		
		for (ServerPlayer player : rpp.players) {
			
			if (player.state == ServerPlayerState.Connecting) continue; // ignore connecting players
			
			if (player.auth.equals("---")) {
				requestAuth(player);
			}
			
			// find player in serverplayerlist
			ServerPlayer found = null;
			for (ServerPlayer player_x : players) {
				if (player.equals(player_x)) {
					player_x.copy(player);
					found = player_x;
					break;
				}
			}
			
			if (found != null) {
				if (found.state == ServerPlayerState.Disconnected) {
					found.state = ServerPlayerState.Reconnected;
					LOGGER.info("Player " + player.name + " (" + found.auth + ") reconnected.");
				}
				oldPlayers.remove(found);
			} else {
				LOGGER.info("Player " + player.name + " (" + player.auth + ") connected.");
				newPlayers.add(player);
			}
		}

		for (ServerPlayer player : oldPlayers) {
			if (player.state != ServerPlayerState.Disconnected) {
				player.state = ServerPlayerState.Disconnected;
				LOGGER.info("Player " + player.name + " (" + player.auth + ") disconnected.");
			}
		}
		
		for (ServerPlayer player : newPlayers) {
			players.add(player);
		}
	}
	
	private void requestAuth(ServerPlayer player) {
		String replyAuth = server.sendRcon("auth-whois " + player.id);
		LOGGER.fine(replyAuth);
		if (replyAuth != null && !replyAuth.isEmpty()) {
			if (replyAuth.startsWith("Client in slot")) return;
			String[] splitted = replyAuth.split(" ");
			player.auth = splitted[8];
			player.auth = player.auth.isEmpty() ? "---" : player.auth;
		} else {
			requestAuth(player);
			LOGGER.severe("requesting auth again for " + player.name);
		}
	}

	private boolean getSwapRoles() {
		String swaproles = server.sendRcon("g_swaproles");
		LOGGER.fine(swaproles);
		String[] split = swaproles.split("\"");
		if (split.length > 4) {
			return split[3].equals("1^7");
		}
		return false;
	}

	private RconPlayersParsed parseRconPlayers() {

		RconPlayersParsed rpp = new RconPlayersParsed();
		
		String playersString = server.sendRcon("players");
//		LOGGER.fine("rcon players: >>>" + playersString + "<<<");
		String[] stripped = playersString.split("\n");
		
		boolean awaitsStats = false;		
		for (String line : stripped)
		{
			LOGGER.fine("parseRconPlayers: " + line);
			if (line.isEmpty()) continue;
			if (line.equals("print")) continue;
			if (line.equals("==== ShutdownGame ====")) break;
			
			if (line.startsWith("Map:"))
			{
				rpp.map = line.split(" ")[1];
			}
			else if (line.startsWith("Players"))
			{
				rpp.playercount = Integer.valueOf(line.split(" ")[1]);				
			}
			else if (line.startsWith("GameType"))
			{
				rpp.gametype = line.split(" ")[1];
			}
			else if (line.startsWith("Scores"))
			{
				rpp.scores[0] = Integer.valueOf(line.split(" ")[1].split(":")[1]);
				rpp.scores[1] = Integer.valueOf(line.split(" ")[2].split(":")[1]);
			}
			else if (line.startsWith("MatchMode"))
			{
				rpp.matchmode = line.split(" ")[1].equals("ON") ? true : false;
			}
			else if (line.startsWith("MatchReady"))
			{
				rpp.matchready[0] = line.split(" ")[1].split(":")[1].equals("YES") ? true : false;
				rpp.matchready[1] = line.split(" ")[2].split(":")[1].equals("YES") ? true : false;
			}
			else if (line.startsWith("WarmupPhase"))
			{
				rpp.warmupphase = line.split(" ")[1].equals("YES") ? true : false;
			}
			else if (line.startsWith("GameTime"))
			{
				rpp.gametime = line.split(" ")[1];
			}
			else if (line.startsWith("RoundTime"))
			{
				rpp.roundtime = line.split(" ")[1];
			}
			else if (line.startsWith("Half"))
			{
				rpp.half = line.split(" ")[1];
			}
			else
			{
				String[] splitted = line.split(" ");
//				LOGGER.fine("splitted = " + Arrays.toString(splitted));
				
				if (splitted[0].equals("[connecting]")) continue;
				
				if (splitted[0].equals("CTF:") && awaitsStats) {
					// ctfstats
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).caps = splitted[1].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).returns = splitted[2].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).fc_kills = splitted[3].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).stop_caps = splitted[4].split(":")[1];
					((CTF_Stats) rpp.players.get(rpp.players.size()-1).ctfstats).protect_flag = splitted[5].split(":")[1];
					awaitsStats = false;
				}
				else if (splitted[0].equals("BOMB:") && awaitsStats)
				{
					/*	BOMB: PLT:%i SBM:%i PKB:%i DEF:%i KBD:%i KBC:%i PKBC:%i
						>BOMB_PLANT
						>BOMB_BOOM
						>BOMBED
						>BOMB_DEFUSE
						>KILL_DEFUSE
						>KILL_BC
						>PROTECT_BC
					*/
					awaitsStats = false;
				}
				else if (rpp.players.size() < rpp.playercount) 
				{
					ServerPlayer sp = new ServerPlayer();
					sp.id = splitted[0].split(":")[0];
					sp.name = splitted[0].split(":")[1];
					sp.team = splitted[1].split(":")[1];
					sp.ctfstats.score = splitted[2].split(":")[1];
					sp.ctfstats.deaths = splitted[3].split(":")[1];
					sp.ctfstats.assists = splitted[4].split(":")[1];
					sp.ping = splitted[5].split(":")[1];
					sp.auth = splitted[6].split(":")[1];
					sp.ip = splitted[7].split(":")[1];
					
					if (sp.ping.equals("0")) {
						sp.state = ServerPlayerState.Connecting;
					} else {
						sp.state = ServerPlayerState.Connected;
					}
					
					rpp.players.add(sp);
					awaitsStats = true;
				}
				
			}
		}
		return rpp;
	}
	
	private void endGame() {
		calcStats();
		match.end();
		stop();
	}
	
	private void calcStats() {
		int redscore = score[0][0] + score[1][1]; //score_red_first + score_blue_second;
		int bluescore = score[0][1] + score[1][0]; //score_blue_first + score_red_second;
		int[] finalscore = { redscore, bluescore };
		LOGGER.info("Score: " + Arrays.toString(finalscore));
        for (Player player : match.getPlayerList()) {
        	if (player != null) {
        		int team = match.getTeam(player).equalsIgnoreCase("red") ? 0 : 1;
        		int opp = (team + 1) % 2;
        		
        		int eloSelf = player.getElo();
        		int eloOpp = match.getElo()[opp];
        		
        		// 1 win, 0.5 draw, 0 loss
        		double w = finalscore[team] > finalscore[opp] ? 1d : (finalscore[team] < finalscore[opp] ? 0d : 0.5d);
        		
        		double tSelf = Math.pow(10d, eloSelf/400d);
        		double tOpp = Math.pow(10d, eloOpp/400d);
        		double e = tSelf / (tSelf + tOpp);
        		
        		double result = 32d * (w - e);
        		int elochange = (int) Math.floor(result);
				int newelo = player.getElo() + elochange;
				LOGGER.info("ELO player: " + player.getUrtauth() + " old ELO: " + player.getElo() + " new ELO: " + newelo + " (" + (!String.valueOf(elochange).startsWith("-") ? "+" : "") + elochange + ")");
				player.addElo(elochange);
        	}
        }
        match.setScore(finalscore);
	}
	
	public void surrender(int teamid) {
		// save stats
		if (state == ServerState.LIVE || state == ServerState.SCORE) {
			saveStats(new int[] {0, 0}); // score don't matter as we override them. don't matter
		}
		
		int[] scorex = new int[2];
		scorex[teamid] = 0;
		scorex[(teamid + 1) % 2] = 15;
		score[0] = scorex;
		score[1] = new int[] {0, 0};

		calcStats();
		stop();
	}

	public String getGameTime() {
		return gameTime;
	}
}
