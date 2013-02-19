package fastut.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.objectweb.asm.Type;

public class ObjectPool {

    protected static final List<Character> CHAR_POOL   = Arrays.asList(new Character[] { Character.valueOf('a'),
            Character.valueOf('b'), Character.valueOf('c'), Character.valueOf('d'), Character.valueOf('e'),
            Character.valueOf('f'), Character.valueOf('g'), Character.valueOf('h'), Character.valueOf('i'),
            Character.valueOf('j'), Character.valueOf('k'), Character.valueOf('l'), Character.valueOf('m'),
            Character.valueOf('n'), Character.valueOf('o'), Character.valueOf('p'), Character.valueOf('q'),
            Character.valueOf('r'), Character.valueOf('s'), Character.valueOf('t'), Character.valueOf('u'),
            Character.valueOf('v'), Character.valueOf('w'), Character.valueOf('x'), Character.valueOf('y'),
            Character.valueOf('z'), Character.valueOf('A'), Character.valueOf('B'), Character.valueOf('C'),
            Character.valueOf('D'), Character.valueOf('E'), Character.valueOf('F'), Character.valueOf('G'),
            Character.valueOf('H'), Character.valueOf('I'), Character.valueOf('J'), Character.valueOf('K'),
            Character.valueOf('L'), Character.valueOf('M'), Character.valueOf('N'), Character.valueOf('O'),
            Character.valueOf('P'), Character.valueOf('Q'), Character.valueOf('R'), Character.valueOf('S'),
            Character.valueOf('T'), Character.valueOf('U'), Character.valueOf('V'), Character.valueOf('W'),
            Character.valueOf('X'), Character.valueOf('Y'), Character.valueOf('Z'), Character.valueOf('0'),
            Character.valueOf('1'), Character.valueOf('2'), Character.valueOf('3'), Character.valueOf('4'),
            Character.valueOf('5'), Character.valueOf('6'), Character.valueOf('7'), Character.valueOf('8'),
            Character.valueOf('9'), Character.valueOf('`'), Character.valueOf('~'), Character.valueOf('!'),
            Character.valueOf('@'), Character.valueOf('#'), Character.valueOf('$'), Character.valueOf('%'),
            Character.valueOf('^'), Character.valueOf('&'), Character.valueOf('*'), Character.valueOf('('),
            Character.valueOf(')'), Character.valueOf('_'), Character.valueOf('+'), Character.valueOf('-'),
            Character.valueOf('='), Character.valueOf('{'), Character.valueOf('}'), Character.valueOf('['),
            Character.valueOf(']'), Character.valueOf('\\'), Character.valueOf('|'), Character.valueOf(';'),
            Character.valueOf(':'), Character.valueOf('\''), Character.valueOf('"'), Character.valueOf('<'),
            Character.valueOf('>'), Character.valueOf(','), Character.valueOf('.'), Character.valueOf('?'),
            Character.valueOf('/')                    });

    protected static final List<Integer>   INT_POOL    = new ArrayList<Integer>(Arrays.asList(new Integer[] {
            Integer.valueOf(-1), Integer.valueOf(0), Integer.valueOf(1) }));
    protected static final List<Double>    DOUBLE_POOL = new ArrayList<Double>(Arrays.asList(new Double[] {
            Double.valueOf(-1.0D), Double.valueOf(0.0D), Double.valueOf(1.0D) }));
    protected static final List<Float>     FLOAT_POOL  = new ArrayList<Float>(Arrays.asList(new Float[] {
            Float.valueOf(-1.0F), Float.valueOf(0.0F), Float.valueOf(1.0F) }));
    protected static final List<Long>      LONG_POOL   = new ArrayList<Long>(Arrays.asList(new Long[] {
            Long.valueOf(-1L), Long.valueOf(0L), Long.valueOf(1L) }));
    protected static final List<Short>     SHORT_POOL  = new ArrayList<Short>(Arrays.asList(new Short[] {
            Short.valueOf((short) -1), Short.valueOf((short) 0), Short.valueOf((short) 1) }));
    protected static final List<Byte>      BYTE_POOL   = new ArrayList<Byte>(Arrays.asList(new Byte[] {
            Byte.valueOf((byte) -1), Byte.valueOf((byte) 0), Byte.valueOf((byte) 1) }));
    protected static final List<String>    STRING_POOL = new ArrayList<String>(Arrays.asList(new String[] { "" }));

    private static final Random            random      = new Random(System.currentTimeMillis());
    public static final int                P_POOL      = 5;

    public static void initPool(Set<Object> values) {
        if (values != null) for (Iterator<Object> localIterator = values.iterator(); localIterator.hasNext();) {
            Object o = localIterator.next();
            if (o == null) continue;
            Class<?> type = o.getClass();
            if ((type.equals(Byte.class)) || (type.equals(Byte.TYPE))) {
                BYTE_POOL.add((Byte) o);
            } else if ((type.equals(Character.class)) || (type.equals(Character.TYPE))) {
                CHAR_POOL.add((Character) o);
            } else if ((type.equals(Double.class)) || (type.equals(Double.TYPE))) {
                DOUBLE_POOL.add((Double) o);
            } else if ((type.equals(Float.class)) || (type.equals(Float.TYPE))) {
                FLOAT_POOL.add((Float) o);
            } else if ((type.equals(Long.class)) || (type.equals(Long.TYPE))) {
                LONG_POOL.add((Long) o);
            } else if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
                INT_POOL.add((Integer) o);
            } else if ((type.equals(Short.class)) || (type.equals(Short.TYPE))) SHORT_POOL.add((Short) o);
            else if (type.equals(String.class)) STRING_POOL.add((String) o);
        }
    }

    public static Object getObject(Class<?> type) {
        if ((type.equals(Byte.class)) || (type.equals(Byte.TYPE))) {
            return BYTE_POOL.get(random.nextInt(BYTE_POOL.size()));
        }
        if ((type.equals(Character.class)) || (type.equals(Character.TYPE))) {
            return CHAR_POOL.get(random.nextInt(CHAR_POOL.size()));
        }
        if ((type.equals(Double.class)) || (type.equals(Double.TYPE))) {
            return DOUBLE_POOL.get(random.nextInt(DOUBLE_POOL.size()));
        }
        if ((type.equals(Float.class)) || (type.equals(Float.TYPE))) {
            return FLOAT_POOL.get(random.nextInt(FLOAT_POOL.size()));
        }
        if ((type.equals(Long.class)) || (type.equals(Long.TYPE))) {
            return LONG_POOL.get(random.nextInt(LONG_POOL.size()));
        }
        if ((type.equals(Integer.class)) || (type.equals(Integer.TYPE))) {
            return INT_POOL.get(random.nextInt(INT_POOL.size()));
        }
        if ((type.equals(Short.class)) || (type.equals(Short.TYPE))) {
            return SHORT_POOL.get(random.nextInt(SHORT_POOL.size()));
        }
        if ((type.equals(Boolean.class)) || (type.equals(Boolean.TYPE))) {
            return random.nextBoolean();
        }
        if (type.equals(String.class)) {
            return STRING_POOL.get(random.nextInt(STRING_POOL.size()));
        }
        return null;
    }

    public static Object getObject(Type type) {
        if (type.getSort() == Type.BYTE || type.getDescriptor().equals("Ljava/lang/Byte;")) {
            return BYTE_POOL.get(random.nextInt(BYTE_POOL.size()));
        }
        if (type.getSort() == Type.CHAR || type.getDescriptor().equals("Ljava/lang/Character;")) {
            return CHAR_POOL.get(random.nextInt(CHAR_POOL.size()));
        }
        if (type.getSort() == Type.DOUBLE || type.getDescriptor().equals("Ljava/lang/Double;")) {
            return DOUBLE_POOL.get(random.nextInt(DOUBLE_POOL.size()));
        }
        if (type.getSort() == Type.FLOAT || type.getDescriptor().equals("Ljava/lang/Float;")) {
            return FLOAT_POOL.get(random.nextInt(FLOAT_POOL.size()));
        }
        if (type.getSort() == Type.LONG || type.getDescriptor().equals("Ljava/lang/Long;")) {
            return LONG_POOL.get(random.nextInt(LONG_POOL.size()));
        }
        if (type.getSort() == Type.INT || type.getDescriptor().equals("Ljava/lang/Integer;")) {
            return INT_POOL.get(random.nextInt(INT_POOL.size()));
        }
        if (type.getSort() == Type.SHORT || type.getDescriptor().equals("Ljava/lang/Short;")) {
            return SHORT_POOL.get(random.nextInt(SHORT_POOL.size()));
        }
        if (type.getSort() == Type.OBJECT && type.getDescriptor().equals("Ljava/lang/String;")) {
            return STRING_POOL.get(random.nextInt(STRING_POOL.size()));
        }
        if (type.getSort() == Type.BOOLEAN || type.getDescriptor().equals("Ljava/lang/Boolean;")) {
            return random.nextBoolean();
        }
        return null;
    }

    public static String getObject(String type) {
        if ("byte".equals(type)) {
            return "((byte)" + BYTE_POOL.get(random.nextInt(BYTE_POOL.size())) + ")";
        }
        if ("char".equals(type)) {
            return "((char)" + CHAR_POOL.get(random.nextInt(CHAR_POOL.size())) + ")";
        }
        if ("double".equals(type)) {
            return "((double)" + DOUBLE_POOL.get(random.nextInt(DOUBLE_POOL.size())) + ")";
        }
        if ("float".equals(type)) {
            return "((float)" + FLOAT_POOL.get(random.nextInt(FLOAT_POOL.size())) + ")";
        }
        if ("long".equals(type)) {
            return "((long)" + LONG_POOL.get(random.nextInt(LONG_POOL.size())) + ")";
        }
        if ("int".equals(type)) {
            return "((int)" + INT_POOL.get(random.nextInt(INT_POOL.size())) + ")";
        }
        if ("short".equals(type)) {
            return "((short)" + SHORT_POOL.get(random.nextInt(SHORT_POOL.size())) + ")";
        }
        if ("boolean".equals(type)) {
            return "" + random.nextBoolean();
        }
        if ("string".equals(type) || "String".equals(type)) {
            return "\"" + STRING_POOL.get(random.nextInt(STRING_POOL.size())) + "\"";
        }
        return null;
    }
}
