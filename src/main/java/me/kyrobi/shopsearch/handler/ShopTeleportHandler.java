package me.kyrobi.shopsearch.handler;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Trade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

import static me.kyrobi.shopsearch.CynagenShopSearch.getInstance;
import static me.kyrobi.shopsearch.Util.Utils.*;

public class ShopTeleportHandler {

    public static void onItemClick(InventoryClickEvent event){
        if(event.getCurrentItem() == null){
            return;
        }

        ItemStack item = event.getCurrentItem();
        if(item != null){
            Player player1 = (Player) event.getWhoClicked();
            Location loc = getLocationFromItem(item);
            String owner = getUsernameFromItem(item);

            // FIX: Null check on loc before proceeding
            if(loc == null){
                player1.sendMessage(ChatColor.RED + "Could not determine shop location.");
                return;
            }

            player1.sendMessage(ChatColor.GOLD + "Teleporting...");

            Location finalLocation = findSignViewingPosition(loc);
            if(finalLocation == null){
                player1.sendMessage(ChatColor.RED + "Some shops will have issues due to bugs.");
                player1.sendMessage(ChatColor.RED + "If you see this message, ask the shop owner to");
                player1.sendMessage(ChatColor.RED + "set it up again.");
                return;
            }

            player1.closeInventory();

            World world = finalLocation.getWorld();
            if(world != null){
                /*
                FIX: Use async chunk loading instead of synchronous getChunkAt().load()
                which can cause main thread lag spikes when loading chunks from disk.
                 */
                world.getChunkAtAsync(finalLocation).thenAccept(chunk -> {
                    // This callback runs on the main thread once the chunk is loaded
                    if(!player1.isOnline()) return; // Player may have left during chunk load

                    Bukkit.getScheduler().runTaskLater(getInstance(), ()->{
                        if(!player1.isOnline()) return;

                        Essentials ess = getEssentialsAPI();


                        CompletableFuture<Boolean> teleportFuture = new CompletableFuture<>();
                        if(owner.equals(".Pekomart")){
                            ess.getUser(player1).getAsyncTeleport().teleport(
                                    finalLocation,
                                    new Trade(0, ess),
                                    PlayerTeleportEvent.TeleportCause.COMMAND,
                                    teleportFuture
                            );
                            teleportFuture.thenAccept(success -> {
                                if(success != null && success && player1.isOnline()){
                                    Bukkit.getScheduler().runTask(getInstance(), () -> setGlowing(loc, player1));
                                }
                            });
                        } else {
                            player1.performCommand("plot " + owner);
                            // /plot teleport is sync; give the client a tick to catch up
                            Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
                                if(player1.isOnline()) setGlowing(loc, player1);
                            }, 10);
                        }
                        teleportFuture.exceptionally(ex -> {
                            player1.sendMessage(ChatColor.RED + "Teleportation failed. Please try again.");
                            ex.printStackTrace();
                            return false;
                        });

                    }, 20);
                });
            }
        }
    }
}