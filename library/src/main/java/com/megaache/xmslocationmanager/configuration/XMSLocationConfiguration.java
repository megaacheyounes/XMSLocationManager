package com.megaache.xmslocationmanager.configuration;

import androidx.annotation.Nullable;

import com.megaache.xmslocationmanager.providers.permissionprovider.StubPermissionProvider;

public class XMSLocationConfiguration {

    private final boolean keepTracking;
    private final PermissionConfiguration permissionConfiguration;
    private final XMSConfiguration xmsConfiguration;
    private final DefaultProviderConfiguration defaultProviderConfiguration;

    private XMSLocationConfiguration(Builder builder) {
        this.keepTracking = builder.keepTracking;
        this.permissionConfiguration = builder.permissionConfiguration;
        this.xmsConfiguration = builder.xmsConfiguration;
        this.defaultProviderConfiguration = builder.defaultProviderConfiguration;
    }

    public XMSLocationConfiguration.Builder newBuilder() {
        return new XMSLocationConfiguration.Builder()
              .keepTracking(keepTracking)
              .askForPermission(permissionConfiguration)
              .useXMS(xmsConfiguration)
              .useDefaultProviders(defaultProviderConfiguration);
    }

    // region Getters
    public boolean keepTracking() {
        return keepTracking;
    }

    public PermissionConfiguration permissionConfiguration() {
        return permissionConfiguration;
    }

    @Nullable
    public XMSConfiguration xmsConfiguration() {
        return xmsConfiguration;
    }

    @Nullable public DefaultProviderConfiguration defaultProviderConfiguration() {
        return defaultProviderConfiguration;
    }
    // endregion

    public static class Builder {

        private boolean keepTracking = Defaults.KEEP_TRACKING;
        private PermissionConfiguration permissionConfiguration;
        private XMSConfiguration xmsConfiguration;
        private DefaultProviderConfiguration defaultProviderConfiguration;

        /**
         * If you need to keep receiving location updates, then you need to set this as true.
         * Otherwise manager will be aborted after any location received.
         * Default is False.
         */
        public Builder keepTracking(boolean keepTracking) {
            this.keepTracking = keepTracking;
            return this;
        }

        /**
         * This configuration is required in order to configure Permission Request process.
         * If this is not set, then no permission will be requested from user and
         * if {@linkplain Defaults#LOCATION_PERMISSIONS} permissions are not granted already,
         * then getting location will fail silently.
         */
        public Builder askForPermission(PermissionConfiguration permissionConfiguration) {
            this.permissionConfiguration = permissionConfiguration;
            return this;
        }

        /**
         * This configuration is required in order to configure XMS Api.
         * If this is not set, then XMS will not be used.
         */
        public Builder useXMS(XMSConfiguration xmsConfiguration) {
            this.xmsConfiguration = xmsConfiguration;
            return this;
        }

        /**
         * This configuration is required in order to configure Default Location Providers.
         * If this is not set, then they will not be used.
         */
        public Builder useDefaultProviders(DefaultProviderConfiguration defaultProviderConfiguration) {
            this.defaultProviderConfiguration = defaultProviderConfiguration;
            return this;
        }

        public XMSLocationConfiguration build() {
            if (xmsConfiguration == null && defaultProviderConfiguration == null) {
                throw new IllegalStateException("You need to specify one of the provider configurations."
                      + " Please see XMSConfiguration and DefaultProviderConfiguration");
            }

            if (permissionConfiguration == null) {
                permissionConfiguration = new PermissionConfiguration.Builder()
                      .permissionProvider(new StubPermissionProvider())
                      .build();
            }

            return new XMSLocationConfiguration(this);
        }

    }
}
