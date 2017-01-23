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
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RotationAndCursor extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private SurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;

    private String lastDataType;
    private final Float[] lastData = new Float[3];
    private final Float[] calibration = new Float[3];
    private float[] mRotationMatrix = new float[16];
    private GoogleApiClient mGoogleApiClient;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private float horizontal;
    private int vertical;
    private float matrixTransition;
    private CursorView cursorView;


    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
            results.setResultCallback(new ResultCallback<DataItemBuffer>() {
                @Override
                public void onResult(@NonNull DataItemBuffer dataItems) {

                    if (dataItems.getCount() != 0) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                        lastDataType = dataMapItem.getDataMap().getString("TYPE");
                        mRotationMatrix = dataMapItem.getDataMap().getFloatArray("rot");
                        lastData[0] = dataMapItem.getDataMap().getFloat("x");
                        lastData[1] = dataMapItem.getDataMap().getFloat("y");
                        lastData[2] = dataMapItem.getDataMap().getFloat("z");
                        // wenn der reset Button auf der Uhr gedrückt wird, wird der aktuelle Wert
                        // des Sensors zum Startwert des Cursors
                        if (lastDataType.contentEquals("RESET")) {
                            calibration[0] = lastData[0];
                            calibration[1] = lastData[1];
                        } else {
                            SensorDataChanged();
                        }


                        //Log.d("test", String.format("%f", mRotationMatrix[0]));
                    }

                    dataItems.release();
                }
            });

            timerHandler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Timer starten
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 1);

        // Create our Preview view and set it as the content of our
        // Activity
        mRenderer = new MyRenderer();
        mGLSurfaceView = new SurfaceView(this);
        mGLSurfaceView.setRenderer(mRenderer);
        setContentView(mGLSurfaceView);

//        LinearLayout rect = (LinearLayout) findViewById(R.id.rect);
//        cursorView = new CursorView(this);
//        rect.addView(cursorView);

        // Init Google Service API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        mGoogleApiClient.connect();

        calibration[0] = 0.0f;
        calibration[1] = 0.0f;
        lastData[0] = 0.0f;
        lastData[1] = 0.0f;
    }

    public void SensorDataChanged() {
        int width = mGLSurfaceView.getWidth();
        int height = mGLSurfaceView.getHeight();
//        if (lastDataType.contentEquals("CLICK")) {
//            boolean found = false;
//            for(Rectangle r : cursorView.rectangles) {
//                if(r.find((int)horizontal, this.vertical) != null) {
//                    found = true;
//                    this.cursorView.setBg(r.getColor());
//                }
//            }
//            if( found ) {
//                Log.d("Found", String.format("Rectangle found! "));
//            }
//            else {
//                Log.d("Not Found", String.format("Rectangle not found! "));
//
//            }
//        }
        if (lastDataType.contentEquals("GAME_ROTATION")) {

            //Umrechnung an die Kalibrierung
            vertical = (int) (lastData[1] * height) + height / 2;
            horizontal = ((lastData[0] - calibration[0]) * width + width / 2);
            if (horizontal > width) {
                horizontal -= width;
            } else if (horizontal < 0) {
                horizontal += width;
            }

            //Cursor bleibt im Bild auch wenn man außerhalb zeigt
            if (vertical > height) {
                vertical = height;
            }
            if (vertical < 1) {
                vertical = 1;
            }
            if (horizontal > width) {
                horizontal = width;
            }
            if (horizontal < 1) {
                horizontal = 1;
            }

            //Umrechnen des Wertebereichs von Bildschirmgröße auf -20 bis +20
            matrixTransition = (horizontal / width * 40) - 20;
        }
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

//    class MyRenderer implements GLSurfaceView.Renderer {
//        private Cube mCube;
//        private Square square;
//
//        MyRenderer() {
//            square = new Square();
//
//            mCube = new Cube();
//            // initialize the rotation matrix to identity
//            mRotationMatrix[0] = 1;
//            mRotationMatrix[4] = 1;
//            mRotationMatrix[8] = 1;
//            mRotationMatrix[12] = 1;
//        }
//
//        public void onDrawFrame(GL10 gl) {
//
//
//            // clear screen
//            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
//            // set-up modelview matrix
//            gl.glMatrixMode(GL10.GL_MODELVIEW);
//            gl.glLoadIdentity();
//            gl.glTranslatef(matrixTransition, lastData[1] * 10, -8.0f);
//            gl.glMultMatrixf(mRotationMatrix, 0);
//            // draw our object
//            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
//
//            mCube.draw(gl);
//
//            square.setVerticesAndDraw(0.8f, gl, (byte) 255);
//
////            square.setVerticesAndDraw(0.7f, gl, (byte) 150);
////            square.setVerticesAndDraw(0.6f, gl, (byte) 100);
////            square.setVerticesAndDraw(0.5f, gl, (byte) 80);
////            square.setVerticesAndDraw(0.4f, gl, (byte) 50);
//
//
//        }
//
//        public void onSurfaceChanged(GL10 gl, int width, int height) {
//            // set view-port
//            gl.glViewport(0, 0, width, height);
//            // set projection matrix
//            float ratio = (float) width / height;
//            gl.glMatrixMode(GL10.GL_PROJECTION);
//            gl.glLoadIdentity();
//            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
//        }
//
//        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//            // dither is enabled by default, we don't need it
//            gl.glDisable(GL10.GL_DITHER);
//            // clear screen in white
//            gl.glClearColor(1, 1, 1, 1);
//        }
//
//        class Cube {
//            // initialize our cube
//            private FloatBuffer mVertexBuffer;
//            private FloatBuffer mColorBuffer;
//            private ByteBuffer mIndexBuffer;
//
//            Cube() {
//                final float vertices[] = {
//                        -1, -1, -1, 1, -1, -1,
//                        1, 1, -1, -1, 1, -1,
//                        -1, -1, 1, 1, -1, 1,
//                        1, 1, 1, -1, 1, 1,
//                };
//                final float colors[] = {
//                        0, 0, 0, 1, 1, 0, 0, 1,
//                        1, 1, 0, 1, 0, 1, 0, 1,
//                        0, 0, 1, 1, 1, 0, 1, 1,
//                        1, 1, 1, 1, 0, 1, 1, 1,
//                };
//                final byte indices[] = {
//                        0, 4, 5, 0, 5, 1,
//                        1, 5, 6, 1, 6, 2,
//                        2, 6, 7, 2, 7, 3,
//                        3, 7, 4, 3, 4, 0,
//                        4, 7, 6, 4, 6, 5,
//                        3, 0, 1, 3, 1, 2
//                };
//                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
//                vbb.order(ByteOrder.nativeOrder());
//                mVertexBuffer = vbb.asFloatBuffer();
//                mVertexBuffer.put(vertices);
//                mVertexBuffer.position(0);
//                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
//                cbb.order(ByteOrder.nativeOrder());
//                mColorBuffer = cbb.asFloatBuffer();
//                mColorBuffer.put(colors);
//                mColorBuffer.position(0);
//                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
//                mIndexBuffer.put(indices);
//                mIndexBuffer.position(0);
//            }
//
//            void draw(GL10 gl) {
//                gl.glEnable(GL10.GL_CULL_FACE);
//                gl.glFrontFace(GL10.GL_CW);
//                gl.glShadeModel(GL10.GL_SMOOTH);
//                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
//                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
//                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
//            }
//        }
//    }

    class MyRenderer implements GLSurfaceView.Renderer{
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(1.0f, 1.0f, 0.0f, 0.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            float aspect = (float)width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            Square square=new Square();
            square.setVerticesAndDraw(0.8f, gl, (byte) 255);
            square.setVerticesAndDraw(0.7f, gl, (byte) 150);
            square.setVerticesAndDraw(0.6f, gl, (byte) 100);
            square.setVerticesAndDraw(0.5f, gl, (byte) 80);
            square.setVerticesAndDraw(0.4f, gl, (byte) 50);
        }
    }


    class Square {

        public void setVerticesAndDraw(Float value, GL10 gl, byte color) {
            FloatBuffer vertexbuffer;
            ByteBuffer indicesBuffer;
            ByteBuffer mColorBuffer;

            byte indices[] = {0, 1, 2, 0, 2, 3};

            float vetices[] = {
                    -value, value, 0.0f,
                    value, value, 0.0f,
                    value, -value, 0.0f,
                    -value, -value, 0.0f
            };

            byte colors[] = {
                    color, color, 0, color,
                    0, color, color, color,
                    0, 0, 0, color,
                    color, 0, color, 1
            };


            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vetices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexbuffer = byteBuffer.asFloatBuffer();
            vertexbuffer.put(vetices);
            vertexbuffer.position(0);

            indicesBuffer = ByteBuffer.allocateDirect(indices.length);
            indicesBuffer.put(indices);
            indicesBuffer.position(0);

            mColorBuffer = ByteBuffer.allocateDirect(colors.length);
            mColorBuffer.put(colors);
            mColorBuffer.position(0);


            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexbuffer);
            gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, mColorBuffer);

            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indicesBuffer);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        }
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
}