package iran.flame.network.cube.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.tasks.CubeClaimProcess;
import iran.flame.network.cube.tasks.GenCuboidPreview;
import iran.flame.network.cube.tasks.PluginTask;
import iran.flame.network.cube.tasks.edit.CubeEdition;
import iran.flame.network.cube.enums.EditTask;

public class TaskManager {
    private final CopyOnWriteArrayList<PluginTask> tasks = new CopyOnWriteArrayList<>();

    public void register(PluginTask task) {
        this.tasks.add(task);
    }

    public void unRegister(PluginTask task) {
        this.tasks.remove(task);
    }

    public CubeEdition getCubeEditionByCube(GenCube cube) {
        for (PluginTask task : this.tasks) {
            if (!(task instanceof CubeEdition cubeEdition)) continue;
            if (!cubeEdition.getCube().equals(cube)) continue;
            return cubeEdition;
        }
        return null;
    }

    public GenCuboidPreview getGenCuboidPreviewByPlayer(Player player) {
        for (PluginTask task : this.tasks) {
            if (!(task instanceof GenCuboidPreview preview)) continue;
            if (!preview.getPlayer().equals(player)) continue;
            return preview;
        }
        return null;
    }

    public boolean isCubeBeingRemovedAtLocation(Location location) {
        for (PluginTask task : this.tasks) {
            if (!(task instanceof CubeEdition cubeEdition)) continue;
            if (cubeEdition.getEditTask() != EditTask.SET) continue;
            if (!cubeEdition.getCube().getMainCuboid().contains(location)) continue;
            return true;
        }
        return false;
    }

    public CubeClaimProcess getCubeClaimProcessByPlayer(Player player) {
        for (PluginTask task : this.tasks) {
            if (!(task instanceof CubeClaimProcess claimProcess)) continue;
            if (!player.getUniqueId().equals(claimProcess.getPlayerId())) continue;
            return claimProcess;
        }
        return null;
    }

    public List<CubeClaimProcess> getCubeClaimProcesses() {
        ArrayList<CubeClaimProcess> result = new ArrayList<>();
        for (PluginTask task : this.tasks) {
            if (!(task instanceof CubeClaimProcess)) continue;
            result.add((CubeClaimProcess) task);
        }
        return result;
    }

    public CopyOnWriteArrayList<PluginTask> getTasks() {
        return this.tasks;
    }
}