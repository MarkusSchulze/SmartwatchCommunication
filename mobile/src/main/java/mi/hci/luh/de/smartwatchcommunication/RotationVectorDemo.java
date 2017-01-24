package mi.hci.luh.de.smartwatchcommunication;
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.sql.Timestamp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Wrapper activity demonstrating the use of the new
 * {@link SensorEvent#values rotation vector sensor}
 * ({@link Sensor#TYPE_ROTATION_VECTOR TYPE_ROTATION_VECTOR}).
 *
 * @see Sensor
 * @see SensorEvent
 * @see SensorManager
 */
public class RotationVectorDemo extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final float NS2S = 1.0f / 1000000000.0f;
    private GLSurfaceView mGLSurfaceView;
    private SensorManager mSensorManager;
    private MyRenderer mRenderer;
    private String lastDataType;
    private float[] lastData = new float[16];
    private float[] mRotationMatrix = new float[16];
    private com.google.android.gms.common.api.GoogleApiClient mGoogleApiClient;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private float accXRaw = 0.000f;
    private float tiefe = 0.3f;
    private Timestamp lastLinAccTime;
    private float accX = 0.000f;
    private float accY = 0.000f;
    private float accZ = 0.000f;
    private float distX = 0.000f;
    private int direction = 0;
    private double Velocity_Old = 0.0000f;
    private double Velocity = 0.0000f;

    private float timestamp;
    private float dT = 0;
    private float accX_old = 0;
    private float[] acc;

    private double omegaMagnitude;
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            //txt_output.setText(String.format("%d:%02d", minutes, seconds));

            PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
            results.setResultCallback(new ResultCallback<DataItemBuffer>() {
                @Override
                public void onResult(@NonNull DataItemBuffer dataItems) {

                    if (dataItems.getCount() != 0) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                        lastDataType = dataMapItem.getDataMap().getString("TYPE");
                        mRotationMatrix = dataMapItem.getDataMap().getFloatArray("rot");
                        // wenn der reset Button auf der Uhr gedrückt wird, wird der aktuelle Wert
                        // des Sensors zum Startwert des Cursors
                        //mRotationMatrix = lastData;

                        //float accX_old = accXRaw;
                        //accXRaw = dataMapItem.getDataMap().getFloat("accX");

                        /*if (accXRaw > 0.5) {
                            tiefe = tiefe + accXRaw;
                        }*/

                        //Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));

                        /*float accX_old = accX;

                        // Filter Acceleration
                        double threshold = 0.3;
                        accX = ((accXRaw < threshold) && (!(accXRaw < -threshold))) ? 0 : accXRaw;

                        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                        //Log.d("currentTime", String.format("CurrentTime: %d", currentTime.getTime()));

                        if (lastLinAccTime == null) {
                            lastLinAccTime = currentTime;
                        }
                        double deltaTime = (currentTime.getTime() - lastLinAccTime.getTime());

                        double deltaTime_square = Math.pow(deltaTime, 2.0) / 100000;

                        distX = (float)(0.5 * deltaTime_square * accX + deltaTime_square * accX_old + distX);

                        tiefe = Math.abs(distX)*10 ;

                        if (distX < 0) {
                            tiefe = 1;
                        }
                        else if(distX>5){
                            tiefe = 5;
                        }

                        lastLinAccTime = currentTime;
                        */


/*
                        if (accXRaw>0.2){
                        }
                        if ((accXRaw - accX_old)>0) {
                            direction = 1;
                        }
                        else{
                            direction = -1;
                        }

                        tiefe = tiefe + accXRaw * direction;

                        if (tiefe < -10) {
                            tiefe = -10;
                        }
                        else if(tiefe>10){
                            tiefe = 10;
                        }
*/

                        //float accX_old = accXRaw;
                        //tiefe = dataMapItem.getDataMap().getFloat("z");

                        //if (Math.abs(accXRaw) > 0.4) {
                        //Velocity_Old = Velocity;
                        //accX_old = accX;

                        //double Gravity = 9806.65; // mm/s^2
                            /*for (int i = 0; i < 1000; i++) {
                                if (accXRaw < 0) {
                                    accXRaw = accXRaw - (accXRaw * 2);
                                }
                            }*/
                        //double freq = 50; // 50 Hz da 20000 mikro sekunde in der Uhr angestellt!
                        //double Velocity = (Gravity * accXRaw) / (2 * Math.PI * freq); // mm/s

                        /*
                            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                            //Log.d("currentTime", String.format("CurrentTime: %d", currentTime.getTime()));

                            if (lastLinAccTime == null) {
                                lastLinAccTime = currentTime;
                            }

                            double deltaTime = (currentTime.getTime() - lastLinAccTime.getTime());

                            Velocity = accXRaw * deltaTime + Velocity; // mm/s

                            double deltaTime_square = Math.pow(deltaTime, 2.0);

                            distX = (float) (0.5 * deltaTime_square * accXRaw + deltaTime * Velocity + distX); // mm

                            lastLinAccTime = currentTime;
                        //}
                        */

                        acc = dataMapItem.getDataMap().getFloatArray("acc");

                        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                        if (lastLinAccTime == null) {
                            lastLinAccTime = currentTime;
                        }
                        double dT = (currentTime.getTime() - lastLinAccTime.getTime());

                        // not normalized yet.
                        accX = acc[0];
                        accY = acc[1];
                        accZ = acc[2];

                        omegaMagnitude = Math.sqrt(accX * accX + accY * accY + accZ * accZ);

                        // Normalize, if it's big enough to get the axis
                        if (omegaMagnitude > 0.2) {
                            accX /= omegaMagnitude;
                            //accY /= omegaMagnitude;
                            //accZ /= omegaMagnitude;
                            //Log.d("linAcc", String.format("%f", (float) omegaMagnitude));
                            //Log.d("*-*accX*-*", String.format("%.3f", accX));

                            /*if ((accX - accX_old) > 0) {
                                direction = 1;
                            } else if ((accX - accX_old) == 0) {
                                direction = 0;
                            } else {
                                direction = -1;
                            }*/

                            //Velocity = 0;
                            //if (accX > 0.05) {

                            //accX += accX;
                            Velocity = accX * dT;// + Velocity; // mm/s
                            direction = (int) Math.signum(Velocity);

                            double dT_square = Math.pow(dT, 2.0);
                            distX -= -(float) Velocity * dT / 50000;

                            //distX = (float) (0.5 * dT_square * accX + dT * Velocity + distX) /1000; // mm
                            //}

                            Log.d("distX----", String.format("%f", distX));

                            accX_old = accX;
                        }

                        //distX = (distX % 6) - 1.4f;
                        if (distX < -7f) {
                            distX = -7f;
                        } else if (distX > -1.4f) {
                            distX = -1.4f;
                        }

                        lastLinAccTime = currentTime;
                    }

                    dataItems.release();
                }
            });

            timerHandler.postDelayed(this, 50);
        }
    };

    /*public void SensorDataChanged() {
        if (lastDataType.contentEquals("GAME_ROTATION")) {

        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get an instance of the SensorManager

        //Timer starten
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 1);

        /*mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor type : deviceSensors){
            Log.e("sensors",type.getStringType());
        }*/

        // Create our Preview view and set it as the content of our
        // Activity
        mRenderer = new MyRenderer();
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mRenderer);
        setContentView(mGLSurfaceView);

        // Init Google Service API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mRenderer.start();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mRenderer.stop();
        mGLSurfaceView.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    class MyRenderer implements GLSurfaceView.Renderer, SensorEventListener {
        private Cube mCube;

        //private Sensor mRotationVectorSensor;
        //private final float[] mRotationMatrix = new float[16];
        public MyRenderer() {
            // find the rotation-vector sensor
            //mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mCube = new Cube(tiefe);
            // initialize the rotation matrix to identity
            mRotationMatrix[0] = 1;
            mRotationMatrix[4] = 1;
            mRotationMatrix[8] = 1;
            mRotationMatrix[12] = 1;
        }

        public void start() {
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
            //mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        }

        public void stop() {
            // make sure to turn our sensor off when the activity is paused
            //mSensorManager.unregisterListener(this);
        }

        public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
         /*   if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.
                SensorManager.getRotationMatrixFromVector(
                        mRotationMatrix , event.values);
            }*/
        }

        public void onDrawFrame(GL10 gl) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            //mCube = new Cube(tiefe);
            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, distX);
            gl.glMultMatrixf(mRotationMatrix, 0);
            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            mCube.draw(gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // set view-port
            // 1 und 2 ändern die Position des Würfels
            gl.glViewport(0, 0, width, height);
            // set projection matrix
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER);
            // clear screen in white
            gl.glClearColor(1, 1, 1, 1);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        class Cube {
            // initialize our cube
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer mIndexBuffer;

            public Cube(float z) {
                final float vertices[] = {
                        -1, -1, -1, 1, -1, -1,
                        1, 1, -1, -1, 1, -1,
                        -1, -1, 1, 1, -1, 1,
                        1, 1, 1, -1, 1, 1,
                };

                for (int i = 0; i < vertices.length; i++) {
                    vertices[i] = vertices[i] * z;
                }

                final float colors[] = {
                        0, 0, 0, 1, 1, 0, 0, 1,
                        1, 1, 0, 1, 0, 1, 0, 1,
                        0, 0, 1, 1, 1, 0, 1, 1,
                        1, 1, 1, 1, 0, 1, 1, 1,
                };
                final byte indices[] = {
                        0, 4, 5, 0, 5, 1,
                        1, 5, 6, 1, 6, 2,
                        2, 6, 7, 2, 7, 3,
                        3, 7, 4, 3, 4, 0,
                        4, 7, 6, 4, 6, 5,
                        3, 0, 1, 3, 1, 2
                };
                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);
                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);
                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }

            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }
        }
    }
}