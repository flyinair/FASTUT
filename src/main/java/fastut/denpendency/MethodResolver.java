package fastut.denpendency;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class MethodResolver extends MethodAdapter {

    private String             methodName;
    private String             methodDesc;
    private MethodConstantPool pool;
    private Set<String>        typeSet;
    private String             internalClassName;

    public MethodResolver(MethodConstantPool pool, MethodVisitor mv){
        super(mv);
        this.pool = pool;
        this.methodName = pool.getName();
        this.methodDesc = pool.getDesc();
        this.internalClassName = pool.getClassName().replace('.', '/');
        typeSet = MethodConstantPool.CLASS_TYPE_SET.get(pool.getClassName());
        if (typeSet == null) {
            typeSet = new HashSet<String>();
            MethodConstantPool.CLASS_TYPE_SET.put(pool.getClassName(), typeSet);
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return super.visitAnnotationDefault();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        return super.visitParameterAnnotation(parameter, desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        super.visitFrame(type, nLocal, local, nStack, stack);
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.ICONST_0:
                pool.addConstant(0);
                break;
            case Opcodes.ICONST_1:
                pool.addConstant(1);
                break;
            case Opcodes.ICONST_2:
                pool.addConstant(2);
                break;
            case Opcodes.ICONST_3:
                pool.addConstant(3);
                break;
            case Opcodes.ICONST_4:
                pool.addConstant(4);
                break;
            case Opcodes.ICONST_5:
                pool.addConstant(5);
                break;
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        pool.addConstant(operand);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (!internalClassName.equals(type)) {
            typeSet.add(type);
        }
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (!internalClassName.equals(owner)) {
            typeSet.add(owner);
        }
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (!internalClassName.equals(owner)) {
            typeSet.add(owner);
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        pool.addConstant(cst);
        super.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        super.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

}
