package de.gost0r.pickupbot.pickup;

public class Config {

	public static final String CMD_QUIT					= "!quit" ;

	public static final String CMD_ADD					= "!add";
	public static final String CMD_REMOVE				= "!remove";
	public static final String CMD_MAPS					= "!maps";
	public static final String CMD_MAP					= "!map";
	public static final String CMD_STATUS				= "!status";
	public static final String CMD_HELP					= "!help";
	
	public static final String CMD_LOCK					= "!lock";
	public static final String CMD_UNLOCK				= "!unlock";
	public static final String CMD_RESET				= "!reset";
	public static final String CMD_GETDATA				= "!getdata";
	public static final String CMD_ENABLEMAP			= "!enablemap";
	public static final String CMD_DISABLEMAP			= "!disablemap";
	public static final String CMD_RCON					= "!rcon";
	
	public static final String CMD_REGISTER				= "!register";
	public static final String CMD_GETELO				= "!elo";
	public static final String CMD_TOP5					= "!top5";
	
	//public static final String CMD_REPORT				= "!report";
	//public static final String CMD_EXCUSE				= "!excuse";
	//public static final String CMD_REPORTLIST			= "!reportlist";
	
	//public static final String CMD_ADDBAN				= "!addban";
	//public static final String CMD_REMOVEBAN			= "!removeban";

	public static final String CMD_SHOWSERVERS			= "!showservers";
	public static final String CMD_ADDSERVER			= "!addserver";
	public static final String CMD_ENABLESERVER			= "!enableserver";
	public static final String CMD_DISABLESERVER		= "!disableserver";
	public static final String CMD_UPDATESERVER			= "!updateserver";

	public static final String PUB_LIST = "" + CMD_ADD + " " + CMD_REMOVE + " " + CMD_MAPS + " "
	+ CMD_MAP + " " + CMD_STATUS + " " + CMD_HELP + " " + CMD_REGISTER + " " + CMD_GETELO + " " + CMD_TOP5;
	
	public static final String ADMIN_LIST = "" + CMD_LOCK + " " + CMD_UNLOCK + " " + CMD_RESET + " " + CMD_GETDATA + " "
	+ CMD_ENABLEMAP + " " + CMD_DISABLEMAP + " " + CMD_RCON + " " + CMD_SHOWSERVERS
	+ " " + CMD_ADDSERVER + " " + CMD_ENABLESERVER + " " + CMD_DISABLESERVER + " " + CMD_UPDATESERVER;
		
//------------------------------------------------------------------------------------//
	
	public static final String USE_CMD_QUIT				= "!quit" ;

	public static final String USE_CMD_ADD				= "!add <gametype>";
	public static final String USE_CMD_REMOVE			= "!remove";
	public static final String USE_CMD_MAPS				= "!maps";
	public static final String USE_CMD_MAP				= "!map <mapname>";
	public static final String USE_CMD_STATUS			= "!status";
	public static final String USE_CMD_HELP				= "!help <!command>";
	
	public static final String USE_CMD_LOCK				= "!lock";
	public static final String USE_CMD_UNLOCK			= "!unlock";
	public static final String USE_CMD_RESET			= "!reset <all/cur/id>";
	public static final String USE_CMD_GETDATA			= "!getdata <id>";
	public static final String USE_CMD_ENABLEMAP		= "!enablemap <ut4_map> <gametype>";
	public static final String USE_CMD_DISABLEMAP		= "!disablemap <ut4_map> <gametype>";
	public static final String USE_CMD_RCON				= "!rcon <rconstring>";
	
	public static final String USE_CMD_REGISTER			= "!register <urtauth>";
	public static final String USE_CMD_GETELO			= "!elo </urtauth/>";
	public static final String USE_CMD_TOP5				= "!top5";
	
	//public static final String USE_CMD_REPORT			= "!report <qauth> <reason>";
	//public static final String USE_CMD_EXCUSE			= "!excuse <excuse>";
	//public static final String USE_CMD_REPORTLIST		= "!reportlist";
	
	//public static final String USE_CMD_ADDBAN			= "!addban <urtauth>";
	//public static final String USE_CMD_REMOVEBAN		= "!removeban <urtauth>";

	public static final String USE_CMD_SHOWSERVERS		= "!showservers";
	public static final String USE_CMD_ADDSERVER		= "!addserver <ip:port> <rcon>";
	public static final String USE_CMD_REMOVESERVER		= "!removeserver <id>";
	
	public static final String USE_CMD_ENABLESERVER		= "!enableserver <id>";
	public static final String USE_CMD_DISABLESERVER	= "!disableserver <id>";
	public static final String USE_CMD_UPDATESERVER		= "!updateserver <id> <rcon>";

	public static final String pkup_help				= "CMDs are !add !remove !status !map !maps !lostpass !gameover !ring";
	public static final String pkup_lock				= "This game is currently locked";
	public static final String pkup_map					= "Map was successfully voted.";
	public static final String pkup_signup				= "You can sign up again!";
	public static final String pkup_pw					= "[ /connect .server. ; password .password. ]";
	public static final String pkup_status_noone		= "Nobody has signed up. Type !add to play.";
	public static final String pkup_status_signup		= "Sign up: [.playernumber./10]";
	public static final String pkup_status_server		= "Awaiting available server.";
	public static final String pkup_status_signup_priv	= "Players [.playernumber./10]: .playerlist.";
	public static final String pkup_started				= "Game has already started. .status. - .time. minutes in.";

	public static final String pkup_reset_all 			= "*All matches have been reset.*";
	public static final String pkup_reset_cur 			= "*The current match have been reset.*";
	public static final String pkup_reset_id 			= "*The match .id. has been reset.*";
	
	public static final String pkup_go_admin			= "[ Pickup Game #.gamenumber. ][ Password: .password. ][ Map: .map. ][ ELO red: .elored. ELO blue: .eloblue. ]";
	public static final String pkup_go_player			= "Pickup-pro starts now! Connect to the server and join Team .team. in order to play. Make up positions and ready up! [ /connect .server. ; password .password. ]";
	public static final String pkup_go_pub_head			= "Pickup-pro .gametype. #.gamenumber. (avg ELO: .elo.) is about to start!";
	public static final String pkup_go_pub_red			= "Red team: .playerlist.";
	public static final String pkup_go_pub_blue			= "Blue team: .playerlist.";
	public static final String pkup_go_pub_map			= "Map: .map.";
	public static final String pkup_go_pub_calm			= "You will receive the connection info via DM.";
	
	public static final String pkup_sign				= "You can sign up again.";
	
	public static final String pkup_aftermath			= ".team. team .result. (.score.) - .player1. (.elochange1.), .player2. (.elochange2.), .player3. (.elochange3.), .player4. (.elochange4.), .player5. (.elochange5.),";
	
	public static final String pkup_getelo				= "ELO .urtauth.: .elo. (.elochange.)";
	public static final String pkup_top5				= "#.rank. - .urtauth.: ELO: .elo. (.elochange.)";
			 
	public static final String is_banned				= "You are banned and therefore you cannot participate.";
	public static final String map_not_found			= "Map not found.";
	public static final String map_not_unique			= "Mapstring not unique.";
	public static final String map_already_voted		= "You have already voted.";

	public static final String player_not_found			= "Player not found.";
	public static final String user_not_registered		= "You're not registered. Please use " + USE_CMD_REGISTER;

	public static final String auth_taken				= "Your auth has been already registered with another account.";
	public static final String auth_invalid				= "Your urtauth seems to be invalid.";
	public static final String auth_success				= "Your auth has been successfully registered with your account.";

//	public static final String report_wrong_arg			= "Your report reason is invalid, check !reportlist to check the list.";
//	public static final String report_invalid_urtauth	= "No player could be found to the specific urtauth.";
//	public static final String report_not_played		= "The reported player hasn't played the past match.";
//	public static final String report_didnt_play		= "You haven't played the last match, you aren't able to report someone.";
//	public static final String report_already_reported	= "This player has already been reported by you.";
//	public static final String report_successful		= "Your report was successfully stored.";
//	public static final String report_raise_issue		= ".urtauth. has been auto-banned. [strength: .strength.] Type !review .urtauth. to review the ban.";

	public static final String wrong_argument_amount	= "Wrong amount of arguments: ";
	public static final String help_prefix				= "How to use the command: ";
	public static final String help_cmd_avi				= "These commands are available (use !help <!command> for more info): ";

	public static final String lock_enable				= "Game is now locked";
	public static final String lock_disable				= "Game is now unlocked";

	public static final String pkup_match_unavi 		= "Match is not available right now.";
	public static final String pkup_match_invalid_gt	= "No match for that gametype is not available right now.";
	
	public static final String admin_cmd_successful		= "Successful: ";
	public static final String admin_cmd_unsuccessful	= "Unuccessful: ";
}
