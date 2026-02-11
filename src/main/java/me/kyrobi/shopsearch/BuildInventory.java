package me.kyrobi.shopsearch;

import com.earth2me.essentials.Essentials;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.kyrobi.shopsearch.CynagenShopSearch.*;
import static me.kyrobi.shopsearch.Util.Utils.*;
import static me.kyrobi.shopsearch.handler.ShopTeleportHandler.onItemClick;

public class BuildInventory {

    private CynagenShopSearch plugin;
    private Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

    private static HashMap<String, ShopMode> playerShopMode = new HashMap<>();

    private Player player;
    private ShopMode mode;
    private String itemName;
    private String metaText;

    public enum ShopMode {
        ALL, // Default. Show all items including buying and selling
        SELL, // Only shows shops where players are selling
        BUY, // Only shows shops where players are buying
        SERVICES
    }

    public BuildInventory(CynagenShopSearch plugin){
        this.plugin = plugin;
    }

    public void createInventory(Player player, ShopMode mode, String itemName, String metaText){
//        System.out.println("Mode: " + mode);
//        System.out.println("Itemname: " + itemName);
//        System.out.println("metaText: " + metaText);

        this.player = player;
        this.mode = mode;
        this.itemName = itemName;
        this.metaText = metaText;

        CompletableFuture.supplyAsync(() -> getItemsToShow(player, mode, itemName, metaText)).thenAccept(items -> {
            // Switch back to main thread for GUI creation
            Bukkit.getScheduler().runTask(plugin, () -> {
                ShowFinalGUI(player, items);
                if(!playerShopMode.containsKey(player.getName())){
                    playerShopMode.put(player.getName(), ShopMode.ALL);
                }
            });

        });
    }

    private List<ItemStack> getItemsToShow(Player player, ShopMode mode, String itemName, String metaText){
        List<ItemStack> items = new ArrayList<>();

        ShowItems items1 = new ShowItems(plugin);

        items.addAll(items1.getItems(playerShopMode.getOrDefault(player.getName(), ShopMode.ALL), itemName, metaText));

        return items;
    }

    // Don't touch this unless you need to change the layout of the GUI
    private void ShowFinalGUI(Player player, List<ItemStack> items){
        // Create the main GUI with 6 rows
        ChestGui gui = new ChestGui(6, SHOP_TITLE);

        gui.setOnGlobalClick(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
        });

        gui.setOnGlobalDrag(inventoryDragEvent -> {
            inventoryDragEvent.setCancelled(true);
        });

        // Create the paginated pane for content (5 rows)
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 4);

        // Add your items to the 'items' list here
        pages.populateWithItemStacks(items);

        // Add click handler for items
        pages.setOnClick(event -> {
            event.setCancelled(true);
            onItemClick(event);
        });


        gui.addPane(pages);

        // Create black glass background for navigation bar
        OutlinePane background = new OutlinePane(0, 4, 9, 1);

        ItemStack borderBlock = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderBlock.getItemMeta();
        borderMeta.setDisplayName(ChatColor.GRAY + "-");
        borderBlock.setItemMeta(borderMeta);
        background.addItem(new GuiItem(borderBlock, event -> event.setCancelled(true)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        gui.addPane(background);

        // Create navigation pane
        StaticPane navigation = new StaticPane(0, 5, 9, 1);

        // Previous page button
        ItemStack previousButton = new ItemStack(Material.getMaterial(PREVIOUS_PAGE_ITEM));
        ItemMeta previousMeta = previousButton.getItemMeta();
        previousMeta.setDisplayName(PREVIOUS_PAGE_TEXT);
        previousButton.setItemMeta(previousMeta);
        navigation.addItem(new GuiItem(previousButton, event -> {
            event.setCancelled(true);
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        }), 0, 0);

        // Next page button
        ItemStack nextButton = new ItemStack(Material.getMaterial(NEXT_PAGE_ITEM));
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.setDisplayName(NEXT_PAGE_TEXT);
        nextButton.setItemMeta(nextMeta);
        navigation.addItem(new GuiItem(nextButton, event -> {
            event.setCancelled(true);
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), 8, 0);

        ShopMode mode = playerShopMode.getOrDefault(player.getName(), ShopMode.ALL);

        if (mode != ShopMode.SERVICES) {
            // Guide book
            ItemStack guideBook = new ItemStack(Material.getMaterial(SHOP_GUIDE_ITEM));
            ItemMeta guideMeta = guideBook.getItemMeta();
            guideMeta.setDisplayName(SHOP_GUIDE_TEXT);
            guideMeta.setLore(getTutorialBookLore(guideMeta));
            guideBook.setItemMeta(guideMeta);
            navigation.addItem(new GuiItem(guideBook, event -> {
                event.setCancelled(true);
            }), 4, 0);
        }

        // Mode button (Comparator)
        ItemStack modeButton = new ItemStack(Material.COMPARATOR);
        ItemMeta modeMeta = modeButton.getItemMeta();
        modeMeta.setDisplayName(ChatColor.GRAY + "Player Shop Type");

        List<String> lore = new ArrayList<>();
        final String spacing = "    ";


        if(mode == ShopMode.ALL){
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "-> " + "All Items");
        } else {
            lore.add(ChatColor.GRAY + spacing + "All Items");
        }

        if(mode == ShopMode.BUY){
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "-> " + "Buying Items");
        } else {
            lore.add(ChatColor.GRAY + spacing + "Buying");
        }

        if(mode == ShopMode.SELL){
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "-> " + "Selling Items");
        } else {
            lore.add(ChatColor.GRAY + spacing + "Selling");
        }
        lore.add(ChatColor.GRAY + "   ---------");
//        if(mode == ShopMode.SERVICES){
//            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "-> " + "Job Listing");
//        } else {
//            lore.add(ChatColor.GRAY + spacing + "Job Listing");
//        }

        modeMeta.setLore(lore);
        modeButton.setItemMeta(modeMeta);

        modeButton.setItemMeta(modeMeta);
        navigation.addItem(new GuiItem(modeButton, event -> {
            event.setCancelled(true);

            if(mode == ShopMode.ALL){
                playerShopMode.put(player.getName(), ShopMode.BUY);
            }
            else if(mode == ShopMode.BUY){
                playerShopMode.put(player.getName(), ShopMode.SELL);
            }
            else if(mode == ShopMode.SELL){
                playerShopMode.put(player.getName(), ShopMode.ALL);
            }
//            else if(mode == ShopMode.SERVICES){
//                playerShopMode.put(player.getName(), ShopMode.ALL);
//            }

            createInventory(this.player, playerShopMode.get(player.getName()), this.itemName, this.metaText);

        }), 5, 0);


        gui.addPane(navigation);
        gui.show(player);
    }
}
