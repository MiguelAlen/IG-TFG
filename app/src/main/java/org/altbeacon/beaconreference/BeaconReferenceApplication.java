package org.altbeacon.beaconreference;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import org.altbeacon.login.LoginActivity;
import org.altbeacon.utilities.Constants;
import org.altbeacon.utilities.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BeaconReferenceApplication extends Application implements MonitorNotifier {

    private static final String TAG = "BeaconReferenceApp";

    private final OkHttpClient client = new OkHttpClient();

    public static final Region wildcardRegion = new Region("wildcardRegion", null, null, null);
    public static boolean insideRegion = false;

    private static List<String> validNames = new ArrayList<>(Arrays.asList("Kontakt", "abeacon", "ETEBEA", "DSD TECH"));

    private PreferenceManager preferenceManager;

    public boolean hasBeaconName(String currentBeacon, List<String> beaconNames) {
        for(String keyword : beaconNames){
            if(currentBeacon.contains(keyword)){
                return true;
            }
        }
        return false;
    }

    public void sendPostRequestJson(OkHttpClient client,String userName ,String macAddress, int rssi, String avg_rssi, int txPower) {

        System.out.println("Beacon found: " + macAddress);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> beaconData = new HashMap<>();
        beaconData.put("User", userName);
        beaconData.put("Mac", macAddress);
        beaconData.put("rssi", rssi);
        beaconData.put("avg_rssi", avg_rssi);
        beaconData.put("power", txPower);
        database.collection("beacons").add(beaconData);
        /*JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user", userName);
            jsonObject.put("mac", macAddress.replace(":", ""));
            jsonObject.put("rssi", String.valueOf(rssi));
            jsonObject.put("avg_rssi", avg_rssi.replace(",", "."));
            jsonObject.put("tx", String.valueOf(txPower));
        } catch (JSONException e) {
            System.out.println("This is the exception for " + macAddress);
            e.printStackTrace();
        }


        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url("https:")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        System.out.println("Unexpected code " + response);
                    throw new IOException("Unexpected code " + response);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });*/
    }

    public void onCreate() {

        preferenceManager = new PreferenceManager(getApplicationContext());

        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        // To find a different type of beacon, you must specify the byte layout for that beacon's

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"));

        beaconManager.setDebug(true);

        // Uncomment the code below to use backgroud in exchange for showing an icon
        // at the top of the screen and a always-on notification

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.logo_rm);
        builder.setContentTitle("Press to open");

        Intent intent= new Intent(this, LoginActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(650);
        beaconManager.setForegroundBetweenScanPeriod(0);
        beaconManager.setForegroundScanPeriod(650);


        Log.d(TAG, "setting up background monitoring in app onCreate");
        beaconManager.addMonitorNotifier(this);

        for (Region region : beaconManager.getMonitoredRegions()) {
            beaconManager.stopMonitoring(region);
        }

        beaconManager.startMonitoring(wildcardRegion);

        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    //Beacon currentBeacon = beacons.iterator().next();
                    Iterator iterator = beacons.iterator();
                    while (iterator.hasNext()){
                        Beacon currentBeacon = (Beacon) iterator.next();
                        System.out.println("MAC1:" + currentBeacon.getBluetoothAddress());

                        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
                            if (currentBeacon.getBluetoothName() != null) {
                                if (hasBeaconName(currentBeacon.getBluetoothName(), validNames)) {
                                    sendPostRequestJson(client, preferenceManager.getString(Constants.KEY_NAME),currentBeacon.getBluetoothAddress(), currentBeacon.getRssi(), (String) String.format("%.2f", currentBeacon.getRunningAverageRssi()), currentBeacon.getTxPower());
                                }
                            } else {
                                System.out.println("the problem is caused by " + currentBeacon.getBluetoothAddress());
                                sendPostRequestJson(client, preferenceManager.getString(Constants.KEY_NAME),currentBeacon.getBluetoothAddress(), currentBeacon.getRssi(), (String) String.format("%.2f", currentBeacon.getRunningAverageRssi()), currentBeacon.getTxPower());
                            }
                        }
                        // currentBeacon.getRunningAverageRssi()
                        // currentBeacon.getRssi()
                    }
                }
            }

        };
        beaconManager.addRangeNotifier(rangeNotifier);
        beaconManager.startRangingBeacons(BeaconReferenceApplication.wildcardRegion);
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "did enter region.");
        insideRegion = true;
        // Send a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "Sending notification.");
        //sendNotification();
    }

    @Override
    public void didExitRegion(Region region) {
        insideRegion = false;
        // do nothing here. logging happens in MonitoringActivity
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        // do nothing here. logging happens in MonitoringActivity
    }

    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Beacon Reference Notifications",
                    "Beacon Reference Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channel.getId());
        }
        else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        builder.setSmallIcon(R.drawable.logo_rm);
        builder.setContentTitle("I detect a beacon");
        builder.setContentText("Tap here to see details in the reference app");
        notificationManager.notify(1, builder.build());
    }
}
