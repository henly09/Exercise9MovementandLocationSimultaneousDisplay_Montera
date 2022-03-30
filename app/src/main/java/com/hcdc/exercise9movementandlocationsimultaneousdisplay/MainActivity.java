package com.hcdc.exercise9movementandlocationsimultaneousdisplay;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    TextView dataviewmain;
    Sensor tiltSensor;
    SensorManager tiltManager;
    ToggleButton swap;
    BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataviewmain = findViewById(R.id.dataview);
        swap = findViewById(R.id.toggleButton);
        tiltManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        tiltSensor = tiltManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener sel = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                dataviewmain.setText("X: " + sensorEvent.values[0] + "\nY: " + sensorEvent.values[1] + "\nZ: " + sensorEvent.values[2] + "\n");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        swap.setOnClickListener(view -> {
            if (!swap.isChecked()) {
                tiltManager.registerListener(sel, tiltSensor, tiltManager.SENSOR_DELAY_NORMAL);
            }
            if (swap.isChecked()) {
                tiltManager.unregisterListener(sel);
                dataviewmain.setText("Wait Please!");
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        Intent intent = new Intent(getBaseContext(), GPSService.class);
        if (getSystemService(GPSService.class) != null) {
            stopService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(getBaseContext(), GPSService.class);
        if (getSystemService(GPSService.class) == null) {
            startService(intent);
        }
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    dataviewmain.setText("Coordinates: " +
                            "\nLatitude: " + intent.getDoubleExtra("latitude", 0) +
                            "\nLongitude: " + intent.getDoubleExtra("longitude", 0) +
                            "\nAltitude: " + intent.getDoubleExtra("altitude", 0) +
                            "\nProvider: " + intent.getStringExtra("provider"));
                }
            };
        }
        registerReceiver(receiver, new IntentFilter("updateLocation"));
    }
}