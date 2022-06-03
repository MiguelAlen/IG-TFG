package org.altbeacon.beaconreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.altbeacon.activities.MainActivity;
import org.altbeacon.activities.SignInActivity;
import org.altbeacon.activities.TaskActivity;
import org.altbeacon.utilities.Constants;
import org.altbeacon.utilities.PreferenceManager;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.HashMap;

import okhttp3.OkHttpClient;

/**
 *
 * @author dyoung
 * @author Matt Tyler
 */
public class MonitoringActivity extends Activity implements MonitorNotifier {
	protected static final String TAG = "MonitoringActivity";
	private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

	private final OkHttpClient client = new OkHttpClient();
	Context context;
	private PreferenceManager preferenceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		requestPermissions();

		BeaconManager.getInstanceForApplication(this).startMonitoring(BeaconReferenceApplication.wildcardRegion);

		BeaconManager.getInstanceForApplication(this).addMonitorNotifier(this);
		context = this;
		preferenceManager = new PreferenceManager(getApplicationContext());

		TextView tusername, tuserstaff, tusermail;

		tusername = findViewById(R.id.userTV);
		tuserstaff = findViewById(R.id.staffTV);
		tusermail = findViewById(R.id.mailTV);
		ImageView tuserimage = findViewById(R.id.userImage);

		tusername.setText(preferenceManager.getString(Constants.KEY_NAME));
		tusermail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
		tuserstaff.setText(preferenceManager.getString(Constants.KEY_DEPARTMENT));

		byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		tuserimage.setImageBitmap(bitmap);

		ImageButton chat = findViewById(R.id.btnChat);

		chat.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
			}
		});

		ImageButton task = findViewById(R.id.btnTask);

		task.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	public void didEnterRegion(Region region) {
		//logToDisplay("didEnterRegion called");
		}
	@Override
	public void didExitRegion(Region region) {
		//logToDisplay("didExitRegion called");
	}
	@Override
	public void didDetermineStateForRegion(int state, Region region) {
		//logToDisplay("didDetermineStateForRegion called with state: " + (state == 1 ? "INSIDE ("+state+")" : "OUTSIDE ("+state+")"));
	}

	private void requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("This app needs background location access");
							builder.setMessage("Please grant location access so this app can detect beacons in the background.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@TargetApi(23)
								@Override
								public void onDismiss(DialogInterface dialog) {
									requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
											PERMISSION_REQUEST_BACKGROUND_LOCATION);
								}

							});
							builder.show();
						}
						else {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("Functionality limited");
							builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
								}

							});
							builder.show();
						}
					}
				}
			} else {
				if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
							PERMISSION_REQUEST_FINE_LOCATION);
				}
				else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}

			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_FINE_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "fine location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
						}
					});
					builder.show();
				}
				return;
			}
			case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					//Log.d(TAG, "background location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
						}
					});
					builder.show();
				}
				return;
			}
		}
	}

	public void onLogOut(View view) {

		ImageButton button = (ImageButton) findViewById(R.id.btnLogOut);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Exit Application?");
		alertDialogBuilder
				.setMessage("Click yes to exit!")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								FirebaseFirestore database = FirebaseFirestore.getInstance();
								DocumentReference documentReference =
										database.collection(Constants.KEY_COLLECTION_USERS).document(
											preferenceManager.getString(Constants.KEY_USER_ID)
										);
								HashMap<String, Object> updates = new HashMap<>();
								updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
								documentReference.update(updates)
										.addOnSuccessListener(unused -> {
											preferenceManager.clear();
											startActivity(new Intent(getApplicationContext(), SignInActivity.class));
											finish();
										})
										.addOnFailureListener(e -> showToast("Unable to Sing Out"));
								//moveTaskToBack(true);
								//android.os.Process.killProcess(android.os.Process.myPid());
								//System.exit(1);
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	/*public void onEnableClicked(View view) {
		// This is a toggle.  Each time we tap it, we start or stop
		Button button = (Button) findViewById(R.id.enableButton);
		if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
			BeaconManager.getInstanceForApplication(this).stopMonitoring(BeaconReferenceApplication.wildcardRegion);
			button.setText("Enable Monitoring");
		}
		else {
			BeaconManager.getInstanceForApplication(this).startMonitoring(BeaconReferenceApplication.wildcardRegion);
			button.setText("Disable Monitoring");
		}
	}*/
	private void verifyBluetooth() {
		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finishAffinity();
					}
				});
				builder.show();
			}
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finishAffinity();
				}

			});
			builder.show();

		}
	}
	/*private String cumulativeLog = "";
	private void logToDisplay(String line) {
		cumulativeLog += line+"\n";
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.setText(cumulativeLog);
    	    }
    	});
    }*/
	private void showToast(String message){
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
