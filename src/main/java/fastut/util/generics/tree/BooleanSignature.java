package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type boolean. */
public class BooleanSignature implements BaseType {

    private static BooleanSignature singleton = new BooleanSignature();

    private BooleanSignature(){
    }

    public static BooleanSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitBooleanSignature(this);
    }
}
