package fastut.mock;

import java.util.HashMap;
import java.util.Map;

public class MockPool {

    static Map<Condition, Expect> mockers = new HashMap<Condition, Expect>();

    public static void setExpect(Condition conditon, Expect expect) {
        mockers.put(conditon, expect);
    }

    public static Expect getExpect(Condition condition) {
        if (mockers.containsKey(condition)) {
            return mockers.get(condition);
        }
        return null;
    }
}
