package com.wimbli.WorldBorder;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;


public class WorldBorder extends PluginBase
{
	public static volatile WorldBorder plugin = null;
	public static volatile WBCommand wbCommand = null;

	@Override
	public void onEnable()
	{
		if (plugin == null)
			plugin = this;
		if (wbCommand == null)
			wbCommand = new WBCommand("wborder");

		// Load (or create new) config file
		Config.load(this, false);

		// our one real command, though it does also have aliases "wb" and "worldborder"
		this.getServer().getCommandMap().register("wborder", wbCommand);//("wborder", wbCommand);

		// keep an eye on teleports, to redirect them to a spot inside the border if necessary
		getServer().getPluginManager().registerEvents(new WBListener(), this);


		// Well I for one find this info useful, so...
		Location spawn = getServer().getLevels().values().toArray(new Level[0])[0].getSpawnLocation().getLocation();
		Config.log("For reference, the main world's spawn location is at X: " + Config.coord.format(spawn.getX()) + " Y: " + Config.coord.format(spawn.getY()) + " Z: " + Config.coord.format(spawn.getZ()));
	}

	@Override
	public void onDisable()
	{
		Config.StopBorderTimer();
		Config.StoreFillTask();
		Config.StopFillTask();
	}

	// for other plugins to hook into
	public BorderData getWorldBorder(String worldName)
	{
		return Config.Border(worldName);
	}

	/**
	 * @deprecated  Replaced by {@link #getWorldBorder(String worldName)};
	 * this method name starts with an uppercase letter, which it shouldn't
	 */
	public BorderData GetWorldBorder(String worldName)
	{
		return getWorldBorder(worldName);
	}
}
