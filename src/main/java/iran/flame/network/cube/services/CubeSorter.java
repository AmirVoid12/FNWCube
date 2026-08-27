package iran.flame.network.cube.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;

public class CubeSorter {
    private static final List<Material> MATERIAL_ORDER = Arrays.asList(Material.values());

    public static void sort(GenCube cube) {
        CubeInventory cubeInventory = cube.getInventory();
        InventoryType type = cubeInventory.getType();

        if (type == InventoryType.NORMAL) {
            sortNormalInventory((NormalInventory) cubeInventory);
        }
    }

    private static void sortNormalInventory(NormalInventory cubeInventory) {
        List<ItemStack> items = new ArrayList<>(Arrays.asList((ItemStack[]) cubeInventory.getContent()));

        if (items.stream().allMatch(Objects::isNull)) {
            return;
        }

        mergeStacks(items);

        Comparator<ItemStack> byMaterialOrder = Comparator.comparing(itemStack -> MATERIAL_ORDER.indexOf(itemStack.getType()));
        List<ItemStack> sorted = items.stream()
                .sorted(Comparator.nullsLast(byMaterialOrder))
                .toList();

        int index = 0;
        for (Integer slot : cubeInventory.getContentSlots()) {
            cubeInventory.getInventory().setItem(slot, sorted.get(index));
            index++;
        }
    }

    private static void mergeStacks(List<ItemStack> items) {
        outer:
        for (int i = 0; i <= items.size() - 1; ++i) {
            ItemStack current = items.get(i);
            if (current == null || current.getAmount() >= current.getMaxStackSize()) continue;

            int maxStackSize = current.getMaxStackSize();
            int spaceLeft = maxStackSize - current.getAmount();

            for (int j = i + 1; j <= items.size() - 1; ++j) {
                ItemStack other = items.get(j);
                if (current.getAmount() == maxStackSize) continue outer;
                if (other == null || other.getAmount() >= maxStackSize || !other.isSimilar(current)) continue;

                int otherAmount = other.getAmount();
                if (otherAmount > spaceLeft) {
                    current.setAmount(maxStackSize);
                    other.setAmount(otherAmount - spaceLeft);
                    continue;
                }

                current.setAmount(current.getAmount() + otherAmount);
                items.set(j, null);
                spaceLeft = maxStackSize - current.getAmount();
            }
        }
    }
}