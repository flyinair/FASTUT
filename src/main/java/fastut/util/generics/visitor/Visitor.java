package fastut.util.generics.visitor;

import fastut.util.generics.tree.ClassSignature;
import fastut.util.generics.tree.MethodTypeSignature;

public interface Visitor<T> extends TypeTreeVisitor<T> {

    void visitClassSignature(ClassSignature cs);

    void visitMethodTypeSignature(MethodTypeSignature ms);
}
