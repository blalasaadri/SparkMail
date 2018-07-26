package com.senacor.sparkmail;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

import static com.senacor.sparkmail.SystemUtils.isAndroidApiVersion22OrHigher;
import static com.senacor.sparkmail.SystemUtils.showLongToast;
import static com.senacor.sparkmail.SystemUtils.showShortToast;

final class DjiSdkInitializationUtils {

    private static final String TAG = MainActivity.class.getName();
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
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private static AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static List<String> missingPermissions = new ArrayList<>();

    static void registerDjiSdk(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Runnable notifyStatusChange, Context context) {
        // Check for granted permission and remove form missing list
        if (requestCode == DjiSdkInitializationUtils.REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.remove(permissions[i]);
                }
            }
        }
        // If there are enough permissions, we will start the registration
        if (missingPermissions.isEmpty()) {
            DjiSdkInitializationUtils.startSDKRegistration(context, notifyStatusChange);
        } else {
            showLongToast("Missing permissions!!!", context);
        }
    }

    static void checkAndRequestPermissionsForDjiSdk(Activity activity, Runnable notifyStatusChange) {
        for (String requiredPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(requiredPermission);
            }
        }
        if (missingPermissions.isEmpty()) {
            startSDKRegistration(activity.getApplicationContext(), notifyStatusChange);
        } else if (isAndroidApiVersion22OrHigher()) {
            showLongToast("Need to grant the permissions!", activity.getApplicationContext());
            ActivityCompat.requestPermissions(
                    activity,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    REQUEST_PERMISSION_CODE
            );
        }
    }

    private static void startSDKRegistration(Context context, Runnable notifyStatusChange) {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                showLongToast("Registering, please wait...", context);
                DJISDKManager.getInstance().registerApp(context, new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            showShortToast("Register Success", context);
                            DJISDKManager.getInstance().startConnectionToProduct();
                        } else {
                            showLongToast("Registration of SDK failed, please check the bundle id and the network connection.", context);
                        }
                        Log.v(TAG, djiError.getDescription());
                    }

                    @Override
                    public void onProductDisconnect() {
                        notifyStatusChange.run();
                    }

                    @Override
                    public void onProductConnect(BaseProduct newProduct) {
                        notifyStatusChange.run();
                    }

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {
                        if (newComponent != null) {
                            newComponent.setComponentListener(isConnected -> notifyStatusChange.run());
                        }
                        notifyStatusChange.run();
                    }
                });
            });
        }
    }
}
