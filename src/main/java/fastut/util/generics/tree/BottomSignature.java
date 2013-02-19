package fastut.util.generics.tree;

import fastut.util.generics.visitor.TypeTreeVisitor;

public class BottomSignature implements FieldTypeSignature {

    private static BottomSignature singleton = new BottomSignature();

    private BottomSignature(){
    }

    public static BottomSignature make() {
        return singleton;
    }

    public void accept(TypeTreeVisitor<?> v) {
        v.visitBottomSignature(this);
    }
}
