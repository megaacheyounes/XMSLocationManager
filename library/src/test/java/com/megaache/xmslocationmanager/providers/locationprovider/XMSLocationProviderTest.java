package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.location.Location;

import androidx.annotation.NonNull;

import org.xms.g.common.api.ApiException;
import org.xms.g.common.api.ResolvableApiException;
import org.xms.g.common.api.Status;
import org.xms.g.location.LocationResult;
import org.xms.g.location.LocationSettingsResponse;
import org.xms.g.location.LocationSettingsResult;
import org.xms.g.location.LocationSettingsStatusCodes;
import com.megaache.xmslocationmanager.configuration.XMSConfiguration;
import com.megaache.xmslocationmanager.configuration.XMSLocationConfiguration;
import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.ProcessType;
import com.megaache.xmslocationmanager.constants.RequestCode;
import com.megaache.xmslocationmanager.listener.FallbackListener;
import com.megaache.xmslocationmanager.listener.LocationListener;
import com.megaache.xmslocationmanager.fakes.FakeSimpleTask;
import com.megaache.xmslocationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xms.g.utils.XBox;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XMSLocationProviderTest {

    @Mock
    XMSLocationSource mockedSource;

    @Mock
    Location location;
    @Mock
    Context context;
    @Mock
    Activity activity;

    @Mock
    ContextProcessor contextProcessor;
    @Mock
    LocationListener locationListener;

    @Mock
    XMSLocationConfiguration locationConfiguration;
    @Mock
    XMSConfiguration xmsConfiguration;
    @Mock
    FallbackListener fallbackListener;

    private XMSLocationProvider XMSLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        XMSLocationProvider = spy(new XMSLocationProvider(fallbackListener));
        XMSLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
        XMSLocationProvider.setDispatcherLocationSource(mockedSource);

        when(locationConfiguration.xmsConfiguration()).thenReturn(xmsConfiguration);
        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);
    }

    @Test
    public void onResumeShouldNotRequestLocationUpdateWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        XMSLocationProvider.onResume();

        verify(mockedSource, never()).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldNotRequestLocationUpdateWhenLocationIsAlreadyProvidedAndNotRequiredToKeepTracking() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        XMSLocationProvider.onResume();

        verify(mockedSource, never()).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldRequestLocationUpdateWhenLocationIsNotYetProvided() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.setWaiting(true);

        XMSLocationProvider.onResume();

        getLocationTask.success(null);

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void onResumeShouldRequestLocationUpdateWhenLocationIsAlreadyProvidedButRequiredToKeepTracking() {
        XMSLocationProvider.setWaiting(true);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.onResume();

        getLocationTask.success(location);

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void onPauseShouldNotRemoveLocationUpdatesWhenSettingsDialogIsOnTrue() {
        makeSettingsDialogIsOnTrue();

        XMSLocationProvider.onPause();

        verify(mockedSource, never()).requestLocationUpdate();
        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onPauseShouldRemoveLocationUpdates() {
        XMSLocationProvider.onPause();

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onDestroyShouldRemoveLocationUpdates() {
        XMSLocationProvider.onDestroy();

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void isDialogShownShouldReturnFalseWhenSettingsApiDialogIsNotShown() {
        assertThat(XMSLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void isDialogShownShouldReturnTrueWhenSettingsApiDialogShown() {
        makeSettingsDialogIsOnTrue();

        assertThat(XMSLocationProvider.isDialogShowing()).isTrue();
    }

    @Test
    public void getShouldSetWaitingTrue() {
        assertThat(XMSLocationProvider.isWaiting()).isFalse();

        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.get();

        getLocationTask.success(null);

        assertThat(XMSLocationProvider.isWaiting()).isTrue();
    }

    @Test
    public void getShouldFailWhenThereIsNoContext() {
        when(contextProcessor.getContext()).thenReturn(null);

        XMSLocationProvider.get();

        verify(locationListener).onLocationFailed(FailType.VIEW_DETACHED);
    }

    @Test
    public void getShouldRequestLocationUpdate() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.get();

        getLocationTask.success(null);

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void cancelShouldRemoveLocationRequestWhenInvokeCancel() {
        XMSLocationProvider.cancel();

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onActivityResultShouldSetDialogShownToFalse() {
        makeSettingsDialogIsOnTrue();
        assertThat(XMSLocationProvider.isDialogShowing()).isTrue();

        XMSLocationProvider.onActivityResult(RequestCode.SETTINGS_API, -1, null);

        assertThat(XMSLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void onActivityResultShouldRequestLocationUpdateWhenResultIsOk() {
        XMSLocationProvider.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_OK, null);

        verify(XMSLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onActivityResultShouldCallSettingsApiFailWhenResultIsNotOk() {
        XMSLocationProvider.onActivityResult(RequestCode.SETTINGS_API, Activity.RESULT_CANCELED, null);

        verify(XMSLocationProvider).settingsApiFail(FailType.XMS_SETTINGS_DENIED);
    }

    @Test
    public void onConnectedShouldNotCheckLastKnowLocationWhenRequirementsIgnore() {
        when(xmsConfiguration.ignoreLastKnowLocation()).thenReturn(true);

        XMSLocationProvider.onConnected();

        verify(XMSLocationProvider, never()).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldCheckLastKnowLocation() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.onConnected();

        getLocationTask.success(location);

        verify(XMSLocationProvider).checkLastKnowLocation();
    }

    @Test
    public void onConnectedShouldNotCallLocationRequiredWhenLastKnowIsReadyAndNoNeedToKeepTracking() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);
        when(locationConfiguration.keepTracking()).thenReturn(false);

        XMSLocationProvider.onConnected();

        getLocationTask.success(location);

        verify(XMSLocationProvider, never()).locationRequired();
    }

    @Test
    public void onConnectedShouldCallRequestLocationUpdateWhenLastLocationIsNull() {
        // Have first condition false
        when(locationConfiguration.keepTracking()).thenReturn(false);

        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        // Have second condition false
        when(xmsConfiguration.ignoreLastKnowLocation()).thenReturn(false);
        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        // isWaiting is false on start, onConnected don't changes it

        XMSLocationProvider.onConnected();

        getLocationTask.success(null);

        verify(XMSLocationProvider).locationRequired();
        verify(XMSLocationProvider).requestLocationUpdate();
        assertThat(XMSLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onConnectedShouldCallLocationRequiredWhenLastKnowIsNotAvailable() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.onConnected();

        getLocationTask.success(null);

        verify(XMSLocationProvider).locationRequired();
    }

    @Test
    public void onConnectedShouldCallLocationRequiredWhenConfigurationRequiresKeepTracking() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);
        when(locationConfiguration.keepTracking()).thenReturn(true);

        XMSLocationProvider.onConnected();

        getLocationTask.success(location);

        verify(XMSLocationProvider).locationRequired();
    }

    @Test
    public void onLocationChangedShouldNotifyListener() {
        XMSLocationProvider.onLocationChanged(location);

        verify(locationListener).onLocationChanged(location);
        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldSetWaitingFalse() {
        XMSLocationProvider.setWaiting(true);
        assertThat(XMSLocationProvider.isWaiting()).isTrue();

        XMSLocationProvider.onLocationChanged(location);

        assertThat(XMSLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void onLocationChangedShouldRemoveUpdateLocationWhenKeepTrackingIsNotRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(false);

        XMSLocationProvider.onLocationChanged(location);

        verify(mockedSource).removeLocationUpdates();
    }

    @Test
    public void onLocationChangedShouldNotRemoveUpdateLocationWhenKeepTrackingIsRequired() {
        when(locationConfiguration.keepTracking()).thenReturn(true);

        XMSLocationProvider.onLocationChanged(location);

        verify(mockedSource, never()).removeLocationUpdates();
    }

    @Test
    public void onLocationResultShouldCallOnLocationChangedWhenLocationListIsNotEmpty() {
        List<Location> locations = new ArrayList<>();

        locations.add(new Location("1"));
        locations.add(new Location("2"));

        LocationResult locationResult = LocationResult.create(locations);

        XMSLocationProvider.onLocationResult(locationResult);

        verify(XMSLocationProvider, atLeastOnce()).onLocationChanged(any(Location.class));

        verify(locationListener, atLeastOnce()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onLocationResultShouldNotCallOnLocationChangedWhenLocationListIsEmpty() {
        List<Location> locations = new ArrayList<>();

        LocationResult locationResult = LocationResult.create(locations);

        XMSLocationProvider.onLocationResult(locationResult);

        verify(XMSLocationProvider, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onLocationResultShouldNotCallOnLocationChangedWhenLocationResultIsNull() {
        XMSLocationProvider.onLocationResult(null);

        verify(XMSLocationProvider, never()).onLocationChanged(any(Location.class));
    }

    @Test
    public void onResultShouldCallRequestLocationUpdateWhenSuccess() {
        XMSLocationProvider.onSuccess(getSettingsResultWithSuccess(LocationSettingsStatusCodes.getSUCCESS()));

        verify(XMSLocationProvider).requestLocationUpdate();
    }

    @Test
    public void onResultShouldCallSettingsApiFailWhenChangeUnavailable() {
        XMSLocationProvider
.onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.getSETTINGS_CHANGE_UNAVAILABLE()));

        verify(XMSLocationProvider).settingsApiFail(FailType.XMS_SETTINGS_DIALOG);
    }

    @Test
    public void onResultShouldCallResolveSettingsApiWhenResolutionRequired() {
        Exception settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.getRESOLUTION_REQUIRED());
        XMSLocationProvider.onFailure(settingsResultWith);

        verify(XMSLocationProvider).resolveSettingsApi((any(ResolvableApiException.class)));
    }

    @Test
    public void onResultShouldCallSettingsApiFailWithSettingsDeniedWhenOtherCase() {
        Exception settingsResultWith = getSettingsResultWithError(LocationSettingsStatusCodes.getCANCELED());
        XMSLocationProvider.onFailure(settingsResultWith);

        verify(XMSLocationProvider).settingsApiFail(FailType.XMS_SETTINGS_DENIED);
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenThereIsNoActivity() {
        when(contextProcessor.getActivity()).thenReturn(null);

        XMSLocationProvider.resolveSettingsApi(new ResolvableApiException(new Status(1)));

        verify(XMSLocationProvider).settingsApiFail(FailType.VIEW_NOT_REQUIRED_TYPE);
    }

    @Test
    public void resolveSettingsApiShouldStartSettingsApiResolutionForResult() throws Exception {
        Status status = new Status(1);
        ResolvableApiException resolvable = new ResolvableApiException(status);

        XMSLocationProvider.resolveSettingsApi(resolvable);

        verify(mockedSource).startSettingsApiResolutionForResult(resolvable, activity);
    }

    @Test
    public void resolveSettingsApiShouldCallSettingsApiFailWhenExceptionThrown() throws Exception {
        Status status = new Status(1);
        ResolvableApiException resolvable = new ResolvableApiException(status);

        doThrow(new SendIntentException()).when(mockedSource).startSettingsApiResolutionForResult(resolvable, activity);

        XMSLocationProvider.resolveSettingsApi(resolvable);

        verify(XMSLocationProvider).settingsApiFail(FailType.XMS_SETTINGS_DIALOG);
    }

    @Test
    public void checkLastKnowLocationInvokeRequestLocationTrueWhenLocationIsAvailable() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.checkLastKnowLocation();

        getLocationTask.success(location);

        verify(mockedSource).getLastLocation();

        verify(XMSLocationProvider).onLocationChanged(location);
        verify(XMSLocationProvider).requestLocation(true);
    }

    @Test
    public void checkLastKnowLocationShouldInvokeRequestLocationFalseWhenLastKnownLocationIsNull() {
        FakeSimpleTask<Location> getLocationTask = new FakeSimpleTask<>();

        when(mockedSource.getLastLocation()).thenReturn(getLocationTask);

        XMSLocationProvider.checkLastKnowLocation();

        getLocationTask.success(null);

        verify(mockedSource).getLastLocation();
        verify(XMSLocationProvider).requestLocation(false);
    }

    @Test
    public void locationRequiredShouldCheckLocationSettingsWhenConfigurationAsksForSettingsApi() {
        when(xmsConfiguration.askForSettingsApi()).thenReturn(true);

        XMSLocationProvider.locationRequired();

        verify(mockedSource).checkLocationSettings();
    }

    @Test
    public void locationRequiredShouldRequestLocationUpdateWhenConfigurationDoesntRequireToAskForSettingsApi() {
        XMSLocationProvider.locationRequired();

        verify(XMSLocationProvider).requestLocationUpdate();
    }

    @Test
    public void requestLocationUpdateShouldUpdateProcessTypeOnListener() {
        XMSLocationProvider.requestLocationUpdate();

        verify(locationListener).onProcessTypeChanged(ProcessType.GETTING_LOCATION_FROM_XMS);
    }

    @Test
    public void requestLocationUpdateShouldRequest() {
        XMSLocationProvider.requestLocationUpdate();

        verify(mockedSource).requestLocationUpdate();
    }

    @Test
    public void settingsApiFailShouldCallFailWhenConfigurationFailOnSettingsApiSuspendedTrue() {
        when(xmsConfiguration.failOnSettingsApiSuspended()).thenReturn(true);

        XMSLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(XMSLocationProvider).failed(FailType.UNKNOWN);
    }

    @Test
    public void settingsApiFailShouldCallRequestLocationUpdateWhenConfigurationFailOnSettingsApiSuspendedFalse() {
        when(xmsConfiguration.failOnSettingsApiSuspended()).thenReturn(false);

        XMSLocationProvider.settingsApiFail(FailType.UNKNOWN);

        verify(XMSLocationProvider).requestLocationUpdate();
    }

    @Test
    public void failedShouldRedirectToListenerWhenFallbackToDefaultIsFalse() {
        when(xmsConfiguration.fallbackToDefault()).thenReturn(false);

        XMSLocationProvider.failed(FailType.UNKNOWN);

        verify(locationListener).onLocationFailed(FailType.UNKNOWN);
    }

    @Test
    public void failedShouldCallFallbackWhenFallbackToDefaultIsTrue() {
        when(xmsConfiguration.fallbackToDefault()).thenReturn(true);

        XMSLocationProvider.failed(FailType.UNKNOWN);

        verify(fallbackListener).onFallback();
    }

    @Test
    public void failedShouldSetWaitingFalse() {
        XMSLocationProvider.setWaiting(true);
        assertThat(XMSLocationProvider.isWaiting()).isTrue();

        XMSLocationProvider.failed(FailType.UNKNOWN);

        assertThat(XMSLocationProvider.isWaiting()).isFalse();
    }

    private void makeSettingsDialogIsOnTrue() {
        XMSLocationProvider.onFailure(getSettingsResultWithError(LocationSettingsStatusCodes.getRESOLUTION_REQUIRED()));
    }

    @NonNull
    private static LocationSettingsResponse getSettingsResultWithSuccess(int statusCode) {
        Status status = new Status(statusCode, null, null);

        LocationSettingsResult result = new LocationSettingsResult(new XBox(status.getGInstance(),status.getHInstance()));

        LocationSettingsResponse response = new LocationSettingsResponse(new XBox(
                new com.google.android.gms.location.LocationSettingsResponse(),
                new com.huawei.hms.location.LocationSettingsResponse(null)));

        response.setResult(result);

        return response;
    }

    @NonNull
    private static Exception getSettingsResultWithError(int statusCode) {
        Status status = new Status(statusCode, null, null);

        if (statusCode == LocationSettingsStatusCodes.getRESOLUTION_REQUIRED()) {
            return new ResolvableApiException(status);
        } else {
            return new ApiException(status);
        }
    }
}