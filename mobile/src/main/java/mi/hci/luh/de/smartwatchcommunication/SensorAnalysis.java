package mi.hci.luh.de.smartwatchcommunication;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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

/**
 * Created by Vincent on 15.12.16.
 * Updated by Markus on 31.12.16
 */

public class SensorAnalysis extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final Float[] lastData = new Float[3];
    private final Float[] calibration = new Float[3];
    long startTime = 0;
    Handler timerHandler = new Handler();
    private float currentX, currentY;
    private String lastDataType;
    private GoogleApiClient mGoogleApiClient;
    private CursorView cursorView;

    private int width;
    private int height;

    private float[] XY;

    private float y;
    private float x;

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

                            //xref = lastData[0];
                            //yref = lastData[1];
                            /*calibration[0] = height - mod(x, height) - (height / 2);
                            calibration[1] = width - mod(y, width) - (width / 2);

                            Log.d("x,y ---test", String.format("%d, %d", height, width));
                            Log.d("x,y ---test", String.format("%f, %f", mod(x, height), mod(y, width)));
                            Log.d("x,y ---test", String.format("%f, %f", height - mod(x, height), width - mod(y, width)));
                            Log.d("x,y ---test", String.format("%d, %d", (height / 2), (width / 2)));*/

                        } else {
                            SensorDataChanged();
                        }
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
        setContentView(R.layout.a_showcursor);

        //Timer starten
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        calibration[0] = 0.0f;
        calibration[1] = 0.0f;
        calibration[2] = 0.0f;

        //cursorView.setCursor(width/2, height/2, width, height);
    }

    public void SensorDataChanged() {

        if (lastDataType.contentEquals("GAME_ROTATION")) {

            width = cursorView.getWidth();
            height = cursorView.getHeight();

            currentX = lastData[0];
            currentY = lastData[1];

            //Umrechnung an die Kalibrierung
            //TODO Vertikale Richtung muss noch verbessert werden
            int vertical = (int) (currentY * height) + height / 2;
            float horizontal = ((lastData[0] - calibration[0]) * width + width / 2);
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

            cursorView.setCursor(horizontal, vertical);
            cursorView.invalidate();
        }
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
