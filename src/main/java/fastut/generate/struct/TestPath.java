package fastut.generate.struct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a test branch.
 *
 * @author William Whitney
 */
public class TestPath implements Cloneable {

    public ArrayList<TestCondition>      conditions            = new ArrayList<TestCondition>();
    public ArrayList<UserParamTestValue> userParamTestValues   = new ArrayList<UserParamTestValue>();
    public String                        expectedOutput        = "";
    public String                        additionalConstraints = "";
    public ArrayList<GenericTestPoint>   genericTestPoints     = new ArrayList<GenericTestPoint>();
    public String                        constructor           = "";
    public Set<String>                   imports               = new HashSet<String>();

    @Override
    public Object clone() throws CloneNotSupportedException {
        TestPath branch = new TestPath();
        branch.genericTestPoints.clear();
        branch.expectedOutput = this.expectedOutput;
        branch.additionalConstraints = additionalConstraints;
        branch.constructor = constructor;

        for (TestCondition testCond : conditions) {
            branch.conditions.add((TestCondition) testCond.clone());
        }

        for (UserParamTestValue userParamVal : userParamTestValues) {
            branch.userParamTestValues.add((UserParamTestValue) userParamVal.clone());
        }

        for (GenericTestPoint point : genericTestPoints) {
            branch.genericTestPoints.add((GenericTestPoint) point.clone());
        }

        return branch;
    }

    public String getExecutionPath() {
        String exPath = "";

        for (TestCondition cond : conditions) {
            exPath += cond.getCondition();
            exPath += ", ";
        }
        // Remove comma
        if (exPath.length() >= 2) {
            exPath = String.valueOf(exPath.subSequence(0, exPath.length() - 2));
        }
        return exPath;
    }

    public String getStatus(UnitMethod parentMethod) {
        // Check the function params
        for (UserParamTestValue paramVal : this.userParamTestValues) {
            if (paramVal.userParamValue.equals("")) {
                return "Parameter " + paramVal.paramBinding.type + " " + paramVal.paramBinding.name + " is  blank";
            }

        }

        // Check for return type
        if (!parentMethod.isVoidMethod() && !parentMethod.isConstructor()) {
            if (this.expectedOutput.equals("")) {
                return "Expected output is blank";
            }
        }

        if (!parentMethod.hasModifier("static") && !parentMethod.isConstructor()) {
            if (this.constructor.equals("")) {
                return "Expected a constructor";
            }
        }

        // Check additonal test verification points
        for (GenericTestPoint point : this.genericTestPoints) {

            if (point.expectedResult.equals("") || point.instanceManipulation.equals("")) {
                return "Missing test verification point";
            }
        }

        return "Complete";
    }

    public boolean isComplete(UnitMethod parentMethod) {
        String result = getStatus(parentMethod);
        return result.equals("Complete");
    }
}
