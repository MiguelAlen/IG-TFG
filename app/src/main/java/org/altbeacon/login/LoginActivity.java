package org.altbeacon.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beaconreference.BeaconReferenceApplication;
import org.altbeacon.beaconreference.MonitoringActivity;
import org.altbeacon.beaconreference.R;
import org.altbeacon.utilities.Constants;
import org.altbeacon.utilities.PreferenceManager;

import okhttp3.OkHttpClient;

public class LoginActivity extends Activity  {

    /*private ApolloClient apolloClient;
    private static final String BASE_URL = "https://";

    Button buttonLogin, buttonResetPassword;
    EditText eUser, ePassword;

    ImageButton kill;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(getApplicationContext());

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            .build();

        buttonLogin = (Button)findViewById(R.id.btnLogin);
        buttonResetPassword = (Button)findViewById(R.id.btnReset);
        kill = (ImageButton)findViewById(R.id.btnLogOut);

        eUser = (EditText)findViewById(R.id.loginEntry);
        ePassword = (EditText)findViewById(R.id.passwordEntry);

        Context context = this;

        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MonitoringActivity.class);
            startActivity(intent);
            finish();
        }

        BeaconManager.getInstanceForApplication(context).stopMonitoring(BeaconReferenceApplication.wildcardRegion);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user = eUser.getText().toString();
                String pwd = ePassword.getText().toString();

                apolloClient.mutate(new LoginMutation(user, pwd))
                        .enqueue(new ApolloCall.Callback<LoginMutation.Data>() {

                            @Override
                            public void onResponse(@NonNull Response<LoginMutation.Data> response) {

                                if(response.getData().tokenAuth().success()){
                                    runOnUiThread(new Runnable() {
                                          public void run() {
                                              Toast.makeText(context, "Login: Succes" , Toast.LENGTH_LONG).show();
                                          }
                                      }
                                    );
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, Boolean.TRUE);
                                    preferenceManager.putString(Constants.KEY_FCM_TOKEN, response.getData().tokenAuth().token());
                                    preferenceManager.putString(Constants.KEY_NAME, response.getData().tokenAuth().user().username());
                                    preferenceManager.putString(Constants.KEY_EMAIL, response.getData().tokenAuth().user().email());
                                    preferenceManager.putString(Constants.KEY_USER_ID, response.getData().tokenAuth().user().id());
                                    if(!response.getData().tokenAuth().user().department().name().equals(null)){
                                        preferenceManager.putString(Constants.KEY_DEPARTMENT, response.getData().tokenAuth().user().department().name());
                                    }else{
                                        preferenceManager.putString(Constants.KEY_DEPARTMENT, "---");

                                    }
                                    BeaconManager.getInstanceForApplication(context).startMonitoring(BeaconReferenceApplication.wildcardRegion);

                                    Intent intent = new Intent(getApplicationContext(), MonitoringActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    runOnUiThread(new Runnable() {
                                          public void run() {
                                              Toast.makeText(context, "Failed to login", Toast.LENGTH_LONG).show();
                                          }
                                      }
                                    );
                                }
                            }
                            @Override
                            public void onFailure(@NonNull ApolloException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                );
            }
        });

        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Exit Application?");
                alertDialogBuilder
                        .setMessage("Click yes to exit!")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        BeaconManager.getInstanceForApplication(context).stopMonitoring(BeaconReferenceApplication.wildcardRegion);
                                        moveTaskToBack(true);
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);
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
        });

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }*/
}
