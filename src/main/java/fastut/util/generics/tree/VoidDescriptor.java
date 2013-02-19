package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the pseudo-type void. */
public class VoidDescriptor implements ReturnType {

    private static VoidDescriptor singleton = new VoidDescriptor();

    private VoidDescriptor(){
    }

    public static VoidDescriptor make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitVoidDescriptor(this);
    }
}
