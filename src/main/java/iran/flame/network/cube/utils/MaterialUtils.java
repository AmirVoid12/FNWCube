package iran.flame.network.cube.utils;

import iran.flame.network.cube.enums.XMaterial;
import iran.flame.network.cube.interfaces.Versionable;
import org.bukkit.Material;

public class MaterialUtils {

    public static Material getLegacy(XMaterial material) {
        String[] legacyNames = material.getLegacy();
        if (legacyNames == null || legacyNames.length == 0) {
            return material.parseMaterial();
        }

        String legacyName = legacyNames[0];

        String targetName = Versionable.IS_LEGACY ? "LEGACY_" + legacyName : legacyName;

        try {
            return Material.valueOf(targetName);
        } catch (IllegalArgumentException e) {
            return material.parseMaterial();
        }
    }
}