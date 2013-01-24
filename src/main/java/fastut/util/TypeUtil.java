package fastut.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

public class TypeUtil {

    public static String getMethodSimpleName(Method method) {
        StringBuffer sb = new StringBuffer();
        sb.append(Modifier.toString(method.getModifiers()) + " ");
        sb.append(getTypeSimpleName(method.getGenericReturnType()) + " ");
        sb.append(method.getName());
        sb.append("(");
        Type[] types = method.getGenericParameterTypes();

        for (int i = 0; i < types.length; i++) {
            sb.append(getTypeSimpleName(types[i]) + ",");
        }
        String s = sb.toString();

        if (s.endsWith(",")) {
            return s.substring(0, s.length() - 1) + ")";
        }
        return s + ")";
    }

    public static String getTypeSimpleName(Type type) {
        int i;
        if ((type instanceof Class)) {
            Class<?> t = (Class<?>) type;
            int n = 0;
            while (t.isArray()) {
                t = t.getComponentType();
                n++;
            }
            StringBuffer sb = new StringBuffer();
            sb.append(t.getSimpleName());
            for (i = 0; i < n; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }

        if ((type instanceof ParameterizedType)) {
            ParameterizedType t = (ParameterizedType) type;
            StringBuffer sb = new StringBuffer();
            sb.append(getTypeSimpleName(t.getRawType()));
            sb.append("<");
            for (Type tt : t.getActualTypeArguments()) {
                sb.append(getTypeSimpleName(tt));
                sb.append(",");
            }
            String r = sb.toString();
            if (r.endsWith(",")) r = r.substring(0, r.length() - 1);
            return r + ">";
        }

        if ((type instanceof GenericArrayType)) {
            GenericArrayType g = (GenericArrayType) type;
            StringBuffer sb = new StringBuffer();
            sb.append(getTypeSimpleName(g.getGenericComponentType()));
            sb.append("[]");
            return sb.toString();
        }

        if ((type instanceof WildcardType)) {
            return type.toString();
        }

        return type.toString();
    }

    public static String getPackageName(String className) {
        int index = className.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return className.substring(0, index);
    }

    private static boolean needImport(Class<?> c) {
        if (c.isPrimitive()) return false;
        if (c.equals(Void.TYPE)) {
            return false;
        }
        return !getPackageName(c.getName()).equals("java.lang");
    }

    public static Set<Class<?>> getImports(Type t) {
        Set<Class<?>> r = new HashSet<Class<?>>();
        if ((t instanceof Class)) {
            Class<?> c = (Class<?>) t;
            if (c.isArray()) r.addAll(getImports(c.getComponentType()));
            else if (needImport(c)) r.add(c);
        } else if ((t instanceof ParameterizedType)) {
            ParameterizedType p = (ParameterizedType) t;
            if (needImport((Class<?>) p.getRawType())) r.add((Class<?>) p.getRawType());
            for (Type tt : p.getActualTypeArguments())
                r.addAll(getImports(tt));
        } else if ((t instanceof GenericArrayType)) {
            r.addAll(getImports(((GenericArrayType) t).getGenericComponentType()));
        }
        return r;
    }

    public static boolean instantiation(Class<?> type) {
        return (!type.isInterface()) && (!Modifier.isAbstract(type.getModifiers()));
    }
}
