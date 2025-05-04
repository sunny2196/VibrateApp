package com.example.vibrateapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final double MAX_RELIABLE_DISTANCE = 10.0; // Maximum reliable distance in meters
    private static final long LOCATION_UPDATE_INTERVAL = 1000; // Update interval in milliseconds
    private static final long VIBRATION_DURATION = 500; // Vibration duration in milliseconds
    private static final int VIBRATION_AMPLITUDE = VibrationEffect.DEFAULT_AMPLITUDE;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView statusText;
    private TextView distanceText;
    private Button startButton;
    private Button vibrateButton;
    private Timer locationUpdateTimer;
    private Location lastKnownLocation;
    private boolean isTracking = false;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        distanceText = findViewById(R.id.distanceText);
        startButton = findViewById(R.id.scanButton);
        vibrateButton = findViewById(R.id.vibrateButton);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request necessary permissions
        requestPermissions();

        startButton.setOnClickListener(v -> {
            if (!isTracking) {
                startLocationTracking();
            } else {
                stopLocationTracking();
            }
        });

        vibrateButton.setOnClickListener(v -> {
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrateDevice();
            }
        });

        // Initialize location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastKnownLocation = location;
                updateDistance();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationTracking() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isTracking = true;
            startButton.setText("Stop Tracking");
            vibrateButton.setEnabled(true);
            statusText.setText("Tracking location...");

            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    1,
                    locationListener
            );

            // Start timer for periodic distance updates
            locationUpdateTimer = new Timer();
            locationUpdateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateDistance();
                }
            }, 0, LOCATION_UPDATE_INTERVAL);
        }
    }

    private void stopLocationTracking() {
        isTracking = false;
        startButton.setText("Start Tracking");
        vibrateButton.setEnabled(false);
        statusText.setText("Tracking stopped");
        
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
            locationUpdateTimer = null;
        }
    }

    private void updateDistance() {
        if (lastKnownLocation != null) {
            // In a real implementation, you would send this location to a server
            // and receive the other device's location to calculate the distance
            // For now, we'll simulate a distance calculation
            double simulatedDistance = Math.random() * 15.0; // Simulated distance in meters
            
            runOnUiThread(() -> {
                if (simulatedDistance > MAX_RELIABLE_DISTANCE) {
                    distanceText.setText(String.format("Distance: %.2f meters (Out of reliable range)", simulatedDistance));
                    vibrateButton.setEnabled(false);
                } else {
                    distanceText.setText(String.format("Distance: %.2f meters", simulatedDistance));
                    vibrateButton.setEnabled(true);
                    // Trigger vibration when devices are close
                    if (simulatedDistance < 5.0) {
                        vibrateDevice();
                    }
                }
            });
        }
    }

    private void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VIBRATION_AMPLITUDE));
            } else {
                vibrator.vibrate(VIBRATION_DURATION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationTracking();
    }
} 