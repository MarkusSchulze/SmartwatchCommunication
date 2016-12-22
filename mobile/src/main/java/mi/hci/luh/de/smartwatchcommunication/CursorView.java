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
    private int x;
    private int y;
    private ShapeDrawable bubble;

    public CursorView(Context context) {
        super(context);
        createBubble();
    }

    private void createBubble() {
        x = 200;
        y = 300;
        diameter = 20;
        bubble = new ShapeDrawable(new OvalShape());
        bubble.setBounds(x, y, x + diameter, y + diameter);
        bubble.getPaint().setColor(0xff00cccc);
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bubble.draw(canvas);
    }

    protected void setCursor(int x, int y) {
        bubble.setBounds(x, y, x + diameter, y + diameter);
    }

   protected void move(float f, float g) {
        x = (int) (x + f);
        y = (int) (y + g);
        bubble.setBounds(x, y, x + diameter, y + diameter);
    }
}