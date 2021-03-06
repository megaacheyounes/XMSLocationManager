package com.megaache.xmslocationmanager.sample.fragment;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.megaache.xmslocationmanager.base.LocationBaseFragment;
import com.megaache.xmslocationmanager.configuration.Configurations;
import com.megaache.xmslocationmanager.configuration.XMSLocationConfiguration;
import com.megaache.xmslocationmanager.constants.FailType;
import com.megaache.xmslocationmanager.constants.ProcessType;
import com.megaache.xmslocationmanager.sample.R;
import com.megaache.xmslocationmanager.sample.SamplePresenter;
import com.megaache.xmslocationmanager.sample.SamplePresenter.SampleView;

public class SampleFragment extends LocationBaseFragment implements SampleView {

    private ProgressDialog progressDialog;
    private TextView locationText;

    private SamplePresenter samplePresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_display_layout, container, false);
        locationText = (TextView) view.findViewById(R.id.locationText);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (samplePresenter != null) samplePresenter.destroy();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        samplePresenter = new SamplePresenter(this);
        getLocation();
    }

    @Override
    public XMSLocationConfiguration getLocationConfiguration() {
        return Configurations.defaultConfiguration("Gimme the permission!", "Would you mind to turn GPS on?");
    }

    @Override
    public void onLocationChanged(Location location) {
        samplePresenter.onLocationChanged(location);
    }

    @Override
    public void onLocationFailed(@FailType int failType) {
        samplePresenter.onLocationFailed(failType);
    }

    @Override
    public void onProcessTypeChanged(@ProcessType int processType) {
        samplePresenter.onProcessTypeChanged(processType);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getLocationManager().isWaitingForLocation()
              && !getLocationManager().isAnyDialogShowing()) {
            displayProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissProgress();
    }

    private void displayProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.getWindow().addFlags(Window.FEATURE_NO_TITLE);
            progressDialog.setMessage("Getting location...");
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    public String getText() {
        return locationText.getText().toString();
    }

    @Override
    public void setText(String text) {
        locationText.setText(text);
    }

    @Override
    public void updateProgress(String text) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(text);
        }
    }

    @Override
    public void dismissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
