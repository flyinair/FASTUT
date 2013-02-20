package fastut.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import samples.RuleType;

import fastut.util.generics.type.ListSignaturedType;
import fastut.util.generics.type.MapSignaturedType;
import fastut.util.generics.type.SignaturedType;

public class FormatOut {

    public static final char   NEW_LINE              = '\n';
    public static final char   EQ_STR                = '=';
    public static final char   BLANK_STR             = ' ';
    public static final char   SEMI_STR              = ';';
    public static final char   LT_STR                = '<';
    public static final char   GT_STR                = '>';
    public static final char   LEFT_PARENTHESES_STR  = '(';
    public static final char   RIGHT_PARENTHESES_STR = ')';
    public static final char   DOT_STR               = '.';
    public static final char   COMMA_STR             = ',';
    public static final char   DOUBLE_QUOTES_STR     = '\"';
    public static final char   LONG_SUFFIX_STR       = 'L';
    public static final char   DOUBLE_SUFFIX_STR     = 'D';
    public static final String NULL_STR              = "null";
    public static final String NEW_STR               = "new";
    public static final String ARRAYLIST_TYPE_STR    = "ArrayList";
    public static final String HASHMAP_TYPE_STR      = "HashMap";
    public static final String ADD_OP                = "add";
    public static final String PUT_OP                = "put";

    static String asExpression(String typeName, String varName, String valueStr) {
        return new StringBuilder().append(asSimpleTypeName(typeName)).append(BLANK_STR).append(NameUtil.transform(varName)).append(BLANK_STR).append(EQ_STR).append(BLANK_STR).append(valueStr).append(SEMI_STR).append(NEW_LINE).toString();
    }

    static String asSimpleTypeName(String typeName) {
        return typeName.substring(typeName.lastIndexOf(".") + 1);
    }

    public static String asString(Object value) {
        if (null == value) {
            return NULL_STR;
        }
        StringBuilder builder = new StringBuilder();
        if (value instanceof String) {
            return builder.append(DOUBLE_QUOTES_STR).append(NameUtil.transform((String)value)).append(DOUBLE_QUOTES_STR).toString();
        }
        if (value instanceof Enum) {
            return builder.append(value.getClass().getSimpleName()).append(DOT_STR).append(value).toString();
        }
        builder.append(value);
        if (value instanceof Long) {
            return builder.append(LONG_SUFFIX_STR).toString();
        }
        if (value instanceof Double) {
            return builder.append(DOUBLE_SUFFIX_STR).toString();
        }
        return builder.toString();
    }

    public static String asDeclartion(SignaturedType st, String varName, Object rawValue) {
        if (st == null || varName == null) {
            return null;
        }
        if (st instanceof ListSignaturedType) {
            return asListDeclartion((ListSignaturedType) st, varName, rawValue);
        } else if (st instanceof MapSignaturedType) {
            return asMapDeclartion((MapSignaturedType) st, varName, rawValue);
        } else {
            return asBaseDeclartion(st, varName, rawValue);
        }
    }

    @SuppressWarnings("unchecked")
    static String asListDeclartion(ListSignaturedType st, String varName, Object rawValue) {
        Type argType = st.getArgType().getType();
        String argSimpleTypeName = asSimpleTypeName(argType.getClassName());
        String listTypeSimpleName = asSimpleTypeName(st.getType().getClassName());
        String typeName = new StringBuilder().append(listTypeSimpleName).append(LT_STR).append(argSimpleTypeName).append(GT_STR).toString();
        String value = new StringBuilder().append(NEW_STR).append(BLANK_STR).append(ARRAYLIST_TYPE_STR).append(LT_STR).append(argSimpleTypeName).append(GT_STR).append(LEFT_PARENTHESES_STR).append(RIGHT_PARENTHESES_STR).toString();
        StringBuilder resultBuilder = new StringBuilder(asExpression(typeName, varName, value));
        if (rawValue != null) {
            List<Object> list = (List<Object>) rawValue;
            for (Object v : list) {
                resultBuilder.append(asOperation(varName, ADD_OP, asString(v)));
            }
        }
        return resultBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    static String asMapDeclartion(MapSignaturedType st, String varName, Object rawValue) {
        Type kType = st.getKtype().getType();
        Type vType = st.getVtype().getType();
        String kSimpleTypeName = asSimpleTypeName(kType.getClassName());
        String vSimpleTypeName = asSimpleTypeName(vType.getClassName());
        String mapTypeSimpleName = asSimpleTypeName(st.getType().getClassName());
        String typeName = new StringBuilder().append(mapTypeSimpleName).append(LT_STR).append(kSimpleTypeName).append(COMMA_STR).append(BLANK_STR).append(vSimpleTypeName).append(GT_STR).toString();
        String value = (rawValue != null) ? new StringBuilder().append(NEW_STR).append(BLANK_STR).append(HASHMAP_TYPE_STR).append(LT_STR).append(kSimpleTypeName).append(COMMA_STR).append(BLANK_STR).append(vSimpleTypeName).append(GT_STR).append(LEFT_PARENTHESES_STR).append(RIGHT_PARENTHESES_STR).toString() : NULL_STR;
        StringBuilder resultBuilder = new StringBuilder(asExpression(typeName, varName, value));
        if (rawValue != null) {
            Map<Object, Object> map = (Map<Object, Object>) rawValue;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                resultBuilder.append(asOperation(varName, PUT_OP, asString(entry.getKey()), asString(entry.getValue())));
            }
        }
        return resultBuilder.toString();
    }

    static String asBaseDeclartion(SignaturedType st, String varName, Object rawValue) {
        String typeName = st.getType().getClassName();
        if (null == rawValue || rawValue instanceof String) {
            return asExpression(typeName, varName, asString(rawValue));
        }
        return NULL_STR;
    }

    public static String asOperation(String varName, String opName, String... params) {
        StringBuilder builder = new StringBuilder();
        builder.append(NameUtil.transform(varName)).append(DOT_STR).append(opName).append(LEFT_PARENTHESES_STR);
        if (params.length >= 1) {
            for (int i = 0; i < params.length - 1; ++i) {
                builder.append(params[i]).append(COMMA_STR).append(BLANK_STR);
            }
            builder.append(params[params.length - 1]);
        }
        builder.append(RIGHT_PARENTHESES_STR).append(SEMI_STR).append(NEW_LINE);
        return builder.toString();
    }

    public static String asNonStrictExpectations(String varName, String methodName, String methodDesc, Object expect) {
        StringBuilder builder = new StringBuilder();
        builder.append("new NonStrictExpectations() {\n")
               .append("    {\n")
               .append("\t").append(varName).append(".").append(methodName).append(FormatOut.asParamString(methodDesc)).append(";\n")
               .append("\treturns("+ asString(expect)+ ");\n")
               .append("    }\n")
               .append("};\n");
        return builder.toString();
    }

    public static String asTypeString(Type type) {
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
                builder.append('(').append(asSimpleTypeName(type.getClassName())).append(')').append("any");
                break;
        }
        return builder.toString();
    }

    public static String asParamString(String desc) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        Type[] types = Type.getArgumentTypes(desc);
        if (types.length >= 1) {
            for (int i = 0; i < types.length - 1; ++i) {
                builder.append(asTypeString(types[i])).append(", ");
            }
            builder.append(asTypeString(types[types.length - 1]));
        }
        builder.append(')');
        return builder.toString();
    }

    public static void main(String[] args) {
        ListSignaturedType lst = new ListSignaturedType(SignaturedType.makeSimpleType(Type.getType(List.class)),
                                                        SignaturedType.makeSimpleType(Type.getType(String.class)));
        List<String> ls = new ArrayList<String>();
        ls.add("hello world!");
        ls.add("happy new year!");
        ls.add(null);
        System.out.println(asDeclartion(lst, "list", ls));

        MapSignaturedType mst = new MapSignaturedType(SignaturedType.makeSimpleType(Type.getType(List.class)),
                                                      SignaturedType.makeSimpleType(Type.getType(Double.class)),
                                                      SignaturedType.makeSimpleType(Type.getType(String.class)));
        Map<Double, String> map = new HashMap<Double, String>();
        map.put(1D, "hello world!");
        map.put(2D, "happy new year!");
        map.put(3D, null);
        System.out.println(asDeclartion(mst, "map", map));

        System.out.println(asNonStrictExpectations("helper", "isGood", "(Ljava/lang/String;)Z", true));

        System.out.println(asString(RuleType.DL));
    }
}
