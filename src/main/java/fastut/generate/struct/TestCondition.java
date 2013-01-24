package fastut.generate.struct;

/**
 * Provides the representation of a single test condition.
 * @author William Whitney
 */
public class TestCondition implements Cloneable
{

    private String condition;
    private EnumCondType type;

    /**
     * Default constructor.
     * @param condition
     */
    public TestCondition(String condition, EnumCondType type)
    {
        this.condition = condition;
        this.type = type;
    }

    public String getCondition()
    {
        return this.condition;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        TestCondition cond = new TestCondition(this.condition, this.type);
        return cond;
    }

    public void setCondtion(String condition)
    {
        this.condition = condition;
    }
}
