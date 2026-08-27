package iran.flame.network.cube.utils;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public class FileUtils {
    public static void create(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File file, YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }
}