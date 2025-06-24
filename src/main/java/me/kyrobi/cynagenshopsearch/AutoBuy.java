package me.kyrobi.cynagenshopsearch;
import com.earth2me.essentials.api.Economy;
import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.QuickShopBukkit;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.economy.AbstractEconomy;
import com.ghostchu.quickshop.api.event.economy.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.economy.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.inventory.InventoryWrapper;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Info;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopAction;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.SimpleInfo;
import com.ghostchu.quickshop.shop.inventory.BukkitInventoryWrapper;
import com.ghostchu.quickshop.util.Util;
import me.kyrobi.cynagenshopsearch.Util.FakePlayerCreator;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;

import static me.kyrobi.cynagenshopsearch.Util.Utils.*;
import static org.bukkit.Bukkit.getLogger;

public class AutoBuy implements Listener {

    private CynagenShopSearch plugin;
    private QuickShopAPI quickShopAPI;
    private ArrayList<Shop> allShops;

    private final Map<Material, Double> itemPrices = new HashMap<>();

    private final boolean enabled;
    private final int priceThresholdPercentage;
    private final int maxPlayerToBuyFrom;
    private final int maxMoneyToSpendPerPlayer;
    private final int maxAmountToBuyPerPlayer;


    public AutoBuy(CynagenShopSearch plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        quickShopAPI = getQuickshopAPI();
        allShops = getAllShops();

        this.enabled = plugin.getConfig().getBoolean("auto-buy-feature.enabled");
        this.priceThresholdPercentage = plugin.getConfig().getInt("auto-buy-feature.price_threshold_percentage");
        this.maxPlayerToBuyFrom = plugin.getConfig().getInt("auto-buy-feature.max_player_to_buy_from");
        this.maxMoneyToSpendPerPlayer = plugin.getConfig().getInt("auto-buy-feature.max_money_to_spend_per_player");
        this.maxAmountToBuyPerPlayer = plugin.getConfig().getInt("auto-buy-feature.max_amount_to_buy_per_player");

        loadPrices();

        Bukkit.getScheduler().runTask(plugin, ()->{
//            for (Map.Entry<Material, Double> entry : itemPrices.entrySet()) {
//                String itemName = String.valueOf(entry.getKey());
//                Double price = entry.getValue();
//
//                // Use the itemName and price
//                plugin.getLogger().info(itemName + " = $" + price);
//            }

            autoBuy();
        });
    }

    private void loadPrices(){
        ConfigurationSection pricesSection = plugin.getConfig().getConfigurationSection("auto-buy-feature.prices");

        if (pricesSection != null) {
            for (String key : pricesSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    List<Double> values = pricesSection.getDoubleList(key);
                    if (!values.isEmpty()) {
                        itemPrices.put(material, values.get(0)); // Use the first value
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in config: " + key);
                }
            }
        } else {
            plugin.getLogger().warning("No prices section found in config.");
        }
    }

    /*
    IDEA OVERVIEW

    The server will randomly select X amount of players that it will decide to buy from.
    Among those randomly selected, for each one, it will try to buy from it until one of these
    conditions are met (whichever is met first):
    - The server has spent all the money allocated to it for that player or
    - The server has bought the max amount of items from that player

    To prevent abuse, the server has a specified list of items along with the price
    per item.

    If the selected player is trying to sell an item that's overprice (i.e. single dirt for $300),
    it will not buy it since the price of the first is higher than the max price specified
    for that item in the config.

    In the setting, there is a config to change the % threshold is the max price. For example,
    if an elytra is being sold for $3000 by a player and the server's price list has it set to $2800,
    having the threshold at 20% will mean the server is willing to pay up to the limit of
    (2800 * 1.2) for the elytra
     */

//    private void autoBuy(){
//
//        for (Shop shop: allShops){
//            if(shop.getOwner().getUsername().equals("Kyrobi") && (shop.getRemainingStock() > 0) && (shop.isSelling())){
//
//                // Player player = Bukkit.getOfflinePlayer(UUID.fromString("164ae726-dd91-4137-8c8e-383a9ecf0713")).getPlayer();
//                Player fakePlayer = FakePlayerCreator.createFakePlayerWithInventory("Pekomart");
//                InventoryWrapper inventoryWrapper = new BukkitInventoryWrapper(fakePlayer.getInventory());
//                AbstractEconomy economy = QuickShop.getInstance().getEconomy();
//                Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, shop.getItem(), null, shop, false);
//                int amount = 10;
//
//                Object result = getQuickshopAPI().getShopManager().actionSelling(
//                        fakePlayer,
//                        inventoryWrapper,
//                        economy,
//                        info,
//                        shop,
//                        amount
//                );
//
//                System.out.println(result);
//
//
//            }
//        }
//
//    }

    private void autoBuy() {
        System.out.println("Starting autoBuy process...");

        for (Shop shop : allShops) {
            if (shop.getOwner().getUsername().equals("Kyrobi") &&
                    (shop.getRemainingStock() > 0) &&
                    (shop.isSelling())) {

                System.out.println("Processing shop: " + shop.getShopName() +
                        " | Stock: " + shop.getRemainingStock() +
                        " | Price: " + shop.getPrice());

                // Create fake player with debugging enabled
                Player fakePlayer = FakePlayerCreator.createFakePlayerWithInventory("Pekomart", true);
                InventoryWrapper inventoryWrapper = new BukkitInventoryWrapper(fakePlayer.getInventory());
                AbstractEconomy economy = QuickShop.getInstance().getEconomy();
                Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, shop.getItem(), null, shop, false);
                int amount = Math.min(10, shop.getRemainingStock()); // Don't try to buy more than available

                // Pre-check validations
                System.out.println("=== Pre-validation checks ===");

                // Check permissions
                boolean hasPermission = fakePlayer.hasPermission("quickshop.other.use");
                System.out.println("Has permission 'quickshop.other.use': " + hasPermission);

                boolean hasShopPermission = shop.playerAuthorize(fakePlayer.getUniqueId(), BuiltInShopPermission.PURCHASE);
                System.out.println("Has shop permission to purchase: " + hasShopPermission);

                // Check if shop owner is trying to buy from themselves
                boolean isSelfTrade = shop.getOwner().getUniqueId() != null &&
                        shop.getOwner().getUniqueId().equals(fakePlayer.getUniqueId());
                System.out.println("Is self trade: " + isSelfTrade);

                // Check shop status
                System.out.println("Shop frozen: " + shop.isFrozen());
                System.out.println("Shop stock: " + shop.getRemainingStock());

                // Check inventory space
                int playerSpace = Util.countSpace(inventoryWrapper, shop);
                System.out.println("Player inventory space: " + playerSpace);
                System.out.println("Trying to buy amount: " + amount);

                // Check economy balance
                double totalCost = amount * shop.getPrice();
                double playerBalance = economy.getBalance(QUserImpl.createFullFilled(fakePlayer),
                        shop.getLocation().getWorld(),
                        shop.getCurrency());
                System.out.println("Total cost: " + totalCost);
                System.out.println("Player balance: " + playerBalance);
                System.out.println("Can afford: " + (playerBalance >= totalCost));

                // If balance is insufficient, try to give the fake player money
                if (playerBalance < totalCost) {
                    System.out.println("Insufficient balance, attempting to set balance...");
                    try {
                        economy.deposit(QUserImpl.createFullFilled(fakePlayer),
                                totalCost + 1000, // Add extra buffer
                                shop.getLocation().getWorld(),
                                shop.getCurrency());
                        System.out.println("New balance: " +
                                economy.getBalance(QUserImpl.createFullFilled(fakePlayer),
                                        shop.getLocation().getWorld(),
                                        shop.getCurrency()));
                    } catch (Exception e) {
                        System.out.println("Failed to set balance: " + e.getMessage());
                    }
                }

                System.out.println("=== Attempting purchase ===");

                boolean result = getQuickshopAPI().getShopManager().actionSelling(
                        fakePlayer,
                        inventoryWrapper,
                        economy,
                        info,
                        shop,
                        amount
                );

                System.out.println("Purchase result: " + result);

                if (!result) {
                    System.out.println("Purchase failed - check the debug output above for reasons");

                    // Try with a smaller amount
                    if (amount > 1) {
                        System.out.println("Retrying with amount: 1");
                        boolean retryResult = getQuickshopAPI().getShopManager().actionSelling(
                                fakePlayer,
                                inventoryWrapper,
                                economy,
                                info,
                                shop,
                                1
                        );
                        System.out.println("Retry result: " + retryResult);
                    }
                }

                System.out.println("=== End of shop processing ===\n");
            }
        }
    }
}


/*
player-bought-from-your-store
 */