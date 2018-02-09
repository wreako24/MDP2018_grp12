package zyuezheng.mdpgp10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import static android.content.ContentValues.TAG;

public class Arena extends View {
    private int col = 20;
    private Robot robot;
    private ArenaThread thread;
    private int gridSize = 34;
    private int[] grid;
    private int[][] obstacles = new int[15][20];
    private float touchY = 0;
    private float touchX = 0;
    private float pointX = 0;
    private float pointY = 0;
    private float wayPointX = 0;
    private float wayPointY = 0;
    private boolean allowDrawPointBox = false;
    private boolean wayPointIsSet=false;
    //rows = 15, cols = 20

    public Arena(Context context) {
        super(context);
        robot = new Robot();
        thread = new ArenaThread(this);
        thread.startThread();
    }

    @Override
    public void onDraw(Canvas canvas) {
        RelativeLayout arenaView = (RelativeLayout) getRootView().findViewById(R.id.arenaView);
        gridSize = 34;// ((arenaView.getMeasuredWidth()) - (arenaView.getMeasuredWidth() / col)) / col;

        robot.drawArena(canvas, gridSize);
        robot.drawBoxOnTouch(canvas, pointX, pointY, allowDrawPointBox);
        robot.drawWaypoint(canvas, wayPointX, wayPointY, wayPointIsSet);
    }

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public void setAllowDrawGreenBox(boolean draw) {
        allowDrawPointBox = draw;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        allowDrawPointBox = true;
        float x = event.getX();
        float y = event.getY();
        touchX = x;
        touchY = y;
        Log.e(TAG, "touch x: " + x);
        Log.e(TAG, "touch y: " + y);
        Log.e(TAG, "Set Waypoint");

        pointX = x / 34;
        pointY = y / 34;
        Log.e(TAG, "WaypointX: " + pointX);
        Log.e(TAG, "WaypointY: " + pointY);

        /*
        //set coordinates
        x_coordinate.setText(String.format("%s", pointX), TextView.BufferType.EDITABLE);
        y_coordinate.setText(String.format("%s", pointY), TextView.BufferType.EDITABLE);
        direction.setText(rDirection, TextView.BufferType.EDITABLE);*/
        return super.onTouchEvent(event);
    }

    public float[] getWaypoints() {
        float[] waypointArray = new float[2];
        while (true) {
            if (pointX != 0 && pointY != 0) {
                waypointArray[0] = pointX;
                waypointArray[1] = pointY;
                break;
            }
        }
        return waypointArray;
    }


    public void setGridArray(int[] gridArray) {
        Log.d("setGridArray()", "");
        this.grid = gridArray;
    }

    public void setObstacles(int[][] obstacles) {
        this.obstacles = obstacles;
    }

    public void update() {
        robot.setGridSettings(grid);
        robot.setObstacles(obstacles);
    }

    public void wayPointIsSet(boolean b, int _wayPointX, int _wayPointY) {
        wayPointX = _wayPointX;
        wayPointY = _wayPointY;
        wayPointIsSet = b;


    }
}
