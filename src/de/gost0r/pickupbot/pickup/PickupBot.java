package de.gost0r.pickupbot.pickup;

import de.gost0r.pickupbot.discord.DiscordBot;
import de.gost0r.pickupbot.discord.DiscordChannel;
import de.gost0r.pickupbot.discord.DiscordUser;

public class PickupBot extends DiscordBot {

	private DiscordChannel pubchan;
	
	private PickupLogic logic;
	
	@Override
	public void init() {
		super.init();
		
		pubchan = DiscordChannel.findChannel("143233743107129344");
		logic = new PickupLogic(this);
	}

	@Override
	protected void recvMessage(DiscordUser user, DiscordChannel channel, String msg) {
		System.out.println("RECV #" + ((channel == null || channel.name == null) ?  "null" : channel.name) + " " + user.username + ": " + msg);
		String[] data = msg.split(" ");

		if (channel.equals(pubchan))
		{
			if (data[0].equals(Config.CMD_ADD))
			{
				if (data.length == 1)
				{
					//logic.gameAdd(user);
				}
				else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_ADD);
			}
//			else if (data[0].equals(Config.CMD_REMOVE))
//			{
//				if (data.length == 1)
//				{
//					logic.gameRemove(user);
//				}
//				else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_REMOVE);
//			}
//			else if (data[0].equals(Config.CMD_MAPS) || data[0].equals(Config.CMD_MAP))
//			{
//				if (data.length == 1)
//				{
//					logic.gameMaps();
//				}
//				else if (data.length == 2)
//				{
//					logic.gameMap(Player.get(user), data[1]);
//				}
//				else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_MAP);
//			}
//			
//			else if (data[0].equals(Config.CMD_PW))
//			{
//				if (isQauthed(nick))
//				{
//					if (data.length == 1)
//					{
//						logic.gameLostPass(nick);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_PW);
//				}
//					
//			}
//			else if (data[0].equals(Config.CMD_STATUS))
//			{
//				if (isQauthed(nick))
//				{
//					if (data.length == 1)
//					{
//						logic.gameStatus(nick);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_STATUS);
//				}
//					
//			}
//			else if (data[0].equals(Config.CMD_RESET))
//			{
//				if (isQauthed(nick))
//				{
//					if (IRCChannel.get(channel).isOpped(User.get(nick)))
//					{
//						if (data.length == 1)
//						{
//							logic.gameReset("all");
//						}
//						else if (data.length == 2)
//						{
//							logic.gameReset(data[1]);
//						}
//						else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_RESET);
//					}
//				}
//			}
//			else if (data[0].equals(Config.CMD_GETELO))
//			{
//				if (isQauthed(nick))
//				{
//					if (data.length == 1)
//					{
//						logic.gameGetElo(nick);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_GETELO);
//				}					
//			}
//			else if (data[0].equals(Config.CMD_TOP5))
//			{
//				if (isQauthed(nick))
//				{
//					if (data.length == 1)
//					{
//						logic.gameTop5();
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_TOP5);
//				}					
//			}
			else if (data[0].equals(Config.CMD_REGISTER))
			{
				if (data.length == 2)
				{
					logic.cmdRegisterPlayer(user, data[1]);
				}
				else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_REGISTER);				
			}
//			else if (data[0].equals(Config.CMD_GETELO))
//			{
//				if (isQauthed(nick))
//				{
//					if (data.length == 2)
//					{
//						logic.gameGetElo(data[1]);
//					}
//					else if (data.length == 1)
//					{
//						Player player = Player.get(User.get(nick).getQauth());
//						if (player != null)
//						{
//							logic.gameGetElo(player.getUrtauth());
//						}
//						else
//						{
//							logic.gameGetElo("#WRONGURTAUTH#");
//						}
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_GETELO);
//				}
//			}
//			
//		} 
//		if (channel.equals(adminchan))
//		{
//			if (IRCChannel.get(channel).isOpped(User.get(nick)) && isQauthed(nick))
//			{
//				if (data[0].equals(Config.CMD_LOCK))
//				{
//					if (data.length == 1)
//					{
//						logic.lock();
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_LOCK);
//					
//				}
//				else if (data[0].equals(Config.CMD_UNLOCK))
//				{
//					if (data.length == 1)
//					{
//						logic.unlock();
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_UNLOCK);
//					
//				}
//				else if (data[0].equals(Config.CMD_GETDATA))
//				{
//					if (data.length == 1)
//					{
//						logic.gameGetData();
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_GETDATA);
//					
//				}
//				else if (data[0].equals(Config.CMD_SHOWSERVERS))
//				{
//					if (data.length == 1)
//					{
//						logic.listServers();
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_SHOWSERVERS);
//					
//				}
//				else if (data[0].equals(Config.CMD_ENABLEMAP))
//				{
//					if (data.length == 2)
//					{
//						logic.enableMap(data[1]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_ENABLEMAP);
//						
//				}
//				else if (data[0].equals(Config.CMD_DISABLEMAP))
//				{
//					if (data.length == 2)
//					{
//						logic.disableMap(data[1]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_DISABLEMAP);
//					
//				}
//				else if (data[0].equals(Config.CMD_REMOVESERVER))
//				{
//					if (data.length == 2)
//					{
//						logic.removeServer(data[1]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_REMOVESERVER);
//					
//				}
//				else if (data[0].equals(Config.CMD_SETSERVER))
//				{
//					if (data.length == 3)
//					{
//						logic.changeServer(data[1], data[2]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_SETSERVER);
//						
//				}
//				else if (data[0].equals(Config.CMD_ADDSERVER))
//				{
//					if (data.length == 3)
//					{
//						logic.addServer(data[1], data[2]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_ADDSERVER);
//						
//				}
//				else if (data[0].equals(Config.CMD_SETRCON))
//				{
//					if (data.length == 3)
//					{
//						logic.changeRcon(data[1], data[2]);
//					}
//					else super.sendMsg(channel, Config.wrong_argument_amount + Config.USE_CMD_SETRCON);
//				}
//				else if (data[0].equals(Config.CMD_RCON))
//				{
//					logic.sendRcon(data[1], message.substring(Config.CMD_RCON.length() + data[1].length() + 2));
//				}
//			}
//		}
//		if (data[0].equals(Config.CMD_HELP))
//		{
//			if (data.length == 2)
//			{
//				switch (data[1])
//				{
//					case Config.CMD_ADD: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_ADD); break;
//					case Config.CMD_REMOVE: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REMOVE); break;
//					case Config.CMD_MAPS: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_MAPS); break;
//					case Config.CMD_MAP: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_MAP); break;
//					case Config.CMD_PW: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_PW); break;
//					//case Config.CMD_GAMEOVER: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_GAMEOVER); break;
//					case Config.CMD_STATUS: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_STATUS); break;
//					case Config.CMD_HELP: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_HELP); break;
//					//case Config.CMD_RING: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_RING); break;
//					case Config.CMD_LOCK: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_LOCK); break;
//					case Config.CMD_UNLOCK: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_UNLOCK); break;
//					case Config.CMD_RESET: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_RESET); break;
//					case Config.CMD_GETDATA: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_GETDATA); break;
//					case Config.CMD_ENABLEMAP: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_ENABLEMAP); break;
//					case Config.CMD_DISABLEMAP: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_DISABLEMAP); break;
//					case Config.CMD_SETSERVER: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_SETSERVER); break;
//					case Config.CMD_SETRCON: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_SETRCON); break;
//					case Config.CMD_RCON: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_RCON); break;
//					case Config.CMD_REGISTER: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REGISTER); break;
//					case Config.CMD_GETELO: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_GETELO); break;
//					case Config.CMD_TOP5: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_TOP5); break;
//					//case Config.CMD_REPORT: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REPORT); break;
//					//case Config.CMD_EXCUSE: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_EXCUSE); break;
//					//case Config.CMD_REPORTLIST: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REPORTLIST); break;
//					//case Config.CMD_ADDBAN: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_ADDBAN); break;
//					//case Config.CMD_REMOVEBAN: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REMOVEBAN); break;
//					case Config.CMD_SHOWSERVERS: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_SHOWSERVERS); break;
//					case Config.CMD_ADDSERVER: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_ADDSERVER); break;
//					case Config.CMD_REMOVESERVER: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_REMOVESERVER); break;
//					case Config.CMD_ENABLEDEBUG: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_ENABLEDEBUG); break;
//					case Config.CMD_DISABLEDEBUG: super.sendMsg(channel, Config.help_prefix + Config.USE_CMD_DISABLEDEBUG); break;
//				}
//			}
//			else
//			{
//				if (channel.equals(pubchan))
//				{
//					super.sendMsg(channel, Config.help_cmd_avi);
//					super.sendMsg(channel, Config.PUB_LIST);
//				}
//				if (channel.equals(adminchan))
//				{
//					super.sendMsg(channel, Config.help_cmd_avi);
//					super.sendMsg(channel, Config.PRV_LIST);
//				}
//			}
		}
	}


	public void sendNotice(DiscordUser user, String msg) {
		sendMsg(getPubchan(), "<@" + user.id + "> " + msg);
	}
	
	public DiscordChannel getPubchan() {
		return pubchan;
	}

	public void setPubchan(DiscordChannel pubchan) {
		this.pubchan = pubchan;
	}
}
