package fastut.util.generics.visitor;

import fastut.util.generics.tree.ArrayTypeSignature;
import fastut.util.generics.tree.BooleanSignature;
import fastut.util.generics.tree.BottomSignature;
import fastut.util.generics.tree.ByteSignature;
import fastut.util.generics.tree.CharSignature;
import fastut.util.generics.tree.ClassTypeSignature;
import fastut.util.generics.tree.DoubleSignature;
import fastut.util.generics.tree.FloatSignature;
import fastut.util.generics.tree.FormalTypeParameter;
import fastut.util.generics.tree.IntSignature;
import fastut.util.generics.tree.LongSignature;
import fastut.util.generics.tree.ShortSignature;
import fastut.util.generics.tree.SimpleClassTypeSignature;
import fastut.util.generics.tree.TypeVariableSignature;
import fastut.util.generics.tree.VoidDescriptor;
import fastut.util.generics.tree.Wildcard;

/**
 * Visit a TypeTree and produce a result of type T.
 */
public interface TypeTreeVisitor<T> {

    /**
     * Returns the result of the visit.
     *
     * @return the result of the visit
     */
    T getResult();

    // Visitor methods, per node type

    void visitFormalTypeParameter(FormalTypeParameter ftp);

    void visitClassTypeSignature(ClassTypeSignature ct);

    void visitArrayTypeSignature(ArrayTypeSignature a);

    void visitTypeVariableSignature(TypeVariableSignature tv);

    void visitWildcard(Wildcard w);

    void visitSimpleClassTypeSignature(SimpleClassTypeSignature sct);

    void visitBottomSignature(BottomSignature b);

    // Primitives and Void
    void visitByteSignature(ByteSignature b);

    void visitBooleanSignature(BooleanSignature b);

    void visitShortSignature(ShortSignature s);

    void visitCharSignature(CharSignature c);

    void visitIntSignature(IntSignature i);

    void visitLongSignature(LongSignature l);

    void visitFloatSignature(FloatSignature f);

    void visitDoubleSignature(DoubleSignature d);

    void visitVoidDescriptor(VoidDescriptor v);
}
