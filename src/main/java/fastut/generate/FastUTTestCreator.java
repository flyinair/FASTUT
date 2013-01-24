package fastut.generate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fastut.generate.struct.GenericTestPoint;
import fastut.generate.struct.TestPath;
import fastut.generate.struct.UnitMethod;
import fastut.generate.struct.UserParamTestValue;

public class FastUTTestCreator {

    private List<UnitMethod> methods;
    private final String     className;
    private final boolean    printStubs;
    private List<String>     invokeMethodNames = new ArrayList<String>();
    private Set<String>      imports           = new HashSet<String>();
    {
        imports.add("mockit.Mocked");
        imports.add("org.jtester.utility.ReflectUtil");
        imports.add("org.jtester.testng.JTester");
        imports.add("org.testng.annotations.Test");
    }

    public FastUTTestCreator(String className, UnitMethod currentMethod, boolean printStubs){
        methods = new ArrayList<UnitMethod>();
        methods.add(currentMethod);
        this.printStubs = printStubs;
        this.className = className;
    }

    public FastUTTestCreator(String className, List<UnitMethod> currentMethods, boolean printStubs){
        methods = new ArrayList<UnitMethod>();
        methods.addAll(currentMethods);
        this.printStubs = printStubs;
        this.className = className;
    }

    public String getTest() {
        String test = "";

        test += "public class " + this.className + "Test extends JTester { \n";
        test += "\n";

        for (UnitMethod method : this.methods) {
            test += getMethodTest(method, methods.indexOf(method));
        }
        test += "}\n";
        test = getPackageAndImports() + test;
        return test;
    }

    private String getMethodTest(UnitMethod method, int methodNum) {
        String testStr = "";

        int branchNum = 0;
        for (TestPath branch : method.testBranches) {
            if (branch.isComplete(method) || printStubs) {
                imports.addAll(branch.imports);
                testStr += "\t@Test\n";
                testStr += "\tpublic void ";
                String methodName = "test_method_" + method.name + "_" + methodNum + "_branch_" + branchNum;
                invokeMethodNames.add(methodName);
                testStr += methodName + "() throws Throwable\n";
                testStr += "\t{\n";
                testStr += getTestBody(method, branch, branchNum);
                testStr += "\n\t}\n\n";
            }
            branchNum++;
        }

        return testStr;

    }

    private String getTestBody(UnitMethod method, TestPath branch, int branchNum) {
        String testBody = "";

        String testPrintout = "Now Testing Method:" + method.name + " Branch:" + branchNum;
        testBody += "System.out.println(\"" + testPrintout + "\");\n";
        testBody += getConstructor(method, branch);
        testBody += getAdditionalTestConstraints(branch);

        if (method.isConstructor()) {
            // Do nothing
        } else if (method.isVoidMethod()) {
            testBody += "\n//Call Method\n";
            testBody += getMethodCall(method, branch);
        } else {
            testBody += "\n//Get expected result and result\n";
            testBody += "Object expResult = " + branch.expectedOutput + ";\n";
            testBody += "Object result = " + getMethodCall(method, branch);

            testBody += "\n//Check Return value\n";
            testBody += "want.object(result).isEqualTo(expResult);\n";
        }
        testBody += getAdditionalVerificationPoints(branch);
        testBody = addTabs(testBody, 2);

        return testBody;
    }

    private String getMethodCall(UnitMethod method, TestPath branch) {
        String methodCall = "";
        if (method.hasModifier("static")) {

            methodCall = this.className + "." + getMethodStr(method, branch);
            methodCall += "\n";
        } else {
            methodCall = "instance." + getMethodStr(method, branch);
            methodCall += "\n";
        }
        return methodCall;

    }

    private String getConstructor(UnitMethod method, TestPath branch) {
        String constructor = "";
        if (method.isConstructor()) {
            if (method.params.size() == 0) {
                constructor += "\n//Constructor\n";
                constructor += this.className + " instance = (" + this.className + ")bean.build(" + this.className
                               + ".class)";
                constructor += ";\n";
            } else {
                constructor += "\n//Constructor\n";
                constructor += this.className + " instance = new " + this.getMethodStr(method, branch);
                constructor += "\n";
            }
        } else if (!method.hasModifier("static")) {
            if (branch.constructor.indexOf("()") != -1) {
                constructor += "\n//Constructor\n";
                constructor += this.className + " instance = (" + this.className + ")bean.build(" + this.className
                               + ".class)";
                constructor += ";\n";
            } else {
                constructor += "\n//Constructor\n";
                constructor += this.className + " instance = (" + this.className + ")bean.build(" + this.className
                               + ".class)";
                constructor += ";\n";
            }
        }
        return constructor;
    }

    private String getAdditionalVerificationPoints(TestPath branch) {
        String str = "";

        if (branch.genericTestPoints.size() > 0) {
            str += "\n//Check Test Verification Points\n";
        }
        for (GenericTestPoint point : branch.genericTestPoints) {

            String expectedResult = point.expectedResult;
            String result = "instance." + point.instanceManipulation;
            str += "want.object(" + expectedResult + ").isEqualTo(" + result + ");\n";

        }
        return str;
    }

    private String getAdditionalTestConstraints(TestPath branch) {
        String str = "";

        if (!branch.additionalConstraints.equals("")) {
            str += "\n//Add additional test constraints\n";
            str += branch.additionalConstraints;
            str += "\n";
        }

        return str;

    }

    private String getPackageAndImports() {
        String imports = "";
        for (String i : this.imports) {
            imports += "import " + i + ";\n";
        }
        imports += "\n";
        return imports;
    }

    private String addTabs(String text, int numTabs) {
        String tabOffset = "";

        for (int i = 0; i < numTabs; i++) {
            tabOffset += "\t";
        }
        text = tabOffset + text;
        text = text.replaceAll("\n", "\n" + tabOffset);
        return text;
    }

    private String getMethodStr(UnitMethod method, TestPath branch) {
        String methodStr = "";
        methodStr += method.name + "(";

        for (UserParamTestValue param : branch.userParamTestValues) {
            methodStr += param.userParamValue;
            methodStr += ", ";
        }
        if (branch.userParamTestValues.size() > 0) {
            methodStr = methodStr.substring(0, methodStr.length() - 2);
        }
        methodStr += ");";

        return methodStr;
    }
}
