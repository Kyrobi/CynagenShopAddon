package me.kyrobi.cynagenshopsearch;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import me.kyrobi.cynagenshopsearch.logging.ShopEvents;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.kyrobi.cynagenshopsearch.Util.Utils.entitiesToRemove;
import static me.kyrobi.cynagenshopsearch.Util.Utils.getAllShops;
import static me.kyrobi.cynagenshopsearch.logging.SQLHelper.bulkInsert;
import static me.kyrobi.cynagenshopsearch.logging.SQLHelper.initialize;

public final class CynagenShopSearch extends JavaPlugin {

    public static final String shopCommand = "/finditem";
    private static CynagenShopSearch instance;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;

        initialize(); // Init the logging database

        this.getCommand("finditem").setExecutor((CommandExecutor)new ShopCommand(this));
        // this.getCommand("finditem").setExecutor((CommandExecutor)new FindItemOverride());

        Bukkit.getScheduler().runTaskLater(this, ()->{
            ArrayList<Shop> shops = getAllShops();
            ArrayList<Shop> shopsToDelete = new ArrayList<>();

            List<String> uuid = new ArrayList<>();
            List<String> username = new ArrayList<>();
            List<String> item = new ArrayList<>();
            List<Double> price = new ArrayList<>();
            List<Integer> amount = new ArrayList<>();
            List<String> type = new ArrayList<>();

            for(Shop shop: shops){
                if(shop.isUnlimited()){
                    continue;
                }

                uuid.add(shop.getOwner().getUniqueId().toString());
                username.add(shop.getOwner().getUsername());
                item.add(shop.getItem().getType().name());
                price.add(Double.valueOf(shop.getPrice()));
                amount.add(Integer.valueOf(shop.getRemainingStock()));
                type.add(shop.getShopType().name());

            }

            bulkInsert(uuid, username, item, price, amount, type);
        }, 20 * 10);

        new ShopEvents(this);
        new CleanupShops(this);
        new AutoBuy(this);
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
