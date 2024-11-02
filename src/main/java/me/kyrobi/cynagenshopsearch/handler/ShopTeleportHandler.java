package me.kyrobi.cynagenshopsearch.handler;

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

import static me.kyrobi.cynagenshopsearch.CynagenShopSearch.getInstance;
import static me.kyrobi.cynagenshopsearch.Util.Utils.*;

public class ShopTeleportHandler {

    public static void onItemClick(InventoryClickEvent event){
        ItemStack item = event.getCurrentItem();
        if(item != null){
            Player player1 = (Player) event.getWhoClicked();
            Location loc = getLocationFromItem(item);

            player1.sendMessage(ChatColor.GOLD + "Teleporting...");

            Location finalLocation = findSignViewingPosition(loc);
            if(finalLocation == null){
                player1.sendMessage(ChatColor.RED + "Some shops will have issues due to bugs.");
                player1.sendMessage(ChatColor.RED + "If you see this message, ask the shop owner to");
                player1.sendMessage(ChatColor.RED + "set it up again.");
                return;
            }

            player1.closeInventory();

            World world = Bukkit.getWorld("Testworld");
            if(world != null){
                world.getChunkAt(loc).load();

                Bukkit.getScheduler().runTaskLater(getInstance(), ()->{
                    setGlowing(loc, player1);
                    Essentials ess = getEssentialsAPI();
                    ess.getUser(player1).getAsyncTeleport().teleport(
                            finalLocation,
                            new Trade(0, ess),
                            PlayerTeleportEvent.TeleportCause.COMMAND,
                            new CompletableFuture<>()
                    );

                }, 20);
            }
        }
    }
}
