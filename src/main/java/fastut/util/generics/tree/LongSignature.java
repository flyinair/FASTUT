package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

/** AST that represents the type long. */
public class LongSignature implements BaseType {

    private static LongSignature singleton = new LongSignature();

    private LongSignature(){
    }

    public static LongSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitLongSignature(this);
    }
}
