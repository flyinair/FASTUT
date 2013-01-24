package fastut.evolution;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.objectweb.asm.Type;

import fastut.denpendency.MethodCall;
import fastut.generate.TestDataGenerator;
import fastut.mock.Condition;
import fastut.mock.Expect;
import fastut.mock.MockFactory;
import fastut.mock.MockPool;

public class DependencyFitnessFunction extends FitnessFunction {

    private static final long                           serialVersionUID = 5891009005406173159L;
    private final fastut.denpendency.MethodConstantPool pool;
    private final Map<Integer, Type>                    geneTypes;
    private final Map<Integer, String>                  geneNames;
    private final Map<String, String>                   mockInternalNames;
    private final List<MethodCall>                      methodCalls;
    /* private final InnerLoader innerLoader; */
    private final Set<Integer>                          paramSet;

    public DependencyFitnessFunction(fastut.denpendency.MethodConstantPool pool, Map<Integer, Type> geneTypes,
                                     Map<Integer, String> geneNames, Map<String, String> mockInternalNames,
                                     List<MethodCall> methodCalls, Set<Integer> paramSet/* , InnerLoader innerLoader */){
        this.pool = pool;
        this.geneTypes = geneTypes;
        this.geneNames = geneNames;
        this.mockInternalNames = mockInternalNames;
        this.methodCalls = methodCalls;
        this.paramSet = paramSet;
        /* this.innerLoader = innerLoader; */
    }

    @Override
    protected double evaluate(IChromosome a_subject) {
        fastut.coverage.data.TouchCollector.reset();
        if (TestDataGenerator.projectData != null) {
            TestDataGenerator.projectData.reset();
        }

        int size = a_subject.size();
        int iSize = geneTypes.size();
        int gSize = size / iSize;
        Map<String, Object> valueMap = new HashMap<String, Object>();
        int gIndex = 0;
        for (int i = 0; i < gSize; ++i) {
            Type[] argumentTypes = Type.getArgumentTypes(pool.getDesc());
            Object[] initargs = new Object[argumentTypes.length];
            for (int j = 0; j < iSize; ++j) {

                String gName = geneNames.get(j);
                if (paramSet.contains(j)) {
                    int index = Integer.parseInt(gName.substring("arg".length()));
                    initargs[index] = a_subject.getGene(gIndex).getAllele();
                    gIndex++;
                    continue;
                }

                Type type = geneTypes.get(j);
                if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
                    Object value = a_subject.getGene(gIndex).getAllele();
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
                            Class<?> mockClass = MockFactory.mock(call.getOwner().replace('/', '.')/* , innerLoader */);
                            Object mock = fastut.util.TypeResolverFactory.newInstance(mockClass);
                            Condition condition = new Condition(call.getOwner().replace('/', '.') + "."
                                                                + call.getName() + call.getDesc());
                            Object value = a_subject.getGene(gIndex).getAllele();
                            gIndex++;
                            Expect expect = new Expect(value);
                            MockPool.setExpect(condition, expect);
                            valueMap.put(mockName, mock);
                        }
                    }

                } else {
                    Object value = a_subject.getGene(gIndex).getAllele();
                    valueMap.put(gName, value);
                    gIndex++;
                }

            }

            try {

                java.lang.reflect.Method method = fastut.util.Initializer.getMethod(Class.forName(pool.getClassName(),
                                                                                                  true/*
                                                                                                       * , innerLoader
                                                                                                       */,
                                                                                                  MockFactory.currentLoader()),
                                                                                    pool.getName(), pool.getDesc());

                Object receiver = null;
                if (!Modifier.isStatic(method.getModifiers())) {
                    Class<?> receiverClass = Class.forName(pool.getClassName(), true/* , innerLoader */,
                                                           MockFactory.currentLoader());
                    receiver = fastut.util.TypeResolverFactory.newInstance(receiverClass);
                    for (Map.Entry<String, Object> entryV : valueMap.entrySet()) {
                        fastut.util.ObjectSelector.set(receiver, entryV.getKey(), entryV.getValue());
                    }
                }

                method.invoke(receiver, initargs);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        fastut.coverage.data.TouchCollector.applyTouchesOnProjectData(TestDataGenerator.projectData);
        fastut.coverage.data.ClassData classData = TestDataGenerator.projectData.getClassData(pool.getClassName());
        return classData.getBranchCoverageRate(pool.getName() + pool.getDesc());
    }

}
