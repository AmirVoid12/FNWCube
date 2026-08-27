package iran.flame.network.cube.managers;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.HumanEntity;
import iran.flame.network.cube.utils.inventory.InteractiveInventory;

public class InventoryManager {
    private final Set<InteractiveInventory> inventories = new HashSet<>();

    public void register(InteractiveInventory inventory) {
        this.inventories.add(inventory);
    }

    public void unregister(InteractiveInventory inventory) {
        this.inventories.remove(inventory);
    }

    public InteractiveInventory getInventoryByViewer(HumanEntity viewer) {
        for (InteractiveInventory inventory : this.inventories) {
            if (!inventory.getInventory().getViewers().contains(viewer)) continue;
            return inventory;
        }
        return null;
    }
}