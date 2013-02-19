package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type int. */
public class IntSignature implements BaseType {

    private static IntSignature singleton = new IntSignature();

    private IntSignature(){
    }

    public static IntSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitIntSignature(this);
    }
}
