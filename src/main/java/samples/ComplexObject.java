package samples;

import java.util.List;
import java.util.Map;

public class ComplexObject<T> {

    private List<String>         content;
    private Map<String, Integer> indexs;

    public String getContent(String prefix) {
        if (content == null || prefix == null) {
            return null;
        }
        if (indexs == null) {
            return null;
        }
        if (indexs.containsKey(prefix)) {
            return prefix + "____" + indexs.get(prefix);
        }
        for (String str : content) {
            if (str.startsWith(prefix)) {
                return str;
            }
        }
        return null;
    }
}
