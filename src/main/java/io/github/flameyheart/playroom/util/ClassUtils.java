package io.github.flameyheart.playroom.util;

public class ClassUtils {
    public static boolean exists(String className) {
        try {
            Class.forName(className, false, ClassUtils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
