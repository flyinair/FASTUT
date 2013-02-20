package fastut.evolution;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import fastut.denpendency.MethodCall;
import fastut.denpendency.MethodConstantPool;
import fastut.mock.Condition;
import fastut.mock.Expect;
import fastut.mock.MockFactory;
import fastut.mock.MockPool;
import fastut.util.ClassUtil;
import fastut.util.NameUtil;
import fastut.util.ObjectSelector;
import fastut.util.TypeResolverFactory;
import fastut.util.generics.type.ListSignaturedType;
import fastut.util.generics.type.MapSignaturedType;
import fastut.util.generics.type.SignaturedType;

public class MethodInvokeContext {

    private final MethodConstantPool           pool;
    private final List<MethodCall>             methodCalls;
    private final Map<Integer, SignaturedType> geneTypes         = new HashMap<Integer, SignaturedType>();
    private final Map<Integer, String>         geneNames         = new HashMap<Integer, String>();
    private final Map<String, String>          mockInternalNames = new HashMap<String, String>();
    private final Set<Integer>                 paramSet          = new HashSet<Integer>();
    private final Map<String, Object>          valueMap          = new HashMap<String, Object>();
    private final Object[]                     initargs;

    public MethodInvokeContext(MethodConstantPool pool, List<MethodCall> methodCalls){
        this.pool = pool;
        this.methodCalls = methodCalls;
        initargs = new Object[Type.getArgumentTypes(pool.getDesc()).length];
    }

    void reset() {
        valueMap.clear();
        for (int i = 0; i < initargs.length; ++i) {
            initargs[i] = null;
        }
    }

    public Object[] getInitArgs() {
        return initargs;
    }

    public Map<Integer, SignaturedType> getGeneTypes() {
        return geneTypes;
    }

    public List<MethodCall> getMethodCalls() {
        return methodCalls;
    }

    public void setGeneInfo(int index, String name, SignaturedType type) {
        geneNames.put(index, name);
        geneTypes.put(index, type);
    }

    public void setMockName(String internalClassName, String name) {
        mockInternalNames.put(internalClassName, name);
    }

    public SignaturedType getGeneType(int index) {
        return geneTypes.get(index);
    }

    public Object processField(int index, GeneValueIterator geneIter) {
        SignaturedType st = geneTypes.get(index);
        if (st instanceof ListSignaturedType) {
            return processListField((ListSignaturedType) st, index, geneIter);
        } else if (st instanceof MapSignaturedType) {
            return processMapField((MapSignaturedType) st, index, geneIter);
        } else if (st.getType().getSort() == Type.OBJECT && !st.getType().getDescriptor().equals("Ljava/lang/String;")) {
            return processMockField(st, index, geneIter);
        } else {
            return processBaseField(st, index, geneIter);
        }
    }

    Object adjustValue(Type type, GeneValueIterator geneIter) {
        if (type.getSort() == Type.SHORT || type.getDescriptor().equals("Ljava/lang/Short;")) {
            return Short.valueOf((short) (int) (Integer) (geneIter.next()));
        }
        if (type.getDescriptor().equals("Ljava/lang/String;")) {
            return pool.getObject(type, (Integer) (geneIter.next()));
        }
        return geneIter.next();
    }

    @SuppressWarnings("unchecked")
    Object processListField(ListSignaturedType lst, int index, GeneValueIterator geneIter) {
        String gName = geneNames.get(index);
        List<Object> value = (List<Object>) valueMap.get(gName);
        Type argType = lst.getArgType().getType();
        Object obj = adjustValue(argType, geneIter);
        if (obj != null) {
            if (value == null) {
                value = new ArrayList<Object>();
            }
            value.add(obj);
        }

        valueMap.put(gName, value);
        return value;
    }

    @SuppressWarnings("unchecked")
    Object processMapField(MapSignaturedType mst, int index, GeneValueIterator geneIter) {
        String gName = geneNames.get(index);
        String mapName = NameUtil.transform(gName);
        Map<Object, Object> value = (Map<Object, Object>) valueMap.get(mapName);
        Type kType = mst.getKtype().getType();
        Type vType = mst.getVtype().getType();
        Object k = adjustValue(kType, geneIter);
        Object v = adjustValue(vType, geneIter);
        if (k != null) {
            if (value == null) {
                value = new HashMap<Object, Object>();
            }
            value.put(k, v);
            valueMap.put(mapName, value);
        } else {
            valueMap.put(mapName, null);
        }
        return value;
    }

    List<Object> processMockField(SignaturedType st, int index, GeneValueIterator geneIter) {
        List<Object> expectValues = new ArrayList<Object>();
        String gName = geneNames.get(index);
        for (MethodCall call : methodCalls) {
            if (mockInternalNames.containsKey(call.getOwner()) && Type.getReturnType(call.getDesc()) != Type.VOID_TYPE) {
                String mockName = mockInternalNames.get(call.getOwner());
                if (!mockName.equals(gName)) {
                    continue;
                }
                Class<?> mockClass = MockFactory.mock(call.getOwner().replace('/', '.'));
                Object mock = TypeResolverFactory.newInstance(mockClass);
                Condition condition = new Condition(call.getOwner().replace('/', '.') + "." + call.getName()
                                                    + call.getDesc());
                Object value = geneIter.next();
                expectValues.add(value);
                Expect expect = new Expect(value);
                MockPool.setExpect(condition, expect);
                valueMap.put(mockName, mock);
            }
        }
        return expectValues;
    }

    Object processBaseField(SignaturedType st, int index, GeneValueIterator geneIter) {
        Object value = adjustValue(st.getType(), geneIter);
        valueMap.put(geneNames.get(index), value);
        return value;
    }

    public Object processParam(int index, GeneValueIterator geneIter) {
        String gName = geneNames.get(index);
        SignaturedType st = geneTypes.get(index);
        int argIndex = Integer.parseInt(gName.substring("arg".length()));
        if (st instanceof ListSignaturedType) {
            return processListParam(st, argIndex, geneIter);
        } else if (st instanceof MapSignaturedType) {
            return processMapParam(st, argIndex, geneIter);
        } else {
            return processBaseParam(st, argIndex, geneIter);
        }
    }

    Object processListParam(SignaturedType st, int argIndex, GeneValueIterator geneIter) {
        return null;
    }

    Object processMapParam(SignaturedType st, int argIndex, GeneValueIterator geneIter) {
        return null;
    }

    Object processBaseParam(SignaturedType st, int argIndex, GeneValueIterator geneIter) {
        return initargs[argIndex] = adjustValue(st.getType(), geneIter);
    }

    public void markParamSign(int index) {
        paramSet.add(index);
    }

    public String getClassName() {
        return pool.getClassName();
    }

    public String getMethodName() {
        return pool.getName();
    }

    public String getMethodDesc() {
        return pool.getDesc();
    }

    public String getMethodSignature() {
        return pool.getName() + pool.getDesc();
    }

    public boolean isParam(int index) {
        return paramSet.contains(index);
    }

    public boolean isMapBegin(int index) {
        String gName = geneNames.get(index);
        if(gName.endsWith("$fastut$map$key")) {
            return true;
        }
        return false;
    }

    public boolean shouldBeMock(String owner) {
        return mockInternalNames.containsKey(owner);
    }

    public String getMockName(String owner) {
        return mockInternalNames.get(owner);
    }

    public int getGeneTypeSize() {
        return geneTypes.size();
    }

    public String getGeneName(int index) {
        return geneNames.get(index);
    }

    public Object tryInvoke() {
        try {
            Class<?> receiverClass = Class.forName(pool.getClassName(), true, MockFactory.currentLoader());
            Method method = ClassUtil.getMethod(receiverClass, pool.getName(), pool.getDesc());

            Object receiver = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                receiver = TypeResolverFactory.newInstance(receiverClass);
                for (Map.Entry<String, Object> entryV : valueMap.entrySet()) {
                    ObjectSelector.set(receiver, entryV.getKey(), entryV.getValue());
                }
            }
            Object ret = method.invoke(receiver, initargs);
            reset();
            return ret;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
