package iran.flame.network.cube.gencube.inventory;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;

public class NormalInventory extends CubeInventory {
    private final List<Integer> contentSlots = new ArrayList<>();

    public NormalInventory(GenCube cube) {
        super(cube);
    }

    @Override
    public void prepareInventory(Object[] content) {
        super.prepareInventory(null);
        this.contentSlots.clear();

        for (int row = 0; row <= 4; ++row) {
            for (int slot = 1 + row * 9; slot <= 7 + row * 9; ++slot) {
                this.contentSlots.add(slot);
            }
        }

        if (content == null) {
            content = new ItemStack[35];
        }

        int index = 0;
        for (Integer slot : this.contentSlots) {
            this.inventory.setItem(slot, (ItemStack) content[index]);
            ++index;
        }

        this.updateInventory();
    }

    @Override
    public InventoryType getType() {
        return InventoryType.NORMAL;
    }

    @Override
    public Object[] getContent() {
        ItemStack[] content = new ItemStack[35];
        for (int i = 0; i <= 34; ++i) {
            Integer slot = this.getContentSlots().get(i);
            content[i] = this.inventory.getItem(slot);
        }
        return content;
    }

    @Override
    public boolean isEmpty() {
        for (Integer slot : this.contentSlots) {
            if (this.inventory.getItem(slot) != null) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getContentSlots() {
        return this.contentSlots;
    }
}