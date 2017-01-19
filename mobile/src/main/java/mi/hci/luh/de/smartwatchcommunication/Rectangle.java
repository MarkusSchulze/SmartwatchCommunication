package mi.hci.luh.de.smartwatchcommunication;

/**
 * Created by Vincent on 19.01.17.
 */

public class Rectangle {


    private int left;
    private int right;
    private int top;
    private int bottom;
    private int color;

    public Rectangle find(int x, int y) {
        if(y <= this.bottom && y >= this.top && x <= this.right && x >= this.left) {
            return this;
        }
        else {
            return null;
        }
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }



}
