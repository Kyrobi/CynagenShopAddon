package me.kyrobi.cynagenshopsearch.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.Arrays;


/*
Fully AI generated code LOL
 */

public class FakePlayerCreator {

    public static Player createFakePlayerWithInventory(String name) {
        return createFakePlayerWithInventory(name, false);
    }

    public static Player createFakePlayerWithInventory(String name, boolean debug) {
        // Create a fake inventory with more space
        Inventory fakeInventory = Bukkit.createInventory(null, 36, name + "'s Inventory");

        // Create fake player inventory wrapper
        PlayerInventory fakePlayerInventory = (PlayerInventory) Proxy.newProxyInstance(
                PlayerInventory.class.getClassLoader(),
                new Class[]{PlayerInventory.class},
                new InventoryHandler(fakeInventory, debug)
        );

        // Create the fake player
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class[]{Player.class},
                new PlayerHandler(name, fakePlayerInventory, debug)
        );
    }

    private static class PlayerHandler implements InvocationHandler {
        private final String name;
        private final PlayerInventory inventory;
        private final UUID uuid;
        private final boolean debug;

        public PlayerHandler(String name, PlayerInventory inventory, boolean debug) {
            this.name = name;
            this.inventory = inventory;
            this.uuid = UUID.nameUUIDFromBytes(name.getBytes());
            this.debug = debug;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (debug) {
                System.out.println("FakePlayer method called: " + method.getName() +
                        (args != null ? " with args: " + java.util.Arrays.toString(args) : ""));
            }

            switch (method.getName()) {
                case "getName":
                    return name;
                case "getUniqueId":
                    return uuid;
                case "getInventory":
                    return inventory;
                case "isOnline":
                    return true;
                case "hasPermission":
                    // Grant specific permissions that might be needed
                    if (args != null && args.length > 0) {
                        String permission = (String) args[0];
                        if (debug) {
                            System.out.println("Permission check: " + permission);
                        }
                        // Grant permissions that are typically needed for shop interactions
                        if (permission.contains("quickshop.other.use") ||
                                permission.contains("quickshop.buy") ||
                                permission.contains("quickshop.use")) {
                            return true;
                        }
                    }
                    return true; // Default to true for fake player
                case "sendMessage":
                    if (debug && args != null && args.length > 0) {
                        System.out.println("Message to fake player: " + args[0]);
                    }
                    return null;
                case "getWorld":
                    return Bukkit.getWorlds().get(0); // Return first world
                case "getLocation":
                    World world = Bukkit.getWorlds().get(0);
                    return world != null ? world.getSpawnLocation() :
                            new Location(null, 0, 0, 0);
                case "toString":
                    return "FakePlayer{name=" + name + "}";
                case "hashCode":
                    return uuid.hashCode();
                case "equals":
                    return args.length > 0 && args[0] instanceof Player &&
                            ((Player) args[0]).getUniqueId().equals(uuid);
                case "isValid":
                    return true;
                case "isDead":
                    return false;
                case "getHealth":
                    return 20.0;
                case "getMaxHealth":
                    return 20.0;
                case "isSneaking":
                    return false;
                case "isSprinting":
                    return false;
                case "isFlying":
                    return false;
                case "getGameMode":
                    return org.bukkit.GameMode.SURVIVAL;
                case "getLocale":
                    return java.util.Locale.ENGLISH;
                default:
                    // Handle return types
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) return false;
                    if (returnType == int.class) return 0;
                    if (returnType == double.class) return 0.0;
                    if (returnType == float.class) return 0.0f;
                    if (returnType == long.class) return 0L;
                    return null;
            }
        }
    }

    private static class InventoryHandler implements InvocationHandler {
        private final Inventory backingInventory;
        private final boolean debug;

        public InventoryHandler(Inventory backingInventory, boolean debug) {
            this.backingInventory = backingInventory;
            this.debug = debug;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (debug) {
                System.out.println("Inventory method called: " + method.getName() +
                        (args != null ? " with args: " + java.util.Arrays.toString(args) : ""));
            }

            // Delegate most inventory operations to the backing inventory
            switch (method.getName()) {
                case "getSize":
                    return backingInventory.getSize();
                case "getMaxStackSize":
                    return backingInventory.getMaxStackSize();
                case "setMaxStackSize":
                    backingInventory.setMaxStackSize((Integer) args[0]);
                    return null;
                case "getItem":
                    return backingInventory.getItem((Integer) args[0]);
                case "setItem":
                    backingInventory.setItem((Integer) args[0], (ItemStack) args[1]);
                    return null;
                case "addItem":
                    // Fix: Properly handle the Object[] to ItemStack[] conversion
                    if (args != null && args.length > 0 && args[0] instanceof Object[]) {
                        Object[] objArray = (Object[]) args[0];
                        ItemStack[] itemArray = new ItemStack[objArray.length];
                        for (int i = 0; i < objArray.length; i++) {
                            if (objArray[i] instanceof ItemStack) {
                                itemArray[i] = (ItemStack) objArray[i];
                            }
                        }
                        return backingInventory.addItem(itemArray);
                    } else if (args != null && args.length > 0 && args[0] instanceof ItemStack[]) {
                        return backingInventory.addItem((ItemStack[]) args[0]);
                    }
                    return backingInventory.addItem();
                case "removeItem":
                    // Apply similar fix for removeItem
                    if (args != null && args.length > 0 && args[0] instanceof Object[]) {
                        Object[] objArray = (Object[]) args[0];
                        ItemStack[] itemArray = new ItemStack[objArray.length];
                        for (int i = 0; i < objArray.length; i++) {
                            if (objArray[i] instanceof ItemStack) {
                                itemArray[i] = (ItemStack) objArray[i];
                            }
                        }
                        return backingInventory.removeItem(itemArray);
                    } else if (args != null && args.length > 0 && args[0] instanceof ItemStack[]) {
                        return backingInventory.removeItem((ItemStack[]) args[0]);
                    }
                    return backingInventory.removeItem();
                case "getContents":
                    return backingInventory.getContents();
                case "setContents":
                    // Fix: Handle Object[] to ItemStack[] conversion for setContents too
                    if (args != null && args.length > 0) {
                        if (args[0] instanceof Object[]) {
                            Object[] objArray = (Object[]) args[0];
                            ItemStack[] itemArray = new ItemStack[objArray.length];
                            for (int i = 0; i < objArray.length; i++) {
                                if (objArray[i] instanceof ItemStack) {
                                    itemArray[i] = (ItemStack) objArray[i];
                                }
                            }
                            backingInventory.setContents(itemArray);
                        } else if (args[0] instanceof ItemStack[]) {
                            backingInventory.setContents((ItemStack[]) args[0]);
                        }
                    }
                    return null;
                case "getStorageContents":
                    return backingInventory.getContents();
                case "setStorageContents":
                    // Apply same fix here
                    if (args != null && args.length > 0) {
                        if (args[0] instanceof Object[]) {
                            Object[] objArray = (Object[]) args[0];
                            ItemStack[] itemArray = new ItemStack[objArray.length];
                            for (int i = 0; i < objArray.length; i++) {
                                if (objArray[i] instanceof ItemStack) {
                                    itemArray[i] = (ItemStack) objArray[i];
                                }
                            }
                            backingInventory.setContents(itemArray);
                        } else if (args[0] instanceof ItemStack[]) {
                            backingInventory.setContents((ItemStack[]) args[0]);
                        }
                    }
                    return null;
                case "contains":
                    if (args.length == 1) {
                        if (args[0] instanceof Material) {
                            return backingInventory.contains((Material) args[0]);
                        } else if (args[0] instanceof ItemStack) {
                            return backingInventory.contains((ItemStack) args[0]);
                        }
                    } else if (args.length == 2) {
                        if (args[0] instanceof Material) {
                            return backingInventory.contains((Material) args[0], (Integer) args[1]);
                        } else if (args[0] instanceof ItemStack) {
                            return backingInventory.contains((ItemStack) args[0], (Integer) args[1]);
                        }
                    }
                    return false;
                case "clear":
                    if (args == null || args.length == 0) {
                        backingInventory.clear();
                    } else {
                        backingInventory.clear((Integer) args[0]);
                    }
                    return null;
                case "first":
                    if (args[0] instanceof Material) {
                        return backingInventory.first((Material) args[0]);
                    } else {
                        return backingInventory.first((ItemStack) args[0]);
                    }
                case "firstEmpty":
                    return backingInventory.firstEmpty();
                case "isEmpty":
                    return backingInventory.isEmpty();
                case "remove":
                    if (args[0] instanceof Material) {
                        backingInventory.remove((Material) args[0]);
                    } else {
                        backingInventory.remove((ItemStack) args[0]);
                    }
                    return null;
                // PlayerInventory specific methods
                case "getArmorContents":
                    return new ItemStack[4]; // Empty armor
                case "setArmorContents":
                    return null; // Do nothing
                case "getExtraContents":
                    return new ItemStack[1]; // Empty off-hand
                case "setExtraContents":
                    return null; // Do nothing
                case "getHelmet":
                case "getChestplate":
                case "getLeggings":
                case "getBoots":
                case "getItemInMainHand":
                case "getItemInOffHand":
                    return null;
                case "setHelmet":
                case "setChestplate":
                case "setLeggings":
                case "setBoots":
                case "setItemInMainHand":
                case "setItemInOffHand":
                    return null;
                case "getHeldItemSlot":
                    return 0;
                case "setHeldItemSlot":
                    return null;
                default:
                    // Try to delegate to backing inventory
                    try {
                        return method.invoke(backingInventory, args);
                    } catch (Exception e) {
                        if (debug) {
                            System.out.println("Failed to delegate method " + method.getName() + ": " + e.getMessage());
                        }
                        // Return appropriate default for return type
                        Class<?> returnType = method.getReturnType();
                        if (returnType == boolean.class) return false;
                        if (returnType == int.class) return 0;
                        if (returnType == void.class) return null;
                        return null;
                    }
            }
        }
    }
}