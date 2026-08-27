package iran.flame.network.cube.tasks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import iran.flame.network.cube.GenCubes;
import iran.flame.network.cube.managers.TaskManager;
import iran.flame.network.cube.utils.CubeUtils;
import iran.flame.network.cube.utils.Cuboid;

public class GenCuboidPreview extends PluginTask {
    private final TaskManager taskManager = GenCubes.getInstance().getTaskManager();
    private final Cuboid cuboid;
    private final Location origin;
    private final Player player;
    private Integer elapsedTicks;
    private final List<Location> previewLocations;

    public GenCuboidPreview(Cuboid cuboid, Location origin, Player player) {
        super(20L, true);
        this.cuboid = cuboid;
        this.origin = origin;
        this.player = player;

        this.previewLocations = buildPreviewLocations();
        this.elapsedTicks = 0;
        this.runTask();
    }

    @Override
    public void run() {
        this.elapsedTicks += 20;
        try {
            Particle particle = CubeUtils.isASafeLocationToBuild(this.cuboid, this.origin, this.player, new ArrayList<>())
                    ? Particle.VILLAGER_HAPPY
                    : Particle.SMOKE_NORMAL;
            for (Location location : this.previewLocations) {
                this.player.spawnParticle(particle, location, 3, 0.0, 0.0, 0.0, 0.0);
            }
        } catch (NoClassDefFoundError | NoSuchFieldError modernApiMissing) {
            playLegacyEffect();
        }

        if (this.elapsedTicks >= 180) {
            this.stopTask();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void playLegacyEffect() {
        try {
            boolean safe = CubeUtils.isASafeLocationToBuild(this.cuboid, this.origin, this.player, new ArrayList<>());
            String effectName = safe ? "HAPPY_VILLAGER" : "SMOKE";

            Class<?> effectClass = Class.forName("org.bukkit.Effect");
            Object effect = Enum.valueOf((Class<Enum>) effectClass, effectName);

            Object spigot = this.player.getClass().getMethod("spigot").invoke(this.player);
            Method playEffect = spigot.getClass().getMethod(
                    "playEffect",
                    Location.class, effectClass, int.class, int.class,
                    float.class, float.class, float.class, float.class,
                    int.class, int.class
            );

            for (Location location : this.previewLocations) {
                playEffect.invoke(spigot, location, effect, 1, 0, 0.0f, 0.0f, 0.0f, 0.0f, 3, 5000);
            }
        } catch (ReflectiveOperationException ignored) { }
    }

    @Override
    public void stopTask() {
        super.stopTask();
        this.taskManager.unRegister(this);
    }

    private List<Location> buildPreviewLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        List<Block> corners = Arrays.asList(this.cuboid.corners());

        for (Cuboid border : this.getCuboid().getBorders()) {
            for (Block block : border) {
                Location location = block.getLocation().clone();

                if (block.getY() == this.cuboid.getUpperY()) {
                    locations.add(location.add(0.0, 1.0, 0.0));
                }
                if (block.getZ() == this.cuboid.getUpperZ()) {
                    locations.add(location.add(0.0, 0.0, 1.0));
                }
                if (block.getX() == this.cuboid.getUpperX()) {
                    locations.add(location.add(1.0, 0.0, 0.0));
                }

                if (corners.contains(block)) {
                    if (block.getY() == this.cuboid.getUpperY()) {
                        locations.add(location.clone().add(0.0, -1.0, 0.0));
                    }
                    if (block.getZ() == this.cuboid.getUpperZ()) {
                        locations.add(location.clone().add(0.0, 0.0, -1.0));
                    }
                    if (block.getX() == this.cuboid.getUpperX()) {
                        locations.add(location.clone().add(-1.0, 0.0, 0.0));
                    }
                }

                locations.add(location);
            }
        }
        return locations;
    }

    public Cuboid getCuboid() {
        return this.cuboid;
    }

    public Location getOrigin() {
        return this.origin;
    }

    public Player getPlayer() {
        return this.player;
    }
}