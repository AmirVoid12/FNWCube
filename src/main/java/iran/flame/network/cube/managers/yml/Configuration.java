package iran.flame.network.cube.managers.yml;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.enums.SellPlugin;

public class Configuration {
    private final FileConfiguration config;
    private final Map<String, Map<String, Object>> options;
    private Map<String, Object> oldCubesSection;
    private final List<String> disabledWorlds;

    public Configuration(FileConfiguration fileConfiguration) {
        this.config = fileConfiguration;
        this.options = new HashMap<>();
        this.oldCubesSection = new HashMap<>();
        this.migrateLegacyOptions();
        this.disabledWorlds = fileConfiguration.getStringList("disabled-worlds");
        this.mergeMissingDefaults(loadBundledDefaultConfig());
        this.loadOptions();

        ConfigurationSection oldCubesConfigSection = fileConfiguration.getConfigurationSection("gencubes");
        if (oldCubesConfigSection != null) {
            this.oldCubesSection = oldCubesConfigSection.getValues(true);
            fileConfiguration.set("gencubes", null);
            GenCubes plugin = GenCubes.getInstance();
            plugin.saveConfig();
        }
    }

    private void loadOptions() {
        ConfigurationSection optionsSection = this.config.getConfigurationSection("options");

        assert optionsSection != null;
        for (String categoryKey : optionsSection.getKeys(false)) {
            ConfigurationSection categorySection = optionsSection.getConfigurationSection(categoryKey);
            Map<String, Object> categoryValues = new HashMap<>();

            assert categorySection != null;
            for (String optionKey : categorySection.getKeys(false)) {
                Object value = categorySection.get(optionKey);
                categoryValues.put(optionKey, value);
            }

            this.options.put(categoryKey, categoryValues);
        }
    }
    private void migrateLegacyOptions() {
        if (this.config.isSet("options.fastasyncworldedit.show_block_animation")) {
            boolean showAnimation = this.config.getBoolean("options.fastasyncworldedit.show_block_animation");
            this.config.set("options.fastasyncworldedit.show_block_animation_on_building", showAnimation);
            this.config.set("options.fastasyncworldedit.show_block_animation_on_regeneration", showAnimation);
            this.config.set("options.fastasyncworldedit.show_block_animation", null);
        }

        if (!this.config.isSet("disabled-worlds")) {
            this.config.set("disabled-worlds", Arrays.asList("example_world1", "example_world2"));
        }

        GenCubes.getInstance().saveConfig();
    }

    private void mergeMissingDefaults(YamlConfiguration defaultsConfig) {
        ConfigurationSection defaultsSection = defaultsConfig.getConfigurationSection("options");

        assert defaultsSection != null;
        for (String key : defaultsSection.getKeys(true)) {
            String fullPath = "options" + "." + key;
            if (this.config.isSet(fullPath)) {
                continue;
            }
            this.config.createSection(fullPath);
            this.config.set(fullPath, defaultsSection.get(key));
        }

        GenCubes.getInstance().saveConfig();
    }

    public SellPlugin getSellsPlugin() {
        Map<String, Object> enhancementOptions = this.options.get("enhancement");
        try {
            return SellPlugin.valueOf(((String) enhancementOptions.get("sells_plugin")).toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getBlockBatchEditRate() {
        long rate = (Integer) this.options.get("edition").get("block_batch_edit_rate");
        return Math.max(rate, 1L);
    }

    private static YamlConfiguration loadBundledDefaultConfig() {
        YamlConfiguration defaultsConfig = null;
        try {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(GenCubes.getInstance().getResource("config.yml")), StandardCharsets.UTF_8);
            defaultsConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultsConfig;
    }

    public Map<String, Map<String, Object>> getOptions() {
        return this.options;
    }

    public Map<String, Object> getOldCubesSection() {
        return this.oldCubesSection;
    }

    public List<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }
}