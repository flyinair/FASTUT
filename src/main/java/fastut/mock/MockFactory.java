package fastut.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import fastut.generate.TestDataGenerator;
import fastut.object.ObjectPool;

public class MockFactory implements Opcodes {

    private static Map<String, WeakReference<Class<?>>> factory       = new HashMap<String, WeakReference<Class<?>>>();

    private static ThreadLocal<InnerLoader>             currentLoader = new ThreadLocal<InnerLoader>() {

                                                                          @Override
                                                                          protected InnerLoader initialValue() {
                                                                              return new InnerLoader();
                                                                          }

                                                                      };

    public static InnerLoader currentLoader() {
        return currentLoader.get();
    }

    public static class InnerLoader extends ClassLoader {

        private static java.security.ProtectionDomain DOMAIN;

        static {
            DOMAIN = (java.security.ProtectionDomain) java.security.AccessController.doPrivileged(new PrivilegedAction<Object>() {

                public Object run() {
                    return InnerLoader.class.getProtectionDomain();
                }
            });
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> loaded = this.findLoadedClass(name);
            if (loaded != null) {
                return loaded;
            }
            if (!((name != null) && (name.startsWith("java.") || name.startsWith("sun.")))) {
                if (!name.equals("fastut.mock.MockUp") && !name.startsWith("fastut.")) {
                    try {
                        TestDataGenerator generator = new TestDataGenerator(name);
                        return publicDefineClass(name, generator.getCode());
                    } catch (Throwable e) {
                        e.printStackTrace();
                        super.loadClass(name);
                    }
                }
            }
            return super.loadClass(name);
        }

        public Class<?> publicDefineClass(String name, byte[] code) {
            return defineClass(name, code, 0, code.length, DOMAIN);
        }
    }

    public static Class<?> mock(String className, InnerLoader innerLoader) {
        if ((className != null) && className.startsWith("java.")) {
            System.err.println("FastUT Mock: Prohibited package name: "
                               + className.substring(0, className.lastIndexOf('.')));
            return null;
        }
        WeakReference<Class<?>> mockClassRef = factory.get(className);
        if (mockClassRef != null) {
            Class<?> retClass = mockClassRef.get();
            if (retClass != null) {
                return retClass;
            }
        }
        try {
            ClassReader cr = new ClassReader(className);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            Collector collector = new Collector(cw);
            cr.accept(collector, ClassReader.SKIP_DEBUG);
            // mock parent first
            ClassNode node = collector.getFirstClassNode();
            if (node.superName != null && !"java/lang/Object".equals(node.superName)) {
                mockNormal(node.superName.replace('/', '.'), innerLoader);
            }
            if (collector.hasAbstract()) {
                // mock parent first
                mockNormal(className.replace('/', '.'), innerLoader);
                return mockAbstract(collector, className + "$$fastutmock", className, innerLoader);
            } else {
                return mockNormal(className, innerLoader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> mock(String className) {
        if ((className != null) && className.startsWith("java.")) {
            System.err.println("FastUT Mock: Prohibited package name: "
                               + className.substring(0, className.lastIndexOf('.')));
            return null;
        }
        WeakReference<Class<?>> mockClassRef = factory.get(className);
        if (mockClassRef != null) {
            Class<?> retClass = mockClassRef.get();
            if (retClass != null) {
                return retClass;
            }
        }
        try {
            InnerLoader innerLoader = currentLoader();
            ClassReader cr = new ClassReader(className);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            Collector collector = new Collector(cw);
            cr.accept(collector, ClassReader.SKIP_DEBUG);
            // mock parent first
            ClassNode node = collector.getFirstClassNode();
            if (node.superName != null && !"java/lang/Object".equals(node.superName)) {
                mockNormal(node.superName.replace('/', '.'), innerLoader);
            }
            if (collector.hasAbstract()) {
                // mock parent first
                mockNormal(className.replace('/', '.'), innerLoader);
                return mockAbstract(collector, className + "$$fastutmock", className, innerLoader);
            } else {
                return mockNormal(className, innerLoader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static Class<?> mockAbstract(Collector collector, String className, String originalClassName,
                                 InnerLoader innerLoader) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassNode classNode = collector.getFirstClassNode();
        String innerClassName = classNode.name.replace('.', '/');
        className = className.replace('.', '/');
        if (Modifier.isInterface(classNode.access)) {
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[] { innerClassName });
        } else {
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, innerClassName, new String[] {});
        }

        MethodNode initNode = collector.getSimplestInitNode();
        if (initNode == null) {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            if (Modifier.isInterface(classNode.access)) {
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            } else {
                mv.visitMethodInsn(INVOKESPECIAL, innerClassName, "<init>", "()V");
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        } else {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                                              "<init>",
                                              initNode.desc,
                                              initNode.signature,
                                              (String[]) initNode.exceptions.toArray(new String[initNode.exceptions.size()]));
            GeneratorAdapter generator = new GeneratorAdapter(ACC_PUBLIC,
                                                              new org.objectweb.asm.commons.Method("<init>",
                                                                                                   initNode.desc), mv);
            generator.loadThis();
            generator.loadArgs();
            generator.visitMethodInsn(INVOKESPECIAL, innerClassName, "<init>", initNode.desc);
            generator.returnValue();
            generator.visitMaxs(1, 1);
            generator.visitEnd();
        }

        List<MethodNode> methodNodes = collector.getAbstracts();
        for (MethodNode mnode : methodNodes) {
            makeMethod(cw, mnode, className, originalClassName);
        }
        cw.visitEnd();

        try {
            IOUtils.write(cw.toByteArray(), new FileOutputStream("/tmp/" + className + ".class"));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Class<?> implClass = innerLoader.publicDefineClass(className.replace('/', '.'), cw.toByteArray());
        WeakReference<Class<?>> wClass = new WeakReference<Class<?>>(implClass);
        factory.put(originalClassName, wClass);
        return implClass;
    }

    static void makeMethod(ClassWriter cw, MethodNode mnode, String className, String originalClassName) {
        int access = mnode.access ^ ACC_ABSTRACT;
        Type returnType = Type.getReturnType(mnode.desc);
        @SuppressWarnings("unchecked")
        MethodVisitor mv = cw.visitMethod(access, mnode.name, mnode.desc, mnode.signature,
                                          (String[]) mnode.exceptions.toArray(new String[mnode.exceptions.size()]));
        GeneratorAdapter generator = new GeneratorAdapter(access, new org.objectweb.asm.commons.Method(mnode.name,
                                                                                                       mnode.desc), mv);
        if (returnType.getSort() != Type.VOID) {
            String normalId = originalClassName.replace('/', '.') + "." + mnode.name + mnode.desc;
            generator.visitCode();
            generator.visitLdcInsn(normalId);

            Type[] argumentTypes = Type.getArgumentTypes(mnode.desc);
            generator.push(argumentTypes.length);
            generator.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            for (int i = 0; i < argumentTypes.length; ++i) {
                generator.dup();
                generator.push(i);
                generator.loadArg(i);
                if (argumentTypes[i].getSort() != Type.OBJECT) {
                    generator.box(argumentTypes[i]);
                }
                generator.visitInsn(AASTORE);
            }
            generator.visitMethodInsn(INVOKESTATIC, "fastut/mock/MockUp", "tryMock",
                                      "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
            generator.visitVarInsn(ASTORE, argumentTypes.length + 1);
            generator.visitVarInsn(ALOAD, argumentTypes.length + 1);
            Label l0 = new Label();
            generator.visitJumpInsn(IFNULL, l0);
            generator.visitVarInsn(ALOAD, argumentTypes.length + 1);
            if (returnType.getSort() == Type.OBJECT) {
                generator.checkCast(returnType);
            } else {
                generator.unbox(returnType);
            }
            generator.returnValue();
            generator.visitLabel(l0);
            switch (returnType.getSort()) {
                case Type.BYTE:
                    generator.push((Byte) ObjectPool.getObject(returnType));
                    break;
                case Type.CHAR:
                    generator.push((Character) ObjectPool.getObject(returnType));
                    break;
                case Type.DOUBLE:
                    generator.push((Double) ObjectPool.getObject(returnType));
                    break;
                case Type.FLOAT:
                    generator.push((Float) ObjectPool.getObject(returnType));
                    break;
                case Type.LONG:
                    generator.push((Long) ObjectPool.getObject(returnType));
                    break;
                case Type.INT:
                    generator.push((Integer) ObjectPool.getObject(returnType));
                    break;
                case Type.SHORT:
                    generator.push((Short) ObjectPool.getObject(returnType));
                    break;
                case Type.OBJECT:
                    if (returnType.getDescriptor().equals("Ljava/lang/String;")) {
                        generator.visitLdcInsn("mock by fastut");
                    } else {
                        generator.visitInsn(ACONST_NULL);
                    }
            }
        }
        generator.visitMaxs(1, 1);
        generator.returnValue();
    }

    static Class<?> mockNormal(String className, InnerLoader innerLoader) {
        try {
            ClassReader cr = new ClassReader(className);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            MockMaker mocker = new MockMaker(cw);
            cr.accept(mocker, ClassReader.SKIP_DEBUG);
            try {
                File file = new File("/tmp/" + className.replace('.', '/') + ".class");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                IOUtils.write(cw.toByteArray(), new FileOutputStream("/tmp/" + className.replace('.', '/') + ".class"));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Class<?> clazz = innerLoader.publicDefineClass(className, cw.toByteArray());
            WeakReference<Class<?>> mockClassRef = new WeakReference<Class<?>>(clazz);
            factory.put(className, mockClassRef);
            return clazz;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
