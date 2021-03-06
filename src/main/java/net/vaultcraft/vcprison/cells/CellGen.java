package net.vaultcraft.vcprison.cells;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.data.DataException;
import net.vaultcraft.vcprison.VCPrison;
import net.vaultcraft.vcutils.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by tacticalsk8er on 10/26/2014.
 */
public class CellGen extends ChunkGenerator {

    CuboidClipboard cells;
    CuboidClipboard hallway;

    public CellGen() {
        try {
            cells = CuboidClipboard.loadSchematic(new File(VCPrison.getInstance().getDataFolder(), "cells.schematic"));
            hallway = CuboidClipboard.loadSchematic(new File(VCPrison.getInstance().getDataFolder(), "hallway.schematic"));
            cells.rotate2D(90);
            hallway.rotate2D(90);
        } catch (DataException | IOException e) {
            Logger.error(VCPrison.getInstance(), e);
        }
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        world.getPopulators().add(new CellPop(cells, hallway));
        return world.getPopulators();
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random rand) {
        return new Location(world, -9, 84, -1);
    }

    @Override
    public byte[][] generateBlockSections(World world, Random rand, int chunkX, int chunkY, BiomeGrid biomes) {
        byte[][] result = new byte[world.getMaxHeight() / 16][];
        for (int x = 0; x < 16; x++) {
            for (int y = 80; y < 99; y++) {
                for (int z = 0; z < 16; z++) {
                    if (chunkX % 2 == 0) {
                        if (cells.getBlock(new Vector(x, y - 80, z)).getId() != 0 && cells.getBlock(new Vector(x, y - 80, z)).getId() != 50)
                            if (cells.getBlock(new Vector(x, y - 80, z)).getData() == 0)
                                setBlock(result, x, y, z, (byte) cells.getBlock(new Vector(x, y - 80, z)).getId());
                    } else {
                        if (hallway.getBlock(new Vector(x, y - 80, z)).getId() != 0 && cells.getBlock(new Vector(x, y - 80, z)).getId() != 50)
                            if (hallway.getBlock(new Vector(x, y - 80, z)).getData() == 0)
                                setBlock(result, x, y, z, (byte) hallway.getBlock(new Vector(x, y - 80, z)).getId());
                    }
                }
            }
        }
        return result;
    }

    void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
        // is this chunk part already initialized?
        if (result[y >> 4] == null) {
            // Initialize the chunk part
            result[y >> 4] = new byte[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }
}
