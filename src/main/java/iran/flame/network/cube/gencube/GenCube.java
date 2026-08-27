package iran.flame.network.cube.gencube;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.managers.yml.Configuration;
import iran.flame.network.cube.enums.CompressionStatus;
import iran.flame.network.cube.enums.BuildingStatus;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.tasks.edit.CubeRegeneration;
import iran.flame.network.cube.utils.ChunkLocation;
import iran.flame.network.cube.utils.CubeUtils;
import iran.flame.network.cube.utils.Cuboid;
import iran.flame.network.cube.utils.LocationUtils;
import iran.flame.network.cube.utils.Range;
import iran.flame.network.cube.enums.Sound;

public class GenCube {
    private String type;
    private final BlockFace direction;
    private Cuboid mainCuboid;
    private Cuboid genCuboid;
    private Map<GenerationBlock, Range> genBlockRangeMap;
    private CubeRegeneration cubeRegeneration;
    private List<Block> borderBlocks;
    private UUID uuid;
    private Integer size;
    private UUID owner;
    private CubeInventory inventory;
    private final List<UUID> pairingAttempts;
    private final String serializedLocation;
    private final GenCubes plugin;
    private final Configuration configuration;
    private final DataManager dataManager;
    private final CubeManager cubeManager;
    private final TaskManager taskManager;
    private Location cachedLocation;
    private final Random random;
    private ChunkLocation chunkLocation;
    public String linkedContainerLocation;

    GenCube(String type, String serializedLocation, BlockFace direction) {
        this.type = type;
        this.serializedLocation = serializedLocation;
        this.direction = direction;
        this.inventory = new NormalInventory(this);
        this.plugin = GenCubes.getInstance();
        this.configuration = this.plugin.getConfiguration();
        this.dataManager = this.plugin.getDataManager();
        this.cubeManager = this.plugin.getCubeManager();
        this.taskManager = this.plugin.getTaskManager();
        this.borderBlocks = new ArrayList<>();
        this.pairingAttempts = new ArrayList<>();
        this.random = new Random();
    }

    void load(Object[] inventoryContent) {
        if (this.isLoaded()) {
            this.chunkLocation = new ChunkLocation(this.getLocation());

            this.recalculateCuboids();

            this.inventory.prepareInventory(inventoryContent);

            if (this.dataManager.isACube(this.type)) {
                this.recalculateGenBlockRanges();
                this.start();
                this.borderBlocks = CubeUtils.getBorderBlocks(this);
            }
        }
        this.cubeManager.register(this);
    }

    public void start() {
        if (this.isLoaded() && !this.isRunning()) {
            this.cubeRegeneration = new CubeRegeneration(this);
            this.taskManager.register(this.cubeRegeneration);
        }
    }

    public void stop() {
        if (this.isRunning()) {
            this.cubeRegeneration.stopTask();
            this.taskManager.unRegister(this.cubeRegeneration);
            this.cubeRegeneration = null;
        }
    }

    private void recalculateGenBlockRanges() {
        this.genBlockRangeMap = new HashMap<>();
        List<GenerationBlock> generationBlocks = this.dataManager.getGenerationBlocks(this.type);

        if (!generationBlocks.isEmpty()) {
            for (int i = 0; i <= generationBlocks.size() - 1; i++) {
                GenerationBlock currentBlock = generationBlocks.get(i);
                GenerationBlock previousBlock = i - 1 >= 0 ? generationBlocks.get(i - 1) : null;

                Range range;
                if (previousBlock != null) {
                    Range previousRange = this.genBlockRangeMap.get(previousBlock);
                    range = new Range(previousRange.high(), previousRange.high() + currentBlock.getPercentage());
                } else {
                    range = new Range(0.0, currentBlock.getPercentage());
                }

                this.genBlockRangeMap.put(currentBlock, range);
            }
        }
    }

    private void recalculateCuboids() {
        int expandAmount = this.size - 1;
        this.mainCuboid = new Cuboid(this.getLocation(), this.getLocation());
        this.mainCuboid = this.mainCuboid.expandForGenCube(expandAmount, this.direction);
        this.genCuboid = this.mainCuboid.expandAllDirections(-1);
    }

    public BuildingStatus build(RebuildType rebuildType, QuantityType quantityType, int quantityValue, boolean skipAnimation) {
        Location location = this.getLocation();
        if (location == null) {
            return null;
        }

        Location teleportDestination = location.clone().add(0.5, 1.0, 0.5);
        BuildingStatus status = this.plugin.getCubeEditorService()
                .build(this, rebuildType, quantityType, quantityValue, skipAnimation);

        boolean teleportOutsideOnRebuild = (Boolean) this.configuration.getOptions()
                .get("enhancement").get("teleport_outside_when_rebuild");

        if (teleportOutsideOnRebuild && status == BuildingStatus.SUCCESS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!this.genCuboid.contains(player.getLocation())) {
                    continue;
                }
                player.teleport(teleportDestination);
            }
        }

        return status;
    }

    public void remove() {
        this.cubeManager.unRegister(this);

        if (LocationUtils.verify(this.getLocation())) {
            this.inventory.onUnload();
            if (this.dataManager.isACube(this.type)) {
                this.stop();
            }
            this.plugin.getCubeEditorService().remove(this);
            return;
        }

        this.cubeManager.getCubesStorage().delete(this.getUuid(), false);
    }

    public boolean sell(Player player) {
        boolean hasPermission = player.hasPermission("gencubes.sell.*")
                || player.hasPermission("gencubes.sell." + this.getType());

        if (!hasPermission) {
            GenCubes.sendMessage(player, "&cYou don't have permissions to sell this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
            return false;
        }

        Double soldValue = this.plugin.getCubeSeller().sell(this, player);

        if (soldValue > 0.0) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String message = "&aSold cube contents for &6$" + decimalFormat.format(soldValue) + "&a!";
            GenCubes.sendMessage(player, message, false);
            player.playSound(player.getLocation(), Sound.LEVEL_UP.bukkitSound(), 100.0f, 1.0f);
            return true;
        }

        GenCubes.sendMessage(player, "&cThere's nothing to sell in this cube!", false);
        player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
        return false;
    }

    public boolean smelt(Player player) {
        boolean hasPermission = player.hasPermission("gencubes.smelt.*")
                || player.hasPermission("gencubes.smelt." + this.getType());

        if (!hasPermission) {
            GenCubes.sendMessage(player, "&cYou don't have permissions to smelt this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
            return false;
        }

        boolean smelted = this.plugin.getCubeSmelter().smelt(this);

        if (smelted) {
            GenCubes.sendMessage(player, "&aSmelted the contents of this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_USE.bukkitSound(), 100.0f, 1.0f);
        } else {
            GenCubes.sendMessage(player, "&cThere's nothing to smelt in this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
        }

        return smelted;
    }

    public boolean compress(Player player) {
        boolean hasPermission = player.hasPermission("gencubes.compress.*")
                || player.hasPermission("gencubes.compress." + this.getType());

        if (!hasPermission) {
            GenCubes.sendMessage(player, "&cYou don't have permissions to compress this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
            return false;
        }

        CompressionStatus status = this.plugin.getCubeCompressor().compress(this);

        if (status == CompressionStatus.SUCCESS) {
            GenCubes.sendMessage(player, "&aCompressed the contents of this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_USE.bukkitSound(), 100.0f, 1.0f);
            return true;
        }

        if (status == CompressionStatus.INVENTORY_FULL) {
            GenCubes.sendMessage(player, "&cNot enough space to compress!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
        } else if (status == CompressionStatus.NOTHING_TO_COMPRESS) {
            GenCubes.sendMessage(player, "&cThere's nothing to compress in this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
        }

        return false;
    }

    public boolean upgrade(Player player) {
        if (!this.isLoaded()) {
            return false;
        }

        boolean hasPermission = player.hasPermission("gencubes.upgrade.*")
                || player.hasPermission("gencubes.upgrade." + this.getType());

        if (!hasPermission) {
            GenCubes.sendMessage(player, "&cYou don't have permissions to upgrade this cube!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
            return false;
        }

        if (this.taskManager.getCubeEditionByCube(this) != null) {
            GenCubes.sendMessage(player, "&cThe cube is currently being edited!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
            return false;
        }

        this.type = this.dataManager.getNextUpgrade(this.getType());
        this.inventory.closeForAllViewers();
        this.inventory.prepareInventory(this.inventory.getContent());
        this.recalculateGenBlockRanges();

        this.cubeRegeneration.stopTask();
        this.taskManager.unRegister(this.cubeRegeneration);
        this.cubeRegeneration = new CubeRegeneration(this);
        this.taskManager.register(this.cubeRegeneration);

        this.plugin.getCubeEditorService().upgrade(this);

        GenCubes.sendMessage(player, "&aCube upgraded successfully!");
        player.playSound(player.getLocation(), Sound.LEVEL_UP.bukkitSound(), 100.0f, 1.0f);
        return true;
    }

    public void save() {
        this.cubeManager.getCubesStorage().save(this);
    }

    public Location getLocation() {
        if (this.serializedLocation != null && !this.serializedLocation.isEmpty()) {
            if (this.cachedLocation == null) {
                this.cachedLocation = LocationUtils.deserializeLoc(this.serializedLocation);
            }
            return this.cachedLocation;
        }
        return null;
    }

    public void setLinkedContainer(Block block) {
        this.linkedContainerLocation = block != null ? LocationUtils.serializeLoc(block.getLocation()) : null;
    }

    public Location getLinkedContainer() {
        if (this.linkedContainerLocation != null && !this.linkedContainerLocation.isEmpty()) {
            return LocationUtils.deserializeLoc(this.linkedContainerLocation);
        }
        return null;
    }

    public List<Block> getBlocksToRebuild(RebuildType rebuildType, QuantityType quantityType, int quantityValue) {
        List<Block> blocksToRebuild = new ArrayList<>();

        switch (rebuildType) {
            case LINEAL: {
                blocksToRebuild = this.genCuboid.getBlocksPercentageToRebuildGenCube(quantityType, quantityValue);
                break;
            }
            case RANDOM: {
                List<Block> airBlocks = new ArrayList<>();
                List<Block> allBlocks = this.genCuboid.getBlocks();

                for (Block block : allBlocks) {
                    if (block.getType() != Material.AIR) {
                        continue;
                    }
                    airBlocks.add(block);
                }

                Collections.shuffle(airBlocks, this.random);

                if (quantityType == QuantityType.PERCENTAGE) {
                    int amountByPercentage = quantityValue * allBlocks.size() / 100;
                    if (amountByPercentage >= airBlocks.size()) {
                        return airBlocks;
                    }
                    for (int i = 0; i <= amountByPercentage - 1; i++) {
                        blocksToRebuild.add(airBlocks.get(i));
                    }
                }

                if (quantityType == QuantityType.FIXED) {
                    for (int i = 0; i <= quantityValue - 1; i++) {
                        if (i > airBlocks.size() - 1) {
                            continue;
                        }
                        blocksToRebuild.add(airBlocks.get(i));
                    }
                }
                break;
            }
        }

        return blocksToRebuild;
    }

    public boolean isLoaded() {
        return LocationUtils.verify(this.getLocation()) && this.dataManager.isACube(this.getType());
    }

    public boolean isRunning() {
        return this.cubeRegeneration != null;
    }

    public String getType() {
        return this.type;
    }

    public BlockFace getDirection() {
        return this.direction;
    }

    public Cuboid getMainCuboid() {
        return this.mainCuboid;
    }

    public Cuboid getGenCuboid() {
        return this.genCuboid;
    }

    public Map<GenerationBlock, Range> getGenBlockRangeMap() {
        return this.genBlockRangeMap;
    }

    public CubeRegeneration getCubeRegeneration() {
        return this.cubeRegeneration;
    }

    public List<Block> getBorderBlocks() {
        return this.borderBlocks;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getSize() {
        return this.size;
    }

    void setSize(Integer size) {
        this.size = size;
    }

    public UUID getOwner() {
        return this.owner;
    }

    void setOwner(UUID owner) {
        this.owner = owner;
    }

    public CubeInventory getInventory() {
        return this.inventory;
    }

    void setInventory(CubeInventory inventory) {
        this.inventory = inventory;
    }

    public List<UUID> getPairingAttempts() {
        return this.pairingAttempts;
    }

    public ChunkLocation getChunkLocation() {
        return this.chunkLocation;
    }
}