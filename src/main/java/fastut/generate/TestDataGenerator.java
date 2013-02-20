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

import fastut.coverage.data.ProjectData;
import fastut.coverage.data.TouchCollector;
import fastut.coverage.instrument.ClassInstrumenter;
import fastut.denpendency.DependencyCollector;
import fastut.denpendency.DependencyKey;
import fastut.denpendency.FastUTFieldNode;
import fastut.denpendency.FieldCall;
import fastut.denpendency.MethodCall;
import fastut.denpendency.MethodConstantPool;
import fastut.denpendency.MethodScanner;
import fastut.evolution.DependencyFitnessFunction;
import fastut.evolution.GeneValueIterator;
import fastut.evolution.MethodInvokeContext;
import fastut.generate.struct.ParamBinding;
import fastut.generate.struct.TestPath;
import fastut.generate.struct.UnitMethod;
import fastut.generate.struct.UserParamTestValue;
import fastut.mock.MockFactory;
import fastut.util.FastUTRegxString;
import fastut.util.FormatOut;
import fastut.util.NameUtil;
import fastut.util.TypeMatcher;
import fastut.util.generics.parser.SignatureParser;
import fastut.util.generics.tree.TypeSignature;
import fastut.util.generics.type.ListSignaturedType;
import fastut.util.generics.type.MapSignaturedType;
import fastut.util.generics.type.SignaturedType;
import fastut.util.generics.visitor.Reifier;

public class TestDataGenerator {

    final DependencyCollector collector;
    final MethodScanner       scanner;
    final List<Gene>          genes                 = new ArrayList<Gene>();
    final Map<Integer, Type>  geneTypeMap           = new HashMap<Integer, Type>();
    final Configuration       geneConfiguration     = new FastUTDefaultConfiguration();
    private Collection<?>     ignoreRegexes         = new Vector<Object>();
    private Collection<?>     ignoreBranchesRegexes = new Vector<Object>();
    public static ProjectData projectData           = new ProjectData();
    private final byte[]      codes;

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
        ClassInstrumenter ccv = new ClassInstrumenter(projectData, ccw, ignoreRegexes, ignoreBranchesRegexes);
        ccr.accept(ccv, 0);

        codes = ccw.toByteArray();
    }

    public Gene getGene(SignaturedType type, MethodConstantPool pool) throws Throwable {
        if (type instanceof ListSignaturedType) {
            ListSignaturedType lt = (ListSignaturedType) type;
            return getGene(lt.getArgType(), pool);
        } else {
            return getGene(type.getType(), pool);
        }
    }

    public Gene getGene(Type type, MethodConstantPool pool) throws Throwable {
        int size = pool.getSize(type);
        switch (type.getSort()) {
            case Type.OBJECT:
                if (type.getDescriptor().equals("Ljava/lang/String;")) {
                    size += 10;
                    return new IntegerGene(geneConfiguration, 0, size);
                } else if (type.getDescriptor().equals("Ljava/lang/Integer;")) {
                    return getGene(Type.INT_TYPE, pool);
                } else if (type.getDescriptor().equals("Ljava/lang/Long;")) {
                    return getGene(Type.LONG_TYPE, pool);
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

    public IChromosome getBest(int branchNum, List<Gene> template, MethodInvokeContext invokeContext) throws Throwable {
        Configuration.reset();
        DependencyFitnessFunction function = new DependencyFitnessFunction(invokeContext);
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
        for (int i = 0; i < 10; i++) {
            bestSolutionSoFar = population.getFittestChromosome();
            System.out.println("the " + (i + 1) + " generation best:" + bestSolutionSoFar);
            if (bestSolutionSoFar.getFitnessValue() >= 1.0) {
                break;
            }
            population.evolve();
        }

        return bestSolutionSoFar;
    }

    static UnitMethod toUnitMethod(MethodNode node) {
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

    public static boolean hasChinese(String string) {
        return !(string.getBytes().length == string.length());
    }

    public static boolean hasAnyRegex(String target) {
        return target.contains(".*?");
    }

    public static void makeSharing(Map<String, fastut.denpendency.MethodConstantPool> values) {
        Set<String> STRING_POOL = new HashSet<String>();
        Set<String> FINAL_STRING_POOL = new HashSet<String>();
        for (Map.Entry<String, fastut.denpendency.MethodConstantPool> entry : values.entrySet()) {
            STRING_POOL.addAll(entry.getValue().STRING_POOL);
        }
        Set<String> regexlist = new HashSet<String>();
        for (String left : STRING_POOL) {
            if (hasChinese(left)) {
                continue;
            }
            if (hasAnyRegex(left)) {
                regexlist.add(left);
                continue;
            }
            FINAL_STRING_POOL.add(left);
        }
        FastUTRegxString regxStr = new FastUTRegxString();
        if (FINAL_STRING_POOL.size() < 5) {
            regxStr.parseRegx("\\w{2,12}");
            for (int i = 0; i < 4; ++i) {
                String randStr = regxStr.randString();
                FINAL_STRING_POOL.add(randStr);
                FINAL_STRING_POOL.add(randStr.substring(1));
                FINAL_STRING_POOL.add(randStr.substring(0, randStr.length() - 1));
            }
        }
        FINAL_STRING_POOL.add("");
        Set<String> externals = new HashSet<String>();
        for (String regexStr : regexlist) {
            externals.addAll(regxStr.replaceAndParse(regexStr, ".*?", FINAL_STRING_POOL));
        }
        FINAL_STRING_POOL.addAll(externals);

        for (Map.Entry<String, MethodConstantPool> entry : values.entrySet()) {
            entry.getValue().STRING_POOL = new ArrayList<String>(FINAL_STRING_POOL);
        }
    }

    public static void main(String[] args) throws Throwable {

        String className = "samples.ComplexObject";
        String orignalName = className;
        TestDataGenerator generator = new TestDataGenerator(className);

        Map<String, MethodConstantPool> values = generator.scanner.getMethodConstants();
        makeSharing(values);

        List<UnitMethod> unitMethods = new ArrayList<UnitMethod>();
        for (Map.Entry<DependencyKey, MethodNode> entry : generator.collector.declared_methods.entrySet()) {
            MethodNode mNode = entry.getValue();
            if (!Modifier.isPublic(mNode.access) || mNode.name.equals("<init>")) {
                continue;
            }

            UnitMethod autoUnitMethod = toUnitMethod(mNode);
            unitMethods.add(autoUnitMethod);

            String methodName = mNode.name;
            String methodDesc = mNode.desc;

            if (methodName.equals("main") && methodDesc.equals("([Ljava/lang/String;)V")) {
                System.out.println("skip main!");
                continue;
            }

            className = entry.getKey().getClassName().replace('/', '.');
            String methodId = className + "." + methodName + methodDesc;
            MethodConstantPool pool = values.get(methodId);
            pool.reduce();
            System.err.println(pool);
            Set<String> allTypes = MethodConstantPool.CLASS_TYPE_SET.get(className);
            System.err.println("allTypes: " + allTypes);
            Set<Type> matchedTypes = TypeMatcher.match(Type.getType(List.class), allTypes);
            System.err.println("match: " + matchedTypes);
            System.err.println("methodID: " + methodId);

            DependencyKey key = new DependencyKey(className.replace('.', '/'), methodName, methodDesc);
            List<MethodCall> methodCalls = generator.collector.METHOD_VISITED_METHODS.get(key);
            Set<FieldCall> fieldCalls = generator.collector.METHOD_VISITED_FIELDS.get(key);
            for (FieldCall call : fieldCalls) {
                System.err.println(call);
            }

            MethodInvokeContext invokeContext = new MethodInvokeContext(pool, methodCalls);

            // force to load class
            MockFactory.currentLoader().loadClass(pool.getClassName());
            int branchNum = projectData.getClassData(pool.getClassName()).getNumberOfValidBranches(pool.getName()
                                                                                                           + pool.getDesc());

            if (branchNum <= 0) {
                System.out.println("no branch, so skip! for " + methodId);
                continue;
            } else {
                System.err.println(methodId + "'s branch num [" + branchNum + "].");
            }

            List<Gene> genes = new ArrayList<Gene>();
            List<FastUTFieldNode> fields = generator.getAllFields(entry.getKey().getClassName());
            System.err.println(fields);
            for (FastUTFieldNode node : fields) {
                if ((Modifier.isFinal(node.access) && node.signature == null)) {
                    continue;
                } else {
                    Type ft = Type.getType(node.desc);
                    if (ft.getSort() == Type.OBJECT && !ft.getClassName().equals("java.lang.String")) {
                        Class<?> retClass = MockFactory.mock(ft.getClassName());
                        if (retClass != null) {
                            invokeContext.setMockName(ft.getInternalName(), node.name);
                            node.setMockable(true);
                            node.setMockedClass(retClass);
                        } else {
                            System.err.println("mock failed for " + node);
                            node.setMockable(false);
                            if (node.signature != null) {
                                Reifier rf = Reifier.make();
                                TypeSignature tree = SignatureParser.make().parseTypeSig(node.signature);
                                tree.accept(rf);
                                SignaturedType t = rf.getResult();
                                if (t instanceof ListSignaturedType) {
                                    genes.add(generator.getGene(t, pool));
                                    invokeContext.setGeneInfo(genes.size() - 1, node.name, t);
                                } else if (t instanceof MapSignaturedType) {
                                    MapSignaturedType mst = (MapSignaturedType)t;
                                    genes.add(generator.getGene(mst.getKtype(), pool));
                                    invokeContext.setGeneInfo(genes.size() - 1, NameUtil.getMapKeyName(node.name), mst);
                                    genes.add(generator.getGene(mst.getVtype(), pool));
                                    invokeContext.setGeneInfo(genes.size() - 1, NameUtil.getMapValueName(node.name), mst);
                                }
                            }
                        }
                    } else {
                        node.setMockable(false);
                        SignaturedType t = SignaturedType.makeSimpleType(ft);
                        genes.add(generator.getGene(t, pool));
                        invokeContext.setGeneInfo(genes.size() - 1, node.name, t);
                    }
                }
            }

            for (MethodCall call : methodCalls) {
                System.err.println(call);
                if (invokeContext.shouldBeMock(call.getOwner()) && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                    Type returnType = Type.getReturnType(call.getDesc());
                    genes.add(generator.getGene(SignaturedType.makeSimpleType(returnType), pool));
                    Class<?> mockClass = MockFactory.mock(call.getOwner().replace('/', '.'));
                    String mockName = invokeContext.getMockName(call.getOwner());
                    invokeContext.setGeneInfo(genes.size() - 1, mockName,
                                              SignaturedType.makeSimpleType(Type.getType(mockClass)));
                }
            }

            Type[] argumentTypes = Type.getArgumentTypes(pool.getDesc());
            for (int i = 0; i < argumentTypes.length; ++i) {
                if (argumentTypes[i].getSort() != Type.OBJECT
                    || argumentTypes[i].getDescriptor().equals("Ljava/lang/String;")) {
                    genes.add(generator.getGene(SignaturedType.makeSimpleType(argumentTypes[i]), pool));
                    invokeContext.setGeneInfo(genes.size() - 1, "arg" + i,
                                              SignaturedType.makeSimpleType(argumentTypes[i]));
                    invokeContext.markParamSign(genes.size() - 1);
                } else {
                    // TODO
                }
            }

            IChromosome bestSolutionSoFar = generator.getBest(branchNum, genes, invokeContext);

            makeCode(initUnitMethod(autoUnitMethod, branchNum, invokeContext.getClassName()), invokeContext,
                     bestSolutionSoFar);
        }

        int dot = orignalName.lastIndexOf('.');
        orignalName = (dot != -1) ? orignalName.substring(dot + 1) : orignalName;
        FastUTTestCreator testCreator = new FastUTTestCreator(orignalName, unitMethods, true);
        System.out.println(testCreator.getTest());
    }

    static UnitMethod initUnitMethod(UnitMethod target, int branchNum, String className) {
        for (int i = 0; i < branchNum; ++i) {
            TestPath tp = new TestPath();
            tp.imports.add(className);
            for (ParamBinding param : target.params) {
                tp.userParamTestValues.add(new UserParamTestValue(param));
            }
            target.testBranches.add(tp);
        }
        return target;
    }

    public static void makeCode(UnitMethod target, MethodInvokeContext invokeContext, IChromosome a_subject) {
        List<TestPath> paths = target.testBranches;
        int size = a_subject.size();
        int iSize = invokeContext.getGeneTypeSize();
        int gSize = size / iSize;
        GeneValueIterator geneIter = new GeneValueIterator(a_subject);
        for (int i = 0; i < gSize; ++i) {
            TestPath path = paths.get(i);
            int pIndex = 0;
            Object[] initargs = invokeContext.getInitArgs();
            for (int j = 0; j < iSize; ++j) {

                SignaturedType st = invokeContext.getGeneType(j);
                Type type = st.getType();

                if (invokeContext.isParam(j)) {
                    Object paramValue = invokeContext.processParam(j, geneIter);
                    path.userParamTestValues.get(pIndex).userParamValue = FormatOut.asString(paramValue);
                    pIndex++;
                    continue;
                }

                String gName = invokeContext.getGeneName(j);
                Object value = invokeContext.processField(j, geneIter);
                if (!invokeContext.isParam(j)
                    && (type.getSort() != Type.OBJECT || type.getDescriptor().equals("Ljava/lang/String;") || st instanceof ListSignaturedType || st instanceof MapSignaturedType)) {
                    if (st instanceof ListSignaturedType || st instanceof MapSignaturedType) {
                        path.additionalConstraints += FormatOut.asDeclartion(st, gName, value);
                        path.additionalConstraints += FormatOut.asOperation("ReflectUtil", "setFieldValue", "instance",
                                                                            FormatOut.asString(gName), NameUtil.transform(gName));
                    } else {
                        path.additionalConstraints += FormatOut.asOperation("ReflectUtil", "setFieldValue", "instance",
                                                                            FormatOut.asString(gName),
                                                                            FormatOut.asString(value));
                    }
                } else if (invokeContext.isParam(j)) {
                    path.userParamTestValues.get(pIndex).userParamValue = FormatOut.asString(initargs[pIndex]);
                    pIndex++;
                } else {
                    @SuppressWarnings("unchecked")
                    List<Object> vl = (List<Object>) value;
                    int mIndex = 0;
                    for (MethodCall call : invokeContext.getMethodCalls()) {
                        if (invokeContext.shouldBeMock(call.getOwner())
                            && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                            String mockName = invokeContext.getMockName(call.getOwner());
                            if (!mockName.equals(gName)) {
                                continue;
                            }
                            path.additionalConstraints += FormatOut.asNonStrictExpectations(gName, call.getName(),
                                                                                            call.getDesc(),
                                                                                            vl.get(mIndex));
                            mIndex++;
                        }
                    }
                }

                if(invokeContext.isMapBegin(j)) {
                    ++j;
                }
            }

            try {
                Object ret = invokeContext.tryInvoke();
                path.expectedOutput = FormatOut.asString(ret);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

}
