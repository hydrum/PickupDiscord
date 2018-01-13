package de.gost0r.pickupbot.pickup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.gost0r.pickupbot.pickup.server.Server;

public class Database {
	
	private Connection c = null;
	
	public Database() {
		initConnection();
	}
	
	private void initConnection() {
		try {
			c = DriverManager.getConnection("jdbc:sqlite:pickup.db");
			initTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initTable() {
		String sql;
		try {
			Statement stmt = c.createStatement();
			sql = "CREATE TABLE IF NOT EXISTS player ( userid TEXT NOT NULL,"
													+ "urtauth TEXT PRIMARY KEY,"
													+ "elo INTEGER DEFAULT 1000,"
													+ "elochange INTEGER DEFAULT 0 )";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS map ( map TEXT PRIMARY KEY,"
												+ " active TEXT )";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS banlist ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "player REFERENCES player,"
													+ "reason TEXT,"
													+ "start INTEGER,"
													+ "end INTEGER)";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS report ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "player REFERENCES player,"
													+ "reporter REFERENCES player,"
													+ "reason TEXT,"
													+ "match REFERENCES match )";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS match ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "server REFERENCES server,"
													+ "gametype TEXT,"
													+ "state INTEGER,"
													+ "starttime INTEGER,"
													+ "map REFERENCES map,"
													+ "elo_red INTEGER,"
													+ "elo_blue INTEGER,"
													+ "score_red INTEGER DEFAULT 0,"
													+ "score_blue INTEGER DEFAULT 0 )";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS player_in_match ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "matchid REFERENCES match,"
													+ "player REFRENCES player,"
													+ "team TEXT)";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS score ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "kills INTEGER DEFAULT 0,"
													+ "deaths INTEGER DEFAULT 0,"
													+ "assists INTEGER DEFAULT 0,"
													+ "caps INTEGER DEFAULT 0,"
													+ "returns INTEGER DEFAULT 0,"
													+ "fckills INTEGER DEFAULT 0,"
													+ "stopcaps INTEGER DEFAULT 0,"
													+ "protflag INTEGER DEFAULT 0)";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS stats ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "pim REFERENCES player_in_match,"
													+ "ip TEXT,"
													+ "score_1 REFERENCES score,"
													+ "score_2 REFERENCES score,"
													+ "status INTEGER)";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS server ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "ip TEXT,"
													+ "port INTEGER,"
													+ "rcon TEXT,"
													+ "password TEXT,"
													+ "active TEXT)";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void createPlayer(Player player) {
		try {
			String sql = "INSERT INTO player (userid, urtauth, elo, elochange) VALUES (?, ?, ?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, player.getDiscordUser().id);
			pstmt.setString(2, player.getUrtauth());
			pstmt.setInt(3,  player.getElo());
			pstmt.setInt(4,  player.getEloChange());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createServer(Server server) {
		try {
			String sql = "INSERT INTO server (ip, port, rcon, password, active) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, server.IP);
			pstmt.setInt(2, server.port);
			pstmt.setString(3, server.rconpassword);
			pstmt.setString(4, server.password);
			pstmt.setString(5, String.valueOf(server.active));
			pstmt.executeUpdate();
			pstmt.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createMap(GameMap map) {
		try {
			String sql = "INSERT INTO map (map, active) VALUES (?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, map.name);
			pstmt.setString(2, String.valueOf(map.active));
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int createMatch(Match match) {
		try {
			String sql = "INSERT INTO match (state, gametype, server, starttime, map, red_elo, blue_elo) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setInt(1, match.getMatchState().ordinal());
			pstmt.setString(2, match.getGametype().getName());
			pstmt.setInt(3, match.getServer().id);
			pstmt.setLong(4, match.getStartTime());
			pstmt.setString(5, match.getMap().name);
			pstmt.setInt(6, match.getEloRed());
			pstmt.setInt(7, match.getEloBlue());
			pstmt.executeUpdate();
			
			Statement stmt = c.createStatement();
			sql = "SELECT ID FROM match ORDER BY ID DESC";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			int mid = rs.getInt("id");
			for (Player player : match.getPlayerList()) {
				sql = "INSERT INTO score (kills, deaths) VALUES (0, 0)";
				stmt.executeUpdate(sql);
				sql = "SELECT ID FROM score ORDER BY ID DESC";
				rs = stmt.executeQuery(sql);
				rs.next();
				int scoreid1 = rs.getInt("ID");
				sql = "INSERT INTO score (kills, deaths) VALUES (0, 0)";
				stmt.executeUpdate(sql);
				sql = "SELECT ID FROM score ORDER BY ID DESC";
				rs = stmt.executeQuery(sql);
				rs.next();
				int scoreid2 = rs.getInt("ID");
				sql = "INSERT INTO player_in_match (matchid, player, team) VALUES (?, ?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, mid);
				pstmt.setString(2, player.getUrtauth());
				pstmt.setString(3, match.getTeam(player));
				pstmt.executeUpdate();
				sql = "SELECT ID FROM player_in_match ORDER BY ID DESC";
				rs = stmt.executeQuery(sql);
				rs.next();
				int pidmid = rs.getInt("ID");
				sql = "INSERT INTO stats (pim, ip, score_1, score_2, status) VALUES (?, null, ?, ?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, pidmid);
				pstmt.setInt(2, scoreid1);
				pstmt.setInt(3, scoreid2);
				pstmt.setString(4, "PLAYING");
				pstmt.executeUpdate();
			}
			pstmt.close();
			stmt.close();
			return mid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

}
