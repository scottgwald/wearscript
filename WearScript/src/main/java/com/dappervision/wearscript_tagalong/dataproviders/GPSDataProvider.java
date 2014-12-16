package com.dappervision.wearscript_tagalong.dataproviders;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.dappervision.wearscript_tagalong.managers.DataManager;


public class GPSDataProvider extends DataProvider {
    private final LocationListener locationListener;
    private LocationManager locationManager;

    public GPSDataProvider(final DataManager parent, long samplePeriod, int type) {
        super(parent, samplePeriod, type, "GPS");
        locationManager = (LocationManager) parent.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location l) {
                long timestamp = System.nanoTime();
                DataPoint dataPoint = new DataPoint(GPSDataProvider.this, System.currentTimeMillis() / 1000., timestamp);
                dataPoint.addValue(Double.valueOf(l.getLatitude()));
                dataPoint.addValue(Double.valueOf(l.getLongitude()));
                dataPoint.addValue(Double.valueOf(l.getSpeed()));
                dataPoint.addValue(Double.valueOf(l.getBearing()));
                parent.queue(dataPoint);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        for (String provider : locationManager.getProviders(true))
            locationManager.requestLocationUpdates(provider, samplePeriod * 1000, 0, locationListener);
    }

    @Override
    public void unregister() {
        super.unregister();
        locationManager.removeUpdates(locationListener);
    }
}
