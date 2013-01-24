package fastut.generate.struct;

import java.util.ArrayList;

/**
 * Represents a method being tested.
 * @author William Whitney
 */
public class UnitMethod implements Cloneable
{

    private String varID;
    public String name;
    public ArrayList<ParamBinding> params;
    public String returnType;
    public ArrayList<TestPath> testBranches;
    public ArrayList<String> modifiers;

    /**
     * Default constructor.
     */
    public UnitMethod()
    {
        varID = "";
        name = "";
        params = new ArrayList<ParamBinding>();
        returnType = "";
        testBranches = new ArrayList<TestPath>();
        modifiers = new ArrayList<String>();
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        UnitMethod method = new UnitMethod();

        method.varID = varID;
        method.name = name;
        method.returnType = returnType;

        for (ParamBinding paramBind : params)
        {
            method.params.add((ParamBinding) paramBind.clone());
        }

        for (TestPath branches : testBranches)
        {
            method.testBranches.add((TestPath) branches.clone());
        }

        for (String modifier : modifiers)
        {
            method.modifiers.add(modifier);
        }

        return method;
    }

    /**
     * Called by JavaBranchParser.
     * @param type
     */
    public void addParamType(String type)
    {
        this.params.add(0, new ParamBinding(type, varID));
    }

    /**
     * Called by JavaBranchParser.
     * @param id
     */
    public void addParamID(String id)
    {
        this.varID = id;
    }

    private String getParamsAsStr_s() {
        String retStr = "";
        if (!this.name.equals(""))
        {
            retStr += "(";

            for (ParamBinding param : params)
            {
                retStr += param.type+ ", ";
            }
            //Remove comma
            if (retStr.length() >= 2)
            {
                retStr = String.valueOf(retStr.subSequence(0, retStr.length() - 2));
            }
            retStr += ")";
        }
        return retStr;
    }

    private String getParamsAsStr()
    {
        String retStr = "";

        if (!this.name.equals(""))
        {
            retStr += "(";

            for (ParamBinding param : params)
            {
                retStr += param.type + " " + param.name + ", ";
            }
            //Remove comma
            if (retStr.length() >= 2)
            {
                retStr = String.valueOf(retStr.subSequence(0, retStr.length() - 2));
            }
            retStr += ")";
        }
        return retStr;
    }

    /**
     * Includes return type in the method name.
     */
    public String getAsString()
    {
        return this.returnType + " " + this.name + getParamsAsStr();
    }

    public String getAsString_s()
    {
        return this.returnType + " " + this.name + getParamsAsStr_s();
    }

    /**
     * Does not include the method return type.
     * @return
     */
    public String getAsStringSimple()
    {
        return this.name + getParamsAsStr();
    }

    public boolean isVoidMethod()
    {
        return returnType.equals("void");
    }

    public boolean isConstructor()
    {
        return returnType.equals("constructor");
    }

    public void addModifier(String modifier)
    {
        this.modifiers.add(modifier);
    }

    public boolean hasModifier(String modifier)
    {
        return modifiers.contains(modifier);
    }
}
