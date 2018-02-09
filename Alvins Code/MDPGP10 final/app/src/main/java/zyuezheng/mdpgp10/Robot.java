package zyuezheng.mdpgp10;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;


public class Robot {
    private int[] gridSettings;
    private int[][] obstacleArray = new int[15][20];
    private int left;
    private int top;
    private int right;
    private int bottom;
    private Paint paint;
    private Canvas canvas;
    private static int size = 0;
    private static final String TAG = "Robot";
    private float viewWidth;
    private float viewHeight;

    public Robot() {
        super();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    public void setCanvas(Canvas c) {
        this.canvas = c;
    }

    public void drawArena(Canvas canvas, int gridSize) {
        setCanvas(canvas);
        this.size = gridSize;

        int rows = gridSettings[0],  //15
                cols = gridSettings[1],  //20
                rHeadX = gridSettings[2],  // center of the head = 3
                rHeadY = gridSettings[3],  // 2
                rRobotX = gridSettings[4],  // center of the robot = 2
                rRobotY = gridSettings[5]; // 2

        //Log.e(TAG, "rHeadX: " + rHeadX);
        //Log.e(TAG, "rHeadY: " + rHeadY);
        //Log.e(TAG, "rRobotX: " + rRobotX);
        //Log.e(TAG, "rRobotY: " + rRobotY);

        boolean facingUpDown = false, facingLeftRight = false;
        Paint arenaPaint = new Paint();
        arenaPaint.setColor(Color.parseColor("#FFF9C4"));
        viewWidth = gridSize*cols ;
        viewHeight = gridSize*rows;
        canvas.drawRect(new RectF(0, 0, viewWidth-1, viewHeight-1),arenaPaint);

        // Draw background
        for (int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                drawBox(j, i, gridSize, Color.parseColor("#FFF9C4"), canvas);
            }
        }

        // Draw Start zone
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                drawBox(j, i, gridSize, Color.WHITE, canvas);
            }
        }

        // Draw Goal zone
        for (int i = rows - 3; i <= rows-1; i++) {
            for (int j = cols - 3; j <= cols-1; j++) {
                drawBox(j, i, gridSize, Color.parseColor("#bdbdbd"), canvas);
            }
        }

        if (rHeadX == rRobotX) {
            facingUpDown = true;
            facingLeftRight = false;
        }

        if (rHeadY == rRobotY) {
            facingUpDown = false;
            facingLeftRight = true;
        }

        // Draw robot
        for (int x = rRobotX - 2; x <= rRobotX; x++) {
            for (int y = rRobotY - 2; y <= rRobotY; y++) {
                drawBox(x, y, gridSize, Color.parseColor("#555555"), canvas);
            }
        }

        if (facingUpDown) {
            int y=rHeadY-1;
            for (int x =rHeadX-2;x<=rHeadX;x++){
                drawBox(x, y, gridSize, Color.parseColor("#777777"), canvas);
            }
        }

        if (facingLeftRight){
            //head of the robot
            int x = rHeadX-1;
            for (int y = rHeadY - 2; y <= rHeadY; y++) {
                drawBox(x, y, gridSize, Color.parseColor("#777777"), canvas);
            }
        }

        // Draw obstacles
        for (int i = 0; i < 15; i++){
            for(int j = 0; j < 20; j++) {
                if (this.obstacleArray[i][j] == 0) {      // unexplored
                    drawBox(j, i, gridSize, Color.TRANSPARENT, canvas);
                }
                else if (this.obstacleArray[i][j] == 1){  // empty
                    drawBox(j, i, gridSize, Color.parseColor("#000000"), canvas);
                }
                else{                                // obstacle
                    drawBox(j, i, gridSize, Color.parseColor("#000000"), canvas);
                }
            }

        }
    }

    public void drawBox(int x, int y, int gridSize, int color, Canvas canvas){
        left = x * gridSize;
        top = y * gridSize;
        right = x * gridSize + gridSize;
        bottom = y * gridSize + gridSize;

        Paint boxPaint = new Paint();
        boxPaint.setColor(color);
        boxPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(new RectF(left,top,right,bottom),boxPaint);

        boxPaint.setColor(Color.BLACK);
        boxPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new RectF(left, top, right, bottom), boxPaint);
    }

    public void setGridSettings(int[] gridArray) {
        this.gridSettings = gridArray;
    }

    public void setObstacles(int[][] obstacleArray) {
        this.obstacleArray = obstacleArray;
    }

    public void drawWaypoint(Canvas canvas, float wayPointX, float wayPointY, boolean isSetWayPoint) {
        Paint waypointTextPaint = new Paint();
        waypointTextPaint.setColor(Color.BLACK);
        waypointTextPaint.setStrokeWidth(40);
        waypointTextPaint.setTextSize(20);

        if (isSetWayPoint){
            drawBox((int)wayPointX, (int)wayPointY, 34, Color.YELLOW, canvas);
            String strInt = "x: " + (int)wayPointX + ", y: " + (int)(wayPointY);
            canvas.drawText(strInt, wayPointX*34, wayPointY*34+50, waypointTextPaint);
        }
    }

    public void drawBoxOnTouch(Canvas canvas, float wayPointX, float wayPointY, boolean allowDrawPointBox) {
        Paint waypointTextPaint = new Paint();
        waypointTextPaint.setColor(Color.BLACK);
        waypointTextPaint.setStrokeWidth(40);
        waypointTextPaint.setTextSize(20);

        if (allowDrawPointBox){
            drawBox((int)wayPointX,(int)wayPointY,34,Color.GREEN,canvas);
            String strInt = "x: "+(int)wayPointX+", y: "+(int)wayPointY;
            if (wayPointX > 16 && wayPointY < 12){
                canvas.drawText(strInt,(wayPointX - 4)*34,wayPointY*34+50,waypointTextPaint);
            }
            else if (wayPointX > 16 && wayPointY > 12) {
                canvas.drawText(strInt,(wayPointX - 4)*34,(wayPointY-3)*34,waypointTextPaint);
            }
            else if (wayPointX < 3 && wayPointY > 13){
                canvas.drawText(strInt,wayPointX*34,(wayPointY-3)*34,waypointTextPaint);
            }
            else{
                canvas.drawText(strInt,wayPointX*34,wayPointY*34+50,waypointTextPaint);
            }
        }
    }

}
