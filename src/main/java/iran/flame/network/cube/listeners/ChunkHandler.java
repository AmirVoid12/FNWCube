package iran.flame.network.cube.listeners;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.utils.ChunkLocation;

public class ChunkHandler implements Listener {
    private final CubeManager cubeManager = GenCubes.getInstance().getCubeManager();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk loadedChunk = event.getChunk();

        for (GenCube cube : this.cubeManager.getCubes()) {
            ChunkLocation cubeChunkLocation = cube.getChunkLocation();
            if (cubeChunkLocation == null) continue;

            boolean sameChunk = cubeChunkLocation.getX() == loadedChunk.getX()
                    && cubeChunkLocation.getZ() == loadedChunk.getZ()
                    && loadedChunk.getWorld().equals(cubeChunkLocation.getWorld());

            if (!sameChunk) continue;

            cube.stop();
            cube.start();
        }
    }
}