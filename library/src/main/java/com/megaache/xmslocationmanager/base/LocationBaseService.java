package com.megaache.xmslocationmanager.base;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;

import com.megaache.xmslocationmanager.XMSLocationManager;
import com.megaache.xmslocationmanager.configuration.XMSLocationConfiguration;
import com.megaache.xmslocationmanager.constants.ProcessType;
import com.megaache.xmslocationmanager.listener.LocationListener;

public abstract class LocationBaseService extends Service implements LocationListener {

    private XMSLocationManager locationManager;

    public abstract XMSLocationConfiguration getLocationConfiguration();

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = new XMSLocationManager.Builder(getApplicationContext())
              .configuration(getLocationConfiguration())
              .notify(this)
              .build();
        return super.onStartCommand(intent, flags, startId);
    }

    protected XMSLocationManager getLocationManager() {
        return locationManager;
    }

    protected void getLocation() {
        if (locationManager != null) {
            locationManager.get();
        } else {
            throw new IllegalStateException("locationManager is null. "
                  + "Make sure you call super.onStartCommand before attempting to getLocation");
        }
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        // override if needed
    }

    @Override
    public void onPermissionGranted(boolean alreadyHadPermission) {
        // override if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // override if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        // override if needed
    }

    @Override
    public void onProviderDisabled(String provider) {
        // override if needed
    }
}
