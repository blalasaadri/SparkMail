package com.senacor.sparkmail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.senacor.sparkmail.DjiSdkInitializationUtils.checkAndRequestPermissionsForDjiSdk;
import static com.senacor.sparkmail.DjiSdkInitializationUtils.registerDjiSdk;
import static com.senacor.sparkmail.SystemUtils.isAndroidApiVersion22OrHigher;

public class MainActivity extends AppCompatActivity {

    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";

    private Handler mHandler;

    @BindView(R.id.takeOff)
    Button takeOff;
    @BindView(R.id.setDown)
    Button setDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher that 22, please request the following permissions at runtime to ensure the SDK works properly.
        if (isAndroidApiVersion22OrHigher()) {
            checkAndRequestPermissionsForDjiSdk(this, this::notifyStatusChange);
        }
        setContentView(com.senacor.sparkmail.R.layout.activity_main);
        // Initialize DJI SDK Manager
        mHandler = new Handler(Looper.getMainLooper());

        // Initialize view fields
        ButterKnife.bind(this);

        takeOff.setOnClickListener(new TakeOffButtonListener(this));
        setDown.setOnClickListener(new SetDownButtonListener(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        registerDjiSdk(requestCode, permissions, grantResults, this::notifyStatusChange, getApplicationContext());
    }

    private void notifyStatusChange() {
        Runnable updateRunnable = () -> {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        };

        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

}
