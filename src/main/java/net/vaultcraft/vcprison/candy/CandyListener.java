package net.vaultcraft.vcprison.candy;

import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcutils.item.ItemUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * @author Connor Hollasch
 * @since 11/1/2014
 */
public class CandyListener implements Listener {

    public CandyListener() {
        Bukkit.getPluginManager().registerEvents(this, VCPrison.getInstance());

        //Recipe for Wrapper
        ShapedRecipe shapedRecipe = new ShapedRecipe(ItemUtils.build(Material.QUARTZ, ChatColor.translateAlternateColorCodes('&', "&7&lCandy Wrapper"), "You'll need something to contain sticky candies!"));
        shapedRecipe.shape("XXX", "XYX", "XXX");
        shapedRecipe.setIngredient('X', Material.INK_SACK, 8);
        shapedRecipe.setIngredient('Y', Material.SNOW_BLOCK);
        Bukkit.addRecipe(shapedRecipe);

        //Recipe for butter
        ShapedRecipe butter = new ShapedRecipe(ItemUtils.build(Material.INK_SACK, (byte)11, ChatColor.translateAlternateColorCodes('&', "&e&lButter"), "Used to create some types of candies"));
        butter.shape("XYX", "YXY", "XYX");
        butter.setIngredient('X', Material.WHEAT);
        butter.setIngredient('Y', Material.GLOWSTONE_DUST);
        Bukkit.addRecipe(butter);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack holding = event.getItem();
        Player player = event.getPlayer();
        Candy from = CandyManager.getCandy(holding);
        if (from == null)
            return;

        ItemStack drop = from.onCandyConsume(player);
        player.playSound(player.getLocation(), Sound.EAT, 1, 1);
        player.playEffect(player.getLocation(), Effect.STEP_SOUND, holding.getTypeId());

        if (player.getItemInHand().getAmount() == 1) {
            player.getInventory().remove(player.getItemInHand());
        } else {
            holding.setAmount(holding.getAmount()-1);
            player.setItemInHand(holding);
        }

        if (drop != null) {
            if (player.getInventory().firstEmpty() == -1) {
                Item iDrop = player.getWorld().dropItem(player.getLocation(), drop.clone());
                iDrop.setPickupDelay(20);
            } else {
                player.getInventory().addItem(drop.clone());
            }
        }

        event.setCancelled(true);
        player.updateInventory();
    }
}