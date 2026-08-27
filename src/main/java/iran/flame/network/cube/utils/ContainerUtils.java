package iran.flame.network.cube.utils;

import iran.flame.network.cube.interfaces.Versionable;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;

public final class ContainerUtils {

    private ContainerUtils() {
    }

    public static boolean isAContainer(BlockState blockState) {
        if (blockState == null) {
            return false;
        }
        if (Versionable.IS_LEGACY) {
            return isLegacyContainer(blockState);
        }
        return blockState instanceof Container;
    }

    public static Inventory getContainerInventory(BlockState blockState) {
        if (blockState == null) {
            return null;
        }
        if (Versionable.IS_LEGACY) {
            return getLegacyContainerInventory(blockState);
        }
        if (blockState instanceof Container container) {
            return container.getInventory();
        }
        return null;
    }

    private static boolean isLegacyContainer(BlockState blockState) {
        Class<?> containerBlockClass = ReflectionUtils.getClass("org.bukkit.block.ContainerBlock");
        if (containerBlockClass == null) {
            return false;
        }
        return containerBlockClass.isInstance(blockState);
    }

    private static Inventory getLegacyContainerInventory(BlockState blockState) {
        try {
            Class<?> containerBlockClass = ReflectionUtils.getClass("org.bukkit.block.ContainerBlock");
            if (containerBlockClass == null || !containerBlockClass.isInstance(blockState)) {
                return null;
            }
            Method getInventoryMethod = ReflectionUtils.getMethod(containerBlockClass, "getInventory");
            if (getInventoryMethod == null) {
                return null;
            }
            Object result = ReflectionUtils.invokeMethod(getInventoryMethod, blockState);
            return result instanceof Inventory inventory ? inventory : null;
        } catch (Exception e) {
            return null;
        }
    }
}