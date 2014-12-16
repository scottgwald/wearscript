package com.dappervision.wearscript_tagalong.dataproviders;

import com.dappervision.wearscript_tagalong.WearScript;
import com.dappervision.wearscript_tagalong.events.PebbleAccelerometerDataEvent;
import com.dappervision.wearscript_tagalong.managers.DataManager;

public class PebbleDataProvider extends DataProvider {
    private static final String TAG = "PebbleDataProvider";

    public PebbleDataProvider(DataManager dataManager, long samplePeriod, int type){
        super(dataManager, samplePeriod, type, "Pebble");
    }

    @Override
    public void unregister() {
        super.unregister();
    }

    public void onEvent(PebbleAccelerometerDataEvent e) {
        if(getType() == WearScript.SENSOR.PEBBLE_ACCELEROMETER.id()) {
            long timestamp = System.nanoTime();
            if(!useSample(timestamp))
                return;
            DataPoint dataPoint = new DataPoint(this, System.currentTimeMillis() / 1000., e.getTimestamp());
            byte[] accel = e.getAccel();
            dataPoint.addValue(Double.valueOf(accel[0]));
            dataPoint.addValue(Double.valueOf(accel[1]));
            dataPoint.addValue(Double.valueOf(accel[2]));
            parent.queue(dataPoint);
        }
    }

}
