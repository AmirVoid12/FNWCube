package iran.flame.network.cube.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    public static double getRandomNumber(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}