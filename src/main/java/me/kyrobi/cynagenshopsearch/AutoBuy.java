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
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.kyrobi.cynagenshopsearch.Util.FakePlayerCreator;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.*;

import static me.kyrobi.cynagenshopsearch.Util.Utils.*;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getResourcePack;

public class AutoBuy implements Listener, CommandExecutor {

    private CynagenShopSearch plugin;
    private QuickShopAPI quickShopAPI;
    private ArrayList<Shop> allShops;

    private final Map<Material, Double> itemPrices = new HashMap<>();

    private final boolean enabled;
    private final int priceThresholdPercentage;
    private final int maxPlayersToBuyFrom;
    private final int maxMoneyToSpendPerPlayer;
    private final int maxAmountToBuyPerPlayer;
    private final String loggingDiscordChannel;


    public AutoBuy(CynagenShopSearch plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        quickShopAPI = getQuickshopAPI();
        allShops = getAllShops();

        this.enabled = plugin.getConfig().getBoolean("auto-buy-feature.enabled");
        this.loggingDiscordChannel = plugin.getConfig().getString("auto-buy-feature.discord_logging_channel_id");
        this.priceThresholdPercentage = plugin.getConfig().getInt("auto-buy-feature.price_threshold_percentage");
        this.maxPlayersToBuyFrom = plugin.getConfig().getInt("auto-buy-feature.max_players_to_buy_from");
        this.maxMoneyToSpendPerPlayer = plugin.getConfig().getInt("auto-buy-feature.max_money_to_spend_per_player");
        this.maxAmountToBuyPerPlayer = plugin.getConfig().getInt("auto-buy-feature.max_amount_to_buy_per_player");

        loadPrices();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender){
            if(!enabled){
                sender.sendMessage("This feature is disabled!");
                return false;
            }
            autoBuy();
            return true;
        } else {
            sender.sendMessage("This command can only be used by the console.");
            return false;
        }
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

    private void autoBuy(){

        // Make a map of individual players and all their shops
        HashMap<String, ArrayList<Shop>> playerShops = new HashMap<>();
        List<String> names = new ArrayList<>();
        Random random = new Random();

        for(Shop shop: allShops){

            if(shop.getOwner().getUsername().equals("Kyrobi") && (shop.getRemainingStock() > 0) && (shop.isSelling())){
            } else {
                continue;
            }

            String ownerName = shop.getOwner().getUsername();
            if(!playerShops.containsKey(ownerName)){
                ArrayList<Shop> newShopList = new ArrayList<>();
                newShopList.add(shop);
                playerShops.put(ownerName, newShopList);
            } else {
                ArrayList<Shop> shopList = playerShops.get(ownerName);
                shopList.add(shop);
            }

            if(!names.contains(ownerName)){
                names.add(ownerName);
            }
        }

        // Shuffle the possible names
        Collections.shuffle(names);

        // Shuffle the list of items in each shop
        for (Map.Entry<String, ArrayList<Shop>> entry : playerShops.entrySet()) {
            ArrayList<Shop> shops = entry.getValue();
            Collections.shuffle(shops);
        }

        // Reduce the size to keep only the first X names
        if (names.size() > maxPlayersToBuyFrom) {
            names = names.subList(0, Math.min(names.size(), maxPlayersToBuyFrom));
        }

        for(String name: names){

            int currentItemsPurchased = 0;
            double currentMoneySpent = 0;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("```");
            stringBuilder.append("**=====" + name + "=====**\n");

            ArrayList<Shop> currentPlayerShops = playerShops.get(name);

            for(Shop shop: currentPlayerShops){
                double price = shop.getPrice();
                int stock = shop.getRemainingStock();
                ItemStack itemStack = shop.getItem();
                ItemMeta itemMeta = itemStack.getItemMeta();

                // Skip items with a lore. Going to assume they're special items, so will just not touch
                if(itemMeta != null && itemStack.getItemMeta().hasLore()){
                    continue;
                }

                Material itemType = shop.getItem().getType();
                Double recommendedPrice = itemPrices.getOrDefault(itemType, -1D);
                double willBuyPrice = recommendedPrice * (1 + (priceThresholdPercentage / 100.0));

                // This item doesn't have a price set. Skipping it
                if(recommendedPrice < 0D){
                    continue;
                }

                /*
                CONDITIONS REQUIREMENT
                - Price of the item must be equal or less than what the server will buy it fore
                - The server has not spent over it's allocated money
                - The server hasn't bought the max defined amount of items yet
                 */
                if( (price <= (willBuyPrice)) && ((currentMoneySpent) <= maxMoneyToSpendPerPlayer) && (currentItemsPurchased <= maxAmountToBuyPerPlayer) ){
                    int amountToBuy = Math.min(random.nextInt(maxAmountToBuyPerPlayer) + 1, stock);
                    int amountPossibleToBuy = 0;

                    /*
                    It's possible that the randomly generated number for amount to buy means the server
                    will spend over its budget. Just to be sure, we keep counting down until amount * price is less
                    than the total budget.
                     */
                    for(int i = amountToBuy;; i--){
                        if((amountToBuy * price) > maxAmountToBuyPerPlayer){
                            continue;
                        } else {
                            amountPossibleToBuy = amountToBuy;
                            break;
                        }
                    }
                    currentItemsPurchased += amountPossibleToBuy;
                    currentMoneySpent += price * amountPossibleToBuy;


                    buyFromShop(shop, amountPossibleToBuy);

                    stringBuilder.append("Bought " + amountPossibleToBuy + "x " + itemType + "\nTotal: $" + String.format("%.2f", (price * amountPossibleToBuy)) + " ($" + price + " each)\n\n");

                } else {
                    // Can no longer buy from this player and all of their shops. Move onto the next player
                    break;
                }

            }

            stringBuilder.append("Total spent: $" + String.format("%.2f", currentMoneySpent) + "\n");
            stringBuilder.append("Total items: " + currentItemsPurchased + "\n");
            stringBuilder.append("```");
            logInfo(stringBuilder.toString());
        }

    }

    private void buyFromShop(Shop shop, int amount){
        Player fakePlayer = FakePlayerCreator.createFakePlayerWithInventory("Pekomart");
        InventoryWrapper inventoryWrapper = new BukkitInventoryWrapper(fakePlayer.getInventory());
        AbstractEconomy economy = QuickShop.getInstance().getEconomy();
        Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, shop.getItem(), null, shop, false);

        getQuickshopAPI().getShopManager().actionSelling(
                fakePlayer,
                inventoryWrapper,
                economy,
                info,
                shop,
                amount
        );
    }

    private void logInfo(String message){
        TextChannel textChannel = DiscordUtil.getTextChannelById(loggingDiscordChannel);
        DiscordUtil.sendMessage(textChannel, message);
    }


    /*
    DEBUG
     */
//    private void autoBuy() {
//        System.out.println("Starting autoBuy process...");
//
//        for (Shop shop : allShops) {
//            if (shop.getOwner().getUsername().equals("Kyrobi") &&
//                    (shop.getRemainingStock() > 0) &&
//                    (shop.isSelling())) {
//
//                System.out.println("Processing shop: " + shop.getShopName() +
//                        " | Stock: " + shop.getRemainingStock() +
//                        " | Price: " + shop.getPrice());
//
//                // Create fake player with debugging enabled
//                Player fakePlayer = FakePlayerCreator.createFakePlayerWithInventory("Pekomart", true);
//                InventoryWrapper inventoryWrapper = new BukkitInventoryWrapper(fakePlayer.getInventory());
//                AbstractEconomy economy = QuickShop.getInstance().getEconomy();
//                Info info = new SimpleInfo(shop.getLocation(), ShopAction.PURCHASE_BUY, shop.getItem(), null, shop, false);
//                int amount = Math.min(10, shop.getRemainingStock()); // Don't try to buy more than available
//
//                // Pre-check validations
//                System.out.println("=== Pre-validation checks ===");
//
//                // Check permissions
//                boolean hasPermission = fakePlayer.hasPermission("quickshop.other.use");
//                System.out.println("Has permission 'quickshop.other.use': " + hasPermission);
//
//                boolean hasShopPermission = shop.playerAuthorize(fakePlayer.getUniqueId(), BuiltInShopPermission.PURCHASE);
//                System.out.println("Has shop permission to purchase: " + hasShopPermission);
//
//                // Check if shop owner is trying to buy from themselves
//                boolean isSelfTrade = shop.getOwner().getUniqueId() != null &&
//                        shop.getOwner().getUniqueId().equals(fakePlayer.getUniqueId());
//                System.out.println("Is self trade: " + isSelfTrade);
//
//                // Check shop status
//                System.out.println("Shop frozen: " + shop.isFrozen());
//                System.out.println("Shop stock: " + shop.getRemainingStock());
//
//                // Check inventory space
//                int playerSpace = Util.countSpace(inventoryWrapper, shop);
//                System.out.println("Player inventory space: " + playerSpace);
//                System.out.println("Trying to buy amount: " + amount);
//
//                // Check economy balance
//                double totalCost = amount * shop.getPrice();
//                double playerBalance = economy.getBalance(QUserImpl.createFullFilled(fakePlayer),
//                        shop.getLocation().getWorld(),
//                        shop.getCurrency());
//                System.out.println("Total cost: " + totalCost);
//                System.out.println("Player balance: " + playerBalance);
//                System.out.println("Can afford: " + (playerBalance >= totalCost));
//
//                // If balance is insufficient, try to give the fake player money
//                if (playerBalance < totalCost) {
//                    System.out.println("Insufficient balance, attempting to set balance...");
//                    try {
//                        economy.deposit(QUserImpl.createFullFilled(fakePlayer),
//                                totalCost + 1000, // Add extra buffer
//                                shop.getLocation().getWorld(),
//                                shop.getCurrency());
//                        System.out.println("New balance: " +
//                                economy.getBalance(QUserImpl.createFullFilled(fakePlayer),
//                                        shop.getLocation().getWorld(),
//                                        shop.getCurrency()));
//                    } catch (Exception e) {
//                        System.out.println("Failed to set balance: " + e.getMessage());
//                    }
//                }
//
//                System.out.println("=== Attempting purchase ===");
//
//                boolean result = getQuickshopAPI().getShopManager().actionSelling(
//                        fakePlayer,
//                        inventoryWrapper,
//                        economy,
//                        info,
//                        shop,
//                        amount
//                );
//
//                System.out.println("Purchase result: " + result);
//
//                if (!result) {
//                    System.out.println("Purchase failed - check the debug output above for reasons");
//
//                    // Try with a smaller amount
//                    if (amount > 1) {
//                        System.out.println("Retrying with amount: 1");
//                        boolean retryResult = getQuickshopAPI().getShopManager().actionSelling(
//                                fakePlayer,
//                                inventoryWrapper,
//                                economy,
//                                info,
//                                shop,
//                                1
//                        );
//                        System.out.println("Retry result: " + retryResult);
//                    }
//                }
//
//                System.out.println("=== End of shop processing ===\n");
//            }
//        }
//    }
}


/*
player-bought-from-your-store
 */