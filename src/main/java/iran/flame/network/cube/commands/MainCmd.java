package iran.flame.network.cube.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.managers.DataManager;
import iran.flame.network.cube.utils.inventory.InventoryUtils;

public class MainCmd implements CommandExecutor, TabCompleter {
    private static final int MAX_GIVE_AMOUNT = 500;
    private static final List<String> SUBCOMMANDS = List.of("help", "give", "list", "reload");

    private final GenCubes plugin = GenCubes.getInstance();
    private final DataManager dataManager = this.plugin.getDataManager();

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("gencubes.admin") || args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (args.length >= 2) {
                    handleGive(sender, args);
                } else {
                    sendHelp(sender);
                }
            }
            case "list" -> sendCubeList(sender);
            case "reload" -> {
                GenCubes.sendMessage(sender, "&7The config was reloaded correctly&6!");
                this.plugin.reloadPlugin(true);
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("gencubes.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return filterStartsWith(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return filterStartsWith(playerNames, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> cubeNames = new ArrayList<>(this.dataManager.getCubesNames());
            return filterStartsWith(cubeNames, args[2]);
        }

        return List.of();
    }

    private static List<String> filterStartsWith(List<String> options, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }

    private void handleGive(CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            GenCubes.sendMessage(sender, "&cThis player is not online&6!");
            return;
        }

        if (args.length < 3) {
            GenCubes.sendMessage(sender, "&cYou must specify a cube name&6!");
            return;
        }

        String cubeType = args[2];

        if (this.dataManager.isANonLoadedCube(cubeType)) {
            GenCubes.sendMessage(sender, "&cThis cube was not loaded correctly please check the config&6!");
            return;
        }

        if (!this.dataManager.isACube(cubeType)) {
            GenCubes.sendMessage(sender, "&cInvalid cube name&6!");
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
                GenCubes.sendMessage(sender, "&cInvalid cube amount&6!");
                return;
            }

            if (amount <= 0) {
                GenCubes.sendMessage(sender, "&cInvalid cube amount&6!");
                return;
            }

            if (amount > MAX_GIVE_AMOUNT) {
                GenCubes.sendMessage(sender, "&cThe amount can't be greater than " + MAX_GIVE_AMOUNT + "&6!");
                return;
            }
        }

        ItemStack cubeIcon = this.dataManager.getIcon(cubeType);
        int availableSpace = InventoryUtils.getSpacesForItem(target.getInventory(), cubeIcon, true);

        if (availableSpace >= amount) {
            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(cubeIcon);
            }
            GenCubes.sendMessage(sender, "&aGave " + amount + " " + cubeType + " cube(s) to " + target.getName() + "&6!");
        } else {
            for (int i = 0; i < amount; i++) {
                target.getWorld().dropItem(target.getLocation(), cubeIcon);
            }
            GenCubes.sendMessage(sender, "&cThe player's inventory was full, the cube(s) were dropped on the ground&6!");
        }
    }

    private void sendCubeList(CommandSender sender) {
        Set<String> cubeNames = this.dataManager.getCubesNames();

        if (cubeNames.isEmpty()) {
            GenCubes.sendMessage(sender, "&cAvailable cubes&9: &8[&7&8]");
            return;
        }

        String joined = cubeNames.stream().collect(Collectors.joining("&8,&7", "&8[&7", "&8]"));
        GenCubes.sendMessage(sender, "&cAvailable cubes&9: " + joined);
    }

    private static void sendHelp(CommandSender sender) {
        GenCubes.sendMessage(sender, "&f&m-------------&7=&8[&9&lGen&a&lCubes&8]&7=&f&m-------------", false);
        GenCubes.sendMessage(sender, "&8- &9/&cgc help &f- &8(&7Show all the commands&8) ", false);
        GenCubes.sendMessage(sender, "&8- &9/&cgc reload &f- &8(&7Reload the plugin&8)", false);
        GenCubes.sendMessage(sender, "&8- &9/&cgc list &f- &8(&7Get a list of the available cubes&8)", false);
        GenCubes.sendMessage(sender, "&8- &9/&cgc give &9<&cplayer&9> &9<&ccube_name&9> &9<&camount&9> &f- &8(&7Give a cube to a player &8)", false);
        GenCubes.sendMessage(sender, "&8- &9/&ccubes info &f- &8(&7See all the available cubes in your bank&8)", false);
        GenCubes.sendMessage(sender, "&8- &9/&ccubes redeem &f- &8(&7Redeem the available cubes in your bank&8)", false);
        GenCubes.sendMessage(sender, "&f&m---------------------------------------", false);
    }
}