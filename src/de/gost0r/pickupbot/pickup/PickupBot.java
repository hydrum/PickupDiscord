package de.gost0r.pickupbot.pickup;

import java.util.List;
import java.util.logging.Logger;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordChannelType;
import de.gost0r.pickupbot.discord.DiscordMessage;
import de.gost0r.pickupbot.discord.DiscordRole;
import de.gost0r.pickupbot.discord.DiscordUser;

public class PickupBot extends DiscordBot {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private DiscordChannel latestMessageChannel;
	
	private PickupLogic logic;
	
	@Override
	public void init() {
		super.init();
		
//		pubchan = DiscordChannel.findChannel("143233743107129344"); // pickup_dev
//		pubchan = DiscordChannel.findChannel("402541587164561419"); // urtpickup
		
		logic = new PickupLogic(this);
//		logic.cmdEnableGametype("TEST", "1");
	}

	@Override
	protected void recvMessage(DiscordMessage msg) {
		LOGGER.info("RECV #" + ((msg.channel == null || msg.channel.name == null) ?  "null" : msg.channel.name) + " " + msg.user.username + ": " + msg.content);
		
		this.latestMessageChannel = msg.channel;
		
		if (msg.user.equals(self)) {
//			System.out.println("Msg from self, ignore.");
			return;
		}
		
		String[] data = msg.content.split(" ");

		if (isPublicChannel(msg.channel))
		{
			Player p = Player.get(msg.user);
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
						else sendNotice(msg.user, Config.user_not_registered);
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADD));
					break;
					
				case Config.CMD_REMOVE:
					if (data.length == 1)
					{
						if (p != null)
						{
							logic.cmdRemovePlayer(p);
						}
						else sendNotice(msg.user, Config.user_not_registered);
					}
					else if (data.length == 2)
					{
						if (hasAdminRights(msg.user))
						{
							DiscordUser u = DiscordUser.getUser(data[1].replaceAll("[^\\d.]", ""));
							if (u != null) {
								Player player = Player.get(u);
								if (player != null) {
									logic.cmdRemovePlayer(player);
								}
								else sendNotice(msg.user, Config.player_not_found);
							}
							else sendNotice(msg.user, Config.player_not_found);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVE));
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVE));
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
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_MAP));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_STATUS:
					if (data.length == 1)
					{
						logic.cmdStatus();
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_STATUS));
					break;
					
				case Config.CMD_SURRENDER:
					if (data.length == 1)
					{
						if (p != null)
						{
							logic.cmdSurrender(p);
						}
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SURRENDER));
					break;
					
				case Config.CMD_RESET:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdReset("all");
						}
						else if (data.length == 2)
						{
							logic.cmdReset(data[1]);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RESET));
					}
					break;
				
				case Config.CMD_LOCK:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdLock();
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LOCK));
					}
					break;
					
				case Config.CMD_UNLOCK:
					if (hasAdminRights(msg.user))
					{
						if (data.length == 1)
						{
							logic.cmdUnlock();
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNLOCK));
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
							else sendNotice(msg.user, Config.player_not_found);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETELO));	
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;
					
				case Config.CMD_TOP5: 
					if (p != null)
					{
						if (data.length == 1)
						{
							logic.cmdTopElo(5);
						}
						else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_TOP5));
					}
					else sendNotice(msg.user, Config.user_not_registered);
					break;

				case Config.CMD_REGISTER:
					if (data.length == 2)
					{
						logic.cmdRegisterPlayer(msg.user, data[1].toLowerCase(), msg.id);
					}
					else sendNotice(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REGISTER));
					break;
			}
		}

		if (msg.channel.type == DiscordChannelType.DM)
		{
			if (hasAdminRights(msg.user))
			{
				// Execute code according to cmd
				switch (data[0].toLowerCase())
				{
					case Config.CMD_GETDATA:
						if (data.length == 2)
						{
							logic.cmdGetData(msg.user, data[1]);
						}
						else super.sendMsg(msg.channel, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_GETDATA));
						break;
						
					case Config.CMD_ENABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdEnableMap(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEMAP));
						break;
						
					case Config.CMD_DISABLEMAP:
						if (data.length == 3)
						{
							if (logic.cmdDisableMap(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLEMAP));
						break;
						
					case Config.CMD_ENABLEGAMETYPE:
						if (data.length == 3)
						{
							if (logic.cmdEnableGametype(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));	
						break;
						
					case Config.CMD_DISABLEGAMETYPE:
						if (data.length == 2)
						{
							if (logic.cmdDisableGametype(data[1]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE));
						break;
						
					case Config.CMD_ADDGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdAddGameConfig(data[1], msg.content.substring(Config.CMD_ADDGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG));
						break;
						
					case Config.CMD_REMOVEGAMECONFIG:
						if (data.length >= 3)
						{
							if (logic.cmdRemoveGameConfig(data[1], msg.content.substring(Config.CMD_REMOVEGAMECONFIG.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG));
						break;
						
					case Config.CMD_LISTGAMECONFIG:
						if (data.length == 2)
						{
							if (logic.cmdListGameConfig(msg.user, data[1]))
							{
								// if successful it will print the info
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG));
						break;
						
					case Config.CMD_ADDSERVER:
						if (data.length == 3)
						{
							if (logic.cmdAddServer(data[1], data[2]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ADDSERVER));
						break;
						
					case Config.CMD_ENABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], true))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_ENABLESERVER));
						break;
						
					case Config.CMD_DISABLESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerActivation(data[1], false))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_DISABLESERVER));
						break;
						
					case Config.CMD_UPDATESERVER:
						if (data.length == 2)
						{
							if (logic.cmdServerChangeRcon(data[1], data[1]))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UPDATESERVER));
						break;
						
					case Config.CMD_SHOWSERVERS:
						if (data.length == 1)
						{
							logic.cmdServerList(msg.user);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWSERVERS));
						break;
					
					case Config.CMD_RCON:
						if (data.length > 2)
						{
							if (logic.cmdServerSendRcon(data[1], msg.content.substring(Config.CMD_RCON.length() + data[1].length() + 2)))
							{
								super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
							}
							else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_RCON));
						break;
						
					case Config.CMD_SHOWMATCHES:
						if (data.length == 1)
						{
							logic.cmdMatchList(msg.user);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_SHOWMATCHES));
						break;
						
					case Config.CMD_UNREGISTER:
						if (data.length == 2)
						{
							Player player = Player.get(data[1].toLowerCase());
							if (player != null)
							{
								if (logic.cmdUnregisterPlayer(player))
								{
									super.sendMsg(msg.channel, Config.admin_cmd_successful + msg.content);
								}
								else super.sendMsg(msg.channel, Config.admin_cmd_unsuccessful + msg.content);
							}
							else sendNotice(msg.user, Config.player_not_found);
						}
						else super.sendMsg(msg.user, Config.wrong_argument_amount.replace(".cmd.", Config.USE_CMD_UNREGISTER));
						break;		
				}
			}
		}
		
		if (isPublicChannel(msg.channel) || msg.channel.type == DiscordChannelType.DM )
		{
			if (data[0].toLowerCase().equals(Config.CMD_HELP))
			{
				if (data.length == 2)
				{
					String cmd = (!data[1].startsWith("!") ? "!" : "") + data[1];
					switch (cmd)
					{
						case Config.CMD_ADD: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADD)); break;
						case Config.CMD_REMOVE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVE)); break;
						case Config.CMD_MAPS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAPS)); break;
						case Config.CMD_MAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_MAP)); break;
						case Config.CMD_STATUS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_STATUS)); break;
						case Config.CMD_HELP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_HELP)); break;
						case Config.CMD_LOCK: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LOCK)); break;
						case Config.CMD_UNLOCK: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UNLOCK)); break;
						case Config.CMD_RESET: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RESET)); break;
						case Config.CMD_GETDATA: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETDATA)); break;
						case Config.CMD_ENABLEMAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEMAP)); break;
						case Config.CMD_DISABLEMAP: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEMAP)); break;
						case Config.CMD_RCON: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_RCON)); break;
						case Config.CMD_REGISTER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REGISTER)); break;
						case Config.CMD_GETELO: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_GETELO)); break;
						case Config.CMD_TOP5: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_TOP5)); break;
						case Config.CMD_SURRENDER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SURRENDER)); break;
						//case Config.CMD_REPORT: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORT)); break;
						//case Config.CMD_EXCUSE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_EXCUSE)); break;
						//case Config.CMD_REPORTLIST: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REPORTLIST)); break;
						//case Config.CMD_ADDBAN: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDBAN)); break;
						//case Config.CMD_REMOVEBAN: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEBAN)); break;
						case Config.CMD_SHOWSERVERS: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWSERVERS)); break;
						case Config.CMD_ADDSERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDSERVER)); break;
						case Config.CMD_ENABLESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLESERVER)); break;
						case Config.CMD_DISABLESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLESERVER)); break;
						case Config.CMD_UPDATESERVER: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_UPDATESERVER)); break;
						case Config.CMD_ENABLEGAMETYPE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ENABLEGAMETYPE)); break;
						case Config.CMD_DISABLEGAMETYPE: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_DISABLEGAMETYPE)); break;
						case Config.CMD_SHOWMATCHES: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_SHOWMATCHES)); break;
						case Config.CMD_ADDGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_ADDGAMECONFIG)); break;
						case Config.CMD_REMOVEGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_REMOVEGAMECONFIG)); break;
						case Config.CMD_LISTGAMECONFIG: super.sendMsg(msg.channel, Config.help_prefix.replace(".cmd.", Config.USE_CMD_LISTGAMECONFIG)); break;
						default: super.sendMsg(msg.channel, Config.help_unknown); break;
					}
				}
				else
				{
					if (isPublicChannel(msg.channel))
					{
						super.sendMsg(msg.channel, Config.help_cmd_avi.replace(".cmds.", Config.PUB_LIST));
					}
					if (hasAdminRights(msg.user) && msg.channel.type == DiscordChannelType.DM )
					{
						super.sendMsg(msg.channel, Config.help_cmd_avi.replace(".cmds.", Config.ADMIN_LIST));
					}
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_ADDCHANNEL))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordChannel targetChannel = DiscordChannel.findChannel(data[1].replaceAll("[^\\d.]", ""));
						PickupChannelType type = PickupChannelType.valueOf(data[2].toUpperCase()); // TODO: check what exception might be thrown
						if (targetChannel != null)
						{
							if (type != null)
							{
								if (!logic.getChannelByType(type).contains(targetChannel)) 
								{
									logic.addChannel(type, targetChannel);
									sendNotice(msg.user, "successfully added the channel.");
								}
							} // TODO: write return msgs
						}
					}
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_REMOVECHANNEL))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordChannel targetChannel = DiscordChannel.findChannel(data[1].replaceAll("[^\\d.]", ""));
						PickupChannelType type = PickupChannelType.valueOf(data[2].toUpperCase()); // TODO: check what exception might be thrown
						if (targetChannel != null)
						{
							if (type != null)
							{
								if (logic.getChannelByType(type).contains(targetChannel))
								{
									logic.removeChannel(type, targetChannel);
									sendNotice(msg.user, "successfully removed the channel.");
								}
							} // TODO: write return msgs
						}
					}
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_ADDROLE))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordRole targetRole = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
						PickupRoleType type = PickupRoleType.valueOf(data[2].toUpperCase()); // TODO: check what exception might be thrown
						if (targetRole != null)
						{
							if (type != null)
							{
								if (!logic.getRoleByType(type).contains(targetRole)) 
								{
									logic.addRole(type, targetRole);
									sendNotice(msg.user, "successfully added the role.");
								}
							} // TODO: write return msgs
						}
					}
				}
			}
			else if (data[0].toLowerCase().equals(Config.CMD_REMOVEROLE))
			{
				if (hasAdminRights(msg.user))
				{
					if (data.length == 3)
					{
						DiscordRole targetRole = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
						PickupRoleType type = PickupRoleType.valueOf(data[2].toUpperCase()); // TODO: check what exception might be thrown
						if (targetRole != null)
						{
							if (type != null)
							{
								if (logic.getRoleByType(type).contains(targetRole)) 
								{
									logic.removeRole(type, targetRole);
									sendNotice(msg.user, "successfully removed the role.");
								}
							} // TODO: write return msgs
						}
					}
				}
			}
				
		}
		
		if (data[0].toLowerCase().equals("!showroles"))
		{
			DiscordUser u = msg.user;
			if (data.length == 2)
			{
				DiscordUser testUser = super.parseMention(data[1]);
				if (testUser != null)
				{
					u = testUser;
				}
			}
			List<DiscordRole> list = u.getRoles(DiscordBot.getGuild());
			String message = "";
			for (DiscordRole role : list)
			{
				message += "<@&" + role.id + "> ";
			}
			sendNotice(u, message);
		}
		
		if (data[0].toLowerCase().equals("!showknownroles"))
		{
			String message = "Roles: ";
			for (PickupRoleType type : logic.getRoleTypes()) {
				message += "\n" + type.name() + ":";

				for (DiscordRole role : logic.getRoleByType(type))
				{
					message += " <@&" + role.id + "> ";
				}
			}
			sendNotice(msg.user, message);
		}
		
		if (data[0].toLowerCase().equals("!godrole")) {
			if (logic.getRoleByType(PickupRoleType.SUPERADMIN).size() == 0) {
				if (data.length == 2) {
					DiscordRole role = DiscordRole.getRole(data[1].replaceAll("[^\\d.]", ""));
					logic.addRole(PickupRoleType.SUPERADMIN, role);
					sendNotice(msg.user, "*Channel set as SUPERADMIN channel*");
				}
			}
		}
	}
	
	public boolean isPublicChannel(DiscordChannel channel) {
		return logic.getChannelByType(PickupChannelType.PUBLIC).contains(channel);
	}
		
	public boolean hasAdminRights(DiscordUser user) {
		List<DiscordRole> roleList = user.getRoles(DiscordBot.getGuild());
		List<DiscordRole> adminList = logic.getAdminList();
		for (DiscordRole s : roleList) {
			for (DiscordRole r : adminList) {
				if (s.equals(r)) {
					return true;
				}
			}
		}
		return false;
	}


	public void sendNotice(DiscordUser user, String msg) {
		sendMsg(getLatestMessageChannel(), user.getMentionString() + " " + msg);
	}

	public void sendMsg(List<DiscordChannel> channelList, String msg) {
		for (DiscordChannel channel : channelList) {
			sendMsg(channel, msg);
		}
	}
	
	public DiscordChannel getLatestMessageChannel() {
		return latestMessageChannel;
	}
}