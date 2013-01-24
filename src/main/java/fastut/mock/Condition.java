package fastut.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Condition implements Serializable {

    private static final long serialVersionUID = 2503718265287491463L;
    private String            normalId;
    private List<Object>      paramValues;

    public Condition(String normalId, Object... values){
        this.normalId = normalId;
        this.paramValues = Arrays.asList(values);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((normalId == null) ? 0 : normalId.hashCode());
        result = prime * result + ((paramValues == null) ? 0 : paramValues.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Condition)) {
            return false;
        }
        Condition other = (Condition) obj;
        if (normalId == null) {
            if (other.normalId != null) {
                return false;
            }
        } else if (!normalId.equals(other.normalId)) {
            return false;
        }
        if (paramValues == null) {
            if (other.paramValues != null) {
                return false;
            }
        } else if (!paramValues.equals(other.paramValues)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Condition [normalId=" + normalId + ", paramValues=" + paramValues + "]";
    }

}
