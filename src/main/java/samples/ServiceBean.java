package samples;

public class ServiceBean extends SuperSample {

    private long              intV;
    private String            stringV;
    private ServiceBeanHelper helper;
    private Saver             saver;
    protected int             protectedI;

    public boolean shouldHelpNow(long type) {
        if (type > intV) {
            return false;
        }
        if (this.protectedI > 0) {
            return true;
        }
        if (helper.isGood(stringV)) {
            return true;
        }
        if (intV > 0 && stringV != null && stringV.equals("pass")) {
            return true;
        }
        if (saver.getSaveType() == 5) {
            saver.save();
            return true;
        }
        return false;
    }
}
