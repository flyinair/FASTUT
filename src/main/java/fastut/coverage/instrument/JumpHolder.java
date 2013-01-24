package fastut.coverage.instrument;

public class JumpHolder {

    protected int lineNumber;

    protected int jumpNumber;

    public JumpHolder(int lineNumber, int jumpNumber){
        super();
        this.lineNumber = lineNumber;
        this.jumpNumber = jumpNumber;
    }

    public int getJumpNumber() {
        return jumpNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

}
