package iran.flame.network.cube.services.autominer;

import java.util.HashMap;
import java.util.Map;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.enums.AutoMinerStatus;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.managers.DataManager;

public class AutoMinerManager {
    private final Map<GenCube, AutoMinerTask> activeAutoMiners = new HashMap<>();

    private DataManager getDataManager() {
        return GenCubes.getInstance().getDataManager();
    }

    public void register(GenCube cube, AutoMinerTask task) {
        this.activeAutoMiners.put(cube, task);
    }

    public void unRegister(GenCube cube) {
        AutoMinerTask task = this.activeAutoMiners.remove(cube);
        if (task != null) {
            task.stopTask();
        }
    }

    public AutoMinerTask getAutoMinerByCube(GenCube cube) {
        return this.activeAutoMiners.get(cube);
    }

    public boolean isRunning(GenCube cube) {
        return this.activeAutoMiners.containsKey(cube);
    }

    public AutoMinerStatus toggle(GenCube cube) {
        if (!this.getDataManager().getAutoMinerAvailability(cube.getType())) {
            return AutoMinerStatus.NOT_AVAILABLE;
        }

        if (this.isRunning(cube)) {
            this.unRegister(cube);
            return AutoMinerStatus.STOPPED;
        }

        AutoMinerTask task = new AutoMinerTask(cube);
        this.register(cube, task);
        return AutoMinerStatus.STARTED;
    }

    public Map<GenCube, AutoMinerTask> getActiveAutoMiners() {
        return this.activeAutoMiners;
    }
}