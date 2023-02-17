package de.gost0r.pickupbot.pickup;

public class Config {
	public static final String CMD_ADD					= "!add";
	public static final String CMD_BM					= "!bm";
	public static final String CMD_CTF					= "!ctf";
	public static final String CMD_TS					= "!ts";
	public static final String CMD_1v1					= "!1v1";
	public static final String CMD_2v2					= "!2v2";
	public static final String CMD_DIV1					= "!div1";
	public static final String CMD_REMOVE				= "!remove";
	public static final String CMD_MAPS					= "!maps";
	public static final String CMD_MAP					= "!map";
	public static final String CMD_STATUS				= "!status";
	public static final String CMD_HELP					= "!help";
	public static final String CMD_SURRENDER			= "!surrender";
	public static final String CMD_LIVE					= "!live";
	public static final String CMD_VOTES				= "!votes";
	public static final String CMD_TEAM					= "!team";
	public static final String CMD_LEAVETEAM			= "!leaveteam";
	public static final String CMD_SCRIM				= "!scrim";
	public static final String CMD_REMOVETEAM			= "!removeteam";
	public static final String CMD_TEAMS				= "!teams";
	public static final String CMD_PING					= "!ping";

	public static final String CMD_LOCK					= "!lock";
	public static final String CMD_UNLOCK				= "!unlock";
	public static final String CMD_RESET				= "!reset";
	public static final String CMD_GETDATA				= "!getdata";
	public static final String CMD_ENABLEMAP			= "!enablemap";
	public static final String CMD_DISABLEMAP			= "!disablemap";
	public static final String CMD_RCON					= "!rcon";
	public static final String CMD_FORCEADD				= "!forceadd";
	public static final String CMD_REBOOT				= "!reboot";

	public static final String CMD_ENABLEGAMETYPE		= "!enablegametype";
	public static final String CMD_DISABLEGAMETYPE		= "!disablegametype";

	public static final String CMD_ENABLEDYNSERVER		= "!enabledynservers";
	public static final String CMD_DISABLEDYNSERVER		= "!disabledynservers";

	//public static final String CMD_ADDGAMECONFIG		= "!addgameconfig";
	//public static final String CMD_REMOVEGAMECONFIG	= "!delgameconfig";
	public static final String CMD_LISTGAMECONFIG		= "!showgameconfig";

	public static final String CMD_REGISTER				= "!register";
	public static final String CMD_COUNTRY				= "!country";
	public static final String CMD_GETELO				= "!elo";
	public static final String CMD_GETSTATS				= "!stats";
	public static final String CMD_TOP_PLAYERS			= "!top10";
	public static final String CMD_TOP_COUNTRIES		= "!topcountries";
	public static final String CMD_TOP_WDL				= "!topwin";
	public static final String CMD_TOP_KDR				= "!topkdr";

	public static final String CMD_MATCH				= "!match";
	public static final String CMD_LAST					= "!last";

	//public static final String CMD_REPORT				= "!report";
	//public static final String CMD_EXCUSE				= "!excuse";
	//public static final String CMD_REPORTLIST			= "!reportlist";

	public static final String CMD_ADDBAN				= "!ban";
	public static final String CMD_REMOVEBAN			= "!unban";
	public static final String CMD_BANINFO				= "!baninfo";

	public static final String CMD_SHOWSERVERS			= "!showservers";
	public static final String CMD_ADDSERVER			= "!addserver";
	public static final String CMD_ENABLESERVER			= "!enableserver";
	public static final String CMD_DISABLESERVER		= "!disableserver";
	public static final String CMD_UPDATESERVER			= "!updateserver";

	public static final String CMD_SHOWMATCHES			= "!showmatches";
	
	public static final String CMD_UNREGISTER			= "!unregister";
	public static final String CMD_ENFORCEAC			= "!enforceac";

	public static final String CMD_ADDCHANNEL			= "!addchannel";
	public static final String CMD_REMOVECHANNEL		= "!removechannel";
	public static final String CMD_ADDROLE				= "!addrole";
	public static final String CMD_REMOVEROLE			= "!removerole";

	public static final String CMD_RESETELO  			= "!resetelo";

	public static final String PUB_LIST = "" + CMD_ADD + " " + CMD_REMOVE + " " + CMD_MAPS + " " + CMD_MAP + " " + CMD_MATCH + " " 
	+ CMD_LAST + " " + CMD_LIVE + " " + CMD_STATUS + " " + CMD_HELP + " " + CMD_REGISTER + " " + CMD_GETELO + " " + CMD_TOP_PLAYERS 
	+ " " + CMD_TOP_COUNTRIES + " " + CMD_TOP_KDR + " " + CMD_TOP_WDL + " " + CMD_COUNTRY + " " + CMD_SURRENDER + " " + CMD_BANINFO 
	+ " " + CMD_VOTES + " " + CMD_LAST + " " + CMD_TEAM + " " + CMD_LEAVETEAM + " " + CMD_SCRIM + " " + CMD_REMOVETEAM + " " + CMD_TEAMS;
	
	public static final String ADMIN_LIST = "" + CMD_LOCK + " " + CMD_UNLOCK + " " + CMD_RESET + " " + CMD_GETDATA + " " + CMD_ENABLEMAP 
	+ " " + CMD_DISABLEMAP + " " + CMD_RCON + " " + CMD_SHOWSERVERS + " " + CMD_ENABLEGAMETYPE + " " + CMD_DISABLEGAMETYPE + " " 
	+ CMD_ADDSERVER + " " + CMD_ENABLESERVER + " " + CMD_DISABLESERVER + " " + CMD_UPDATESERVER + " " + CMD_ADDBAN + " " + CMD_REMOVEBAN 
	+ " " + CMD_SHOWMATCHES + " " + CMD_UNREGISTER + " " + CMD_ADDROLE + " " + CMD_REMOVEROLE + " " + CMD_ADDCHANNEL + " " + CMD_REMOVECHANNEL 
	+ " " + CMD_FORCEADD + " " + CMD_REBOOT;

//------------------------------------------------------------------------------------//


	public static final String USE_CMD_ADD				= "!<gametype>";
	public static final String USE_CMD_REMOVE			= "!remove <gametype>";
	public static final String USE_CMD_BM				= "!bm <map>";
	public static final String USE_CMD_CTF				= "!ctf <map>";
	public static final String USE_CMD_TS				= "!ts <map>";
	public static final String USE_CMD_1v1				= "!1v1 <map>";
	public static final String USE_CMD_2v2				= "!2v2 <map>";
	public static final String USE_CMD_DIV1				= "!div1 <map>";
	public static final String USE_CMD_MAPS				= "!maps displays the map list for each gametype.";
	public static final String USE_CMD_MAP				= "!map <gametype> <mapname>";
	public static final String USE_CMD_STATUS			= "Type !status to get information on the queues.";
	public static final String USE_CMD_HELP				= "!help <command>";
	public static final String USE_CMD_SURRENDER		= "Type !surrender to abandon your match.";
	public static final String USE_CMD_LIVE				= "!live sends info on the live matches.";
	public static final String USE_CMD_VOTES			= "Type !votes to get the current votes.";

	public static final String USE_CMD_LOCK				= "!lock to prevent joining the queues.";
	public static final String USE_CMD_UNLOCK			= "!unlock";
	public static final String USE_CMD_RESET			= "!reset <all/cur/type/id>";
	public static final String USE_CMD_GETDATA			= "!getdata";
	public static final String USE_CMD_ENABLEMAP		= "!enablemap <ut4_map> <gametype>";
	public static final String USE_CMD_DISABLEMAP		= "!disablemap <ut4_map> <gametype>";
	public static final String USE_CMD_RCON				= "!rcon <serverid> <rconstring>";
	public static final String USE_CMD_FORCEADD			= "!forceadd <gamemode> </@DiscordUser|urtauth/>";
	public static final String USE_CMD_REBOOT			= "Use !reboot to restart the BOT. This will reset queues and current matches.";

	public static final String USE_CMD_ENABLEGAMETYPE	= "!enablegametype <name> <teamsize>";
	public static final String USE_CMD_DISABLEGAMETYPE	= "!disablegametype <name>";

	//public static final String USE_CMD_ADDGAMECONFIG	= "!addgameconfig <gametype> <string>";
	//public static final String USE_CMD_REMOVEGAMECONFIG	= "!delgameconfig <gametype> <string>";
	public static final String USE_CMD_LISTGAMECONFIG	= "!showgameconfig <gametype>";

	public static final String USE_CMD_REGISTER			= "!register <urtauth>";
	public static final String USE_CMD_COUNTRY			= "!country <COUNTRY CODE> See:` <https://datahub.io/core/country-list/r/0.html>";
	public static final String USE_CMD_CHANGE_COUNTRY	= "!country <urtauth> <COUNTRY CODE> See:` <https://datahub.io/core/country-list/r/0.html>";
	public static final String USE_CMD_GETELO			= "!elo </@User|urtauth/>";
	public static final String USE_CMD_GETSTATS			= "!stats </@User|urtauth/>";
	public static final String USE_CMD_TOP10			= "!top10 displays the top 10 players";
	public static final String USE_CMD_TOP_COUNTRIES	= "!topcountries ordered by average ELO";
	public static final String USE_CMD_TOP_WDL			= "!topwin <gametype>: players with the best win ratio for a specific gamemode";
	public static final String USE_CMD_TOP_KDR			= "!topkdr <gametype>: players with the best KDR for a specific gamemode";

	public static final String USE_CMD_MATCH			= "!match <id>";
	public static final String USE_CMD_LAST				= "!last </@User|urtauth/>";

	//public static final String USE_CMD_REPORT			= "!report <qauth> <reason>";
	//public static final String USE_CMD_EXCUSE			= "!excuse <excuse>";
	//public static final String USE_CMD_REPORTLIST		= "!reportlist";

	public static final String USE_CMD_ADDBAN			= "!ban <urtauth> <reason> <duration> (duration=1y1M1w1d1h1m1s)";
	public static final String USE_CMD_REMOVEBAN		= "!unban <urtauth>";
	public static final String USE_CMD_BANINFO			= "!baninfo </@User|urtauth/>";

	public static final String USE_CMD_SHOWSERVERS		= "!showservers";
	public static final String USE_CMD_ADDSERVER		= "!addserver <ip:port> <rcon> <region>";
	public static final String USE_CMD_ENABLESERVER		= "!enableserver <id>";
	public static final String USE_CMD_DISABLESERVER	= "!disableserver <id>";
	public static final String USE_CMD_UPDATESERVER		= "!updateserver <id> <rcon>";

	public static final String USE_CMD_SHOWMATCHES		= "!showmatches displays the queues AND live matches";

	public static final String USE_CMD_UNREGISTER		= "!unregister <urtauth>";
	public static final String USE_CMD_ENFORCEAC		= "!enforceac <urtauth>";

	public static final String USE_CMD_ADDCHANNEL		= "!addchannel <#name> <public/admin>";
	public static final String USE_CMD_REMOVECHANNEL	= "!removechannel <#name> <public/admin>";
	public static final String USE_CMD_ADDROLE			= "!addrole <@role> <admin/superadmin>";
	public static final String USE_CMD_REMOVEROLE		= "!removerole <@role> <admin/superadmin>";

	public static final String USE_CMD_SCRIM			= "!scrim <ts/ctf/2v2>";
	public static final String USE_CMD_REMOVETEAM		= "!removeteam <gametype>";
	public static final String USE_CMD_TEAM				= "!team <@user1> <@user2> <...>";
	public static final String USE_CMD_LEAVETEAM		= "!leaveteam removes you from your current team";
	public static final String USE_CMD_TEAMS			= "!teams lists the active teams";

	//------------------------------------------------------------------------------------//
	
	public static final String INT_PICK					= "pick";
	public static final String INT_LAUNCHAC				= "launchac";
	public static final String INT_TEAMINVITE			= "teaminvite";
	public static final String INT_TEAMREMOVE			= "teamremove";
	public static final String INT_SEASONSTATS			= "seasonstats";
	public static final String INT_SEASONLIST			= "seasonlist";
	public static final String INT_SEASONSELECTED		= "seasonselected";
	
	//------------------------------------------------------------------------------------//

	public static final String BTN_LAUNCHAC				= "Connect to server";

	//------------------------------------------------------------------------------------//
	public static final String pkup_lock				= "Pickup games are now locked. :lock:";
	public static final String pkup_map					= "You voted for .map..";
	public static final String pkup_map_list			= "**.gametype.**: .maplist.";
//	public static final String pkup_signup				= "You can sign up again!";
	public static final String pkup_pw					= "[ /connect .server. ; password .password. ]";
	public static final String pkup_status_noone		= "**.gametype.**: Nobody signed up. Type `" + USE_CMD_ADD + "` to play.";
	public static final String pkup_status_signup		= "**.gametype.**: Sign up: [.playernumber./.maxplayer.] .playerlist.";
	public static final String pkup_status_server		= "**.gametype.**: The match is about to start, while captains are picking players, you can continue to vote for a map with ``!map <map>``. \nCurrent votes: .votes.";
//	public static final String pkup_status_players		= "**.gametype.**: Players [.playernumber./10]: .playerlist.";
//	public static final String pkup_started				= "**.gametype.**: Match has already started. .status. - .time. minutes in.";

	public static final String pkup_reset_all 			= "*All matches and queues have been reset.*";
	public static final String pkup_reset_cur 			= "*All queues have been reset.*";
	public static final String pkup_reset_type 			= "*.gametype. queue has been reset.*";
	public static final String pkup_reset_id 			= "*Match #.id. has been reset.*";

	public static final String pkup_match_print_live	= "**[** Match #.gamenumber. **][** Live **][** .gametype. **][** .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_signup	= "**[** Match #--- **][** Signup **][** Gametype: .gametype. **][** .playernumber./.maxplayer. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_server	= "**[** Match #--- **][** AwaitingServer **][** Gametype: .gametype. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_done	= "**[** Match #.gamenumber. **][** Done **][** Score: .score. **][** Gametype: .gametype. **][** .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_abort	= "**[** Match #.gamenumber. **][** Abort **][** .gametype. **][** .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_abandon	= "**[** Match #.gamenumber. **][** Abandon **][** .gametype. **][** .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";
	public static final String pkup_match_print_sur		= "**[** Match #.gamenumber. **][** Surrender **][** Score: .score. **][** .gametype. **][** .map. **][** ELO red: .elored. ELO blue: .eloblue. **][** Players: .playerlist. **]**";

	public static final String pkup_match_print_info	= "**.gametype. #.gamenumber.**: **[**.map.**] [**.ingame.**] [**.redteam.**]** VS **[**.blueteam.**]**";

	public static final String pkup_go_player			= "Pickup starts now! Connect, choose positions and ready up! **[ /connect .server. ; password .password. ]**";
	public static final String pkup_go_player_ac		= "Pickup starts now! Connect, choose positions and ready up!\n :warning: **You are forced to use the anticheat, click the button below to connect.**";
	public static final String pkup_go_captains			= "Pickup is about to start and you are captain! Please pick players in the recently created discord thread.";
	public static final String pkup_go_pub_head			= "**.gametype.: Match #.gamenumber.** .region. (avg ELO: .elo.)";
	public static final String pkup_go_pub_team			= ".team. team: .playerlist.";
	public static final String pkup_go_pub_map			= "Map: .map.";
	public static final String pkup_go_pub_calm			= "**GTV**: connect gtv.b00bs-clan.com:709; password SevenAndJehar"; // temporarily hard coded
	public static final String pkup_go_pub_calm_notavi	= "GTV: not available";
	public static final String pkup_go_pub_sent			= "**.gametype.**: Server info has been sent. If you didn't get a DM try **!lostpass**.";
	public static final String pkup_go_pub_threadtitle	= "Match .ID.";
	public static final String pkup_go_pub_captains 	= "The captains are .captain1. (**red**) and .captain2. (**blue**) as they have the highest elo. Player stats:";
	public static final String pkup_go_pub_pick 		= ".captain. pick a player:";
	public static final String pkup_go_pub_pickjoin		= ".pick. joins the **.color.** team.";
	public static final String pkup_go_pub_servspawn	= "**Spawning Server:** .flag. ``.city.`` (fairest server to all players)\n:hourglass: The ip will be send to all players when the server is spawned.";

	public static final String pkup_aftermath_head		= "**.gametype.**: Aftermath #.gamenumber. (.map.):";
	public static final String pkup_aftermath_result	= ".team. .result. (.score.) -";
	public static final String pkup_aftermath_player	= ".player. (.elochange.)";
	public static final String pkup_aftermath_rank		= ".player. was ranked .updown. to **.rank.**";
	public static final String pkup_aftermath_abandon_1	= "Match was abandoned due to **.reason.**.";
	public static final String pkup_aftermath_abandon_2	= ".players. .be. punished accordingly.";

	public static final String pkup_config_list			= "Gameconfig for .gametype.\n.configlist.";

	public static final String pkup_getelo				= "#.position.\t **.rank.**\t .country.   **.urtauth.**\t .elo.\t .wdl.%\t .kdr.";
	public static final String pkup_getelo_country		= "#.position.\t .country.\t .elo.";
	public static final String pkup_top5_header			= "**Top countries:**";

	public static final String pkup_surrender_cast		= "You voted to surrender. **.num.** more teammate.s. needed.";
	public static final String pkup_surrender_time		= "You cannot surrender this early. Please wait .time..";

	public static final String is_banned				= ".user. (.urtauth.) is suspended .time. for .reason.";
	public static final String is_unbanned				= ".user. (.urtauth.) is unbanned.";
	public static final String is_notbanned				= ".urtauth. is not banned (yet).";
	public static final String not_banned				= "No active bans found for .urtauth.";
	public static final String ban_history				= "**__Ban history:__** (Past 2 months)";
	public static final String ban_history_item			= "<t:.date.:d> .duration. .reason.";
	public static final String map_not_found			= "Map not found.";
	public static final String map_not_unique			= "Mapstring not unique.";
	public static final String map_cannot_vote			= "You cannot vote right now.";
	public static final String map_specify_gametype		= "Please use: **!map <gametype> <map>**.";
	public static final String map_played_last_game		= "This map was played last game, please vote for a different map.";

	public static final String player_not_found			= "Player not found.";
	public static final String user_not_registered		= "You're not registered. Please use `" + USE_CMD_REGISTER + "`";
	public static final String other_user_not_registered= "The user .user. is not registered.";
	public static final String country_added			= "Your country has been set.";

	public static final String auth_taken_urtauth		= "This **urtauth** is already registered.";
	public static final String auth_taken_user			= "You have already registered an account.";
	public static final String auth_invalid				= "Your **urtauth** seems to be invalid.";
	public static final String auth_success				= "Your **urtauth** has been linked to your account.";
	public static final String auth_success_admin		= "The user .user. registered using the auth ``.urtauth.``.";
	public static final String auth_sent_key			= "You have to register your auth name and not your auth key!!!";

	public static final String player_already_added		= "You are already added to a pickup game.";
	public static final String player_already_removed	= "You are not added to any pickup game.";
	public static final String player_cannot_add		= "You cannot add right now.";
	public static final String player_cannot_remove		= "You cannot remove.";
	public static final String player_not_in_match		= "You are not added to any queue.";
	public static final String player_already_match		= "You are already in a match.";
	public static final String player_not_admin			= "You must be an admin to use this command.";
	public static final String player_notdiv1			= "Hi <:puma:849287183474884628>, you need to be in the either Top ``.minrank.``  **ELO** (#``.rank.``) *OR* Top ``.minkdrrank.``  **KDR** (#``.kdrrank.``) *OR* Top ``.minwinrank.``  **Win Rate** (#``.winrank.``) to add to the **div1** queue. Keep practicing on Sexy CTF.";

	public static final String player_not_captain		= "You can't pick a player. You are not captain or it is not your turn to pick.";

	public static final String player_already_surrender	= "You already surrendered.";
	
	public static final String afk_reminder				= "**[AFK]** .user. will be removed in 3 minutes. Write something in the channel to stay in queue.";
	public static final String pick_reminder			= "**[CAPTAIN PICK]** .user. you have 1 min to pick a player in the game thread channel.";
	public static final String pick_reset				= "**[CAPTAIN PICK]** The match **.matchid.** has been reset. .user. did not pick a player in time and was punished accordingly.";

//	public static final String report_wrong_arg			= "Your report reason is invalid, check !reportlist to check the possible reasons.";
//	public static final String report_invalid_urtauth	= "No player could be found with this urtauth.";
//	public static final String report_not_played		= "The reported player hasn't played the past match.";
//	public static final String report_didnt_play		= "You haven't played the last match, you aren't able to report someone.";
//	public static final String report_already_reported	= "This player has already been reported by you.";
//	public static final String report_successful		= "Your report was successfully submited.";
//	public static final String report_raise_issue		= ".urtauth. has been auto-banned. [strength: .strength.] Type !review .urtauth. to review the ban.";

	public static final String wrong_argument_amount	= "Wrong amount of arguments: `.cmd.`";
	public static final String help_prefix				= "How to use the command: `.cmd.`";
	public static final String help_cmd_avi				= "Here is the command list (use `" + USE_CMD_HELP + "` for more info):\n`.cmds.`";

	public static final String help_unknown				= "I do not know that command :(";

	public static final String lock_enable				= "*Game is LOCKED.* :lock:";
	public static final String lock_disable				= "*Game is now unlocked.*";

	public static final String pkup_match_unavi 		= "Match is not available right now.";
	public static final String pkup_match_invalid_gt	= "No match for that gametype is available right now.";
	public static final String no_gt_found				= "Unable to find a matching gametype. Try `!add ctf`";
	public static final String no_gt_team_found			= "Unable to find a matching gametype. Try `!scrim ctf`";
	
	public static final String banreason_not_found		= "Use one of the following ban reasons: .banreasons.";	
	public static final String banduration_invalid		= "Invalid ban duration. Try 1m,1h,1d,1w,1M.";

	public static final String admin_cmd_successful		= ":white_check_mark: Successful: ";
	public static final String admin_cmd_unsuccessful	= ":x: Unsuccessful: ";
	public static final String wait_testing_server		= "Testing server list. This can take a while...";
	public static final String admin_enforce_ac_on		= ":white_check_mark: The anticheat is now enforced for the player ``.urtauth.``";
	public static final String admin_enforce_ac_off		= ":negative_squared_cross_mark: The anticheat is no longer enforced for the player ``.urtauth.``";
	
	public static final String bot_online				= "The bot is back online!";

	public static final String elo_reset   				= "The elo has been reset! Don't forget to reboot the bot and remove all rank roles manually.";

	public static final String ftw_playernotinmatch   	= "Player not in match";
	public static final String ftw_matchnotfound    	= "Match not found";
	public static final String ftw_success     	    	= "Game launched";
	public static final String ftw_notconnected  	    = "User not connected to the FTW launcher";
	public static final String ftw_error 		  	    = "Unknown error";
	public static final String ftw_error_noping	  	    = "Check your dms and click on the link to register your ping in the different server locations.";
	public static final String ftw_dm_noping	  	    = "Please click here to register your ping in the different server locations: .url.";

	public static final String team_involved_other		= "You are already involved in an active team. You can leave it by sending ``!leaveteam``.";
	public static final String team_already_involved	= "``.auth.`` is already in a team.";
	public static final String team_is_full				= "Your team already has 5 players.";
	public static final String team_already_in			= "The player ``.auth.`` is already in your team.";
	public static final String team_already_invited		= "The player ``.auth.`` has already been invited to your team.";
	public static final String team_invited				= ".invited. You are invited to join .captain.'s team.";
	public static final String team_accepted			= ".player. is now a member of this team.";
	public static final String team_declined			= ".player. declined the invitation to join this team.";
	public static final String team_canceled			= ".player.'s invitation to join your team has been canceled.";
	public static final String team_removed				= ".player. has been removed from this team.";
	public static final String team_leave_captain		= "Your team has been dissolved.";
	public static final String team_leave				= "You left ``.captain.``'s team.";
	public static final String team_noteam				= "You are currently not involved in any team.";
	public static final String team_noteam_captain		= "You are not captain of any team.";
	public static final String team_error_invite		= "Only the player ``.player.`` can answer.";
	public static final String team_error_active		= "This team is no longer active.";
	public static final String team_error_remove		= "Only the team captain or the player in question can press this button.";
	public static final String team_error_teamsize      = "You need to be exactly ``.teamsize.`` in your team to queue for this gamemode.";
	public static final String team_error_wrong_gt      = "You can't add as a team for that gamemode.";
	public static final String team_added				= "**.gamemode.**: Team sign up: .team.";
	public static final String team_removed_queue		= "Team removed from all queues: .team.";
	public static final String team_cant_soloqueue		= "You can't queue by yourself as you are currently in a team. ``!leaveteam`` to solo queue.";
	public static final String team_print_info			= "Your current team: .team.";
	public static final String team_print_all			= "__List of all active teams:__";
	public static final String team_print_noteam		= "No team is currently active.";
	public static final String team_only_mentions 		= "Please specify players using: ``!team @user``.";
	public static final String team_no_scrim	 		= "**SCRIM**: No team signed up. Type ``!scrim <ts/ctf/2v2>`` to play.";
}
