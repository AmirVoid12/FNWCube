package iran.flame.network.cube.tasks;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import iran.flame.network.cube.CubeBankAccount;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.managers.CubeBankManager;
import iran.flame.network.cube.managers.DataManager;

public class CubeClaimProcess extends PluginTask {
    private final GenCubes plugin = GenCubes.getInstance();
    private final DataManager dataManager = this.plugin.getDataManager();
    private final UUID playerId;
    private final List<String> pendingCubes;

    public CubeClaimProcess(UUID playerId, List<String> pendingCubes) {
        this.playerId = playerId;
        this.pendingCubes = pendingCubes;
    }

    @Override
    public void run() {
        Player player = Bukkit.getPlayer(this.getPlayerId());

        CubeBankManager cubeBankManager = this.plugin.getCubeBankManager();
        assert player != null;
        CubeBankAccount account = cubeBankManager.getAccountByUuid(player.getUniqueId());

        boolean hadOverflow = false;
        for (String cubeType : this.pendingCubes) {
            ItemStack icon = this.dataManager.getIcon(cubeType);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(icon);

            if (leftover.isEmpty()) {
                continue;
            }

            account.add(cubeType, 1);
            hadOverflow = true;
        }

        if (hadOverflow) {
            GenCubes.sendMessage(player, "&cSome cubes couldn't be added into your inventory, please use &6/cubes redeem &cto redeem your cubes&6!", false);
        }

        account.save();
    }

    @Override
    public void stopTask() {
        this.plugin.getCacheManager().getCacheStorage().save(this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public List<String> getPendingCubes() {
        return this.pendingCubes;
    }
}