package mi.hci.luh.de.smartwatchcommunication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //HIER einfach die activity wählen, die man starten möchte Auswahl im Moment
        // RotationVectorDemo oder SensorAnalysis
        Intent i = new Intent(this, SensorAnalysis.class);
        //Intent i = new Intent(this, RotationVectorDemo.class);

        startActivity(i);
    }
}

