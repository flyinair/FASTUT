package fastut.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.objectweb.asm.Type;

public class ClassUtil {

    public static Class<?> loadClass(Type type, ClassLoader loader) throws ClassNotFoundException {
        if (type.getSort() == Type.OBJECT) {
            return loader.loadClass(type.getClassName());
        }
        if (type.getSort() == Type.ARRAY) {
            Class<?> arrayClass = loadClass(type.getElementType(), loader);
            for (int i = 0; i < type.getDimensions(); ++i) {
                arrayClass = Array.newInstance(arrayClass, 0).getClass();
            }
            return arrayClass;
        }
        if (type.getSort() == Type.VOID) {
            return Void.TYPE;
        }
        return getPrimitiveClass(type.getClassName());
    }

    public static Class<?> getPrimitiveClass(String name) {
        if (name.length() <= 8) {
            for (Class<?> clazz : PRIMITIVE_CLASS) {
                if (clazz.getName().equals(name)) {
                    return clazz;
                }
            }
        }
        return null;
    }

    public static Class<?>[] loadClass(Type[] types, ClassLoader loader) throws ClassNotFoundException {
        Class<?>[] clazzes = new Class<?>[types.length];
        for (int i = 0; i < types.length; ++i) {
            clazzes[i] = loadClass(types[i], loader);
        }
        return clazzes;
    }

    public static Method getMethod(Class<?> targetClass, String methodName, String methodDesc)
                                                                                              throws NoSuchMethodException,
                                                                                              ClassNotFoundException {
        Class<?>[] argumentClasses = loadClass(Type.getArgumentTypes(methodDesc), targetClass.getClassLoader());
        Class<?> returnClass = loadClass(Type.getReturnType(methodDesc), targetClass.getClassLoader());
        return getMethod(targetClass, methodName, argumentClasses, returnClass);
    }

    public static String makeParamBody(Class<?>[] paramClasses) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (paramClasses != null) {
            for (int i = 0; i < paramClasses.length; ++i) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(paramClasses[i] == null ? "null" : paramClasses[i].getName());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    public static Method getMethod(Class<?> targetClass, String methodName, Class<?>[] argumentClasses,
                                   Class<?> returnClass) throws NoSuchMethodException {
        Method[] methods = targetClass.getDeclaredMethods();
        for (Method method : methods) {
            if ((method.getName().equals(methodName)) && (Arrays.equals(method.getParameterTypes(), argumentClasses))
                && (method.getReturnType().equals(returnClass))) {
                return method;
            }
        }
        throw new NoSuchMethodException(returnClass.getName() + " " + targetClass.getName() + "." + methodName
                                        + makeParamBody(argumentClasses));
    }

    static Class<?>[] PRIMITIVE_CLASS = new Class<?>[] { Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE };
}
