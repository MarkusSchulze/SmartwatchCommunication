package mi.hci.luh.de.smartwatchcommunication;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;

/**
 * Created by Vincent on 15.12.16.
 * Updated by Markus on 31.12.16
 */

public class SensorAnalysis extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Canvas canvas;
    private Paint paint;
    private int clickCount = 0;
    private float currentX, currentY, topY, bottomY, leftX, rightX, middleX, middleY;
    private float accX, accY;
    private boolean calibrated;
    private Timestamp lastLinAccTime;
    private TextView txt_output;
    private String lastDataType;
    private final Float[] lastData = new Float[3];
    private final Float[] calibration = new Float[3];
    private GoogleApiClient mGoogleApiClient;
    private CursorView cursorView;
    private Button showData;
    private double distX, distY, distX_right, distX_left, distY_top, distY_bottom;
    private boolean cursorEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_showcursor);

        //Timer starten
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        LinearLayout rect = (LinearLayout) findViewById(R.id.rect);
        cursorView = new CursorView(this);
        rect.addView(cursorView);

        // Init Google Service API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        mGoogleApiClient.connect();

//        //vorläufiger Button zur Aktualisierung der Empfangsdaten
//        txt_output = (TextView) findViewById(R.id.output);
//        txt_output.setText("Test123");
//        showData = (Button) findViewById(R.id.refresh);
//        showData.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                txt_output.setText(String.valueOf(MyService.getData()));
//                ClickButton();
//            }
//        });
        calibration[0] = 0.0f;
        calibration[1] = 0.0f;
        calibration[2] = 0.0f;

        // int width = cursorView.getWidth();
        //int height = cursorView.getHeight();
        //cursorView.setCursor(width/2, height/2, width, height);
    }

    long startTime = 0;
    Handler timerHandler = new Handler();
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
                        lastData[0] = dataMapItem.getDataMap().getFloat("x");
                        lastData[1] = dataMapItem.getDataMap().getFloat("y");
                        lastData[2] = dataMapItem.getDataMap().getFloat("z");
                        // wenn der reset Button auf der Uhr gedrückt wird, wird der aktuelle Wert
                        // des Sensors zum Startwert des Cursors
                        if (lastDataType.contentEquals("RESET")) {
                            calibration[0] = lastData[0];
                            calibration[1] = lastData[1];
                            calibration[2] = lastData[2];
                        } else {
                            SensorDataChanged();
                        }
//                        Log.d("TYPE", lastDataType);
//                        Log.d("x", String.valueOf(lastData[0]));
//                        Log.d("y", String.valueOf(lastData[1]));
//                        Log.d("z", String.valueOf(lastData[2]));
                    }

                    dataItems.release();
                }
            });

            timerHandler.postDelayed(this, 50);
        }
    };

    public void SensorDataChanged() {
//        if (lastDataType.contentEquals("LINEAR_ACC")) {
//            Float[] v = lastData;
//            //Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));
//
//            float accX_old = accX;
//            float accY_old = accY;
//
//            // Filter Acceleration
//            double threshold = 0.05;
//            accY = ((v[1] < threshold) && (!(v[1] < -threshold))) ? 0 : v[1];
//            accX = ((v[2] < threshold) && (!(v[2] < -threshold))) ? 0 : v[2];
//            accY = (-1) * accY;
//            //Log.d("Acceleration", String.format("accX: %f", v[1]));
//            //Log.d("Acceleration", String.format("accY: %f", v[2]));
//
//
//            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//            //Log.d("currentTime", String.format("CurrentTime: %d", currentTime.getTime()));
//
//            if (lastLinAccTime == null) {
//                lastLinAccTime = currentTime;
//            }
//            double deltaTime = (currentTime.getTime() - lastLinAccTime.getTime());
//
//            double deltaTime_square = Math.pow(deltaTime, 2.0) / 100000;
//
//            distX = 0.5 * deltaTime_square * accX + deltaTime_square * accX_old + distX;
//            distY = 0.5 * deltaTime_square * accY + deltaTime_square * accY_old + distY;
//
//            lastLinAccTime = currentTime;
//            //Log.d("deltaTime", String.format("DeltaTime: %d", deltaTime));
//            Log.d("distX", String.format("distX: %f", distX));
//            Log.d("distY", String.format("distY: %f", distY));
//        }

       if (lastDataType.contentEquals("GAME_ROTATION")) {

            currentX = lastData[1];
            currentY = lastData[2];

            int width = cursorView.getWidth();
            int height = cursorView.getHeight();

//            if (this.calibrated) {
//                float mid_x_rot = (rightX + leftX) / 2;
//                float mid_y_rot = (topY + bottomY) / 2;
//
//                double mid_x_acc = (this.distX_right + this.distX_left) / 2;
//                double mid_y_acc = (this.distY_top + this.distY_bottom) / 2;
//
//                float x_rot = (currentX - mid_x_rot) / ((rightX - leftX) / 2);
//                float y_rot = (currentY - mid_y_rot) / ((topY - bottomY) / 2);
//
//                double x_dist = (distX - mid_x_acc) / ((distX_right - distX_left) / 2);
//                double y_dist = (distY - mid_y_acc) / ((distY_top - distY_bottom) / 2);
//
//                double x = 0.5 * x_rot + 0.5 * x_dist;
//                double y = 0.5 * y_rot + 0.5 * y_dist;
//
//                y = (int) (x_rot * width / 2 + width / 2);
//                x = (int) (y_rot * height / 2 + height / 2);
//            } else {
//                y = (int) (currentX * height ) + height / 2;
//                x = (int) (lastData[2] - calibration[2]) * width  + width / 2;
//                if (x > width){
//                    x -= width;
//                }else if (x < 0){
//
//                }
//            }

            //Umrechnung an die Kalibrierung
            //TODO Vertikale Richtung muss noch verbessert werden
            float y = ((-currentX + calibration[1]) * height) + height / 2;
            float x = ((-currentY + calibration[2]) * width + width / 2);

            //Cursor bleibt im Bild auch wenn man außerhalb zeigt
            /*if (y > height) {
                y = height;
            }
            if (y < 1) {
                y = 1;
            }
            if (x > width) {
                x = width;
            }
            if (x < 1) {
                x = 1;
            }*/


           // Log.d("++++ x,y ++++ ", String.format("%f, %f", x, y));
          //  Log.d("currentX # currentX ", String.format("%f, %f", currentX, currentY));

//            canvas.drawRect(x, y, x + 10, y + 10, paint)
            cursorView.setCursor(x, y, width, height);
            cursorView.invalidate();
//            LinearLayout ll = (LinearLayout) findViewById(R.id.rect);
//            ll.setBackgroundDrawable(new BitmapDrawable(bg));

        }
    }

//    public void ClickButton() {
//        if (!cursorEnabled) {
//            cursorEnabled = true;
//            middleX = currentX;
//            middleY = currentY;
//        } else {
//            cursorEnabled = false;
//        }
//        switch (clickCount) {
//            case 0:
//                Log.d("Calibration", "Start Calibration");
//                showData.setText("Choose Top");
//                break;
//            case 1:
//                topY = currentY;
//                distY_top = distY;
//                Log.d("Calibration", String.format("topY: %f", topY));
//                Log.d("Calibration", String.format("topY dist: %f", distY));
//                showData.setText("Choose Bottom");
//
//
//                break;
//            case 2:
//                bottomY = currentY;
//                distY_bottom = distY;
//                Log.d("Calibration", String.format("bottomY: %f", bottomY));
//                Log.d("Calibration", String.format("bottomY dist: %f", distY));
//                showData.setText("Choose Right");
//
//
//                break;
//            case 3:
//                rightX = currentX;
//                distX_right = distX;
//                Log.d("Calibration", String.format("rightX: %f", rightX));
//                Log.d("Calibration", String.format("rightX dist: %f", distX));
//                showData.setText("Choose Left");
//
//
//                break;
//            case 4:
//                leftX = currentX;
//                distX_left = distX;
//                Log.d("Calibration", String.format("leftX: %f", leftX));
//                Log.d("Calibration", String.format("leftX dist: %f", distX));
//                showData.setText("Finish");
//
//                break;
//            case 5:
//                calibrated = true;
//                Log.d("Calibration", "Calibration completed");
//                showData.setText("Completed");
//                break;
//            default:
//                break;
//
//        }
//        clickCount++;
//    }

//    public void drawSensorData() {
//        if (lastDataType.contentEquals("LINEAR_ACC")) {
//            Float[] v = lastData;
//            Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));
//
//            accToPath(v);
//
//            Log.d("distX", String.format("distX: %f", distX));
//            Log.d("distY", String.format("distY: %f", distY));
//
//        }
//        if (lastDataType.contentEquals("GAME_ROTATION")) {
//            Float[] vx = lastData;
//            //Log.d("Game", String.format("%.3f, %.3f, %.3f", vx[0], vx[1], vx[2]));
//            currentX = (-1) * vx[2];
//            currentY = (-1) * vx[1];
//            int w = bg.getWidth();
//            int h = bg.getHeight();
//            double x, y;
//
//            if (calibrated) {
//                double[] coordinates;
//                coordinates = calibrateCoordinates(currentX, currentY);
//
//                x = coordinates[0];
//                y = coordinates[1];
//
//            } else {
//                x = currentX;
//                y = currentY;
//
//            }
//
//            int pixel[];
//            pixel = mapCoordinatesToDisplay(x, y, w, h);
//
//            canvas.drawRect(pixel[1], pixel[0], pixel[1] + 10, pixel[0] + 10, paint);
//
//        }
//    }
//    public double[] calibrateCoordinates(double x_in, double y_in) {
//
//        double[] coordinates = new double[2];
//
//        if (calibrated) {
//            float mid_x = (rightX + leftX) / 2;
//            float mid_y = (topY + bottomY) / 2;
//
//            coordinates[0] = (currentX - mid_x) / ((rightX - leftX) / 2);
//            coordinates[1] = (currentY - mid_y) / ((topY - bottomY) / 2);
//
//        }
//
//        return coordinates;
//
//    }
//    public int[] mapCoordinatesToDisplay(double x_in, double y_in, int w, int h) {
//        int[] coordinates = new int[2];
//
//        int point_x = (int) (x_in * w / 2 + w / 2);
//        int point_y = (int) (y_in * h / 2 + h / 2);
//
//        if (point_x > h) {
//            point_x = h;
//        }
//        if (point_x < 0) {
//            point_x = 0;
//        }
//        if (point_y > w) {
//            point_y = w;
//        }
//        if (point_y < 0) {
//            point_y = 0;
//        }
//
//        coordinates[0] = point_x;
//        coordinates[1] = point_y;
//        return coordinates;
//    }
//
//    public void accToPath(Float[] v) {
//
//        float accX_old = accX;
//        float accY_old = accY;
//
//        // Filter Acceleration
//        double threshold = 0.2;
//        accX = ((v[1] < threshold) && (!(v[1] < -threshold))) ? 0 : v[1];
//        accY = ((v[2] < threshold) && (!(v[2] < -threshold))) ? 0 : v[2];
//
//        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
//        Log.d("currentTime", String.format("CurrentTime: %d", currentTime.getTime()));
//
//        if (lastLinAccTime == null) {
//            lastLinAccTime = currentTime;
//        }
//        double deltaTime = (currentTime.getTime() - lastLinAccTime.getTime());
//
//        double deltaTime_square = Math.pow(deltaTime, 2.0) / 100000;
//
//        distX = 0.5 * deltaTime_square * accX + deltaTime_square * accX_old + distX;
//        distY = 0.5 * deltaTime_square * accY + deltaTime_square * accY_old + distY;
//
//
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
