package mi.hci.luh.de.smartwatchcommunication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Vincent on 15.12.16.
 */

public class SensorAnalysis extends Activity{
    private Sensor accSensor, gyroSensor, accCleanSensor, gameRotationSensor, linearAccSensor;
    private SensorManager sensorManager;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bg;
    private Button BigB;
    private int clickCount = 0;
    private float currentX, currentY, topY, bottomY, leftX, rightX, middleX, middleY;
    private float accX, accY;
    private double distX, distY;
    private boolean calibrated;
    private Timestamp lastLinAccTime;
    private static TextView txt_output;
    private Button showData;
    private Bitmap bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paint = new Paint();
        paint.setColor(Color.parseColor("#CD5C5C"));
        bg = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);
        final GoogleApiClient mGoogleApiClient = MainActivity.getAPI();

        LinearLayout ll = (LinearLayout) findViewById(R.id.activity_main);
        ll.setBackgroundDrawable(new BitmapDrawable(bg));

        //vorläufiger Button zur Aktualisierung der Empfangsdaten
        txt_output = (TextView) findViewById(R.id.output);
        showData = (Button) findViewById(R.id.refresh);
        showData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
                results.setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        if (dataItems.getCount() != 0) {
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                            // This should read the correct value.
                            float value = dataMapItem.getDataMap().getFloat("x");
                            Log.d("receiveDataMain", String.valueOf(value));
                            txt_output.setText(String.valueOf(value));
                        }

                        dataItems.release();
                    }
                });
            }
        });
    }

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
    public void drawSensorData(SensorEvent event) {
        if (event.sensor == linearAccSensor) {
            float[] v = event.values;
            Log.d("linAcc", String.format("%.3f\t%.3f\t%.3f", v[0], v[1], v[2]));

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

            lastLinAccTime = currentTime;
            //Log.d("deltaTime", String.format("DeltaTime: %d", deltaTime));
            Log.d("distX", String.format("distX: %f", distX));
            Log.d("distY", String.format("distY: %f", distY));

        }
        if (event.sensor == gameRotationSensor) {
            float[] vx = event.values;
            //Log.d("Game", String.format("%.3f, %.3f, %.3f", vx[0], vx[1], vx[2]));
            currentX = (-1) * vx[2];
            currentY = (-1) * vx[1];
            int w = bg.getWidth();
            int h = bg.getHeight();

            int point_x, point_y = 0;
            if (this.calibrated) {
                float mid_x = (rightX + leftX) / 2;
                float mid_y = (topY + bottomY) / 2;

                float x = (currentX - mid_x) / ((rightX - leftX) / 2);
                float y = (currentY - mid_y) / ((topY - bottomY) / 2);

                //point_x = (int) (x * 1080/2 + 1080/2 * (1-middleX));
                //point_y = (int) (y * 1920/2 + 1920/2 * (1-middleY));
                point_x = (int) (x * w / 2 + w / 2);
                point_y = (int) (y * h / 2 + h / 2);
            } else {
                point_x = (int) (currentX * h / 2) + h / 2;
                point_y = (int) (currentY * w / 2) + w / 2;
            }
            // Z = X-Achse ()
            // Y = Y-Achse ()
            //if(clickCount > 4) {
            //int x = (int)
            //int y = ()
            //}
            //Log.d("Game", String.format("%f x %f", vx[2], vx[1]));
            //Log.d("Game", String.format("%d x %d", point_x, point_y));

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
            canvas.drawRect(point_y, point_x, point_y + 10, point_x + 10, paint);

            //canvas.drawRect(point_y, point_x, point_y+10, point_x+10, paint);

            canvas.drawRect(20, 20, 30, 30, paint);


            LinearLayout ll = (LinearLayout) findViewById(R.id.rect);
            ll.setBackgroundDrawable(new BitmapDrawable(bg));

        }
    }
    public double[] calibrate(double x_in, double y_in, double z_in) {

        if (this.calibrated) {
            float mid_x = (rightX + leftX) / 2;
            float mid_y = (topY + bottomY) / 2;

            float x = (currentX - mid_x) / ((rightX - leftX) / 2);
            float y = (currentY - mid_y) / ((topY - bottomY) / 2);

            //point_x = (int) (x * 1080/2 + 1080/2 * (1-middleX));
            //point_y = (int) (y * 1920/2 + 1920/2 * (1-middleY));
            point_x = (int) (x * w / 2 + w / 2);
            point_y = (int) (y * h / 2 + h / 2);
        } else {
            point_x = (int) (currentX * h / 2) + h / 2;
            point_y = (int) (currentY * w / 2) + w / 2;
        }
    }


}
