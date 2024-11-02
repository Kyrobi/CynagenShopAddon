package me.kyrobi.cynagenshopsearch;

import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.kyrobi.cynagenshopsearch.CynagenShopSearch.getInstance;
import static me.kyrobi.cynagenshopsearch.Util.Utils.*;

public class ShowItems {


    public ShowItems(CynagenShopSearch plugin){

    }

    public List<ItemStack> getItems(BuildInventory.ShopMode mode, String itemName, String metaText){
        List<ItemStack> items = new ArrayList<>();

        List<Shop> copyOfShops = new ArrayList<>(getAllShops());



        // Put all the service listings
        if(mode == BuildInventory.ShopMode.SERVICES){
            List<Shop> servicesShop = new ArrayList<>();
            for(Shop shop: copyOfShops){
                ItemStack item = shop.getItem();
                String name = item.getItemMeta().getDisplayName();
                if(name.startsWith("[Service]")){
                    servicesShop.add(shop);
                }
            }

            for(Shop shop: servicesShop){
                String name = shop.getItem().getItemMeta().getDisplayName();
                String description = name.replaceFirst("\\[Service\\]\\s*", "");
                ItemStack itemEdited = addDescriptionToService(shop, description);
                items.add(itemEdited);
            }

            servicesShop.sort(Comparator.comparingLong(shop -> {
                Player player = (Player) Bukkit.getOfflinePlayer(shop.getOwner().getUniqueId());
                if (player != null) {
                    // Use PLAY_ONE_MINUTE statistic as last_online
                    return player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                } else {
                    // Return a high value or a default if player is null
                    return Long.MAX_VALUE;
                }
            }));

            return items;
        }


        /*
        Clean up the list before processing it later
         */

        for (int i = copyOfShops.size() - 1; i >= 0; i--) {
            Shop shop = copyOfShops.get(i);
            if (shop.getRemainingStock() == 0) {
                // System.out.println("Removing " + shop.getItem());
                copyOfShops.remove(i);
            }
            if (shop.getRemainingSpace() == 0) {
                // System.out.println("Removing " + shop.getItem());
                copyOfShops.remove(i);
            }
        }

        // Sort everything by price
        copyOfShops.sort(Comparator.comparingDouble(Shop::getPrice)); // ascending order

        // If there is no item name, only
        // match via meta string
        if(itemName.isEmpty() && !metaText.isEmpty()){
            for(Shop shop: copyOfShops){


                if(mode == BuildInventory.ShopMode.BUY){
                    if(shop.getShopType() == ShopType.BUYING){
                        boolean containsMeta = doesItemContainStringInMeta(shop.getItem(), metaText);
                        if(containsMeta){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);
                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.SELL){
                    if(shop.getShopType() == ShopType.SELLING){
                        boolean containsMeta = doesItemContainStringInMeta(shop.getItem(), metaText);
                        if(containsMeta){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);
                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.ALL){
                    boolean containsMeta = doesItemContainStringInMeta(shop.getItem(), metaText);
                    if(containsMeta){
                        ItemStack item = addLoreToShopItem(shop);
                        items.add(item);
                    }
                }
            }
        }

        if(!itemName.isEmpty() && metaText.isEmpty()){
            for(Shop shop: copyOfShops){
                if(mode == BuildInventory.ShopMode.BUY){
                    if(shop.getShopType() == ShopType.BUYING){
                        if(shop.getItem().getType() == Material.getMaterial(itemName)){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);
                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.SELL){
                    if(shop.getShopType() == ShopType.SELLING){
                        if(shop.getItem().getType() == Material.getMaterial(itemName)){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);
                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.ALL){
                    if(shop.getItem().getType() == Material.getMaterial(itemName)){
                        ItemStack item = addLoreToShopItem(shop);
                        items.add(item);
                    }
                }
            }
        }

        // Specific item AND with a specific meta
        if(!itemName.isEmpty() && !metaText.isEmpty()){
            for(Shop shop: copyOfShops){
                if(mode == BuildInventory.ShopMode.SELL){
                    if(shop.getShopType() == ShopType.SELLING){
                        if(shop.getItem().getType() == Material.getMaterial(itemName) && doesItemContainStringInMeta(shop.getItem(), metaText)){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);

                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.BUY){
                    if(shop.getShopType() == ShopType.BUYING){
                        if(shop.getItem().getType() == Material.getMaterial(itemName) && doesItemContainStringInMeta(shop.getItem(), metaText)){
                            ItemStack item = addLoreToShopItem(shop);
                            items.add(item);

                        }
                    }
                }
                else if(mode == BuildInventory.ShopMode.ALL){
                    if(shop.getItem().getType() == Material.getMaterial(itemName) && doesItemContainStringInMeta(shop.getItem(), metaText)){
                        ItemStack item = addLoreToShopItem(shop);
                        items.add(item);

                    }
                }
            }
        }

        if(itemName.isEmpty() && metaText.isEmpty()){

            // If buy mode, we want to show the most profitable items
            if(mode == BuildInventory.ShopMode.BUY){
                copyOfShops.sort(Comparator.comparingDouble(Shop::getPrice).reversed()); // desc order
            } else {
                /*
                Since we don't care about the price for this mode, we sort it by the time the item
                was added onto the market
                 */
                copyOfShops.sort(Comparator.comparingLong(Shop::getShopId).reversed()); //.reverse if you want descending order
            }

            for(Shop shop: copyOfShops){
                if(mode == BuildInventory.ShopMode.SELL){
                    if(shop.getShopType() == ShopType.SELLING){
                        ItemStack item = addLoreToShopItem(shop);
                        items.add(item);
                    }
                }
                else if(mode == BuildInventory.ShopMode.BUY){
                    if(shop.getShopType() == ShopType.BUYING){
                        ItemStack item = addLoreToShopItem(shop);
                        items.add(item);
                    }
                }
                else if(mode == BuildInventory.ShopMode.ALL){
                    ItemStack item = addLoreToShopItem(shop);
                    items.add(item);
                }
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(getInstance(), ()->{
            for(ItemStack item: items) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                for (NamespacedKey key : pdc.getKeys()) {
                    pdc.remove(key);
                }
            }
        }, 20 * 60 * 3);

        return items;
    }

}
