package iran.flame.network.cube.managers.yml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.BaseBlock;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.enums.CubeAttribute;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.utils.FileUtils;
import iran.flame.network.cube.enums.XMaterial;
import iran.flame.network.cube.utils.builders.ItemBuilder;
import iran.flame.network.cube.utils.builders.SkullBuilder;

public class CubeConfiguration {
    private final GenCubes plugin = GenCubes.getInstance();
    private File cubesFile;
    private YamlConfiguration cubesYaml;
    private final Map<String, Map<CubeAttribute, Object>> cubeDataMap = new HashMap<>();
    private final List<String> nonLoadedCubeIds = new ArrayList<>();

    public CubeConfiguration() {
        this.setupCubesFile();
        this.migrateOldCubesSection();
        this.loadCubes();
    }

    private void setupCubesFile() {
        InputStream resourceStream = this.plugin.getResource("cubes.yml");

        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdirs();
        }

        this.cubesFile = new File(this.plugin.getDataFolder().getPath() + File.separator + "cubes.yml");

        if (!this.cubesFile.exists()) {
            try {
                this.cubesFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(this.cubesFile);
                assert resourceStream != null;
                CubeConfiguration.copyStream(resourceStream, fileOutputStream);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        this.cubesYaml = YamlConfiguration.loadConfiguration(this.cubesFile);
    }

    private void migrateOldCubesSection() {
        Configuration mainConfig = this.plugin.getConfiguration();
        Map<String, Object> oldCubesSection = mainConfig.getOldCubesSection();

        if (oldCubesSection != null && !oldCubesSection.isEmpty()) {
            this.cubesYaml.set("gencubes", null);
            for (String key : oldCubesSection.keySet()) {
                Object value = oldCubesSection.get(key);
                this.cubesYaml.set("gencubes." + key, value);
            }
            FileUtils.save(this.cubesFile, this.cubesYaml);
        }
    }

    private void loadCubes() {
        ConfigurationSection cubesSection = this.cubesYaml.getConfigurationSection("gencubes");

        assert cubesSection != null;
        for (String cubeId : cubesSection.getKeys(false)) {
            Map<CubeAttribute, Object> attributes = new HashMap<>();
            ConfigurationSection cubeSection = cubesSection.getConfigurationSection(cubeId);

            assert cubeSection != null;
            int size = cubeSection.getInt("size");
            if (size % 2 == 0 || size <= 0) {
                CubeConfiguration.warn("&7Invalid size for the GenCube&8: &6" + cubeId);
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }

            Optional<XMaterial> borderMaterial = XMaterial.matchXMaterial(Objects.requireNonNull(cubeSection.getString("border.material")));
            if (borderMaterial.isEmpty() || borderMaterial.get().parseMaterial() == null) {
                CubeConfiguration.warn("&7Invalid border material for the GenCube&8: &6" + cubeId);
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }
            if (!borderMaterial.get().parseMaterial().isBlock()) {
                CubeConfiguration.warn("&7Invalid border material&8: &c" + borderMaterial + " &7for the GenCube&8: &6" + cubeId);
                continue;
            }

            int borderData = cubeSection.getInt("border.data");
            BaseBlock borderBlock = new BaseBlock(borderMaterial.get().parseMaterial(), (short) borderData);

            if (cubeSection.isSet("regeneration-every")) {
                long oldRegenEvery = cubeSection.getLong("regeneration-every");
                cubeSection.set("regeneration-every", null);
                cubeSection.set("regeneration.timing", oldRegenEvery);
                cubeSection.set("regeneration.quantity.type", "PERCENTAGE");
                cubeSection.set("regeneration.quantity.value", 100);
                FileUtils.save(this.cubesFile, this.cubesYaml);
            }

            this.setDefaultIfAbsent(cubeSection, "remove-price", 0);
            double removePrice = cubeSection.getDouble("remove-price", 0.0);

            this.setDefaultIfAbsent(cubeSection, "regeneration.type", "RANDOM");
            RebuildType regenerationType;
            try {
                regenerationType = RebuildType.valueOf(cubeSection.getString("regeneration.type", "RANDOM"));
            } catch (IllegalArgumentException e) {
                regenerationType = RebuildType.LINEAL;
            }

            long regenerationTiming = cubeSection.getLong("regeneration.timing");

            if (cubeSection.isSet("regeneration.percentage")) {
                int oldPercentage = cubeSection.getInt("regeneration.percentage");
                cubeSection.set("regeneration.percentage", null);
                cubeSection.set("regeneration.quantity.type", "PERCENTAGE");
                cubeSection.set("regeneration.quantity.value", oldPercentage);
                FileUtils.save(this.cubesFile, this.cubesYaml);
            }

            QuantityType regenerationQuantityType = QuantityType.valueOf(Objects.requireNonNull(cubeSection.getString("regeneration.quantity.type")).toUpperCase());
            int regenerationQuantityValue = cubeSection.getInt("regeneration.quantity.value");
            if (regenerationQuantityType == QuantityType.PERCENTAGE && (regenerationQuantityValue > 100 || regenerationQuantityValue < 0)) {
                regenerationQuantityValue = 100;
            }

            if (regenerationTiming < 0L) {
                CubeConfiguration.warn("&7Invalid regeneration time for the GenCube&8: &6" + cubeId);
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }

            List<GenerationBlock> generationBlocks = new ArrayList<>();
            ConfigurationSection generationSection = cubeSection.getConfigurationSection("generation");

            assert generationSection != null;
            for (String materialKey : generationSection.getKeys(false)) {
                ConfigurationSection blockSection = generationSection.getConfigurationSection(materialKey);
                Optional<XMaterial> generationMaterial = XMaterial.matchXMaterial(materialKey);

                if (generationMaterial.isEmpty() || generationMaterial.get().parseMaterial() == null) {
                    CubeConfiguration.warn("&7Invalid generation material&8: &c" + materialKey + " &7for the GenCube&8: &6" + cubeId);
                    continue;
                }
                if (!generationMaterial.get().parseMaterial().isBlock()) {
                    CubeConfiguration.warn("&7Invalid generation material&8: &c" + generationMaterial + " &7for the GenCube&8: &6" + cubeId);
                    continue;
                }

                assert blockSection != null;
                double percentage = blockSection.getDouble("percentage");
                XMaterial matchedMaterial = generationMaterial.get();
                GenerationBlock newBlock = new GenerationBlock(matchedMaterial.parseMaterial(), matchedMaterial.getData(), percentage);

                boolean merged = false;
                for (GenerationBlock existingBlock : generationBlocks) {
                    if (newBlock.isSimilarTo(existingBlock)) {
                        existingBlock.setPercentage(existingBlock.getPercentage() + newBlock.getPercentage());
                        merged = true;
                    }
                }
                if (!merged) {
                    generationBlocks.add(newBlock);
                }
            }

            if (generationBlocks.isEmpty()) {
                CubeConfiguration.warn("&7GenCube&8: &6" + cubeId + " &7needs to have at least 1 Generation block&6!");
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }

            String itemMaterialKey = cubeSection.getString("item.material");
            assert itemMaterialKey != null;
            Optional<XMaterial> itemMaterial = XMaterial.matchXMaterial(itemMaterialKey);
            if (!itemMaterial.isPresent() || itemMaterial.get().parseMaterial() == null) {
                CubeConfiguration.warn("&7Invalid item material for the GenCube&8: &6" + cubeId);
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }

            String skullOwner = itemMaterial.get() == XMaterial.PLAYER_HEAD ? cubeSection.getString("item.head") : null;
            String itemName = cubeSection.getString("item.name");
            List<String> itemLore = cubeSection.getStringList("item.lore");

            ItemStack cubeItem = skullOwner == null
                    ? new ItemBuilder(itemMaterial.get().parseMaterial()).setName(itemName).setLore(itemLore).build()
                    : new SkullBuilder().setOwner(skullOwner).setName(itemName).setLore(itemLore).build();

            if (cubeSection.isSet("inventory-name")) {
                String oldInventoryName = cubeSection.getString("inventory-name");
                cubeSection.set("inventory-name", null);
                cubeSection.set("inventory.name", oldInventoryName);
                cubeSection.set("inventory.type", "NORMAL");
                FileUtils.save(this.cubesFile, this.cubesYaml);
            }

            String inventoryName = cubeSection.getString("inventory.name");
            InventoryType inventoryType = InventoryType.valueOf(cubeSection.getString("inventory.type", "NORMAL").toUpperCase());

            assert inventoryName != null;
            if (inventoryName.length() > 32) {
                CubeConfiguration.warn("&7The inventory name can be greater that 32 characters for GenCube&8: &6" + cubeId);
                this.nonLoadedCubeIds.add(cubeId);
                continue;
            }

            this.setDefaultIfAbsent(cubeSection, "rebuild.enable", Boolean.TRUE);
            this.setDefaultIfAbsent(cubeSection, "rebuild.price", 0);
            this.setDefaultIfAbsent(cubeSection, "rebuild.type", "LINEAL");
            this.setDefaultIfAbsent(cubeSection, "smelt.enable", Boolean.TRUE);
            this.setDefaultIfAbsent(cubeSection, "smelt.price", 0);
            this.setDefaultIfAbsent(cubeSection, "sells.enable", Boolean.TRUE);

            boolean rebuildEnabled = cubeSection.getBoolean("rebuild.enable", true);
            double rebuildPrice = cubeSection.getDouble("rebuild.price", 0.0);

            if (cubeSection.isSet("rebuild.percentage")) {
                int oldRebuildPercentage = cubeSection.getInt("rebuild.percentage");
                cubeSection.set("rebuild.percentage", null);
                cubeSection.set("rebuild.quantity.type", "PERCENTAGE");
                cubeSection.set("rebuild.quantity.value", oldRebuildPercentage);
                FileUtils.save(this.cubesFile, this.cubesYaml);
            }

            QuantityType rebuildQuantityType = QuantityType.valueOf(Objects.requireNonNull(cubeSection.getString("rebuild.quantity.type")).toUpperCase());
            int rebuildQuantityValue = cubeSection.getInt("rebuild.quantity.value");
            if (rebuildQuantityType == QuantityType.PERCENTAGE && (rebuildQuantityValue > 100 || rebuildQuantityValue < 0)) {
                rebuildQuantityValue = 100;
            }

            RebuildType rebuildType;
            try {
                rebuildType = RebuildType.valueOf(cubeSection.getString("rebuild.type", "LINEAL"));
            } catch (IllegalArgumentException e) {
                rebuildType = RebuildType.LINEAL;
            }

            boolean smeltEnabled = cubeSection.getBoolean("smelt.enable", true);
            double smeltPrice = cubeSection.getDouble("smelt.price", 0.0);
            boolean sellsEnabled = cubeSection.getBoolean("sells.enable", true);

            this.setDefaultIfAbsent(cubeSection, "build.type", "LINEAL");

            if (cubeSection.isSet("build.percentage")) {
                int oldBuildPercentage = cubeSection.getInt("build.percentage");
                cubeSection.set("build.percentage", null);
                cubeSection.set("build.quantity.type", "PERCENTAGE");
                cubeSection.set("build.quantity.value", oldBuildPercentage);
                FileUtils.save(this.cubesFile, this.cubesYaml);
            }

            QuantityType buildQuantityType = QuantityType.valueOf(Objects.requireNonNull(cubeSection.getString("build.quantity.type")).toUpperCase());
            int buildQuantityValue = cubeSection.getInt("build.quantity.value");
            if (buildQuantityType == QuantityType.PERCENTAGE && (buildQuantityValue > 100 || buildQuantityValue < 0)) {
                buildQuantityValue = 100;
            }

            RebuildType buildType;
            try {
                buildType = RebuildType.valueOf(cubeSection.getString("build.type", "LINEAL"));
            } catch (IllegalArgumentException e) {
                buildType = RebuildType.LINEAL;
            }

            this.setDefaultIfAbsent(cubeSection, "inventory-linking.enable", Boolean.FALSE);
            boolean inventoryLinkingEnabled = cubeSection.getBoolean("inventory-linking.enable");

            this.setDefaultIfAbsent(cubeSection, "upgrades.enable", Boolean.FALSE);
            this.setDefaultIfAbsent(cubeSection, "upgrades.price", 0);
            this.setDefaultIfAbsent(cubeSection, "upgrades.next-upgrade", "");

            boolean upgradesEnabled = cubeSection.getBoolean("upgrades.enable", false);
            double upgradePrice = cubeSection.getDouble("upgrades.price", 0.0);
            String nextUpgradeId = cubeSection.getString("upgrades.next-upgrade", "");

            if (upgradesEnabled) {
                if (!cubesSection.getKeys(false).contains(nextUpgradeId) || cubeId.equals(nextUpgradeId)) {
                    CubeConfiguration.warn("&7Invalid upgrade for cube&8: &6" + cubeId);
                    this.nonLoadedCubeIds.add(cubeId);
                    continue;
                }

                ConfigurationSection nextUpgradeSection = this.cubesYaml.getConfigurationSection("gencubes." + nextUpgradeId);
                assert nextUpgradeSection != null;
                int nextUpgradeSize = nextUpgradeSection.getInt("size");
                if (size != nextUpgradeSize) {
                    CubeConfiguration.warn("&7The size of the upgrade should be the same as the cube&8: &6" + cubeId);
                    this.nonLoadedCubeIds.add(cubeId);
                    continue;
                }

                ConfigurationSection nextUpgradeInventorySection = this.cubesYaml.getConfigurationSection("gencubes." + nextUpgradeId);
                assert nextUpgradeInventorySection != null;
                InventoryType nextUpgradeInventoryType = InventoryType.valueOf(nextUpgradeInventorySection.getString("inventory.type", "NORMAL"));
                if (inventoryType != nextUpgradeInventoryType) {
                    CubeConfiguration.warn("&7The inventory type of the upgrade should be the same as the cube&8: &6" + cubeId);
                    this.nonLoadedCubeIds.add(cubeId);
                    continue;
                }
            }

            this.setDefaultIfAbsent(cubeSection, "compressor.enable", Boolean.FALSE);
            this.setDefaultIfAbsent(cubeSection, "compressor.price", 0);
            boolean compressorEnabled = cubeSection.getBoolean("compressor.enable", false);
            double compressorPrice = cubeSection.getDouble("compressor.price", 0.0);

            this.setDefaultIfAbsent(cubeSection, "autominer.enable", Boolean.FALSE);
            this.setDefaultIfAbsent(cubeSection, "autominer.timing", 30);
            boolean autoMinerEnabled = cubeSection.getBoolean("autominer.enable", false);
            long autoMinerTiming = cubeSection.getLong("autominer.timing", 30L);
            if (autoMinerTiming <= 0L) {
                autoMinerTiming = 30L;
            }

            attributes.put(CubeAttribute.INVENTORY_NAME, inventoryName);
            attributes.put(CubeAttribute.INVENTORY_TYPE, inventoryType);
            attributes.put(CubeAttribute.ID, cubeId);
            attributes.put(CubeAttribute.SIZE, size);
            attributes.put(CubeAttribute.BORDER_BLOCK, borderBlock);
            attributes.put(CubeAttribute.REMOVE_PRICE, removePrice);
            attributes.put(CubeAttribute.REGENERATION_TIMING, regenerationTiming);
            attributes.put(CubeAttribute.REGENERATION_QUANTITY_TYPE, regenerationQuantityType);
            attributes.put(CubeAttribute.REGENERATION_QUANTITY_VALUE, regenerationQuantityValue);
            attributes.put(CubeAttribute.REGENERATION_TYPE, regenerationType);
            attributes.put(CubeAttribute.GENERATION_BLOCKS, generationBlocks);
            attributes.put(CubeAttribute.DISPLAY_ITEM, cubeItem);
            attributes.put(CubeAttribute.REBUILD_ENABLED, rebuildEnabled);
            attributes.put(CubeAttribute.REBUILD_PRICE, rebuildPrice);
            attributes.put(CubeAttribute.REBUILD_QUANTITY_TYPE, rebuildQuantityType);
            attributes.put(CubeAttribute.REBUILD_QUANTITY_VALUE, rebuildQuantityValue);
            attributes.put(CubeAttribute.REBUILD_TYPE, rebuildType);
            attributes.put(CubeAttribute.SMELT_ENABLED, smeltEnabled);
            attributes.put(CubeAttribute.SMELT_PRICE, smeltPrice);
            attributes.put(CubeAttribute.SELLS_ENABLED, sellsEnabled);
            attributes.put(CubeAttribute.BUILD_QUANTITY_TYPE, buildQuantityType);
            attributes.put(CubeAttribute.BUILD_QUANTITY_VALUE, buildQuantityValue);
            attributes.put(CubeAttribute.BUILD_TYPE, buildType);
            attributes.put(CubeAttribute.INVENTORY_LINKING_ENABLED, inventoryLinkingEnabled);
            attributes.put(CubeAttribute.UPGRADES_ENABLED, upgradesEnabled);
            attributes.put(CubeAttribute.NEXT_UPGRADE_ID, nextUpgradeId);
            attributes.put(CubeAttribute.UPGRADE_PRICE, upgradePrice);
            attributes.put(CubeAttribute.COMPRESSOR_ENABLED, compressorEnabled);
            attributes.put(CubeAttribute.COMPRESSOR_PRICE, compressorPrice);
            attributes.put(CubeAttribute.AUTOMINER_ENABLED, autoMinerEnabled);
            attributes.put(CubeAttribute.AUTOMINER_TIMING, autoMinerTiming);

            this.cubeDataMap.put(cubeId, attributes);
        }
    }

    private static void copyStream(InputStream input, FileOutputStream output) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try (input; output) {
            try {
                while ((bytesRead = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultIfAbsent(ConfigurationSection section, String path, Object defaultValue) {
        if (!section.isSet(path)) {
            section.set(path, defaultValue);
            FileUtils.save(this.cubesFile, this.cubesYaml);
        }
    }

    private boolean isSameSizeAsCube(int size, String cubeId) {
        ConfigurationSection section = this.cubesYaml.getConfigurationSection("gencubes." + cubeId);
        assert section != null;
        return size == section.getInt("size");
    }

    private boolean isSameInventoryTypeAsCube(InventoryType inventoryType, String cubeId) {
        ConfigurationSection section = this.cubesYaml.getConfigurationSection("gencubes." + cubeId);
        assert section != null;
        InventoryType otherType = InventoryType.valueOf(section.getString("inventory.type", "NORMAL"));
        return inventoryType == otherType;
    }

    private static void warn(String message) {
        GenCubes.sendMessage(Bukkit.getConsoleSender(), "&4&lWARNING " + message);
    }

    public Map<String, Map<CubeAttribute, Object>> getDataMap() {
        return this.cubeDataMap;
    }

    public List<String> getNonLoaded() {
        return this.nonLoadedCubeIds;
    }
}