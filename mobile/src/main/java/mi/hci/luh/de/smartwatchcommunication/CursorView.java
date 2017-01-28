package mi.hci.luh.de.smartwatchcommunication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;


/**
 * Created by mox on 27.11.2016.
 * https://www.linux.com/learn/how-code-2d-drawing-android-motion-sensors
 */

public class CursorView extends View {
    private int diameter;
    private ShapeDrawable bubble;
    private Rect r = new Rect();
    private float Rotation;
    private int bg;
    public ArrayList<Rectangle> rectangles;


    public CursorView(Context context) {
        super(context);
        createBubble();

        rectangles = new ArrayList<>();
        this.bg = Color.parseColor("#BDBDBD");

    }

    private void createBubble() {
        int x = 200;
        int y = 300;
        diameter = 20;
        bubble = new ShapeDrawable(new OvalShape());
        bubble.setBounds(x, y, x + diameter, y + diameter);
        bubble.getPaint().setColor(0xff00cccc);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.rectangles.isEmpty()) {
            this.createRectangles(canvas);
        }

        // background color
        bubble.getPaint().setColor(this.bg);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bubble.getPaint());

        //Rechtecke zum anklicken im Hintergrund
        this.drawRectangles(canvas);

        //Rechteck um den Cursor
        canvas.save();
        canvas.rotate(Rotation, bubble.getBounds().centerX(), bubble.getBounds().centerY());
        canvas.drawRect(r, bubble.getPaint());
        canvas.restore();

        // cursor color
        bubble.getPaint().setColor(0xff00cccc);
        bubble.draw(canvas);
    }

    protected void setCursor(float x, float y, int width, int height, float rotation, float zoom) {
        //Rechteck um den Cursor herumzeichnen
        int zone = 40 - (int) zoom;
        if (zone > 80) zone = 80;
        int rect_left = (int) x - zone;
        int rect_top = (int) y - zone;
        int rect_right = (int) x + diameter + zone;
        int rect_bottom = (int) y + diameter + zone;

        Rotation = rotation;

        //Cursor bleibt im Bild auch wenn man außerhalb zeigt
        if ((int) x + diameter / 2 >= width) {
            x = width - diameter;
        }
        if (x < 1) {
            x = 1;
            rect_left = 1;
        }

        if ((int) y + diameter / 2 >= height) {
            y = height - diameter;
        }
        if (y < 1) {
            y = 1;
            rect_top = 1;
        }

        r.set(rect_left, rect_top, rect_right, rect_bottom);
        bubble.setBounds((int) x, (int) y, (int) x + diameter, (int) y + diameter);
    }

    public void createRectangles(Canvas canvas) {

        int color = Color.parseColor("#000000");

        int x_step = canvas.getWidth() / 3;
        int y_step = canvas.getHeight() / 3;

        Log.d("canvasHeight", String.format("canvasheight: %d", this.getHeight()));
        Log.d("canvasWidth", String.format("canvaswidth: %d", this.getWidth()));


        int x_margin = (int) (y_step * 0.05);
        int y_margin = (int) (y_step * 0.05);

        for (int x = 0; x <= canvas.getWidth(); x += x_step) {
            for (int y = 0; y <= canvas.getHeight(); y += y_step) {

                int right = x + x_step;
                int bottom = y + y_step;

                Rectangle currentRect = new Rectangle();

                currentRect.setTop(y + y_margin / 2);
                currentRect.setBottom(bottom - y_margin / 2);
                currentRect.setLeft(x + x_margin / 2);
                currentRect.setRight(right - x_margin / 2);
                currentRect.setColor(color);
                rectangles.add(currentRect);
                color += 900000;

            }
        }
    }

    protected void drawRectangles(Canvas canvas) {

        for (Rectangle currentRect : this.rectangles) {

            bubble.getPaint().setColor(currentRect.getColor());

            canvas.drawRect(currentRect.getLeft(), currentRect.getTop(), currentRect.getRight(), currentRect.getBottom(), bubble.getPaint());
        }
    }

    public void setBg(int bg) {
        this.bg = bg;
    }
}