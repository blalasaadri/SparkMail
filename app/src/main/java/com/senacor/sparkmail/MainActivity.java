package com.senacor.sparkmail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends AppCompatActivity {

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private static BaseProduct mProduct;

    private Handler mHandler;
    private List<String> missingPermissions = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);

    private Button takeOff;
    private Button setDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher that 22, please request teh following permissions at runtime to ensure the SDK works properly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }
        setContentView(com.senacor.sparkmail.R.layout.activity_main);
        // Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());

        takeOff = findViewById(R.id.takeOff);
        takeOff.setOnClickListener(new TakeOffButtonListener());

        setDown = findViewById(R.id.setDown);
        setDown.setOnClickListener(new SetDownButtonListener());
    }

    private void checkAndRequestPermissions() {
        for (String requiredPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(requiredPermission);
            }
        }
        if (missingPermissions.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    REQUEST_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove form missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.remove(permissions[i]);
                }
            }
        }
        // If there are enough permissions, we will start the registration
        if (missingPermissions.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                showToast("Registering, please wait...");
                DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            showToast("Register Success");
                            DJISDKManager.getInstance().startConnectionToProduct();
                        } else {
                            showToast("Registration of SDK failed, please check the bundle id and the network connection.");
                        }
                        Log.v(TAG, djiError.getDescription());
                    }

                    @Override
                    public void onProductDisconnect() {
                        notifyStatusChange();
                    }

                    @Override
                    public void onProductConnect(BaseProduct newProduct) {
                        mProduct = newProduct;
                        notifyStatusChange();
                    }

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
                        if (newComponent != null) {
                            newComponent.setComponentListener(isConnected -> notifyStatusChange());
                        }
                        notifyStatusChange();
                    }
                });
            });
        }
    }

    private void notifyStatusChange() {
        Runnable updateRunnable = () -> {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        };

        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private void showToast(final String toastMessage) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show());
    }

    public static FlightController getFlightController() {
        Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();

        if (aircraft != null) {
            return aircraft.getFlightController();
        }
        return null;
    }

    public void showShortToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private class TakeOffButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showShortToast("Take off");

            FlightController flightController = getFlightController();
            if (flightController == null) {
                showShortToast("Error while taking off :-(");
            } else {
                flightController.startTakeoff(djiError -> {
                    if (djiError == null) {
                        showShortToast("Success!");
                    } else {
                        showShortToast("Miserable failure :-( because: " + djiError.getDescription());
                    }
                });
            }
        }
    }

    private class SetDownButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showShortToast("Set down");

            FlightController flightController = getFlightController();
            if (flightController == null) {
                showShortToast("Error while setting down :-(");
            } else {
                flightController.startLanding(djiError -> {
                    if (djiError == null) {
                        showShortToast("Success!");
                    } else {
                        showShortToast("Miserable failure :-( because: " + djiError.getDescription());
                    }
                });
            }
        }
    }
}
