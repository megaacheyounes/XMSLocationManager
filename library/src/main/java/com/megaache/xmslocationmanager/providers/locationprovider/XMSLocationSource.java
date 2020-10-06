package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xms.g.common.api.ResolvableApiException;
import org.xms.g.location.FusedLocationProviderClient;
import org.xms.g.location.LocationCallback;
import org.xms.g.location.LocationRequest;
import org.xms.g.location.LocationResult;
import org.xms.g.location.LocationServices;
import org.xms.g.location.LocationSettingsRequest;
import org.xms.g.location.LocationSettingsResponse;
import org.xms.g.tasks.OnFailureListener;
import org.xms.g.tasks.OnSuccessListener;
import org.xms.g.tasks.Task;

import com.megaache.xmslocationmanager.XMSLocationRequest;
import com.megaache.xmslocationmanager.constants.RequestCode;

class XMSLocationSource extends LocationCallback {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final XMSLocationRequest xmsLocationRequest;
    private final SourceListener sourceListener;
    private Context context;
    interface SourceListener extends OnSuccessListener<LocationSettingsResponse>, OnFailureListener {
        void onConnected();

        void onSuccess(LocationSettingsResponse locationSettingsResponse);

        void onFailure(@NonNull Exception exception);

        void onLocationResult(@Nullable LocationResult locationResult);
    }

    XMSLocationSource(Context context, XMSLocationRequest xmsLocationRequest, SourceListener sourceListener) {
        this.context = context;
        this.sourceListener = sourceListener;
        this.xmsLocationRequest = xmsLocationRequest;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    void checkLocationSettings() {
        LocationServices.getSettingsClient(context)
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(xmsLocationRequest.getRequest())
                                .build()
                )
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (sourceListener != null) sourceListener.onSuccess(locationSettingsResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (sourceListener != null) sourceListener.onFailure(exception);
                    }
                });
    }

    void startSettingsApiResolutionForResult(@NonNull ResolvableApiException resolvable, Activity activity) throws SendIntentException {
        resolvable.startResolutionForResult(activity, RequestCode.SETTINGS_API);
    }

    @SuppressWarnings("ResourceType")
    void requestLocationUpdate() {
        // This method is suited for the foreground use cases
        fusedLocationProviderClient.requestLocationUpdates(xmsLocationRequest.getRequest(), this, Looper.myLooper());
    }

    @NonNull
    Task<Void> removeLocationUpdates() {
        return fusedLocationProviderClient.removeLocationUpdates(this);
    }

    @SuppressWarnings("ResourceType")
    @NonNull
    Task<Location> getLastLocation() {
        return fusedLocationProviderClient.getLastLocation();
    }

    @Override
    public void onLocationResult(@Nullable LocationResult locationResult) {
        if (sourceListener != null) sourceListener.onLocationResult(locationResult);
    }

}
