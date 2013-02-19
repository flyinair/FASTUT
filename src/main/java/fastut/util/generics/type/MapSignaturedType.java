package fastut.util.generics.type;

import java.util.HashMap;
import java.util.Map;

public class MapSignaturedType extends SignaturedType {

    private SignaturedType declType;
    private SignaturedType ktype;
    private SignaturedType vtype;

    public MapSignaturedType(SignaturedType declType, SignaturedType ktype, SignaturedType vtype){
        super(declType.getType());
        this.ktype = ktype;
        this.vtype = vtype;
        this.declType = declType;
    }

    public SignaturedType getKtype() {
        return ktype;
    }

    public SignaturedType getVtype() {
        return vtype;
    }

    public SignaturedType getDeclType() {
        return declType;
    }

    @Override
    public String toString() {
        return "MapSignaturedType [declType=" + declType + ", ktype=" + ktype + ", vtype=" + vtype + "]";
    }

    @Override
    public Object make() {
        if (declType.getType() != null && declType.getType().getDescriptor().equals("Ljava/util/Map;")) {
            Map<Object, Object> rets = new HashMap<Object, Object>();
            if (ktype != null && vtype != null) {
                rets.put(ktype.make(), vtype.make());
            }
            return rets;
        }
        return null;
    }

}
