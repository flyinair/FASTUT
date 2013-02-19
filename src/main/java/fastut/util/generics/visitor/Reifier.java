package fastut.util.generics.visitor;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Type;

import fastut.util.generics.tree.ArrayTypeSignature;
import fastut.util.generics.tree.BooleanSignature;
import fastut.util.generics.tree.BottomSignature;
import fastut.util.generics.tree.ByteSignature;
import fastut.util.generics.tree.CharSignature;
import fastut.util.generics.tree.ClassTypeSignature;
import fastut.util.generics.tree.DoubleSignature;
import fastut.util.generics.tree.FieldTypeSignature;
import fastut.util.generics.tree.FloatSignature;
import fastut.util.generics.tree.FormalTypeParameter;
import fastut.util.generics.tree.IntSignature;
import fastut.util.generics.tree.LongSignature;
import fastut.util.generics.tree.ShortSignature;
import fastut.util.generics.tree.SimpleClassTypeSignature;
import fastut.util.generics.tree.TypeArgument;
import fastut.util.generics.tree.TypeVariableSignature;
import fastut.util.generics.tree.VoidDescriptor;
import fastut.util.generics.tree.Wildcard;
import fastut.util.generics.type.ArraySignaturedType;
import fastut.util.generics.type.ListSignaturedType;
import fastut.util.generics.type.MapSignaturedType;
import fastut.util.generics.type.SignaturedType;

/**
 * Visitor that converts AST to reified types.
 */
public class Reifier implements TypeTreeVisitor<SignaturedType> {

    private SignaturedType resultType;

    private Reifier(){
    }

    /**
     * Factory method. The resulting visitor will convert an AST representing generic signatures into corresponding
     * reflective objects, using the provided factory, <tt>f</tt>.
     *
     * @param f - a factory that can be used to manufacture reflective objects returned by this visitor
     * @return A visitor that can be used to reify ASTs representing generic type information into reflective objects
     */
    public static Reifier make() {
        return new Reifier();
    }

    // Helper method. Visits an array of TypeArgument and produces
    // reified Type array.
    private SignaturedType[] reifyTypeArguments(TypeArgument[] tas) {
        SignaturedType[] ts = new SignaturedType[tas.length];
        for (int i = 0; i < tas.length; i++) {
            tas[i].accept(this);
            ts[i] = resultType;
        }
        return ts;
    }

    /**
     * Accessor for the result of the last visit by this visitor,
     *
     * @return The type computed by this visitor based on its last visit
     */
    public SignaturedType getResult() {
        assert resultType != null;
        return resultType;
    }

    public void visitFormalTypeParameter(FormalTypeParameter ftp) {
        resultType = makeTypeVariable(ftp.getName(), ftp.getBounds());
    }

    public void visitClassTypeSignature(ClassTypeSignature ct) {
        // This method examines the pathname stored in ct, which has the form
        // n1.n2...nk<targs>....
        // where n1 ... nk-1 might not exist OR
        // nk might not exist (but not both). It may be that k equals 1.
        // The idea is that nk is the simple class type name that has
        // any type parameters associated with it.
        // We process this path in two phases.
        // First, we scan until we reach nk (if it exists).
        // If nk does not exist, this identifies a raw class n1 ... nk-1
        // which we can return.
        // if nk does exist, we begin the 2nd phase.
        // Here nk defines a parameterized type. Every further step nj (j > k)
        // down the path must also be represented as a parameterized type,
        // whose owner is the representation of the previous step in the path,
        // n{j-1}.

        // extract iterator on list of simple class type sigs
        List<SimpleClassTypeSignature> scts = ct.getPath();
        assert (!scts.isEmpty());
        Iterator<SimpleClassTypeSignature> iter = scts.iterator();
        SimpleClassTypeSignature sc = iter.next();
        StringBuilder n = new StringBuilder(sc.getName());
        boolean dollar = sc.getDollar();

        // phase 1: iterate over simple class types until
        // we are either done or we hit one with non-empty type parameters
        while (iter.hasNext() && sc.getTypeArguments().length == 0) {
            sc = iter.next();
            dollar = sc.getDollar();
            n.append(dollar ? "$" : ".").append(sc.getName());
        }

        // Now, either sc is the last element of the list, or
        // it has type arguments (or both)
        assert (!(iter.hasNext()) || (sc.getTypeArguments().length > 0));
        // Create the raw type
        SignaturedType c = makeNamedType(n.toString());
        // if there are no type arguments
        if (sc.getTypeArguments().length == 0) {
            // we have surely reached the end of the path
            assert (!iter.hasNext());
            resultType = c; // the result is the raw type
        } else {
            assert (sc.getTypeArguments().length > 0);
            // otherwise, we have type arguments, so we create a parameterized
            // type, whose declaration is the raw type c, and whose owner is
            // the declaring class of c (if any). This latter fact is indicated
            // by passing null as the owner.
            // First, we reify the type arguments
            SignaturedType[] pts = reifyTypeArguments(sc.getTypeArguments());

            SignaturedType owner = makeParameterizedType(c, pts, null);
            // phase 2: iterate over remaining simple class types
            dollar = false;
            while (iter.hasNext()) {
                sc = iter.next();
                dollar = sc.getDollar();
                n.append(dollar ? "$" : ".").append(sc.getName()); // build up raw class name
                c = makeNamedType(n.toString()); // obtain raw class
                pts = reifyTypeArguments(sc.getTypeArguments());// reify params
                // Create a parameterized type, based on type args, raw type
                // and previous owner
                owner = makeParameterizedType(c, pts, owner);
            }
            resultType = owner;
        }
    }

    public void visitArrayTypeSignature(ArrayTypeSignature a) {
        // extract and reify component type
        a.getComponentType().accept(this);
        SignaturedType ct = resultType;
        resultType = makeArrayType(ct);
    }

    public void visitTypeVariableSignature(TypeVariableSignature tv) {
        // resultType = getFactory().findTypeVariable(tv.getIdentifier());
        System.err.println("visitTypeVariableSignature(" + tv.getIdentifier() + ")");
    }

    public void visitWildcard(Wildcard w) {
        resultType = makeWildcard(w.getUpperBounds(), w.getLowerBounds());
    }

    public void visitSimpleClassTypeSignature(SimpleClassTypeSignature sct) {
        resultType = makeNamedType(sct.getName());
    }

    public void visitBottomSignature(BottomSignature b) {

    }

    public void visitByteSignature(ByteSignature b) {
        resultType = SignaturedType.makeSimpleType(Type.BYTE_TYPE);
    }

    public void visitBooleanSignature(BooleanSignature b) {
        resultType = SignaturedType.makeSimpleType(Type.BOOLEAN_TYPE);
    }

    public void visitShortSignature(ShortSignature s) {
        resultType = SignaturedType.makeSimpleType(Type.SHORT_TYPE);
    }

    public void visitCharSignature(CharSignature c) {
        resultType = SignaturedType.makeSimpleType(Type.CHAR_TYPE);
    }

    public void visitIntSignature(IntSignature i) {
        resultType = SignaturedType.makeSimpleType(Type.INT_TYPE);
    }

    public void visitLongSignature(LongSignature l) {
        resultType = SignaturedType.makeSimpleType(Type.LONG_TYPE);
    }

    public void visitFloatSignature(FloatSignature f) {
        resultType = SignaturedType.makeSimpleType(Type.FLOAT_TYPE);
    }

    public void visitDoubleSignature(DoubleSignature d) {
        resultType = SignaturedType.makeSimpleType(Type.DOUBLE_TYPE);
    }

    public void visitVoidDescriptor(VoidDescriptor v) {
        resultType = SignaturedType.makeSimpleType(Type.VOID_TYPE);
    }

    SignaturedType makeTypeVariable(String name, FieldTypeSignature[] bounds) {
        System.err.println("makeTypeVariable(name:=" + name + ",bounds:=" + bounds + ")");
        return null;
    }

    void processFieldTypeSignature(FieldTypeSignature f, StringBuilder builder) {
        if (f instanceof ArrayTypeSignature) {
        } else if (f instanceof BottomSignature) {
        } else if (f instanceof ClassTypeSignature) {
            ClassTypeSignature c = (ClassTypeSignature)f;
            for(SimpleClassTypeSignature s : c.getPath()) {
                builder.append(s.getDollar() ? "$" : ".").append(s.getName());
            }
        } else if (f instanceof SimpleClassTypeSignature) {
        } else if (f instanceof TypeVariableSignature) {

        }
    }

    SignaturedType makeWildcard(FieldTypeSignature[] ubs, FieldTypeSignature[] lbs) {
        StringBuilder builder = new StringBuilder();
        for (FieldTypeSignature f : ubs) {
            processFieldTypeSignature(f, builder);
        }
        Type utype = null;
        if(builder.length() > 0) {
            utype = Type.getType("L" + builder.toString().substring(1).replace('.', '/') + ";");
        }
        builder = new StringBuilder();
        for (FieldTypeSignature f : lbs) {
            processFieldTypeSignature(f, builder);
        }
        Type ltype = null;
        if(builder.length() > 0) {
            ltype = Type.getType("L" + builder.toString().substring(1).replace('.', '/') + ";");
        }
        return SignaturedType.makeSimpleType(ltype, utype);
    }

    SignaturedType makeParameterizedType(SignaturedType declaration, SignaturedType[] typeArgs, SignaturedType owner) {
        if(typeArgs == null || typeArgs.length == 0) {
            return declaration;
        }
        if (typeArgs.length == 1) {
            return new ListSignaturedType(declaration, typeArgs[0]);
        }
        if (typeArgs.length == 2) {
            return new MapSignaturedType(declaration, typeArgs[0], typeArgs[1]);
        }
        throw new RuntimeException("type argument's size more than two.");
    }

    SignaturedType makeNamedType(String name) {
        return SignaturedType.makeSimpleType(Type.getType("L" + name.replace('.', '/') + ";"));
    }

    SignaturedType makeArrayType(SignaturedType componentType) {
        return new ArraySignaturedType(componentType);
    }

}
