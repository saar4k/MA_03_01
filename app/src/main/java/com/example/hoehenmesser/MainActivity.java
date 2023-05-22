package com.example.hoehenmesser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private float calibratedPressure = 1013.25f;
    private boolean manualMode = false;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String CALIBRATED_PRESSURE_KEY = "calibrated_pressure";
    private static final String MANUAL_MODE_KEY = "manual_mode";
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        calibratedPressure = sharedPreferences.getFloat(CALIBRATED_PRESSURE_KEY, 1012.25f);
        manualMode = sharedPreferences.getBoolean(MANUAL_MODE_KEY, false);


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        SeekBar calibrationSlider = findViewById(R.id.calibration_slider);
        TextView calibration_value = findViewById(R.id.calibration_value);
        Button modeButton = findViewById(R.id.mode_button);


        // Setzen der SeekBar-Position basierend auf dem gespeicherten kalibrierten Druck
        calibrationSlider.setProgress((int) calibratedPressure);

        modeButton.setOnClickListener(v -> {
            manualMode = !manualMode;
            if (manualMode) {
                modeButton.setText("Automatisch");
                modeButton.setBackgroundColor(Color.parseColor("#BB86FC"));
                calibrationSlider.getProgressDrawable().setColorFilter(Color.parseColor("#BB86FC"), PorterDuff.Mode.SRC_IN);
                calibrationSlider.getThumb().setColorFilter(Color.parseColor("#BB86FC"), PorterDuff.Mode.SRC_IN);
            } else {
                modeButton.setText("Manuell");
                modeButton.setBackgroundColor(Color.parseColor("#ff964f"));
                calibrationSlider.getProgressDrawable().setColorFilter(Color.parseColor("#ff964f"), PorterDuff.Mode.SRC_IN);
                calibrationSlider.getThumb().setColorFilter(Color.parseColor("#ff964f"), PorterDuff.Mode.SRC_IN);
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(MANUAL_MODE_KEY, manualMode);
            editor.apply();
        });

        SensorEventListener pressureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Umrechnung von Luftdruck in Höhe mit der Internationalen Höhenformel
                float pressure = event.values[0];
                float altitude;

                if (manualMode) {
                    altitude = (float) ((288.15 / 0.0065) * (1.0 - Math.pow(calibratedPressure / 1013.25f, 0.1903)));
                } else {
                    altitude = (float) ((288.15 / 0.0065) * (1.0 - Math.pow(pressure / 1013.25f, 0.1903)));
                }


                updateAltitude(altitude);
                updatePressure(calibratedPressure);

                if (!manualMode) {
                    // Aktualisieren der SeekBar-Position basierend auf dem aktuellen Druckwert des Sensors
                    int progress = (int) pressure;
                    calibrationSlider.setProgress(progress);

                    // Aktualisieren des Kalibrierungswerts in der TextView
                    calibration_value.setText(String.format(Locale.getDefault(), "%.0f hPa", pressure));
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };


        calibrationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                calibratedPressure = progress;
                calibration_value.setText(String.format(Locale.getDefault(), "%.0f hPa", calibratedPressure));

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat(CALIBRATED_PRESSURE_KEY, calibratedPressure);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sensorManager.registerListener(pressureListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Initialisierung der UI-Komponenten basierend auf dem gespeicherten Zustand
        if (manualMode) {
            modeButton.setText("Automatisch");
            modeButton.setBackgroundColor(Color.parseColor("#BB86FC"));
            calibrationSlider.getProgressDrawable().setColorFilter(Color.parseColor("#BB86FC"), PorterDuff.Mode.SRC_IN);
            calibrationSlider.getThumb().setColorFilter(Color.parseColor("#BB86FC"), PorterDuff.Mode.SRC_IN);
        } else {
            modeButton.setText("Manuell");
            modeButton.setBackgroundColor(Color.parseColor("#ff964f"));
            calibrationSlider.getProgressDrawable().setColorFilter(Color.parseColor("#ff964f"), PorterDuff.Mode.SRC_IN);
            calibrationSlider.getThumb().setColorFilter(Color.parseColor("#ff964f"), PorterDuff.Mode.SRC_IN);
        }
    }

    private void updateAltitude(float altitude) {
        // Aktualisieren der View mit der berechneten Höhe
        TextView altitudeView = findViewById(R.id.altitude_view);
        altitudeView.setText(String.format(Locale.getDefault(), "%.2f m", altitude));
    }

    private void updatePressure(float pressure) {
        // Aktualisieren des Drucks in der TextView
        TextView calibratedValue = findViewById(R.id.calibration_value);
        calibratedValue.setText(String.format(Locale.getDefault(), "%.0f hPa", pressure));
    }
}