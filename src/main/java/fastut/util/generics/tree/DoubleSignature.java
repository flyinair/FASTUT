package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type double. */
public class DoubleSignature implements BaseType {

    private static DoubleSignature singleton = new DoubleSignature();

    private DoubleSignature(){
    }

    public static DoubleSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitDoubleSignature(this);
    }
}
