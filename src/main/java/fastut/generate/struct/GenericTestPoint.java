package fastut.generate.struct;

/**
 *
 * @author William Whitney
 */
public class GenericTestPoint implements Cloneable
{

    public String instanceManipulation = "";
    public String expectedResult = "";

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        GenericTestPoint testPt = new GenericTestPoint();
        testPt.instanceManipulation = instanceManipulation;
        testPt.expectedResult = expectedResult;
        return testPt;
    }
}
