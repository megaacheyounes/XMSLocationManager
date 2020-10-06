package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.ProcessType;
import com.megaache.xmslocationmanager.constants.RequestCode;
import com.megaache.xmslocationmanager.helper.LogUtils;
import com.megaache.xmslocationmanager.listener.FallbackListener;
import com.megaache.xmslocationmanager.providers.locationprovider.XMSLocationSource.SourceListener;

import org.xms.g.common.api.ApiException;
import org.xms.g.common.api.CommonStatusCodes;
import org.xms.g.common.api.ResolvableApiException;
import org.xms.g.location.LocationResult;
import org.xms.g.location.LocationSettingsResponse;
import org.xms.g.location.LocationSettingsStatusCodes;
import org.xms.g.tasks.OnCompleteListener;
import org.xms.g.tasks.Task;

import java.lang.ref.WeakReference;

public class XMSLocationProvider extends LocationProvider implements SourceListener {

    private final WeakReference<FallbackListener> fallbackListener;

    private boolean settingsDialogIsOn = false;

    private XMSLocationSource xmsLocationSource;

    XMSLocationProvider(FallbackListener fallbackListener) {
        this.fallbackListener = new WeakReference<>(fallbackListener);
    }

    @Override
    public void onResume() {
        // not getSourceProvider, because we don't want to connect if it is not already attempt
        if (!settingsDialogIsOn && xmsLocationSource != null &&
                (isWaiting() || getConfiguration().keepTracking())) {
            onConnected();
        }
    }

    @Override
    public void onPause() {
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (!settingsDialogIsOn && xmsLocationSource != null) {
            removeLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (xmsLocationSource != null) removeLocationUpdates();
    }

    @Override
    public boolean isDialogShowing() {
        return settingsDialogIsOn;
    }

    @Override
    public void get() {
        setWaiting(true);

        if (getContext() != null) {
            onConnected();
        } else {
            failed(FailType.VIEW_DETACHED);
        }
    }

    @Override
    public void cancel() {
        LogUtils.logI("Canceling GooglePlayServiceLocationProvider...");
        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (xmsLocationSource != null) {
            removeLocationUpdates();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.SETTINGS_API) {
            settingsDialogIsOn = false;

            if (resultCode == Activity.RESULT_OK) {
                LogUtils.logI("We got settings changed, requesting location update...");
                requestLocationUpdate();
            } else {
                LogUtils.logI("User denied settingsApi dialog, XMS SettingsApi failing...");
                settingsApiFail(FailType.XMS_SETTINGS_DENIED);
            }
        }

    }

    @Override
    public void onConnected() {
        LogUtils.logI("Start request location updates.");

        if (getConfiguration().xmsConfiguration().ignoreLastKnowLocation()) {
            LogUtils.logI("Configuration requires to ignore last know location from XMS Api.");

            // Request fresh location
            requestLocation(false);
        } else {
            // Try to get last location, if failed then request fresh location
            checkLastKnowLocation();
        }
    }

    public void onLocationChanged(@NonNull Location location) {
        if (getListener() != null) {
            getListener().onLocationChanged(location);
        }

        // Set waiting as false because we got at least one, even though we keep tracking user's location
        setWaiting(false);

        if (!getConfiguration().keepTracking()) {
            // If need to update location once, clear the listener to prevent multiple call
            LogUtils.logI("We got location and no need to keep tracking, so location update is removed.");

            removeLocationUpdates();
        }
    }

    @Override
    public void onLocationResult(@Nullable LocationResult locationResult) {
        if (locationResult == null) {
            // Do nothing, wait for other result
            return;
        }

        for (Location location : locationResult.getLocations()) {
            onLocationChanged(location);
        }
    }

    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        // All location settings are satisfied. The client can initialize location
        // requests here.
        LogUtils.logI("We got GPS, Wifi and/or Cell network providers enabled enough "
                + "to receive location as we needed. Requesting location update...");
        requestLocationUpdate();
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        int statusCode = (ApiException.dynamicCast(exception)).getStatusCode();

        if (statusCode == LocationSettingsStatusCodes.getSETTINGS_CHANGE_UNAVAILABLE()) {// Location settings are not satisfied.
            // However, we have no way to fix the settings so we won't show the dialog.
            LogUtils.logE("Settings change is not available, SettingsApi failing...");
            settingsApiFail(FailType.XMS_SETTINGS_DIALOG);
        } else if (statusCode == LocationSettingsStatusCodes.getRESOLUTION_REQUIRED()) {// Location settings are not satisfied. But could be fixed by showing the user
            // a dialog.
            // Cast to a resolvable exception.
            resolveSettingsApi(ResolvableApiException.dynamicCast(exception));
        } else {// for other CommonStatusCodes values
            LogUtils.logE("LocationSettings failing, status: " + CommonStatusCodes.getStatusCodeString(statusCode));
            settingsApiFail(FailType.XMS_SETTINGS_DENIED);
        }
    }

    void resolveSettingsApi(@NonNull ResolvableApiException resolvable) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            LogUtils.logI("We need settingsApi dialog to switch required settings on.");
            if (getActivity() != null) {
                LogUtils.logI("Displaying the dialog...");
                getSourceProvider().startSettingsApiResolutionForResult(resolvable, getActivity());
                settingsDialogIsOn = true;
            } else {
                LogUtils.logI("Settings Api cannot show dialog if LocationManager is not running on an activity!");
                settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
            }
        } catch (IntentSender.SendIntentException e) {
            LogUtils.logE("Error on displaying SettingsApi dialog, SettingsApi failing...");
            settingsApiFail(FailType.XMS_SETTINGS_DIALOG);
        }
    }

    void checkLastKnowLocation() {
        getSourceProvider().getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        /*
                         * Returns the best most recent location currently available.
                         *
                         * If a location is not available, which should happen very rarely, null will be returned.
                         * The best accuracy available while respecting the location permissions will be returned.
                         *
                         * This method provides a simplified way to get location.
                         * It is particularly well suited for applications that do not require an accurate location and that do not want to maintain extra logic for location updates.
                         *
                         * GPS location can be null if GPS is switched off
                         */
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location lastKnownLocation = task.getResult();

                            LogUtils.logI("LastKnowLocation is available.");
                            onLocationChanged(lastKnownLocation);

                            requestLocation(true);
                        } else {
                            LogUtils.logI("LastKnowLocation is not available.");

                            requestLocation(false);
                        }
                    }
                });
    }

    void requestLocation(boolean locationIsAlreadyAvailable) {
        if (getConfiguration().keepTracking() || !locationIsAlreadyAvailable) {
            locationRequired();
        } else {
            LogUtils.logI("We got location, no need to ask for location updates.");
        }
    }

    void locationRequired() {
        LogUtils.logI("Ask for location update...");
        if (getConfiguration().xmsConfiguration().askForSettingsApi()) {
            LogUtils.logI("Asking for SettingsApi...");
            getSourceProvider().checkLocationSettings();
        } else {
            LogUtils.logI("SettingsApi is not enabled, requesting for location update...");
            requestLocationUpdate();
        }
    }

    void requestLocationUpdate() {
        if (getListener() != null) {
            getListener().onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_XMS);
        }

        LogUtils.logI("Requesting location update...");
        getSourceProvider().requestLocationUpdate();
    }

    void settingsApiFail(@FailType int failType) {
        if (getConfiguration().xmsConfiguration().failOnSettingsApiSuspended()) {
            failed(failType);
        } else {
            LogUtils.logE("Even though settingsApi failed, configuration requires moving on. "
                    + "So requesting location update...");

            requestLocationUpdate();
        }
    }

    void failed(@FailType int type) {
        if (getConfiguration().xmsConfiguration().fallbackToDefault() && fallbackListener.get() != null) {
            fallbackListener.get().onFallback();
        } else {
            if (getListener() != null) {
                getListener().onLocationFailed(type);
            }
        }
        setWaiting(false);
    }

    // For test purposes
    void setDispatcherLocationSource(XMSLocationSource xmsLocationSource) {
        this.xmsLocationSource = xmsLocationSource;
    }

    private XMSLocationSource getSourceProvider() {
        if (xmsLocationSource == null) {
            xmsLocationSource = new XMSLocationSource(getContext(),
                    getConfiguration()  .xmsConfiguration().locationRequest(), this);
        }
        return xmsLocationSource;
    }

    private void removeLocationUpdates() {
        LogUtils.logI("Stop location updates...");

        // not getSourceProvider, because we don't want to create if it doesn't already exist
        if (xmsLocationSource != null) {
            setWaiting(false);
            xmsLocationSource.removeLocationUpdates();
        }
    }

}