package iran.flame.network.cube.services.seller.compatibility;

import java.util.Optional;
import me.clip.autosell.AutoSellAPI;
import me.clip.autosell.objects.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.interfaces.SellService;

public class AutoSellWorth implements SellService {

    @Override
    public Double getSellPrice(Player player, ItemStack itemStack) {
        Shop shop = null;

        if (AutoSellAPI.hasShop(player)) {
            shop = AutoSellAPI.getCurrentShop(player);
        } else {
            String defaultShop = (String) GenCubes.getInstance().getConfiguration()
                    .getOptions().get("autosell").get("default_shop");

            Optional<Shop> found = AutoSellAPI.getAllShops().stream()
                    .filter(s -> matchesShopName(defaultShop, s))
                    .findFirst();

            if (found.isPresent()) {
                shop = found.get();
            }
        }

        if (shop == null) {
            return 0.0;
        }

        ItemStack single = itemStack.clone();
        single.setAmount(1);
        return shop.getBaseWorth(single);
    }

    @Override
    public Double getSellPrice(ItemStack itemStack) {
        ItemStack single = itemStack.clone();
        single.setAmount(1);

        String defaultShop = (String) GenCubes.getInstance().getConfiguration()
                .getOptions().get("autosell").get("default_shop");

        Optional<Shop> found = AutoSellAPI.getAllShops().stream()
                .filter(s -> matchesShopName(defaultShop, s))
                .findFirst();

        if (found.isPresent()) {
            Shop shop = found.get();
            return shop.getBaseWorth(single);
        }
        return 0.0;
    }

    private static boolean matchesShopName(String name, Shop shop) {
        return shop.getName().equals(name);
    }
}