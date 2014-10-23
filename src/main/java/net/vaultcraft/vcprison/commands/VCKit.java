package net.vaultcraft.vcprison.commands;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.expression.runtime.For;
import net.vaultcraft.vcutils.chat.Form;
import net.vaultcraft.vcutils.chat.Prefix;
import net.vaultcraft.vcutils.command.ICommand;
import net.vaultcraft.vcutils.user.Group;
import net.vaultcraft.vcutils.user.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by CraftFest on 10/20/2014.
 */
public class VCKit extends ICommand {

    private HashMap<Group, ArrayList<ItemStack>> kits = new HashMap<Group, ArrayList<ItemStack>>() {{
        put(Group.COMMON, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.IRON_HELMET, "&7&lCommon Helmet").toItemStack());
            add(new ItemStackBuilder(Material.IRON_CHESTPLATE, "&7&lCommon Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.IRON_LEGGINGS, "&7&lCommon Leggings").toItemStack());
            add(new ItemStackBuilder(Material.IRON_BOOTS, "&7&lCommon Boots").toItemStack());
        }});

        put(Group.WOLF, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.IRON_HELMET, "&8&lWolf Helmet").toItemStack());
            add(new ItemStackBuilder(Material.IRON_CHESTPLATE, "&8&lWolf Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.IRON_LEGGINGS, "&8&lWolf Leggings").toItemStack());
            add(new ItemStackBuilder(Material.IRON_BOOTS, "&8&lWolf Boots").toItemStack());
        }});

        put(Group.SLIME, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.IRON_HELMET, "&a&lSlime Helmet").toItemStack());
            add(new ItemStackBuilder(Material.IRON_CHESTPLATE, "&a&lSlime Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.IRON_LEGGINGS, "&a&lSlime Leggings").toItemStack());
            add(new ItemStackBuilder(Material.IRON_BOOTS, "&a&lSlime Boots").toItemStack());
        }});

        put(Group.SKELETON, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.DIAMOND_HELMET, "&f&lSkeleton Helmet").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_CHESTPLATE, "&f&lSkeleton Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_LEGGINGS, "&f&lSkeleton Leggings").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_BOOTS, "&f&lSkeleton Boots").toItemStack());
        }});

        put(Group.ENDERMAN, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.DIAMOND_HELMET, "&5&lEnderman Helmet").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_CHESTPLATE, "&5&lEnderman Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_LEGGINGS, "&5&lEnderman Leggings").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_BOOTS, "&5&lEnderman Boots").toItemStack());
        }});

        put(Group.WITHER, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.DIAMOND_HELMET, "&f&lSkeleton Helmet").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_CHESTPLATE, "&f&lSkeleton Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_LEGGINGS, "&f&lSkeleton Leggings").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_BOOTS, "&f&lSkeleton Boots").toItemStack());
        }});

        put(Group.ENDERDRAGON, new ArrayList<ItemStack>() {{
            add(new ItemStackBuilder(Material.DIAMOND_HELMET, "&5&lEnder&7&lDragon Helmet").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_CHESTPLATE, "&5&lEnder&7&lDragon Chestplate").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_LEGGINGS, "&5&lEnder&7&lDragon Leggings").toItemStack());
            add(new ItemStackBuilder(Material.DIAMOND_BOOTS, "&5&lEnder&7&lDragon Boots").toItemStack());
        }});
    }};

    public VCKit(String name, Group permission, String... aliases) {
        super(name, permission, aliases);
    }

    public void processCommand(Player player, String[] args) {
        if(args.length == 0) {
            player.sendMessage(ChatColor.DARK_PURPLE + "Available kits" + ChatColor.GRAY + ":");
            for(Group g : kits.keySet()) {
                player.sendMessage(ChatColor.GRAY + "- " + ChatColor.LIGHT_PURPLE + g.getName());
            }
            player.sendMessage(ChatColor.GRAY + "Usage: /kit <kit name>");
        } else if(args.length == 1) {
            Group g = Group.fromString(args[0]);
            if(g == null) {
                Form.at(player, Prefix.ERROR, "That rank does not exist!");
                return;
            }
            if(!kits.containsKey(g)) {
                Form.at(player, Prefix.ERROR, "That rank does not have a kit!");
                return;
            }
            User theUser = User.fromPlayer(player);
            if(!theUser.getGroup().hasPermission(g)) {
                Form.at(player, Prefix.ERROR, "You don't have that rank!");
                return;
            }
            if(theUser.hasUserdata("kitCooldown"+g.getName())) {
                long lastGet = Long.parseLong(theUser.getUserdata("kitCooldown"+g.getName()));
                if(lastGet + 21600000 < System.currentTimeMillis()) {
                    theUser.removeUserdata("kitCooldown"+g.getName());
                } else {
                    long timeToWait = (lastGet + 21600000) - System.currentTimeMillis();
                    Form.at(player, Prefix.ERROR, "You need to wait " + String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeToWait),
                            TimeUnit.MILLISECONDS.toMinutes(timeToWait)) + " before getting this kit again.");
                    return;
                }
            }
            ArrayList<ItemStack> items = kits.get(g);
            int freeSpace = 0;
            for(ItemStack i : player.getInventory().getContents()) {
                if(i == null) {
                    freeSpace++;
                }

                if(freeSpace >= items.size()) {
                    break;
                }
            }
            if(freeSpace < items.size()) {
                Form.at(player, Prefix.ERROR, "You do not have enough space in your inventory for this kit. You need at least " + items.size() + " free slots.");
                return;
            }
            for(ItemStack i : items) {
                player.getInventory().addItem(i);
            }
            Form.at(player, Prefix.SUCCESS, "You have received the " + g.getName() + " kit!");
            theUser.addUserdata("kitCooldown" + g.getName(), String.valueOf(System.currentTimeMillis()));
        } else if(args.length > 1) {
            Form.at(player, Prefix.ERROR, "Too many arguments.");
        }
    }

    private class ItemStackBuilder {
        private ItemStack item;
        public ItemStackBuilder(Material type, byte data, String displayName, String... lore) {
            ItemStack stack = new ItemStack(type, 1, (short)1, data);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if (lore.length > 0) {
                List<String> l = Lists.newArrayList();
                for (String s : lore) {
                    l.add(ChatColor.translateAlternateColorCodes('&', s));
                }
                meta.setLore(l);
            }
            stack.setItemMeta(meta);
            this.item = stack;
        }

        public ItemStackBuilder(Material type, String displayName, String... lore) {
            new ItemStackBuilder(type, (byte)0, displayName, lore);
        }

        public ItemStackBuilder addEnchantment(Enchantment e, int level) {
            this.item.addEnchantment(e, level);
            return this;
        }

        public ItemStack toItemStack() {
            return item;
        }
    }
}
