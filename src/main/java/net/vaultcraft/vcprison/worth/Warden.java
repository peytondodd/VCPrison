package net.vaultcraft.vcprison.worth;

import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcprison.user.PrisonUser;
import net.vaultcraft.vcprison.utils.Rank;
import net.vaultcraft.vcutils.chat.Form;
import net.vaultcraft.vcutils.chat.Prefix;
import net.vaultcraft.vcutils.events.TimeUnit;
import net.vaultcraft.vcutils.user.Group;
import net.vaultcraft.vcutils.user.User;
import net.vaultcraft.vcutils.util.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

/**
 * Created by Connor on 8/8/14. Designed for the VCPrison project.
 */

public class Warden {

    private static WardenListener wl;

    private static HashMap<Player, BukkitTask> selling = new HashMap<>();
    private static HashMap<String, ISDC> resume = new HashMap<>();

    public Warden() {
        wl = new WardenListener();

        Bukkit.getPluginManager().registerEvents(wl, VCPrison.getInstance());
    }

    public static void sell(final Player player) {
        //read inventory and determine sell time based on rank
        if (selling.containsKey(player)) {
            Form.at(player, Prefix.ERROR, "You are currently selling items right now.");
            return;
        }

        final User user = User.fromPlayer(player);
        final PrisonUser pUser = PrisonUser.fromPlayer(player);

        Rank rank = pUser.getRank();
        Group.GroupHandler group = user.getGroup();

        int worth = 0;
        int ticks = 0;

        for (ItemStack inv : player.getInventory().getContents()) {
            if (inv == null)
                continue;

            double add = ItemWorthLoader.getWorth(rank, inv.getType());
            if (add == -1)
                continue;

            worth+=(add*inv.getAmount());
            ticks+=(20*getSellTimeMultiplier(group));
            player.getInventory().remove(inv);
        }

        ticks = (ticks < 0 ? 0 : ticks);

        player.updateInventory();

        if (worth == 0) {
            Form.atCharacter(player, Prefix.CHARACTER, "You don't have any items to sell me!", "WARDEN");
            return;
        }

        final ISDC dc = new ISDC();
        dc.worth = worth;
        dc.ticks = ticks;
        resume.put(player.getName(), dc);

        String date = (DateUtil.fromTime(TimeUnit.SECONDS, ticks/20));
        Form.atCharacter(player, Prefix.CHARACTER, "I'm selling your items, it will take about &e&n" + (date.equals("") ? "0 seconds" : date), "WARDEN");

        final int finalWorth = worth;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(VCPrison.getInstance(), new Runnable() {
            public void run() {
                if (!(player.isOnline())) {
                    //handle players who quit
                    return;
                    //ez ^
                }

                user.setMoney(user.getMoney()+finalWorth);
                Form.atCharacter(player, Prefix.CHARACTER, "Your items were sold for &e$"+Form.at(finalWorth)+Prefix.SUCCESS.getChatColor()+"!", "WARDEN");
                player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 0);
                selling.remove(player);
                resume.remove(player.getName());
            }
        }, ticks);
        selling.put(player, task);
    }

    private double getMultiplier(Group group) {
        switch (group) {
            case WOLF:
                return 1.25;
            case SLIME:
                return 1.75;
            case SKELETON:
                return 2.0;
            case ENDERMAN:
                return 2.5;
            case ENDERDRAGON:
                return 3.5;
        }
        return 1;
    }

    private static double getSellTimeMultiplier(Group.GroupHandler group) {
        double val = 1;
        if (group.hasPermission(Group.WOLF))
            val = 2;
        if (group.hasPermission(Group.SLIME))
            val = 3;
        if (group.hasPermission(Group.SKELETON))
            val = 4;
        if (group.hasPermission(Group.ENDERMAN))
            val = 5;
        else return 1;
        return -1;
    }

    private static class WardenListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
            final Player player = event.getPlayer();
            final User user = User.fromPlayer(player);
            ISDC c = resume.get(player.getName());
            if (c == null)
                return;

            Form.atCharacter(player, Prefix.CHARACTER, "I'll resume selling your items at once!", "WARDEN");

            final int finalWorth = c.worth;

            BukkitTask task = Bukkit.getScheduler().runTaskLater(VCPrison.getInstance(), new Runnable() {
                public void run() {
                    if (!(player.isOnline())) {
                        //handle players who quit
                        return;
                        //ez ^
                    }

                    user.setMoney(user.getMoney()+finalWorth);
                    Form.atCharacter(player, Prefix.CHARACTER, "Your items were sold for &e$" + Form.at(finalWorth) + Prefix.SUCCESS.getChatColor() + "!", "WARDEN");
                    player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1, 0);
                    resume.remove(player.getName());
                    selling.remove(player);
                }
            }, c.ticks);

            if (selling.containsKey(player))
                selling.remove(player);

            selling.put(player, task);
        }

        @EventHandler
        public void onEntityInteract(PlayerInteractEntityEvent event) {
            if (event.getRightClicked().getType().equals(EntityType.VILLAGER)) {
                Warden.sell(event.getPlayer());
            }
        }
    }

    private static class ISDC {
        private int worth;
        private int ticks;
    }
}