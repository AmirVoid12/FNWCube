package iran.flame.network.cube.services.seller.compatibility;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.interfaces.SellService;

public class ShopGuiPlusWorth implements SellService {

    @Override
    public Double getSellPrice(Player player, ItemStack itemStack) {
        boolean online = player != null && player.isOnline();

        ShopItem shopItem = online
                ? ShopGuiPlusApi.getItemStackShopItem(player, itemStack)
                : ShopGuiPlusApi.getItemStackShopItem(itemStack);

        if (shopItem != null) {
            return online
                    ? shopItem.getSellPriceForAmount(player, 1)
                    : shopItem.getSellPriceForAmount(1);
        }
        return 0.0;
    }

    @Override
    public Double getSellPrice(ItemStack itemStack) {
        ShopItem shopItem = ShopGuiPlusApi.getItemStackShopItem(itemStack);
        if (shopItem != null) {
            return shopItem.getSellPriceForAmount(1);
        }
        return 0.0;
    }
}