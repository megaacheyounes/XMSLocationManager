package com.megaache.xmslocationmanager.providers.locationprovider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;

import org.xms.g.common.ConnectionResult;
import com.megaache.xmslocationmanager.configuration.DefaultProviderConfiguration;
import com.megaache.xmslocationmanager.configuration.XMSConfiguration;
import com.megaache.xmslocationmanager.configuration.XMSLocationConfiguration;
import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.RequestCode;
import com.megaache.xmslocationmanager.helper.continuoustask.ContinuousTask;
import com.megaache.xmslocationmanager.listener.LocationListener;
import com.megaache.xmslocationmanager.view.ContextProcessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DispatcherLocationProviderTest {

    private final static long XMS_SWITCH_PERIOD = 5 * 1000;
private final static int RESOLVABLE_ERROR = ConnectionResult.getSERVICE_MISSING();
private final static int NOT_RESOLVABLE_ERROR = ConnectionResult.getINTERNAL_ERROR();

    @Mock ContextProcessor contextProcessor;
    @Mock LocationListener locationListener;

    @Mock Activity activity;
    @Mock Context context;
    @Mock Dialog dialog;

    @Mock
    XMSLocationConfiguration locationConfiguration;
    @Mock
    XMSConfiguration xmsConfiguration;
    @Mock DefaultProviderConfiguration defaultProviderConfiguration;

    @Mock DispatcherLocationSource dispatcherLocationSource;
    @Mock DefaultLocationProvider defaultLocationProvider;
    @Mock
    XMSLocationProvider xmsLocationProvider;
    @Mock ContinuousTask continuousTask;

    private DispatcherLocationProvider dispatcherLocationProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        dispatcherLocationProvider = spy(new DispatcherLocationProvider());
        dispatcherLocationProvider.configure(contextProcessor, locationConfiguration, locationListener);
        dispatcherLocationProvider.setDispatcherLocationSource(dispatcherLocationSource);

        when(locationConfiguration.defaultProviderConfiguration()).thenReturn(defaultProviderConfiguration);
        when(locationConfiguration.xmsConfiguration()).thenReturn(xmsConfiguration);
        when(xmsConfiguration.xmsWaitPeriod()).thenReturn(XMS_SWITCH_PERIOD);

        when(dispatcherLocationSource.createDefaultLocationProvider()).thenReturn(defaultLocationProvider);
        when(dispatcherLocationSource.createXMSLocationProvider(dispatcherLocationProvider))
              .thenReturn(xmsLocationProvider);
        when(dispatcherLocationSource.gpServicesSwitchTask()).thenReturn(continuousTask);

        when(contextProcessor.getContext()).thenReturn(context);
        when(contextProcessor.getActivity()).thenReturn(activity);
    }

    @Test
    public void onPauseShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onPause();

        verify(defaultLocationProvider).onPause();
        verify(continuousTask).pause();
    }

    @Test
    public void onResumeShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onResume();

        verify(defaultLocationProvider).onResume();
        verify(continuousTask).resume();
    }

    @Test
    public void onDestroyShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.onDestroy();

        verify(defaultLocationProvider).onDestroy();
        verify(continuousTask).stop();
    }

    @Test
    public void cancelShouldRedirectToActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);

        dispatcherLocationProvider.cancel();

        verify(defaultLocationProvider).cancel();
        verify(continuousTask).stop();
    }

    @Test
    public void isWaitingShouldReturnFalseWhenNoActiveProvider() {
        assertThat(dispatcherLocationProvider.isWaiting()).isFalse();
    }

    @Test
    public void isWaitingShouldRetrieveFromActiveProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        when(defaultLocationProvider.isWaiting()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isWaiting()).isTrue();
        verify(defaultLocationProvider).isWaiting();
    }

    @Test
    public void isDialogShowingShouldReturnFalseWhenNoDialogShown() {
        assertThat(dispatcherLocationProvider.isDialogShowing()).isFalse();
    }

    @Test
    public void isDialogShowingShouldReturnTrueWhenGpServicesIsShowing() {
        showGpServicesDialogShown(); // so dialog is not null
        when(dialog.isShowing()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isDialogShowing()).isTrue();
    }

    @Test
    public void isDialogShowingShouldRetrieveFromActiveProviderWhenExists() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider); // so provider is not null
        when(defaultLocationProvider.isDialogShowing()).thenReturn(true);

        assertThat(dispatcherLocationProvider.isDialogShowing()).isTrue();
        verify(defaultLocationProvider).isDialogShowing();
    }

    @Test
    public void runScheduledTaskShouldDoNothingWhenActiveProviderIsNotGPServices() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        verify(defaultLocationProvider).configure(dispatcherLocationProvider);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.XMS_SWITCH_TASK);

        verifyNoMoreInteractions(defaultLocationProvider);
    }

    @Test
    public void runScheduledTaskShouldDoNothingWhenNoOnGoingTask() {
        dispatcherLocationProvider.setLocationProvider(xmsLocationProvider);
        verify(xmsLocationProvider).configure(dispatcherLocationProvider);
        when(xmsLocationProvider.isWaiting()).thenReturn(false);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.XMS_SWITCH_TASK);
        verify(xmsLocationProvider).isWaiting();

        verifyNoMoreInteractions(xmsLocationProvider);
    }

    @Test
    public void runScheduledTaskShouldCancelCurrentProviderAndRunWithDefaultWhenGpServicesTookEnough() {
        dispatcherLocationProvider.setLocationProvider(xmsLocationProvider);
        when(xmsLocationProvider.isWaiting()).thenReturn(true);

        dispatcherLocationProvider.runScheduledTask(DispatcherLocationSource.XMS_SWITCH_TASK);

        verify(dispatcherLocationProvider).cancel();
        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void onActivityResultShouldRedirectToActiveProvider() {
        Intent data = new Intent();
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        dispatcherLocationProvider.onActivityResult(-1, -1, data);

        verify(defaultLocationProvider).onActivityResult(eq(-1), eq(-1), eq(data));
    }

    @Test
    public void onActivityResultShouldCallCheckXMSAvailabilityWithFalseWhenRequestCodeMatches() {
        when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.onActivityResult(RequestCode.XMS, -1, null);

        verify(dispatcherLocationProvider).checkXMSAvailability(eq(false));
    }

    @Test
    public void getShouldContinueWithDefaultProviderIfThereIsNoGpServicesConfiguration() {
        when(locationConfiguration.xmsConfiguration()).thenReturn(null);

        dispatcherLocationProvider.get();

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void getShouldCallCheckXMSAvailabilityWithTrue() {
        when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);
        when(xmsConfiguration.askForXMS()).thenReturn(true);

        dispatcherLocationProvider.get();

        verify(dispatcherLocationProvider).checkXMSAvailability(eq(true));
    }

    @Test
    public void onFallbackShouldCallCancelAndContinueWithDefaultProviders() {
        dispatcherLocationProvider.onFallback();

        verify(dispatcherLocationProvider).cancel();
        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void checkXMSAvailabilityShouldGetLocationWhenApiIsAvailable() {
when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(ConnectionResult.getSUCCESS());

        dispatcherLocationProvider.checkXMSAvailability(false); // could be also true, wouldn't matter

        verify(dispatcherLocationProvider).getLocationFromXMS();
    }

    @Test
    public void checkXMSAvailabilityShouldContinueWithDefaultWhenCalledWithFalse() {
        when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.checkXMSAvailability(false);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void checkXMSAvailabilityShouldAskForXMSWhenCalledWithTrue() {
        when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);

        dispatcherLocationProvider.checkXMSAvailability(true);

        verify(dispatcherLocationProvider).askForXMS(eq(RESOLVABLE_ERROR));
    }

    @Test
    public void askForXMSShouldContinueWithDefaultProvidersWhenErrorNotResolvable() {
        when(xmsConfiguration.askForXMS()).thenReturn(true);
        when(dispatcherLocationSource.isXApiErrorUserResolvable(NOT_RESOLVABLE_ERROR)).thenReturn(false);

        dispatcherLocationProvider.askForXMS(NOT_RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void askForXMSShouldContinueWithDefaultProvidersWhenConfigurationNoRequire() {
        when(xmsConfiguration.askForXMS()).thenReturn(false);
        when(dispatcherLocationSource.isXApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);

        dispatcherLocationProvider.askForXMS(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void askForXMSShouldResolveXMSWhenPossible() {
        when(xmsConfiguration.askForXMS()).thenReturn(true);
        when(dispatcherLocationSource.isXApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);

        dispatcherLocationProvider.askForXMS(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).resolveXMS(RESOLVABLE_ERROR);
    }

    @Test
    public void resolveXMSShouldContinueWithDefaultWhenResolveDialogIsNull() {
        when(dispatcherLocationSource.getXApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.XMS), any(OnCancelListener.class))).thenReturn(null);

        dispatcherLocationProvider.resolveXMS(RESOLVABLE_ERROR);

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveXMSShouldContinueWithDefaultWhenErrorCannotBeResolved() {

int unresolvableError = ConnectionResult.getSERVICE_INVALID();

        final DialogInterface.OnDismissListener[] dismissListener = new DialogInterface.OnDismissListener[1];

        when(dispatcherLocationSource.getXApiErrorDialog(eq(activity), eq(unresolvableError),
                eq(RequestCode.XMS), any(OnCancelListener.class))).thenReturn(dialog);

        // catch and store real OnDismissListener listener
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                dismissListener[0] = invocation.getArgument(0);

                return null;
            }
        }).when(dialog).setOnDismissListener(any(DialogInterface.OnDismissListener.class));

        // simulate dialog dismiss event
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                dismissListener[0].onDismiss(dialog);

                return null;
            }
        }).when(dialog).dismiss();

        dispatcherLocationProvider.resolveXMS(unresolvableError);

        verify(dialog).show();

        dialog.dismiss(); // Simulate dismiss dialog (error cannot be resolved)

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveXMSShouldContinueWithDefaultWhenWhenResolveDialogIsCancelled() {

        final DialogInterface.OnCancelListener[] cancelListener = new OnCancelListener[1];

        // catch and store real OnCancelListener listener
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                cancelListener[0] = invocation.getArgument(3);

                return dialog;
            }
        }).when(dispatcherLocationSource).getXApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
                eq(RequestCode.XMS), any(OnCancelListener.class));

        // simulate dialog cancel event
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                cancelListener[0].onCancel(dialog);

                return null;
            }
        }).when(dialog).cancel();

        dispatcherLocationProvider.resolveXMS(RESOLVABLE_ERROR);

        verify(dialog).show();

        dialog.cancel(); // Simulate cancel dialog (user cancelled dialog)

        verify(dispatcherLocationProvider).continueWithDefaultProviders();
    }

    @Test
    public void resolveXMSShouldShowDialogWhenResolveDialogNotNull() {
        when(dispatcherLocationSource.getXApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.XMS), any(OnCancelListener.class))).thenReturn(dialog);

        dispatcherLocationProvider.resolveXMS(RESOLVABLE_ERROR);

        verify(dialog).show();
    }

    @Test
    public void getLocationFromXMS() {
        dispatcherLocationProvider.getLocationFromXMS();

        verify(xmsLocationProvider).configure(dispatcherLocationProvider);
        verify(continuousTask).delayed(XMS_SWITCH_PERIOD);
        verify(xmsLocationProvider).get();
    }

    @Test
    public void continueWithDefaultProvidersShouldNotifyFailWhenNoDefaultProviderConfiguration() {
        when(locationConfiguration.defaultProviderConfiguration()).thenReturn(null);

        dispatcherLocationProvider.continueWithDefaultProviders();

        //noinspection WrongConstant
        verify(locationListener).onLocationFailed(eq(FailType.XMS_NOT_AVAILABLE));
    }

    @Test
    public void continueWithDefaultProviders() {
        dispatcherLocationProvider.continueWithDefaultProviders();

        verify(defaultLocationProvider).configure(dispatcherLocationProvider);
        verify(defaultLocationProvider).get();
    }

    @Test
    public void setLocationProviderShouldConfigureGivenProvider() {
        dispatcherLocationProvider.setLocationProvider(defaultLocationProvider);
        verify(defaultLocationProvider).configure(dispatcherLocationProvider);
        dispatcherLocationProvider.setLocationProvider(xmsLocationProvider);
        verify(xmsLocationProvider).configure(dispatcherLocationProvider);
    }

    private void showGpServicesDialogShown() {
        when(xmsConfiguration.askForXMS()).thenReturn(true);
        when(dispatcherLocationSource.isXApiAvailable(context)).thenReturn(RESOLVABLE_ERROR);
        when(dispatcherLocationSource.isXApiErrorUserResolvable(RESOLVABLE_ERROR)).thenReturn(true);
        when(dispatcherLocationSource.getXApiErrorDialog(eq(activity), eq(RESOLVABLE_ERROR),
              eq(RequestCode.XMS), any(OnCancelListener.class))).thenReturn(dialog);

        dispatcherLocationProvider.checkXMSAvailability(true);

        verify(dialog).show();
    }


}