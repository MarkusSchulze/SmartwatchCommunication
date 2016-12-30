package mi.hci.luh.de.smartwatchcommunication;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends WearableActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, SensorEventListener {

    long lastSendRotationData = 0;
    long lastSendAccData = 0;
    private BoxInsetLayout mContainerView;
    private TextView sensorX, sensorY, sensorZ;
    private boolean reset = false;
    private Sensor gameRotationSensor;
    private Sensor linearAccSensor;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor type : deviceSensors) {
            Log.e("sensors", type.getStringType());
        }
        gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR); //works
        sensorManager.registerListener(this, gameRotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); //works
        sensorManager.registerListener(this, linearAccSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Sensor gynoscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gynoscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        sensorX = (TextView) findViewById(R.id.sensor_X);
        sensorY = (TextView) findViewById(R.id.sensor_Y);
        sensorZ = (TextView) findViewById(R.id.sensor_Z);

        Button setMiddle = (Button) findViewById(R.id.BigButton);
        setMiddle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset = true;
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    @SuppressWarnings("deprecation")
    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            sensorY.setTextColor(getResources().getColor(android.R.color.white));
            sensorX.setTextColor(getResources().getColor(android.R.color.white));
            sensorZ.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mContainerView.setBackground(null);
            sensorY.setTextColor(getResources().getColor(android.R.color.black));
            sensorX.setTextColor(getResources().getColor(android.R.color.black));
            sensorZ.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    public void sendSensorData(String type, float x, float y, float z) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/SensorData");
        if (reset) {
            putDataMapRequest.getDataMap().putString("TYPE", "RESET");
            reset = false;
        } else {
            putDataMapRequest.getDataMap().putString("TYPE", type);
        }
        putDataMapRequest.getDataMap().putFloat("x", x);
        putDataMapRequest.getDataMap().putFloat("y", y);
        putDataMapRequest.getDataMap().putFloat("z", z);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        long millis = System.currentTimeMillis();
        //int seconds = (int) (millis / 1000);
        //int minutes = seconds / 60;
        //seconds = seconds % 60;

        float[] v = event.values;
        if (event.sensor == gameRotationSensor) {
            // Umrechnung der Sensordaten in brauchbare Werte
            // Werte gegen 1 geben ein schlechteres Sensorverhalten, deswegen der Welchsel zwischen den beiden SensorAchsen
            // Normierung von Werten zwischen -1 und 1 auf 0 bis 2
            // Dritter Versuch
            double grad;
            double x1 = 0;
            double x2 = 0;
            if (v[2] > 0 && v[3] > 0) {
                x1 = 1 - v[2];
                x2 = v[3];
            } else if (v[2] < 0 && v[3] > 0) {
                x1 = -v[2] + 1;
                x2 = (1 - v[3]) + 1;
            } else if (v[2] < 0 && v[3] < 0) {
                x1 = v[2] + 1;
                x2 = -v[3];
            } else if (v[2] > 0 && v[3] < 0) {
                x1 = v[2] + 1;
                x2 = v[3] + 2;
            }
            grad = (x1 + x2) / 2;

            // Erster Versuch
//            double grad = 0;
//            if (v[2] > 0 && v[3] > 0) {
//
//                grad = v[3];
//
//            } else if (v[2] < 0 && v[3] > 0) {
//
//                grad = (1 - v[3]) + 1;
//
//            } else if (v[2] < 0 && v[3] < 0) {
//                grad = -v[3];
//            } else if (v[2] > 0 && v[3] < 0) {
//                grad = v[3] + 2;
//            }

            //Zweiter Versuch
//            double grad = 0;
//            if (v[2] > 0 && v[3] > 0) {
//                if (v[3] > 0.5) {
//                    grad = 1 - v[2];
//                } else {
//                    grad = v[3];
//                }
//            } else if (v[2] < 0 && v[3] > 0) {
//                if (v[3] > 0.5) {
//                    grad = -v[2] + 1;
//                } else {
//                    grad = (1 - v[3]) + 1;
//                }
//            } else if (v[2] < 0 && v[3] < 0) {
//                if (v[3] < -0.5) {
//                    grad = v[2] + 1;
//                } else {
//                    grad = -v[3];
//                }
//            } else if (v[2] > 0 && v[3] < 0) {
//                if (v[3] < -0.5) {
//                    grad = v[2] + 1;
//                } else {
//                    grad = v[3] + 2;
//                }
//            }

            sensorX.setText("x: " + String.format("%.3f", v[3]));
            sensorY.setText("y: " + String.format("%.3f", grad * 0.5));
            sensorZ.setText("z: " + String.format("%.3f", v[2]));

            //Log.d("gettinDataRight", v[3] + " - " + String.valueOf(grad * 0.5) + " - " + v[2]);

            if (millis - lastSendRotationData > 100) {
                sendSensorData("GAME_ROTATION", v[0], -2*v[1], (float) (grad * -2));
                lastSendRotationData = millis;
            }
        } else if (event.sensor == linearAccSensor) {
            if (millis - lastSendAccData > 100) {
                //sendSensorData("LINEAR_ACC", v[0], v[1], v[2]);
                lastSendAccData = millis;
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
