package iran.flame.network.cube.utils.inventory;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Icon {
    private final String key;
    private ItemStack itemStack;
    private Inventory inventory;
    private Integer slot;
    protected final List<IconPlaceHolder> placeholders;
    private ItemMeta baseItemMeta;

    public Icon(String key, @Nullable ItemStack itemStack) {
        this.key = key;
        if (itemStack != null) {
            this.itemStack = itemStack;
            this.baseItemMeta = Objects.requireNonNull(itemStack.getItemMeta()).clone();
        }
        this.placeholders = new ArrayList<>();
    }

    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.baseItemMeta = Objects.requireNonNull(itemStack.getItemMeta()).clone();
    }

    public void addPlaceholder(IconPlaceHolder iconPlaceHolder) {
        this.placeholders.add(iconPlaceHolder);
    }

    public void refresh() {
        ItemMeta itemMeta = this.baseItemMeta.clone();

        if (itemMeta.hasDisplayName()) {
            String displayName = itemMeta.getDisplayName();
            for (IconPlaceHolder placeholder : this.placeholders) {
                String key = placeholder.getPlaceHolder();
                if (!displayName.contains(key)) {
                    continue;
                }
                displayName = displayName.replaceAll(key,
                        ChatColor.translateAlternateColorCodes('&', placeholder.getReplacement()));
            }
            itemMeta.setDisplayName(displayName);
        }

        if (itemMeta.hasLore()) {
            List<String> lore = itemMeta.getLore();
            if (!Objects.requireNonNull(lore).isEmpty()) {
                for (int i = 0; i < lore.size(); ++i) {
                    String line = lore.get(i);
                    for (IconPlaceHolder placeholder : this.placeholders) {
                        String key = placeholder.getPlaceHolder();
                        if (!line.contains(key)) {
                            continue;
                        }
                        line = line.replaceAll(key,
                                ChatColor.translateAlternateColorCodes('&', placeholder.getReplacement()));
                    }
                    lore.set(i, line);
                }
                itemMeta.setLore(lore);
            }
        }

        this.itemStack.setItemMeta(itemMeta);
        if (this.inventory != null && this.slot != null) {
            this.inventory.setItem(this.slot, this.itemStack);
        }
    }

    public String getKey() {
        return this.key;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Integer getSlot() {
        return this.slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }
}