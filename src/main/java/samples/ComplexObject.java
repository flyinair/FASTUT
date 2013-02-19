package samples;

import java.util.List;

public class ComplexObject<T> {

    private List<String> content;

    public String getContent(String prefix) {
        if (content == null || prefix == null) {
            return null;
        }
        for (String str : content) {
            if (str.startsWith(prefix)) {
                return str;
            }
        }
        return null;
    }
}
