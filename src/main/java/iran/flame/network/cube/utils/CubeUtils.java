package iran.flame.network.cube.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.GenerationBlock;
import iran.flame.network.cube.gencube.GenCube;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CubeUtils {
    private static final String[] IGNORABLE_MATERIAL_NAMES = {
            "AIR", "CAVE_AIR", "VOID_AIR",
            "GRASS_PATH", "DIRT_PATH",
            "YELLOW_FLOWER", "DANDELION",
            "LONG_GRASS", "SHORT_GRASS", "TALL_GRASS", "GRASS", "FERN", "LARGE_FERN",
            "CHORUS_FLOWER", "CHORUS_PLANT",
            "WATER", "LAVA",
            "RED_ROSE", "POPPY",
            "VINE",
            "SUGAR_CANE_BLOCK", "SUGAR_CANE",
            "DEAD_BUSH",
            "RED_MUSHROOM", "BROWN_MUSHROOM",
            "SNOW_LAYER", "SNOW"
    };

    private static final Set<Material> IGNORABLE_MATERIALS = buildIgnorableMaterials();

    private static Set<Material> buildIgnorableMaterials() {
        Set<Material> set = new HashSet<>();
        for (String name : IGNORABLE_MATERIAL_NAMES) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                set.add(material);
            }
        }
        return set;
    }

    public static List<Block> getBorderBlocks(GenCube genCube) {
        List<Block> blocks = new ArrayList<>();
        Cuboid mainCuboid = genCube.getMainCuboid();

        for (Cuboid borderCuboid : mainCuboid.getBorders()) {
            for (Block block : borderCuboid) {
                blocks.add(block);
            }
        }

        for (Block block : mainCuboid.getFace(Cuboid.CuboidDirection.DOWN)) {
            blocks.add(block);
        }

        return blocks;
    }

    public static boolean isASafeLocationToBuild(Cuboid cuboid, Location location, Player player, List<Block> protectedBlocks) {
        GenCubes genCubes = GenCubes.getInstance();

        if (genCubes.getConfiguration().getDisabledWorlds().contains(player.getWorld().getName())) {
            return false;
        }

        List<Block> filteredBlocks = cuboid.getBlocks().stream()
                .filter(block -> protectedBlocks.contains(block) || isIgnorable(block.getType()))
                .toList();

        return filteredBlocks.size() == cuboid.getBlocks().size();
    }

    private static boolean isIgnorable(Material type) {
        if (IGNORABLE_MATERIALS.contains(type)) {
            return true;
        }

        try {
            if (Tag.LEAVES.isTagged(type)) return true;
        } catch (Throwable ignored) {
        }
        try {
            if (Tag.SMALL_FLOWERS.isTagged(type)) return true;
        } catch (Throwable ignored) {
        }
        try {
            if (Tag.SAPLINGS.isTagged(type)) return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static Double getGenBlockRangeMapMaxChance(Map<GenerationBlock, Range> map) {
        Double max = 0.0;
        for (GenerationBlock generationBlock : map.keySet()) {
            Range range = map.get(generationBlock);
            if (range.high() > max) {
                max = range.high();
            }
        }
        return max;
    }

    public static GenerationBlock getGenBlockByChance(Map<GenerationBlock, Range> map, Double chance) {
        for (GenerationBlock generationBlock : map.keySet()) {
            Range range = map.get(generationBlock);
            if (range.contains(chance)) {
                return generationBlock;
            }
        }
        return null;
    }
}