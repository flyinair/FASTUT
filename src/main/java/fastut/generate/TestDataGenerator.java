package fastut.generate;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.BooleanGene;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.FastUTDefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.LongGene;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import fastut.denpendency.DependencyCollector;
import fastut.denpendency.DependencyKey;
import fastut.denpendency.FastUTFieldNode;
import fastut.denpendency.MethodCall;
import fastut.evolution.DependencyFitnessFunction;
import fastut.generate.struct.UnitMethod;
import fastut.mock.Condition;
import fastut.mock.Expect;
import fastut.mock.MockFactory;
import fastut.mock.MockPool;

public class TestDataGenerator {

    final DependencyCollector                      collector;
    final fastut.denpendency.MethodScanner         scanner;
    final List<Gene>                               genes                 = new ArrayList<Gene>();
    final Map<Integer, Type>                       geneTypeMap           = new HashMap<Integer, Type>();
    final Configuration                            geneConfiguration     = new FastUTDefaultConfiguration();
    private Collection<?>                          ignoreRegexes         = new Vector<Object>();
    private Collection<?>                          ignoreBranchesRegexes = new Vector<Object>();
    public static fastut.coverage.data.ProjectData projectData           = new fastut.coverage.data.ProjectData();
    private final byte[]                           codes;

    public byte[] getCode() {
        return codes;
    }

    public TestDataGenerator(String className) throws IOException{
        Configuration.reset();
        // collector
        ClassReader cr = new ClassReader(className);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        scanner = new fastut.denpendency.MethodScanner(cw);
        collector = new DependencyCollector(scanner);
        cr.accept(collector, ClassReader.SKIP_DEBUG);

        // point
        ClassReader ccr = new ClassReader(className);
        ClassWriter ccw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        fastut.coverage.instrument.ClassInstrumenter ccv = new fastut.coverage.instrument.ClassInstrumenter(
                                                                                                            projectData,
                                                                                                            ccw,
                                                                                                            ignoreRegexes,
                                                                                                            ignoreBranchesRegexes);
        ccr.accept(ccv, 0);

        codes = ccw.toByteArray();
    }

    public Gene getGene(Type type, fastut.denpendency.MethodConstantPool pool) throws Throwable {
        int size = pool.getSize(type);
        switch (type.getSort()) {
            case Type.OBJECT:
                if (type.getDescriptor().equals("Ljava/lang/String;")) {
                    size += 10;
                    return new IntegerGene(geneConfiguration, 0, size);
                }
                break;
            case Type.BOOLEAN:
                return new BooleanGene(geneConfiguration);
            case Type.DOUBLE:
                if (size == -1 || size == 0) {
                    return new DoubleGene(geneConfiguration);
                } else {
                    double min = (Double) pool.getObject(double.class, 0) - 10;
                    double max = (Double) pool.getObject(double.class, size - 1) + 10;
                    return new DoubleGene(geneConfiguration, min, max);
                }
            case Type.LONG:
                if (size == -1 || size == 0) {
                    return new LongGene(geneConfiguration);
                } else {
                    long min = (Long) pool.getObject(long.class, 0) - 10;
                    long max = (Long) pool.getObject(long.class, size - 1) + 10;
                    return new LongGene(geneConfiguration, (int) min, (int) max);
                }
            case Type.SHORT:
                if (size == -1 || size == 0) {
                    return new IntegerGene(geneConfiguration, Short.MIN_VALUE, Short.MAX_VALUE);
                } else {
                    short min = (short) ((Short) pool.getObject(short.class, 0) - 10);
                    short max = (short) ((Short) pool.getObject(short.class, size - 1) + 10);
                    return new IntegerGene(geneConfiguration, min, max);
                }
            default:
                break;
        }
        if (size == -1 || size == 0) {
            return new IntegerGene(geneConfiguration);
        } else {
            int min = (Integer) pool.getObject(int.class, 0) - 10;
            int max = (Integer) pool.getObject(int.class, size - 1) + 10;
            return new IntegerGene(geneConfiguration, min, max);
        }
    }

    public List<FastUTFieldNode> getAllFields(String name) {
        Set<FastUTFieldNode> nodeSet = new HashSet<FastUTFieldNode>();
        boolean stop = false;
        for (String classname : collector.getOverrideList()) {
            if (!stop) {
                nodeSet.addAll(collector.declared_fields.get(classname));
                if (classname.equals(name)) {
                    stop = true;
                }
            }
        }
        return new ArrayList<FastUTFieldNode>(nodeSet);
    }

    public IChromosome getBest(int branchNum, List<Gene> template, Map<Integer, Type> geneTypes,
                               Map<Integer, String> geneNames, Map<String, String> mockInternalNames,
                               List<MethodCall> methodCalls, Set<Integer> paramSet,
                               fastut.denpendency.MethodConstantPool pool) throws Throwable {
        Configuration.reset();
        DependencyFitnessFunction function = new DependencyFitnessFunction(pool, geneTypes, geneNames,
                                                                           mockInternalNames, methodCalls, paramSet);
        geneConfiguration.setFitnessFunction(function);
        List<Gene> lgs = new ArrayList<Gene>();
        for (int i = 0; i < branchNum; ++i) {
            for (int j = 0; j < template.size(); ++j) {
                lgs.add(template.get(j).newGene());
            }
        }

        Gene[] sampleGenes = lgs.toArray(new Gene[lgs.size()]);

        Chromosome sampleChromosome = new Chromosome(geneConfiguration, sampleGenes);
        geneConfiguration.setSampleChromosome(sampleChromosome);
        geneConfiguration.setPopulationSize(10);
        Genotype population = Genotype.randomInitialGenotype(geneConfiguration);
        IChromosome bestSolutionSoFar = null;
        for (int i = 0; i < 1000; i++) {
            bestSolutionSoFar = population.getFittestChromosome();
            System.out.println("the " + (i + 1) + " generation best:" + bestSolutionSoFar);
            if (bestSolutionSoFar.getFitnessValue() >= 1.0) {
                break;
            }
            population.evolve();
        }

        return bestSolutionSoFar;
    }

    public static UnitMethod toUnitMethod(MethodNode node) {
        if (Modifier.isPublic(node.access)) {
            UnitMethod m = new UnitMethod();
            m.name = node.name;
            if (Modifier.isStatic(node.access)) {
                m.modifiers.add("static");
            }
            if (node.name.equals("<init>")) {
                m.returnType = "constructor";
            } else {
                Type returnType = Type.getReturnType(node.desc);
                m.returnType = returnType.getClassName();
            }
            Type[] params = Type.getArgumentTypes(node.desc);
            Map<String, Integer> paramNameIDs = new HashMap<String, Integer>();
            for (Type t : params) {
                String paramName = t.getClassName();
                char one = paramName.charAt(0);
                paramName = Character.toLowerCase(one) + paramName.substring(1, paramName.length()) + "Arg";
                Integer id = paramNameIDs.get(paramName);
                if (id != null) {
                    id++;
                    paramNameIDs.put(paramName, id);
                    paramName += id;
                } else {
                    paramNameIDs.put(paramName, 0);
                }
                fastut.generate.struct.ParamBinding binding = new fastut.generate.struct.ParamBinding(t.getClassName(),
                                                                                                      paramName);
                m.params.add(binding);
            }
            return m;
        }
        return null;
    }

    public static final String[] LINK_STR = { "_" };

    public static boolean hasDoubleChar(String string) {
        return !(string.getBytes().length == string.length());
    }

    public static void makeSharing(Map<String, fastut.denpendency.MethodConstantPool> values) {
        Set<String> STRING_POOL = new HashSet<String>();
        Set<String> FINAL_STRING_POOL = new HashSet<String>();
        for (Map.Entry<String, fastut.denpendency.MethodConstantPool> entry : values.entrySet()) {
            STRING_POOL.addAll(entry.getValue().STRING_POOL);
        }
        for (String left : STRING_POOL) {
            if (hasDoubleChar(left)) {
                continue;
            }
            String right = (String) fastut.object.ObjectPool.getObject(String.class);
            FINAL_STRING_POOL.add(left);
            for (String link : LINK_STR) {
                FINAL_STRING_POOL.add(left + link + right);
                // FINAL_STRING_POOL.add(right + link + left);
            }
        }
        for (Map.Entry<String, fastut.denpendency.MethodConstantPool> entry : values.entrySet()) {
            entry.getValue().STRING_POOL = new ArrayList<String>(FINAL_STRING_POOL);
        }
    }

    public static void main(String[] args) throws Throwable {

        String className = "samples.ServiceBean";
        String orignalName = className;
        TestDataGenerator generator = new TestDataGenerator(className);

        Map<String, fastut.denpendency.MethodConstantPool> values = generator.scanner.getMethodConstants();
        makeSharing(values);

        List<UnitMethod> unitMethods = new ArrayList<UnitMethod>();
        for (Map.Entry<DependencyKey, MethodNode> entry : generator.collector.declared_methods.entrySet()) {
            MethodNode mNode = entry.getValue();
            if (!Modifier.isPublic(mNode.access) || mNode.name.equals("<init>")) {
                continue;
            }

            UnitMethod autoJUnitMethod = toUnitMethod(mNode);
            unitMethods.add(autoJUnitMethod);

            String methodName = mNode.name;
            String methodDesc = mNode.desc;
            className = entry.getKey().getClassName().replace('/', '.');
            Map<String, Object> valueMap = new HashMap<String, Object>();
            String methodId = className + "." + methodName + methodDesc;
            fastut.denpendency.MethodConstantPool pool = values.get(methodId);
            pool.reduce();
            System.out.println("methodID: " + methodId);

            // force to load class
            MockFactory.currentLoader().loadClass(pool.getClassName());

            int branchNum = projectData.getClassData(pool.getClassName()).getNumberOfValidBranches(pool.getName()
                                                                                                           + pool.getDesc());
            if (branchNum <= 0) {
                System.out.println("no branch, so skip! for " + methodId);
                continue;
            }

            List<Gene> genes = new ArrayList<Gene>();
            Map<Integer, Type> geneTypes = new HashMap<Integer, Type>();
            Map<Integer, String> geneNames = new HashMap<Integer, String>();
            Set<Integer> paramSet = new HashSet<Integer>();
            List<FastUTFieldNode> fields = generator.getAllFields(entry.getKey().getClassName());
            Map<String, String> mockInternalNames = new HashMap<String, String>();
            for (FastUTFieldNode node : fields) {
                if ((Modifier.isFinal(node.access) && node.signature == null)) {
                    continue;
                } else {
                    Type ft = Type.getType(node.desc);
                    if (ft.getSort() == Type.OBJECT && !ft.getClassName().equals("java.lang.String")) {
                        Class<?> retClass = MockFactory.mock(ft.getClassName());
                        if (retClass != null) {
                            mockInternalNames.put(ft.getInternalName(), node.name);
                            node.setMockable(true);
                            node.setMockedClass(retClass);
                        }
                    } else {
                        node.setMockable(false);
                        genes.add(generator.getGene(ft, pool));
                        geneNames.put(genes.size() - 1, node.name);
                        geneTypes.put(genes.size() - 1, ft);
                        // TODO
                        valueMap.put(node.name, fastut.object.ObjectPool.getObject(ft));
                    }
                }
            }

            DependencyKey key = new DependencyKey(className.replace('.', '/'), methodName, methodDesc);
            List<MethodCall> methodCalls = generator.collector.METHOD_VISITED_METHODS.get(key);
            for (MethodCall call : methodCalls) {
                if (mockInternalNames.containsKey(call.getOwner())
                    && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                    Type returnType = Type.getReturnType(call.getDesc());
                    genes.add(generator.getGene(returnType, pool));
                    Class<?> mockClass = MockFactory.mock(call.getOwner().replace('/', '.'));
                    geneTypes.put(genes.size() - 1, Type.getType(mockClass));
                    Object mock = fastut.util.TypeResolverFactory.newInstance(mockClass);
                    Condition condition = new Condition(call.getOwner().replace('/', '.') + "." + call.getName()
                                                        + call.getDesc());
                    Expect expect = new Expect(fastut.object.ObjectPool.getObject(returnType));
                    MockPool.setExpect(condition, expect);
                    String mockName = mockInternalNames.get(call.getOwner());
                    geneNames.put(genes.size() - 1, mockName);
                    // TODO
                    valueMap.put(mockName, mock);
                }
            }

            Type[] argumentTypes = Type.getArgumentTypes(pool.getDesc());
            for (int i = 0; i < argumentTypes.length; ++i) {
                if (argumentTypes[i].getSort() != Type.OBJECT
                    || argumentTypes[i].getDescriptor().equals("Ljava/lang/String;")) {
                    genes.add(generator.getGene(argumentTypes[i], pool));
                    geneTypes.put(genes.size() - 1, argumentTypes[i]);
                    geneNames.put(genes.size() - 1, "arg" + i);
                    paramSet.add(genes.size() - 1);
                } else {
                    // TODO
                }
            }

            IChromosome bestSolutionSoFar = generator.getBest(branchNum, genes, geneTypes, geneNames,
                                                              mockInternalNames, methodCalls, paramSet, pool);

            makeCode(autoJUnitMethod, pool, geneTypes, geneNames, mockInternalNames, methodCalls, paramSet,
                     bestSolutionSoFar, branchNum);

        }

        int dot = orignalName.lastIndexOf('.');
        orignalName = (dot != -1) ? orignalName.substring(dot + 1) : orignalName;
        FastUTTestCreator testCreator = new FastUTTestCreator(orignalName, unitMethods, true);
        System.out.println(testCreator.getTest());
    }

    public static void makeCode(UnitMethod target, fastut.denpendency.MethodConstantPool pool,
                                Map<Integer, Type> geneTypes, Map<Integer, String> geneNames,
                                Map<String, String> mockInternalNames, List<MethodCall> methodCalls,
                                Set<Integer> paramSet, IChromosome a_subject, int branchNum) {
        for (int i = 0; i < branchNum; ++i) {
            fastut.generate.struct.TestPath tp = new fastut.generate.struct.TestPath();
            for (fastut.generate.struct.ParamBinding param : target.params) {
                tp.userParamTestValues.add(new fastut.generate.struct.UserParamTestValue(param));
            }
            target.testBranches.add(tp);
        }

        List<fastut.generate.struct.TestPath> paths = target.testBranches;
        int size = a_subject.size();
        int iSize = geneTypes.size();
        int gSize = size / iSize;
        int gIndex = 0;
        for (int i = 0; i < gSize; ++i) {
            fastut.generate.struct.TestPath path = paths.get(i);
            String className = pool.getClassName().replace('$', '.');
            path.constructor = className + ".class";
            path.additionalConstraints = "";
            path.imports.add(className);
            Map<String, Object> valueMap = new HashMap<String, Object>();
            int pIndex = 0;
            int mIndex = 0;
            Type[] argumentTypes = Type.getArgumentTypes(pool.getDesc());
            Object[] initargs = new Object[argumentTypes.length];
            for (int j = 0; j < iSize; ++j) {

                String gName = geneNames.get(j);
                if (paramSet.contains(j)) {
                    int index = Integer.parseInt(gName.substring("arg".length()));
                    String paramStr = "";
                    if (geneTypes.get(j).getSort() == Type.OBJECT) {
                        Integer locate = (Integer) a_subject.getGene(gIndex).getAllele();
                        initargs[index] = pool.getObject(geneTypes.get(j), locate);
                        if (initargs[index] != null) {
                            paramStr += "\"" + initargs[index] + "\"";
                        } else {
                            paramStr += initargs[index];
                        }
                    } else {
                        initargs[index] = a_subject.getGene(gIndex).getAllele();
                        paramStr += initargs[index];
                    }
                    gIndex++;
                    path.userParamTestValues.get(pIndex).userParamValue = paramStr;
                    pIndex++;
                    continue;
                }

                Type type = geneTypes.get(j);
                List<Object> vauleList = new ArrayList<Object>();
                if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
                    Object value = a_subject.getGene(gIndex).getAllele();
                    vauleList.add(value);
                    Integer index = (Integer) a_subject.getGene(iSize * i + j).getAllele();
                    String str = (String) pool.getObject(type, index);
                    try {
                        value = str;
                    } catch (Throwable e) {

                    }
                    valueMap.put(gName, value);
                    gIndex++;
                } else if (type.getSort() == Type.SHORT) {
                    Object value = a_subject.getGene(gIndex).getAllele();
                    vauleList.add(value);
                    Integer index = (Integer) a_subject.getGene(iSize * i + j).getAllele();
                    value = Short.valueOf((short) (int) index);
                    valueMap.put(gName, value);
                    gIndex++;
                } else if (type.getSort() == Type.OBJECT) {
                    for (MethodCall call : methodCalls) {
                        if (mockInternalNames.containsKey(call.getOwner())
                            && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                            String mockName = mockInternalNames.get(call.getOwner());
                            if (!mockName.equals(gName)) {
                                continue;
                            }
                            Class<?> mockClass = MockFactory.mock(call.getOwner().replace('/', '.'));
                            Object mock = fastut.util.TypeResolverFactory.newInstance(mockClass);
                            Condition condition = new Condition(call.getOwner().replace('/', '.') + "."
                                                                + call.getName() + call.getDesc());
                            Object value = a_subject.getGene(gIndex).getAllele();
                            vauleList.add(value);
                            gIndex++;
                            Expect expect = new Expect(value);
                            MockPool.setExpect(condition, expect);
                            valueMap.put(mockName, mock);
                        }
                    }
                } else {
                    Object value = a_subject.getGene(gIndex).getAllele();
                    vauleList.add(value);
                    valueMap.put(gName, value);
                    gIndex++;
                }

                String valueStr = "";
                Object value = valueMap.get(gName);
                if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
                    if (value != null) {
                        valueStr = "\"" + value + "\"";
                    } else {
                        valueStr = "null";
                    }
                } else if (type.getSort() == Type.LONG) {
                    valueStr = value + "L";
                } else if (type.getSort() == Type.DOUBLE) {
                    valueStr = value + "D";
                } else if (type.getSort() == Type.SHORT) {
                    valueStr = "((short)" + value + ")";
                } else if (type.getSort() == Type.OBJECT) {
                    // TODO
                } else {
                    valueStr += "((" + geneTypes.get(j).getClassName() + ")" + value + ")";
                }
                if (!paramSet.contains(j)
                    && (type.getSort() != Type.OBJECT || type.getDescriptor().equals("Ljava/lang/String;"))) {
                    path.additionalConstraints += "ReflectUtil.setFieldValue(instance, \"" + gName + "\", " + valueStr
                                                  + ");\n";
                } else if (paramSet.contains(j)) {

                    valueStr = "" + initargs[pIndex];
                    if (geneTypes.get(j).getSort() == Type.OBJECT) {
                        valueStr = "\"" + valueStr + "\"";
                    }
                    path.userParamTestValues.get(pIndex).userParamValue = valueStr;
                    pIndex++;
                } else {

                    for (MethodCall call : methodCalls) {
                        if (mockInternalNames.containsKey(call.getOwner())
                            && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                            String mockName = mockInternalNames.get(call.getOwner());
                            if (!mockName.equals(gName)) {
                                continue;
                            }
                            mIndex++;
                            path.additionalConstraints += "new NonStrictExpectations() {\n";
                            path.additionalConstraints += "    {\n";
                            path.additionalConstraints += "\t" + gName + "." + call.getName()
                                                          + getParamString(call.getDesc()) + ";\n";
                            path.additionalConstraints += "\treturns(" + a_subject.getGene(gIndex).getAllele() + ");\n";
                            path.additionalConstraints += "    }\n";
                            path.additionalConstraints += "};\n";
                        }
                    }
                }

            }

            try {

                java.lang.reflect.Method method = fastut.util.Initializer.getMethod(Class.forName(pool.getClassName(),
                                                                                                  true,
                                                                                                  MockFactory.currentLoader()),
                                                                                    pool.getName(), pool.getDesc());

                Object receiver = null;
                if (!Modifier.isStatic(method.getModifiers())) {
                    Class<?> receiverClass = Class.forName(pool.getClassName(), true, MockFactory.currentLoader());
                    receiver = fastut.util.TypeResolverFactory.newInstance(receiverClass);
                    for (Map.Entry<String, Object> entryV : valueMap.entrySet()) {
                        fastut.util.ObjectSelector.set(receiver, entryV.getKey(), entryV.getValue());
                    }
                }

                Object ret = method.invoke(receiver, initargs);
                String retStr = (ret instanceof String) && (ret != null) ? "\"" + ret + "\"" : "" + ret;
                path.expectedOutput = "" + retStr;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTypeString(Type type) {
        StringBuilder builder = new StringBuilder();
        switch (type.getSort()) {
            case Type.LONG:
                builder.append("anyLong");
                break;
            case Type.SHORT:
                builder.append("anyShort");
                break;
            case Type.BYTE:
                builder.append("anyByte");
                break;
            case Type.BOOLEAN:
                builder.append("anyBoolean");
                break;
            case Type.CHAR:
                builder.append("anyChar");
                break;
            case Type.FLOAT:
                builder.append("anyFloat");
                break;
            case Type.OBJECT:
                builder.append('(').append(type.getClassName()).append(')').append("any");
                break;
        }
        return builder.toString();
    }

    public static String getParamString(String desc) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        Type[] types = Type.getArgumentTypes(desc);
        if (types.length >= 1) {
            for (int i = 0; i < types.length - 1; ++i) {
                builder.append(getTypeString(types[i])).append(", ");
            }
            builder.append(getTypeString(types[types.length - 1]));
        }
        builder.append(')');
        return builder.toString();
    }
}
