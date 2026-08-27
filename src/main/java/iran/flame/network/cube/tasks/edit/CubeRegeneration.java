package iran.flame.network.cube.tasks.edit;

import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.gencube.GenCube;
import iran.flame.network.cube.tasks.PluginTask;

public class CubeRegeneration extends PluginTask {
    private final GenCubes plugin = GenCubes.getInstance();
    private final GenCube cube;

    public CubeRegeneration(GenCube cube) {
        super(GenCubes.getInstance().getDataManager().getRegenerationTime(cube.getType()), true);
        this.cube = cube;
        this.setDelay(this.plugin.getDataManager().getRegenerationTime(cube.getType()));
        this.runTask();
    }

    @Override
    public void run() {
        if (!this.cube.getChunkLocation().isLoaded()) {
            this.cube.stop();
            return;
        }

        boolean skipAnimation = (Boolean) this.plugin.getConfiguration()
                .getOptions()
                .get("fastasyncworldedit")
                .get("show_block_animation_on_regeneration") == false;

        this.cube.build(
                this.plugin.getDataManager().getRegenerationType(this.cube.getType()),
                this.plugin.getDataManager().getRegenerationQuantityType(this.cube.getType()),
                this.plugin.getDataManager().getRegenerationQuantityValue(this.cube.getType()),
                skipAnimation
        );
    }
}