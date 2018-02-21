package com.example.konoj.mdp2018_grp12.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;

import com.example.konoj.mdp2018_grp12.R;

public class PixelGridView extends View {
    //private int numColumns, numRows;
    private int numColumns = 15;
    private int numRows = 20;
    private int cellWidth, cellHeight;
    private Paint blackPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint bluePaint = new Paint();
    private boolean[][] cellChecked;
    private int[][] cellType;

    private boolean[][] cellFront;
    private boolean[][] cellRear;

    //north 0, south 180, east 90, west 270
    private int currentAngle;
    Bitmap rightArrow;
    Bitmap leftArrow;
    Bitmap upArrow;
    Bitmap downArrow;
    PixelGridView pgv;
    private float scale = 1f;
    private ScaleGestureDetector SGD;



    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SGD = new ScaleGestureDetector(context, new ScaleListener());
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        greenPaint.setColor(Color.GREEN);
        redPaint.setColor(Color.RED);
        whitePaint.setColor(Color.WHITE);
        bluePaint.setColor(Color.BLUE);

        rightArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.right_icon_32);
        leftArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.left_icon_32);
        upArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.up_icon_32);
        downArrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.down_icon_32);
    }




    public void setCoordinates(int coor1,int coor2){
        Log.e("coor",coor1+" ,"+coor2);
        cellFront[coor1][coor2]=true;
        cellRear[coor1][coor2-1]=true;
        calculateDimensions(coor1,coor2);
        //System.out.println(cellFront);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(0,0);
    }

    private void calculateDimensions(int x ,  int y) {
        if (x == 0 && y == 0) {
            if (numColumns == 0 || numRows == 0)
                return;

            cellWidth = getWidth() / numColumns;
            cellHeight = getHeight() / numRows;

            cellChecked = new boolean[numRows][numColumns];

            cellFront = new boolean[numRows][numColumns];
            cellRear = new boolean[numRows][numColumns];
            cellType = new int[numRows][numColumns];

            //default cellChecked in the middle
            //AMD test
//            cellFront[0][1] = true;
//            cellRear[0][0] = true;
            //actual robot
        cellChecked[18][0] = true;
        cellChecked[18][1] = true;
        cellChecked[19][0] = true;
        cellChecked[19][1] = true;
            currentAngle = 0;
        } else {
            cellWidth = getWidth() / numColumns;
            cellHeight = getHeight() / numRows;

            cellChecked = new boolean[numRows][numColumns];

            cellFront = new boolean[numRows][numColumns];
            cellRear = new boolean[numRows][numColumns];
            cellType = new int[numRows][numColumns];

            //default cellChecked in the middle
            //AMD test
//            cellFront[x][y]=true;
//            cellRear[x][y-1]=true;
            //actual robot
            cellChecked[18][0] = true;
            cellChecked[18][1] = true;
            cellChecked[19][0] = true;
            cellChecked[19][1] = true;
            currentAngle = 0;

            invalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //System.out.println("Hi");
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            // matrix.setScale(scale, scale);
            //img.setImageMatrix(matrix);

            invalidate();
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(scale, scale);
        canvas.drawColor(Color.WHITE);

        if (numColumns == 0 || numRows == 0)
            return;

        int width = getWidth();
        int height = getHeight();

        //AMD test
       /* for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                //cellChecked[i][j]=true;
                if(cellChecked[i][j]) {
                    System.out.println("Green");
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
               }
                if (cellRear[i][j]) {
                    canvas.drawRect(j * cellWidth, (numRows - 1 - i) * (cellHeight), (j + 1) * cellWidth, (numRows - 1 - i + 1) * (cellHeight), bluePaint);
                    //canvas.drawRect((j-1) * cellWidth, (numRows-1-i) * (cellHeight), (j) * cellWidth, (numRows-1-i + 1) * (cellHeight), greenPaint);
                    cellChecked[i][j] = true;
                    //System.out.println("Blue");
                } else if (cellFront[i][j]) {
                    canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, redPaint);
                    // System.out.println("Red");
                } else {
                    if (cellType[i][j] == 0) {
                        canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, whitePaint);
                        //System.out.println("White");
                    } else if (cellType[i][j] == 1) {
                        canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, blackPaint);
                        // System.out.println("Black");
                    }

                }

                if (cellChecked[i][j]) {
                    System.out.println("Green");
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                    System.out.println(cellWidth);
                    System.out.println(cellHeight);

                }
            }

        }
*/
        //actual robot
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (cellChecked[i][j]) {
                	//cellChecked = current robot position, cellChecked in green
                    //canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                	if (currentAngle == 0) {
                		canvas.drawBitmap(upArrow, j * cellWidth, i * cellHeight, null);
                	} else if (currentAngle == 90) {
                		canvas.drawBitmap(rightArrow, j * cellWidth, i * cellHeight, null);
                	} else if (currentAngle == 180) {
                		canvas.drawBitmap(downArrow, j * cellWidth, i * cellHeight, null);
                	} else if (currentAngle == 270) {
                		canvas.drawBitmap(leftArrow, j * cellWidth, i * cellHeight, null);
                	}
                } else {

	                if (cellType[i][j] == 0) {
	                	//red
	                	//unexplored, define to do
	                	canvas.drawRect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight, whitePaint);
	                } else if (cellType[i][j] == 1) {
	                	//white
	                	//empty, define to do
	                	canvas.drawRect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight, whitePaint);
	                } else if (cellType[i][j] == 2) {
	                	//obstacle, color black
	                	canvas.drawRect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight, blackPaint);
	                } else if (cellType[i][j] == 3) {
	                	//green
	                	canvas.drawRect(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight, greenPaint);
	                }
                }
            }
        }

        //drawing lines
        for (int i = 0; i < numColumns+1; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 0; i < numRows+1; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }
        canvas.restore();
    }

    /*public void moveRight() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        cellChecked[i][j]=true;
                        rowFront = i;
                        columnFront = j;
                        System.out.println(i);
                        System.out.println(j);

                    }
                }
            }
            if(columnFront+1 < numColumns && columnFront >=0) {
                if (cellType[rowFront][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront][columnFront+1] = !cellFront[rowFront][columnFront+1];
                    cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                }
            }
//    		else if (columnFront+1 == numColumns) {
//    			if (cellFront[rowFront][columnFront]) {
//    				cellRear[rowFront][columnFront-1] = !cellRear[rowFront][columnFront-1]; 
//        			cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
//        			cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
//    			}
//    		}
        } else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns) {
                if (cellType[rowFront][columnFront+1] != 1 && cellType[rowFront-1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront+1] = !cellFront[rowFront-1][columnFront+1];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront+1 != numColumns ) {
                if (cellType[rowFront][columnFront+1] != 1 && cellType[rowFront+1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    currentAngle = 0;
                }
            }
        } else if (currentAngle == 180) {

        }
        invalidate();
    }
    public void moveLeft() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront][columnFront-1] = !cellFront[rowFront][columnFront-1];
                    cellRear[rowFront][columnFront+1] = !cellRear[rowFront][columnFront+1];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                }
            }
        } else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1 && cellType[rowFront-1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront-1][columnFront-1] = !cellFront[rowFront-1][columnFront-1];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(columnFront-1 != -1) {
                if (cellType[rowFront][columnFront-1] != 1 && cellType[rowFront+1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    currentAngle = 180;
                }
            }
        } else if (currentAngle == 0) {

        }
        invalidate();
    }*/

    public void moveDown() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1 && cellType[rowFront+1][columnFront-1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront-1] = !cellFront[rowFront+1][columnFront-1];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront] = !cellFront[rowFront+1][columnFront];
                    cellRear[rowFront-1][columnFront] = !cellRear[rowFront-1][columnFront];
                    cellRear[rowFront][columnFront] = !cellRear[rowFront][columnFront];
                }
            }
        } else if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if(rowFront+1 != numRows) {
                if (cellType[rowFront+1][columnFront] != 1 && cellType[rowFront+1][columnFront+1] != 1) {
                    cellFront[rowFront][columnFront] = !cellFront[rowFront][columnFront];
                    cellFront[rowFront+1][columnFront+1] = !cellFront[rowFront+1][columnFront+1];
                    currentAngle = 90;
                }
            }
        } else if (currentAngle == 270){

        }
        invalidate();
    }


    //actual robot
    public void moveForward() {
    	int column = -1;
    	int row = -1;
    	if (currentAngle == 0) {

            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellChecked[i][j]) {
                        column = i;
                        row = j;
                    }
                }
            }
            //single grid movement
//	        cellChecked[column][row] = !cellChecked[column][row];
//	    	cellChecked[column-1][row] = !cellChecked[column-1][row];

            //uncheck cells
            if(column-2 != -1) {
                cellChecked[column][row] = !cellChecked[column][row];
                cellChecked[column][row-1] = !cellChecked[column][row-1];
                //check cells
                cellChecked[column-2][row] = !cellChecked[column-2][row];
                cellChecked[column-2][row-1] = !cellChecked[column-2][row-1];
            }

            invalidate();


    	} else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
	                if (cellChecked[i][j]) {
	                    column = i;
	                    row = j;
	                }
	            }
	        }
    		//single grid movement
//    		cellChecked[column][row] = !cellChecked[column][row];
//    		cellChecked[column][row+1] = !cellChecked[column][row+1];

    		 if(row+1 != numRows) {
	    		//uncheck cells
		    	cellChecked[column-1][row-1] = !cellChecked[column-1][row-1];
		    	cellChecked[column][row-1] = !cellChecked[column][row-1];
		    	//check cells
		    	cellChecked[column-1][row+1] = !cellChecked[column-1][row+1];
		    	cellChecked[column][row+1] = !cellChecked[column][row+1];
    		 }

	    	invalidate();
    	} else if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (cellChecked[i][j]) {
                        column = i;
                        row = j;

                    }
                }
            }
            //single grid movement
//	        cellChecked[column][row] = !cellChecked[column][row];
//	    	cellChecked[column+1][row] = !cellChecked[column+1][row];
            //last cell checked == [1][1]

            if(column+1 != numColumns) {
//	        	//4 grids
//	        	//uncheck cells
                cellChecked[column-1][row-1] = !cellChecked[column-1][row-1];
                cellChecked[column-1][row] = !cellChecked[column-1][row];
                //check cellls
                cellChecked[column+1][row] = !cellChecked[column+1][row];
                cellChecked[column+1][row-1] = !cellChecked[column+1][row-1];
//	        	cellChecked[column-1][row] = !cellChecked[column-1][row];
//	        	cellChecked[column+1][row] = !cellChecked[column+1][row];

            }

            invalidate();
    	} else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
	                if (cellChecked[i][j]) {
	                    column = i;
	                    row = j;
	                }
	            }
	        }
    		//single grid movement
//	        cellChecked[column][row] = !cellChecked[column][row];
//	    	cellChecked[column][row-1] = !cellChecked[column][row-1];

    		if(row-2 != -1) {
	    		//uncheck cells
	    		cellChecked[column][row] = !cellChecked[column][row];
	    		cellChecked[column-1][row] = !cellChecked[column-1][row];
	    		//check cells
	    		cellChecked[column][row-2] = !cellChecked[column][row-2];
	    		cellChecked[column-1][row-2] = !cellChecked[column-1][row-2];
    		}

    		invalidate();
    	}
    }
//
    public void moveRight() {
    	if (currentAngle == 0) {
    		currentAngle = 90;
    	} else if (currentAngle == 90) {
    		currentAngle = 180;
    	} else if (currentAngle == 180) {
    		currentAngle = 270;
    	} else {
    		currentAngle = 0;
    	}
    	invalidate();
    }

    public void moveLeft() {
    	if (currentAngle == 0) {
    		currentAngle = 270;
    	} else if (currentAngle == 90) {
    		currentAngle = 0;
    	} else if (currentAngle == 180) {
    		currentAngle = 90;
    	} else {
    		currentAngle = 180;
    	}
    	invalidate();
    }
//    
    public void updateMap() {

        invalidate();
    }

    public void mapInString(String map, int column, int row, String direction) {


        if (direction.equalsIgnoreCase("n")) {
            currentAngle = 0;
        } else if (direction.equalsIgnoreCase("e")) {
            currentAngle = 90;
        } else if (direction.equalsIgnoreCase("s")) {
            currentAngle = 180;
        } else if (direction.equalsIgnoreCase("w")) {
            currentAngle = 270;
        }

        String mapFilter = map.replaceAll(" ", "");
        //String mapFilter = map.replaceAll(",", "");

        for (int i = numRows; i > 0; i--) {
            for (int j = 0; j < numColumns; j++) {
                if (mapFilter.length() != 0) {
                    cellType[j][i] = Integer.parseInt(mapFilter.substring(0,1));
                    mapFilter = mapFilter.substring(1);
                }
            }
        }

        invalidate();
    }

    public void mapInStringAMD(String map, int column, int row) {
        int colBlue = 0;
        int rowBlue = 0;

        int colRed = 1;
        int rowRed = 0;
//    	cellChecked[colBlue][rowBlue] = !cellChecked[rowBlue][rowBlue];
//    	cellChecked[colRed][rowRed] = !cellChecked[colRed][rowRed];


        String mapFilter = map.replaceAll(" ", "");

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if (mapFilter.length() != 0) {
                    cellType[i][j] = Integer.parseInt(mapFilter.substring(0,1));
                    mapFilter = mapFilter.substring(1);
                }
            }
        }
        invalidate();
    }
}
