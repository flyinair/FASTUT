package fastut.mock;

import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MockMaker extends ClassAdapter implements Opcodes {

    private String className;

    public MockMaker(ClassVisitor cv){
        super(cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor smv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<init>") || name.equals("<clinit>") || Modifier.isAbstract(access)
            || Type.getReturnType(desc).getSort() == Type.VOID) {
            return smv;
        }
        return new MockWorker(smv, this.className, name, desc, access);
    }

}
