package fastut.util.generics.type;

import java.util.ArrayList;
import java.util.List;

public class ListSignaturedType extends SignaturedType {

    private SignaturedType argType;
    private SignaturedType declType;

    public ListSignaturedType(SignaturedType declType, SignaturedType argType){
        super(declType.getType());
        this.declType = declType;
        this.argType = argType;
    }

    public SignaturedType getArgType() {
        return argType;
    }

    public SignaturedType getDeclType() {
        return declType;
    }

    @Override
    public String toString() {
        return "ListSignaturedType [argType=" + argType + ", declType=" + declType + "]";
    }

    @Override
    public Object make() {
        if (declType.getType() != null && declType.getType().getDescriptor().equals("Ljava/util/List;")) {
            List<Object> rets = new ArrayList<Object>();
            if (argType != null) {
                rets.add(argType.make());
            }
            return rets;
        }
        return null;
    }

}
