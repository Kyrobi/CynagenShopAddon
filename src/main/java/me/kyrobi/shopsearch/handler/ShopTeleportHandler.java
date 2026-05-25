package me.kyrobi.shopsearch.handler;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import static me.kyrobi.shopsearch.CynagenShopSearch.getInstance;
import static me.kyrobi.shopsearch.Util.Utils.*;

public class ShopTeleportHandler {

    private static final String PEKOMART_OWNER = ".Pekomart";

    public static void onItemClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        Location shopLoc = getLocationFromItem(item);
        String owner = getUsernameFromItem(item);

        if (shopLoc == null) {
            player.sendMessage(ChatColor.RED + "Could not determine shop location.");
            return;
        }

        Location finalLocation = findSignViewingPosition(shopLoc);
        if (finalLocation == null) {
            player.sendMessage(ChatColor.RED + "Some shops will have issues due to bugs.");
            player.sendMessage(ChatColor.RED + "If you see this message, ask the shop owner to");
            player.sendMessage(ChatColor.RED + "set it up again.");
            return;
        }

        World world = finalLocation.getWorld();
        if (world == null) return;

        player.sendMessage(ChatColor.GOLD + "Teleporting...");
        player.closeInventory();

        boolean isPekomart = PEKOMART_OWNER.equals(owner);

        // KEY FIX: resolve the plot from the shop's *location*, not the owner's name.
        // This handles plot members (e.g. John building in Kyrobi's plot) correctly
        // because we no longer care who created the shop — only where it sits.
        String plotId = isPekomart ? null : findPlotIdAtLocation(shopLoc);

        world.getChunkAtAsync(finalLocation).thenAccept(chunk -> {
            if (!player.isOnline()) return;

            Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                if (!player.isOnline()) return;

                if (isPekomart) {
                    // Pekomart lives outside any plot — go straight to the chest.
                    teleportDirect(player, finalLocation, shopLoc);
                } else if (plotId != null) {
                    // /plot <plotId> already finds the lodestone for that region.
                    player.performCommand("plot " + plotId);
                    Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                        if (player.isOnline()) setGlowing(shopLoc, player);
                    }, 10);
                } else {
                    // Shop isn't in any known plot region and isn't Pekomart.
                    // Fall back to a direct teleport so the shop is still reachable.
                    teleportDirect(player, finalLocation, shopLoc);
                }
            }, 20);
        });
    }

    /** Sync teleport — the destination chunk is already loaded by the caller. */
    private static void teleportDirect(Player player, Location dest, Location shopLoc) {
        boolean ok = player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN);
        if (ok && player.isOnline()) {
            setGlowing(shopLoc, player);
        } else if (!ok) {
            player.sendMessage(ChatColor.RED + "Teleportation failed. Please try again.");
        }
    }

    /** Returns the ID of the plot region containing this location, or null. */
    private static String findPlotIdAtLocation(Location loc) {
        World bukkitWorld = loc.getWorld();
        if (bukkitWorld == null) return null;

        com.sk89q.worldedit.world.World weWorld = WorldGuard.getInstance()
                .getPlatform().getMatcher().getWorldByName(bukkitWorld.getName());
        if (weWorld == null) return null;

        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(weWorld);
        if (rm == null) return null;

        BlockVector3 pos = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        for (ProtectedRegion region : rm.getApplicableRegions(pos)) {
            String id = region.getId();
            if (id.matches("^[+-][a-zA-Z]\\d$") || id.matches("^(sushi|ramen)\\d$")) {
                return id;
            }
        }
        return null;
    }
}