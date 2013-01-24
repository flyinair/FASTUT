package fastut.denpendency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DependencyCollectorMethodWorker extends MethodAdapter implements Opcodes {

    private final DependencyKey    key;
    private final Set<FieldCall>   fieldCalls;
    private final List<MethodCall> methodCalls;

    public DependencyCollectorMethodWorker(MethodVisitor mv, String className, String methodName, String methodDesc, DependencyCollector collector){
        super(mv);
        key = new DependencyKey(className, methodName, methodDesc);
        fieldCalls = new HashSet<FieldCall>();
        methodCalls = new ArrayList<MethodCall>();
        collector.METHOD_VISITED_FIELDS.put(key, fieldCalls);
        collector.METHOD_VISITED_METHODS.put(key, methodCalls);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        FieldCall call = new FieldCall(opcode, owner, name, desc);
        fieldCalls.add(call);
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        MethodCall call = new MethodCall(opcode, owner, name, desc);
        methodCalls.add(call);
        super.visitMethodInsn(opcode, owner, name, desc);
    }

}
