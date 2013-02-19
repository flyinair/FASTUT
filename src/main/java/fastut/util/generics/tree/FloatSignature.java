package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type float. */
public class FloatSignature implements BaseType {

    private static FloatSignature singleton = new FloatSignature();

    private FloatSignature(){
    }

    public static FloatSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitFloatSignature(this);
    }
}
