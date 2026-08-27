package iran.flame.network.cube;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import iran.flame.network.cube.enums.RegionManagementPlugin;
import iran.flame.network.cube.services.autominer.AutoMinerManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import iran.flame.network.cube.commands.CubesCmd;
import iran.flame.network.cube.commands.MainCmd;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.listeners.BlockInteraction;
import iran.flame.network.cube.listeners.ChunkHandler;
import iran.flame.network.cube.listeners.CubeHandler;
import iran.flame.network.cube.listeners.InventoryInteraction;
import iran.flame.network.cube.listeners.PlayerHandler;
import iran.flame.network.cube.managers.CacheManager;
import iran.flame.network.cube.managers.CubeBankManager;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.managers.InventoryManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.managers.yml.Configuration;
import iran.flame.network.cube.managers.yml.CubeConfiguration;
import iran.flame.network.cube.services.CubeCompressor;
import iran.flame.network.cube.services.CubeSmelter;
import iran.flame.network.cube.services.edit.CubeEditorService;
import iran.flame.network.cube.services.seller.CubeSeller;
import iran.flame.network.cube.enums.SellPlugin;
import iran.flame.network.cube.tasks.PluginTask;
import iran.flame.network.cube.interfaces.Versionable;

public class GenCubes extends JavaPlugin implements Versionable {
    private static GenCubes instance;
    private static String prefix;
    private Economy economy = null;
    private List<SellPlugin> sellPlugins;
    private List<RegionManagementPlugin> regionManagementPlugins;
    private Configuration configuration;
    private CubeConfiguration cubeConfiguration;
    private DataManager dataManager;
    private InventoryManager inventoryManager;
    private CubeManager cubeManager;
    private CubeBankManager cubeBankManager;
    private AutoMinerManager autoMinerManager;
    private TaskManager taskManager;
    private CacheManager cacheManager;
    private CubeEditorService cubeEditorService;
    private CubeSeller cubeSeller;
    private CubeSmelter cubeSmelter;
    private CubeCompressor cubeCompressor;
    private boolean fawe = false;
    private boolean advancedChests = false;
    private boolean loaded = false;

    @Override
    public void onEnable() {
        instance = this;

        GenCubes.prefix = "[FNWCube]";

        this.sellPlugins = new ArrayList<>();
        this.regionManagementPlugins = new ArrayList<>();

        saveDefaultConfig();

        if (!setupEconomy()) {
            sendMessage(getServer().getConsoleSender(), "&7Vault was not found, the upgrades and sells were disabled!");
        }

        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.sellPlugins.add(SellPlugin.ESSENTIALS);
        }
        if (getServer().getPluginManager().getPlugin("ShopGUIPlus") != null) {
            this.sellPlugins.add(SellPlugin.SHOPGUIPLUS);
        }

        if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            this.fawe = true;
        } else {
            sendMessage(getServer().getConsoleSender(), "&cPlease install &4FastAsyncWorldEdit &cto enhance the performance of the plugin&6!");
        }

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            this.regionManagementPlugins.add(RegionManagementPlugin.WORLDGUARD);
        }

        Plugin plotSquared = getServer().getPluginManager().getPlugin("PlotSquared");
        if (plotSquared != null) {
            RegionManagementPlugin plotSquaredRegion = RegionManagementPlugin.PLOTSQUARED;
            plotSquaredRegion.setVersion(plotSquared.getDescription().getVersion());
            this.regionManagementPlugins.add(plotSquaredRegion);
        }

        if (getServer().getPluginManager().getPlugin("AdvancedChests") != null) {
            this.advancedChests = true;
        }
        if (getServer().getPluginManager().getPlugin("AutoSell") != null) {
            this.sellPlugins.add(SellPlugin.AUTOSELL);
        }

        setupConfig();
        setupServices();
        setupManagers();
        registerListeners();
        registerCommands();
        loadPlayerData();

        this.loaded = true;
    }

    @Override
    public void onDisable() {
        if (this.cubeManager != null) {
            for (GenCube cube : this.cubeManager.getCubes()) {
                cube.getInventory().onUnload();
                cube.save();
            }
        }
        if (this.cubeBankManager != null) {
            for (CubeBankAccount account : this.cubeBankManager.getAccounts()) {
                account.save();
            }
        }
        if (this.taskManager != null) {
            for (PluginTask task : this.taskManager.getTasks()) {
                task.stopTask();
            }
        }
    }

    private void setupConfig() {
        this.configuration = new Configuration(getConfig());
        this.cubeConfiguration = new CubeConfiguration();
    }

    private void setupServices() {
        this.cubeEditorService = new CubeEditorService();
        this.cubeSeller = new CubeSeller();
        this.cubeSmelter = new CubeSmelter();
        this.cubeCompressor = new CubeCompressor();
        this.autoMinerManager = new AutoMinerManager();
    }

    private void setupManagers() {
        this.dataManager = new DataManager();
        this.inventoryManager = new InventoryManager();
        this.taskManager = new TaskManager();
        this.cubeManager = new CubeManager();
        this.cubeBankManager = new CubeBankManager();
        this.cacheManager = new CacheManager();

        if (this.loaded) {
            this.cubeManager.load();
            this.cacheManager.load();
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.cubeManager.load();
            this.cacheManager.load();
        }, 1L);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }
        this.economy = provider.getProvider();
        return true;
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockInteraction(), this);
        getServer().getPluginManager().registerEvents(new InventoryInteraction(), this);
        getServer().getPluginManager().registerEvents(new CubeHandler(), this);
        getServer().getPluginManager().registerEvents(new ChunkHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getServer().getPluginCommand("gencubes")).setExecutor(new MainCmd());
        Objects.requireNonNull(getServer().getPluginCommand("gencubes")).setTabCompleter(new MainCmd());

        Objects.requireNonNull(getServer().getPluginCommand("cubes")).setExecutor(new CubesCmd());
        Objects.requireNonNull(getServer().getPluginCommand("cubes")).setTabCompleter(new CubesCmd());
    }

    private void loadPlayerData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.cubeBankManager.loadOrCreate(player.getUniqueId());
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " " + message));
    }

    public static void sendMessage(CommandSender sender, String message, boolean withPrefix) {
        String prefixPart = withPrefix ? getPrefix() + " " : "";
        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', prefixPart + message));
    }

    public void reloadPlugin(boolean reloadServices) {
        onDisable();
        reloadConfig();
        saveDefaultConfig();
        setupConfig();
        if (reloadServices) {
            setupServices();
        }
        HandlerList.unregisterAll(this);
        setupManagers();
        registerListeners();
        registerCommands();
        loadPlayerData();
    }

    public static GenCubes getInstance() {
        return instance;
    }

    public static String getPrefix() {
        return prefix;
    }

    public Economy getEcon() {
        return this.economy;
    }

    public List<SellPlugin> getSellPlugins() {
        return this.sellPlugins;
    }

    public List<RegionManagementPlugin> getRegionManagementPlugins() {
        return this.regionManagementPlugins;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public CubeConfiguration getCubeConfiguration() {
        return this.cubeConfiguration;
    }

    public DataManager getDataManager() {
        return this.dataManager;
    }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public CubeManager getCubeManager() {
        return this.cubeManager;
    }

    public CubeBankManager getCubeBankManager() {
        return this.cubeBankManager;
    }

    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    public CubeEditorService getCubeEditorService() {
        return this.cubeEditorService;
    }

    public CubeSeller getCubeSeller() {
        return this.cubeSeller;
    }

    public CubeSmelter getCubeSmelter() {
        return this.cubeSmelter;
    }

    public CubeCompressor getCubeCompressor() {
        return this.cubeCompressor;
    }

    public boolean isFawe() {
        return this.fawe;
    }

    public AutoMinerManager getAutoMinerManager() {
        return this.autoMinerManager;
    }

    public boolean isAdvancedChests() {
        return this.advancedChests;
    }

    public boolean isLoaded() {
        return this.loaded;
    }
}