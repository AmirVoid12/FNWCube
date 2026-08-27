package iran.flame.network.cube.managers;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.enums.CacheType;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.yml.CacheStorage;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.tasks.CubeClaimProcess;
import iran.flame.network.cube.enums.EditTask;

public class CacheManager {
    private final CacheStorage cacheStorage = new CacheStorage();
    private final TaskManager taskManager;
    private final CubeManager cubeManager;

    public CacheManager() {
        GenCubes plugin = GenCubes.getInstance();
        this.cubeManager = plugin.getCubeManager();
        this.taskManager = plugin.getTaskManager();
    }

    public void load() {
        for (UUID uuid : this.cacheStorage.getCacheConfigs().keySet()) {
            YamlConfiguration config = this.cacheStorage.getCacheConfigs().get(uuid);
            CacheType cacheType = CacheType.valueOf(config.getString("type"));

            if (cacheType == CacheType.CUBE_EDITION) {
                GenCube cube = this.cubeManager.getCubeByUUID(UUID.fromString(Objects.requireNonNull(config.getString("cube"))));
                EditTask editTask = EditTask.valueOf(config.getString("edit-task"));
                QuantityType quantityType = QuantityType.valueOf(config.getString("edit-task-quantity-type", "PERCENTAGE"));
                int quantityValue = config.getInt("edit-task-quantity-value", 100);

                if (editTask == EditTask.REMOVE) {
                    cube.build(RebuildType.RANDOM, quantityType, quantityValue, true);
                }
                if (editTask == EditTask.ADD) {
                    cube.build(RebuildType.LINEAL, quantityType, quantityValue, true);
                }
                if (editTask == EditTask.SET) {
                    cube.remove();
                }
            }

            if (cacheType == CacheType.CUBE_CLAIM) {
                List<String> cubes = config.getStringList("cubes");
                UUID player = UUID.fromString(Objects.requireNonNull(config.getString("player")));
                this.taskManager.register(new CubeClaimProcess(player, cubes));
            }

            this.cacheStorage.remove(uuid);
        }
    }

    public CacheStorage getCacheStorage() {
        return this.cacheStorage;
    }
}