package me.kyrobi.shopsearch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.kyrobi.shopsearch.Util.Utils.entitiesToRemove;

public final class CynagenShopSearch extends JavaPlugin {

    public static final String shopCommand = "/finditem";
    private static CynagenShopSearch instance;

    public static String SHOP_TITLE = "";

    public static String PREVIOUS_PAGE_TEXT = "";
    public static String PREVIOUS_PAGE_ITEM = "";
    public static String NEXT_PAGE_TEXT = "";
    public static String NEXT_PAGE_ITEM = "";

    public static String SHOP_GUIDE_TEXT = "";
    public static String SHOP_GUIDE_ITEM = "";
    public static List<String> SHOP_GUIDE_INFO = new ArrayList<>();

    public static String SHOP_TYPE_GUIDE_TEXT = "";
    public static String SHOP_TYPE_GUIDE_ITEM = "";

    public static String SHOP_LOADING_MESSAGE = "";

    public static boolean SHOULD_RANDOMIZE = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;

        this.getCommand("finditem").setExecutor((CommandExecutor)new ShopCommand(this));

        /*
        Regular text
         */
        SHOP_TITLE = getConfigValueString("shop-title");

        PREVIOUS_PAGE_TEXT = getConfigValueString("previous-page-text");
        PREVIOUS_PAGE_ITEM = getConfigValueString("previous-page-item");
        NEXT_PAGE_TEXT = getConfigValueString("next-page-text");
        NEXT_PAGE_ITEM = getConfigValueString("next-page-item");

        SHOP_GUIDE_TEXT = getConfigValueString("shop-guide-text");
        SHOP_GUIDE_INFO = getConfigValueList("shop-guide-info");
        SHOP_GUIDE_ITEM = getConfigValueString("shop-guide-item");

        SHOP_TYPE_GUIDE_TEXT = getConfigValueString("shop-type-guide-text");
        SHOP_TYPE_GUIDE_ITEM = getConfigValueString("shop-type-guide-item");

        SHOULD_RANDOMIZE = this.getConfig().getBoolean("randomize-listing");

        SHOP_LOADING_MESSAGE = getConfigValueString("loading-message");
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

    private String getConfigValueString(String node){
        String value = this.getConfig().getString(node);
        if(value != null){
            return ChatColor.translateAlternateColorCodes('&', value);
        } else {
            return ChatColor.GRAY + "-";
        }
    }

    private List<String> getConfigValueList(String node){
        String value = this.getConfig().getString(node);
        if(value == null || value.isEmpty()){
            return new ArrayList<>();
        }

        List<String> rawLore = this.getConfig().getStringList(node);
        List<String> newLore = new ArrayList<>();

        for (String line : rawLore) {

            // Translate the '&' color codes to Minecraft colors
            String formattedLine = ChatColor.translateAlternateColorCodes('&', line);

            newLore.add(formattedLine);
        }

        return newLore;
    }

    public static CynagenShopSearch getInstance() {
        return instance;
    }
}
