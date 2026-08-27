package iran.flame.network.cube.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import iran.flame.network.cube.enums.XMaterial;

import java.util.Objects;

public class InventoryUtils {

    public static Integer getSpacesForItem(Inventory inventory, ItemStack itemStack, boolean stackable) {
        int spaces = 0;
        int slotCount = inventory.getType() == InventoryType.PLAYER ? inventory.getSize() - 5 : inventory.getSize();

        for (int i = 0; i < slotCount; i++) {
            ItemStack slotItem = inventory.getContents()[i];

            if (slotItem == null) {
                spaces += stackable ? itemStack.getMaxStackSize() : 1;
                continue;
            }

            if (stackable && areSimilarItems(slotItem, itemStack)) {
                spaces += itemStack.getMaxStackSize() - slotItem.getAmount();
            }
        }

        return spaces;
    }

    public static void removeSingleItemInHand(Player player, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        PlayerInventory playerInventory = player.getInventory();

        if (equipmentSlot == EquipmentSlot.HAND) {
            if (playerInventory.getItemInMainHand().getAmount() == 1) {
                playerInventory.setItemInMainHand(null);
                return;
            }
            itemStack.setAmount(itemStack.getAmount() - 1);
            playerInventory.setItemInMainHand(itemStack);
            player.updateInventory();
            return;
        }

        if (equipmentSlot == EquipmentSlot.OFF_HAND) {
            if (playerInventory.getItemInOffHand().getAmount() == 1) {
                playerInventory.setItemInOffHand(null);
                return;
            }
            itemStack.setAmount(itemStack.getAmount() - 1);
            playerInventory.setItemInOffHand(itemStack);
            player.updateInventory();
        }
    }

    public static boolean areSimilarItems(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1.getType() != XMaterial.PLAYER_HEAD.parseMaterial()
                || itemStack2.getType() != XMaterial.PLAYER_HEAD.parseMaterial()) {
            return itemStack1.isSimilar(itemStack2);
        }

        if (!itemStack1.hasItemMeta() || !itemStack2.hasItemMeta()) {
            return false;
        }

        SkullMeta meta1 = (SkullMeta) itemStack1.getItemMeta();
        SkullMeta meta2 = (SkullMeta) itemStack2.getItemMeta();

        if (Objects.requireNonNull(meta2).getOwner() == null && Objects.requireNonNull(meta1).getOwner() != null) {
            return false;
        }

        if (meta2.getOwner() != null && Objects.requireNonNull(meta1).getOwner() != null && !meta1.getOwner().equals(meta2.getOwner())) {
            return false;
        }

        if (!meta2.hasDisplayName()) {
            return !Objects.requireNonNull(meta1).hasDisplayName();
        }
        if (!Objects.requireNonNull(meta1).hasDisplayName()) {
            return false;
        }

        if (!meta2.hasLore()) {
            return meta1.getDisplayName().equals(meta2.getDisplayName());
        }
        if (!meta1.hasLore()) {
            return false;
        }

        return meta1.getDisplayName().equals(meta2.getDisplayName())
                && Objects.requireNonNull(meta1.getLore()).equals(meta2.getLore());
    }
}