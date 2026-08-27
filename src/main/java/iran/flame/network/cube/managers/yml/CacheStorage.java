package iran.flame.network.cube.managers.yml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.enums.CacheType;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.tasks.CubeClaimProcess;
import iran.flame.network.cube.tasks.edit.CubeEdition;
import iran.flame.network.cube.utils.FileUtils;

public class CacheStorage {
    private final Map<UUID, YamlConfiguration> cacheConfigs = new HashMap<>();
    private final File cacheFolder = new File(GenCubes.getInstance().getDataFolder().getPath() + File.separator + "cache");

    public CacheStorage() {
        if (!this.cacheFolder.exists()) {
            this.cacheFolder.mkdirs();
        }
        this.loadAllCacheFiles();
    }

    private void loadAllCacheFiles() {
        File[] files = this.cacheFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!FileUtils.getFileExtension(file.getName()).equals(".yml")) {
                continue;
            }

            String uuidString = FileUtils.removeExtension(file.getName());
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            this.cacheConfigs.put(UUID.fromString(uuidString), config);
        }
    }

    public void save(CubeEdition cubeEdition) {
        File file = new File(this.cacheFolder.getPath() + File.separator + UUID.randomUUID() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("type", CacheType.CUBE_EDITION.toString());
        config.set("cube", cubeEdition.getCube().getUuid().toString());
        config.set("edit-task", cubeEdition.getEditTask().toString());

        FileUtils.save(file, config);
    }

    public void save(CubeClaimProcess cubeClaimProcess) {
        File file = new File(this.cacheFolder.getPath() + File.separator + UUID.randomUUID() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("type", CacheType.CUBE_CLAIM.toString());
        config.set("cubes", cubeClaimProcess.getPendingCubes());
        config.set("player", cubeClaimProcess.getPlayerId().toString());

        FileUtils.save(file, config);
    }

    public void remove(UUID cacheId) {
        File file = new File(this.cacheFolder.getPath() + File.separator + cacheId + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public Map<UUID, YamlConfiguration> getCacheConfigs() {
        return this.cacheConfigs;
    }
}