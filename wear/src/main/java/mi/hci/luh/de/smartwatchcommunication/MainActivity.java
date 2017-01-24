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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.List;

public class MainActivity extends WearableActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, SensorEventListener {

    private static final float NS2S = 1.0f / 1000000000.0f;
    long lastSendRotationData = 0;
    long lastSendAccData = 0;
    private BoxInsetLayout mContainerView;
    private TextView sensorX, sensorY, sensorZ;
    private boolean reset = false, click = false;
    private Sensor gameRotationSensor;
    private Sensor linearAccSensor;
    private GoogleApiClient mGoogleApiClient;
    private float timestamp;

    private float[] mRotationMatrix = new float[16];

    private float[] mOrientation = new float[9];

    private float[] history = new float[2];

    private float mYaw;
    private float mPitch;
    private float mRoll;

    private float x = 0.000f;
    private float y = 0.000f;
    private Timestamp lastLinAccTime;
    private float[] acc;
    private float accY = 0.000f;
    private float accZ = 0.000f;
    private float Velocity = 0.000f;
    private float distX = 0.000f;
    private float dT = 0;
    private double omegaMagnitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor type : deviceSensors) {
            Log.e("sensors", type.getStringType());
        }
        gameRotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        sensorManager.registerListener(this, gameRotationSensor, 20000);  // 20000 mikro sec. ~ 50Hz ~ SENSOR_DELAY_GAME

        linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, linearAccSensor, 200); // 20000 mikro sec. ~ 50Hz ~ SENSOR_DELAY_GAME

        //Sensor gynoscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //sensorManager.registerListener(this, gynoscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        sensorX = (TextView) findViewById(R.id.sensor_X);
        sensorY = (TextView) findViewById(R.id.sensor_Y);
        sensorZ = (TextView) findViewById(R.id.sensor_Z);

        Button setMiddle = (Button) findViewById(R.id.BigButton);
        setMiddle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reset = true;
            }
        });
        Button clickButton = (Button) findViewById(R.id.Click);
        clickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                click = true;
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

    public void sendSensorData(String type, float x, float y, float[] acc, float[] rotMatirx, float roll) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/SensorData");
        if (reset) {
            putDataMapRequest.getDataMap().putString("TYPE", "RESET");
            reset = false;
        }
        else if (click) {
            putDataMapRequest.getDataMap().putString("TYPE", "CLICK");
            click = false;
        }
        else {
            putDataMapRequest.getDataMap().putString("TYPE", type);
        }
        putDataMapRequest.getDataMap().putFloat("x", x);
        putDataMapRequest.getDataMap().putFloat("y", y);
        putDataMapRequest.getDataMap().putFloatArray("acc", acc);
        putDataMapRequest.getDataMap().putFloatArray("rot", rotMatirx);
        putDataMapRequest.getDataMap().putFloat("roll", roll);

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

            //Log.d("gettinDataRight", v[3] + " - " + String.valueOf(grad * 0.5) + " - " + v[2]);


            // see: http://stackoverflow.com/questions/23658572/game-rotation-vector-sensor
            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y,
                    SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, mOrientation);

            mPitch = (float) Math.toDegrees(mOrientation[1]); // Pitch is the rotations about the y axis  (between -90 and 90 deg);
            mYaw = (float) Math.toDegrees(mOrientation[0]); // Yaw is the rotation about the z axis (between -180 and 180).
            mRoll = (float) Math.toDegrees(mOrientation[2]);

            float yDelta = history[0] - mPitch;
            float zDelta = history[1] - mYaw; // Currently unused

            history[0] = mPitch;
            history[1] = mYaw;

            // Make sure value is between 0-360
            mYaw = mod(mYaw, 360.0f);


            if (millis - lastSendRotationData > 100) {
                /*float x = v[0];
                float y = -2*v[1];
                float z = (float) (grad * -2);*/

                /*float x = v[0];
                float y = v[1];
                float z = v[2];*/

                x = mYaw;
                y = mPitch;
                //z = 0.0f;

                sensorX.setText("x: " + String.format("%.3f", x));
                sensorY.setText("y: " + String.format("%.3f", y));
                sensorZ.setText("z: " + String.format("%.3f", acc[0]));

                sendSensorData("GAME_ROTATION", x / 360, y / 90, acc, mRotationMatrix, mRoll);
                lastSendRotationData = millis;
            }

        } else if (event.sensor == linearAccSensor) {
            //if (millis - lastSendAccData > 100) {
            //sendSensorData("LINEAR_ACC", v[0], v[1], v[2]);

            acc = event.values;
            sendSensorData("GAME_ROTATION", x / 360, y / 90, acc, mRotationMatrix, mRoll);

            //Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));
            //lastSendAccData = millis;
            //}
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private float mod(float x, float y) {
        float result = x % y;
        if (result < 0)
            result += y;
        return result;
    }
}
