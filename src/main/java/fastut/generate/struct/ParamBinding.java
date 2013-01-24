package fastut.generate.struct;

/**
 * Represents a variable bound to a value
 * @author William Whitney
 */
public class ParamBinding  implements Cloneable
{

    public String type = "";
    public String name = "";

    public ParamBinding(String varType, String id)
    {

        this.type = varType;
        this.name = id;

    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }


}
