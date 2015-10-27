package com.michaldrabik.compassnav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * <p>Simple class that manages Google Location Services, and hardware sensors.</p>
 * <p>{@link OnSensorsReadingsListener} is used to return the correct bearing.</p>
 *
 * Author: Michal Drabik (michal.drabik@outlook.com)
 */
public class NavigationManager {

  public interface OnSensorsReadingsListener {
    void onNorthAzimuthUpdate(float northAzimuthInDegrees);

    void onHeadingUpdate(float headingInDegrees);

    void onDistanceUpdate(float distance);
  }

  private final SensorManager sensorManager;
  private final Sensor magnetometerSensor;
  private final Sensor accelerometerSensor;
  private final GoogleApiClient googleApiClient;
  private final LocationRequest locationRequest;
  private final LocationListener locationListener;
  private final SensorEventListener sensorEventListener;
  private final SensorsObserver sensorsObserver;

  private final Location location;
  private final float[] accelerometerReadings = new float[3];
  private final float[] magnetometerReadings = new float[3];
  private final float[] rotationMatrix = new float[9];
  private final float[] orientation = new float[3];
  private final float[] distance = new float[3];

  public NavigationManager(Context context, OnSensorsReadingsListener onSensorsReadingsListener) {
    this.sensorsObserver = new SensorsObserver(onSensorsReadingsListener);
    this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    this.magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    this.sensorEventListener = createSensorEventListener();
    this.googleApiClient = createGoogleApiClient(context);
    this.locationRequest = createLocationRequest();
    this.locationListener = createLocationListener();
    this.location = new Location("");
  }

  /**
   * Starts navigating to given location.
   *
   * @param latitude Location's latitude.
   * @param longitude Location's longitude.
   */
  public void startNavigating(double latitude, double longitude) {
    this.location.setLatitude(latitude);
    this.location.setLongitude(longitude);
    this.sensorManager.registerListener(sensorEventListener, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    this.sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    this.googleApiClient.connect();
  }

  /**
   * Stops navigation.
   */
  public void stopNavigating() {
    this.sensorManager.unregisterListener(sensorEventListener);
    if (googleApiClient.isConnected()) {
      LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
      this.googleApiClient.disconnect();
    }
  }

  /**
   * @return Selected {@link Location}.
   */
  public Location getLocation() {
    return this.location;
  }

  private SensorEventListener createSensorEventListener() {
    return new SensorEventListener() {
      @Override public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometerSensor) {
          System.arraycopy(event.values, 0, accelerometerReadings, 0, event.values.length);
        } else if (event.sensor == magnetometerSensor) {
          System.arraycopy(event.values, 0, magnetometerReadings, 0, event.values.length);
        }
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReadings, magnetometerReadings);
        SensorManager.getOrientation(rotationMatrix, orientation);
        float azimuthInRadians = orientation[0];
        sensorsObserver.updateNorthAzimuth(azimuthInRadians);
      }

      @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
      }
    };
  }

  private GoogleApiClient createGoogleApiClient(Context context) {
    return new GoogleApiClient.Builder(context).addConnectionCallbacks(createGoogleApiConnectionCallbacks())
        .addOnConnectionFailedListener(createGoogleApiConnectionFailListener())
        .addApi(LocationServices.API)
        .build();
  }

  private LocationRequest createLocationRequest() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(1000);
    return locationRequest;
  }

  private LocationListener createLocationListener() {
    return new LocationListener() {
      @Override public void onLocationChanged(Location deviceLocation) {
        Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(), location.getLatitude(), location.getLongitude(),
            distance);
        sensorsObserver.updateHeading(deviceLocation.bearingTo(location));
        sensorsObserver.updateDistance(distance[0]);
      }
    };
  }

  @NonNull private GoogleApiClient.OnConnectionFailedListener createGoogleApiConnectionFailListener() {
    return new GoogleApiClient.OnConnectionFailedListener() {
      @Override public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("Google Api connection ERROR");
      }
    };
  }

  @NonNull private GoogleApiClient.ConnectionCallbacks createGoogleApiConnectionCallbacks() {
    return new GoogleApiClient.ConnectionCallbacks() {
      @Override public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        Timber.w("Google Api connection SUCCESS");
      }

      @Override public void onConnectionSuspended(int i) {
        Timber.w("Google Api connection SUSPENDED");
      }
    };
  }

  /**
   * Simple helper class that gathers information about north direction, heading and distance.
   * Applies smoothing filter and invoke {@link OnSensorsReadingsListener} callbacks.
   */
  private static class SensorsObserver {

    private static final int FILTER_THRESHOLD = 20;

    private final OnSensorsReadingsListener onSensorsReadingsListener;
    private final List<Float> recentNorthAzimuths;
    private float filteredNorthAzimuth;
    private float lastHeading;
    private float lastDistance;

    public SensorsObserver(OnSensorsReadingsListener onSensorsReadingsListener) {
      this.onSensorsReadingsListener = onSensorsReadingsListener;
      this.recentNorthAzimuths = new ArrayList<>();
    }

    public void updateNorthAzimuth(float azimuth) {
      float lastNorthAzimuthDegrees = (float) Math.round(Utils.normalizeDegree(-Math.toDegrees(azimuth)));
      recentNorthAzimuths.add(lastNorthAzimuthDegrees);
      if (recentNorthAzimuths.size() > FILTER_THRESHOLD) {
        recentNorthAzimuths.remove(0);
      }
      filteredNorthAzimuth = Utils.average(recentNorthAzimuths);
      invokeCallbacks();
    }

    public void updateHeading(float heading) {
      lastHeading = (float) Utils.normalizeDegree(heading) + filteredNorthAzimuth;
    }

    public void updateDistance(float distance) {
      lastDistance = distance;
    }

    private void invokeCallbacks() {
      onSensorsReadingsListener.onNorthAzimuthUpdate(filteredNorthAzimuth);
      onSensorsReadingsListener.onHeadingUpdate(lastHeading);
      onSensorsReadingsListener.onDistanceUpdate(lastDistance);
    }

  }
}
