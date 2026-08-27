package iran.flame.network.cube.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import iran.flame.network.cube.enums.CompressionStatus;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;
import iran.flame.network.cube.enums.XMaterial;
import iran.flame.network.cube.utils.inventory.InventoryUtils;

public class CubeCompressor {
    private final Map<ItemStack, ItemStack> compressionRecipes = new HashMap<>();

    public CubeCompressor() {
        ItemStack recipeResult;

        ItemStack diamond = XMaterial.DIAMOND.parseItem();
        Objects.requireNonNull(diamond).setAmount(9);
        ItemStack ironIngot = XMaterial.IRON_INGOT.parseItem();
        Objects.requireNonNull(ironIngot).setAmount(9);
        ItemStack coal = XMaterial.COAL.parseItem();
        Objects.requireNonNull(coal).setAmount(9);
        ItemStack emerald = XMaterial.EMERALD.parseItem();
        Objects.requireNonNull(emerald).setAmount(9);
        ItemStack goldIngot = XMaterial.GOLD_INGOT.parseItem();
        Objects.requireNonNull(goldIngot).setAmount(9);
        ItemStack redstone = XMaterial.REDSTONE.parseItem();
        Objects.requireNonNull(redstone).setAmount(9);
        ItemStack melonSlice = XMaterial.MELON_SLICE.parseItem();
        Objects.requireNonNull(melonSlice).setAmount(9);
        ItemStack brick = XMaterial.BRICK.parseItem();
        Objects.requireNonNull(brick).setAmount(4);
        ItemStack lapisLazuli = XMaterial.LAPIS_LAZULI.parseItem();
        Objects.requireNonNull(lapisLazuli).setAmount(9);
        ItemStack glowstoneDust = XMaterial.GLOWSTONE_DUST.parseItem();
        Objects.requireNonNull(glowstoneDust).setAmount(4);

        ItemStack boneBlock = XMaterial.BONE_BLOCK.parseItem();
        if (boneBlock != null) {
            recipeResult = XMaterial.BONE_MEAL.parseItem();
            Objects.requireNonNull(recipeResult).setAmount(9);
            this.compressionRecipes.put(boneBlock, recipeResult);
        }

        if ((recipeResult = XMaterial.IRON_NUGGET.parseItem()) != null) {
            recipeResult.setAmount(9);
            this.compressionRecipes.put(XMaterial.IRON_INGOT.parseItem(), recipeResult);
        }

        ItemStack goldNugget = XMaterial.GOLD_NUGGET.parseItem();
        Objects.requireNonNull(goldNugget).setAmount(9);

        this.compressionRecipes.put(XMaterial.DIAMOND_BLOCK.parseItem(), diamond);
        this.compressionRecipes.put(XMaterial.IRON_BLOCK.parseItem(), ironIngot);
        this.compressionRecipes.put(XMaterial.COAL_BLOCK.parseItem(), coal);
        this.compressionRecipes.put(XMaterial.EMERALD_BLOCK.parseItem(), emerald);
        this.compressionRecipes.put(XMaterial.GOLD_BLOCK.parseItem(), goldIngot);
        this.compressionRecipes.put(XMaterial.REDSTONE_BLOCK.parseItem(), redstone);
        this.compressionRecipes.put(XMaterial.MELON.parseItem(), melonSlice);
        this.compressionRecipes.put(XMaterial.BRICKS.parseItem(), brick);
        this.compressionRecipes.put(XMaterial.LAPIS_BLOCK.parseItem(), lapisLazuli);
        this.compressionRecipes.put(XMaterial.GLOWSTONE.parseItem(), glowstoneDust);
        this.compressionRecipes.put(XMaterial.GOLD_INGOT.parseItem(), goldNugget);
    }

    public CompressionStatus compress(GenCube cube) {
        CubeInventory cubeInventory = cube.getInventory();
        InventoryType type = cubeInventory.getType();

        if (type == InventoryType.NORMAL) {
            return compressNormalInventory(cube, (NormalInventory) cubeInventory);
        }

        return null;
    }

    private CompressionStatus compressNormalInventory(GenCube cube, NormalInventory cubeInventory) {
        List<ItemStack> content = new ArrayList<>(Arrays.asList((ItemStack[]) cubeInventory.getContent()));
        CubeSorter.sort(cube);

        if (content.stream().allMatch(Objects::isNull)) {
            return CompressionStatus.NOTHING_TO_COMPRESS;
        }

        Inventory inventory = cubeInventory.getInventory();
        List<Integer> contentSlots = cubeInventory.getContentSlots();
        boolean compressedAnything = false;

        for (int slot : contentSlots) {
            ItemStack stack = inventory.getItem(slot);
            if (stack == null) continue;

            for (ItemStack rawMaterial : this.compressionRecipes.keySet()) {
                ItemStack compressedForm = this.compressionRecipes.get(rawMaterial);
                if (!compressedForm.isSimilar(stack) || stack.getAmount() < compressedForm.getAmount()) continue;

                compressedAnything = true;

                int compressedCount = (int) Math.floor((double) stack.getAmount() / (double) compressedForm.getAmount());
                int remainder = stack.getAmount() - compressedCount * compressedForm.getAmount();

                ItemStack compressedStack = new ItemStack(compressedForm.getType());
                compressedStack.setAmount(compressedCount);

                ItemStack remainderStack = new ItemStack(rawMaterial.getType());
                remainderStack.setAmount(remainder);

                if (InventoryUtils.getSpacesForItem(inventory, compressedStack, compressedStack.getMaxStackSize() > 1) > 0) {
                    inventory.setItem(slot, remainderStack);
                    inventory.addItem(compressedStack);
                    break;
                }

                if (slot != contentSlots.get(0)) continue;
                return CompressionStatus.INVENTORY_FULL;
            }
        }

        if (!compressedAnything) {
            return CompressionStatus.NOTHING_TO_COMPRESS;
        }

        CubeSorter.sort(cube);
        return CompressionStatus.SUCCESS;
    }
}