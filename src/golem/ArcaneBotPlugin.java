/**
 * @author Morios (Mark Talrey)
 * @version 2 for Minecraft 1.8.*
 */

package golem;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;
import java.util.Random;

public final class ArcaneBotPlugin extends JavaPlugin
{
	private boolean active = false;
	
	private String[] resp = {
		", this isn't twitter.",
		", my responses are limited.",
		"- wait, who are you?",
		", my daddy told me not to talk to strangers.",
		", you should talk to a real person #rekt"
	};
	
	private String PREF = "§D[BOT] §r<ArcaneBot> "; // formatting and name of chatbot
	private String FORMAT_ERR = "§CError: ";
	
	private String COM_GEN = "@bot";
	private String COM_HI = "@bot hi";
	private String COM_TEST = "@bot test";
	
	private Random rand;
	private MathEval calc;
	
	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equals("golem"))
		{
			if (args == null || args.length == 0 || args[0].isEmpty())
			{
				return false;
			}
			
			// console can do anything
			if (!(sender instanceof Player))
			{
				if (args[0].equals("off") || args[0].equals("on"))
				{
					doWake(sender, args);
					return true;
				}
				else if (args[0].equals("calc"))
				{
					if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("please provide an expression");
						return false;
					}
					doCalc(sender, args);
					return true;
				}
				else if (args[0].equals("order"))
				{
					if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("Please provide a command");
						return false;
					}
					sender.sendMessage("... this just runs a console command. Why run it from console...?");
					sender.sendMessage("Not that the golem won't obey, of course.");
					doOrder(sender, args);
					return true;
				}
			}
			// top-level permission active
			else if ( ((Player)sender).hasPermission("golem.*") )
			{
				if (args[0].equals("off") || args[0].equals("on"))
				{
					doWake(sender, args);
					return true;
				}
				else if (args[0].equals("calc"))
				{
					if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("please provide an expression");
						return false;
					}
					doCalc(sender, args);
					return true;
				}
				else if (args[0].equals("order"))
				{
					if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("Please provide a command");
						return false;
					}
					doOrder(sender, args);
					return true;
				}
			}
			// case-by-case mode
			else if ( ((Player)sender).hasPermission("golem.wake") &&
						(args[0].equals("off") || args[0].equals("on")) )
			{
				doWake(sender, args);
				return true;
			}
			else if ( ((Player)sender).hasPermission("golem.calc") &&
						args[0].equals("calc") )
			{
				if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("please provide an expression");
						return false;
					}
					doCalc(sender, args);
					return true;
			}
			
			else if ( ((Player)sender).hasPermission("golem.order") &&
						args[0].equals("order") )
			{
				if (args.length == 1 || args[1] == null || args[1].isEmpty())
					{
						sender.sendMessage("Please provide a command");
						return false;
					}
					doOrder(sender, args);
					return true;
			}
		}
			// gotta be a smaller | cleaner way to do the above?
			return false;
	}
	
	private void doWake(CommandSender sender, String[] args)
	{
		if (args[0].equals("off") && active)
		{
			active = false;
			sender.sendMessage("§7Arcane's chat golem is now asleep.");
		}
		else if (args[0].equals("on") && !active)
		{
			active = true;
			sender.sendMessage("§7Arcane's chat golem is now awake.");
		}
	}
	
	private void doOrder(CommandSender sender, String[] args)
	{
		if (args.length == 1) return;
		StringBuilder sb = new StringBuilder();
		for (int i=1; i<args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		getServer().dispatchCommand(getServer().getConsoleSender(), sb.toString());
	}
	
	private void doCalc(CommandSender sender, String[] args)
	{
		
		if (args.length == 1) return;
		StringBuilder sb = new StringBuilder();
		for (int i=1; i<args.length; i++)
		{
			sb.append(args[i]);
			sb.append(" ");
		}
		
		sender.sendMessage("§7ArcaneBot is solving " + sb.toString());
		double ret = calc.evaluate(sb.toString());
		sender.sendMessage("§7ArcaneBot says the answer is " + ret);
	}
	
	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(new GolemListener(), this);
		rand = new Random();
		calc = new MathEval();
	}
	
	@Override public void onDisable()
	{
		// ze goggles
	}
	
	private String getHiResponse()
	{
		int choice = (int)(rand.nextInt(5));
		return resp[choice];
	}
	
	private void chat(String msg)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			this,
			new ParamRunnable(msg),
			20L
		);
	}
	
	private class ParamRunnable implements Runnable
	{
		String param;
		ParamRunnable(String s)
		{
			param = s;
		}
		public void run()
		{
			Bukkit.broadcastMessage(PREF + param);
		}
	}
	
	public final class GolemListener implements Listener
	{
		@EventHandler
		public void detectCommand (PlayerCommandPreprocessEvent pcpe)
		{
			Player pl = pcpe.getPlayer();
			UUID pID = pl.getUniqueId();
			String msg = pcpe.getMessage();
			
			//pl.sendMessage("message: " + msg); // DEBUG
			
			if (!msg.startsWith(COM_GEN) || !pl.hasPermission("golem.chat"))
			{
				return;
			}
			
			if (msg.startsWith(COM_HI) && active)
			{
				getServer().dispatchCommand(getServer().getConsoleSender(),"a &6" + pl.getPlayerListName() + getHiResponse());
				return;
			}
			else if (msg.contains(COM_HI) && !active)
			{
				chat(pl.getPlayerListName() + ", I'm asleep, sorry.");
			}
			
			if (msg.startsWith(COM_TEST) && pl.hasPermission("golem.*"))
			{
				pl.sendMessage("message: " + msg);
			}
		}
		
		@EventHandler
		public void detectChat (PlayerChatEvent pce)
		{
			Player pl = pce.getPlayer();
			UUID pID = pl.getUniqueId();
			String msg = pce.getMessage();
			
			//pl.sendMessage("message: " + msg); // DEBUG
			
			if (!msg.startsWith(COM_GEN) || !pl.hasPermission("golem.chat"))
			{
				return;
			}
			
			if (msg.startsWith(COM_HI) && active)
			{
				chat(pl.getPlayerListName() + getHiResponse());
				return;
			}
			else if (msg.startsWith(COM_HI) && !active)
			{
				chat(pl.getPlayerListName() + ", I'm asleep, sorry.");
			}
			
			if (msg.startsWith(COM_TEST) && pl.hasPermission("golem.*"))
			{
				pl.sendMessage("message: " + msg);
			}
		}
	}
}
