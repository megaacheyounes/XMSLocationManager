package com.megaache.xmslocationmanager.configuration;

import org.xms.g.location.LocationRequest;
import com.megaache.xmslocationmanager.configuration.XMSConfiguration.Builder;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class XMSConfigurationTest {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void checkDefaultValues() {
        XMSConfiguration configuration = new XMSConfiguration.Builder().build();
        assertThat(configuration.locationRequest()).isEqualTo(createDefaultLocationRequest());
        assertThat(configuration.fallbackToDefault()).isTrue();
        assertThat(configuration.askForXMS()).isFalse();
        assertThat(configuration.askForSettingsApi()).isTrue();
        assertThat(configuration.failOnSettingsApiSuspended()).isFalse();
        assertThat(configuration.ignoreLastKnowLocation()).isFalse();
        assertThat(configuration.xmsWaitPeriod()).isEqualTo(20 * SECOND);
    }

    @Test public void setWaitPeriodShouldThrowExceptionWhenXmsWaitPeriodIsSet() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CoreMatchers.startsWith("waitPeriod"));

        new XMSConfiguration.Builder().setWaitPeriod(-1);
    }

    @Test public void clonesShouldShareSameInstances() {
        XMSConfiguration configuration = new Builder().build();

        XMSConfiguration firstClone = configuration.newBuilder().build();
        XMSConfiguration secondClone = configuration.newBuilder().build();

        assertThat(firstClone.locationRequest())
              .isEqualTo(secondClone.locationRequest())
              .isEqualTo(createDefaultLocationRequest());
        assertThat(firstClone.askForXMS())
              .isEqualTo(secondClone.askForXMS())
              .isFalse();
        assertThat(firstClone.askForSettingsApi())
              .isEqualTo(secondClone.askForSettingsApi())
              .isTrue();
        assertThat(firstClone.failOnSettingsApiSuspended())
              .isEqualTo(secondClone.failOnSettingsApiSuspended())
              .isFalse();
        assertThat(firstClone.ignoreLastKnowLocation())
              .isEqualTo(secondClone.ignoreLastKnowLocation())
              .isFalse();
        assertThat(firstClone.xmsWaitPeriod())
              .isEqualTo(secondClone.xmsWaitPeriod())
              .isEqualTo(20 * SECOND);
    }

    private LocationRequest createDefaultLocationRequest() {
        return LocationRequest.create()
.setPriority(LocationRequest.getPRIORITY_BALANCED_POWER_ACCURACY())
              .setInterval(5 * MINUTE)
              .setFastestInterval(MINUTE);
    }

}
