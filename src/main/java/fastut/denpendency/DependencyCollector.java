package fastut.denpendency;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import fastut.mock.MockFactory;

public class DependencyCollector extends ClassAdapter implements Opcodes {

    public Map<String, List<FastUTFieldNode>>   declared_fields        = new HashMap<String, List<FastUTFieldNode>>();
    public Map<String, List<MethodNode>>        declared_inits         = new HashMap<String, List<MethodNode>>();

    public Map<DependencyKey, Set<FieldCall>>   METHOD_VISITED_FIELDS  = new HashMap<DependencyKey, Set<FieldCall>>();
    public Map<DependencyKey, List<MethodCall>> METHOD_VISITED_METHODS = new HashMap<DependencyKey, List<MethodCall>>();
    public Map<DependencyKey, MethodNode>       declared_methods       = new HashMap<DependencyKey, MethodNode>();

    List<String>                                superlist              = new ArrayList<String>();
    boolean                                     reversed               = false;
    Stack<String>                               class_name_stack       = new Stack<String>();

    public DependencyCollector(ClassVisitor cv){
        super(cv);
    }

    void collect(String className) {
        try {
            className = className.replace('/', '.');
            ClassReader cr = new ClassReader(className);
            cr.accept(this, ClassReader.SKIP_DEBUG);
        } catch (Throwable e) {

        }
    }

    List<FastUTFieldNode> currentFields() {
        return declared_fields.get(class_name_stack.peek());
    }

    List<MethodNode> currentInits() {
        return declared_inits.get(class_name_stack.peek());
    }

    public List<String> getOverrideList() {
        if (!reversed) {
            Collections.reverse(superlist);
            reversed = true;
        }
        return superlist;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        class_name_stack.push(name);
        if (superlist.size() == 0) {
            superlist.add(name);
        }
        if (superName != null && !"java/lang/Object".equals(superName)) {
            collect(superName);
            superlist.add(superName);
        }
        declared_fields.put(name, new ArrayList<FastUTFieldNode>());
        List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        declared_inits.put(name, methodNodes);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        currentFields().add(new FastUTFieldNode(access, name, desc, signature, value, class_name_stack.peek()));
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("<init>")) {
            currentInits().add(new MethodNode(access, name, desc, signature, exceptions));
        }
        DependencyKey key = new DependencyKey(class_name_stack.peek(), name, desc);
        declared_methods.put(key, new MethodNode(access, name, desc, signature, exceptions));
        return new DependencyCollectorMethodWorker(super.visitMethod(access, name, desc, signature, exceptions),
                                                   class_name_stack.peek(), name, desc, this);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        class_name_stack.pop();
    }

    public static void main(String[] args) throws IOException {
        String className = "samples.ServiceBean";
        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        DependencyCollector collector = new DependencyCollector(cw);
        cr.accept(collector, ClassReader.SKIP_DEBUG);
        for (Map.Entry<String, List<FastUTFieldNode>> entry : collector.declared_fields.entrySet()) {
            System.err.println("class:\t" + entry.getKey());
            for (FieldNode node : entry.getValue()) {
                String type = "";
                if (Modifier.isFinal(node.access) && node.signature == null) {
                    type += "FINAL";
                } else {
                    Type ft = Type.getType(node.desc);
                    if (ft.getSort() == Type.OBJECT && !ft.getClassName().equals("java.lang.String")) {
                        Class<?> retClass = MockFactory.mock(ft.getClassName());
                        if (retClass != null) {
                            type += "MOCKED";
                        } else {
                            type += "MOCK FAIL";
                        }
                    } else {
                        type += "NORMAL";
                    }
                }
                System.err.println(type + "\tfield:\tname=" + node.name + "\tdesc=" + node.desc + "\tsig="
                                   + node.signature);
            }
            System.err.println();
        }

        System.err.println(collector.METHOD_VISITED_FIELDS);
        System.err.println(collector.METHOD_VISITED_METHODS);

        for (Map.Entry<String, List<MethodNode>> entry : collector.declared_inits.entrySet()) {
            System.err.println("class:\t" + entry.getKey());
            for (MethodNode node : entry.getValue()) {
                System.err.println("\tmethod:\tname=" + node.name + "\tdesc=" + node.desc + "\tsig=" + node.signature);
            }
            System.err.println();
        }
    }
}
