package com.megaache.xmslocationmanager;

import android.content.Intent;

import com.megaache.xmslocationmanager.XMSLocationManager.Builder;
import com.megaache.xmslocationmanager.configuration.XMSLocationConfiguration;
import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.ProcessType;
import com.megaache.xmslocationmanager.listener.LocationListener;
import com.megaache.xmslocationmanager.providers.locationprovider.DispatcherLocationProvider;
import com.megaache.xmslocationmanager.providers.locationprovider.LocationProvider;
import com.megaache.xmslocationmanager.providers.permissionprovider.PermissionProvider;
import com.megaache.xmslocationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XMSLocationManagerTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;
    @Mock LocationProvider locationProvider;
    @Mock PermissionProvider permissionProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    XMSLocationConfiguration locationConfiguration;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(locationConfiguration.permissionConfiguration().permissionProvider()).thenReturn(permissionProvider);
    }

    @Test public void buildingWithoutContextProcessorShouldThrowException() {
        expectedException.expect(IllegalStateException.class);

        //noinspection ConstantConditions
        new Builder(((ContextProcessor) null))
                .locationProvider(locationProvider)
                .notify(locationListener)
                .build();
    }

    // region Build Tests
    @Test public void buildingWithoutConfigurationShouldThrowException() {
        expectedException.expect(IllegalStateException.class);

        new XMSLocationManager.Builder(contextProcessor)
              .locationProvider(locationProvider)
              .notify(locationListener)
              .build();
    }

    @Test public void buildingWithoutProviderShouldUseDispatcherLocationProvider() {
        XMSLocationManager locationManager = new Builder(contextProcessor)
              .configuration(locationConfiguration)
              .notify(locationListener)
              .build();

        assertThat(locationManager.activeProvider())
              .isNotNull()
              .isExactlyInstanceOf(DispatcherLocationProvider.class);
    }

    @Test public void buildingShouldCallConfigureAndSetListenerOnProvider() {
        buildLocationManager();

        verify(locationProvider).configure(contextProcessor, locationConfiguration, locationListener);
    }

    @Test public void buildingShouldSetContextProcessorAndListenerToPermissionListener() {
        XMSLocationManager locationManager = buildLocationManager();

        verify(permissionProvider).setContextProcessor(contextProcessor);
        verify(permissionProvider).setPermissionListener(locationManager);
    }
    // endregion

    // region Redirect Tests
    @Test public void whenOnPauseShouldRedirectToLocationProvider() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.onPause();

        verify(locationProvider).onPause();
    }

    @Test public void whenOnResumeShouldRedirectToLocationProvider() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.onResume();

        verify(locationProvider).onResume();
    }

    @Test public void whenOnDestroyShouldRedirectToLocationProvider() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.onDestroy();

        verify(locationProvider).onDestroy();
    }

    @Test public void whenCancelShouldRedirectToLocationProvider() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.cancel();

        verify(locationProvider).cancel();
    }

    @Test public void whenOnActivityResultShouldRedirectToLocationProvider() {
        XMSLocationManager locationManager = buildLocationManager();
        int requestCode = 1;
        int resultCode = 2;
        Intent data = new Intent();

        locationManager.onActivityResult(requestCode, resultCode, data);

        verify(locationProvider).onActivityResult(eq(requestCode), eq(resultCode), eq(data));
    }

    @Test public void whenOnRequestPermissionsResultShouldRedirectToPermissionProvider() {
        XMSLocationManager locationManager = buildLocationManager();
        int requestCode = 1;
        String[] permissions = new String[1];
        int[] grantResults = new int[1];

        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

        verify(permissionProvider).onRequestPermissionsResult(eq(requestCode), eq(permissions), eq(grantResults));
    }

    @Test public void whenGetShouldRedirectToLocationProviderWhenPermissionIsGranted() {
        when(permissionProvider.hasPermission()).thenReturn(true);
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.get();

        verify(locationProvider).get();
    }
    // endregion

    // region Retrieve Tests
    @Test public void isWaitingForLocationShouldRetrieveFromLocationProvider() {
        when(locationProvider.isWaiting()).thenReturn(true);
        XMSLocationManager locationManager = buildLocationManager();

        assertThat(locationManager.isWaitingForLocation()).isTrue();
        verify(locationProvider).isWaiting();
    }

    @Test public void isAnyDialogShowingShouldRetrieveFromLocationProvider() {
        when(locationProvider.isDialogShowing()).thenReturn(true);
        XMSLocationManager locationManager = buildLocationManager();

        assertThat(locationManager.isAnyDialogShowing()).isTrue();
        verify(locationProvider).isDialogShowing();
    }
    // endregion

    @Test public void whenGetCalledShouldStartPermissionRequest() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.get();

        verify(permissionProvider).hasPermission();
        verify(permissionProvider).requestPermissions();
    }

    @Test public void whenRequestPermissionsAreAlreadyGrantedShouldNotifyListenerWithTrue() {
        when(permissionProvider.hasPermission()).thenReturn(true);
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        verify(locationListener).onPermissionGranted(eq(true));
    }

    @Test public void whenRequestedPermissionsAreGrantedShouldNotifyListenerWithFalse() {
        XMSLocationManager locationManager = buildLocationManager();
        when(permissionProvider.getPermissionListener()).thenReturn(locationManager);

        permissionProvider.getPermissionListener().onPermissionsGranted();

        verify(locationListener).onPermissionGranted(eq(false));
    }

    @Test public void whenRequestedPermissionsAreDeniedShouldCallFailOnListener() {
        XMSLocationManager locationManager = buildLocationManager();
        when(permissionProvider.getPermissionListener()).thenReturn(locationManager);

        permissionProvider.getPermissionListener().onPermissionsDenied();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.PERMISSION_DENIED));
    }

    @Test public void whenAskForPermissionShouldNotifyListenerWithProcessTypeChanged() {
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        //noinspection WrongConstant
        verify(locationListener).onProcessTypeChanged(eq(ProcessType.ASKING_PERMISSIONS));
    }

    @Test public void whenRequestingPermissionIsNotPossibleThenItShouldFail() {
        when(permissionProvider.requestPermissions()).thenReturn(false);
        XMSLocationManager locationManager = buildLocationManager();

        locationManager.askForPermission();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.PERMISSION_DENIED));
    }

    private XMSLocationManager buildLocationManager() {
        return new Builder(contextProcessor)
              .locationProvider(locationProvider)
              .configuration(locationConfiguration)
              .notify(locationListener)
              .build();
    }

}