/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.tflitecamerademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import android.app.Notification;
import android.app.NotificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Main {@code Activity} class for the Camera app. */
public class CameraActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera); //changed
    if (null == savedInstanceState) {
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.container, Camera2BasicFragment.newInstance())
          .commit();
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.coupon)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
                dialog.cancel();
              }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
              }
            });
    final AlertDialog dialog = builder.create();

    beaconManager = new BeaconManager(this);
    beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
      @Override
      public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
        if (!list.isEmpty()) {
          Beacon nearestBeacon = list.get(0);
          final List<String> places = placesNearBeacon(nearestBeacon);
          dialog.show();
          /*showNotification(
                  "Nearest place:",
                  "" + places);*/
        }
      }
    });
    region = new BeaconRegion("ranged region",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
  }

  private BeaconManager beaconManager;
  private BeaconRegion region;
  private static final Map<String, List<String>> PLACES_BY_BEACONS;

  static {
    Map<String, List<String>> placesByBeacons = new HashMap<>();
    placesByBeacons.put("3092:15549", new ArrayList<String>() {{
      add("Apple");
    }});
    placesByBeacons.put("54104:7049", new ArrayList<String>() {{
      add("Banana");
    }});
    placesByBeacons.put("56662:48345", new ArrayList<String>() {{
      add("Pear");
    }});
    PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
  }

  private List<String> placesNearBeacon(Beacon beacon) {
    String beaconKey = String.format(Locale.ENGLISH, "%d:%d", beacon.getMajor(), beacon.getMinor());
    if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
      return PLACES_BY_BEACONS.get(beaconKey);
    }
    return Collections.emptyList();
  }

  @Override
  protected void onResume() {
    super.onResume();

    SystemRequirementsChecker.checkWithDefaultDialogs(this);

    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        beaconManager.startRanging(region);
      }
    });
  }

  @Override
  protected void onPause() {
    beaconManager.stopRanging(region);

    super.onPause();
  }

  public void showNotification(String title, String message) {
    Intent notifyIntent = new Intent(this, CameraActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
            new Intent[] {notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
    Notification notification = new Notification.Builder(this)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build();
    notification.defaults |= Notification.DEFAULT_SOUND;
    NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(1, notification);
  }
}
