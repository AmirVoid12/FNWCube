package iran.flame.network.cube.services;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;

public class CubeSmelter {
    public boolean smelt(GenCube cube) {
        CubeInventory cubeInventory = cube.getInventory();
        InventoryType type = cubeInventory.getType();
        boolean smeltedAnything = false;

        if (type == InventoryType.NORMAL) {
            NormalInventory normalInventory = (NormalInventory) cubeInventory;

            for (int slot : normalInventory.getContentSlots()) {
                ItemStack itemStack = cubeInventory.getInventory().getItem(slot);
                if (itemStack == null) continue;

                ItemStack smeltedResult = getSmeltedResult(itemStack);
                if (smeltedResult == null) continue;

                smeltedResult.setAmount(itemStack.getAmount());
                cubeInventory.getInventory().setItem(slot, smeltedResult);
                smeltedAnything = true;
            }
        }

        return smeltedAnything;
    }

    private static ItemStack getSmeltedResult(ItemStack itemStack) {
        Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();

        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) continue;

            if (furnaceRecipe.getInput().getType() != itemStack.getType()) continue;

            return furnaceRecipe.getResult();
        }

        return null;
    }
}