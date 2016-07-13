package com.wimbli.WorldBorder;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;


public class BorderCheckTask implements Runnable
{
	@Override
	public void run()
	{
		// if knockback is set to 0, simply return
		if (Config.KnockBack() == 0.0)
			return;

		Player[] players = Server.getInstance().getOnlinePlayers().values().toArray(new Player[0]);

		for (int i = 0; i < players.length; i++)
		{
			checkPlayer(players[i], null, false, true);
		}
	}

	// track players who are being handled (moved back inside the border) already; needed since Bukkit is sometimes sending teleport events with the old (now incorrect) location still indicated, which can lead to a loop when we then teleport them thinking they're outside the border, triggering event again, etc.
	private static Set<String> handlingPlayers = Collections.synchronizedSet(new LinkedHashSet<String>());

	// set targetLoc only if not current player location; set returnLocationOnly to true to have new Location returned if they need to be moved to one, instead of directly handling it
	public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly, boolean notify)
	{
		if (player == null || !player.isOnline()) return null;

		Location loc = (targetLoc == null) ? player.getLocation().clone() : targetLoc;
		if (loc == null) return null;

		Level world = loc.getLevel();
		if (world == null) return null;
		BorderData border = Config.Border(world.getName());
		if (border == null) return null;

		if (border.insideBorder(loc.getX(), loc.getZ(), Config.ShapeRound()))
			return null;

		// if player is in bypass list (from bypass command), allow them beyond border; also ignore players currently being handled already
		if (Config.isPlayerBypassing(player.getName()) || handlingPlayers.contains(player.getName().toLowerCase()))
			return null;

		// tag this player as being handled so we can't get stuck in a loop due to Bukkit currently sometimes repeatedly providing incorrect location through teleport event
		handlingPlayers.add(player.getName().toLowerCase());

		Location newLoc = newLocation(player, loc, border, notify);
		boolean handlingVehicle = false;

		/*
		 * since we need to forcibly eject players who are inside vehicles, that fires a teleport event (go figure) and
		 * so would effectively double trigger for us, so we need to handle it here to prevent sending two messages and
		 * two log entries etc.
		 * after players are ejected we can wait a few ticks (long enough for their client to receive new entity location)
		 * and then set them as passenger of the vehicle again
		 */
		if (player.riding != null)
		{
			//TODO eject players and move
			System.out.println("Look into ejecting players ~~~ Remove this");
//			Entity ride = player.riding;
//			ride.rider =null;
//			player.leaveVehicle();
//			if (ride != null)
//			{	// vehicles need to be offset vertically and have velocity stopped
//				double vertOffset = (ride instanceof LivingEntity) ? 0 : ride.getLocation().getY() - loc.getY();
//				Location rideLoc = newLoc.clone();
//				rideLoc.setY(newLoc.getY() + vertOffset);
//				if (Config.Debug())
//					Config.logWarn("Player was riding a \"" + ride.toString() + "\".");
//				if (ride instanceof Boat)
//				{	// boats currently glitch on client when teleported, so crappy workaround is to remove it and spawn a new one
//					ride.remove();
//					ride = world.spawnEntity(rideLoc, EntityType.BOAT);
//				}
//				else
//				{
//					ride.setVelocity(new Vector(0, 0, 0));
//					ride.teleport(rideLoc, TeleportCause.PLUGIN);
//				}
//
//				if (Config.RemountTicks() > 0)
//				{
//					setPassengerDelayed(ride, player, player.getName(), Config.RemountTicks());
//					handlingVehicle = true;
//				}
//			}
		}

		// check if player has something (a pet, maybe?) riding them; only possible through odd plugins.
		// it can prevent all teleportation of the player completely, so it's very much not good and needs handling
		if (player.rider != null)
		{
			//TODO fix players being ridden
			System.out.println("player is being ridden ~~ remove this");
//			Entity rider = player.getPassenger();
//			player.eject();
//			rider.teleport(newLoc, TeleportCause.PLUGIN);
//			player.sendMessage("Your passenger has been ejected.");
//			if (Config.Debug())
//				Config.logWarn("Player had a passenger riding on them: " + rider.getType());
		}


		// give some particle and sound effects where the player was beyond the border, if "whoosh effect" is enabled
		Config.showWhooshEffect(loc);

		if (!returnLocationOnly)
			player.teleport(newLoc, TeleportCause.PLUGIN);

		if (!handlingVehicle)
			handlingPlayers.remove(player.getName().toLowerCase());

		if (returnLocationOnly)
			return newLoc;

		return null;
	}
	public static Location checkPlayer(Player player, Location targetLoc, boolean returnLocationOnly)
	{
		return checkPlayer(player, targetLoc, returnLocationOnly, true);
	}

	private static Location newLocation(Player player, Location loc, BorderData border, boolean notify)
	{
		if (Config.Debug())
		{
			Config.logWarn((notify ? "Border crossing" : "Check was run") + " in \"" + loc.getLevel().getName() + "\". Border " + border.toString());
			Config.logWarn("Player position X: " + Config.coord.format(loc.getX()) + " Y: " + Config.coord.format(loc.getY()) + " Z: " + Config.coord.format(loc.getZ()));
		}

		Location newLoc = border.correctedPosition(loc, Config.ShapeRound(), player.isOnGround());//TODO check if player is flying not on ground

		// it's remotely possible (such as in the Nether) a suitable location isn't available, in which case...
		if (newLoc == null)
		{
			if (Config.Debug())
				Config.logWarn("Target new location unviable, using spawn or killing player.");
			if (Config.getIfPlayerKill())
			{
				player.setHealth(0.0F);
				return null;
			}
			newLoc = player.getLevel().getSpawnLocation().getLocation();
		}

		if (Config.Debug())
			Config.logWarn("New position in world \"" + newLoc.getLevel().getName() + "\" at X: " + Config.coord.format(newLoc.getX()) + " Y: " + Config.coord.format(newLoc.getY()) + " Z: " + Config.coord.format(newLoc.getZ()));

		if (notify)
			player.sendMessage(Config.Message());

		return newLoc;
	}

	private static void setPassengerDelayed(final Entity vehicle, final Player player, final String playerName, long delay)
	{
		System.out.println("setPassengerDelayed but the passenger can't be set");
//		Server.getInstance().getScheduler().scheduleSyncDelayedTask(WorldBorder.plugin, new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				handlingPlayers.remove(playerName.toLowerCase());
//				if (vehicle == null || player == null)
//					return;
//
//				vehicle.setPassenger(player);
//			}
//		}, delay);
	}
}
