package iran.flame.network.cube.interfaces;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Versionable {

    Pattern VERSION_PATTERN = Pattern.compile("\\(MC:\\s*(\\d+)\\.(\\d+)(?:\\.(\\d+))?\\)");

    int MAJOR_VERSION = parseMajor(Bukkit.getVersion());
    int MINOR_VERSION = parseMinor(Bukkit.getVersion());
    int PATCH_VERSION = parsePatch(Bukkit.getVersion());

    boolean IS_1_16 = MINOR_VERSION == 16;
    boolean IS_1_17 = MINOR_VERSION == 17;
    boolean IS_1_18 = MINOR_VERSION == 18;
    boolean IS_1_19 = MINOR_VERSION == 19;
    boolean IS_1_20 = MINOR_VERSION == 20;
    boolean IS_1_21 = MINOR_VERSION == 21;

    boolean IS_LEGACY = MINOR_VERSION >= 7 && MINOR_VERSION <= 12;

    static boolean isAtLeast(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }

    static boolean isAtMost(int major, int minor) {
        return MAJOR_VERSION < major || (MAJOR_VERSION == major && MINOR_VERSION <= minor);
    }

    private static Matcher matcher(String rawVersion) {
        Matcher matcher = VERSION_PATTERN.matcher(rawVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Unable to parse server version from: " + rawVersion);
        }
        return matcher;
    }

    static int parseMajor(String rawVersion) {
        return Integer.parseInt(matcher(rawVersion).group(1));
    }

    static int parseMinor(String rawVersion) {
        return Integer.parseInt(matcher(rawVersion).group(2));
    }

    static int parsePatch(String rawVersion) {
        String patch = matcher(rawVersion).group(3);
        return patch != null ? Integer.parseInt(patch) : 0;
    }
}