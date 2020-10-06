package com.megaache.xmslocationmanager.configuration;

import com.megaache.xmslocationmanager.providers.dialogprovider.SimpleMessageDialogProvider;
import com.megaache.xmslocationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.megaache.xmslocationmanager.providers.permissionprovider.StubPermissionProvider;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationsTest {

    @Test public void silentConfigurationWithoutParameterShouldKeepTracking() {
        assertThat(Configurations.silentConfiguration().keepTracking()).isTrue();
    }

    @Test public void silentConfigurationCheckDefaultValues() {
        XMSLocationConfiguration silentConfiguration = Configurations.silentConfiguration(false);

        assertThat(silentConfiguration.keepTracking()).isFalse();
        assertThat(silentConfiguration.permissionConfiguration()).isNotNull();
        assertThat(silentConfiguration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
        assertThat(silentConfiguration.xmsConfiguration()).isNotNull();
        assertThat(silentConfiguration.xmsConfiguration().askForSettingsApi()).isFalse();
        assertThat(silentConfiguration.defaultProviderConfiguration()).isNotNull();
    }

    @Test public void defaultConfigurationCheckDefaultValues() {
        XMSLocationConfiguration defaultConfiguration = Configurations.defaultConfiguration("rationale", "gps");

        assertThat(defaultConfiguration.keepTracking()).isFalse();
        assertThat(defaultConfiguration.permissionConfiguration()).isNotNull();
        assertThat(defaultConfiguration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(DefaultPermissionProvider.class);
        assertThat(defaultConfiguration.permissionConfiguration().permissionProvider().getDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) defaultConfiguration.permissionConfiguration()
                    .permissionProvider().getDialogProvider()).message()).isEqualTo("rationale");
        assertThat(defaultConfiguration.xmsConfiguration()).isNotNull();
        assertThat(defaultConfiguration.defaultProviderConfiguration()).isNotNull();
        assertThat(defaultConfiguration.defaultProviderConfiguration().askForEnableGPS()).isTrue();
        assertThat(defaultConfiguration.defaultProviderConfiguration().gpsDialogProvider())
              .isNotNull()
              .isExactlyInstanceOf(SimpleMessageDialogProvider.class);
        assertThat(((SimpleMessageDialogProvider) defaultConfiguration.defaultProviderConfiguration()
              .gpsDialogProvider()).message()).isEqualTo("gps");

    }
}