package mi.hci.luh.de.smartwatchcommunication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Wearable;

import static com.google.android.gms.wearable.CapabilityApi.FILTER_REACHABLE;


public class MainActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txt_output = (TextView) findViewById(R.id.output);

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Wearable.API)
                .build();

        PendingResult result;
        result = Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient, FILTER_REACHABLE);

        //Wearable.ChannelApi.openChannel(mGoogleApiClient,)
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}

