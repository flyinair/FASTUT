package fastut.coverage.instrument;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * Expects that the visitMaxs is calculated for me ....
 */
public class NewLocalVariableMethodAdapter extends MethodAdapter implements Opcodes
{
    protected int firstStackVariable;
    protected int addedStackWords;

    public NewLocalVariableMethodAdapter(MethodVisitor mv, int access, String desc, int addedStackWords)
    {
        super(mv);
        Type[] args = Type.getArgumentTypes(desc);
        firstStackVariable = ((ACC_STATIC & access) != 0) ? 0 : 1;
        for (int i = 0; i < args.length; i++) {
            firstStackVariable += args[i].getSize();
        }
        this.addedStackWords = addedStackWords;
    }

    public void visitVarInsn(int opcode, int var)
    {
        mv.visitVarInsn(opcode, (var >= firstStackVariable) ? var + addedStackWords : var);
    }

    public void visitIincInsn(int var, int increment) {
        mv.visitIincInsn((var >= firstStackVariable) ? var + addedStackWords : var, increment);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
    {
        mv.visitLocalVariable(name, desc, signature, start, end, (index >= firstStackVariable) ? index + addedStackWords : index);
    }

    public int getAddedStackWords()
    {
        return addedStackWords;
    }

    public int getFirstStackVariable()
    {
        return firstStackVariable;
    }

}
