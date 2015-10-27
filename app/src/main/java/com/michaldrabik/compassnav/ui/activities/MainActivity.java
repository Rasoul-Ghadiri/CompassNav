package com.michaldrabik.compassnav.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.michaldrabik.compassnav.NavigationManager;
import com.michaldrabik.compassnav.R;
import com.michaldrabik.compassnav.ui.views.LocationDialogView;

public class MainActivity extends BaseActivity implements NavigationManager.OnSensorsReadingsListener {

  @Bind(R.id.main_activity_compass_plate_image) ImageView compassImage;
  @Bind(R.id.main_activity_arrow_image) ImageView arrowImage;
  @Bind(R.id.main_activity_navigate_text) TextView navigatingToText;
  @Bind(R.id.main_activity_distance_text) TextView distanceText;

  private NavigationManager navigationManager;

  @Override protected int getLayoutResId() {
    return R.layout.activity_main;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    navigationManager = new NavigationManager(this, this);
  }

  @Override protected void onResume() {
    super.onResume();
    if (isLocationServiceEnabled()) {
      navigationManager.startNavigating(50.0610055, 19.940215); //Main Square in Cracow
      updateNavigationText(navigationManager.getLocation().getLatitude(), navigationManager.getLocation().getLongitude());
    } else {
      navigatingToText.setText(R.string.no_services_error);
    }
  }

  @Override protected void onPause() {
    navigationManager.stopNavigating();
    super.onPause();
  }

  @Override public void onNorthAzimuthUpdate(float northAzimuthInDegrees) {
    compassImage.setRotation(northAzimuthInDegrees);
  }

  @Override public void onHeadingUpdate(float headingInDegrees) {
    arrowImage.setRotation(headingInDegrees);
  }

  @Override public void onDistanceUpdate(float distance) {
    distanceText.setText(String.format("Distance: ~%.0f m", distance));
  }

  @OnClick(R.id.main_activity_button) public void onLocationButtonClick() {
    showLocationDialog();
    navigationManager.stopNavigating();
  }

  private boolean isLocationServiceEnabled() {
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
  }

  private void showLocationDialog() {
    final LocationDialogView dialogView =
        new LocationDialogView(this, navigationManager.getLocation().getLatitude(), navigationManager.getLocation().getLongitude());
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.change_location));
    builder.setView(dialogView);
    builder.setCancelable(false);
    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        navigationManager.startNavigating(dialogView.getLatitude(), dialogView.getLongitude());
        updateNavigationText(dialogView.getLatitude(), dialogView.getLongitude());
      }
    });
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        navigationManager.startNavigating(navigationManager.getLocation().getLatitude(), navigationManager.getLocation().getLongitude());
        dialog.cancel();
      }
    });
    builder.show();
  }

  private void updateNavigationText(double latitude, double longitude) {
    navigatingToText.setText(getString(R.string.navigating_to, latitude, longitude));
  }

}
