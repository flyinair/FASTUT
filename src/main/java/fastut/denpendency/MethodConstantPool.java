package fastut.denpendency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.Type;

public class MethodConstantPool {

    private final String   name;
    private final String   desc;
    private final String   className;

    public List<Character> CHAR_POOL   = new ArrayList<Character>();
    public List<Integer>   INT_POOL    = new ArrayList<Integer>();
    public List<Double>    DOUBLE_POOL = new ArrayList<Double>();
    public List<Float>     FLOAT_POOL  = new ArrayList<Float>();
    public List<Long>      LONG_POOL   = new ArrayList<Long>();
    public List<Short>     SHORT_POOL  = new ArrayList<Short>();
    public List<Byte>      BYTE_POOL   = new ArrayList<Byte>();
    public List<String>    STRING_POOL = new ArrayList<String>();

    public MethodConstantPool(String className, String name, String desc){
        this.className = className;
        this.name = name;
        this.desc = desc;
    }

    public void reduce() {
        CHAR_POOL = new ArrayList<Character>(new HashSet<Character>(CHAR_POOL));
        INT_POOL = new ArrayList<Integer>(new HashSet<Integer>(INT_POOL));
        DOUBLE_POOL = new ArrayList<Double>(new HashSet<Double>(DOUBLE_POOL));
        FLOAT_POOL = new ArrayList<Float>(new HashSet<Float>(FLOAT_POOL));
        LONG_POOL = new ArrayList<Long>(new HashSet<Long>(LONG_POOL));
        SHORT_POOL = new ArrayList<Short>(new HashSet<Short>(SHORT_POOL));
        BYTE_POOL = new ArrayList<Byte>(new HashSet<Byte>(BYTE_POOL));
        STRING_POOL = new ArrayList<String>(new HashSet<String>(STRING_POOL));
        // Collections.shuffle(CHAR_POOL);
        // Collections.shuffle(INT_POOL);
        // Collections.shuffle(DOUBLE_POOL);
        // Collections.shuffle(FLOAT_POOL);
        // Collections.shuffle(LONG_POOL);
        // Collections.shuffle(SHORT_POOL);
        // Collections.shuffle(STRING_POOL);
        // Collections.shuffle(BYTE_POOL);
        Collections.sort(CHAR_POOL);
        Collections.sort(INT_POOL);
        Collections.sort(DOUBLE_POOL);
        Collections.sort(FLOAT_POOL);
        Collections.sort(LONG_POOL);
        Collections.sort(SHORT_POOL);
        Collections.sort(STRING_POOL);
        Collections.sort(BYTE_POOL);
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void addConstant(Object o) {
        if (o == null) return;
        Class<?> type = o.getClass();
        if ((type.equals(Byte.class)) || (type.equals(Byte.TYPE))) {
            byte b = (Byte) o;
            BYTE_POOL.add(b);
            BYTE_POOL.add((byte) (b + 1));
            BYTE_POOL.add((byte) (b - 1));
        } else if ((type.equals(Character.class)) || (type.equals(Character.TYPE))) {
            char c = (Character) o;
            CHAR_POOL.add(c);
            CHAR_POOL.add((char) (c + 1));
            CHAR_POOL.add((char) (c - 1));
        } else if ((type.equals(Double.class)) || (type.equals(Double.TYPE))) {
            double d = (Double) o;
            DOUBLE_POOL.add(d);
            DOUBLE_POOL.add(d + 1);
            DOUBLE_POOL.add(d - 1);
        } else if ((type.equals(Float.class)) || (type.equals(Float.TYPE))) {
            float f = (Float) o;
            FLOAT_POOL.add(f);
            FLOAT_POOL.add(f + 1);
            FLOAT_POOL.add(f - 1);
        } else if ((type.equals(Long.class)) || (type.equals(Long.TYPE))) {
            long l = (Long) o;
            LONG_POOL.add(l);
            LONG_POOL.add(l + 1);
            LONG_POOL.add(l - 1);
        } else if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
            int i = (Integer) o;
            INT_POOL.add(i);
            INT_POOL.add(i - 1);
            INT_POOL.add(i + 1);
            LONG_POOL.add(Long.valueOf(i));
            LONG_POOL.add(Long.valueOf(i + 1));
            LONG_POOL.add(Long.valueOf(i - 1));
            DOUBLE_POOL.add(Double.valueOf(i));
            DOUBLE_POOL.add(Double.valueOf(i + 1));
            DOUBLE_POOL.add(Double.valueOf(i - 1));
            if (i <= Short.MAX_VALUE) {
                short s = (short) i;
                SHORT_POOL.add(s);
                SHORT_POOL.add((short) (s + 1));
                SHORT_POOL.add((short) (s - 1));
            }
        } else if ((type.equals(Short.class)) || (type.equals(Short.TYPE))) {
            short s = (Short) o;
            SHORT_POOL.add(s);
            SHORT_POOL.add((short) (s + 1));
            SHORT_POOL.add((short) (s - 1));
            LONG_POOL.add(Long.valueOf(s));
            LONG_POOL.add(Long.valueOf(s + 1));
            LONG_POOL.add(Long.valueOf(s - 1));
            DOUBLE_POOL.add(Double.valueOf(s));
            DOUBLE_POOL.add(Double.valueOf(s + 1));
            DOUBLE_POOL.add(Double.valueOf(s - 1));
        } else if (type.equals(String.class)) {
            STRING_POOL.add((String) o);
        }
    }

    public int getSize(Type type) {
        if (type.getSort() == Type.BYTE) {
            return BYTE_POOL.size();
        }
        if (type.getSort() == Type.CHAR) {
            return CHAR_POOL.size();
        }
        if (type.getSort() == Type.DOUBLE) {
            return DOUBLE_POOL.size();
        }
        if (type.getSort() == Type.FLOAT) {
            return FLOAT_POOL.size();
        }
        if (type.getSort() == Type.LONG) {
            return LONG_POOL.size();
        }
        if (type.getSort() == Type.INT) {
            return INT_POOL.size();
        }
        if (type.getSort() == Type.SHORT) {
            return SHORT_POOL.size();
        }
        if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
            return STRING_POOL.size();
        }
        return -1;
    }

    public int getSize(Class<?> type) {
        if ((type.equals(Byte.class)) || (type.equals(Byte.TYPE))) {
            return BYTE_POOL.size();
        }
        if ((type.equals(Character.class)) || (type.equals(Character.TYPE))) {
            return CHAR_POOL.size();
        }
        if ((type.equals(Double.class)) || (type.equals(Double.TYPE))) {
            return DOUBLE_POOL.size();
        }
        if ((type.equals(Float.class)) || (type.equals(Float.TYPE))) {
            return FLOAT_POOL.size();
        }
        if ((type.equals(Long.class)) || (type.equals(Long.TYPE))) {
            return LONG_POOL.size();
        }
        if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
            return INT_POOL.size();
        }
        if ((type.equals(Short.class)) || (type.equals(Short.TYPE))) {
            return SHORT_POOL.size();
        }
        if (type.equals(String.class)) {
            return STRING_POOL.size();
        }
        return -1;
    }

    public Object getObject(Class<?> type, int index) {
        int size = getSize(type);
        if (size == -1 || index < 0 || index >= size) {
            return null;
        }
        if ((type.equals(Byte.class)) || (type.equals(Byte.TYPE))) {
            return BYTE_POOL.get(index);
        }
        if ((type.equals(Character.class)) || (type.equals(Character.TYPE))) {
            return CHAR_POOL.get(index);
        }
        if ((type.equals(Double.class)) || (type.equals(Double.TYPE))) {
            return DOUBLE_POOL.get(index);
        }
        if ((type.equals(Float.class)) || (type.equals(Float.TYPE))) {
            return FLOAT_POOL.get(index);
        }
        if ((type.equals(Long.class)) || (type.equals(Long.TYPE))) {
            return LONG_POOL.get(index);
        }
        if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
            return INT_POOL.get(index);
        }
        if ((type.equals(Short.class)) || (type.equals(Short.TYPE))) {
            return SHORT_POOL.get(index);
        }
        if (type.equals(String.class)) {
            return STRING_POOL.get(index);
        }
        return null;
    }

    public Object getObject(Type type, int index) {
        int size = getSize(type);
        if (size == -1 || index < 0 || index >= size) {
            return null;
        }
        if (type.getSort() == Type.BYTE) {
            return BYTE_POOL.get(index);
        }
        if (type.getSort() == Type.CHAR) {
            return CHAR_POOL.get(index);
        }
        if (type.getSort() == Type.DOUBLE) {
            return DOUBLE_POOL.get(index);
        }
        if (type.getSort() == Type.FLOAT) {
            return FLOAT_POOL.get(index);
        }
        if (type.getSort() == Type.LONG) {
            return LONG_POOL.get(index);
        }
        if (type.getSort() == Type.INT) {
            return INT_POOL.get(index);
        }
        if (type.getSort() == Type.SHORT) {
            return SHORT_POOL.get(index);
        }
        if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
            return STRING_POOL.get(index);
        }
        return null;
    }

    public String getClassName() {
        return className;
    }

}
