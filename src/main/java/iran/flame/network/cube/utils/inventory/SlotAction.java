package iran.flame.network.cube.utils.inventory;

import org.bukkit.event.inventory.ClickType;

public abstract class SlotAction extends InventoryAction {
    private final int slot;

    public SlotAction(ClickType clickType, int slot) {
        super(clickType);
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }
}