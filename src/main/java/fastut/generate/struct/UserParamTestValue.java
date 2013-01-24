package fastut.generate.struct;

/**
 *
 * @author William Whitney
 */
public class UserParamTestValue implements Cloneable
{

    public ParamBinding paramBinding;
    public String userParamValue = "";

    public UserParamTestValue(ParamBinding paramBind)
    {
        this.paramBinding = paramBind;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        UserParamTestValue val = new UserParamTestValue(paramBinding);
        val.userParamValue = userParamValue;
        return val;
    }
}
