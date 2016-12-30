package mi.hci.luh.de.smartwatchcommunication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

/**
 * Created by mox on 27.11.2016.
 * https://www.linux.com/learn/how-code-2d-drawing-android-motion-sensors
 */

public class CursorView extends View {
    private int diameter;
    private ShapeDrawable bubble;

    public CursorView(Context context) {
        super(context);
        createBubble();
    }

    private void createBubble() {
        int x = 200;
        int y = 300;
        diameter = 100;
        bubble = new ShapeDrawable(new OvalShape());
        bubble.setBounds(x, y, x + diameter, y + diameter);
        bubble.getPaint().setColor(0xff00cccc);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bubble.draw(canvas);
    }

    protected void setCursor(float x, float y, int width, int height) {

        //Cursor bleibt im Bild auch wenn man außerhalb zeigt
        if ((int) x >= width) {
            x = (int) width - diameter;
        }
        if (x < 1) {
            x = 1;
        }

        if ((int) y >= height) {
            y = (int) height - diameter;
        }
        if (y < 1) {
            y = 1;
        }

        bubble.setBounds((int) x, (int) y, (int) x + diameter, (int) y + diameter);
    }
}