package iran.flame.network.cube.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {

    public static String serializeLoc(Location location) {
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : Bukkit.getWorlds().get(0).getName();

        return "@w;" + worldName +
                ":@x;" + location.getX() +
                ":@y;" + location.getY() +
                ":@z;" + location.getZ() +
                ":@pi;" + location.getPitch() +
                ":@ya;" + location.getYaw();
    }

    public static Location deserializeLoc(String serialized) {
        Location location = new Location(Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0, 0.0f, 0.0f);

        String[] parts = serialized.split(":");
        for (String part : parts) {
            String[] keyValue = part.split(";");
            String key = keyValue[0];
            String value = keyValue[1];

            switch (key.toLowerCase()) {
                case "@w":
                    location.setWorld(Bukkit.getWorld(value));
                    break;
                case "@x":
                    location.setX(Double.parseDouble(value));
                    break;
                case "@y":
                    location.setY(Double.parseDouble(value));
                    break;
                case "@z":
                    location.setZ(Double.parseDouble(value));
                    break;
                case "@pi":
                    location.setPitch((float) Double.parseDouble(value));
                    break;
                case "@ya":
                    location.setYaw((float) Double.parseDouble(value));
                    break;
            }
        }

        return location;
    }

    public static boolean verify(Location location) {
        return location != null && location.getWorld() != null;
    }
}