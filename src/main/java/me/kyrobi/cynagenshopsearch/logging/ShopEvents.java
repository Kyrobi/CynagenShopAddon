package me.kyrobi.cynagenshopsearch.logging;

import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import me.kyrobi.cynagenshopsearch.CynagenShopSearch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.kyrobi.cynagenshopsearch.logging.SQLHelper.insert;

public class ShopEvents implements Listener {

    private CynagenShopSearch plugin;
    public ShopEvents(CynagenShopSearch plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent e){
        String uuid = e.getPurchaser().getUniqueId().toString();
        String username = e.getPurchaser().getUsername();
        String item = e.getShop().getItem().getType().name();
        double price = e.getShop().getPrice();
        int amount = e.getAmount();
        insert(uuid, username, item, price, amount);
    }
}
