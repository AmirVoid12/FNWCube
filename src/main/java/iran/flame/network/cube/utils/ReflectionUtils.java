package iran.flame.network.cube.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import iran.flame.network.cube.GenCubes;

public class ReflectionUtils {
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Class not found: " + className, e);
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Constructor not found in class: " + clazz.getName(), e);
            return null;
        }
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Failed to create instance via constructor: " + constructor, e);
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Method not found: " + methodName + " in class: " + clazz.getName(), e);
            return null;
        }
    }

    public static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            GenCubes.getInstance().getLogger().log(Level.SEVERE, "Failed to invoke method: " + method.getName(), e);
            return null;
        }
    }
}