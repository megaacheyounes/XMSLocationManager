package com.megaache.xmslocationmanager.configuration;

import androidx.annotation.NonNull;

import com.megaache.xmslocationmanager.listener.LocationListener;

public final class Configurations {

    /**
     * Pre-Defined Configurations
     */
    private Configurations() {
        // No instance
    }

    /**
     * Returns a LocationConfiguration that keeps tracking,
     * see also {@linkplain Configurations#silentConfiguration(boolean)}
     */
    public static XMSLocationConfiguration silentConfiguration() {
        return silentConfiguration(true);
    }

    /**
     * Returns a LocationConfiguration that will never ask user anything and will try to use whatever possible options
     * that application has to obtain location. If there is no sufficient permission, provider, etc... then
     * LocationManager will call {@linkplain LocationListener#onLocationFailed(int)} silently
     *
     * # Best use case of this configuration is within Service implementations
     */
    public static XMSLocationConfiguration silentConfiguration(boolean keepTracking) {
        return new XMSLocationConfiguration.Builder()
              .keepTracking(keepTracking)
              .useXMS(new XMSConfiguration.Builder().askForSettingsApi(false).build())
              .useDefaultProviders(new DefaultProviderConfiguration.Builder().build())
              .build();
    }

    /**
     * Returns a LocationConfiguration which tights to default definitions with given messages. Since this method is
     * basically created in order to be used in Activities, User needs to be asked for permission and enabling gps.
     */
    public static XMSLocationConfiguration defaultConfiguration(@NonNull String rationalMessage, @NonNull String gpsMessage) {
        return new XMSLocationConfiguration.Builder()
              .askForPermission(new PermissionConfiguration.Builder().rationaleMessage(rationalMessage).build())
              .useXMS(new XMSConfiguration.Builder().build())
              .useDefaultProviders(new DefaultProviderConfiguration.Builder().gpsMessage(gpsMessage).build())
              .build();
    }
}
