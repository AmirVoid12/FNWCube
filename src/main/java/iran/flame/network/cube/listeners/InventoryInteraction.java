package iran.flame.network.cube.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;
import iran.flame.network.cube.managers.InventoryManager;
import iran.flame.network.cube.utils.inventory.InteractiveInventory;

public class InventoryInteraction implements Listener {
    private final InventoryManager inventoryManager;

    public InventoryInteraction() {
        GenCubes plugin = GenCubes.getInstance();
        this.inventoryManager = plugin.getInventoryManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        InventoryAction action = event.getAction();

        if (clickedInventory == null) {
            return;
        }

        InteractiveInventory openInventory = this.inventoryManager.getInventoryByViewer(player);
        if (openInventory == null) {
            return;
        }

        if (openInventory instanceof CubeInventory) {
            GenCube cube = ((CubeInventory) openInventory).getCube();

            if (cube != null && cube.getInventory().getType() == InventoryType.NORMAL) {
                if (clickedInventory.equals(player.getInventory())) {
                    if (action == InventoryAction.PICKUP_ALL
                            || action == InventoryAction.PICKUP_HALF
                            || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                            || action == InventoryAction.HOTBAR_SWAP
                            || action == InventoryAction.SWAP_WITH_CURSOR
                            || action == InventoryAction.COLLECT_TO_CURSOR
                            || action == InventoryAction.PLACE_ALL
                            || action == InventoryAction.PLACE_ONE
                            || action == InventoryAction.PLACE_SOME) {
                        event.setCancelled(true);
                        return;
                    }
                } else if (!((NormalInventory) cube.getInventory()).getContentSlots().contains(slot)
                        || action == InventoryAction.HOTBAR_SWAP
                        || action == InventoryAction.SWAP_WITH_CURSOR) {
                    event.setCancelled(true);
                }

                iran.flame.network.cube.utils.inventory.InventoryAction slotAction =
                        openInventory.getSlotAction(slot, clickType);
                if (slotAction != null) {
                    slotAction.execute(player);
                }
                return;
            }
        }

        event.setCancelled(true);

        if (!clickedInventory.equals(player.getInventory())) {
            iran.flame.network.cube.utils.inventory.InventoryAction slotAction =
                    openInventory.getSlotAction(slot, clickType);
            if (slotAction != null) {
                slotAction.execute(player);
            }
        }
    }
}