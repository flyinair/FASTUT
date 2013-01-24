package fastut.util;

import net.vidageek.mirror.dsl.AccessorsController;
import net.vidageek.mirror.dsl.Mirror;

public class ObjectSelector {

    static final String DELIMITER = "\\.";

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    public static Object get(Object target, String condition) {
        String[] paths = condition.split(DELIMITER);
        if (paths != null) {
            AccessorsController controller = new Mirror().on(target);
            Object field = null;
            for (String path : paths) {
                path = path.replace(" ", "");
                if (!isBlank(path)) {
                    field = controller.get().field(path);
                    if (field == null) {
                        return null;
                    }
                    controller = new Mirror().on(field);
                }
            }
            return field;
        }
        return null;
    }

    public static void set(Object target, String condition, Object value) {
        String[] paths = condition.split(DELIMITER);
        if (paths != null && paths.length >= 1) {
            AccessorsController controller = new Mirror().on(target);
            Object field = null;
            String path = null;
            for (int i = 0; i < paths.length - 1; ++i) {
                path = paths[i];
                path = path.replace(" ", "");
                if (!isBlank(path)) {
                    field = controller.get().field(path);
                    if (field == null) {
                        return;
                    }
                    controller = new Mirror().on(field);
                }
            }
            path = paths[paths.length - 1].replace(" ", "");
            if (!isBlank(path)) {
                if (field != null) {
                    new Mirror().on(field).set().field(path).withValue(value);
                } else {
                    new Mirror().on(target).set().field(path).withValue(value);
                }
            }
        }
    }

    public static Object invoke(String className, String methodName, Object... args) {
        try {
            return new Mirror().on(Thread.currentThread().getContextClassLoader().loadClass(className)).invoke().method(methodName).withArgs(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invoke(Object target, String methodName, Object... args) {
        try {
            return new Mirror().on(target).invoke().method(methodName).withArgs(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
