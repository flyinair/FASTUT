package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type short. */
public class ShortSignature implements BaseType {

    private static ShortSignature singleton = new ShortSignature();

    private ShortSignature(){
    }

    public static ShortSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitShortSignature(this);
    }
}
