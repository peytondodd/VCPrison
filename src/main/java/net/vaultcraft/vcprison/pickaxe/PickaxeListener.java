package net.vaultcraft.vcprison.pickaxe;

import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcprison.mine.MineLoader;
import net.vaultcraft.vcprison.mine.warp.WarpGUI;
import net.vaultcraft.vcprison.user.PrisonUser;
import net.vaultcraft.vcprison.worth.Warden;
import net.vaultcraft.vcutils.chat.Form;
import net.vaultcraft.vcutils.chat.Prefix;
import net.vaultcraft.vcutils.user.Group;
import net.vaultcraft.vcutils.user.User;
import net.vaultcraft.vcutils.user.UserLoadedEvent;
import org.bukkit.*;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by tacticalsk8er on 8/3/2014.
 */
public class PickaxeListener implements Listener {

    public PickaxeListener() {
        Bukkit.getPluginManager().registerEvents(this, VCPrison.getInstance());
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
            if (event.getCurrentItem().getType() == Material.DIAMOND_PICKAXE) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
            event.setCancelled(true);
            return;
        }

        Inventory perkMenu = event.getInventory();
        if (perkMenu == null)
            return;
        if (!perkMenu.getName().equalsIgnoreCase("Pickaxe Perks"))
            return;
        if (event.getInventory() == null)
            return;
        if (event.getInventory().getName() == null)
            return;
        if (!event.getInventory().getName().equalsIgnoreCase("Pickaxe Perks"))
            return;
        if (event.getCurrentItem() == null)
            return;
        if (event.getCurrentItem().getItemMeta() == null)
            return;
        if(event.getCurrentItem().getItemMeta().getDisplayName() == null)
            return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Pickaxe pickaxe = PrisonUser.fromPlayer(player).getPickaxe();
        if(pickaxe == null)
            return;
        if(!pickaxe.isInUse())
            return;
        if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Perk Points:")) {
            if (pickaxe.getPickPoints() == 0) {
                Form.at(player, Prefix.ERROR, "You have no perk points!");
                return;
            }
            HashMap<Integer, ItemStack> noRoom;
            noRoom = player.getInventory().addItem(Pickaxe.getAddPointItem());
            if (noRoom.isEmpty()) {
                Form.at(player, Prefix.SUCCESS, "You have 1 more perk point item in your inventory.");
                pickaxe.setPickPoints(pickaxe.getPickPoints() - 1);
                perkMenu.setItem(PickaxePerk.getPerks().size(), pickaxe.getPointsIcon());
                return;
            } else {
                Form.at(player, Prefix.ERROR, "You need to clear space in your inventory!");
                return;
            }
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Warps")) {
            player.closeInventory();
            player.openInventory(WarpGUI.create(PrisonUser.fromPlayer(player)));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.translateAlternateColorCodes('&', "&6&lFF&e&lA"))) {
            player.closeInventory();
            player.performCommand("redir ffa");
            return;
        }
        if(event.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "Cells")) {
            player.closeInventory();
            player.performCommand("redir cell");
            return;
        }
        if(event.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.GOLD + ChatColor.BOLD.toString() + "Shop")) {
            player.closeInventory();
            player.performCommand("redir shop");
            return;
        }

        PickaxePerk perk = PickaxePerk.getPerkFromName(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName().replace("Toggle Off ", "").replace("Toggle On ", "").replaceAll(" \\d+", "").replace(" Max", "")));
        if(perk == null)
            return;
        if (pickaxe.getPerkLevel(perk) == perk.getMaxLevel()) {
            Form.at(player, Prefix.ERROR, "Pickaxe perk is at it highest level!");
            return;
        }
        if (perk.isToggleable()) {
            if (event.getCurrentItem().getItemMeta().getDisplayName().contains("Toggle")) {
                pickaxe.togglePerk(player, perk);
                if (pickaxe.getToggle(perk))
                    perkMenu.setItem(event.getSlot(), perk.getToggleOff());
                else
                    perkMenu.setItem(event.getSlot(), perk.getToggleOn());
                return;
            }
            if (pickaxe.getPickPoints() < perk.getCost()) {
                Form.at(player, Prefix.ERROR, "You don't have enough perk points to buy this perk!");
                return;
            }
            pickaxe.addPerkToggle(player, perk);
            perkMenu.setItem(event.getSlot(), perk.getToggleOff());
            perkMenu.setItem(PickaxePerk.getPerks().size(), pickaxe.getPointsIcon());
            return;
        }
        if (pickaxe.getPickPoints() < perk.getCost()) {
            Form.at(player, Prefix.ERROR, "You don't have enough perk points to buy this perk!");
            return;
        }
        pickaxe.addPerkLevel(player, perk);
        perkMenu.setItem(event.getSlot(), perk.getIcon(pickaxe.getPerkLevel(perk)));
        perkMenu.setItem(PickaxePerk.getPerks().size(), pickaxe.getPointsIcon());
    }

    @EventHandler
    public void onHotbarHover(PlayerItemHeldEvent event) {
        if(PrisonUser.fromPlayer(event.getPlayer()) == null)
            return;
        Pickaxe pickaxe = PrisonUser.fromPlayer(event.getPlayer()).getPickaxe();
        if(pickaxe == null)
            return;
        if(!pickaxe.isInUse())
            return;
        if (event.getNewSlot() == 0) {
            for (PickaxePerk perk : PickaxePerk.getPerks()) {
                if (pickaxe.getPerkLevel(perk) == 0)
                    continue;
                if (perk.isToggleable())
                    if (!pickaxe.getToggle(perk))
                        continue;
                perk.onHoverOn(event.getPlayer(), pickaxe.getPerkLevel(perk));
            }
        }
        if (event.getPreviousSlot() == 0) {
            for (PickaxePerk perk : PickaxePerk.getPerks()) {
                if (pickaxe.getPerkLevel(perk) == 0)
                    continue;
                if (perk.isToggleable())
                    if (!pickaxe.getToggle(perk))
                        continue;
                perk.onHoverOff(event.getPlayer(), pickaxe.getPerkLevel(perk));
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE)
            event.setCancelled(true);
    }

    //
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if(event.getRecipe().getResult().getType() == Material.DIAMOND_PICKAXE
                || event.getRecipe().getResult().getType() == Material.DIAMOND_SWORD
                || event.getRecipe().getResult().getType() == Material.IRON_SWORD
                || event.getRecipe().getResult().getType() == Material.GOLD_SWORD
                || event.getRecipe().getResult().getType() == Material.STONE_SWORD
                || event.getRecipe().getResult().getType() == Material.DIAMOND_AXE
                || event.getRecipe().getResult().getType() == Material.WOOD_SWORD) {

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Pickaxe pickaxe = PrisonUser.fromPlayer(event.getPlayer()).getPickaxe();
        if(pickaxe == null)
            return;
        if(!pickaxe.isInUse())
            return;
        if (event.getAction().name().contains("RIGHT") && event.getPlayer().getInventory().getHeldItemSlot() == 0) {
            event.getPlayer().openInventory(pickaxe.getStatsMenu());
        }

        if (event.getAction().name().contains("RIGHT")) {
            if (event.getPlayer().getItemInHand() != null) {
                if (event.getPlayer().getItemInHand().getItemMeta() != null) {
                    if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName() != null) {
                        if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&6&lRight Click: &2&lAdd Perk Point"))) {
                            if(PrisonUser.fromPlayer(event.getPlayer()).getPickaxe().isInUse()) {
                                if (event.getPlayer().getItemInHand().getAmount() != 1)
                                    event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
                                else
                                    event.getPlayer().getInventory().remove(event.getPlayer().getItemInHand());
                                PrisonUser.fromPlayer(event.getPlayer()).getPickaxe().setPickPoints(PrisonUser.fromPlayer(event.getPlayer()).getPickaxe().getPickPoints() + 1);
                                Form.at(event.getPlayer(), Prefix.SUCCESS, "You have added a perk point to your pickaxe!");
                            }
                        }
                    }
                }
            }
        }
    }

    private HashSet<Player> warnCooloff = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        if (event.getPlayer().getInventory().getHeldItemSlot() != 0)
            return;
        Pickaxe pickaxe = PrisonUser.fromPlayer(event.getPlayer()).getPickaxe();
        if(pickaxe == null)
            return;
        if(MineLoader.fromLocation(event.getBlock().getLocation()) == null) {
            event.getPlayer().getInventory().setItem(0, pickaxe.getPickaxe());
            return;
        }
        if(!pickaxe.isInUse())
            return;
        event.setCancelled(true);
        ItemStack item;
        if (event.getBlock().getType() == Material.LAPIS_ORE)
            item = new ItemStack(Material.INK_SACK, 1, (short) 4);
        else
            item = new ItemStack(changeType(event.getBlock().getType()));
        for (PickaxePerk perk : PickaxePerk.getPerks()) {
            if (pickaxe.getPerkLevel(perk) == 0)
                continue;
            if (perk.isToggleable())
                if (!pickaxe.getToggle(perk))
                    continue;
            item = perk.onBreak(event.getPlayer(), event, event.getBlock(), item, pickaxe.getPerkLevel(perk));
        }
        pickaxe.mineBlock(item.getType());
        event.getBlock().setType(Material.AIR);
        spawnExp(item.getType(), event.getBlock().getWorld(), event.getPlayer().getLocation());
        event.getPlayer().getInventory().addItem(item);
        if(event.getPlayer().getInventory().firstEmpty() == -1) {
            if (warnCooloff.contains(event.getPlayer()))
                return;
            User user = User.fromPlayer(event.getPlayer());
            if(user.getGroup().hasPermission(Group.ENDERDRAGON)) {
                Form.at(event.getPlayer(), Prefix.VAULT_CRAFT, "Your inventory is full, but you have auto selling from your rank!");
                Warden.sell(event.getPlayer());
            } else {
                Form.at(event.getPlayer(), Prefix.WARNING, "Your inventory is full!");
                warnCooloff.add(event.getPlayer());

                Runnable r = new Runnable() {
                    public void run() {
                        warnCooloff.remove(event.getPlayer());
                    }
                };
                Bukkit.getScheduler().scheduleSyncDelayedTask(VCPrison.getInstance(), r, 20);
            }
        }
    }

    public static void spawnExp(Material type, World world, Location location) {
        ExperienceOrb experienceOrb = world.spawn(location, ExperienceOrb.class);
        switch (type) {
            case COAL:
                experienceOrb.setExperience(5);
                break;
            case REDSTONE:
                experienceOrb.setExperience(10);
                break;
            case INK_SACK:
                experienceOrb.setExperience(10);
                break;
            case DIAMOND:
                experienceOrb.setExperience(15);
                break;
            case EMERALD:
                experienceOrb.setExperience(20);
                break;
            default:
                experienceOrb.remove();
                break;
        }
    }

    public static Material changeType(Material type) {
        switch (type) {
            case COAL_ORE:
                return Material.COAL;
            case REDSTONE_ORE:
                return Material.REDSTONE;
            case GLOWING_REDSTONE_ORE:
                return Material.REDSTONE;
            case DIAMOND_ORE:
                return Material.DIAMOND;
            case EMERALD_ORE:
                return Material.EMERALD;
            case STONE:
                return Material.COBBLESTONE;
            default:
                return type;
        }
    }

    @EventHandler
    public void onUserLoad(UserLoadedEvent event) {
        PrisonUser user = PrisonUser.fromPlayer(event.getUser().getPlayer());
        if (event.getUser().getUserdata("Pickaxe") != null)
            user.setPickaxe(new Pickaxe(user.getPlayer(), event.getUser().getUserdata("Pickaxe")));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Pickaxe pickaxe = PrisonUser.fromPlayer(event.getEntity()).getPickaxe();
        event.getDrops().remove(pickaxe.getPickaxe());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Pickaxe pickaxe = PrisonUser.fromPlayer(event.getPlayer()).getPickaxe();
        if(pickaxe.isInUse())
            event.getPlayer().getInventory().setItem(0, pickaxe.getPickaxe());
    }
}