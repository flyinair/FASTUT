package fastut.util.generics.type;

public class MultiArgumentSignaturedType extends SignaturedType {

    private SignaturedType   declType;
    private SignaturedType[] typeArgs;

    public MultiArgumentSignaturedType(SignaturedType declType, SignaturedType[] typeArgs){
        super(declType.getType());
        this.declType = declType;
        this.typeArgs = typeArgs;
    }

}
