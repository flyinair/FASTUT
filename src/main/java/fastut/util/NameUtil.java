package fastut.util;

public class NameUtil {

    public static final String MAP_SIGN         = "$fastut$map";
    public static final String MAP_KEY_SUFFIX   = "$fastut$map$key";
    public static final String MAP_VALUE_SUFFIX = "$fastut$map$value";

    public static String getMapKeyName(String name) {
        return name + MAP_KEY_SUFFIX;
    }

    public static String getMapValueName(String name) {
        return name + MAP_VALUE_SUFFIX;
    }

    public static String transform(String name) {
        int index = name.indexOf(MAP_SIGN);
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
