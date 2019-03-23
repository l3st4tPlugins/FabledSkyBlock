package me.goodandevil.skyblock.levelling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.goodandevil.skyblock.SkyBlock;
import me.goodandevil.skyblock.island.Island;
import me.goodandevil.skyblock.island.IslandEnvironment;
import me.goodandevil.skyblock.island.IslandWorld;

public class Chunk {

	private final SkyBlock skyblock;
	private Island island;

	private List<ChunkSnapshot> chunkSnapshots = new ArrayList<>();
	private boolean complete;

	public Chunk(SkyBlock skyblock, Island island) {
		this.skyblock = skyblock;
		this.island = island;

		complete = false;
	}

	public void prepare() {
		new BukkitRunnable() {
			@Override
			public void run() {
				prepareChunkSnapshots();
			}
		}.runTask(skyblock);
	}

	public List<ChunkSnapshot> getChunkSnapshots() {
		return chunkSnapshots;
	}

	public boolean isComplete() {
		return complete;
	}

	private void prepareChunkSnapshots() {
	    FileConfiguration config = skyblock.getFileManager().getConfig(new File(skyblock.getDataFolder(), "config.yml")).getFileConfiguration();
	    FileConfiguration islandData = skyblock.getFileManager().getConfig(new File(new File(skyblock.getDataFolder().toString() + "/island-data"), island.getOwnerUUID().toString() + ".yml")).getFileConfiguration();
	    boolean hasNormal = true;
	    boolean hasNether = config.getBoolean("Island.World.Nether.Enable") && islandData.getBoolean("Unlocked.Nether", false);
	    boolean hasEnd = config.getBoolean("Island.World.End.Enable") && islandData.getBoolean("Unlocked.End", false);
	    
		for (IslandWorld iWorld : IslandWorld.values()) {
			if ((iWorld == IslandWorld.Normal && hasNormal) || (iWorld == IslandWorld.Nether && hasNether) || (iWorld == IslandWorld.End && hasEnd)) {
				Location islandLocation = island.getLocation(iWorld, IslandEnvironment.Island);

				Location minLocation = new Location(islandLocation.getWorld(),
						islandLocation.getBlockX() - island.getRadius(), 0,
						islandLocation.getBlockZ() - island.getRadius());
				Location maxLocation = new Location(islandLocation.getWorld(),
						islandLocation.getBlockX() + island.getRadius(), islandLocation.getWorld().getMaxHeight(),
						islandLocation.getBlockZ() + island.getRadius());

				int MinX = Math.min(maxLocation.getBlockX(), minLocation.getBlockX());
				int MinZ = Math.min(maxLocation.getBlockZ(), minLocation.getBlockZ());

				int MaxX = Math.max(maxLocation.getBlockX(), minLocation.getBlockX());
				int MaxZ = Math.max(maxLocation.getBlockZ(), minLocation.getBlockZ());

				for (int x = MinX - 16; x <= MaxX + 16; x += 16) {
					for (int z = MinZ - 16; z <= MaxZ + 16; z += 16) {
						org.bukkit.Chunk chunk = islandLocation.getWorld().getBlockAt(x, 0, z).getChunk();
						chunkSnapshots.add(chunk.getChunkSnapshot());
					}
				}
			}
		}

		complete = true;
	}
}