package mi.hci.luh.de.smartwatchcommunication;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class MyService extends WearableListenerService {
    private static Float data;

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent dataEvent : dataEventBuffer){
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                Log.d("receiveData", String.valueOf(dataMap.getFloat("x")));
                data = dataMap.getFloat("x");
                if (path.equals("SensorData")) {
                    //MainActivity.setSensorText(dataMap.getFloat("x"),0,0);
                    Log.d("receiveData", String.valueOf(dataMap.getFloat("x")));
                }
            }
        }
    }

    public static Float getData(){
        return data;
    }
}
