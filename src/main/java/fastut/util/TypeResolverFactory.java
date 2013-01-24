package fastut.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import fastut.object.ObjectPool;

public class TypeResolverFactory {

    public static Random r;

    static {
        long seed = System.currentTimeMillis();
        r = new Random(seed);
    }

    public static Constructor<?> getConstructors(Class<?> type) {
        if (TypeUtil.instantiation(type)) {
            Constructor<?>[] cs = type.getConstructors();
            if (cs.length == 0) return null;
            return cs[r.nextInt(cs.length)];
        }
        return null;
    }

    public static Object newInstance(Class<?> type) {
        Object obj = ObjectPool.getObject(type);
        if (obj != null) {
            return obj;
        }
        Constructor<?> constructor = getConstructors(type);
        Type[] types = constructor.getGenericParameterTypes();
        Object[] initargs = new Object[types.length];
        if (types.length > 0) {
            for (int i = 0; i < types.length; ++i) {
                initargs[i] = newInstance((Class<?>) types[i]);
            }
        }
        try {
            return constructor.newInstance(initargs);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Map<Class<?>, TypeResolver<?>> resolvers = new HashMap<Class<?>, TypeResolver<?>>();

    public static <T> void register(Class<?> clazz, TypeResolver<T> resolver) {
        resolvers.put(clazz, resolver);
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeResolver<T> getResolver(final Class<?> clazz) {
        TypeResolver<T> resolver = (TypeResolver<T>) resolvers.get(clazz);
        if (resolver == null) {
            return new TypeResolver<T>() {

                @Override
                public T resolve() {
                    return (T) newInstance(clazz);
                }

            };
        }
        return resolver;
    }
}
