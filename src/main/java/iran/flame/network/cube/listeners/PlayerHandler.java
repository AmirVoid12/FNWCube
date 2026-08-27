package iran.flame.network.cube.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import iran.flame.network.cube.CubeBankAccount;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.CubeBankManager;
import iran.flame.network.cube.managers.CubeManager;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.services.autominer.AutoMinerManager;
import iran.flame.network.cube.tasks.CubeClaimProcess;

public class PlayerHandler implements Listener {
    private final TaskManager taskManager;
    private final CubeBankManager cubeBankManager;
    private final CubeManager cubeManager;
    private final AutoMinerManager autoMinerManager;

    public PlayerHandler() {
        GenCubes plugin = GenCubes.getInstance();
        this.taskManager = plugin.getTaskManager();
        this.cubeBankManager = plugin.getCubeBankManager();
        this.cubeManager = plugin.getCubeManager();
        this.autoMinerManager = plugin.getAutoMinerManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.cubeBankManager.loadOrCreate(player.getUniqueId());
        CubeClaimProcess claimProcess = this.taskManager.getCubeClaimProcessByPlayer(player);
        if (claimProcess != null) {
            claimProcess.run();
            this.taskManager.unRegister(claimProcess);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CubeBankAccount account = this.cubeBankManager.getAccountByUuid(player.getUniqueId());
        this.cubeBankManager.unRegister(account);

        for (GenCube cube : this.cubeManager.getCubesByOwner(player.getUniqueId())) {
            this.autoMinerManager.unRegister(cube);
        }
    }
}