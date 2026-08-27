package iran.flame.network.cube.interfaces;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface SellService {
    Double getSellPrice(Player player, ItemStack item);
    Double getSellPrice(ItemStack item);
}