package fastut.mock;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Collector extends ClassAdapter implements Opcodes {

    private Map<String, MethodNode> abstracts      = new HashMap<String, MethodNode>();
    private Set<String>             implementss    = new HashSet<String>();
    private boolean                 reduced        = false;
    private ClassNode               classNode      = null;
    private List<MethodNode>        inits          = new ArrayList<MethodNode>();
    private String                  firstClassName = null;

    public Collector(ClassVisitor cv){
        super(cv);
    }

    public List<MethodNode> getAbstracts() {
        if (!reduced) {
            reduce();
        }
        return new ArrayList<MethodNode>(abstracts.values());
    }

    public boolean hasAbstract() {
        if (!reduced) {
            reduce();
        }
        return abstracts.size() > 0;
    }

    public MethodNode getSimplestInitNode() {
        MethodNode simplest = null;
        for (MethodNode node : inits) {
            if (simplest == null) {
                simplest = node;
            } else {
                if (simplest.desc.length() > node.desc.length()) {
                    simplest = node;
                }
            }
        }
        return simplest;
    }

    public ClassNode getFirstClassNode() {
        return classNode;
    }

    void reduce() {
        List<String> removelist = new ArrayList<String>();
        for (Iterator<String> iter = abstracts.keySet().iterator(); iter.hasNext();) {
            String key = iter.next();
            if (implementss.contains(key)) {
                removelist.add(key);
            }
        }
        for (String key : removelist) {
            abstracts.remove(key);
        }
    }

    void collect(String className) {
        try {
            className = className.replace('/', '.');
            ClassReader cr = new ClassReader(className);
            cr.accept(this, ClassReader.SKIP_DEBUG);
        } catch (Throwable e) {

        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (classNode == null) {
            classNode = new ClassNode();
            classNode.visit(version, access, name, signature, superName, interfaces);
            firstClassName = name;
        }
        if (Modifier.isAbstract(access) || Modifier.isInterface(access)) {
            if (superName != null && !"java/lang/Object".equals(superName)) {
                collect(superName);
            }
            if (interfaces != null && interfaces.length > 0) {
                for (String iterName : interfaces) {
                    collect(iterName);
                }
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodNode node = new MethodNode(access, name, desc, signature, exceptions);
        if (name.equals("<init>")) {
            if (classNode != null && classNode.name.equals(firstClassName)) {
                inits.add(node);
            }
        }
        if (Modifier.isAbstract(access)) {
            abstracts.put(name + desc, node);
        } else {
            implementss.add(name + desc);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}
