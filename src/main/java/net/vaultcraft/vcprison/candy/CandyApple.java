package net.vaultcraft.vcprison.candy;

import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcutils.uncommon.Particles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by tacticalsk8er on 11/2/2014.
 */
public class CandyApple implements Candy {

    @Override
    public Recipe getRecipe() {
//        CandyShapedRecipe shapedRecipe = new CandyShapedRecipe(CandyItems.CANDYAPPLE);
//        shapedRecipe.shape('x', 'y', 'x',
//                'x', 'z', 'x',
//                'x', 'x', 'x');
//        shapedRecipe.setItem('x', CandyItems.REDDYE);
//        shapedRecipe.setItem('y', CandyItems.NETHERSTAR);
//        shapedRecipe.setItem('z', CandyItems.SUGARCUBE);
        ShapedRecipe shapedRecipe = new ShapedRecipe(CandyItems.CANDYAPPLE);
        shapedRecipe.shape("xyx", "xzx", "xxx");
        shapedRecipe.setIngredient('x', Material.INK_SACK.getNewData((byte)1));
        shapedRecipe.setIngredient('y', Material.NETHER_STAR);
        shapedRecipe.setIngredient('z', Material.SNOW_BLOCK);
        return shapedRecipe;
    }

    @Override
    public ItemStack getCandyItem() {
        return CandyItems.CANDYAPPLE;
    }

    @Override
    public int getCooldown() {
        return 1;
    }

    @Override
    public int getHarmfulAfter() {
        return 20;
    }

    @Override
    public ItemStack onCandyConsume(Player player, boolean harmful) {
        Item item = player.getWorld().dropItem(player.getEyeLocation(), CandyItems.CANDYAPPLE);
        item.setPickupDelay(20 * 5000);
        Vector vector = player.getLocation().getDirection();
        vector.multiply(2.25);
        item.setVelocity(vector);
        Runnable runnable = () -> {
            List<Entity> entityList = item.getNearbyEntities(4, 4, 4);
            Particles.HUGE_EXPLOSION.sendToLocation(item.getLocation(), 0, 0, 0, 1, 1);
            item.getWorld().playSound(item.getLocation(), Sound.EXPLODE, 1, 0.7f);
            item.remove();
            for(Entity entity : entityList) {
                if(!(entity instanceof LivingEntity))
                    continue;

                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.removePotionEffect(PotionEffectType.SLOW);
                livingEntity.removePotionEffect(PotionEffectType.BLINDNESS);
                livingEntity.addPotionEffect(PotionEffectType.SLOW.createEffect(20 * 20, 0));
                livingEntity.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20 * 20, 0));
            }
        };

        Bukkit.getScheduler().scheduleSyncDelayedTask(VCPrison.getInstance(), runnable, 3 * 20l);
        return null;
    }
}
