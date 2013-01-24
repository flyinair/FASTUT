package fastut.mock;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class MockWorker extends MethodAdapter implements Opcodes {

    private final String className;
    private final String methodName;
    private final String methodDesc;
    private final int    access;

    public MockWorker(MethodVisitor mv, String className, String methodName, String methodDesc, int access){
        super(mv);
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.access = access;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        String normalId = className.replace('/', '.') + "." + methodName + methodDesc;
        GeneratorAdapter generator = new GeneratorAdapter(access, new org.objectweb.asm.commons.Method(methodName,
                                                                                                       methodDesc), mv);
        generator.visitCode();
        generator.visitLdcInsn(normalId);

        Type[] argumentTypes = Type.getArgumentTypes(methodDesc);
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
        Type returnType = Type.getReturnType(methodDesc);
        if (returnType.getSort() == Type.OBJECT) {
            generator.checkCast(returnType);
        } else {
            generator.unbox(returnType);
        }
        generator.returnValue();
        generator.visitLabel(l0);
    }

    @Override
    public void visitEnd() {
        super.visitMaxs(0, 0);
        super.visitEnd();
    }

}
