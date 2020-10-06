package com.megaache.xmslocationmanager.configuration;

import androidx.annotation.NonNull;

import com.megaache.xmslocationmanager.XMSLocationRequest;
import com.megaache.xmslocationmanager.providers.locationprovider.DefaultLocationProvider;
import com.megaache.xmslocationmanager.providers.locationprovider.XMSLocationProvider;

public class XMSConfiguration {

    private final XMSLocationRequest locationRequest;
    private final boolean fallbackToDefault;
    private final boolean askForXMS;
    private final boolean askForSettingsApi;
    private final boolean failOnSettingsApiSuspended;
    private final boolean ignoreLastKnowLocation;
    private final long XMSWaitPeriod;

    private XMSConfiguration(Builder builder) {
        this.locationRequest = builder.xmsLocationRequest;
        this.fallbackToDefault = builder.fallbackToDefault;
        this.askForXMS = builder.askForXMS;
        this.askForSettingsApi = builder.askForSettingsApi;
        this.failOnSettingsApiSuspended = builder.failOnSettingsApiSuspended;
        this.ignoreLastKnowLocation = builder.ignoreLastKnowLocation;
        this.XMSWaitPeriod = builder.XMSWaitPeriod;
    }

    public XMSConfiguration.Builder newBuilder() {
        return new XMSConfiguration.Builder()
              .xmsLocationRequest(locationRequest)
              .fallbackToDefault(fallbackToDefault)
              .askForXMS(askForXMS)
              .askForSettingsApi(askForSettingsApi)
              .failOnSettingsApiSuspended(failOnSettingsApiSuspended)
              .ignoreLastKnowLocation(ignoreLastKnowLocation)
              .setWaitPeriod(XMSWaitPeriod);
    }

    // region Getters
    public XMSLocationRequest locationRequest() {
        return locationRequest;
    }

    public boolean fallbackToDefault() {
        return fallbackToDefault;
    }

    public boolean askForXMS() {
        return askForXMS;
    }

    public boolean askForSettingsApi() {
        return askForSettingsApi;
    }

    public boolean failOnSettingsApiSuspended() {
        return failOnSettingsApiSuspended;
    }

    public boolean ignoreLastKnowLocation() {
        return ignoreLastKnowLocation;
    }

    public long xmsWaitPeriod() {
        return XMSWaitPeriod;
    }

    // endregion

    public static class Builder {

        private XMSLocationRequest xmsLocationRequest = Defaults.createDefaultLocationRequest();
        private boolean fallbackToDefault = Defaults.FALLBACK_TO_DEFAULT;
        private boolean askForXMS = Defaults.ASK_FOR_GP_SERVICES;
        private boolean askForSettingsApi = Defaults.ASK_FOR_SETTINGS_API;
        private boolean failOnSettingsApiSuspended = Defaults.FAIL_ON_SETTINGS_API_SUSPENDED;
        private boolean ignoreLastKnowLocation = Defaults.IGNORE_LAST_KNOW_LOCATION;
        private long XMSWaitPeriod = Defaults.WAIT_PERIOD;

        /**
         * XMSLocationRequest object that you specified to use while getting location from Google/Huawei Play Services whichever is available
         * Default is {@linkplain Defaults#createDefaultLocationRequest()}
         */
        public Builder xmsLocationRequest(@NonNull XMSLocationRequest xmsLocationRequestRequest) {
            this.xmsLocationRequest = xmsLocationRequestRequest;
            return this;
        }

        /**
         * In case of getting location from {@linkplain XMSLocationProvider} fails,
         * library will fallback to {@linkplain DefaultLocationProvider} as a default behaviour.
         * If you set this to false, then library will notify fail as soon as XMSLocationProvider fails.
         */
        public Builder fallbackToDefault(boolean fallbackToDefault) {
            this.fallbackToDefault = fallbackToDefault;
            return this;
        }

        /**
         * Set true to ask user handle when there is some resolvable error
         * on connection XMS, if you don't want to bother user
         * to configure Google Play Services to receive location then set this as false.
         *
         * Default is False.
         */
        public Builder askForXMS(boolean askForXMS) {
            this.askForXMS = askForXMS;
            return this;
        }

        /**
         * While trying to get location via XMS LocationApi,
         * manager will check whether GPS, Wifi and Cell networks are available or not.
         * Then if this flag is on it will ask user to turn them on, again, via XMS
         * by displaying a system dialog if not it will directly try to receive location
         * -which probably not going to return any values.
         *
         * Default is True.
         */
        public Builder askForSettingsApi(boolean askForSettingsApi) {
            this.askForSettingsApi = askForSettingsApi;
            return this;
        }

        /**
         * This flag will be checked when it is not possible to display user a settingsApi dialog
         * to switch necessary providers on, or when there is an error displaying the dialog.
         * If the flag is on, then manager will setDialogListener listener as location failed,
         * otherwise it will try to get location anyway -which probably not gonna happen.
         *
         * Default is False. -Because after XMS Provider it might switch
         * to default providers, if we fail here then those provider will never trigger.
         */
        public Builder failOnSettingsApiSuspended(boolean failOnSettingsApiSuspended) {
            this.failOnSettingsApiSuspended = failOnSettingsApiSuspended;
            return this;
        }

        /**
         * XMS Api returns the best most recent location currently available. It is highly recommended to
         * use this functionality unless your requirements are really specific and precise.
         *
         * Default is False. So XMS Api will return immediately if there is location already.
         */
        public Builder ignoreLastKnowLocation(boolean ignore) {
            this.ignoreLastKnowLocation = ignore;
            return this;
        }

        /**
         * Indicates waiting time period for XMS before switching to next possible provider.
         *
         * Default values are {@linkplain Defaults#WAIT_PERIOD}
         */
        public Builder setWaitPeriod(long milliseconds) {
            if (milliseconds < 0) {
                throw new IllegalArgumentException("waitPeriod cannot be set to negative value.");
            }

            this.XMSWaitPeriod = milliseconds;
            return this;
        }

        public XMSConfiguration build() {
            return new XMSConfiguration(this);
        }
    }
}
