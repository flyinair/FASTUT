package fastut.util.generics.type;

import org.objectweb.asm.Type;

import fastut.object.ObjectPool;

public class SignaturedType implements Makable {

    private boolean eq;
    private Type    type;
    private Type    ltype;
    private Type    utype;

    public SignaturedType(Type type){
        this.type = type;
        eq = true;
    }

    public SignaturedType(Type ltype, Type utype){
        this.ltype = ltype;
        this.utype = utype;
        eq = false;
    }

    public Type getLtype() {
        return ltype;
    }

    public Type getUtype() {
        return utype;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        if (eq) {
            return "SignaturedType[:=" + type + "]";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("SignaturedType[");
            if (ltype != null) {
                builder.append("<").append(ltype);
            }
            if (utype != null) {
                builder.append(">").append(utype);
            }
            builder.append("]");
            return builder.toString();
        }
    }

    public static SignaturedType makeSimpleType(Type type) {
        return new SignaturedType(type);
    }

    public static SignaturedType makeSimpleType(Type ltype, Type utype) {
        return new SignaturedType(ltype, utype);
    }

    @Override
    public Object make() {
        if(eq) {
            if(type.getDescriptor().equals("Ljava/lang/String;")) {
                return "fastut";
            }
            return ObjectPool.getObject(type);
        }
        return null;
    }
}
