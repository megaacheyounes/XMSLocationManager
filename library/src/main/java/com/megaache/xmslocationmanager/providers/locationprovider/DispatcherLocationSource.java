package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

import androidx.annotation.Nullable;

import com.megaache.xmslocationmanager.helper.continuoustask.ContinuousTask;
import com.megaache.xmslocationmanager.helper.continuoustask.ContinuousTask.ContinuousTaskRunner;
import com.megaache.xmslocationmanager.listener.FallbackListener;

import org.xms.g.common.ExtensionApiAvailability;

class DispatcherLocationSource {

    static final String XMS_SWITCH_TASK = "XMSSwitchTask";

    private ContinuousTask gpServicesSwitchTask;

    DispatcherLocationSource(ContinuousTaskRunner continuousTaskRunner) {
        this.gpServicesSwitchTask = new ContinuousTask(XMS_SWITCH_TASK, continuousTaskRunner);
    }

    DefaultLocationProvider createDefaultLocationProvider() {
        return new DefaultLocationProvider();
    }

    XMSLocationProvider createXMSLocationProvider(FallbackListener fallbackListener) {
        return new XMSLocationProvider(fallbackListener);
    }

    ContinuousTask gpServicesSwitchTask() {
        return gpServicesSwitchTask;
    }

    int isXApiAvailable(Context context) {
        if (context == null) return -1;
        return ExtensionApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    boolean isXApiErrorUserResolvable(int gpServicesAvailability) {
        return ExtensionApiAvailability.getInstance().isUserResolvableError(gpServicesAvailability);
    }

    @Nullable
    Dialog getXApiErrorDialog(Activity activity, int gpServicesAvailability, int requestCode,
                              OnCancelListener onCancelListener) {
        if (activity == null) return null;
        return ExtensionApiAvailability.getInstance()
                .getErrorDialog(activity, gpServicesAvailability, requestCode, onCancelListener);
    }

}
