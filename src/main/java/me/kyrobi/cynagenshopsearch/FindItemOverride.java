package me.kyrobi.cynagenshopsearch;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FindItemOverride implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        Player player = (Player) commandSender;

        player.sendMessage(ChatColor.RED + "/finditem has been replaced with" + ChatColor.GOLD + " /shop");

        String message =
                """
                    §a/shop
                    §fThis will open the shop menu
                    §fand show all items.
                    \n
                    §a/shop ENCHANTED_BOOK
                    §fThis will find shops with
                    §fenchanted books.
                    \n
                    §a/shop ENCHANTED_BOOK fortune
                    §fThis will find shops with
                    §fenchanted books that contains
                    §fthe text "fortune".
                    \n
                    §a/shop voting item
                    §fThis will find all items containing
                    §fthe word "voting item".
               \s""";

        player.sendMessage(message);


        return true;
    }
}
