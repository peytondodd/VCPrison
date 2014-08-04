package net.vaultcraft.vcprison.mine;

import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcprison.utils.Rank;
import net.vaultcraft.vcutils.protection.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Connor on 8/1/14. Designed for the VCPrison project.
 */

public class MineLoader {

    private static HashMap<Mine, BlockCollection> mines = new HashMap<>();

    public static void loadMines() {
        try {
            //load
            File folder = VCPrison.getInstance().getDataFolder();
            if (!(folder.exists()))
                folder.mkdirs();

            File file = new File(folder, ".mines");
            if (!(file.exists()))
                file.mkdir();

            for (String line : FileUtils.readLines(file)) {
                line = line.toLowerCase().replace(" ", "");
                HashMap<String, Object> data = new HashMap<>();
                if (line.contains("rank")) {
                    Rank parse = Rank.fromName(findInsideMap(line, "rank"));
                    String blocks = findInsideMap(line, "blocks");
                    data.put("rank", parse);

                    //load block data
                    HashMap<Material, Double> blcks = new HashMap<>();
                    for (String key : blocks.split(",")) {
                        String use = key.replaceAll("[\\),\\(]", "");
                        double percent = Double.parseDouble(use.split("[\\%]")[0]);
                        Material type = Material.getMaterial(use.split("%")[1].toUpperCase());
                        blcks.put(type, percent);
                    }
                    data.put("blocks", blcks);
                }

                Rank rank = (Rank)data.get("rank");
                Mine mine = new Mine(rank, ProtectionManager.getInstance().getArea(rank.toString()).getArea());
                mines.put(mine, new BlockCollection((HashMap<Material, Double>)data.get("blocks")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String findInsideMap(String total, String key) {
        int after = total.indexOf(key)+key.length()+1;
        String modify = total.substring(after);
        String make = "";
        int deep = 0;
        for (char c : modify.toCharArray()) {
            if (c == '(')
                deep++;
            if (c == ')')
                deep--;

            if (deep >= 0)
                make+=c;
            else
                break;
        }
        return make;
    }

    public static void resetMine(Mine mine) {
        mines.get(mine).reset(BlockCollection.iterator(mine.getArea()));
        for (Player player : mine.getArea().getMax().getWorld().getPlayers()) {
            if (mine.getArea().isInArea(player.getLocation())) {
                Location tp = player.getLocation().clone();
                tp.setY(mine.getArea().getMax().getY());
                player.teleport(tp);
            }
        }
    }

    public static Collection<Mine> getMines() {
        return mines.keySet();
    }

    public static Mine fromRank(Rank rank) {
        for (Mine mine : mines.keySet()) {
            if (mine.getRank().equals(rank))
                return mine;
        }
        return null;
    }
}