package mi.hci.luh.de.smartwatchcommunication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.opengl.GLSurfaceView;

/**
 * Created by Vincent on 19.01.17.
 */

public class SurfaceView extends GLSurfaceView {

    private ShapeDrawable shape;

    public SurfaceView(Context context) {
        super(context);

        shape = new ShapeDrawable(new OvalShape());
        shape.setBounds(200, 300, 300, 400);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, 200, 200, shape.getPaint());
        shape.getPaint().setColor(0xff00cccc);

        shape.draw(canvas);
    }
}
