package me.kyrobi.cynagenshopsearch;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private CynagenShopSearch plugin;

    public ShopCommand(final CynagenShopSearch plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if(!(commandSender instanceof Player)){
            return true;
        }


        // System.out.println("Using command");
        Player player = (Player) commandSender;
        // System.out.println("For " + player.getName());

//        if(!player.getName().equals("Mokokotei")){
//            return true;
//        }

        BuildInventory buildInventory = new BuildInventory(plugin);

        StringBuilder metaText = new StringBuilder();

        if(args.length >= 1){
            if(Material.getMaterial(args[0]) != null){
                for(int i = 1; i < args.length; i++){
                    metaText.append(args[i] + " ");
                }
            } else {
                for(int i = 0; i < args.length; i++){
                    metaText.append(args[i] + " ");
                }
            }
        }

        // Remove trailing whitespaces
        String metaString = metaText.toString().replaceFirst("\\s++$", "");

        player.sendMessage(ChatColor.GREEN + "Getting your items... this could take a moment");
        if(args.length >= 1){

            if(Material.getMaterial(args[0].toUpperCase()) != null){
                // If first argument is not null, find the item associated containing the meta
                buildInventory.createInventory(player, BuildInventory.ShopMode.ALL, args[0], metaString);
            } else {
                // If the material doesn't exist, assuming they are searching via meta
                buildInventory.createInventory(player, BuildInventory.ShopMode.ALL, "", metaString);
            }

        } else {
            buildInventory.createInventory(player, BuildInventory.ShopMode.ALL, "", "");
        }


        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {  // Only suggest for the first argument
            String partialInput = args[0].toUpperCase();  // Convert to uppercase since Material names are uppercase

            // Loop through all materials
            for (Material material : Material.values()) {
                String materialName = material.name();
                // Add material name if it starts with the partial input
                if (materialName.contains(partialInput.toUpperCase())) {
                    completions.add(materialName);
                }
            }
        }

        return completions;
    }
}
