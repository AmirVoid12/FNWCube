package iran.flame.network.cube.managers;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.gencube.CubeBuilder;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.yml.CubesStorage;
import iran.flame.network.cube.utils.Cuboid;

public class CubeManager {
    private final CubesStorage cubesStorage = new CubesStorage();
    private final CopyOnWriteArraySet<GenCube> cubes = new CopyOnWriteArraySet<>();
    private final CopyOnWriteArraySet<GenCube> nonLoadedCubes = new CopyOnWriteArraySet<>();

    public void load() {
        for (UUID uuid : this.cubesStorage.getCubesConfigs().keySet()) {
            YamlConfiguration config = this.cubesStorage.getCubesConfigs().get(uuid);
            CubeManager.loadCube(uuid, config);
        }
        for (UUID uuid : this.cubesStorage.getNonLoadedCubesConfigs().keySet()) {
            YamlConfiguration config = this.cubesStorage.getNonLoadedCubesConfigs().get(uuid);
            CubeManager.loadCube(uuid, config);
        }
    }

    private static void loadCube(UUID uuid, YamlConfiguration config) {
        String type = config.getString("type");
        String location = config.getString("location");
        BlockFace direction = BlockFace.valueOf(Objects.requireNonNull(config.getString("direction")).toUpperCase());
        UUID owner = UUID.fromString(Objects.requireNonNull(config.getString("owner")));
        Object[] inventoryContent = Objects.requireNonNull(config.getList("inventory")).toArray();
        Integer size = config.getInt("size");
        String linkedContainer = config.getString("linked-container");

        new CubeBuilder(type, location, direction)
                .setUUID(uuid)
                .setInventoryContent(inventoryContent)
                .setOwner(owner)
                .setSize(size)
                .setLinkedContainer(linkedContainer)
                .build();
    }

    public void register(GenCube cube) {
        if (cube.isLoaded()) {
            this.cubesStorage.save(cube);
            this.cubes.add(cube);
            return;
        }
        this.nonLoadedCubes.add(cube);
    }

    public void unRegister(GenCube cube) {
        if (cube.isLoaded()) {
            this.cubes.remove(cube);
            return;
        }
        this.nonLoadedCubes.remove(cube);
    }

    public Set<GenCube> getCubesByOwner(UUID owner) {
        HashSet<GenCube> result = new HashSet<>();
        for (GenCube cube : this.cubes) {
            UUID cubeOwner = cube.getOwner();
            if (cubeOwner == null || !cubeOwner.equals(owner)) continue;
            result.add(cube);
        }
        return result;
    }

    public GenCube getCubeByLocation(Location location, boolean loaded) {
        CopyOnWriteArraySet<GenCube> source = loaded ? this.cubes : this.nonLoadedCubes;
        for (GenCube cube : source) {
            Cuboid cuboid = cube.getMainCuboid();
            if (cuboid == null || !cuboid.contains(location)) continue;
            return cube;
        }
        return null;
    }

    public GenCube getCubeByUUID(UUID uuid) {
        for (GenCube cube : this.cubes) {
            if (!cube.getUuid().equals(uuid)) continue;
            return cube;
        }
        for (GenCube cube : this.nonLoadedCubes) {
            if (!cube.getUuid().equals(uuid)) continue;
            return cube;
        }
        return null;
    }

    public GenCube getCubeByLinkedContainer(Location location) {
        for (GenCube cube : this.cubes) {
            Location linked = cube.getLinkedContainer();
            if (linked == null || !linked.equals(location)) continue;
            return cube;
        }
        return null;
    }

    public GenCube getCubeTryingToPair(UUID uuid) {
        for (GenCube cube : this.cubes) {
            if (!cube.getPairingAttempts().contains(uuid)) continue;
            return cube;
        }
        return null;
    }

    public CubesStorage getCubesStorage() {
        return this.cubesStorage;
    }

    public CopyOnWriteArraySet<GenCube> getCubes() {
        return this.cubes;
    }

    public CopyOnWriteArraySet<GenCube> getNonLoadedCubes() {
        return this.nonLoadedCubes;
    }
}