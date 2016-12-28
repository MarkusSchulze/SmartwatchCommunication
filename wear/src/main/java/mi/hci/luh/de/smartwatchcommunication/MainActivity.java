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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, SensorEventListener {

    private BoxInsetLayout mContainerView;
    private TextView sensorX, sensorY, sensorZ;
    private Button setMiddle;
    private boolean reset = false;

    private Sensor gameRotationSensor, linearAccSensor;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        sensorManager.registerListener(this, gameRotationSensor, 100 * 1000);
        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, linearAccSensor, 100 * 1000);

        sensorX = (TextView) findViewById(R.id.sensor_X);
        sensorY = (TextView) findViewById(R.id.sensor_Y);
        sensorZ = (TextView) findViewById(R.id.sensor_Z);

        setMiddle = (Button) findViewById(R.id.BigButton);
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
        if (reset){
            putDataMapRequest.getDataMap().putString("TYPE", "RESET");
            reset = false;
        }else{
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

    long lastSendRosationData = 0;
    long lastSendAccData = 0;

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        long millis = System.currentTimeMillis();
        //int seconds = (int) (millis / 1000);
        //int minutes = seconds / 60;
        //seconds = seconds % 60;

        float[] v = event.values;
        if (event.sensor == gameRotationSensor) {
            sensorX.setText(String.format("%.3f", v[0]));
            sensorY.setText(String.format("%.3f", v[1]));
            sensorZ.setText(String.format("%.3f", v[2]));
            if (millis - lastSendRosationData > 100){
                sendSensorData("GAME_ROTATION", v[0], v[1], v[2]);
                lastSendRosationData = millis;
            }
        } else if (event.sensor == linearAccSensor) {
            if (millis - lastSendAccData > 100){
                sendSensorData("LINEAR_ACC", v[0], v[1], v[2]);
                lastSendAccData = millis;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
