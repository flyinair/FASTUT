package fastut.util;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;

public class TypeMatcher {

    static final ClassLoader loader = new ClassLoader() {
                                    };

    static Type asType(String internalName) {
        return Type.getType('L' + internalName + ';');
    }

    static Class<?> loadClassByType(Type type) {
        try {
            return ClassUtil.loadClass(type, loader);
        } catch (Throwable e) {
            return null;
        }
    }

    static boolean isAssignable(Type left, Type right) {
        Class<?> leftClass = loadClassByType(left);
        if (leftClass == null) {
            return false;
        }
        Class<?> rightClass = loadClassByType(right);
        if (rightClass == null) {
            return false;
        }
        return leftClass.isAssignableFrom(rightClass);
    }

    static boolean isInstantiation(Type type) {
        return TypeUtil.instantiation(loadClassByType(type));
    }

    public static Set<Type> match(Type varType, Set<String> allTypes) {
        Set<Type> matched = new HashSet<Type>();
        if (allTypes != null) {
            for (String typeStr : allTypes) {
                Type type = asType(typeStr);
                if (isAssignable(varType, type) && isInstantiation(type)) {
                    matched.add(type);
                }
            }
        }
        return matched;
    }

    public static void main(String[] args) {
        System.err.println(isAssignable(Type.getType(Object.class), Type.getType(String.class)));
    }
}
