package iran.flame.network.cube.services.seller.compatibility;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import java.math.BigDecimal;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.interfaces.SellService;

public class EssentialsWorth implements SellService {
    private final Essentials essentials = Essentials.getPlugin(Essentials.class);
    private final Worth worth = this.essentials.getWorth();

    @Override
    public Double getSellPrice(Player player, ItemStack itemStack) {
        BigDecimal price = this.worth.getPrice(this.essentials, itemStack);
        if (price != null) {
            return price.doubleValue();
        }
        return 0.0;
    }

    @Override
    public Double getSellPrice(ItemStack itemStack) {
        return this.getSellPrice(null, itemStack);
    }
}