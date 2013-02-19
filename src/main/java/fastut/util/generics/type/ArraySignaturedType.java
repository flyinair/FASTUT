package fastut.util.generics.type;

import java.util.ArrayList;
import java.util.List;

public class ArraySignaturedType extends SignaturedType {

    private SignaturedType declType;

    public ArraySignaturedType(SignaturedType declType){
        super(declType.getType());
        this.declType = declType;
    }

    public SignaturedType getDeclType() {
        return declType;
    }

    @Override
    public String toString() {
        return "ArraySignaturedType [declType=" + declType + "]";
    }

    @Override
    public Object make() {
        if (declType.getType() != null) {
            List<Object> rets = new ArrayList<Object>();
            if (declType != null) {
                rets.add(declType.make());
            }
            return rets.toArray();
        }
        return null;
    }

}
