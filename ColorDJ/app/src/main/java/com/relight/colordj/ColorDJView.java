package com.relight.colordj;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Patrick on 3/22/2015.
 */
public class ColorDJView extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = ColorDJView.class.getSimpleName();
    private static final double HEIGHT_0_HUE = .0375;
    private static final double HEIGHT_1_HUE = .825;
    private static final double HEIGHT_CMY_LIMIT = .8625;
    private static final double HEIGHT_K_LIMIT = .9;
    private static final double WIDTH_ONE_THIRD = .33;
    private static final double WIDTH_TWO_THIRDS = .67;
    private static final double WIDTH_0_BRIGHT = .062;
    private static final double WIDTH_1_BRIGHT = .936;
    private static final double HEIGHT_BRIGHT = .9375;
    private static final double WIDTH_C = .166;
    private static final double WIDTH_M = .5;
    private static final double WIDTH_Y = .833;

    Context context;
    SurfaceHolder surfaceHolder;
    ColorDJThread thread;

    private ColorDJColorInterface colorSender;
    private Bitmap gridBitmap;

    private boolean update = true;
    private double cyanMult = 1;
    private double magentaMult = 1;
    private double yellowMult = 1;
    private double blackMult = 1;
    private double minDif = .015;


    /*
    var redMult:Number = 1;
var greenMult:Number = 1;
var blueMult:Number = 1;
var cyanMult:Number = 1;
var magentaMult:Number = 1;
var yellowMult:Number = 1;
var blackMult:Number = 1;
     */

    public ColorDJView(Context ctx){
        super(ctx);
        init();
    }

    public ColorDJView(Context ctx, AttributeSet attrs){
        super(ctx, attrs);
        init();
    }

    public ColorDJView(Context ctx, AttributeSet attrs, int defStyle){
        super(ctx, attrs, defStyle);
        init();
    }

    private void init(){
        surfaceHolder = getHolder();
        getHolder().addCallback(this);
    }

    public void receiveSender(ColorDJColorInterface face){
        colorSender = face;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        for(int i= 0; i < e.getPointerCount(); i++) {
            int width = getWidth();
            int height = getHeight();
            float x = e.getX(e.findPointerIndex(e.getPointerId(i))) / width;
            float y = e.getY(e.findPointerIndex(e.getPointerId(i))) / height;
            double newMult = 0;

            update = false;
            Log.d(TAG, "#CDJ: height = " + getHeight());

            if (y < HEIGHT_CMY_LIMIT) {
                //cmy
                if (y > HEIGHT_0_HUE) {
                    if (y < HEIGHT_1_HUE) {
                        newMult = (y - HEIGHT_0_HUE) / (HEIGHT_1_HUE - HEIGHT_0_HUE);
                    } else {
                        newMult = 1;
                    }
                }

                if (x <= WIDTH_ONE_THIRD) { //c
                    if(Math.abs(newMult - magentaMult) <= minDif && cyanMult != newMult){
                        cyanMult = magentaMult;
                        update = true;
                    } else if(Math.abs(newMult - yellowMult) <= minDif && cyanMult != newMult){
                        cyanMult = yellowMult;
                        update = true;
                    } else if(Math.abs(newMult - cyanMult) >= minDif || newMult == 0 && cyanMult != 0 || newMult == 1 && cyanMult != 1) {
                        cyanMult = newMult;
                        update = true;
                    }
                } else if (x < WIDTH_TWO_THIRDS) { //m
                    if(Math.abs(newMult - cyanMult) <= minDif && magentaMult != newMult){
                        magentaMult = cyanMult;
                        update = true;
                    } else if(Math.abs(newMult - yellowMult) <= minDif && magentaMult != newMult){
                        magentaMult = yellowMult;
                        update = true;
                    } else if(Math.abs(newMult - magentaMult) >= minDif || newMult == 0 && magentaMult != 0 || newMult == 1 && magentaMult != 1) {
                        magentaMult = newMult;
                        update = true;
                    }
                } else {//y
                    if(Math.abs(newMult - cyanMult) <= minDif && yellowMult != newMult){
                        yellowMult = cyanMult;
                        update = true;
                    } else if(Math.abs(newMult - magentaMult) <= minDif && yellowMult != newMult){
                        yellowMult = magentaMult;
                        update = true;
                    } else if(Math.abs(newMult - yellowMult) >= minDif || newMult == 0 && yellowMult != 0|| newMult == 1 && yellowMult != 1) {
                        yellowMult = newMult;
                        update = true;
                    }
                }

            } else if (y > HEIGHT_K_LIMIT) {
                //k
                if (x > WIDTH_0_BRIGHT) {
                    if (x < WIDTH_1_BRIGHT) {
                        newMult = (x - WIDTH_0_BRIGHT) / (WIDTH_1_BRIGHT - WIDTH_0_BRIGHT);
                    } else {
                        newMult = 1;
                    }
                }

                if(Math.abs(newMult - blackMult) >= minDif * 1.5 || newMult == 0 || newMult == 1){
                    blackMult = newMult;
                    update = true;
                }
            }

            if(update && colorSender != null){
                colorSender.colorChanged(((int)(255 * (cyanMult / 3 + cyanMult * blackMult / 3 + blackMult / 3)) * 256 * 256) + ((int)(255 * (magentaMult / 3 + magentaMult * blackMult / 3 + blackMult / 3)) * 256) + (int)(255 * (yellowMult / 3 + yellowMult * blackMult / 3 + blackMult / 3)));
            }
        }

        //Log.d(TAG, "#CDJ " + cyanMult + ", " + magentaMult + ", " + yellowMult + ", " + blackMult);

        return true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        try{
            thread.setRunning(false);
            thread.join();
        }catch(InterruptedException e){}
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        thread = new ColorDJThread(holder, context, this);
        thread.setRunning(true);
        thread.start();

        minDif = .015 * 1022/getHeight();
        drawGrid();
        update = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        //back color
        canvas.drawARGB(255, (int) (255 * (cyanMult / 3 + cyanMult * blackMult / 3 + blackMult / 3)), (int) (255 * (magentaMult / 3 + magentaMult * blackMult / 3 + blackMult / 3)), (int) (255 * (yellowMult / 3 + yellowMult * blackMult / 3 + blackMult / 3)));

        //bubble borders
        Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.STROKE);
        bubblePaint.setStrokeWidth((float) (.0125 * width));
        bubblePaint.setARGB(150, 100, 100, 100);
        canvas.drawCircle((float) (WIDTH_C * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .035), bubblePaint);
        canvas.drawCircle((float) (WIDTH_M * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .035), bubblePaint);
        canvas.drawCircle((float) (WIDTH_Y * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .035), bubblePaint);
        canvas.drawCircle((float) ((blackMult * (WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) + WIDTH_0_BRIGHT) * width), (float) (HEIGHT_BRIGHT * height), (float) (width * .035), bubblePaint);

        //bubble colors
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setARGB(175, (int) ((cyanMult / 3 + cyanMult * blackMult / 3 + blackMult / 3) * 255), (int) ((1 - (.5 - blackMult / 2)) * 255), (int) ((1 - (.5 - blackMult / 2)) * 255));
        canvas.drawCircle((float) (WIDTH_C * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .036), bubblePaint);
        bubblePaint.setARGB(125, (int) ((1 - (.5 - blackMult / 2)) * 255), (int) ((magentaMult / 3 + magentaMult * blackMult / 3 + blackMult / 3) * 255), (int) ((1 - (.5 - blackMult / 2)) * 255));
        canvas.drawCircle((float) (WIDTH_M * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .036), bubblePaint);
        bubblePaint.setARGB(125, (int) ((1 - (.5 - blackMult / 2)) * 255), (int) ((1 - (.5 - blackMult / 2)) * 255), (int) ((yellowMult / 3 + yellowMult * blackMult / 3 + blackMult / 3) * 255));
        canvas.drawCircle((float) (WIDTH_Y * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) (width * .036), bubblePaint);
        bubblePaint.setARGB(125, (int) ((1 - (.75 - 3 * blackMult / 4)) * 255), (int) ((1 - (.75 - 3 * blackMult / 4)) * 255), (int) ((1 - (.75 - 3 * blackMult / 4)) * 255));
        canvas.drawCircle((float) ((blackMult * (WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) + WIDTH_0_BRIGHT) * width), (float) (HEIGHT_BRIGHT * height), (float) (width * .036), bubblePaint);

        //guide lines
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth((float) (.01 * width));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setARGB(125, 100, 100, 100);
        canvas.drawLine((float) ((WIDTH_M - .02) * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_M + .02) * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .02) * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_Y + .02) * width), (float) ((cyanMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_C - .02) * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_C + .02) * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .02) * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_Y + .02) * width), (float) ((magentaMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_C - .02) * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_C + .02) * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .02) * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), (float) ((WIDTH_M + .02) * width), (float) ((yellowMult * (HEIGHT_1_HUE - HEIGHT_0_HUE) + HEIGHT_0_HUE) * height), linePaint);

        canvas.drawBitmap(gridBitmap, 0, 0, null);
        update = false;
    }

    private void drawGrid(){
        int width = getWidth();
        int height = getHeight();

        gridBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(gridBitmap);
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth((float) (.01 * width));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        //middle marks
        linePaint.setARGB(100, 200, 200, 200);
        canvas.drawLine((float) ((WIDTH_C - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), (float) ((WIDTH_C + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_C - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), (float) ((WIDTH_C + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_C - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), (float) ((WIDTH_C + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), (float) ((WIDTH_M + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), (float) ((WIDTH_M + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), (float) ((WIDTH_M + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), (float) ((WIDTH_Y + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .25) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), (float) ((WIDTH_Y + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .5) + HEIGHT_0_HUE) * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), (float) ((WIDTH_Y + .002) * width), (float) ((((HEIGHT_1_HUE - HEIGHT_0_HUE) * .75) + HEIGHT_0_HUE) * height), linePaint);

        canvas.drawLine((float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .25) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) - (.002 * width)), (float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .25) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) + (.002 * width)), linePaint);
        canvas.drawLine((float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .5) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) - (.002 * width)), (float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .5) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) + (.002 * width)), linePaint);
        canvas.drawLine((float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .75) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) - (.002 * width)), (float) ((((WIDTH_1_BRIGHT - WIDTH_0_BRIGHT) * .75) + WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) + (.002 * width)), linePaint);

        //edge marks
        linePaint.setARGB(100, 150, 150, 150);
        canvas.drawLine((float) ((WIDTH_C - .02) * width), (float) (HEIGHT_0_HUE * height), (float) ((WIDTH_C + .02) * width), (float) (HEIGHT_0_HUE * height), linePaint);
        canvas.drawLine((float) ((WIDTH_C - .02) * width), (float) (HEIGHT_1_HUE * height), (float) ((WIDTH_C + .02) * width), (float) (HEIGHT_1_HUE * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .02) * width), (float) (HEIGHT_0_HUE * height), (float) ((WIDTH_M + .02) * width), (float) (HEIGHT_0_HUE * height), linePaint);
        canvas.drawLine((float) ((WIDTH_M - .02) * width), (float) (HEIGHT_1_HUE * height), (float) ((WIDTH_M + .02) * width), (float) (HEIGHT_1_HUE * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .02) * width), (float) (HEIGHT_0_HUE * height), (float) ((WIDTH_Y + .02) * width), (float) (HEIGHT_0_HUE * height), linePaint);
        canvas.drawLine((float) ((WIDTH_Y - .02) * width), (float) (HEIGHT_1_HUE * height), (float) ((WIDTH_Y + .02) * width), (float) (HEIGHT_1_HUE * height), linePaint);

        canvas.drawLine((float) ((WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) - (.02 * width)), (float) ((WIDTH_0_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) + (.02 * width)), linePaint);
        canvas.drawLine((float) ((WIDTH_1_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) - (.02 * width)), (float) ((WIDTH_1_BRIGHT) * width), (float) ((HEIGHT_BRIGHT * height) + (.02 * width)), linePaint);
    }

    public boolean getUpdate(){
        return update;
    }

    public class ColorDJThread extends Thread{
        SurfaceHolder surfaceHolder;
        Context context;
        boolean isRun;
        ColorDJView colorDJView;
        Canvas canvas;
        public ColorDJThread(SurfaceHolder holder, Context ctx, ColorDJView view){
            surfaceHolder = holder;
            context = ctx;
            isRun = false;
            colorDJView = view;
        }

        void setRunning(boolean running){
            isRun = running;
        }

        @Override
        public void run(){
            super.run();
            while(isRun){
                if(colorDJView.getUpdate()) {
                    canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        colorDJView.postInvalidate();
                        colorDJView.onDraw(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

    }
}
