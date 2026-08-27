package iran.flame.network.cube.services.edit.compatibility;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.block.Block;
import iran.flame.network.cube.BaseBlock;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.interfaces.EditorService;
import iran.flame.network.cube.enums.QuantityType;
import iran.flame.network.cube.enums.RebuildType;
import iran.flame.network.cube.utils.CubeUtils;
import iran.flame.network.cube.utils.RandomUtils;
import iran.flame.network.cube.utils.Range;

public class FaweLatest implements EditorService {

    private EditSession createEditSession(org.bukkit.World bukkitWorld) {
        World world = BukkitAdapter.adapt(Objects.requireNonNull(bukkitWorld, "World cannot be null"));
        return WorldEdit.getInstance().newEditSessionBuilder()
                .world(world)
                .fastMode(true)
                .build();
    }

    private BlockStateHolder<?> stateFor(Material material) {
        Objects.requireNonNull(material, "Material cannot be null");
        return Objects.requireNonNull(
                BlockTypes.parse(material.getKey().toString()),
                "Unknown block type: " + material
        ).getDefaultState();
    }

    /**
     * Restores the border blocks to the given state, then paints the rebuild
     * blocks with a randomly rolled generation block. Shared by build/upgrade
     * to avoid duplicating the two-loop logic.
     */
    private void paintBlocks(EditSession editSession,
                             List<Block> borderBlocks,
                             BlockStateHolder<?> borderState,
                             List<Block> fillBlocks,
                             Map<GenerationBlock, Range> genBlockRangeMap,
                             double maxChance) {
        for (Block block : borderBlocks) {
            editSession.setBlock(block.getX(), block.getY(), block.getZ(), borderState);
        }

        for (Block block : fillBlocks) {
            double roll = RandomUtils.getRandomNumber(0.0, maxChance);
            GenerationBlock genBlock = CubeUtils.getGenBlockByChance(genBlockRangeMap, roll);
            BlockStateHolder<?> blockState = stateFor(Objects.requireNonNull(genBlock).getMaterial());
            editSession.setBlock(block.getX(), block.getY(), block.getZ(), blockState);
        }
    }

    @Override
    public boolean build(GenCube cube, RebuildType rebuildType, QuantityType quantityType, int quantityValue) {
        DataManager dataManager = GenCubes.getInstance().getDataManager();
        BaseBlock borderBlock = dataManager.getBorderBlock(cube.getType());
        Material borderMaterial = borderBlock.getMaterial();

        List<Block> blocksToRestoreBorder = new ArrayList<>();
        for (Block block : cube.getBorderBlocks()) {
            if (block.getType().equals(borderMaterial)) continue;
            blocksToRestoreBorder.add(block);
        }

        List<Block> blocksToRebuild = cube.getBlocksToRebuild(rebuildType, quantityType, quantityValue);
        boolean anyBlocksToRebuild = !blocksToRebuild.isEmpty();

        Map<GenerationBlock, Range> genBlockRangeMap = cube.getGenBlockRangeMap();
        Double maxChance = CubeUtils.getGenBlockRangeMapMaxChance(genBlockRangeMap);

        try (EditSession editSession = createEditSession(cube.getLocation().getWorld())) {
            BlockStateHolder<?> borderState = stateFor(borderMaterial);
            paintBlocks(editSession, blocksToRestoreBorder, borderState, blocksToRebuild, genBlockRangeMap, maxChance);
        } catch (Exception exception) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Error while rebuilding cube", exception);
        }

        return anyBlocksToRebuild;
    }

    @Override
    public void upgrade(GenCube cube) {
        DataManager dataManager = GenCubes.getInstance().getDataManager();
        List<Block> borderBlocks = cube.getBorderBlocks();
        List<Block> cuboidBlocks = cube.getGenCuboid().getBlocks();

        BaseBlock borderBlock = dataManager.getBorderBlock(cube.getType());
        Material borderMaterial = borderBlock.getMaterial();
        Map<GenerationBlock, Range> genBlockRangeMap = cube.getGenBlockRangeMap();
        Double maxChance = CubeUtils.getGenBlockRangeMapMaxChance(genBlockRangeMap);

        try (EditSession editSession = createEditSession(cube.getLocation().getWorld())) {
            BlockStateHolder<?> borderState = stateFor(borderMaterial);
            paintBlocks(editSession, borderBlocks, borderState, cuboidBlocks, genBlockRangeMap, maxChance);
        } catch (Exception exception) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Error while upgrading cube", exception);
        }
    }

    @Override
    public void remove(GenCube cube) {
        List<Block> blocks = cube.getMainCuboid().getBlocks();

        try (EditSession editSession = createEditSession(cube.getLocation().getWorld())) {
            BlockStateHolder<?> air = Objects.requireNonNull(BlockTypes.AIR).getDefaultState();
            for (Block block : blocks) {
                editSession.setBlock(block.getX(), block.getY(), block.getZ(), air);
            }
        } catch (Exception exception) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Error while removing cube", exception);
        }
    }
}