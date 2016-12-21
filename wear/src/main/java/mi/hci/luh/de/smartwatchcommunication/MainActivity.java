package mi.hci.luh.de.smartwatchcommunication;

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
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearableActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, SensorEventListener {

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    //private TextView mClockView;
    private TextView sensorX, sensorY, sensorZ;

    private SensorManager sensorManager;
    private Sensor gameRotationSensor, linearAccSensor;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        sensorManager.registerListener(this, gameRotationSensor, 100 * 1000);

        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, linearAccSensor, 100 * 1000);

        sensorX = (TextView) findViewById(R.id.sensor_X);
        sensorY = (TextView) findViewById(R.id.sensor_Y);
        sensorZ = (TextView) findViewById(R.id.sensor_Z);

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

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            sensorY.setTextColor(getResources().getColor(android.R.color.white));
            sensorX.setTextColor(getResources().getColor(android.R.color.white));
            sensorZ.setTextColor(getResources().getColor(android.R.color.white));
            //mClockView.setVisibility(View.VISIBLE);

            //mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            sensorY.setTextColor(getResources().getColor(android.R.color.black));
            sensorX.setTextColor(getResources().getColor(android.R.color.black));
            sensorZ.setTextColor(getResources().getColor(android.R.color.black));
            //mClockView.setVisibility(View.GONE);
        }
    }

    public void sendSensorData(String type, float x, float y, float z) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/SensorData");
        putDataMapRequest.getDataMap().putString("TYPE", type);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        long millis = System.currentTimeMillis();
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        float[] v = event.values;
        if (event.sensor == gameRotationSensor) {
            sensorX.setText(String.format("%.3f", v[0]));
            sensorY.setText(String.format("%.3f", v[1]));
            sensorZ.setText(String.format("%.3f", v[2]));
            if (millis - lastSendRosationData > 500){
                sendSensorData("GAME_ROTATION", v[0], v[1], v[2]);
                lastSendRosationData = millis;
            }

            //Log.d("SendData", String.valueOf(vx[0]));
        } else if (event.sensor == linearAccSensor) {
            if (millis - lastSendAccData > 500){
                sendSensorData("LINEAR_ACC", v[0], v[1], v[2]);
                lastSendAccData = millis;
            }

            //Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
