package com.wimbli.WorldBorder.cmd;

import java.util.List;

import com.wimbli.WorldBorder.Config;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;


public class CmdBypasslist extends WBCmd
{
	public CmdBypasslist()
	{
		name = permission = "bypasslist";
		minParams = maxParams = 0;

		addCmdExample(nameEmphasized() + "- list players with border bypass enabled.");
		helpText = "The bypass list will persist between server restarts, and applies to all worlds. Use the " +
			commandEmphasized("bypass") + C_DESC + "command to add or remove players.";
	}

	@Override
	public void execute(CommandSender sender, Player player, List<String> params, String worldName)
	{
		sender.sendMessage("Players with border bypass enabled: " + Config.getPlayerBypassList());
	}
}
