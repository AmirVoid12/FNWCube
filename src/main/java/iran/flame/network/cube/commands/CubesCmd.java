package iran.flame.network.cube.commands;

import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import iran.flame.network.cube.CubeBankAccount;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.managers.CubeBankManager;
import iran.flame.network.cube.enums.Sound;

public class CubesCmd implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("redeem", "info");

    private final GenCubes plugin = GenCubes.getInstance();
    private final CubeBankManager cubeBankManager = this.plugin.getCubeBankManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            GenCubes.sendMessage(sender, "&cThis command can only be used by players&6!");
            return true;
        }

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "redeem" -> {
                    handleRedeem(sender, player);
                    return true;
                }
                case "info" -> {
                    handleInfo(sender, player);
                    return true;
                }
            }
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            String lowerPrefix = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(sub -> sub.startsWith(lowerPrefix))
                    .toList();
        }
        return List.of();
    }

    private void handleRedeem(CommandSender sender, Player player) {
        CubeBankAccount account = this.cubeBankManager.getAccountByUuid(player.getUniqueId());
        Map<String, Integer> cubesBeforeRedeem = Map.copyOf(account.getCubes());

        if (cubesBeforeRedeem.isEmpty()) {
            GenCubes.sendMessage(sender, "&cYou don't have any cubes to redeem!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
            return;
        }

        boolean redeemed = account.redeem(player);

        if (redeemed) {
            GenCubes.sendMessage(sender, "&aYou redeemed all of your cubes!", false);
            player.playSound(player.getLocation(), Sound.LEVEL_UP.bukkitSound(), 100.0f, 1.0f);
        } else {
            GenCubes.sendMessage(sender, "&cYou don't have enough inventory space to redeem all of your cubes!", false);
            player.playSound(player.getLocation(), Sound.ANVIL_LAND.bukkitSound(), 100.0f, 1.0f);
        }
    }

    private void handleInfo(CommandSender sender, Player player) {
        CubeBankAccount account = this.cubeBankManager.getAccountByUuid(player.getUniqueId());
        Map<String, Integer> cubes = account.getCubes();

        GenCubes.sendMessage(sender, "&6Your Cube Bank:", false);

        if (cubes.isEmpty()) {
            GenCubes.sendMessage(sender, "&7You don't have any cubes in your bank.", false);
            return;
        }

        for (Map.Entry<String, Integer> entry : cubes.entrySet()) {
            String line = "&7- &f" + entry.getKey() + " &7x&f" + entry.getValue();
            GenCubes.sendMessage(sender, line, false);
        }
    }

    private void sendHelp(CommandSender sender) {
        GenCubes.sendMessage(sender, "&6&lGenCubes Help", false);
        GenCubes.sendMessage(sender, "&7/cubes redeem &f- Redeem all cubes in your bank", false);
        GenCubes.sendMessage(sender, "&7/cubes info &f- View the cubes in your bank", false);
    }
}