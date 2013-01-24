package fastut.denpendency;

import org.objectweb.asm.tree.FieldNode;

public class FastUTFieldNode extends FieldNode {

    private boolean  mockable;
    private Class<?> mockedClass;
    private String   declaredClassName;

    public FastUTFieldNode(int access, String name, String desc, String signature, Object value,
                           String declaredClassName){
        super(access, name, desc, signature, value);
        this.declaredClassName = declaredClassName;
    }

    public String getDeclaredClassName() {
        return declaredClassName;
    }

    public boolean isMockable() {
        return mockable;
    }

    public void setMockable(boolean mockable) {
        this.mockable = mockable;
    }

    public Class<?> getMockedClass() {
        return mockedClass;
    }

    public void setMockedClass(Class<?> mockedClass) {
        this.mockedClass = mockedClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + access;
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((signature == null) ? 0 : signature.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        if (!(obj instanceof FastUTFieldNode)) {
            return false;
        }
        FastUTFieldNode other = (FastUTFieldNode) obj;
        if (access != other.access) {
            return false;
        }
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (signature == null) {
            if (other.signature != null) {
                return false;
            }
        } else if (!signature.equals(other.signature)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FastUTFieldNode [access=" + access + ", name=" + name + ", desc=" + desc + ", signature=" + signature
               + ", value=" + value + "]";
    }

}
