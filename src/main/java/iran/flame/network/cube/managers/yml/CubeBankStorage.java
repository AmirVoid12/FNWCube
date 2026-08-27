package iran.flame.network.cube.managers.yml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.CubeBankAccount;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.utils.FileUtils;

public class CubeBankStorage {
    private final File bankFolder;
    private final Map<UUID, YamlConfiguration> accountConfigs = new HashMap<>();

    public CubeBankStorage() {
        this.bankFolder = new File(GenCubes.getInstance().getDataFolder().getPath() + File.separator + "bank");
        if (!this.bankFolder.exists()) {
            this.bankFolder.mkdirs();
        }
        this.loadAllAccountFiles();
    }

    private void loadAllAccountFiles() {
        File[] files = this.bankFolder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!FileUtils.getFileExtension(file.getName()).equals(".yml")) {
                continue;
            }

            String uuidString = FileUtils.removeExtension(file.getName());
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            this.accountConfigs.put(UUID.fromString(uuidString), config);
        }
    }

    public void save(CubeBankAccount account) {
        UUID owner = account.getOwner();
        File file = new File(this.bankFolder.getPath() + File.separator + owner.toString() + ".yml");
        if (!file.exists()) {
            FileUtils.create(file);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, ?> cubes = account.getCubes();

        if (config.isConfigurationSection("cubes")) {
            config.set("cubes", null);
        }

        for (String cubeId : cubes.keySet()) {
            config.set("cubes." + cubeId, cubes.get(cubeId));
        }

        FileUtils.save(file, config);
        this.accountConfigs.putIfAbsent(owner, config);
    }

    public void delete(UUID owner) {
        File file = new File(this.bankFolder.getPath() + File.separator + owner.toString() + ".yml");
        file.delete();
        this.accountConfigs.remove(owner);
    }

    public Map<UUID, YamlConfiguration> getAccountsConfigs() {
        return this.accountConfigs;
    }
}