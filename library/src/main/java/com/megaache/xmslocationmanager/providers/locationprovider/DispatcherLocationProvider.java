package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.RequestCode;
import com.megaache.xmslocationmanager.helper.LogUtils;
import com.megaache.xmslocationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;
import com.megaache.xmslocationmanager.listener.FallbackListener;

import org.xms.g.common.ConnectionResult;

public class DispatcherLocationProvider extends LocationProvider implements ContinuousTaskRunner, FallbackListener {

    private Dialog gpServicesDialog;
    private LocationProvider activeProvider;
    private DispatcherLocationSource dispatcherLocationSource;

    @Override
    public void onPause() {
        super.onPause();

        if (activeProvider != null) {
            activeProvider.onPause();
        }

        getSourceProvider().gpServicesSwitchTask().pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (activeProvider != null) {
            activeProvider.onResume();
        }

        getSourceProvider().gpServicesSwitchTask().resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activeProvider != null) {
            activeProvider.onDestroy();
        }

        getSourceProvider().gpServicesSwitchTask().stop();

        dispatcherLocationSource = null;
        gpServicesDialog = null;
    }

    @Override
    public void cancel() {
        if (activeProvider != null) {
            activeProvider.cancel();
        }

        getSourceProvider().gpServicesSwitchTask().stop();
    }

    @Override
    public boolean isWaiting() {
        return activeProvider != null && activeProvider.isWaiting();
    }

    @Override
    public boolean isDialogShowing() {
        boolean gpServicesDialogShown = gpServicesDialog != null && gpServicesDialog.isShowing();
        boolean anyProviderDialogShown = activeProvider != null && activeProvider.isDialogShowing();
        return gpServicesDialogShown || anyProviderDialogShown;
    }

    @Override
    public void runScheduledTask(@NonNull String taskId) {
        if (taskId.equals(DispatcherLocationSource.XMS_SWITCH_TASK)) {
            if (activeProvider instanceof XMSLocationProvider && activeProvider.isWaiting()) {
                LogUtils.logI("We couldn't receive location from XMS, so switching default providers...");
                cancel();
                continueWithDefaultProviders();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.XMS) {
            // Check whether do we have gpServices now or still not!
            checkXMSAvailability(false);
        } else {
            if (activeProvider != null) {
                activeProvider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void get() {
        if (getConfiguration().xmsConfiguration() != null) {
            checkXMSAvailability(true);
        } else {
            LogUtils.logI("Configuration requires not to use XMS Play Services, "
                    + "so skipping that step to Default Location Providers");
            continueWithDefaultProviders();
        }
    }

    @Override
    public void onFallback() {
        // This is called from XMSLocationProvider when it fails to before its scheduled time
        cancel();
        continueWithDefaultProviders();
    }

    void checkXMSAvailability(boolean askForXMS) {
        int gpServicesAvailability = getSourceProvider().isXApiAvailable(getContext());

if (gpServicesAvailability == ConnectionResult.getSUCCESS()) {
            LogUtils.logI("XMS is available on device.");
            getLocationFromXMS();
        } else {
            LogUtils.logI("XMS is NOT available on device.");
            if (askForXMS) {
                askForXMS(gpServicesAvailability);
            } else {
                LogUtils.logI("XMS is NOT available and even though we ask user to handle error, "
                        + "it is still NOT available.");

                // This means get method is called by onActivityResult
                // which we already ask user to handle with gpServices error
                continueWithDefaultProviders();
            }
        }
    }

    void askForXMS(int gpServicesAvailability) {
        if (getConfiguration().xmsConfiguration().askForXMS() &&
                getSourceProvider().isXApiErrorUserResolvable(gpServicesAvailability)) {

            resolveXMS(gpServicesAvailability);
        } else {
            LogUtils.logI("Either XMS error is not resolvable "
                    + "or the configuration doesn't wants us to bother user.");
            continueWithDefaultProviders();
        }
    }

    /**
     * Handle XMS error. Try showing a dialog that maybe can fix the error by user action.
     * If error cannot be resolved or user cancelled dialog or dialog cannot be displayed, then {@link #continueWithDefaultProviders()} is called.
     * <p>
     *   returns one of following in {@link ConnectionResult}:
     * SUCCESS, SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID.
     *
     */
    void resolveXMS(int gpServicesAvailability) {
        LogUtils.logI("Asking user to handle XMS error...");
        gpServicesDialog = getSourceProvider().getXApiErrorDialog(getActivity(), gpServicesAvailability,
                RequestCode.XMS, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        LogUtils.logI("XMS error could've been resolved, "
                                + "but user canceled it.");
                        continueWithDefaultProviders();
                    }
                });

        if (gpServicesDialog != null) {

            /*
            The SERVICE_INVALID, SERVICE_UPDATING errors cannot be resolved via user action.
            In these cases, when user closes dialog by clicking OK button, OnCancelListener is not called.
            So, to handle these errors, we attach a dismiss event listener that calls continueWithDefaultProviders(), when dialog is closed.
             */
//            switch (//DispatcherLocationProviderINT.translateValue(gpServicesAvailability)) {
//                // The version of the Google Play services installed on this device is not authentic.
//                case //DISPATCHERLOCATIONPROVIDERINT_CONNECTIONRESULT_SERVICE_INVALID:
//                // Google Play service is currently being updated on this device.
//                case //DISPATCHERLOCATIONPROVIDERINT_CONNECTIONRESULT_SERVICE_UPDATING:
//                    gpServicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            LogUtils.logI("XMS error could not have been resolved");
//                            continueWithDefaultProviders();
//                        }
//                    });
//
//                    break;
//            }
            // The version of the Google Play services installed on this device is not authentic.
            if (gpServicesAvailability == ConnectionResult.getSERVICE_INVALID() ||
                    gpServicesAvailability == ConnectionResult.getSERVICE_UPDATING()) {// Google Play service is currently being updated on this device.

                gpServicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        LogUtils.logI("XMS error could not have been resolved");
                        continueWithDefaultProviders();
                    }
                });

            }

            gpServicesDialog.show();
        } else {
            LogUtils.logI("XMS error could've been resolved, but since LocationManager "
                    + "is not running on an Activity, dialog cannot be displayed.");
            continueWithDefaultProviders();
        }
    }

    void getLocationFromXMS() {
        LogUtils.logI("Attempting to get location from Google/Huawei Play Services providers...");
        setLocationProvider(getSourceProvider().createXMSLocationProvider(this));
        getSourceProvider().gpServicesSwitchTask().delayed(getConfiguration()
                .xmsConfiguration().xmsWaitPeriod());
        activeProvider.get();
    }

    /**
     * Called in case of Google Play Services failed to retrieve location,
     * or XMSConfiguration doesn't provided by developer
     */
    void continueWithDefaultProviders() {
        if (getConfiguration().defaultProviderConfiguration() == null) {
            LogUtils.logI("Configuration requires not to use default providers, abort!");
            if (getListener() != null) {
                getListener().onLocationFailed(FailType.XMS_NOT_AVAILABLE);
            }
        } else {
            LogUtils.logI("Attempting to get location from default providers...");
            setLocationProvider(getSourceProvider().createDefaultLocationProvider());
            activeProvider.get();
        }
    }

    void setLocationProvider(LocationProvider provider) {
        this.activeProvider = provider;
        activeProvider.configure(this);
    }

    // For test purposes
    void setDispatcherLocationSource(DispatcherLocationSource dispatcherLocationSource) {
        this.dispatcherLocationSource = dispatcherLocationSource;
    }

    private DispatcherLocationSource getSourceProvider() {
        if (dispatcherLocationSource == null) {
            dispatcherLocationSource = new DispatcherLocationSource(this);
        }
        return dispatcherLocationSource;
    }
}
