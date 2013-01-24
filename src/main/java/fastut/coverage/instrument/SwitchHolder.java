package fastut.coverage.instrument;

public class SwitchHolder extends JumpHolder {

    protected int branch;

    public SwitchHolder(int lineNumber, int switchNumber, int branch){
        super(lineNumber, switchNumber);
        this.branch = branch;
    }

    public int getSwitchNumber() {
        return jumpNumber;
    }

    public int getBranch() {
        return branch;
    }

}
