package de.gost0r.pickupbot.pickup;

import java.util.List;
import java.util.logging.Logger;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordChannelType;
import de.gost0r.pickupbot.discord.DiscordUser;

public class PickupBot extends DiscordBot {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private DiscordChannel pubchan;
	
	private PickupLogic logic;
	
	@Override
	public void init() {
		super.init();
		
//		pubchan = DiscordChannel.findChannel("143233743107129344"); // pickupadmins
		pubchan = DiscordChannel.findChannel("402541587164561419"); // public testchan
		
		logic = new PickupLogic(this);
//		logic.cmdEnableGametype("TEST", "1");
	}

	@Override
	protected void recvMessage(DiscordUser user, DiscordChannel channel, String msg) {
		LOGGER.info("RECV #" + ((channel == null || channel.name == null) ?  "null" : channel.name) + " " + user.username + ": " + msg);
		
		if (user.equals(self)) {
//			System.out.println("Msg from self, ignore.");
			return;
		}
		
		String[] data = msg.split(" ");

		if (channel.equals(pubchan))
		{
			Player p = Player.get(user);
			// Execute code according to cmd
			switch (data[0].toLowerCase()) 
			{
				case Config.CMD_ADD:
					if (data.length == 2)
					{
						if (p != null)
						{					
							logic.cmdAddPlayer(p, data[1]);
						}
						else sendNotice(user, Config.user_not_registered);
					}
					else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADD));
					break;
					
				case Config.CMD_REMOVE:
					if (data.length == 1)
					{
						if (p != null)
						{
							logic.cmdRemovePlayer(p);
						}
						else sendNotice(user, Config.user_not_registered);
					}
					else if (data.length == 2)
					{
						if (hasAdminRights(user))
						{
							DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
							if (u != null) {
								Player player = Player.get(u);
								if (player != null) {
									logic.cmdRemovePlayer(player);
								}
								else sendNotice(user, Config.player_not_found);
							}
							else sendNotice(user, Config.player_not_found);
						}
						else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVE));
					}
					else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVE));
					break;
					
				case Config.CMD_MAPS:
				case Config.CMD_MAP:
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdGetMaps();
						}
						else if (data.length == 2)
						{
							logic.cmdMapVote(p, data[1]);
						}
						else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_MAP));
					}
					else sendNotice(user, Config.user_not_registered);
					break;
					
				case Config.CMD_STATUS:
					if (data.length == 1)
					{
						logic.cmdStatus();
					}
					else super.sendMsg(channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_STATUS));
					break;
					
				case Config.CMD_SURRENDER:
					if (data.length == 1)
					{
						if (p != null)
						{
							logic.cmdSurrender(p);
						}
					}
					else super.sendMsg(channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SURRENDER));
					break;
					
				case Config.CMD_RESET:
					if (hasAdminRights(user))
					{
						if (data.length == 1)
						{
							logic.cmdReset("all");
						}
						else if (data.length == 2)
						{
							logic.cmdReset(data[1]);
						}
						else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RESET));
					}
					break;
				
				case Config.CMD_LOCK:
					if (hasAdminRights(user))
					{
						if (data.length == 1)
						{
							logic.cmdLock();
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LOCK));
					}
					break;
					
				case Config.CMD_UNLOCK:
					if (hasAdminRights(user))
					{
						if (data.length == 1)
						{
							logic.cmdUnlock();
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNLOCK));
					}
					break;
					
				case Config.CMD_GETELO:
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdGetElo(p);
						}
						else if (data.length == 2)
						{
							Player pOther = null;
							DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
							if (u != null)
							{
								pOther = Player.get(u);
							}
							else
							{
								pOther = Player.get(data[1].toLowerCase());
							}
							
							if (pOther != null)
							{
								logic.cmdGetElo(pOther);
							}
							else sendNotice(user, Config.player_not_found);
						}
						else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETELO));	
					}
					else sendNotice(user, Config.user_not_registered);
					break;
					
				case Config.CMD_TOP5: 
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdTopElo(5);
						}
						else super.sendMsg(channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_TOP5));
					}
					else sendNotice(user, Config.user_not_registered);
					break;

				case Config.CMD_REGISTER:
					if (data.length == 2)
					{
						logic.cmdRegisterPlayer(user, data[1].toLowerCase());
					}
					else sendNotice(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REGISTER));
					break;
			}
		}

		if (channel.type == DiscordChannelType.DM)
		{
			if (hasAdminRights(user))
			{
				// Execute code according to cmd
				switch (data[0].toLowerCase())
				{
					case Config.CMD_GETDATA:
						if (data.length == 2)
						{
							logic.cmdGetData(user, data[1]);
						}
						else super.sendMsg(channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETDATA));
						break;
						
					case Config.CMD_ENABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdEnableMap(data[1], data[2]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEMAP));
						break;
						
					case Config.CMD_DISABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdDisableMap(data[1], data[2]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLEMAP));
						break;
						
					case Config.CMD_ENABLEGAMETYPE:
						if (data.length == 3)
						{
							if (logic.cmdEnableGametype(data[1], data[2]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));	
						break;
						
					case Config.CMD_DISABLEGAMETYPE:
						if (data.length == 2)
						{
							if (logic.cmdDisableGametype(data[1]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));
						break;
						
					case Config.CMD_ADDGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdAddGameConfig(data[1], msg.substring(Config.CMD_ADDGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG));
						break;
						
					case Config.CMD_REMOVEGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdRemoveGameConfig(data[1], msg.substring(Config.CMD_REMOVEGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG));
						break;
						
					case Config.CMD_LISTGAMECONFIG:
						if (data.length == 2)
						{
							if (logic.cmdListGameConfig(user, data[1]))
							{
								// if successful it will print the info
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG));
						break;
						
					case Config.CMD_ADDSERVER:
						if (data.length == 3)
						{
							if (logic.cmdAddServer(data[1], data[2]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDSERVER));
						break;
						
					case Config.CMD_ENABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], true))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLESERVER));
						break;
						
					case Config.CMD_DISABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], false))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLESERVER));
						break;
						
					case Config.CMD_UPDATESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerChangeRcon(data[1], data[1]))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UPDATESERVER));
						break;
						
					case Config.CMD_SHOWSERVERS:
						if (data.length == 1)
						{
							logic.cmdServerList(user);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWSERVERS));
						break;
					
					case Config.CMD_RCON:
						if (data.length > 2)
						{
							if (logic.cmdServerSendRcon(data[1], msg.substring(Config.CMD_RCON.length() + data[1].length() + 2)))
							{
								super.sendMsg(channel, Config.admin_cmd_successful + msg);
							}
							else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RCON));
						break;
						
					case Config.CMD_SHOWMATCHES:
						if (data.length == 1)
						{
							logic.cmdMatchList(user);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWMATCHES));
						break;
						
					case Config.CMD_UNREGISTER:
						if (data.length == 2)
						{
							Player player = Player.get(data[1].toLowerCase());
							if (player != null)
							{
								if (logic.cmdUnregisterPlayer(player))
								{
									super.sendMsg(channel, Config.admin_cmd_successful + msg);
								}
								else super.sendMsg(channel, Config.admin_cmd_unsuccessful + msg);
							}
							else sendNotice(user, Config.player_not_found);
						}
						else super.sendMsg(user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNREGISTER));
						break;		
				}
			}
		}
		if (channel == getPubchan() || channel.type == DiscordChannelType.DM )
		{
			if (data[0].toLowerCase().equals(Config.CMD_HELP))
			{
				if (data.length == 2)
				{
					String cmd = (!data[1].startsWith("!") ? "!" : "") + data[1];
					switch (cmd)
					{
						case Config.CMD_ADD: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADD)); break;
						case Config.CMD_REMOVE: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVE)); break;
						case Config.CMD_MAPS: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAPS)); break;
						case Config.CMD_MAP: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAP)); break;
						case Config.CMD_STATUS: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_STATUS)); break;
						case Config.CMD_HELP: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_HELP)); break;
						case Config.CMD_LOCK: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LOCK)); break;
						case Config.CMD_UNLOCK: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UNLOCK)); break;
						case Config.CMD_RESET: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RESET)); break;
						case Config.CMD_GETDATA: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETDATA)); break;
						case Config.CMD_ENABLEMAP: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEMAP)); break;
						case Config.CMD_DISABLEMAP: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEMAP)); break;
						case Config.CMD_RCON: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RCON)); break;
						case Config.CMD_REGISTER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REGISTER)); break;
						case Config.CMD_GETELO: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETELO)); break;
						case Config.CMD_TOP5: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_TOP5)); break;
						case Config.CMD_SURRENDER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SURRENDER)); break;
						//case Config.CMD_REPORT: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORT)); break;
						//case Config.CMD_EXCUSE: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_EXCUSE)); break;
						//case Config.CMD_REPORTLIST: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORTLIST)); break;
						//case Config.CMD_ADDBAN: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDBAN)); break;
						//case Config.CMD_REMOVEBAN: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEBAN)); break;
						case Config.CMD_SHOWSERVERS: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWSERVERS)); break;
						case Config.CMD_ADDSERVER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDSERVER)); break;
						case Config.CMD_ENABLESERVER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLESERVER)); break;
						case Config.CMD_DISABLESERVER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLESERVER)); break;
						case Config.CMD_UPDATESERVER: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UPDATESERVER)); break;
						case Config.CMD_ENABLEGAMETYPE: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE)); break;
						case Config.CMD_DISABLEGAMETYPE: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEGAMETYPE)); break;
						case Config.CMD_SHOWMATCHES: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWMATCHES)); break;
						case Config.CMD_ADDGAMECONFIG: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG)); break;
						case Config.CMD_REMOVEGAMECONFIG: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG)); break;
						case Config.CMD_LISTGAMECONFIG: super.sendMsg(channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG)); break;
						default: super.sendMsg(channel, Config.help_unknown); break;
					}
				}
				else
				{
					if (channel.equals(pubchan))
					{
						super.sendMsg(channel, Config.help_cmd_avi.replace(".cmds.", Config.PUB_LIST));
					}
					if (hasAdminRights(user) && channel.type == DiscordChannelType.DM )
					{
						super.sendMsg(channel, Config.help_cmd_avi.replace(".cmds.", Config.ADMIN_LIST));
					}
				}
			}
		}
		if (data[0].toLowerCase().equals("!showroles"))
		{
			DiscordUser u = user;
			if (data.length == 2)
			{
				DiscordUser testUser = super.parseMention(data[1]);
				if (testUser != null)
				{
					u = testUser;
				}
			}
			List<String> list = u.getRoles(DiscordBot.getGuildID());
			String message = "";
			for (String s : list)
			{
				message += "<@&" + s + "> ";
			}
			sendNotice(u, message);
		}
		if (data[0].toLowerCase().equals("!showadminroles"))
		{			
			String message = "Admin roles: ";
			for (String s : logic.getAdminList())
			{
				message += "<@&" + s + "> ";
			}
			sendNotice(user, message);
		}
	}
		
	public boolean hasAdminRights(DiscordUser user) {
		List<String> roleList = user.getRoles(DiscordBot.getGuildID());
		List<String> adminList = logic.getAdminList();
		for (String s : roleList) {
			for (String r : adminList) {
				if (s.equals(r)) {
					return true;
				}
			}
		}
		return false;
	}


	public void sendNotice(DiscordUser user, String msg) {
		sendMsg(getPubchan(), user.getMentionString() + " " + msg);
	}
	
	public DiscordChannel getPubchan() {
		return pubchan;
	}

	public void setPubchan(DiscordChannel pubchan) {
		this.pubchan = pubchan;
	}
}