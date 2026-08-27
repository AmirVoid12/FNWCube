package iran.flame.network.cube.services.seller;

import iran.flame.network.cube.enums.SellPlugin;
import iran.flame.network.cube.interfaces.SellService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.CubeInventory;
import iran.flame.network.cube.gencube.inventory.NormalInventory;
import iran.flame.network.cube.services.seller.compatibility.AutoSellWorth;
import iran.flame.network.cube.services.seller.compatibility.EssentialsWorth;
import iran.flame.network.cube.services.seller.compatibility.ShopGuiPlusWorth;

public class CubeSeller {
    private final GenCubes plugin = GenCubes.getInstance();
    private SellService sellService;

    public CubeSeller() {
        SellPlugin sellPlugin = this.plugin.getConfiguration().getSellsPlugin();
        if (this.plugin.getSellPlugins().contains(sellPlugin)) {
            if (sellPlugin == SellPlugin.ESSENTIALS) {
                this.sellService = new EssentialsWorth();
            }
            if (sellPlugin == SellPlugin.SHOPGUIPLUS) {
                this.sellService = new ShopGuiPlusWorth();
            }
            if (sellPlugin == SellPlugin.AUTOSELL) {
                this.sellService = new AutoSellWorth();
            }
        }
    }

    public double sell(GenCube cube, Player player) {
        if (this.sellService == null) {
            return 0.0;
        }

        CubeInventory cubeInventory = cube.getInventory();
        double total = 0.0;
        InventoryType type = cube.getInventory().getType();

        if (type == InventoryType.NORMAL) {
            for (int slot : ((NormalInventory) cubeInventory).getContentSlots()) {
                ItemStack itemStack = cubeInventory.getInventory().getItem(slot);
                if (itemStack == null) continue;

                double price = this.sellService.getSellPrice(player, itemStack);
                if (price <= 0.0 || itemStack.hasItemMeta() || !itemStack.getEnchantments().isEmpty()) continue;

                total += price * (double) itemStack.getAmount();
                cubeInventory.getInventory().setItem(slot, null);
            }
        }

        this.plugin.getEcon().depositPlayer(player, total);
        return total;
    }

    public double getContentValue(GenCube cube) {
        if (this.sellService == null) {
            return 0.0;
        }

        CubeInventory cubeInventory = cube.getInventory();
        double total = 0.0;
        InventoryType type = cube.getInventory().getType();

        if (type == InventoryType.NORMAL) {
            for (int slot : ((NormalInventory) cubeInventory).getContentSlots()) {
                ItemStack itemStack = cubeInventory.getInventory().getItem(slot);
                if (itemStack == null) continue;

                double price = this.sellService.getSellPrice(itemStack);
                if (price <= 0.0 || itemStack.hasItemMeta() || !itemStack.getEnchantments().isEmpty()) continue;

                total += price * (double) itemStack.getAmount();
            }
        }

        return total;
    }

    public SellService getService() {
        return this.sellService;
    }

    public void setService(SellService sellService) {
        this.sellService = sellService;
    }
}