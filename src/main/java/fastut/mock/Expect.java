package fastut.mock;

import java.io.Serializable;

public class Expect implements Serializable {

    private static final long serialVersionUID = -5074056079230609804L;
    private volatile boolean  mocked;
    private volatile Object   result;

    public Expect(Object result){
        this(true, result);
    }

    public Expect(boolean mocked, Object result){
        this.mocked = mocked;
        this.result = result;
    }

    public boolean isMocked() {
        return mocked;
    }

    public void setMocked(boolean mocked) {
        this.mocked = mocked;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
