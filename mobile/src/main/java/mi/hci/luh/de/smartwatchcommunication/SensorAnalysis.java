package mi.hci.luh.de.smartwatchcommunication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
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
    private float currentX, currentY, topY, bottomY, leftX, rightX;
    private float accX, accY;
    private double distX, distY;
    private boolean calibrated;
    private Timestamp lastLinAccTime;
    private TextView txt_output;
    private Bitmap bg;
    private String lastDataType;
    private final Float[] lastData = new Float[3];
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        //Timer starten
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        //Zeichnen
        paint = new Paint();
        paint.setColor(Color.parseColor("#CD5C5C"));
        bg = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);

        //LinearLayout ll = (LinearLayout) findViewById(R.id.activity_main);
        //ll.setBackgroundDrawable(new BitmapDrawable(bg));

        // Init Google Service API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        mGoogleApiClient.connect();

        //vorläufiger Button zur Aktualisierung der Empfangsdaten
        txt_output = (TextView) findViewById(R.id.output);
        //txt_output.setText("Test123");
        Button showData = (Button) findViewById(R.id.refresh);
        showData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                txt_output.setText(String.valueOf(MyService.getData()));
                ClickButton();
            }
        });
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

                        // This should read the correct value.
                        float value = dataMapItem.getDataMap().getFloat("x");
                        txt_output.setText(String.valueOf(value));

                        lastDataType = dataMapItem.getDataMap().getString("TYPE");
                        lastData[0] = dataMapItem.getDataMap().getFloat("x");
                        lastData[1] = dataMapItem.getDataMap().getFloat("y");
                        lastData[2] = dataMapItem.getDataMap().getFloat("z");
                        Log.d("TYPE", lastDataType);
                        Log.d("x", String.valueOf(lastData[0]));
                        Log.d("y", String.valueOf(lastData[1]));
                        Log.d("z", String.valueOf(lastData[2]));
                    }

                    dataItems.release();
                }
            });

            timerHandler.postDelayed(this, 500);
        }
    };

    public void ClickButton(){

        switch(clickCount) {
            case 0:
                this.topY =  currentY;
                Log.d("Calibration", String.format("topY: %f", this.topY));
                break;
            case 1:
                this.bottomY = currentY;
                Log.d("Calibration", String.format("bottomY: %f", this.bottomY));
                break;
            case 2:
                this.rightX = currentX;
                Log.d("Calibration", String.format("rightX: %f", this.rightX));
                break;
            case 3:
                this.leftX = currentX;
                Log.d("Calibration", String.format("leftX: %f", this.leftX));
                break;
            case 4:
                this.calibrated = true;
                Log.d("Calibration", "Calibration completed");
                break;
            default:
                break;

        }
        clickCount++;
    }
    public void drawSensorData() {
        if (lastDataType.contentEquals("LINEAR_ACC")) {
            Float[] v = lastData;
            Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));

            this.accToPath(v);

            Log.d("distX", String.format("distX: %f", distX));
            Log.d("distY", String.format("distY: %f", distY));

        }
        if (lastDataType.contentEquals("GAME_ROTATION")) {
            Float[] vx = lastData;
            //Log.d("Game", String.format("%.3f, %.3f, %.3f", vx[0], vx[1], vx[2]));
            currentX = (-1) * vx[2];
            currentY = (-1) * vx[1];
            int w = bg.getWidth();
            int h = bg.getHeight();
            double x, y;

            if (this.calibrated) {
                double[] coordinates;
                coordinates = this.calibrateCoordinates(currentX, currentY);

                x = coordinates[0];
                y = coordinates[1];

            } else {
                x = currentX;
                y = currentY;

            }

            int pixel[];
            pixel = this.mapCoordinatesToDisplay(x, y, w, h);

            canvas.drawRect(pixel[1], pixel[0], pixel[1] + 10, pixel[0] + 10, paint);

        }
    }
    public double[] calibrateCoordinates(double x_in, double y_in) {

        double[] coordinates = new double[2];

        if (this.calibrated) {
            float mid_x = (rightX + leftX) / 2;
            float mid_y = (topY + bottomY) / 2;

            coordinates[0] = (currentX - mid_x) / ((rightX - leftX) / 2);
            coordinates[1] = (currentY - mid_y) / ((topY - bottomY) / 2);

        }

        return coordinates;

    }
    public int[] mapCoordinatesToDisplay(double x_in, double y_in, int w, int h) {
        int[] coordinates = new int[2];

        int point_x = (int) (x_in * w / 2 + w / 2);
        int point_y = (int) (y_in * h / 2 + h / 2);

        if (point_x > h) {
            point_x = h;
        }
        if (point_x < 0) {
            point_x = 0;
        }
        if (point_y > w) {
            point_y = w;
        }
        if (point_y < 0) {
            point_y = 0;
        }

        coordinates[0] = point_x;
        coordinates[1] = point_y;
        return coordinates;
    }

    public void accToPath(Float[] v) {

        float accX_old = accX;
        float accY_old = accY;

        // Filter Acceleration
        double threshold = 0.2;
        accX = ((v[1] < threshold) && (!(v[1] < -threshold))) ? 0 : v[1];
        accY = ((v[2] < threshold) && (!(v[2] < -threshold))) ? 0 : v[2];

        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Log.d("currentTime", String.format("CurrentTime: %d", currentTime.getTime()));

        if (lastLinAccTime == null) {
            lastLinAccTime = currentTime;
        }
        double deltaTime = (currentTime.getTime() - lastLinAccTime.getTime());

        double deltaTime_square = Math.pow(deltaTime, 2.0) / 100000;

        distX = 0.5 * deltaTime_square * accX + deltaTime_square * accX_old + distX;
        distY = 0.5 * deltaTime_square * accY + deltaTime_square * accY_old + distY;


    }

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
