package org.altbeacon.beaconreference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


import org.altbeacon.utilities.Constants;
import org.altbeacon.utilities.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;


public class RangingActivity extends Activity {

    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private static List<String> validNames = new ArrayList<>(Arrays.asList("Kontakt", "abeacon", "ETEBEA", "DSD TECH"));
    private final OkHttpClient client = new OkHttpClient();

    PreferenceManager preferenceManager;

    public boolean hasBeaconName(String currentBeacon, List<String> beaconNames) {
        for(String keyword : beaconNames){
            if(currentBeacon.contains(keyword)){
                return true;
            }
        }
        return false;
    }

    public void sendPostRequestJson(OkHttpClient client,String userName ,String macAddress, int rssi, int txPower) {
        System.out.println("Beacon found: " + macAddress);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> beaconData = new HashMap<>();
        beaconData.put("User", userName);
        beaconData.put("Mac", macAddress);
        beaconData.put("rssi", rssi);
        //beaconData.put("avg_rssi", avg_rssi);
        beaconData.put("power", txPower);
        database.collection("beacons").add(beaconData);
        /*JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user", userName);
            jsonObject.put("mac", macAddress.replace(":", ""));
            jsonObject.put("rssi", String.valueOf(rssi));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(R.layout.activity_ranging);

    }

    @Override
    protected void onResume() {
        super.onResume();

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
                                    sendPostRequestJson(client,preferenceManager.getString(Constants.KEY_NAME) ,currentBeacon.getBluetoothAddress(), currentBeacon.getRssi(), currentBeacon.getTxPower());
                                }
                            } else {

                                System.out.println("the problem is caused by " + currentBeacon.getBluetoothAddress());
                                sendPostRequestJson(client,preferenceManager.getString(Constants.KEY_NAME),currentBeacon.getBluetoothAddress(), currentBeacon.getRssi(), currentBeacon.getTxPower());
                            }
                        }

                        //	getRunningAverageRssi()
                    }
                }
            }

        };

        beaconManager.addRangeNotifier(rangeNotifier);
        beaconManager.startRangingBeacons(BeaconReferenceApplication.wildcardRegion);
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.stopRangingBeacons(BeaconReferenceApplication.wildcardRegion);
        beaconManager.removeAllRangeNotifiers();
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                editText.append(line+"\n");
            }
        });
    }
}
