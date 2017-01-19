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
    private String lastDataType;
    private GoogleApiClient mGoogleApiClient;
    private CursorView cursorView;
    private int pointX, pointY;

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
                        //lastData[2] = dataMapItem.getDataMap().getFloat("z");
                        // wenn der reset Button auf der Uhr gedrückt wird, wird der aktuelle Wert
                        // des Sensors zum Startwert des Cursors
                        if (lastDataType.contentEquals("RESET")) {
                            calibration[0] = lastData[0];
                            calibration[1] = lastData[1];
                            //calibration[2] = lastData[2];
                        }

                        else {
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
        //calibration[2] = 0.0f;
    }

    public void SensorDataChanged() {

        if (lastDataType.contentEquals("CLICK")) {
            boolean found = false;
            for(Rectangle r : cursorView.rectangles) {
                if(r.find(this.pointX, this.pointY) != null) {
                    found = true;
                    this.cursorView.setBg(r.getColor());
                }
            }
            if( found ) {
                Log.d("Found", String.format("Rectangle found! "));
            }
            else {
                Log.d("Not Found", String.format("Rectangle not found! "));

            }
        }
        else if (lastDataType.contentEquals("GAME_ROTATION")) {

            int width = cursorView.getWidth();
            int height = cursorView.getHeight();

            //Umrechnung an die Kalibrierung
            int vertical = (int) (lastData[1] * height) + height / 2;
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
            pointX = (int) horizontal;
            pointY = vertical;

            cursorView.setCursor(horizontal, vertical, width, height);
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
