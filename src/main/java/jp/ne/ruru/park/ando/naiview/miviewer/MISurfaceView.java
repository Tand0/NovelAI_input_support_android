package jp.ne.ruru.park.ando.naiview.miviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import jp.ne.ruru.park.ando.naiview.R;

public class MISurfaceView extends SurfaceView {
    private MIViewerData settingData = null;

    private SurfaceHolder holder;

    private ScheduledExecutorService executor;

    private STATE scheduleStateFlag = STATE.START;
    public enum STATE {
        START,
        REPOSITION,
        MOVING,
        VIBRATION,
        BOX
    }
    /**
     * detector for scale
     */
    private ScaleGestureDetector scaleGestureDetector;

    /**
     * detector for click
     */
    private GestureDetector gestureDetector;

    private Runnable finishListener;

    private Bitmap bitmapBase = null;

    private long timeMoveBase = 0;

    private Matrix matrixNow = null;


    public static final int MESH_MAX = 20;
    public Bitmap bitmapMesh = null;
    public float[] meshArray;
    public int meshXMax;
    public int meshYMax;


    public MISurfaceView(Context context) {
        this(context,null);
    }

    public MISurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MISurfaceView(Context context, AttributeSet attrs,
                         int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    public void init(Context context) {
        holder = getHolder();
        holder.addCallback(myCallback);
        setFocusable(true);
        requestFocus();
        //
        scaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureDetector);
        gestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        //
        scheduleStateFlag = STATE.START;
    }
    public void setFinishListener(Runnable finishListener) {
        this.finishListener = finishListener;
    }
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean flagX = false;
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            flagX = mSimpleOnGestureListener.onUp(event);
        }
        boolean flagA = flagX || scaleGestureDetector.onTouchEvent(event);
        boolean flagB = gestureDetector.onTouchEvent(event);
        flagA = flagX || flagA || flagB;
        if (flagA) {
            performClick();
        }
        return flagA;
    }
    protected SurfaceHolder.Callback myCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            scheduleStop();
        }
        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder,
                                   int format, int width, int height) {
            scheduleStop();
            scheduleStateFlag = STATE.START;
            changeStateStart(0);
        }
        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            scheduleStop();
        }
    };

    public ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureDetector = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            if (scheduleStateFlag == STATE.MOVING) {
                return super.onScaleBegin(detector);
            }
            scheduleStop();
            //
            //
            if ((scheduleStateFlag == STATE.VIBRATION)
                    || (scheduleStateFlag == STATE.BOX)) {
                if (timeMoveBase == 0) {
                    timeMoveBase = System.currentTimeMillis();
                }
                return true;
            }
            return super.onScaleBegin(detector);
        }
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (scheduleStateFlag == STATE.MOVING) {
                return super.onScale(detector);
            }
            scheduleStop();
            //
            float lastScaleFactor = detector.getScaleFactor();
            if (lastScaleFactor == 1.0) {
                return super.onScale(detector);
            }
            long time = System.currentTimeMillis() - timeMoveBase;
            float touchPointX = detector.getFocusX();
            float touchPointY = detector.getFocusY();
            if (scheduleStateFlag == STATE.VIBRATION) {
                PlotScale plotScale = new PlotScale();
                plotScale.time = time;
                plotScale.lastScaleFactor = lastScaleFactor;
                plotScale.touchPointX = touchPointX;
                plotScale.touchPointY = touchPointY;
                settingData.getPlotList().add(plotScale);
            }
            if ((scheduleStateFlag == STATE.REPOSITION)
                    || (scheduleStateFlag == STATE.VIBRATION)) {
                imageViewScale(lastScaleFactor, touchPointX, touchPointY);
                draw();
                return true;
            }
            return super.onScale(detector);
        }
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            // Intentionally empty
        }
    };
    public interface OnGestureListener extends GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        boolean onUp(@NonNull MotionEvent e);
    }
    /**
     * detector for click
     */
    public OnGestureListener mSimpleOnGestureListener = new OnGestureListener() {

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            if (scheduleStateFlag == STATE.MOVING) {
                return false;
            }
            scheduleStop();
            //
            if ((scheduleStateFlag == STATE.VIBRATION)
                    || (scheduleStateFlag == STATE.BOX)) {
                if (timeMoveBase == 0) {
                    timeMoveBase = System.currentTimeMillis();
                }
            }
            if (scheduleStateFlag == STATE.VIBRATION) {
                return true;
            } else if (scheduleStateFlag == STATE.BOX) {
                Box box = new Box();
                final float[] nowFloat = new float[9];
                matrixNow.getValues(nowFloat);
                float displayX = e.getX();
                float displayY = e.getY();
                float matrixTransX = nowFloat[Matrix.MTRANS_X];
                float matrixTransY = nowFloat[Matrix.MTRANS_Y];
                float scale = nowFloat[Matrix.MSCALE_X];
                box.startX = getDisplayXYtoBitmapXY(displayX,matrixTransX,scale);
                box.startY = getDisplayXYtoBitmapXY(displayY,matrixTransY,scale);
                box.endX = box.startX;
                box.endY = box.startY;
                settingData.getBoxList().add(box);
                return true;
            }
            return false;
        }
        @Override
        public void onShowPress(@NonNull MotionEvent e) {
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            if (scheduleStateFlag == STATE.MOVING) {
                return false;
            }
            scheduleStop();
            //
            long time = System.currentTimeMillis() - timeMoveBase;
            if ((distanceX == 0.0f) && (distanceY == 0.0f)) {
                return false;
            }
            if (scheduleStateFlag == STATE.REPOSITION) {
                imageViewMove(distanceX, distanceY);
                draw();
                return true;
            } else if (scheduleStateFlag == STATE.VIBRATION) {
                PlotDistance plot = new PlotDistance();
                plot.time = time;
                plot.distanceX = distanceX;
                plot.distanceY = distanceY;
                settingData.getPlotList().add(plot);
                imageViewMove(distanceX, distanceY);
                draw();
                return true;
            } else if (scheduleStateFlag == STATE.BOX) {
                if (! settingData.getBoxList().isEmpty()) {
                    Box box = settingData.getBoxList().getLast();
                    final float[] nowFloat = new float[9];
                    matrixNow.getValues(nowFloat);
                    float displayX = e2.getX();
                    float displayY = e2.getY();
                    float matrixTransX = nowFloat[Matrix.MTRANS_X];
                    float matrixTransY = nowFloat[Matrix.MTRANS_Y];
                    float scale = nowFloat[Matrix.MSCALE_X];
                    box.endX = getDisplayXYtoBitmapXY(displayX,matrixTransX,scale);
                    box.endY = getDisplayXYtoBitmapXY(displayY,matrixTransY,scale);
                }
                draw();
                return true;
            }
            return false;
        }
        @Override
        public void onLongPress(@NonNull MotionEvent e) {
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            return false; // for onUp()
        }
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
                               float velocityY) {
            return false; // for onUp()
        }
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            return false; // for onUp()
        }
        @Override
        public boolean onUp(@NonNull MotionEvent e) {
            scheduleStop();
            //
            switch (scheduleStateFlag) {
                case MOVING:
                case REPOSITION:
                case VIBRATION:
                case BOX:
                    changeStateEnd(scheduleStateFlag);
                    return true;
                default:
                    break;
            }
            return false;
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            return false;
        }

        /**
         * Notified when an event within a double-tap gesture occurs, including
         * the down, move, and up events.
         *
         * @param e The motion event that occurred during the double-tap gesture.
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return false;
        }
    };
    public void setSettingData(MIViewerData settingData) {
        this.settingData = settingData;
    }
    public void setImageBitMap(Bitmap b) {
        scheduleStop();
        this.bitmapBase = b;
        this.scheduleStateFlag = STATE.START;
    }
    public void changeStateStart(int nextStateId) {
        scheduleStop();
        //
        if ((getHeight() == 0) || (getHeight() == 0) || (bitmapBase == null)) {
            return;
        }
        if ((scheduleStateFlag == STATE.START)) {
            changeBitmap();
            changeMatrixBase();
            nextStateId = 0; // moving
        }
        if (nextStateId == R.id.mi_reset) {
            matrixNow.setValues(settingData.baseFloat);
            matrixNow.getValues(settingData.baseMovingFloat);
            settingData.getPlotList().clear();
            settingData.reset();
            settingData.getBoxList().clear();
            nextStateId = 0; //  moving
        }
        if (nextStateId == R.id.mi_reposition) {
            matrixNow.setValues(settingData.baseMovingFloat);
            plotIndex = -1;
            timeMoveBase = 0;
            speedX = 0;
            speedY = 0;
            scheduleStateFlag = STATE.REPOSITION;
        } else if (nextStateId == 0) { // moving
            matrixNow.setValues(settingData.baseMovingFloat);
            plotIndex = -1;
            timeMoveBase = 0;
            speedX = 0;
            speedY = 0;
            timeMoveBase = System.currentTimeMillis();
            scheduleStateFlag = STATE.MOVING;
            createDefaultPlot();
            setInitialMesh();
            //
            scheduleStart(); // thread start!!
            //
        } else if (nextStateId == R.id.mi_learning_mode) {
            settingData.getPlotList().clear();
            matrixNow.setValues(settingData.baseMovingFloat);
            plotIndex = -1;
            timeMoveBase = 0;
            speedX = 0;
            speedY = 0;
            scheduleStateFlag = STATE.VIBRATION;
        } else if (nextStateId == R.id.mi_add_box) {
            matrixNow.setValues(settingData.baseMovingFloat);
            plotIndex = -1;
            timeMoveBase = 0;
            speedX = 0;
            speedY = 0;
            setInitialMesh();
            scheduleStateFlag = STATE.BOX;
        }
        draw();
    }
    public void changeStateEnd(STATE oldState) {
        scheduleStop();
        //
        if (oldState == STATE.START) {
            scheduleStateFlag = STATE.START;
        } else if ((oldState == STATE.REPOSITION) || (oldState == STATE.MOVING)) {
            if (finishListener != null) {
                scheduleStateFlag = STATE.REPOSITION;
                finishListener.run();
            }
        } else if (oldState == STATE.VIBRATION) {
            if (settingData.getVibrationLoop()) {
                reversePlot();
            }
            scheduleStateFlag = STATE.MOVING;
            changeStateStart(0);
        } else if (oldState == STATE.BOX) {
            scheduleStateFlag = STATE.MOVING;
            changeStateStart(0);
        }
    }
    public void draw() {
        if ((getHeight() == 0) || (getHeight() == 0) ||  (bitmapBase == null)) {
            return;
        }
        //
        if (meshArray != null) {
            final float[] nowFloat = new float[9];
            matrixNow.getValues(nowFloat);
            int width = bitmapBase.getWidth();
            int height = bitmapBase.getHeight();
            for (int y = 0; y <= meshYMax; y++) {
                for (int x = 0; x <= meshXMax; x++) {
                    int pos = (y * (meshXMax + 1) + x) * 2;
                    meshArray[pos] = (float) x * width / meshXMax;
                    meshArray[pos + 1] = (float) y * height / meshYMax;
                }
            }
            if (settingData.getBoxList().isEmpty()) {
                int isX = meshXMax / 10;
                int isY = meshYMax / 10;
                int ieX = meshXMax * 9 / 10;
                int ieY = meshYMax * 9 / 10;
                updateMeshArrayForBox(isX,isY,ieX,ieY);
            } else {
                for (Box box : settingData.getBoxList()) {
                    int isX = Math.max(0, (int)(box.startX * meshXMax / width) + 1);
                    int isY = Math.max(0, (int)(box.startY * meshYMax / height) + 1);
                    int ieX = Math.min(meshXMax,(int)(box.endX * meshXMax / width));
                    int ieY = Math.min(meshYMax,(int)(box.endY * meshYMax / height));
                    updateMeshArrayForBox(isX,isY,ieX,ieY);
                }
            }
            if (settingData.getMeshMovingFlag() && (bitmapMesh != null)) {
                try {
                    Canvas meshCanvas = new Canvas(bitmapMesh);
                    meshCanvas.drawBitmapMesh(bitmapBase,
                            meshXMax,
                            meshYMax,
                            meshArray,
                            0,
                            null,
                            0,
                            null);
                } catch(Exception e) {
                    bitmapMesh = null;
                }
            } else {
                bitmapMesh = null;
            }
        }
        //
        // create canvas !
        //
        Canvas c = null;
        try {
            c = holder.lockCanvas();
            if (c == null) {
                return;
            }
            c.drawColor(Color.BLACK);
            //
            //
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            if ((!settingData.getMeshMovingFlag()) || (bitmapMesh == null)) {
                c.drawBitmap(bitmapBase, matrixNow, p);
            } else {
                c.drawBitmap(bitmapMesh, matrixNow, p);
            }
            //
            if (scheduleStateFlag == STATE.REPOSITION) {
                matrixNow.getValues(settingData.baseMovingFloat);
            }
            if ((scheduleStateFlag == STATE.REPOSITION)
                    || (scheduleStateFlag == STATE.VIBRATION)) {
                float x = getWidth() / 2.0f;
                float y = getHeight() / 2.0f;
                for (Plot plotNow: settingData.getPlotList()) {
                    if (plotNow instanceof PlotDistance) {
                        p.setColor(Color.RED);
                        PlotDistance plot = (PlotDistance) plotNow;
                        float dx = x + plot.distanceX;
                        float dy = y + plot.distanceY;
                        c.drawLine(x, y, dx, dy, p);
                        x = dx;
                        y = dy;
                    } else if (plotNow instanceof PlotScale) {
                        p.setColor(Color.GREEN);
                        PlotScale plot = (PlotScale) plotNow;
                        float dx = plot.touchPointX;
                        float dy = plot.touchPointY;
                        c.drawLine(x, y, dx, dy, p);
                        x = dx;
                        y = dy;
                    }
                }
            }
            //
            final float[] nowFloat = new float[9];
            matrixNow.getValues(nowFloat);
            if (!((scheduleStateFlag == STATE.MOVING) && (!settingData.getMeshFlag()) && settingData.getMeshMovingFlag())) {
                p.setColor(settingData.getBoxColorColor());
                for (Box box : settingData.getBoxList()) {
                    if ((box.endX == box.startX) && (box.endY == box.startY)) {
                        continue;
                    }
                    float sX = getBitmapXYtoDisplayXY(box.startX, nowFloat[Matrix.MTRANS_X], nowFloat[Matrix.MSCALE_X]);
                    float sY = getBitmapXYtoDisplayXY(box.startY, nowFloat[Matrix.MTRANS_Y], nowFloat[Matrix.MSCALE_Y]);
                    float eX = getBitmapXYtoDisplayXY(box.endX, nowFloat[Matrix.MTRANS_X], nowFloat[Matrix.MSCALE_X]);
                    float eY = getBitmapXYtoDisplayXY(box.endY, nowFloat[Matrix.MTRANS_Y], nowFloat[Matrix.MSCALE_Y]);
                    if ((scheduleStateFlag == STATE.BOX)
                            || (!settingData.getMeshFlag())) {
                        p.setStyle(Paint.Style.FILL);
                    } else {
                        p.setStyle(Paint.Style.STROKE);
                    }
                    c.drawRect(sX, sY, eX, eY, p);
                }
            }
            //
            if (settingData.getMeshFlag() && (meshArray != null)) {
                for (int y = 0; y <= meshYMax; y++) {
                    for (int x = 0; x <= meshXMax; x++) {
                        int posB = (y * (meshXMax + 1) + x) * 2;
                        float bX = getBitmapXYtoDisplayXY(meshArray[posB], nowFloat[Matrix.MTRANS_X], nowFloat[Matrix.MSCALE_X]);
                        float bY = getBitmapXYtoDisplayXY(meshArray[posB + 1], nowFloat[Matrix.MTRANS_Y], nowFloat[Matrix.MSCALE_Y]);
                        if (y != 0) {
                            int posA = ((y - 1) * (meshXMax + 1) + x) * 2;
                            float aX = getBitmapXYtoDisplayXY(meshArray[posA], nowFloat[Matrix.MTRANS_X], nowFloat[Matrix.MSCALE_X]);
                            float aY = getBitmapXYtoDisplayXY(meshArray[posA + 1], nowFloat[Matrix.MTRANS_Y], nowFloat[Matrix.MSCALE_Y]);
                            c.drawLine(aX, aY, bX, bY, p);
                        }
                        if (x != 0) {
                            int posA = (y * (meshXMax + 1) + (x - 1)) * 2;
                            float aX = getBitmapXYtoDisplayXY(meshArray[posA], nowFloat[Matrix.MTRANS_X], nowFloat[Matrix.MSCALE_X]);
                            float aY = getBitmapXYtoDisplayXY(meshArray[posA + 1], nowFloat[Matrix.MTRANS_Y], nowFloat[Matrix.MSCALE_Y]);
                            c.drawLine(aX, aY, bX, bY, p);
                        }
                    }
                }
            }
            //
            //
            if (settingData.getColorFilterFlag()) {
                int alpha = (settingData.getColorFilterAlpha() << 6 * 4) & 0xff000000;
                int color = alpha | (settingData.getColorFilterColor() & 0x00ffffff);
                p.setColor(color);
                c.drawRect(0, 0, getWidth(), getHeight(), p);
            }
            //
        } finally {
            if (c != null) {
                holder.unlockCanvasAndPost(c);
            }
        }
    }
    public void createDefaultPlot() {
        plotIndex = 0;
        if ((settingData.getVibrationType() == 0) && (!settingData.getPlotList().isEmpty())) {
            return;
        }
        if (settingData.getVibrationType() == 0) {
            settingData.setVibrationType(1);
        }
        settingData.getPlotList().clear();
        int maxLen;
        switch (settingData.getVibrationType()) {
            case 1:
                maxLen = getHeight() / 60;
                for (float t = 0 ; ; t += 0.1f) {
                    float len =  2.0f * 9.8f * t * t;
                    if (maxLen < len) {
                        break;
                    }
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long)(t * settingData.getVibrationSpeed());
                    plot.distanceX = 0;
                    plot.distanceY = - len;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 2:
                maxLen = getHeight() / 60;
                for (float t = 0 ; ; t += 0.1f) {
                    float len =  2.0f * 9.8f * t * t;
                    if (maxLen < len) {
                        break;
                    }
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long)(t * settingData.getVibrationSpeed());
                    plot.distanceX = 0;
                    plot.distanceY = len;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 3:
                maxLen = getWidth() / 60;
                for (float t = 0 ; ; t += 0.1f) {
                    float len =  2.0f * 9.8f * t * t;
                    if (maxLen < len) {
                        break;
                    }
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long)(t * settingData.getVibrationSpeed());
                    plot.distanceX = -len;
                    plot.distanceY = 0;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 4:
                maxLen = getWidth() / 60;
                for (float t = 0 ; ; t += 0.1f) {
                    float len =  2.0f * 9.8f * t * t;
                    if (maxLen < len) {
                        break;
                    }
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long)(t * settingData.getVibrationSpeed());
                    plot.distanceX = len;
                    plot.distanceY = 0;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 5:
                maxLen = getWidth() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? 10 : -10;
                    plot.distanceY = 0;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 6:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = 0;
                    plot.distanceY = (t < (maxLen / 2)) ? 10 : -10;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 7:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? 10 : -10;
                    plot.distanceY = (t < (maxLen / 2)) ? -10 : 10;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 8:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? -10 : 10;
                    plot.distanceY = (t < (maxLen / 2)) ? 10 : -10;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 9:
                for (int t = 0; t < 360 /10 ; t++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = (float) Math.sin(t*10 * Math.PI /180.0) *10;
                    plot.distanceY = (float) Math.cos(t*10 * Math.PI /180.0) *10;
                    settingData.getPlotList().add(plot);
                }
                break;
            case 10:
            default:
                for (int t = 0; t <  360 /10 ; t++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * settingData.getVibrationSpeed();
                    plot.distanceX = (float) -Math.sin(t*10 * Math.PI /180.0) *10;
                    plot.distanceY = (float) -Math.cos(t*10 * Math.PI /180.0) *10;
                    settingData.getPlotList().add(plot);
                }
                break;
        }
        if (settingData.getVibrationLoop()) {
            reversePlot();
        }
    }
    public void reversePlot() {
        int max = settingData.getPlotList().size();
        if (0 < max) {
            long latestTime = 10 + settingData.getPlotList().get(max - 1).time;
            for (int i = max - 1; 0 < i; i--) {
                Plot oldPlot = settingData.getPlotList().get(i);
                if (oldPlot instanceof PlotDistance) {
                    PlotDistance newPlot = new PlotDistance();
                    newPlot.time = latestTime + latestTime - oldPlot.time;
                    newPlot.distanceX = -((PlotDistance) oldPlot).distanceX;
                    newPlot.distanceY = -((PlotDistance) oldPlot).distanceY;
                    settingData.getPlotList().add(newPlot);
                } else if (oldPlot instanceof PlotScale) {
                    PlotScale newPlot = new PlotScale();
                    newPlot.time = latestTime + latestTime - oldPlot.time;
                    newPlot.lastScaleFactor = 1 / ((PlotScale) oldPlot).lastScaleFactor;
                    newPlot.touchPointX = ((PlotScale) oldPlot).touchPointX;
                    newPlot.touchPointY = ((PlotScale) oldPlot).touchPointY;
                    settingData.getPlotList().add(newPlot);
                }
            }
        }
    }
    public void setInitialMesh() {
        speedX = 0;
        speedY = 0;
        if (settingData.getMeshFlag() || settingData.getMeshMovingFlag()) {
            //
            float lenX = (bitmapMesh != null) ? bitmapMesh.getWidth(): getWidth();
            float lenY = (bitmapMesh != null) ? bitmapMesh.getHeight(): getHeight();
            if (lenY < lenX) {
                meshYMax = MESH_MAX;
                meshXMax = (int) (lenX * MESH_MAX / lenY);
            } else {
                meshXMax = MESH_MAX;
                meshYMax = (int) (lenY * MESH_MAX / lenX);
            }
            meshArray = new float[(meshXMax + 1) * (meshYMax + 1) * 2];
            //
            if (settingData.getMeshMovingFlag()) {
                bitmapMesh = Bitmap.createBitmap(
                        bitmapBase.getWidth(), bitmapBase.getHeight(), Bitmap.Config.ARGB_8888);
            }
            //
        } else {
            meshXMax = MESH_MAX;
            meshYMax = MESH_MAX;
            meshArray = null;
            bitmapMesh = null;
        }
    }
    private int plotIndex = -1;
    private float speedX = 0;
    private float speedY = 0;
    public void scheduleDraw() {
        //
        //
        boolean invalidate = false;
        if (!settingData.getPlotList().isEmpty()) {
            long nowTime = System.currentTimeMillis();
            if (plotIndex < 0) {
                invalidate = true;
                matrixNow.setValues(settingData.baseMovingFloat);
                plotIndex = 0;
                timeMoveBase = nowTime;
                speedX = 0;
                speedY = 0;
            } else {
                Plot plot = settingData.getPlotList().get(plotIndex);
                if (plot.time + timeMoveBase < nowTime) {
                    invalidate = true;
                    //
                    if (plot instanceof PlotDistance) {
                        PlotDistance pd = (PlotDistance) plot;
                        //
                        speedX += pd.distanceX;
                        speedY += pd.distanceY;
                        imageViewMove(pd.distanceX, pd.distanceY);
                    } else if (plot instanceof PlotScale) {
                        PlotScale ps = (PlotScale) plot;
                        imageViewScale(ps.lastScaleFactor,ps.touchPointX,ps.touchPointY);
                    }
                    //
                    plotIndex++;
                    if (settingData.getPlotList().size() <= plotIndex) {
                        plotIndex = -1;
                    }
                }
            }
        }
        if (invalidate) {
            draw();
        }
    }
    public void updateMeshArrayForBox(int isX, int isY, int ieX, int ieY) {
        float addedX = speedX * settingData.getMeshMovingProgress() / 100.0f;
        float addedY = speedY * settingData.getMeshMovingProgress() / 100.0f;
        float rX = (float) (ieX - isX) / 2;
        float rY = (float) (ieY - isY) / 2;
        if (rY <= 0f) {
            return;
        }
        float rate = rX / rY;
        float root2 = (float)Math.sqrt(2.0) * rX;
        for (int y = isY; y <= ieY; y++) {
            for (int x = isX; x <= ieX; x++) {
                float distance = (float)Math.sqrt(
                        (rX - (x - isX))*(rX - (x - isX))
                                +  (rX - (y - isY) * rate)*(rX - (y -isY) * rate));
                distance = distance /root2;
                distance = 1.0f - distance;
                float flexibility = 1.0f - (settingData.getMeshMovingFlexibility()/100f);
                distance = (flexibility <= distance) ? 1.0f : distance / flexibility;
                int pos = (y * (meshXMax + 1) + x) * 2;
                meshArray[pos] += addedX * distance;
                meshArray[pos + 1] += addedY * distance;
            }
        }
    }
    public void changeBitmap() {
        int bitmapX = bitmapBase.getWidth();
        int bitmapY = bitmapBase.getHeight();
        int windowX = this.getWidth();
        int windowY = this.getHeight();
        //
        if (((bitmapX - bitmapY) != 0) &&
                (((bitmapX - bitmapY) > 0) ^ ((windowX - windowY) > 0))) {
            float degrees;
            if ((bitmapX - bitmapY) > 0) {
                degrees = -90;
            } else {
                degrees = 90;
            }
            Matrix m = new Matrix();
            m.setRotate(degrees,bitmapX,bitmapY);
            bitmapBase = Bitmap.createBitmap(bitmapBase,0,0,bitmapX,bitmapY,m,true);
        }
    }
    public void changeMatrixBase() {
        this.matrixNow = new Matrix();
        if (bitmapBase == null) {
            return;
        }
        int bitmapX = bitmapBase.getWidth();
        int bitmapY = bitmapBase.getHeight();
        int windowX = this.getWidth();
        int windowY = this.getHeight();
        //
        int dx = (windowX - bitmapX) / 2;
        int dy = (windowY - bitmapY) / 2;
        matrixNow.setTranslate(dx,dy);
        float scale = (windowX < windowY) ?
            ((float)windowX)/bitmapX:
            ((float)windowY)/bitmapY;
        matrixNow.postScale(scale, scale, windowX/2.0f, windowY/2.0f);
        // same check
        float[] next = new float[9];
        matrixNow.getValues(next);
        if ((next[Matrix.MSCALE_X] != settingData.baseFloat[Matrix.MSCALE_X])
                || (next[Matrix.MSKEW_X]  != settingData.baseFloat[Matrix.MSKEW_X])
                || (next[Matrix.MTRANS_X] != settingData.baseFloat[Matrix.MTRANS_X])
                || (next[Matrix.MSKEW_Y]  != settingData.baseFloat[Matrix.MSKEW_Y])
                || (next[Matrix.MSCALE_Y] != settingData.baseFloat[Matrix.MSCALE_Y])
                || (next[Matrix.MTRANS_Y] != settingData.baseFloat[Matrix.MTRANS_Y])
                || (next[Matrix.MPERSP_0] != settingData.baseFloat[Matrix.MPERSP_0])
                || (next[Matrix.MPERSP_1] != settingData.baseFloat[Matrix.MPERSP_1])
                || (next[Matrix.MPERSP_2] != settingData.baseFloat[Matrix.MPERSP_2])) {
            matrixNow.getValues(settingData.baseFloat);
            matrixNow.getValues(settingData.baseMovingFloat);
        }
    }
    private void imageViewScale(float lastScaleFactor,float touchPointX,float touchPointY) {
        if (bitmapBase == null) {
            return;
        }
        matrixNow.postScale(lastScaleFactor, lastScaleFactor, touchPointX, touchPointY);
    }
    private void imageViewMove(float x, float y) {
        if (bitmapBase == null) {
            return;
        }
        if ((x == 0.0f) && (y == 0.0f)) {
            return ;
        }
        float[] matrixNowFloat = new float[9];
        matrixNow.getValues(matrixNowFloat);
        float scaleBase = settingData.baseFloat[Matrix.MSCALE_X];
        float scaleNow = matrixNowFloat[Matrix.MSCALE_X];
        x = x * scaleNow / scaleBase;
        y = y * scaleNow / scaleBase;
        matrixNow.postTranslate(-x,-y);
        //
    }

    public void scheduleStart() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(this::scheduleDraw, 50, 50, TimeUnit.MILLISECONDS);
        }
    }
    public void scheduleStop() {
        ScheduledExecutorService e = executor;
        if (e != null) {
            executor = null;
            e.shutdown();
        }
    }

    public float getBitmapXYtoDisplayXY(
            float bitmapXY,
            float matrixTransXY,
            float scale) {
        return (bitmapXY * scale) + matrixTransXY;
    }
    public float getDisplayXYtoBitmapXY(
            float displayXY,
            float matrixTransXY,
            float scale) {
        return (displayXY - matrixTransXY) / scale;
    }
}
