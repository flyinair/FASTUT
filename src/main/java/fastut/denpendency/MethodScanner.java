package fastut.denpendency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class MethodScanner extends ClassAdapter {

    Map<String, MethodConstantPool> methodConstants = new HashMap<String, MethodConstantPool>();
    static Set<String>              doneSet         = new HashSet<String>();
    String                          name;

    public MethodConstantPool getMethodConstantPool(String methodId) {
        return methodConstants.get(methodId);
    }

    public Map<String, MethodConstantPool> getMethodConstants() {
        return methodConstants;
    }

    public MethodScanner(ClassVisitor cv){
        super(cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name.replace('/', '.');
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor smv = super.visitMethod(access, name, desc, signature, exceptions);
        MethodConstantPool pool = new MethodConstantPool(this.name, name, desc);
        MethodResolver methodVisitor = new MethodResolver(pool, smv);
        methodConstants.put(this.name + "." + name + desc, pool);
        return methodVisitor;
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String className = name.replace('/', '.');
        if (!doneSet.contains(className)) {
            try {
                doneSet.add(className);
                ClassReader cr = new ClassReader(className);
                ClassWriter cw = new ClassWriter(cr, 0);
                MethodScanner ca = new MethodScanner(cw);
                cr.accept(ca, ClassReader.SKIP_DEBUG);
                methodConstants.putAll(ca.getMethodConstants());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }

}
