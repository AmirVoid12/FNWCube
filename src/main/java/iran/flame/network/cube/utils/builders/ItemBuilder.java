package iran.flame.network.cube.utils.builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    protected ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemBuilder setAmount(Integer amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setDamage(Short damage) {
        try {
            ItemMeta itemMeta = this.itemStack.getItemMeta();
            if (itemMeta instanceof Damageable damageable) {
                damageable.setDamage(damage);
                this.itemStack.setItemMeta(itemMeta);
            }
        } catch (NoClassDefFoundError e) {
            this.itemStack.setDurability(damage);
        }
        return this;
    }

    public ItemBuilder setName(String name) {
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (!lore.isEmpty()) {
            lore.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
        }
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setLore(lore);
        this.itemStack.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, Integer level, boolean unsafe) {
        if (unsafe) {
            this.itemStack.addUnsafeEnchantment(enchantment, level);
        } else {
            this.itemStack.addEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments, boolean unsafe) {
        if (unsafe) {
            this.itemStack.addUnsafeEnchantments(enchantments);
        } else {
            this.itemStack.addEnchantments(enchantments);
        }
        return this;
    }

    public ItemStack build() {
        return this.itemStack;
    }
}