package iran.flame.network.cube.tasks.edit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import iran.flame.network.cube.enums.EditTask;
import iran.flame.network.cube.enums.QuantityType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import iran.flame.network.cube.BaseBlock;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.tasks.PluginTask;
import iran.flame.network.cube.utils.CubeUtils;
import iran.flame.network.cube.utils.RandomUtils;
import iran.flame.network.cube.utils.Range;

public class CubeEdition extends PluginTask {
    private final GenCube cube;
    private final EditTask editTask;
    private final QuantityType quantityType;
    private final int quantityValue;
    private final TaskManager taskManager;
    private final List<EditBatch> batches;
    private int currentBatchIndex;
    private boolean finished;
    private boolean alreadyBuilt;
    private final BaseBlock airBlock;
    private final BaseBlock borderBlock;

    public CubeEdition(GenCube cube, EditTask editTask) {
        this(cube, editTask, QuantityType.PERCENTAGE, 100);
    }

    public CubeEdition(GenCube cube, EditTask editTask, QuantityType quantityType, int quantityValue) {
        super(GenCubes.getInstance().getConfiguration().getBlockBatchEditRate(), true);
        this.cube = cube;
        this.editTask = editTask;
        this.quantityType = quantityType;
        this.quantityValue = quantityValue;
        DataManager dataManager = this.plugin.getDataManager();
        this.taskManager = this.plugin.getTaskManager();
        this.finished = false;
        this.alreadyBuilt = false;
        this.batches = new ArrayList<>();
        this.airBlock = new BaseBlock(Material.AIR, (short) 0);
        this.borderBlock = dataManager.getBorderBlock(cube.getType());
        this.prepareBatches();
        this.currentBatchIndex = 0;
        this.runTask();
    }

    @Override
    public void run() {
        if (this.currentBatchIndex != this.batches.size()) {
            EditBatch previousBatch = this.currentBatchIndex == 0 ? null : this.batches.get(this.currentBatchIndex - 1);
            if (previousBatch == null || previousBatch.isDone()) {
                this.batches.get(this.currentBatchIndex).edit();
                ++this.currentBatchIndex;
            }
        } else {
            this.finished = true;
        }

        if (this.finished) {
            if (this.onFinish != null) {
                this.onFinish.run();
            }
            this.stopTask();
            this.taskManager.unRegister(this);
        }
    }

    private void prepareBatches() {
        switch (this.editTask) {
            case ADD: {
                ArrayList<Block> nonBorderMaterialBlocks = new ArrayList<>();
                for (Block block : this.cube.getBorderBlocks()) {
                    if (block.getType().equals(this.borderBlock.getMaterial())) continue;
                    nonBorderMaterialBlocks.add(block);
                }
                List<Block> blocksToRebuild = this.cube.getBlocksToRebuild(RebuildType.LINEAL, this.quantityType, this.quantityValue);
                if (!nonBorderMaterialBlocks.isEmpty()) {
                    this.addBorderBatches(nonBorderMaterialBlocks);
                }
                if (!blocksToRebuild.isEmpty()) {
                    this.addRandomFillBatches(blocksToRebuild);
                    return;
                }
                this.alreadyBuilt = true;
                return;
            }
            case REMOVE: {
                ArrayList<Block> nonBorderMaterialBlocks = new ArrayList<>();
                for (Block block : this.cube.getBorderBlocks()) {
                    if (block.getType().equals(this.borderBlock.getMaterial())) continue;
                    nonBorderMaterialBlocks.add(block);
                }
                List<Block> blocksToRebuild = this.cube.getBlocksToRebuild(RebuildType.RANDOM, this.quantityType, this.quantityValue);
                if (!nonBorderMaterialBlocks.isEmpty()) {
                    this.addBorderBatches(nonBorderMaterialBlocks);
                }
                if (!blocksToRebuild.isEmpty()) {
                    this.addRandomFillBatches(blocksToRebuild);
                    return;
                }
                this.alreadyBuilt = true;
                return;
            }
            case SET: {
                List<Block> borderBlocks = this.cube.getBorderBlocks();
                List<Block> nonAirBlocksTopDown = this.cube.getGenCuboid().getBlocks().stream()
                        .filter(block -> block.getType() != Material.AIR)
                        .sorted(Comparator.comparing(Block::getY).reversed())
                        .collect(Collectors.toList());
                if (!nonAirBlocksTopDown.isEmpty()) {
                    this.addRandomFillBatches(nonAirBlocksTopDown);
                }
                if (borderBlocks.isEmpty()) break;
                this.addBorderBatches(borderBlocks);
                return;
            }
            case MULTIPLY: {
                List<Block> borderBlocks = this.cube.getBorderBlocks();
                List<Block> allBlocks = this.cube.getGenCuboid().getBlocks();
                if (!borderBlocks.isEmpty()) {
                    this.addBorderBatches(borderBlocks);
                }
                if (allBlocks.isEmpty()) break;
                this.addRandomFillBatches(allBlocks);
            }
        }
    }

    private void addBorderBatches(List<Block> blocks) {
        int lastIndex = blocks.size() - 1;
        int batchDivisor = blocks.size() >= 100 ? 100 : 10;
        int batchSize = blocks.size() / batchDivisor;
        if (batchSize <= 1) {
            batchSize = 5;
        }

        for (int i = 0; i <= lastIndex; ++i) {
            Block block = blocks.get(i);
            if (i % batchSize == 0 && i != lastIndex || i == 0 && lastIndex == 0) {
                this.batches.add(new EditBatch());
            }
            if (this.editTask == EditTask.ADD || this.editTask == EditTask.MULTIPLY) {
                this.batches.getLast().add(block, this.borderBlock);
            }
            if (this.editTask != EditTask.SET) continue;
            this.batches.getLast().add(block, this.airBlock);
        }
    }

    private void addRandomFillBatches(List<Block> blocks) {
        int lastIndex = blocks.size() - 1;
        int batchDivisor = blocks.size() >= 100 ? 100 : 10;
        int batchSize = (int) (0.5 * (double) blocks.size() / (double) batchDivisor);
        if (batchSize <= 1) {
            batchSize = 5;
        }

        Map<GenerationBlock, Range> genBlockRangeMap = this.cube.getGenBlockRangeMap();
        Double maxChance = CubeUtils.getGenBlockRangeMapMaxChance(genBlockRangeMap);

        for (int i = 0; i <= lastIndex; ++i) {
            Block block = blocks.get(i);
            if (i % batchSize == 0 && i != lastIndex || i == 0 && lastIndex == 0) {
                this.batches.add(new EditBatch());
            }
            if (this.editTask == EditTask.ADD || this.editTask == EditTask.MULTIPLY) {
                double randomChance = RandomUtils.getRandomNumber(0.0, maxChance);
                BaseBlock chosenBlock = CubeUtils.getGenBlockByChance(genBlockRangeMap, randomChance);
                this.batches.getLast().add(block, chosenBlock);
            }
            if (this.editTask != EditTask.SET) continue;
            this.batches.getLast().add(block, this.airBlock);
        }
    }

    @Override
    public void stopTask() {
        super.stopTask();
        if (!this.finished) {
            GenCubes.getInstance().getCacheManager().getCacheStorage().save(this);
        }
    }

    public GenCube getCube() {
        return this.cube;
    }

    public EditTask getEditTask() {
        return this.editTask;
    }

    public boolean isAlreadyBuilt() {
        return this.alreadyBuilt;
    }
}