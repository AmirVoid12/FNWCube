package iran.flame.network.cube.managers;

import java.util.List;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.BaseBlock;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.enums.CubeAttribute;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.managers.yml.CubeConfiguration;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;

public class DataManager {
    private final CubeConfiguration cubeConfiguration = GenCubes.getInstance().getCubeConfiguration();

    public Integer getSize(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.SIZE);
        }
        return null;
    }

    public BaseBlock getBorderBlock(String cubeName) {
        if (this.isACube(cubeName)) {
            return (BaseBlock) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.BORDER_BLOCK);
        }
        return null;
    }

    public long getRegenerationTime(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Long) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REGENERATION_TIMING);
        }
        return 0L;
    }

    public QuantityType getRegenerationQuantityType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (QuantityType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REGENERATION_QUANTITY_TYPE);
        }
        return null;
    }

    public int getRegenerationQuantityValue(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REGENERATION_QUANTITY_VALUE);
        }
        return 0;
    }

    public RebuildType getRegenerationType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (RebuildType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REGENERATION_TYPE);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<GenerationBlock> getGenerationBlocks(String cubeName) {
        if (this.isACube(cubeName)) {

            return (List<GenerationBlock>) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.GENERATION_BLOCKS);
        }
        return null;
    }

    public ItemStack getIcon(String cubeName) {
        if (this.isACube(cubeName)) {
            return ((ItemStack) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.DISPLAY_ITEM)).clone();
        }
        return null;
    }

    public String getInventoryName(String cubeName) {
        if (this.isACube(cubeName)) {
            return (String) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.INVENTORY_NAME);
        }
        return "";
    }

    public InventoryType getInventoryType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (InventoryType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.INVENTORY_TYPE);
        }
        return null;
    }

    public boolean getAutoMinerAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.AUTOMINER_ENABLED);
    }

    public long getAutoMinerTiming(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Long) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.AUTOMINER_TIMING);
        }
        return 30L;
    }

    public int getAutoMinerBatchMin(String cubeName) {
        if (this.isACube(cubeName) && this.cubeConfiguration.getDataMap().get(cubeName).containsKey(CubeAttribute.AUTOMINER_BATCH_MIN)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.AUTOMINER_BATCH_MIN);
        }
        return 1;
    }

    public int getAutoMinerBatchMax(String cubeName) {
        if (this.isACube(cubeName) && this.cubeConfiguration.getDataMap().get(cubeName).containsKey(CubeAttribute.AUTOMINER_BATCH_MAX)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.AUTOMINER_BATCH_MAX);
        }
        return 4;
    }

    public boolean getRebuildAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REBUILD_ENABLED);
    }

    public double getRebuildPrice(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Double) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REBUILD_PRICE);
        }
        return 0.0;
    }

    public QuantityType getRebuildQuantityType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (QuantityType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REBUILD_QUANTITY_TYPE);
        }
        return null;
    }

    public int getRebuildQuantityValue(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REBUILD_QUANTITY_VALUE);
        }
        return 0;
    }

    public RebuildType getRebuildType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (RebuildType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REBUILD_TYPE);
        }
        return null;
    }

    public boolean getSmeltingAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.SMELT_ENABLED);
    }

    public double getSmeltingPrice(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Double) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.SMELT_PRICE);
        }
        return 0.0;
    }

    public boolean getSellsAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.SELLS_ENABLED);
    }

    public QuantityType getBuildQuantityType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (QuantityType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.BUILD_QUANTITY_TYPE);
        }
        return null;
    }

    public int getBuildQuantityValue(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Integer) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.BUILD_QUANTITY_VALUE);
        }
        return 0;
    }

    public RebuildType getBuildType(String cubeName) {
        if (this.isACube(cubeName)) {
            return (RebuildType) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.BUILD_TYPE);
        }
        return null;
    }

    public boolean getInventoryLinkingAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.INVENTORY_LINKING_ENABLED);
    }

    public boolean getUpgradeAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.UPGRADES_ENABLED);
    }

    public double getUpgradePrice(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Double) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.UPGRADE_PRICE);
        }
        return 0.0;
    }

    public String getNextUpgrade(String cubeName) {
        if (this.isACube(cubeName)) {
            return (String) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.NEXT_UPGRADE_ID);
        }
        return "";
    }

    public double getRemovePrice(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Double) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.REMOVE_PRICE);
        }
        return 0.0;
    }

    public boolean getCompressorAvailability(String cubeName) {
        return this.isACube(cubeName) && (Boolean) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.COMPRESSOR_ENABLED);
    }

    public double getCompressorPrice(String cubeName) {
        if (this.isACube(cubeName)) {
            return (Double) this.cubeConfiguration.getDataMap().get(cubeName).get(CubeAttribute.COMPRESSOR_PRICE);
        }
        return 0.0;
    }

    public Set<String> getCubesNames() {
        return this.cubeConfiguration.getDataMap().keySet();
    }

    public boolean isACube(String cubeName) {
        return this.cubeConfiguration.getDataMap().containsKey(cubeName);
    }

    public boolean isANonLoadedCube(String cubeName) {
        return this.cubeConfiguration.getNonLoaded().contains(cubeName);
    }
}