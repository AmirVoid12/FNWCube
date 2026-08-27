package iran.flame.network.cube.services.autominer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.tasks.PluginTask;
import iran.flame.network.cube.utils.ContainerUtils;
import iran.flame.network.cube.utils.Cuboid;
import iran.flame.network.cube.utils.inventory.Icon;

public class AutoMinerTask extends PluginTask {
    private final GenCubes plugin;
    private final GenCube cube;
    private final DataManager dataManager;
    private final Random random = new Random();
    private long nextBatchTick = 0L;
    private long tickCounter = 0L;

    public AutoMinerTask(GenCube cube) {
        super(1L, true);
        this.plugin = GenCubes.getInstance();
        this.dataManager = this.plugin.getDataManager();
        this.cube = cube;
        this.setDelay(1L);
        this.runTask();
    }

    @Override
    public void run() {
        this.tickCounter++;

        if (!this.cube.isLoaded() || this.cube.getChunkLocation() == null || !this.cube.getChunkLocation().isLoaded()) {
            this.plugin.getAutoMinerManager().unRegister(this.cube);
            return;
        }

        if (this.tickCounter < this.nextBatchTick) {
            return;
        }

        if (!hasAvailableSpace()) {
            return;
        }

        Cuboid genCuboid = this.cube.getGenCuboid();
        List<Block> minableBlocks = new ArrayList<>();

        for (Block block : genCuboid.getBlocks()) {
            if (block.getType() == Material.AIR) continue;
            minableBlocks.add(block);
        }

        if (minableBlocks.isEmpty()) {
            scheduleNextBatch();
            return;
        }

        int batchMin = this.dataManager.getAutoMinerBatchMin(this.cube.getType());
        int batchMax = this.dataManager.getAutoMinerBatchMax(this.cube.getType());
        int batchSize = batchMin >= batchMax ? batchMin : batchMin + this.random.nextInt(batchMax - batchMin + 1);

        for (int i = 0; i < batchSize; i++) {
            if (minableBlocks.isEmpty()) break;
            if (!hasAvailableSpace()) break;

            int index = this.random.nextInt(minableBlocks.size());
            Block targetBlock = minableBlocks.remove(index);
            boolean broken = breakBlock(targetBlock);
            if (!broken) break;
        }

        scheduleNextBatch();
    }

    private void scheduleNextBatch() {
        long baseDelay = this.dataManager.getAutoMinerTiming(this.cube.getType());
        long extraDelay = baseDelay + this.random.nextInt((int) Math.max(baseDelay, 1L) * 2);
        this.nextBatchTick = this.tickCounter + extraDelay;
    }

    private boolean hasAvailableSpace() {
        boolean linkedFull = true;
        Location linkedLocation = this.cube.getLinkedContainer();

        if (linkedLocation != null) {
            BlockState containerState = linkedLocation.getBlock().getState();
            Inventory containerInventory = ContainerUtils.getContainerInventory(containerState);

            if (containerInventory == null) {
                this.cube.setLinkedContainer(null);
                Icon linkerIcon = this.cube.getInventory().getIconByKey("inventory-linker");
                if (linkerIcon != null) {
                    linkerIcon.refresh();
                }
            } else {
                linkedFull = containerInventory.firstEmpty() == -1;
            }
        }

        boolean normalFull = this.cube.getInventory().getInventory().firstEmpty() == -1;

        if (linkedLocation != null) {
            return !linkedFull || !normalFull;
        }
        return !normalFull;
    }

    private boolean breakBlock(Block block) {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        List<ItemStack> drops = new ArrayList<>(block.getDrops(tool));

        if (!canFitAllDrops(drops)) {
            return false;
        }

        for (ItemStack drop : drops) {
            boolean depositedIntoContainer = tryDepositIntoLinkedContainer(drop);

            if (!depositedIntoContainer) {
                CubeInventory cubeInventory = this.cube.getInventory();

                cubeInventory.getInventory().addItem(drop);

                Icon sellAllIcon = cubeInventory.getIconByKey("sell-all");
                if (sellAllIcon != null) {
                    sellAllIcon.refresh();
                }
            }
        }

        block.setType(Material.AIR);
        return true;
    }

    private boolean canFitAllDrops(List<ItemStack> drops) {
        Location linkedLocation = this.cube.getLinkedContainer();
        Inventory containerInventory = null;

        if (linkedLocation != null) {
            BlockState containerState = linkedLocation.getBlock().getState();
            containerInventory = ContainerUtils.getContainerInventory(containerState);
        }

        Inventory normalInventory = this.cube.getInventory().getInventory();

        ItemStack[] containerSnapshot = containerInventory != null ? cloneContents(containerInventory.getContents()) : null;
        ItemStack[] normalSnapshot = cloneContents(normalInventory.getContents());

        try {
            for (ItemStack drop : drops) {
                boolean fitsContainer = containerInventory != null && containerInventory.addItem(drop.clone()).isEmpty();
                if (!fitsContainer) {
                    boolean fitsNormal = normalInventory.addItem(drop.clone()).isEmpty();
                    if (!fitsNormal) {
                        return false;
                    }
                }
            }
            return true;
        } finally {
            if (containerInventory != null) {
                containerInventory.setContents(containerSnapshot);
            }
            normalInventory.setContents(normalSnapshot);
        }
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] cloned = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            cloned[i] = contents[i] == null ? null : contents[i].clone();
        }
        return cloned;
    }

    private boolean tryDepositIntoLinkedContainer(ItemStack drop) {
        Location linkedLocation = this.cube.getLinkedContainer();
        if (linkedLocation == null) {
            return false;
        }

        BlockState containerState = linkedLocation.getBlock().getState();
        Inventory containerInventory = ContainerUtils.getContainerInventory(containerState);

        if (containerInventory == null) {
            this.cube.setLinkedContainer(null);
            Icon linkerIcon = this.cube.getInventory().getIconByKey("inventory-linker");
            if (linkerIcon != null) {
                linkerIcon.refresh();
            }
            return false;
        }

        HashMap<Integer, ItemStack> leftover = containerInventory.addItem(drop);
        return leftover.isEmpty();
    }



    public GenCube getCube() {
        return this.cube;
    }
}