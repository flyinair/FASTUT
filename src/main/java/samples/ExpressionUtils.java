package samples;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExpressionUtils {

    public static void main(String[] args) {
        Set<String> localVariables = new HashSet<String>();
        {
            localVariables.add("local1");
            localVariables.add("local2");
        }
        Set<String> fields = new HashSet<String>();
        {
            fields.add("field1");
            fields.add("field2");
        }
        Set<String> methods = new HashSet<String>();
        {
            methods.add("method1");
            methods.add("method2");
        }
        String expression = "method2() + method1(field1, field2, local1, local2, \"hello world\", 'jack') + method3();";
        String superTypeName = "hello";
        System.out.println(fixExpression(expression, localVariables, fields, methods, superTypeName));

    }

    private static int handleString(int start, char[] chars, StringBuffer out, char terminator) {
        out.append(chars[start]);
        start++;
        for (int i = start; i < chars.length; i++) {
            out.append(chars[i]);
            if (chars[i] == '\\') {
                if (i + 1 >= chars.length) {
                    break;
                }
                out.append(chars[(i + 1)]);
                i++;
            } else if (chars[i] == terminator) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("unterminated string literal");
    }

    private static int handleString(int start, char[] chars, StringBuffer out) {
        return handleString(start, chars, out, '"');
    }

    private static int handleChar(int start, char[] chars, StringBuffer out) {
        return handleString(start, chars, out, '\'');
    }

    private static int handleName(int start, char[] chars, StringBuffer out, Set<String> localVariables,
                                  Set<String> fields, Set<String> methods, String superTypeName, boolean replace) {
        List<String> names = new ArrayList<String>();
        StringBuffer name = new StringBuffer();

        boolean separatorFound = true;
        int i = start;
        for (; i < chars.length; i++) {
            if (Character.isJavaIdentifierStart(chars[i])) {
                if (!separatorFound) {
                    i--;
                    break;
                }
                i = handleIdenifyer(i, chars, name);
                names.add(name.toString());
                name = new StringBuffer();
                separatorFound = false;
            }
            if (i >= chars.length) {
                break;
            }
            if (Character.isWhitespace(chars[i])) {
                continue;
            }
            if (chars[i] != '.') break;
            if (i + 1 >= chars.length) {
                throw new IllegalArgumentException("invalid name");
            }
            separatorFound = true;
        }

        int endPosition = i;

        if (replace) {
            String firstName = (String) names.get(0);
            String secondName = names.size() > 1 ? (String) names.get(1) : null;

            if ("this".equals(firstName)) {
                names.set(0, "that");
            } else if (("super".equals(firstName)) && (secondName != null) && (fields.contains(secondName))) {
                names.set(0, "((" + superTypeName + ")that)");
            } else if ((!localVariables.contains(firstName))
                       && ((fields.contains(firstName)) || (methods.contains(firstName)))) {
                names.add(0, "that");
            } else if ((secondName != null) && ("this".equals(secondName))) {
                String thirdName = (String) names.get(2);
                if ((thirdName != null) && (fields.contains(thirdName))) {
                    names.set(1, "((" + firstName + ")that)");
                    names.remove(0);
                }
            }
        }

        for (Iterator<String> ii = names.iterator(); ii.hasNext();) {
            String s = (String) ii.next();
            out.append(s);
            if (ii.hasNext()) {
                out.append(".");
            }
        }

        return endPosition;
    }

    private static int handleIdenifyer(int start, char[] chars, StringBuffer out) {
        out.append(chars[start]);
        start++;
        for (int i = start; i < chars.length; i++) {
            if (!Character.isJavaIdentifierPart(chars[i])) {
                return i;
            }
            out.append(chars[i]);
        }
        return chars.length;
    }

    public static String fixExpression(String expression, Set<String> localVariables, Set<String> fields,
                                       Set<String> methods, String superTypeName) {
        if(localVariables == null || fields == null || methods == null) {
            return expression;
        }
        char[] chars = expression.toCharArray();
        StringBuffer out = new StringBuffer();

        boolean wasDot = false;
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '"':
                    i = handleString(i, chars, out);
                    wasDot = false;
                    break;
                case '\'':
                    i = handleChar(i, chars, out);
                    wasDot = false;
                    break;
                default:
                    if (!Character.isJavaIdentifierStart(chars[i])) break;
                    i = handleName(i, chars, out, localVariables, fields, methods, superTypeName, !wasDot);
                    wasDot = false;
            }

            if (i >= chars.length) {
                break;
            }
            if (chars[i] == '.') wasDot = true;
            else if (!Character.isWhitespace(chars[i])) {
                wasDot = false;
            }

            out.append(chars[i]);
        }

        return out.toString();
    }
}
