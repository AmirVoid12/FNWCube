package iran.flame.network.cube.gencube.inventory.confirmations;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.utils.MaterialUtils;
import iran.flame.network.cube.enums.XMaterial;
import iran.flame.network.cube.utils.builders.ItemBuilder;
import iran.flame.network.cube.utils.builders.SkullBuilder;
import iran.flame.network.cube.utils.inventory.SlotAction;

public class SellConfirmation extends ConfirmationGUI {
    private static final int SLOT_CONFIRM = 13;

    public SellConfirmation(GenCube genCube) {
        super(genCube);
    }

    @Override
    void setupInventory() {
        this.inventory = Bukkit.createInventory(null, 27,
                ChatColor.translateAlternateColorCodes('&', "&a&lSell the content"));

        ItemStack background = new ItemBuilder(MaterialUtils.getLegacy(XMaterial.WHITE_STAINED_GLASS_PANE))
                .setName("" + ChatColor.ITALIC + ChatColor.RESET)
                .setDamage((short) 5)
                .build();

        for (int i = 0; i <= 26; i++) {
            this.inventory.setItem(i, background);
        }

        ItemStack confirmIcon = new SkullBuilder()
                .setOwner("MrSnowDK")
                .setName("&a&lSell content!")
                .setLore(List.of("&7Click here to confirm your sell intention!"))
                .build();
        this.inventory.setItem(SLOT_CONFIRM, confirmIcon);
    }

    @Override
    void loadActions() {
        this.addAction(new SlotAction(ClickType.LEFT, SLOT_CONFIRM) {
            @Override
            public void execute(Player player) {
                player.closeInventory();
                if (SellConfirmation.this.getCube().sell(player)) {
                    SellConfirmation.this.getCube().getInventory().getIconByKey("sell-all").refresh();
                }
            }
        });
    }
}