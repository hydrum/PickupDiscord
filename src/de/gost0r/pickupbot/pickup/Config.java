package de.gost0r.pickupbot.pickup;

public class Config {
	public static final String CMD_ADD					= "!add";
	public static final String CMD_REMOVE				= "!remove";
	public static final String CMD_MAPS					= "!maps";
	public static final String CMD_MAP					= "!map";
	public static final String CMD_STATUS				= "!status";
	public static final String CMD_HELP					= "!help";
	public static final String CMD_SURRENDER			= "!surrender";
	
	public static final String CMD_LOCK					= "!lock";
	public static final String CMD_UNLOCK				= "!unlock";
	public static final String CMD_RESET				= "!reset";
	public static final String CMD_GETDATA				= "!getdata";
	public static final String CMD_ENABLEMAP			= "!enablemap";
	public static final String CMD_DISABLEMAP			= "!disablemap";
	public static final String CMD_RCON					= "!rcon";

	public static final String CMD_ENABLEGAMETYPE		= "!enablegametype";
	public static final String CMD_DISABLEGAMETYPE		= "!disablegametype";

	public static final String CMD_ADDGAMECONFIG		= "!addgameconfig";
	public static final String CMD_REMOVEGAMECONFIG		= "!delgameconfig";
	public static final String CMD_LISTGAMECONFIG		= "!showgameconfig";
	
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

	public static final String CMD_SHOWMATCHES			= "!showmatches";
	
	public static final String CMD_UNREGISTER			= "!unregister";

	public static final String PUB_LIST = "" + CMD_ADD + " " + CMD_REMOVE + " " + CMD_MAPS + " "
	+ CMD_MAP + " " + CMD_STATUS + " " + CMD_HELP + " " + CMD_REGISTER + " " + CMD_GETELO + " " + CMD_TOP5 + " " + CMD_SURRENDER;
	
	public static final String ADMIN_LIST = "" + CMD_LOCK + " " + CMD_UNLOCK + " " + CMD_RESET + " " + CMD_GETDATA + " "
	+ CMD_ENABLEMAP + " " + CMD_DISABLEMAP + " " + CMD_RCON + " " + CMD_SHOWSERVERS + " " + CMD_ENABLEGAMETYPE + " " + CMD_DISABLEGAMETYPE
	+ " " + CMD_ADDSERVER + " " + CMD_ENABLESERVER + " " + CMD_DISABLESERVER + " " + CMD_UPDATESERVER + " " + CMD_SHOWMATCHES
	+ " " + CMD_UNREGISTER + " " + CMD_ADDGAMECONFIG + " " + CMD_REMOVEGAMECONFIG + " " + CMD_LISTGAMECONFIG;
		
//------------------------------------------------------------------------------------//
	

	public static final String USE_CMD_ADD				= "!add <gametype>";
	public static final String USE_CMD_REMOVE			= "!remove";
	public static final String USE_CMD_MAPS				= "!maps";
	public static final String USE_CMD_MAP				= "!map <mapname>";
	public static final String USE_CMD_STATUS			= "!status";
	public static final String USE_CMD_HELP				= "!help <command>";
	public static final String USE_CMD_SURRENDER		= "!surrender";
	
	public static final String USE_CMD_LOCK				= "!lock";
	public static final String USE_CMD_UNLOCK			= "!unlock";
	public static final String USE_CMD_RESET			= "!reset <all/cur/id>";
	public static final String USE_CMD_GETDATA			= "!getdata <id>";
	public static final String USE_CMD_ENABLEMAP		= "!enablemap <ut4_map> <gametype>";
	public static final String USE_CMD_DISABLEMAP		= "!disablemap <ut4_map> <gametype>";
	public static final String USE_CMD_RCON				= "!rcon <serverid> <rconstring>";

	public static final String USE_CMD_ENABLEGAMETYPE	= "!enablegametype <name> <teamsize>";
	public static final String USE_CMD_DISABLEGAMETYPE	= "!disablegametype <name>";
	
	public static final String USE_CMD_ADDGAMECONFIG	= "!addgameconfig <gametype> <string>";
	public static final String USE_CMD_REMOVEGAMECONFIG	= "!delgameconfig <gametype> <string>";
	public static final String USE_CMD_LISTGAMECONFIG	= "!showgameconfig <gametype>";
	
	public static final String USE_CMD_REGISTER			= "!register <urtauth>";
	public static final String USE_CMD_GETELO			= "!elo </@DiscordUser|urtauth/>";
	public static final String USE_CMD_TOP5				= "!top5";
	
	//public static final String USE_CMD_REPORT			= "!report <qauth> <reason>";
	//public static final String USE_CMD_EXCUSE			= "!excuse <excuse>";
	//public static final String USE_CMD_REPORTLIST		= "!reportlist";
	
	//public static final String USE_CMD_ADDBAN			= "!addban <urtauth>";
	//public static final String USE_CMD_REMOVEBAN		= "!removeban <urtauth>";

	public static final String USE_CMD_SHOWSERVERS		= "!showservers";
	public static final String USE_CMD_ADDSERVER		= "!addserver <ip:port> <rcon>";
	
	public static final String USE_CMD_ENABLESERVER		= "!enableserver <id>";
	public static final String USE_CMD_DISABLESERVER	= "!disableserver <id>";
	public static final String USE_CMD_UPDATESERVER		= "!updateserver <id> <rcon>";
		
	public static final String USE_CMD_SHOWMATCHES		= "!showmatches";
	
	public static final String USE_CMD_UNREGISTER		= "!unregister <urtauth>";
	

	//------------------------------------------------------------------------------------//

	public static final String pkup_lock				= "This game is currently locked";
	public static final String pkup_map					= "Your vote for .map. was successfully casted.";
	public static final String pkup_map_list			= "**.gametype.**: .maplist.";
//	public static final String pkup_signup				= "You can sign up again!";
	public static final String pkup_pw					= "[ /connect .server. ; password .password. ]";
	public static final String pkup_status_noone		= "**.gametype.**: Nobody has signed up. Type `" + USE_CMD_ADD + "` to play.";
	public static final String pkup_status_signup		= "**.gametype.**: Sign up: [.playernumber./.maxplayer.] .playerlist.";
	public static final String pkup_status_server		= "**.gametype.**: Awaiting available server.";
//	public static final String pkup_status_players		= "**.gametype.**: Players [.playernumber./10]: .playerlist.";
//	public static final String pkup_started				= "**.gametype.**: Game has already started. .status. - .time. minutes in.";

	public static final String pkup_reset_all 			= "*All matches have been reset.*";
	public static final String pkup_reset_cur 			= "*The current matches have been reset.*";
	public static final String pkup_reset_type 			= "*.gametype. has been reset.*";
	public static final String pkup_reset_id 			= "*The match .id. has been reset.*";
	
	public static final String pkup_match_print_live	= "**[** Pickup Game #.gamenumber. **][** Live **][** Gametype: .gametype. **][** Map: .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_signup	= "**[** Pickup Game #--- **][** Signup **][** Gametype: .gametype. **][** .playernumber./.maxplayer. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_server	= "**[** Pickup Game #--- **][** AwaitingServer **][** Gametype: .gametype. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_done	= "**[** Pickup Game #.gamenumber. **][** Done **][** Score: .score. **][** Gametype: .gametype. **][** Map: .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_abort	= "**[** Pickup Game #.gamenumber. **][** Abort **][** Gametype: .gametype. **][** Map: .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_sur		= "**[** Pickup Game #.gamenumber. **][** Surrender **][** Score: .score. **][** Gametype: .gametype. **][** Map: .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	
	public static final String pkup_go_admin			= "[ Pickup Game #.gamenumber. ][ Password: .password. ][ Map: .map. ][ ELO red: .elored. ELO blue: .eloblue. ]";
	public static final String pkup_go_player			= "UrTPickup starts now! Connect to the server and join team *.team.* in order to play. Make up positions and ready up! [ /connect .server. ; password .password. ]";
	public static final String pkup_go_pub_head			= "**.gametype.**: UrTPickup #.gamenumber. (avg ELO: .elo.) is about to start!";
	public static final String pkup_go_pub_team			= "**.gametype.**: .team. team: .playerlist.";
	public static final String pkup_go_pub_map			= "**.gametype.**: Map: .map.";
	public static final String pkup_go_pub_calm			= "**.gametype.**: You will receive the connection info via DM.";
	public static final String pkup_go_pub_sent			= "**.gametype.**: All connection info has been sent. Enjoy the match!";

	public static final String pkup_aftermath_head		= "**.gametype.**: Aftermath #.gamenumber.:";
	public static final String pkup_aftermath_result	= ".team. team .result. (.score.) -";
	public static final String pkup_aftermath_player	= ".player. (.elochange.)";
	public static final String pkup_aftermath_rank		= ".player. was ranked .updown. to **.rank.**";

	public static final String pkup_config_list 		= "Gameconfig for .gametype.\n.configlist.";
	
	public static final String pkup_getelo				= "#.position.\t **.rank.**\t **.urtauth.**\t .elo. (.elochange.)";
	public static final String pkup_top5_header			= "**Top5:**";
	
	public static final String pkup_surrender_cast		= "You have voted to surrender. **.num.** more teammate.s. needed.";
	public static final String pkup_surrender_time		= "You cannot surrender this early. You still have to wait for .time..";
			 
	public static final String is_banned				= "You are banned and therefore you cannot participate.";
	public static final String map_not_found			= "Map not found.";
	public static final String map_not_unique			= "Mapstring not unique.";
	public static final String map_already_voted		= "You have already voted.";

	public static final String player_not_found			= "Player not found.";
	public static final String user_not_registered		= "You're not registered. Please use `" + USE_CMD_REGISTER + "`";

	public static final String auth_taken_urtauth		= "The urtauth has been already registered.";
	public static final String auth_taken_user			= "You have already been registered with an account.";
	public static final String auth_invalid				= "Your urtauth seems to be invalid.";
	public static final String auth_success				= "Your auth has been successfully registered with your account.";
	public static final String auth_sent_key			= "You have to register your auth name and not your auth key!!!";

	public static final String player_already_added		= "You are already added to a pickup game.";
	public static final String player_already_removed	= "You are currently not added.";
	public static final String player_cannot_remove		= "You cannot remove.";
	public static final String player_not_in_match		= "You are currently not in a match.";

	public static final String player_already_surrender	= "You have already surrendered.";

//	public static final String report_wrong_arg			= "Your report reason is invalid, check !reportlist to check the list.";
//	public static final String report_invalid_urtauth	= "No player could be found to the specific urtauth.";
//	public static final String report_not_played		= "The reported player hasn't played the past match.";
//	public static final String report_didnt_play		= "You haven't played the last match, you aren't able to report someone.";
//	public static final String report_already_reported	= "This player has already been reported by you.";
//	public static final String report_successful		= "Your report was successfully stored.";
//	public static final String report_raise_issue		= ".urtauth. has been auto-banned. [strength: .strength.] Type !review .urtauth. to review the ban.";

	public static final String wrong_argument_amount	= "Wrong amount of arguments: `.cmd.`";
	public static final String help_prefix				= "How to use the command: `.cmd.`";
	public static final String help_cmd_avi				= "These commands are available (use `" + USE_CMD_HELP + "` for more info):\n`.cmds.`";
	
	public static final String help_unknown				= "I do not know that command.";

	public static final String lock_enable				= "*Game is now locked.*";
	public static final String lock_disable				= "*Game is now unlocked.*";

	public static final String pkup_match_unavi 		= "Match is not available right now.";
	public static final String pkup_match_invalid_gt	= "No match for that gametype is available right now.";
	
	public static final String admin_cmd_successful		= "Successful: ";
	public static final String admin_cmd_unsuccessful	= "Unsuccessful: ";
}
