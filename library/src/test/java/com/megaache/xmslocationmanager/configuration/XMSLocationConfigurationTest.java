package com.megaache.xmslocationmanager.configuration;

import com.megaache.xmslocationmanager.providers.permissionprovider.DefaultPermissionProvider;
import com.megaache.xmslocationmanager.providers.permissionprovider.StubPermissionProvider;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class XMSLocationConfigurationTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void whenNoProviderConfigurationIsSetBuildShouldThrowException() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
              CoreMatchers.containsString("XMSConfiguration and DefaultProviderConfiguration"));

        new XMSLocationConfiguration.Builder().build();
    }

    @Test public void checkDefaultValues() {
        XMSLocationConfiguration configuration = getConfiguration();
        assertThat(configuration.keepTracking()).isFalse();
    }

    @Test public void whenNoPermissionConfigurationIsSetDefaultConfigurationShouldContainStubProvider() {
        XMSLocationConfiguration configuration = getConfiguration();

        assertThat(configuration.permissionConfiguration()).isNotNull();
        assertThat(configuration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
    }

    @Test public void clonesShouldShareSameInstances() {
        XMSLocationConfiguration configuration = getConfiguration();

        XMSLocationConfiguration firstClone = configuration.newBuilder().build();
        XMSLocationConfiguration secondClone = configuration.newBuilder().build();

        assertThat(firstClone.keepTracking())
              .isEqualTo(secondClone.keepTracking())
              .isFalse();
        assertThat(firstClone.permissionConfiguration())
              .isEqualTo(secondClone.permissionConfiguration())
              .isNotNull();
        assertThat(firstClone.defaultProviderConfiguration())
              .isEqualTo(secondClone.defaultProviderConfiguration())
              .isNotNull();
        assertThat(firstClone.xmsConfiguration())
              .isEqualTo(secondClone.xmsConfiguration())
              .isNotNull();
    }

    @Test public void clonedConfigurationIsIndependent() {
        XMSLocationConfiguration configuration = getConfiguration();
        XMSLocationConfiguration clone = configuration.newBuilder()
              .askForPermission(new PermissionConfiguration.Builder().build())
              .build();

        assertThat(configuration.permissionConfiguration())
              .isNotEqualTo(clone.permissionConfiguration());
        assertThat(configuration.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(StubPermissionProvider.class);
        assertThat(clone.permissionConfiguration().permissionProvider())
              .isNotNull()
              .isExactlyInstanceOf(DefaultPermissionProvider.class);
    }

    private XMSLocationConfiguration getConfiguration() {
        return new XMSLocationConfiguration.Builder()
              .useDefaultProviders(new DefaultProviderConfiguration.Builder().build())
              .useXMS(new XMSConfiguration.Builder().build())
              .build();
    }

}
