package fastut.util;

import java.util.ArrayList;

import fastut.util.generics.type.ListSignaturedType;
import fastut.util.generics.type.SignaturedType;

public class SignatureUtil {

    static final ClassLoader loader = new ClassLoader() {
                                    };

    static interface ContainerType {

        static enum ListType implements ContainerType {
            ARRAY_LIST;
        }

        static enum SetType implements ContainerType {
            HASH_SET;
        }

        static enum MapType implements ContainerType {
            HASH_MAP;
        }
    }

    static class MakedObject {
        ContainerType type;
        Object value;
    }

    static void makeObject(SignaturedType type) {
        try {
            if (type instanceof ListSignaturedType) {
                Class<?> clazz = Initializer.loadClass(type.getType(), loader);
                if (ArrayList.class.isAssignableFrom(clazz)) {

                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // static void make

    public static void main(String[] args) {
    }
}
