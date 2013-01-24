package fastut.coverage.data;

import java.util.TimerTask;

public class SaveTimer extends TimerTask implements HasBeenInstrumented
{

    public void run()
    {
        //ProjectData.saveGlobalProjectData();
    }

}
