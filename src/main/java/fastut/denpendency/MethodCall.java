package fastut.denpendency;

import java.io.Serializable;

public class MethodCall implements Serializable {

    private static final long serialVersionUID = -3251773286012072525L;

    private final int         opcode;
    private final String      owner;
    private final String      name;
    private final String      desc;

    public MethodCall(int opcode, String owner, String name, String desc){
        this.opcode = opcode;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public int getOpcode() {
        return opcode;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + opcode;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
        if (!(obj instanceof MethodCall)) {
            return false;
        }
        MethodCall other = (MethodCall) obj;
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
        if (opcode != other.opcode) {
            return false;
        }
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MethodCall [opcode=" + opcode + ", owner=" + owner + ", name=" + name + ", desc=" + desc + "]";
    }

}
