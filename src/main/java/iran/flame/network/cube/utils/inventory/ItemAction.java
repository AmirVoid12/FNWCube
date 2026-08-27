package iran.flame.network.cube.utils.inventory;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class ItemAction extends InventoryAction {
    private final ItemStack stack;

    public ItemAction(ClickType clickType, ItemStack itemStack) {
        super(clickType);
        this.stack = itemStack.clone();
    }

    public ItemStack getStack() {
        return this.stack;
    }
}