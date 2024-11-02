package me.kyrobi.cynagenshopsearch;

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

import static me.kyrobi.cynagenshopsearch.Util.Utils.entitiesToRemove;

public final class CynagenShopSearch extends JavaPlugin {

    public static final String shopCommand = "shop";
    private static CynagenShopSearch instance;

    @Override
    public void onEnable() {
        instance = this;

        this.getCommand(shopCommand).setExecutor((CommandExecutor)new ShopCommand(this));
        this.getCommand("finditem").setExecutor((CommandExecutor)new FindItemOverride());
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks((Plugin)this);
        HandlerList.unregisterAll((Plugin)this);

        for(UUID uuid: entitiesToRemove){
            for (World world : Bukkit.getWorlds()) {
                Entity entity = world.getEntity(uuid);
                if (entity != null) {
                    entity.remove();
                    break;
                }
            }
        }
    }

    public static CynagenShopSearch getInstance() {
        return instance;
    }
}
