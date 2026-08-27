package iran.flame.network.cube.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class ChunkLocation {
    private final World world;
    private final Integer x;
    private final Integer z;

    public ChunkLocation(Location location) {
        this.world = location.getWorld();
        this.x = location.getBlockX() >> 4;
        this.z = location.getBlockZ() >> 4;
    }

    public boolean isLoaded() {
        return this.world.isChunkLoaded(this.x, this.z);
    }

    public World getWorld() {
        return this.world;
    }

    public Integer getX() {
        return this.x;
    }

    public Integer getZ() {
        return this.z;
    }
}