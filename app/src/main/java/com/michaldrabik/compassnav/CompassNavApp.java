package com.michaldrabik.compassnav;

import android.app.Application;
import timber.log.Timber;

public class CompassNavApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    setupTimber();
  }

  private void setupTimber() {
    Timber.plant(new Timber.DebugTree());
  }

}
