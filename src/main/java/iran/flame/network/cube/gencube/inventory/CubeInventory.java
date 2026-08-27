package iran.flame.network.cube.gencube.inventory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import iran.flame.network.cube.enums.AutoMinerStatus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.enums.InventoryType;
import iran.flame.network.cube.gencube.inventory.confirmations.SellConfirmation;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.managers.InventoryManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.managers.yml.Configuration;
import iran.flame.network.cube.enums.BuildingStatus;
import iran.flame.network.cube.enums.Sound;
import iran.flame.network.cube.interfaces.Versionable;
import iran.flame.network.cube.utils.builders.ItemBuilder;
import iran.flame.network.cube.utils.builders.SkullBuilder;
import iran.flame.network.cube.utils.inventory.Icon;
import iran.flame.network.cube.utils.inventory.IconPlaceHolder;
import iran.flame.network.cube.utils.inventory.InteractiveInventory;
import iran.flame.network.cube.utils.inventory.InventoryUtils;
import iran.flame.network.cube.utils.inventory.SlotAction;
import org.jetbrains.annotations.NotNull;

public abstract class CubeInventory extends InteractiveInventory {
    private static final int SLOT_REBUILD = 49;
    private static final int SLOT_AUTOMINER = 46;
    private static final int SLOT_SMELT = 50;
    private static final int SLOT_SELL_ALL = 48;
    private static final int SLOT_INVENTORY_LINKER = 45;
    private static final int SLOT_COMPRESSOR = 47;
    private static final int SLOT_UPGRADE_WITH_SELL = 51;
    private static final int SLOT_UPGRADE_NO_SELL = 48;
    private static final int SLOT_REMOVE = 53;
    private static final String[] PANE_COLORS = {
            "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
            "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
    };

    protected GenCubes plugin = GenCubes.getInstance();
    protected GenCube cube;
    protected Configuration configuration;
    protected DataManager dataManager;
    private final InventoryManager inventoryManager;
    protected TaskManager taskManager;
    protected Economy economy;
    private SellConfirmation sellConfirmation;
    protected DecimalFormat priceFormat;
    protected List<Icon> icons;

    CubeInventory(GenCube genCube) {
        this.cube = genCube;
        this.configuration = this.plugin.getConfiguration();
        this.dataManager = this.plugin.getDataManager();
        this.inventoryManager = this.plugin.getInventoryManager();
        this.taskManager = this.plugin.getTaskManager();
        this.economy = this.plugin.getEcon();
        this.priceFormat = new DecimalFormat("#.##");
    }

    public void prepareInventory(Object[] object) {
        this.inventory = Bukkit.createInventory(null, 54,
                ChatColor.translateAlternateColorCodes('&', this.dataManager.getInventoryName(this.cube.getType())));

        int colorIndex = getInventoryColor();
        ItemStack background;

        if (Versionable.isAtLeast(1, 13)) {
            background = new ItemBuilder(Material.valueOf(PANE_COLORS[colorIndex] + "_STAINED_GLASS_PANE"))
                    .setName("" + ChatColor.ITALIC + ChatColor.RESET)
                    .build();
        } else {
            background = new ItemBuilder(Material.valueOf("STAINED_GLASS_PANE"))
                    .setName("" + ChatColor.ITALIC + ChatColor.RESET)
                    .setDamage((short) colorIndex)
                    .build();
        }

        for (int i = 0; i <= 53; i++) {
            this.inventory.setItem(i, background);
        }

        if (this.icons == null) {
            this.icons = new ArrayList<>();
        } else {
            this.icons.clear();
        }

        this.actions.clear();

        for (int row = 0; row <= 4; row++) {
            for (int col = 1 + row * 9; col <= 7 + row * 9; col++) {
                this.inventory.setItem(col, null);
            }
        }

        this.inventoryManager.register(this);
    }

    private int getInventoryColor() {
        Map<String, Object> guiOptions = this.configuration.getOptions().get("gui");
        Object colorValue = guiOptions.get("inventory_color");
        if (colorValue instanceof Number num) {
            int color = num.intValue();
            if (color <= 15 && color >= 0) {
                return color;
            }
        }
        return 7;
    }

    public abstract InventoryType getType();

    public abstract Object[] getContent();

    public abstract boolean isEmpty();

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    protected final void updateInventory() {
        setupRebuildIcon();
        setupSmeltIcon();
        setupSellAllIcon();
        setupInventoryLinkingIcon();
        setupCompressorIcon();
        setupAutoMinerIcon();
        setupUpgradeIcon();
        setupRemoveIcon();
    }

    private void setupAutoMinerIcon() {
        if (!this.dataManager.getAutoMinerAvailability(this.cube.getType())) return;

        List<String> autoMinerLore = new ArrayList<>();
        autoMinerLore.add("&7Status: %status%");
        autoMinerLore.add("&7Click to toggle automatic mining.");

        ItemStack baseIcon = new SkullBuilder()
                .setOwner("MHF_Pickaxe")
                .setName("&6&lAuto Miner")
                .setLore(autoMinerLore)
                .build();

        final Icon autoMinerIcon = getIcon(baseIcon);
        autoMinerIcon.refresh();
        this.icons.add(autoMinerIcon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_AUTOMINER) {
            @Override
            public void execute(Player player) {
                if (!CubeInventory.this.cube.getOwner().equals(player.getUniqueId()) && !player.hasPermission("gencubes.admin") && !player.isOp()) {
                    GenCubes.sendMessage(player, "&cYou don't have permissions to toggle the auto miner!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    return;
                }

                AutoMinerStatus status = CubeInventory.this.plugin.getAutoMinerManager().toggle(CubeInventory.this.cube);

                if (status == AutoMinerStatus.STARTED) {
                    GenCubes.sendMessage(player, "&aAuto miner has been enabled!", false);
                    player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), 100.0f, 1.0f);
                } else if (status == AutoMinerStatus.STOPPED) {
                    GenCubes.sendMessage(player, "&cAuto miner has been disabled!", false);
                    player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), 100.0f, 1.0f);
                }

                autoMinerIcon.refresh();
            }
        });
    }

    private @NotNull Icon getIcon(ItemStack baseIcon) {
        final Icon autoMinerIcon = new Icon("auto-miner", baseIcon);
        autoMinerIcon.addPlaceholder(new IconPlaceHolder("%status%") {
            @Override
            public String getReplacement() {
                if (CubeInventory.this.plugin.getAutoMinerManager().isRunning(CubeInventory.this.cube)) {
                    return "&aEnabled";
                }
                return "&cDisabled";
            }
        });
        autoMinerIcon.setInventory(this.inventory);
        autoMinerIcon.setSlot(SLOT_AUTOMINER);
        return autoMinerIcon;
    }

    private void setupRebuildIcon() {
        if (!this.dataManager.getRebuildAvailability(this.cube.getType())) return;

        final Double price = this.dataManager.getRebuildPrice(this.cube.getType());
        List<String> lore = new ArrayList<>();
        lore.add("&7Cost: &a$%price%");
        lore.add("&7Click to rebuild this cube.");
        replacePriceInLore(lore, price);

        ItemStack icon = new SkullBuilder()
                .setOwner("MHF_Anvil")
                .setName("&6&lRebuild Cube")
                .setLore(lore)
                .build();
        this.inventory.setItem(SLOT_REBUILD, icon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_REBUILD) {
            @Override
            public void execute(Player player) {
                player.closeInventory();

                if (CubeInventory.this.economy != null && price > 0.0) {
                    if (CubeInventory.this.economy.getBalance(player) >= price) {
                        CubeInventory.this.economy.withdrawPlayer(player, price);
                    } else {
                        GenCubes.sendMessage(player, "&cYou don't have enough money to rebuild this cube!", false);
                        return;
                    }
                }

                if (!player.hasPermission("gencubes.rebuild.*") && !player.hasPermission("gencubes.rebuild." + CubeInventory.this.cube.getType())) {
                    player.closeInventory();
                    GenCubes.sendMessage(player, "&cYou don't have permissions to rebuild this cube!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    refundIfNeeded(player, price);
                    return;
                }

                boolean showAnimation = !(Boolean) CubeInventory.this.configuration.getOptions()
                        .get("fastasyncworldedit").get("show_block_animation_on_regeneration");

                BuildingStatus status = CubeInventory.this.cube.build(
                        CubeInventory.this.dataManager.getRebuildType(CubeInventory.this.cube.getType()),
                        CubeInventory.this.dataManager.getRebuildQuantityType(CubeInventory.this.cube.getType()),
                        CubeInventory.this.dataManager.getRebuildQuantityValue(CubeInventory.this.cube.getType()),
                        showAnimation);

                if (status == BuildingStatus.SUCCESS) {
                    if (CubeInventory.this.plugin.isFawe()) {
                        GenCubes.sendMessage(player, "&aYour cube has been rebuilt!", false);
                    } else {
                        GenCubes.sendMessage(player, "&aYour cube is being rebuilt, please wait...", false);
                    }
                } else if (status == BuildingStatus.ALREADY_BUILT) {
                    GenCubes.sendMessage(player, "&cThis cube is already fully built!", false);
                    refundIfNeeded(player, price);
                } else if (status == BuildingStatus.ALREADY_IN_PROGRESS) {
                    GenCubes.sendMessage(player, "&cThis cube is already being rebuilt!", false);
                    refundIfNeeded(player, price);
                }
            }
        });
    }

    private void setupSmeltIcon() {
        if (!this.dataManager.getSmeltingAvailability(this.cube.getType())) return;

        final Double price = this.dataManager.getSmeltingPrice(this.cube.getType());
        List<String> lore = new ArrayList<>();
        lore.add("&7Cost: &a$%price%");
        lore.add("&7Click to smelt this cube's contents.");
        replacePriceInLore(lore, price);

        ItemStack icon = new SkullBuilder()
                .setOwner("MHF_Furnace")
                .setName("&6&lSmelt Contents")
                .setLore(lore)
                .build();
        this.inventory.setItem(SLOT_SMELT, icon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_SMELT) {
            @Override
            public void execute(Player player) {
                player.closeInventory();

                if (CubeInventory.this.economy != null && price > 0.0) {
                    if (CubeInventory.this.economy.getBalance(player) >= price) {
                        CubeInventory.this.economy.withdrawPlayer(player, price);
                    } else {
                        GenCubes.sendMessage(player, "&cYou don't have enough money to smelt this cube's contents!", false);
                        return;
                    }
                }

                boolean success = CubeInventory.this.cube.smelt(player);
                if (!success) {
                    player.closeInventory();
                    refundIfNeeded(player, price);
                } else {
                    Icon sellAllIcon = CubeInventory.this.getIconByKey("sell-all");
                    if (sellAllIcon != null) {
                        sellAllIcon.refresh();
                    }
                }
            }
        });
    }

    private void setupSellAllIcon() {
        if (!this.dataManager.getSellsAvailability(this.cube.getType())
                || this.plugin.getEcon() == null
                || this.plugin.getCubeSeller().getService() == null) {
            return;
        }

        List<String> sellLore = new ArrayList<>();
        sellLore.add("&7Value: &a$%value%");
        sellLore.add("&7Click to sell all contents.");

        ItemStack baseIcon = new SkullBuilder()
                .setOwner("MHF_Chest")
                .setName("&6&lSell All")
                .setLore(sellLore)
                .build();

        final Icon sellAllIcon = new Icon("sell-all", baseIcon);
        sellAllIcon.addPlaceholder(new IconPlaceHolder("%value%") {
            @Override
            public String getReplacement() {
                return CubeInventory.this.priceFormat.format(CubeInventory.this.plugin.getCubeSeller().getContentValue(CubeInventory.this.cube));
            }
        });
        sellAllIcon.setInventory(this.inventory);
        sellAllIcon.setSlot(SLOT_SELL_ALL);
        sellAllIcon.refresh();
        this.icons.add(sellAllIcon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_SELL_ALL) {
            @Override
            public void execute(Player player) {
                boolean requireConfirmation = (Boolean) CubeInventory.this.configuration.getOptions()
                        .get("actions").get("onsell_confirmation");

                if (!requireConfirmation) {
                    player.closeInventory();
                    if (CubeInventory.this.cube.sell(player)) {
                        sellAllIcon.refresh();
                    }
                } else {
                    CubeInventory.this.getOrCreateSellConfirmation().open(player);
                }
            }
        });
    }

    private void setupInventoryLinkingIcon() {
        if (!this.dataManager.getInventoryLinkingAvailability(this.cube.getType())) return;

        List<String> linkerLore = new ArrayList<>();
        linkerLore.add("&7Status: %status%");
        linkerLore.add("&7Left-click to link to a chest.");
        linkerLore.add("&7Right-click to unlink.");

        ItemStack baseIcon = new SkullBuilder()
                .setOwner("MHF_Hopper")
                .setName("&6&lLink Inventory")
                .setLore(linkerLore)
                .build();

        final Icon linkerIcon = new Icon("inventory-linker", baseIcon);
        linkerIcon.addPlaceholder(new IconPlaceHolder("%status%") {
            @Override
            public String getReplacement() {
                if (CubeInventory.this.cube.getLinkedContainer() == null) {
                    return "&cNot Linked";
                }
                return "&aLinked";
            }
        });
        linkerIcon.setInventory(this.inventory);
        linkerIcon.setSlot(SLOT_INVENTORY_LINKER);
        linkerIcon.refresh();
        this.icons.add(linkerIcon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_INVENTORY_LINKER) {
            @Override
            public void execute(Player player) {
                UUID uuid = player.getUniqueId();

                if (!player.hasPermission("gencubes.link.*") && !player.hasPermission("gencubes.link." + CubeInventory.this.cube.getType())) {
                    GenCubes.sendMessage(player, "&cYou don't have permissions to link this cube to an external inventory!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                if (CubeInventory.this.cube.getPairingAttempts().contains(uuid)) {
                    GenCubes.sendMessage(player, "&cYou are already trying to link a cube!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    player.closeInventory();
                    return;
                }

                CubeInventory.this.cube.getPairingAttempts().add(uuid);
                player.closeInventory();
                GenCubes.sendMessage(player, "&aRight-click the chest you want to link this cube to!", false);
            }
        });

        this.addAction(new SlotAction(ClickType.RIGHT, SLOT_INVENTORY_LINKER) {
            @Override
            public void execute(Player player) {
                if (CubeInventory.this.cube.getLinkedContainer() != null) {
                    CubeInventory.this.cube.setLinkedContainer(null);
                    linkerIcon.refresh();
                    player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), 100.0f, 1.0f);
                }
            }
        });
    }

    private void setupCompressorIcon() {
        if (!this.dataManager.getCompressorAvailability(this.cube.getType())) return;

        final Double price = this.dataManager.getCompressorPrice(this.cube.getType());
        List<String> lore = new ArrayList<>();
        lore.add("&7Cost: &a$%price%");
        lore.add("&7Click to compress this cube's contents.");
        replacePriceInLore(lore, price);

        ItemStack icon = new SkullBuilder()
                .setOwner("MHF_Piston")
                .setName("&6&lCompress Contents")
                .setLore(lore)
                .build();
        this.inventory.setItem(SLOT_COMPRESSOR, icon);

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_COMPRESSOR) {
            @Override
            public void execute(Player player) {
                if (CubeInventory.this.economy != null && price > 0.0) {
                    if (CubeInventory.this.economy.getBalance(player) >= price) {
                        CubeInventory.this.economy.withdrawPlayer(player, price);
                    } else {
                        GenCubes.sendMessage(player, "&cYou don't have enough money to compress this cube's contents!", false);
                        return;
                    }
                }

                boolean success = CubeInventory.this.cube.compress(player);
                if (!success) {
                    player.closeInventory();
                    refundIfNeeded(player, price);
                } else {
                    Icon sellAllIcon = CubeInventory.this.getIconByKey("sell-all");
                    if (sellAllIcon != null) {
                        sellAllIcon.refresh();
                    }
                }
            }
        });
    }

    private void setupUpgradeIcon() {
        boolean isNextUpgradeTarget = this.dataManager.getUpgradeAvailability(this.cube.getType())
                && this.getType() == this.dataManager.getInventoryType(this.dataManager.getNextUpgrade(this.cube.getType()));
        if (!isNextUpgradeTarget) return;

        final double price = Double.parseDouble(this.priceFormat.format(this.dataManager.getUpgradePrice(this.cube.getType())).replaceAll(",", "."));

        List<String> lore = new ArrayList<>();
        lore.add("&7Cost: &a$%price%");
        lore.add("&7Click to upgrade this cube.");
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("%price%")) {
                line = line.replaceAll("%price%", Double.toString(price));
            }
            lore.set(i, line);
        }

        boolean sellAllPresent = this.dataManager.getSellsAvailability(this.cube.getType())
                && this.plugin.getEcon() != null
                && this.plugin.getCubeSeller().getService() != null;
        final int slot = sellAllPresent ? SLOT_UPGRADE_WITH_SELL : SLOT_UPGRADE_NO_SELL;

        ItemStack icon = new SkullBuilder()
                .setOwner("MHF_ArrowUp")
                .setName("&6&lUpgrade Cube")
                .setLore(lore)
                .build();
        this.inventory.setItem(slot, icon);

        this.addAction(new SlotAction(ClickType.LEFT, slot) {
            @Override
            public void execute(Player player) {
                if (CubeInventory.this.economy != null && price > 0.0) {
                    if (CubeInventory.this.economy.getBalance(player) >= price) {
                        CubeInventory.this.economy.withdrawPlayer(player, price);
                    } else {
                        GenCubes.sendMessage(player, "&cYou don't have enough money to upgrade this cube!", false);
                        player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                        return;
                    }
                }

                player.closeInventory();
                boolean success = CubeInventory.this.cube.upgrade(player);
                if (!success) {
                    refundIfNeeded(player, price);
                }
            }
        });
    }

    private void setupRemoveIcon() {
        List<String> removeLore = new ArrayList<>();
        removeLore.add("&7Click to remove this cube.");

        ItemStack icon = new SkullBuilder()
                .setOwner("MHF_TNT")
                .setName("&c&lRemove Cube")
                .setLore(removeLore)
                .build();
        this.inventory.setItem(SLOT_REMOVE, icon);

        final Double price = this.dataManager.getRemovePrice(this.cube.getType());

        this.addAction(new SlotAction(ClickType.LEFT, SLOT_REMOVE) {
            @Override
            public void execute(Player player) {
                player.closeInventory();

                if (!CubeInventory.this.cube.getOwner().equals(player.getUniqueId()) && !player.hasPermission("gencubes.admin") && !player.isOp()) {
                    GenCubes.sendMessage(player, "&cYou are not the owner of this cube!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    return;
                }

                if (CubeInventory.this.taskManager.getCubeEditionByCube(CubeInventory.this.cube) != null) {
                    GenCubes.sendMessage(player, "&cThis cube is currently being rebuilt, please wait before removing it!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
                    return;
                }

                boolean waitForEmpty = (Boolean) CubeInventory.this.configuration.getOptions()
                        .get("actions").getOrDefault("onremove_wait_for_empty_cube_inventory", Boolean.FALSE);
                if (!CubeInventory.this.isEmpty() && waitForEmpty) {
                    GenCubes.sendMessage(player, "&cThis cube's inventory must be empty before it can be removed!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                    return;
                }

                ItemStack cubeItem = CubeInventory.this.dataManager.getIcon(CubeInventory.this.cube.getType());
                boolean stackable = cubeItem.getMaxStackSize() > 1;
                if (InventoryUtils.getSpacesForItem(player.getInventory(), cubeItem, stackable) <= 0) {
                    GenCubes.sendMessage(player, "&cYou don't have enough inventory space to remove this cube!", false);
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
                    return;
                }

                if (CubeInventory.this.economy != null && price > 0.0) {
                    if (!(CubeInventory.this.economy.getBalance(player) >= price)) {
                        GenCubes.sendMessage(player, "&cYou don't have enough money to remove this cube!", false);
                        player.playSound(player.getLocation(), Sound.ANVIL_BREAK.bukkitSound(), 100.0f, 1.0f);
                        return;
                    }
                    CubeInventory.this.economy.withdrawPlayer(player, price);
                }

                CubeInventory.this.cube.remove();

                boolean giveBack = (Boolean) CubeInventory.this.configuration.getOptions().get("actions").get("get_cube_back_when_removing");
                if (giveBack) {
                    player.getInventory().addItem(cubeItem);
                }

                GenCubes.sendMessage(player, "&aThe cube has been removed!", false);
                player.playSound(player.getLocation(), Sound.LAVA_POP.bukkitSound(), 100.0f, 1.0f);
            }
        });
    }

    private void replacePriceInLore(List<String> lore, Double price) {
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains("%price%")) {
                line = line.replaceAll("%price%", this.priceFormat.format(price));
            }
            lore.set(i, line);
        }
    }

    private void refundIfNeeded(Player player, Double price) {
        if (this.economy != null && price > 0.0) {
            this.economy.depositPlayer(player, price);
        }
    }

    private SellConfirmation getOrCreateSellConfirmation() {
        if (this.sellConfirmation == null) {
            this.sellConfirmation = new SellConfirmation(this.cube);
            this.inventoryManager.register(this.sellConfirmation);
        }
        return this.sellConfirmation;
    }

    public void closeForAllViewers() {
        List<HumanEntity> viewers = new ArrayList<>(this.inventory.getViewers());
        for (HumanEntity viewer : viewers) {
            viewer.closeInventory();
        }
    }

    public void onUnload() {
        closeForAllViewers();
        this.inventoryManager.unregister(this);
        if (this.sellConfirmation != null) {
            this.inventoryManager.unregister(this.sellConfirmation);
        }
    }

    public Icon getIconByKey(String key) {
        for (Icon icon : this.icons) {
            if (icon.getKey().equals(key)) {
                return icon;
            }
        }
        return null;
    }

    public GenCube getCube() {
        return this.cube;
    }

    public List<Icon> getIcons() {
        return this.icons;
    }
}