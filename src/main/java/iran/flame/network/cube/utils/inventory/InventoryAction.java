package iran.flame.network.cube.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public abstract class InventoryAction {
    private final ClickType clickType;

    InventoryAction(ClickType clickType) {
        this.clickType = clickType;
    }

    public abstract void execute(Player player);

    public ClickType getClickType() {
        return this.clickType;
    }
}