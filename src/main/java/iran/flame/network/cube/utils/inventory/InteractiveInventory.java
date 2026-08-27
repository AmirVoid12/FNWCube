package iran.flame.network.cube.utils.inventory;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;

public abstract class InteractiveInventory {
    protected Inventory inventory;
    protected ArrayList<InventoryAction> actions = new ArrayList<>();

    protected InteractiveInventory() { }

    public static Integer calculateRows(int size) {
        return size % 9 == 0 ? size : size + (9 - size % 9);
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    protected final void addAction(InventoryAction action) {
        this.actions.add(action);
    }

    private void removeAction(InventoryAction action) {
        this.actions.remove(action);
    }

    public SlotAction getSlotAction(int slot, ClickType clickType) {
        for (InventoryAction action : this.getActions()) {
            if (!(action instanceof SlotAction slotAction)) {
                continue;
            }
            if (slot != slotAction.getSlot() || action.getClickType() != clickType) {
                continue;
            }
            return slotAction;
        }
        return null;
    }

    public void removeSlotActions(int slot) {
        ArrayList<InventoryAction> actionsCopy = new ArrayList<>(this.actions);
        for (InventoryAction action : actionsCopy) {
            if (!(action instanceof SlotAction)) {
                continue;
            }
            if (((SlotAction) action).getSlot() != slot) {
                continue;
            }
            this.actions.remove(action);
        }
    }

    public ItemAction getItemAction(ItemStack itemStack, ClickType clickType) {
        for (InventoryAction action : this.getActions()) {
            if (!(action instanceof ItemAction itemAction)) {
                continue;
            }
            if (!itemAction.getStack().isSimilar(itemStack) || action.getClickType() != clickType) {
                continue;
            }
            return itemAction;
        }
        return null;
    }

    public ArrayList<InventoryAction> getActions() {
        return this.actions;
    }
}