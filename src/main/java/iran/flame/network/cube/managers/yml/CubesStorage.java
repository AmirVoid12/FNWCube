package iran.flame.network.cube.managers.yml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.utils.FileUtils;
import iran.flame.network.cube.utils.LocationUtils;

public class CubesStorage {
    private final File dataFolder;
    private final DataManager dataManager;
    private final Map<UUID, YamlConfiguration> loadedCubeConfigs = new HashMap<>();
    private final Map<UUID, YamlConfiguration> nonLoadedCubeConfigs = new HashMap<>();

    public CubesStorage() {
        this.dataFolder = new File(GenCubes.getInstance().getDataFolder().getPath() + File.separator + "data");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
        this.dataManager = GenCubes.getInstance().getDataManager();
        this.loadAllCubeFiles();
    }

    private void loadAllCubeFiles() {
        File[] files = this.dataFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!FileUtils.getFileExtension(file.getName()).equals("yml")) {
                continue;
            }

            String uuidString = FileUtils.removeExtension(file.getName());
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            boolean isInvalid = !this.dataManager.isACube(config.getString("type"))
                    || this.dataManager.isANonLoadedCube(config.getString("type"))
                    || LocationUtils.deserializeLoc(Objects.requireNonNull(config.getString("location"))).getWorld() == null;

            if (isInvalid) {
                this.nonLoadedCubeConfigs.put(UUID.fromString(uuidString), YamlConfiguration.loadConfiguration(file));
                continue;
            }

            this.loadedCubeConfigs.put(UUID.fromString(uuidString), YamlConfiguration.loadConfiguration(file));
        }
    }

    public void save(GenCube genCube) {
        File file = new File(this.dataFolder.getPath() + File.separator + genCube.getUuid().toString() + ".yml");
        if (!file.exists()) {
            FileUtils.create(file);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("type", genCube.getType());
        config.set("size", genCube.getSize());

        if (LocationUtils.verify(genCube.getLocation())) {
            config.set("location", LocationUtils.serializeLoc(genCube.getLocation()));
        }

        config.set("direction", genCube.getDirection().toString());

        if (genCube.getOwner() != null) {
            config.set("owner", genCube.getOwner().toString());
        }

        if (genCube.getLinkedContainer() != null) {
            config.set("linked-container", LocationUtils.serializeLoc(genCube.getLinkedContainer()));
        }

        CubeInventory cubeInventory = genCube.getInventory();
        config.set("inventory-type", cubeInventory.getType().toString());

        if (genCube.isLoaded()) {
            config.set("inventory", cubeInventory.getContent());
        }

        FileUtils.save(file, config);
    }

    public void delete(UUID cubeId, boolean isLoaded) {
        if (isLoaded) {
            this.loadedCubeConfigs.remove(cubeId);
        } else {
            this.nonLoadedCubeConfigs.remove(cubeId);
        }

        File file = new File(this.dataFolder.getPath() + File.separator + cubeId.toString() + ".yml");
        file.delete();
    }

    public Map<UUID, YamlConfiguration> getCubesConfigs() {
        return this.loadedCubeConfigs;
    }

    public Map<UUID, YamlConfiguration> getNonLoadedCubesConfigs() {
        return this.nonLoadedCubeConfigs;
    }
}