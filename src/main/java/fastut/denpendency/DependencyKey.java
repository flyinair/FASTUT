package fastut.denpendency;

import java.io.Serializable;

public class DependencyKey implements Serializable {

    private static final long serialVersionUID = 2796439011548905366L;
    private final String      className;
    private final String      methodName;
    private final String      methodDesc;

    public DependencyKey(String className, String methodName, String methodDesc){
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public String toString() {
        return className + "." + methodName + methodDesc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodDesc == null) ? 0 : methodDesc.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
        if (!(obj instanceof DependencyKey)) {
            return false;
        }
        DependencyKey other = (DependencyKey) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (methodDesc == null) {
            if (other.methodDesc != null) {
                return false;
            }
        } else if (!methodDesc.equals(other.methodDesc)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }

}
