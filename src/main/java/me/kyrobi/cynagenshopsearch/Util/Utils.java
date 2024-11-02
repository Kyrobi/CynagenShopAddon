package me.kyrobi.cynagenshopsearch.Util;

import com.earth2me.essentials.Essentials;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.kyrobi.cynagenshopsearch.CynagenShopSearch.getInstance;

public class Utils {

    public static ArrayList<UUID> entitiesToRemove = new ArrayList<>();
    private static final int SAFE_RADIUS = 10;

    public static ItemStack addLoreToShopItem(Shop shop){
        // IMPORTANT
        // OR ELSE IT WILL BREAK ALL THE SHOPS
        ItemStack originalItem = shop.getItem();
        ItemStack item = new ItemStack(originalItem);

        double price = shop.getPrice();
        int stock = shop.getRemainingStock();
        ShopType type = shop.getShopType();
        String owner = shop.getOwner().getUsername();
        Location loc = shop.getLocation();

        String teleportMessage = ChatColor.GOLD + "Click to teleport to the shop!";

        ItemMeta itemMeta = item.getItemMeta();

        ArrayList<String> lore = new ArrayList<>();

        if(itemMeta.hasLore()){
            lore.addAll(itemMeta.getLore()); // Add existing lore
        }

        lore.add(ChatColor.GRAY + "------------");
        lore.add(ChatColor.GRAY + "Price: " + ChatColor.GREEN + "$" + price);

        if(stock <= -1){
            if(type == ShopType.BUYING){
                lore.add(ChatColor.LIGHT_PURPLE + "Wanting: " + ChatColor.WHITE + shop.getRemainingSpace());
            } else {
                lore.add(ChatColor.GRAY + "Stock: " + ChatColor.WHITE + "Unlimited");
            }
        } else {
            lore.add(ChatColor.GRAY + "Stock: " + ChatColor.WHITE + stock);
        }

        if(type == ShopType.BUYING){
            lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + "Buying");
        } else {
            lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + "Selling");
        }

        if(shop.getOwner().getUniqueId().toString().equals("164ae726-dd91-4137-8c8e-383a9ecf0713")){
            lore.add(ChatColor.GRAY + "Owner: " + ChatColor.GOLD + "Pekomart 🐰");
        } else {
            lore.add(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + owner);
        }

        lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
        lore.add(" ");
        lore.add(teleportMessage);

        itemMeta.setLore(lore);




        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        // Use NamespacedKeys to store each part of the Location
        container.set(new NamespacedKey(getInstance(), "location_world"), PersistentDataType.STRING, loc.getWorld().getName());
        container.set(new NamespacedKey(getInstance(), "location_x"), PersistentDataType.DOUBLE, loc.getX());
        container.set(new NamespacedKey(getInstance(), "location_y"), PersistentDataType.DOUBLE, loc.getY());
        container.set(new NamespacedKey(getInstance(), "location_z"), PersistentDataType.DOUBLE, loc.getZ());
        container.set(new NamespacedKey(getInstance(), "location_yaw"), PersistentDataType.FLOAT, loc.getYaw());
        container.set(new NamespacedKey(getInstance(), "location_pitch"), PersistentDataType.FLOAT, loc.getPitch());

        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack addDescriptionToService(Shop shop, String desc){
        // IMPORTANT
        // OR ELSE IT WILL BREAK ALL THE SHOPS
        ItemStack originalItem = shop.getItem();
        ItemStack item = new ItemStack(originalItem);

        double price = shop.getPrice();
        int stock = shop.getRemainingStock();
        ShopType type = shop.getShopType();
        String owner = shop.getOwner().getUsername();
        Location loc = shop.getLocation();

        String teleportMessage = ChatColor.GOLD + "Click to teleport to the shop!";

        ItemMeta itemMeta = item.getItemMeta();

        Essentials ess = getEssentialsAPI();

        long playTime = Bukkit.getOfflinePlayer(shop.getOwner().getUniqueId()).getStatistic(Statistic.PLAY_ONE_MINUTE)/20;

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Description");
        lore.add(ChatColor.GRAY + "------------");

        lore.add(ChatColor.GRAY + "");

        // lore.add(ChatColor.DARK_AQUA + desc);
        int lineLength = 36;
        for (int i = 0; i < desc.length(); i += lineLength) {
            // Check if the remaining part is less than line length
            if (i + lineLength < desc.length()) {
                lore.add(ChatColor.DARK_AQUA + desc.substring(i, i + lineLength));
            } else {
                // Add the last part of the string
                lore.add(ChatColor.DARK_AQUA + desc.substring(i));
            }
        }

        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.GRAY + "------------");

        long lastOnline = ess.getUser(shop.getOwner().getUniqueId()).getLastOnlineActivity();
        lore.add(ChatColor.GRAY + "Name: " + ChatColor.WHITE + owner);
        lore.add(ChatColor.GRAY + "Last online: " + ChatColor.WHITE + convertMilToHowLongAgo(lastOnline));
        lore.add(ChatColor.GRAY + "Total playtime: " + ChatColor.WHITE + secondsToTimestamp(playTime));


        lore.add(ChatColor.GRAY + "Location: " + ChatColor.WHITE + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
        lore.add(" ");
        lore.add(teleportMessage);

        itemMeta.setLore(lore);
        itemMeta.setDisplayName(ChatColor.GOLD + "Job Listing");



        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        // Use NamespacedKeys to store each part of the Location
        container.set(new NamespacedKey(getInstance(), "location_world"), PersistentDataType.STRING, loc.getWorld().getName());
        container.set(new NamespacedKey(getInstance(), "location_x"), PersistentDataType.DOUBLE, loc.getX());
        container.set(new NamespacedKey(getInstance(), "location_y"), PersistentDataType.DOUBLE, loc.getY());
        container.set(new NamespacedKey(getInstance(), "location_z"), PersistentDataType.DOUBLE, loc.getZ());
        container.set(new NamespacedKey(getInstance(), "location_yaw"), PersistentDataType.FLOAT, loc.getYaw());
        container.set(new NamespacedKey(getInstance(), "location_pitch"), PersistentDataType.FLOAT, loc.getPitch());

        item.setItemMeta(itemMeta);

        return item;
    }

    public static Location getLocationFromItem(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();

            // Retrieve each part of the Location
            String worldName = container.get(new NamespacedKey(getInstance(), "location_world"), PersistentDataType.STRING);
            Double x = container.get(new NamespacedKey(getInstance(), "location_x"), PersistentDataType.DOUBLE);
            Double y = container.get(new NamespacedKey(getInstance(), "location_y"), PersistentDataType.DOUBLE);
            Double z = container.get(new NamespacedKey(getInstance(), "location_z"), PersistentDataType.DOUBLE);
            Float yaw = container.get(new NamespacedKey(getInstance(), "location_yaw"), PersistentDataType.FLOAT);
            Float pitch = container.get(new NamespacedKey(getInstance(), "location_pitch"), PersistentDataType.FLOAT);

            if (worldName != null && x != null && y != null && z != null && yaw != null && pitch != null) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
        }
        return null; // Return null if data is incomplete or invalid
    }

    public static boolean doesItemContainStringInMeta(ItemStack item, String text){

        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta.hasLore()){
            for(String s: itemMeta.getLore()){
                if(s.toLowerCase().contains(text.toLowerCase())){
                    return true;
                }
            }
        }

        if(itemMeta.getAsString().contains(text.toLowerCase())){
            return true;
        }

        // System.out.println("Display name: " + itemMeta.getDisplayName());
        String itemName = itemMeta.getDisplayName();
        if(itemName.toLowerCase().contains(text.toLowerCase())){
            return true;
        }

        return false;
    }

    public static ArrayList<Shop> getAllShops() {
        QuickShopAPI api = QuickShopAPI.getInstance();

        ArrayList<Shop> allShops = new ArrayList<>(api.getShopManager().getAllShops());

        return allShops;
    }

    public static Location findSignViewingPosition(Location chestLocation) {
        Block chestBlock = chestLocation.getBlock();
//        if (!(chestBlock.getState() instanceof Chest) && !(chestBlock.getState() instanceof Barrel)) {
//            return null;
//        }

        // Check all faces of the chest for a sign
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        BlockFace signFace = null;

        for (BlockFace face : faces) {
            Block relativeBlock = chestBlock.getRelative(face);
            if (relativeBlock.getType().toString().contains("SIGN") ||
                    relativeBlock.getType().toString().contains("WALL_SIGN")) {
                signFace = face;
                break;
            }
        }

        if (signFace == null) {
            return null; // No sign found
        }

        // Get the block in front of the sign for player positioning
        Block signBlock = chestBlock.getRelative(signFace);
        Block playerBlock = signBlock.getRelative(signFace);

        // Create a location for the player to stand
        Location playerLoc = playerBlock.getLocation().add(0.5, 0, 0.5);

        // Set the yaw to face the sign (corrected values)
        switch (signFace) {
            case NORTH:
                playerLoc.setYaw(0);    // Was 180
                break;
            case SOUTH:
                playerLoc.setYaw(180);  // Was 0
                break;
            case EAST:
                playerLoc.setYaw(90);   // Was 270
                break;
            case WEST:
                playerLoc.setYaw(270);  // Was 90
                break;
        }

        // Set a neutral pitch to look at the sign
        playerLoc.setPitch(0);

        return playerLoc;
    }

    public static List<String> getTutorialBookLore(ItemMeta meta){
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.AQUA + "/shop");
        lore.add(ChatColor.WHITE + "This will open the shop menu");
        lore.add(ChatColor.WHITE + "and show all items.");

        lore.add("");

        lore.add(ChatColor.AQUA + "/shop ENCHANTED_BOOK");
        lore.add(ChatColor.WHITE + "This will find shops with");
        lore.add(ChatColor.WHITE + "enchanted books.");

        lore.add("");

        lore.add(ChatColor.AQUA + "/shop ENCHANTED_BOOK fortune");
        lore.add(ChatColor.WHITE + "This will find shops with");
        lore.add(ChatColor.WHITE + "enchanted books that contains");
        lore.add(ChatColor.WHITE + "the text \"fortune\".");

        lore.add("");

        lore.add(ChatColor.AQUA + "/shop voting item");
        lore.add(ChatColor.WHITE + "This will find all items containing");
        lore.add(ChatColor.WHITE + "the word \"voting item\".");

        return lore;

    }

    public static List<String> getServiceLore(ItemMeta meta){
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.AQUA + "You can offer/request different");
        lore.add(ChatColor.AQUA + "services like building, gathering,");
        lore.add(ChatColor.AQUA + "terraforming, etc.");

        lore.add("");

        lore.add(ChatColor.AQUA + "To do this, simply set up a shop with");
        lore.add(ChatColor.AQUA + "a paper that's renamed. Name the paper ");
        lore.add(ChatColor.WHITE + "[Service] <description>");
        lore.add(ChatColor.AQUA + "For example:");

        lore.add("");

        lore.add(ChatColor.WHITE + "[Service] I will help you gather all the");
        lore.add(ChatColor.WHITE + "different types of flowers.");

        lore.add("");

        lore.add(ChatColor.GRAY + "Note: You will need to set up your own");
        lore.add(ChatColor.GRAY + "system for how players will contact");
        lore.add(ChatColor.GRAY + "you. For example, buying an item and");
        lore.add(ChatColor.GRAY + "putting it into a hopper, /mail, contact");
        lore.add(ChatColor.GRAY + "on Discord, etc");

        lore.add("");

        lore.add(ChatColor.GRAY + "Use /itemname if text is too long for anvil.");
        lore.add(ChatColor.GRAY + "This list is sorted by last online.");

        return lore;
    }

    public static Essentials getEssentialsAPI(){
        return (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    }

    public static String convertMilToHowLongAgo(long timems){
        // Get the current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Calculate the difference (time elapsed since last online)
        long elapsedMillis = currentTimeMillis - timems;

        // Convert elapsed time into days, hours, and minutes
        long days = TimeUnit.MILLISECONDS.toDays(elapsedMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60;

        // Output the result
        return(days + " days, " + hours + " hours");
    }

    public static String secondsToTimestamp(long timeSeconds) {
        // Convert seconds to milliseconds
        long timems = timeSeconds * 1000;

        // Convert milliseconds into months, days, and hours
        long months = TimeUnit.MILLISECONDS.toDays(timems) / 30;
        long days = TimeUnit.MILLISECONDS.toDays(timems) % 30;
        long hours = TimeUnit.MILLISECONDS.toHours(timems) % 24;

        // Return the result as a string
        return months + " months, " + days + " days";
    }

    public static void setGlowing(Location location,  Player player) {
        World world = location.getWorld();

        // Adjust the location to the center of the block
        Location spawnLocation = location.clone().add(0.5, 0.25, 0.5);

        Shulker shulker = (Shulker) world.spawnEntity(spawnLocation, EntityType.SHULKER);
        // shulker.setSize(2); // Set the slime size (2 for a larger slime)
        shulker.setGravity(false);
        shulker.setAI(false);
        shulker.setCollidable(false);
        // shulker.setSize(1);
        shulker.setInvisible(true);
        shulker.setInvulnerable(true);
        shulker.setVisibleByDefault(false);
        shulker.setGlowing(true);
        shulker.setPersistent(false);
        // slime.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, lifetimeTicks, 1, false, false));

        player.showEntity(getInstance(), shulker);

        String uuidOfshulker = shulker.getUniqueId().toString();
        entitiesToRemove.add(shulker.getUniqueId()); // Just to make sure we can keep track of shulkers to remove on restart



        // Schedule a task to remove the shulker after the specified lifetime
        Bukkit.getScheduler().runTaskLater(getInstance(), () -> {
            entitiesToRemove.remove(shulker.getUniqueId());
            shulker.remove();
        }, 20 * 20);

    }
}
