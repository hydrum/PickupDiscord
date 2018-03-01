package de.gost0r.pickupbot.pickup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordRole;
import de.gost0r.pickupbot.discord.DiscordUser;
import de.gost0r.pickupbot.pickup.server.Server;

public class Database {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	private Connection c = null;
	private PickupLogic logic;
	
	public Database(PickupLogic logic) {
		this.logic = logic;
		initConnection();
	}
	
	private void initConnection() {
		try {
			c = DriverManager.getConnection("jdbc:sqlite:pickup.db");
			initTable();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	
	public void disconnect() {
		try {
			c.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}

	private void initTable() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS player ( userid TEXT,"
													+ "urtauth TEXT,"
													+ "elo INTEGER DEFAULT 1000,"
													+ "elochange INTEGER DEFAULT 0,"
													+ "active TEXT,"
													+ "PRIMARY KEY (userid, urtauth) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS gametype ( gametype TEXT PRIMARY KEY,"
													+ "config TEXT,"
													+ "teamsize INTEGER, "
													+ "active TEXT )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS gameconfig ( gametype TEXT,"
													+ "config TEXT,"
													+ "PRIMARY KEY (gametype, config) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS map ( map TEXT,"
													+ "gametype TEXT,"
													+ "active TEXT,"
													+ "FOREIGN KEY (gametype) REFERENCES gametype(gametype),"
													+ "PRIMARY KEY (map, gametype) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS banlist ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "player_userid TEXT,"
													+ "player_urtauth TEXT,"
													+ "reason TEXT,"
													+ "start INTEGER,"
													+ "end INTEGER,"
													+ "FOREIGN KEY (player_userid, player_urtauth) REFERENCES player(userid, urtauth) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS report ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "player_userid TEXT,"
													+ "player_urtauth TEXT,"
													+ "reporter_userid TEXT,"
													+ "reporter_urtauth TEXT,"
													+ "reason TEXT,"
													+ "match INTEGER,"
													+ "FOREIGN KEY (player_userid, player_urtauth) REFERENCES player(userid, urtauth),"
													+ "FOREIGN KEY (reporter_userid, reporter_urtauth) REFERENCES player(userid, urtauth),"
													+ "FOREIGN KEY (match) REFERENCES match(ID) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS match ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "server INTEGER,"
													+ "gametype TEXT,"
													+ "state TEXT,"
													+ "starttime INTEGER,"
													+ "map TEXT,"
													+ "elo_red INTEGER,"
													+ "elo_blue INTEGER,"
													+ "score_red INTEGER DEFAULT 0,"
													+ "score_blue INTEGER DEFAULT 0,"
													+ "FOREIGN KEY (server) REFERENCES server(id),"
													+ "FOREIGN KEY (map, gametype) REFERENCES map(map, gametype),"
													+ "FOREIGN KEY (gametype) REFERENCES gametype(gametype) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS player_in_match ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "matchid INTEGER,"
													+ "player_userid TEXT,"
													+ "player_urtauth TEXT,"
													+ "team TEXT,"
													+ "FOREIGN KEY (matchid) REFERENCES match(ID), "
													+ "FOREIGN KEY (player_userid, player_urtauth) REFERENCES player(userid, urtauth) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS score ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "kills INTEGER DEFAULT 0,"
													+ "deaths INTEGER DEFAULT 0,"
													+ "assists INTEGER DEFAULT 0,"
													+ "caps INTEGER DEFAULT 0,"
													+ "returns INTEGER DEFAULT 0,"
													+ "fckills INTEGER DEFAULT 0,"
													+ "stopcaps INTEGER DEFAULT 0,"
													+ "protflag INTEGER DEFAULT 0 )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS stats ( pim INTEGER PRIMARY KEY,"
													+ "ip TEXT,"
													+ "status TEXT,"
													+ "score_1 INTEGER,"
													+ "score_2 INTEGER,"
													+ "FOREIGN KEY(pim) REFERENCES player_in_match(ID),"
													+ "FOREIGN KEY (score_1) REFERENCES score(ID),"
													+ "FOREIGN KEY (score_2) REFERENCES score(ID) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS server ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
													+ "ip TEXT,"
													+ "port INTEGER,"
													+ "rcon TEXT,"
													+ "password TEXT,"
													+ "active TEXT)";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS admin_role (role TEXT PRIMARY KEY)";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS roles (role TEXT,"
													+ "type TEXT,"
													+ "PRIMARY KEY (role) )";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS channels (channel TEXT,"
													+ "type TEXT,"
													+ "PRIMARY KEY (channel) )";
			stmt.executeUpdate(sql);
			
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	

	@SuppressWarnings("resource")
	public void createPlayer(Player player) {
		try {			
			// check whether user exists
			String sql = "SELECT * FROM player WHERE userid=? AND urtauth=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, player.getDiscordUser().id);
			pstmt.setString(2, player.getUrtauth());
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {				
				sql = "INSERT INTO player (userid, urtauth, elo, elochange, active) VALUES (?, ?, ?, ?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, player.getDiscordUser().id);
				pstmt.setString(2, player.getUrtauth());
				pstmt.setInt(3,  player.getElo());
				pstmt.setInt(4,  player.getEloChange());
				pstmt.setString(5, String.valueOf(true));
				pstmt.executeUpdate();
			} else {
				sql = "UPDATE player SET active=? WHERE userid=? AND urtauth=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, String.valueOf(true));
				pstmt.setString(2, player.getDiscordUser().id);
				pstmt.setString(3, player.getUrtauth());
				pstmt.executeUpdate();
			}
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
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
			Statement stmt = c.createStatement();
			sql = "SELECT ID FROM server ORDER BY ID DESC";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			server.id = rs.getInt("ID");
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}

	public void createMap(GameMap map, Gametype gametype) {
		try {
			String sql = "INSERT INTO map (map, gametype, active) VALUES (?, ?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, map.name);
			pstmt.setString(2, gametype.getName());
			pstmt.setString(3, String.valueOf(map.isActiveForGametype(gametype)));
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	
	public int createMatch(Match match) {
		try {
			String sql = "INSERT INTO match (state, gametype, server, starttime, map, elo_red, elo_blue) VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, match.getMatchState().name());
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
				int[] score = new int[2];
				for (int i = 0; i < score.length; ++i) {
					sql = "INSERT INTO score (kills, deaths) VALUES (0, 0)";
					stmt.executeUpdate(sql);
					sql = "SELECT ID FROM score ORDER BY ID DESC";
					rs = stmt.executeQuery(sql);
					rs.next();
					score[i] = rs.getInt("ID");
				}
				sql = "INSERT INTO player_in_match (matchid, player_userid, player_urtauth, team) VALUES (?, ?, ?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, mid);
				pstmt.setString(2, player.getDiscordUser().id);
				pstmt.setString(3, player.getUrtauth());
				pstmt.setString(4, match.getTeam(player));
				pstmt.executeUpdate();
				sql = "SELECT ID FROM player_in_match ORDER BY ID DESC";
				rs = stmt.executeQuery(sql);
				rs.next();
				int pidmid = rs.getInt("ID");
				sql = "INSERT INTO stats (pim, ip, score_1, score_2, status) VALUES (?, null, ?, ?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, pidmid);
				pstmt.setInt(2, score[0]);
				pstmt.setInt(3, score[1]);
				pstmt.setString(4, match.getStats(player).getStatus().name());
				pstmt.executeUpdate();
			}
			pstmt.close();
			stmt.close();
			return mid;
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return -1;
	}
	
	
	// LOADING
	
	public Map<PickupRoleType, List<DiscordRole>> loadRoles() {
		Map<PickupRoleType, List<DiscordRole>> map = new HashMap<PickupRoleType, List<DiscordRole>>();
		try {
			String sql = "SELECT role, type FROM roles";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				PickupRoleType type = PickupRoleType.valueOf(rs.getString("type"));
				if (type == PickupRoleType.NONE) continue;
				if (!map.containsKey(type)) {
					map.put(type, new ArrayList<DiscordRole>());
				}
				DiscordRole role = DiscordRole.getRole(rs.getString("role"));
				LOGGER.config("loadRoles(): " + role.id + " type=" + type.name());
				map.get(type).add(role);
			}
			
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return map;
	}

	public Map<PickupChannelType, List<DiscordChannel>> loadChannels() {
		Map<PickupChannelType, List<DiscordChannel>> map = new HashMap<PickupChannelType, List<DiscordChannel>>();
		try {
			String sql = "SELECT channel, type FROM channels";
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				PickupChannelType type = PickupChannelType.valueOf(rs.getString("type"));
				if (type == PickupChannelType.NONE) continue;
				if (!map.containsKey(type)) {
					map.put(type, new ArrayList<DiscordChannel>());
				}
				DiscordChannel channel = DiscordChannel.findChannel(rs.getString("channel"));
				map.get(type).add(channel);
				LOGGER.config("loadChannels(): " + channel.id + " name=" + channel.name + " type=" + type.name());
			}
			
			stmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return map;
	}
	
	
	public List<Server> loadServers() {		
		List<Server> serverList = new ArrayList<Server>();		
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT id, ip, port, rcon, password, active FROM server";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("id");
				String ip = rs.getString("ip");
				int port = rs.getInt("port");
				String rcon = rs.getString("rcon");
				String password = rs.getString("password");
				boolean active = Boolean.valueOf(rs.getString("active"));
				
				Server server = new Server(id, ip, port, rcon, password, active);
				serverList.add(server);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return serverList;
	}
	


	public List<Gametype> loadGametypes() {
		List<Gametype> gametypeList = new ArrayList<Gametype>();
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT gametype, teamsize, active FROM gametype";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Gametype gametype = new Gametype(rs.getString("gametype"), rs.getInt("teamsize"), Boolean.valueOf(rs.getString("active")));
				LOGGER.config("loadGametypes(): " + gametype.getName() + " active=" + gametype.getActive());
				sql = "SELECT config FROM gameconfig WHERE gametype=?";
				PreparedStatement pstmt = c.prepareStatement(sql);
				pstmt.setString(1, gametype.getName());
				ResultSet rs1 = pstmt.executeQuery();
				while (rs1.next()) {
					gametype.addConfig(rs1.getString("config"));
				}
				gametypeList.add(gametype);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return gametypeList;
	}

	public List<GameMap> loadMaps() {
		List<GameMap> maplist = new ArrayList<GameMap>();
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT map, gametype, active FROM map";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				GameMap map = null;
				for (GameMap xmap : maplist) {
					if (xmap.name.equals(rs.getString("map"))) {
						map = xmap;
						break;
					}
				}
				if (map == null) {
					map = new GameMap(rs.getString("map"));
					maplist.add(map);
				}
				map.setGametype(logic.getGametypeByString(rs.getString("gametype")), Boolean.valueOf(rs.getString("active")));
				LOGGER.config(map.name + " " + rs.getString("gametype") + "="+ map.isActiveForGametype(logic.getGametypeByString(rs.getString("gametype"))));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return maplist;
	}
	
	public List<Match> loadOngoingMatches() {
		List<Match> matchList = new ArrayList<Match>();
		try {
			ResultSet rs;
			String sql = "SELECT ID FROM match WHERE state=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, MatchState.Live.name());
			rs = pstmt.executeQuery();
			while(rs.next()) {
				Match m = loadMatch(rs.getInt("ID"));
				if (m != null) {
					matchList.add(m);
				}
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return matchList;
	}
	
	public Match loadMatch(int id) {
		Match match = null;
		try {
			ResultSet rs, rs1, rs2, rs3;
			String sql = "SELECT starttime, map, gametype, score_red, score_blue, elo_red, elo_blue, state, server FROM match WHERE ID=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			rs.next();
			
			Map<Player, MatchStats> stats = new HashMap<Player, MatchStats>();
			Map<String, List<Player>> teamList = new HashMap<String, List<Player>>();
			teamList.put("red", new ArrayList<Player>());
			teamList.put("blue", new ArrayList<Player>());
				
			// getting players in match
			sql = "SELECT ID, player_userid, player_urtauth, team FROM player_in_match WHERE matchid=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs1 = pstmt.executeQuery();
			while (rs1.next()) {
				int pidmid = rs1.getInt("ID");
//				String userid = rs1.getString("player_userid"); // not needed as we potentially load the player via loadPlayer(urtauth)
				String urtauth = rs1.getString("player_urtauth");
				String team = rs1.getString("team");
				
				// getting stats
				sql = "SELECT ip, score_1, score_2, status FROM stats WHERE pim=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, pidmid);
				rs2 = pstmt.executeQuery();
				rs2.next();
				
				int[] scoreid = new int[] { rs2.getInt("score_1"), rs2.getInt("score_2") };
				String ip = rs2.getString("ip");
				MatchStats.Status status = MatchStats.Status.valueOf(rs2.getString("status"));
				
				Score[] scores = new Score[2];
				
				// getting score
				for(int i = 0; i < 2; ++i) {
					sql = "SELECT kills, deaths, assists, caps, returns, fckills, stopcaps, protflag FROM score WHERE ID=? ORDER BY ID DESC";
					pstmt = c.prepareStatement(sql);
					pstmt.setInt(1, scoreid[i]);
					rs3 = pstmt.executeQuery();
					rs3.next();
					
					scores[i] = new Score();
					scores[i].score = rs3.getInt("kills");
					scores[i].deaths = rs3.getInt("deaths");
					scores[i].assists = rs3.getInt("assists");
					scores[i].caps = rs3.getInt("caps");
					scores[i].returns = rs3.getInt("returns");
					scores[i].fc_kills = rs3.getInt("fckills");
					scores[i].stop_caps = rs3.getInt("stopcaps");
					scores[i].protect_flag = rs3.getInt("protflag");
				}
					
				// assemble stats
				Player player = Player.get(urtauth);
				stats.put(player, new MatchStats(scores[0], scores[1], ip, status));				
				teamList.get(team).add(player);
			}
				
			Gametype gametype = logic.getGametypeByString(rs.getString("gametype"));				
			Server server = logic.getServerByID(rs.getInt("server"));
			GameMap map = logic.getMapByName(rs.getString("map"));
			MatchState state = MatchState.valueOf(rs.getString("state"));
			
			match = new Match(id, rs.getLong("starttime"),
									map,
									new int[] { rs.getInt("score_red"), rs.getInt("score_blue") },
									new int[] { rs.getInt("elo_red"), rs.getInt("elo_blue") },
									teamList,
									state,
									gametype,
									server,
									stats); 
			match.setLogic(logic);
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return match;
	}
	
	public Player loadPlayer(DiscordUser user) {
		Player player = null;
		try {
			String sql = "SELECT urtauth, elo, elochange FROM player WHERE userid=? AND active=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, user.id);
			pstmt.setString(2, String.valueOf(true));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				player = new Player(user, rs.getString("urtauth"));
				player.setElo(rs.getInt("elo"));
				player.setEloChange(rs.getInt("elochange"));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return player;
	}

	public Player loadPlayer(String urtauth) {
		Player player = null;
		try {
			String sql = "SELECT userid, elo, elochange FROM player WHERE urtauth=? AND active=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, urtauth);
			pstmt.setString(2, String.valueOf(true));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				DiscordUser user = DiscordUser.getUser(rs.getString("userid"));
				player = new Player(user, urtauth);
				player.setElo(rs.getInt("elo"));
				player.setEloChange(rs.getInt("elochange"));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return player;
	}
	
	
	// UPDATE SERVER
	public void updateServer(Server server) {
		try {
			String sql = "UPDATE server SET ip=?, port=?, rcon=?, password=?, active=? WHERE id=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, server.IP);
			pstmt.setInt(2, server.port);
			pstmt.setString(3, server.rconpassword);
			pstmt.setString(4, server.password);
			pstmt.setString(5, String.valueOf(server.active));
			pstmt.setInt(6, server.id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	


	public void updateMap(GameMap map, Gametype gametype) {
		try {
			String sql = "SELECT * FROM map WHERE map=? AND gametype=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, map.name);
			pstmt.setString(2, gametype.getName());
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				createMap(map, gametype);
				return;
			}			
			sql = "UPDATE map SET active=? WHERE map=? AND gametype=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setString(1, String.valueOf(map.isActiveForGametype(gametype)));
			pstmt.setString(2, map.name);
			pstmt.setString(3, gametype.getName());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}		
	}

	public void updateChannel(DiscordChannel channel, PickupChannelType type) {
		try {
			String sql = "SELECT * FROM channels WHERE channel=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, channel.id);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				sql = "INSERT INTO channels (channel) VALUES (?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, channel.id);
				pstmt.executeUpdate();
			}			
			sql = "UPDATE channels SET type=? WHERE channel=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setString(1, type.name());
			pstmt.setString(2, channel.id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}		
	}

	public void updateRole(DiscordRole role, PickupRoleType type) {
		try {
			String sql = "SELECT * FROM roles WHERE role=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, role.id);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				sql = "INSERT INTO roles (role) VALUES (?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, role.id);
				pstmt.executeUpdate();
			}			
			sql = "UPDATE roles SET type=? WHERE role=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setString(1, type.name());
			pstmt.setString(2, role.id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}		
	}
	

	// SAVE MATCH
	
	public void saveMatch(Match match) {
		try {
			ResultSet rs;
			String sql = "UPDATE match SET state=?, score_red=?, score_blue=? WHERE id=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, match.getMatchState().name());
			pstmt.setInt(2, match.getScoreRed());
			pstmt.setInt(3, match.getScoreBlue());
			pstmt.setInt(4, match.getID());
			pstmt.executeUpdate();
			
			for (Player player : match.getPlayerList()) {
				// get ids
				sql = "SELECT ID FROM player_in_match WHERE matchid=? AND player_userid=? AND player_urtauth=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, match.getID());
				pstmt.setString(2, player.getDiscordUser().id);
				pstmt.setString(3, player.getUrtauth());
				rs = pstmt.executeQuery();
				rs.next();
				int pim = rs.getInt("ID");
				
				sql = "SELECT score_1, score_2 FROM stats WHERE pim=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, pim);
				rs = pstmt.executeQuery();
				rs.next();
				int[] scoreid = new int[] { rs.getInt("score_1"), rs.getInt("score_2") };
				
				// update ip & status (leaver etc)
				sql = "UPDATE stats SET ip=?, status=? WHERE pim=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, match.getStats(player).getIP());
				pstmt.setString(2, match.getStats(player).getStatus().name());
				pstmt.setInt(3, pim);
				pstmt.executeUpdate();
				
				// update playerscore
				sql = "UPDATE score SET kills=?, deaths=?, assists=?, caps=?, returns=?, fckills=?, stopcaps=?, protflag=? WHERE ID=?";
				pstmt = c.prepareStatement(sql);
				for (int i=0; i < 2; ++i) {
					pstmt.setInt(1, match.getStats(player).score[i].score);
					pstmt.setInt(2, match.getStats(player).score[i].deaths);
					pstmt.setInt(3, match.getStats(player).score[i].assists);
					pstmt.setInt(4, match.getStats(player).score[i].caps);
					pstmt.setInt(5, match.getStats(player).score[i].returns);
					pstmt.setInt(6, match.getStats(player).score[i].fc_kills);
					pstmt.setInt(7, match.getStats(player).score[i].stop_caps);
					pstmt.setInt(8, match.getStats(player).score[i].protect_flag);
					pstmt.setInt(9, scoreid[i]);
					pstmt.executeUpdate();
				}
				
				// update elo change
				sql = "UPDATE player SET elo=?, elochange=? WHERE userid=? AND urtauth=?";
				pstmt = c.prepareStatement(sql);
				pstmt.setInt(1, player.getElo());
				pstmt.setInt(2, player.getEloChange());
				pstmt.setString(3, player.getDiscordUser().id);
				pstmt.setString(4, player.getUrtauth());
				pstmt.executeUpdate();			
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}

	
	// need to check whether this is newly created or not
	public void updateGametype(Gametype gt) {
		try {
			String sql = "SELECT gametype FROM gametype WHERE gametype=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, gt.getName());
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				sql = "INSERT INTO gametype (gametype) VALUES (?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, gt.getName());
				pstmt.executeUpdate();
			}
			sql = "UPDATE gametype SET teamsize=?, active=? WHERE gametype=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setInt(1, gt.getTeamSize());
			pstmt.setString(2, String.valueOf(gt.getActive()));
			pstmt.setString(3, gt.getName());
			pstmt.executeUpdate();
			sql = "DELETE FROM gameconfig WHERE gametype=?";
			pstmt = c.prepareStatement(sql);
			pstmt.setString(1, gt.getName());
			pstmt.executeUpdate();
			for (String config : gt.getConfig()) {
				sql = "INSERT INTO gameconfig (gametype, config) VALUES (?, ?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, gt.getName());
				pstmt.setString(2, config);
				pstmt.executeUpdate();
			}
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}	
	}
	



	public void removePlayer(Player player) {
		try {
			String sql = "UPDATE player SET active=? WHERE userid=? AND urtauth=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, String.valueOf(false));
			pstmt.setString(2, player.getDiscordUser().id);
			pstmt.setString(3, player.getUrtauth());
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
	}
	
	
	public List<Player> getTopPlayers(int number) {
		List<Player> list = new ArrayList<Player>();
		try {
			String sql = "SELECT urtauth FROM player WHERE active=? ORDER BY elo DESC LIMIT ?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, String.valueOf(true));
			pstmt.setInt(2, number);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Player p = Player.get(rs.getString("urtauth"));
				list.add(p);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return list;
	}
	
	public int getRankForPlayer(Player player) {
		int rank = -1;
		try {
			String sql = "SELECT (SELECT COUNT(*) FROM player b WHERE a.elo < b.elo AND active=?) AS rank FROM player a WHERE userid=? AND urtauth=?";
			PreparedStatement pstmt = c.prepareStatement(sql);
			pstmt.setString(1, String.valueOf(true));
			pstmt.setString(2, player.getDiscordUser().id);
			pstmt.setString(3, player.getUrtauth());
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				rank = rs.getInt("rank") + 1;
			}
		} catch (SQLException e) {
			LOGGER.log(Level.WARNING, "Exception: ", e);
		}
		return rank;
	}

}
