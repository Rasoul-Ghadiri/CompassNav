package com.michaldrabik.compassnav.ui.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.michaldrabik.compassnav.R;
import com.michaldrabik.compassnav.Utils;

public class LocationDialogView extends LinearLayout {

  private static final int MIN_LATITUDE = -90;
  private static final int MAX_LATITUDE = 90;
  private static final int MIN_LONGITUDE = -180;
  private static final int MAX_LONGITUDE = 180;

  @Bind(R.id.dialog_location_latitude_edit) EditText latitudeEdit;
  @Bind(R.id.dialog_location_longitude_edit) EditText longitudeEdit;

  private final double latitude;
  private final double longitude;

  public LocationDialogView(Context context, double latitude, double longitude) {
    super(context);
    View.inflate(getContext(), R.layout.dialog_location, this);
    ButterKnife.bind(this);
    this.latitude = latitude;
    this.longitude = longitude;
    latitudeEdit.setText(String.valueOf(latitude));
    longitudeEdit.setText(String.valueOf(longitude));
  }

  public double getLatitude() {
    return TextUtils.isEmpty(latitudeEdit.getText()) ? latitude : clampLatitude(Double.valueOf(latitudeEdit.getText().toString()));
  }

  public double getLongitude() {
    return TextUtils.isEmpty(longitudeEdit.getText()) ? longitude : clampLongitude(Double.valueOf(longitudeEdit.getText().toString()));
  }

  private double clampLatitude(double latitude) {
    return Utils.clamp(latitude, MIN_LATITUDE, MAX_LATITUDE);
  }

  private double clampLongitude(double longitude) {
    return Utils.clamp(longitude, MIN_LONGITUDE, MAX_LONGITUDE);
  }
}
