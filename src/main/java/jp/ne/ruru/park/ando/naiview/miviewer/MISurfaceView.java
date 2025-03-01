package jp.ne.ruru.park.ando.naiview.miviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.R;

public class MISurfaceView extends SurfaceView {
    private final MIViewerData data;
    private final MyApplication myApplication;
    private final SurfaceHolder holder;

    private final MISettingDialog dialog = new MISettingDialog();

    /**
     * detector for scale
     */
    private final ScaleGestureDetector scaleGestureDetector;

    /**
     * detector for click
     */
    private final GestureDetector gestureDetector;

    private byte[] oldImageBuffer = null;
    private Bitmap bitmapBase = null;
    public Bitmap getBitmapBase() {
        return this.bitmapBase;
    }
    private Bitmap bitmapMesh;
    private void setBitmapMesh(Bitmap bitmapMesh) {
        this.bitmapMesh = bitmapMesh;
    }
    private Bitmap getBitmapMesh() {
        return this.bitmapMesh;
    }

    private ScheduledExecutorService executor;

    private STATE scheduleStateFlag;
    public enum STATE {
        REPOSITION,
        MOVING,
        VIBRATION,
        BOX,
        CONCENTRATED,
        SPARK
    }

    private Matrix matrixNow = null;


    public static final int MESH_MAX = 20;
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
        this.myApplication = (MyApplication)((AppCompatActivity)context).getApplication();
        this.data = myApplication.getMIViewerData();
        //
        holder = getHolder();
        holder.addCallback(myCallback);
        setFocusable(true);
        requestFocus();
        //
        //
        scaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureDetector);
        gestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
        //
        scheduleStateFlag = STATE.MOVING;
        //
        //
        dialog.setMISettingFinishListener(result->{
            if (result == R.id.mi_finish) {
                onMyDownSelect();
            } else {
                dialog.dismissNow();
                //
                changeStateStart(result);
            }
        });
    }
    public void onMyDownSelect() {
        Toast.makeText(this.getContext(),R.string.mi_back_text,Toast.LENGTH_SHORT).show();
        ((Activity)this.getContext()).finish();
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
            scheduleStateFlag = STATE.MOVING;
            changeStateStart(0);
        }
        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            scheduleStop();
            bitmapBase = null;
            oldImageBuffer = null;
            meshArray = null;
            setBitmapMesh(null);
        }
    };

    public ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureDetector = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            if (scheduleStateFlag == STATE.MOVING) {
                return super.onScaleBegin(detector);
            }
            //
            //
            switch (scheduleStateFlag) {
                case CONCENTRATED:
                case VIBRATION:
                case BOX:
                    return true;
                case REPOSITION:
                case SPARK:
                default:
                    break;
            }
            return super.onScaleBegin(detector);
        }
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            if (scheduleStateFlag == STATE.MOVING) {
                return super.onScale(detector);
            }
            //
            if (data.getPlot().getTimeMoveBase() == 0) {
                data.getPlot().setTimeMoveBase(System.currentTimeMillis());
            }
            float lastScaleFactor = detector.getScaleFactor();
            if (lastScaleFactor == 1.0) {
                return super.onScale(detector);
            }
            long time = System.currentTimeMillis() - data.getPlot().getTimeMoveBase();
            float touchPointX = detector.getFocusX();
            float touchPointY = detector.getFocusY();
            switch (scheduleStateFlag) {
                case VIBRATION:
                    PlotScale plotScale = new PlotScale();
                    plotScale.time = time;
                    plotScale.lastScaleFactor = lastScaleFactor;
                    plotScale.touchPointX = touchPointX;
                    plotScale.touchPointY = touchPointY;
                    data.getPlot().getPlotList().add(plotScale);
                    //
                    imageViewScale(lastScaleFactor, touchPointX, touchPointY);
                    draw();
                    return true;
                case REPOSITION:
                    imageViewScale(lastScaleFactor, touchPointX, touchPointY);
                    draw();
                    return true;
                case CONCENTRATED:
                    data.setConcentratedLineLen(data.getConcentratedLineLen() * lastScaleFactor);
                    draw();
                    return true;
                case SPARK:
                default:
                    break;
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
            //
            if (data.getPlot().getTimeMoveBase() == 0) {
                data.getPlot().setTimeMoveBase(System.currentTimeMillis());
            }
            final float[] nowFloat = new float[9];
            matrixNow.getValues(nowFloat);
            float displayX = e.getX();
            float displayY = e.getY();
            float matrixTransX = nowFloat[Matrix.MTRANS_X];
            float matrixTransY = nowFloat[Matrix.MTRANS_Y];
            float scaleX = nowFloat[Matrix.MSCALE_X];
            float scaleY = nowFloat[Matrix.MSCALE_X];
            int bitmapX = (int) getDisplayXYtoBitmapXY(displayX, matrixTransX, scaleX);
            int bitmapY = (int) getDisplayXYtoBitmapXY(displayY, matrixTransY, scaleY);
            switch (scheduleStateFlag) {
                case VIBRATION:
                    return true;
                case BOX:
                    Box box = new Box();
                    box.startX = bitmapX;
                    box.startY = bitmapY;
                    box.endX = box.startX;
                    box.endY = box.startY;
                    data.getBoxList().add(box);
                    return true;
                case SPARK:
                    PlotIndex plotIndex = new PlotIndex();
                    plotIndex.setIndex(-1);
                    plotIndex.setTimeMoveBase(System.currentTimeMillis());
                    PlotBitmap plot = new PlotBitmap();
                    plot.time = 0;
                    plot.bitmapX = bitmapX;
                    plot.bitmapY = bitmapY;
                    plotIndex.getPlotList().add(plot);
                    data.getSparkList().add(plotIndex);
                    return true;
                case CONCENTRATED:
                    data.setConcentratedX(bitmapX);
                    data.setConcentratedY(bitmapY);
                    draw();
                    return true;
                default:
                    break;
            }
            return false;
        }


        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            if (scheduleStateFlag == STATE.MOVING) {
                return false;
            }
            //
            long time = System.currentTimeMillis() - data.getPlot().getTimeMoveBase();
            if ((distanceX == 0.0f) && (distanceY == 0.0f)) {
                return false;
            }
            final float[] nowFloat = new float[9];
            matrixNow.getValues(nowFloat);
            float displayX = e2.getX();
            float displayY = e2.getY();
            float matrixTransX = nowFloat[Matrix.MTRANS_X];
            float matrixTransY = nowFloat[Matrix.MTRANS_Y];
            float scaleX = nowFloat[Matrix.MSCALE_X];
            float scaleY = nowFloat[Matrix.MSCALE_X];
            int bitmapX = (int) getDisplayXYtoBitmapXY(displayX, matrixTransX, scaleX);
            int bitmapY = (int) getDisplayXYtoBitmapXY(displayY, matrixTransY, scaleY);
            switch (scheduleStateFlag) {
                case BOX:
                    if (!data.getBoxList().isEmpty()) {
                        Box box = data.getBoxList().getLast();
                        box.endX = bitmapX;
                        box.endY = bitmapY;
                    }
                    draw();
                    return true;
                case REPOSITION:
                    imageViewMove(distanceX, distanceY);
                    draw();
                    return true;
                case VIBRATION:
                    PlotDistance plot = new PlotDistance();
                    plot.time = time;
                    plot.distanceX = distanceX;
                    plot.distanceY = distanceY;
                    data.getPlot().getPlotList().add(plot);
                    imageViewMove(distanceX, distanceY);
                    draw();
                    return true;
                case CONCENTRATED:
                    data.setConcentratedX(bitmapX);
                    data.setConcentratedY(bitmapY);
                    draw();
                    return true;
                case SPARK:
                    PlotBitmap plotBitmap = new PlotBitmap();
                    plotBitmap.time = time;
                    plotBitmap.bitmapX = bitmapX;
                    plotBitmap.bitmapY = bitmapY;
                    data.getSparkList().getLast().getPlotList().add(plotBitmap);
                    return true;
                default:
                    break;
            }
            return false;
        }
        @Override
        public boolean onUp(@NonNull MotionEvent e) {
            changeStateEnd(scheduleStateFlag);
            return false;
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            return false;
        }
        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return false;
        }
        @Override
        public void onShowPress(@NonNull MotionEvent e) {
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
    };
    public void changeStateStart(int nextStateId) {
        //
        scheduleStop();
        checkBitmap();
        //
        if ((getHeight() == 0) || (getHeight() == 0) || (getBitmapBase() == null)) {
            return;
        }
        boolean inSchedule = false;
        if (nextStateId == R.id.mi_reset) {
            matrixNow.setValues(data.baseFloat);
            matrixNow.getValues(data.baseMovingFloat);
            data.getPlot().getPlotList().clear();
            data.reset();
            data.getBoxList().clear();
            data.getSparkList().clear();
            data.setConcentratedX(getBitmapBase().getWidth()/2.0f);
            data.setConcentratedY(getBitmapBase().getHeight()/2.0f);
            clearVibratePlot();
            nextStateId = 0; //  moving
        }
        //
        //
        if (nextStateId == R.id.mi_reposition) {
            clearVibratePlot();
            scheduleStateFlag = STATE.REPOSITION;
        } else if (nextStateId == 0) { // moving
            data.getPlot().setTimeMoveBase(System.currentTimeMillis());
            scheduleStateFlag = STATE.MOVING;
            createDefaultPlot();
            clearVibratePlot();
            setInitialMesh();
            //
            inSchedule = true;
            //
        } else if (nextStateId == R.id.mi_learning_mode) {
            data.getPlot().getPlotList().clear();
            clearVibratePlot();
            scheduleStateFlag = STATE.VIBRATION;
        } else if (nextStateId == R.id.mi_add_box) {
            clearVibratePlot();
            setInitialMesh();
            scheduleStateFlag = STATE.BOX;
        } else if (nextStateId == R.id.mi_concentrated_state) {
            clearVibratePlot();
            scheduleStateFlag = STATE.CONCENTRATED;
        } else if (nextStateId == R.id.mi_sparkling_plot) {
            clearVibratePlot();
            scheduleStateFlag = STATE.SPARK;
        }
        if (inSchedule) {
            scheduleStart(); // thread start!!
        } else {
            draw();
        }
    }
    public void clearVibratePlot() {
        matrixNow.setValues(data.baseMovingFloat);
        data.getPlot().setIndex(-1);
        data.getPlot().setTimeMoveBase(0);
        speedX = 0;
        speedY = 0;
    }
    public void changeStateEnd(STATE oldState) {
        //
        switch (oldState) {
            case REPOSITION:
            case MOVING:
                scheduleStop();
                //
                scheduleStateFlag = STATE.CONCENTRATED;
                onMyShowDialog();
                break;
            case VIBRATION:
                if (data.getVibrationLoop()) {
                    reversePlot();
                }
                scheduleStateFlag = STATE.MOVING;
                changeStateStart(0);
                break;
            case BOX:
            case SPARK:
            case CONCENTRATED:
                scheduleStateFlag = STATE.MOVING;
                changeStateStart(0);
                break;
            default:
                break;
        }
    }
    public void draw() {
        //
        checkBitmap();
        //
        if ((getHeight() == 0) || (getHeight() == 0) ||  (getBitmapBase() == null)) {
            return;
        }
        //
        if (meshArray != null) {
            final float[] nowFloat = new float[9];
            matrixNow.getValues(nowFloat);
            int width = getBitmapBase().getWidth();
            int height = getBitmapBase().getHeight();
            for (int y = 0; y <= meshYMax; y++) {
                for (int x = 0; x <= meshXMax; x++) {
                    int pos = (y * (meshXMax + 1) + x) * 2;
                    meshArray[pos] = (float) x * width / meshXMax;
                    meshArray[pos + 1] = (float) y * height / meshYMax;
                }
            }
            if (data.getBoxList().isEmpty()) {
                int isX = meshXMax / 10;
                int isY = meshYMax / 10;
                int ieX = meshXMax * 9 / 10;
                int ieY = meshYMax * 9 / 10;
                updateMeshArrayForBox(isX,isY,ieX,ieY);
            } else {
                for (Box box : data.getBoxList()) {
                    int isX = Math.max(0, (int)(box.startX * meshXMax / width) + 1);
                    int isY = Math.max(0, (int)(box.startY * meshYMax / height) + 1);
                    int ieX = Math.min(meshXMax,(int)(box.endX * meshXMax / width));
                    int ieY = Math.min(meshYMax,(int)(box.endY * meshYMax / height));
                    updateMeshArrayForBox(isX,isY,ieX,ieY);
                }
            }
            if (data.getMeshMovingFlag() && (getBitmapMesh() != null)) {
                try {
                    Canvas meshCanvas = new Canvas(getBitmapMesh());
                    meshCanvas.drawBitmapMesh(getBitmapBase(),
                            meshXMax,
                            meshYMax,
                            meshArray,
                            0,
                            null,
                            0,
                            null);
                } catch(Exception e) {
                    setBitmapMesh(null);
                }
            } else {
                setBitmapMesh(null);
            }
        }
        //
        if (scheduleStateFlag == STATE.REPOSITION) {
            matrixNow.getValues(data.baseMovingFloat);
        }
        final float[] nowFloat = new float[9];
        matrixNow.getValues(nowFloat);
        float matrixTransX = nowFloat[Matrix.MTRANS_X];
        float matrixTransY = nowFloat[Matrix.MTRANS_Y];
        float scaleX = nowFloat[Matrix.MSCALE_X];
        float scaleY = nowFloat[Matrix.MSCALE_Y];
        float baseScaleX = data.baseFloat[Matrix.MSCALE_X];
        float baseScaleY = data.baseFloat[Matrix.MSCALE_Y];
        //
        // create canvas !
        //
        try (CanvasAutoCloseable cac = new CanvasAutoCloseable(holder)) {
            Canvas c = cac.getCanvas();
            if (c == null) {
                return;
            }
            ///
            c.drawColor(Color.BLACK);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            if ((!data.getMeshMovingFlag()) || (getBitmapMesh() == null)) {
                c.drawBitmap(getBitmapBase(), matrixNow, p);
            } else {
                c.drawBitmap(getBitmapMesh(), matrixNow, p);
            }
            //
            if (data.getConcentratedFlag()) {
                int cRandomAngle = data.getConcentratedRandomAngle();
                int concentratedCount = data.getConcentratedCount();
                int concentratedWide = data.getConcentratedWide();
                int displayX = (int) getBitmapXYtoDisplayXY(data.getConcentratedX(), matrixTransX, scaleX);
                int displayY = (int) getBitmapXYtoDisplayXY(data.getConcentratedY(), matrixTransY, scaleY);
                for (int i = 0 ; i < concentratedCount ; i++) {
                    float maxLen = (float)(Math.sqrt(getWidth() * getHeight()) * (Math.random() * 0.2 + 0.8));
                    int concentratedLineLen = (int)(data.getConcentratedLineLen()
                            + Math.random()* data.getConcentratedRandomLine());
                    int angle0 = (i * 360 / concentratedCount) + (int)(Math.random()*cRandomAngle)  % 360;
                    int lineSX = displayX + (int) (concentratedLineLen * scaleX / baseScaleX * Math.cos(Math.PI * angle0 / 180));
                    int lineSY = displayY - (int) (concentratedLineLen * scaleY / baseScaleY * Math.sin(Math.PI * angle0 / 180));
                    if ((0 <= lineSX) && (0 <= lineSY) && (lineSX < getWidth()) && (lineSY < getHeight())) {
                        int lineEX = displayX + (int) (2 * maxLen * scaleX / baseScaleX * Math.cos(Math.PI * angle0 / 180));
                        int lineEY = displayY - (int) (2 * maxLen * scaleY / baseScaleY * Math.sin(Math.PI * angle0 / 180));
                        int angle1 = (angle0 + (concentratedWide / 2) + 360) % 360;
                        int angle2 = (angle0 - (concentratedWide / 2) + 360) % 360;
                        int lineE1X = displayX + (int) (maxLen * Math.cos(Math.PI * angle1 / 180));
                        int lineE1Y = displayY - (int) (maxLen * Math.sin(Math.PI * angle1 / 180));
                        int lineE2X = displayX + (int) (maxLen * Math.cos(Math.PI * angle2 / 180));
                        int lineE2Y = displayY - (int) (maxLen * Math.sin(Math.PI * angle2 / 180));
                        int alpha = (data.getConcentratedAlpha() << 6 * 4) & 0xff000000;
                        int color = alpha | (data.getConcentratedColorColor() & 0x00ffffff);
                        p.setColor(color);
                        p.setStyle(Paint.Style.FILL);
                        Path path = new Path();
                        path.moveTo(lineSX, lineSY);
                        path.lineTo(lineE1X, lineE1Y);
                        path.lineTo(lineEX, lineEY);
                        path.lineTo(lineE2X, lineE2Y);
                        path.close();
                        c.drawPath(path, p);
                    }
                }
            }
            p.setStyle(Paint.Style.STROKE);
            if ((scheduleStateFlag == STATE.REPOSITION)
                    || (scheduleStateFlag == STATE.CONCENTRATED)
                    || (scheduleStateFlag == STATE.VIBRATION)) {
                float x = getWidth() / 2.0f;
                float y = getHeight() / 2.0f;
                for (Plot plotNow: data.getPlot().getPlotList()) {
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
            if (!((scheduleStateFlag == STATE.MOVING) && (!data.getMeshFlag()) && data.getMeshMovingFlag())) {
                p.setColor(data.getBoxColorColor());
                for (Box box : data.getBoxList()) {
                    if ((box.endX == box.startX) && (box.endY == box.startY)) {
                        continue;
                    }
                    float sX = getBitmapXYtoDisplayXY(box.startX, matrixTransX, scaleX);
                    float sY = getBitmapXYtoDisplayXY(box.startY, matrixTransY, scaleY);
                    float eX = getBitmapXYtoDisplayXY(box.endX, matrixTransX, scaleX);
                    float eY = getBitmapXYtoDisplayXY(box.endY, matrixTransY, scaleY);
                    if ((scheduleStateFlag == STATE.BOX)
                            || (!data.getMeshFlag())) {
                        p.setStyle(Paint.Style.FILL);
                    } else {
                        p.setStyle(Paint.Style.STROKE);
                    }
                    c.drawRect(sX, sY, eX, eY, p);
                }
            }
            //
            if (data.getMeshFlag() && (meshArray != null)) {
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
            // sparkling
            if (data.getSparklingFlag()) {
                int alpha = (data.getSparklingAlpha() << 6 * 4) & 0xff000000;
                int color = alpha | (data.getSparklingColorColor() & 0x00ffffff);
                p.setStyle(Paint.Style.FILL);
                p.setColor(color);
                if (data.getSparkList().isEmpty()) {
                    for (int i = 0; i < data.getSparklingCount(); i++) {
                        float radius = data.getSparklingLen();
                        float dx = (float) (Math.random() * getWidth());
                        float dy = (float) (Math.random() * getHeight());
                        c.drawCircle(dx, dy, radius, p);
                    }
                } else {
                    for (PlotIndex plotIndex : data.getSparkList()) {
                        if ((plotIndex.getIndex() < 0) || (plotIndex.getPlotList().size() <= plotIndex.getIndex())) {
                            continue;
                        }
                        PlotBitmap plot = (PlotBitmap)plotIndex.getPlotList().get(plotIndex.getIndex());
                        for (int i = 0; i < data.getSparklingCount(); i++) {
                            float radius = data.getSparklingLen();
                            float ramdomDx = ((float)Math.random() - 0.5f) * data.getSparklingRandom();
                            float ramdomDy = ((float)Math.random() - 0.5f) * data.getSparklingRandom();
                            float dx = getBitmapXYtoDisplayXY(plot.bitmapX + ramdomDx, matrixTransX, scaleX);
                            float dy = getBitmapXYtoDisplayXY(plot.bitmapY + ramdomDy, matrixTransY, scaleY);
                            c.drawCircle(dx, dy, radius, p);
                        }
                    }
                }
            }
            //
            // filter
            if (data.getColorFilterFlag()) {
                int alpha = (data.getColorFilterAlpha() << 6 * 4) & 0xff000000;
                int color = alpha | (data.getColorFilterColor() & 0x00ffffff);
                p.setStyle(Paint.Style.FILL);
                p.setColor(color);
                c.drawRect(0, 0, getWidth(), getHeight(), p);
            }
            //
        }
    }
    public void createDefaultPlot() {
        this.data.getPlot().setIndex(-1);
        if ((data.getVibrationType() == 0) && (!data.getPlot().getPlotList().isEmpty())) {
            return;
        }
        if (data.getVibrationType() == 0) {
            data.setVibrationType(1);
        }
        data.getPlot().getPlotList().clear();
        int maxLen;
        switch (data.getVibrationType()) {
            case 1:
                maxLen = getHeight() / 60;
                for (float t = 0 ; ; t += 0.1f) {
                    float len =  2.0f * 9.8f * t * t;
                    if (maxLen < len) {
                        break;
                    }
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long)(t * data.getVibrationSpeed());
                    plot.distanceX = 0;
                    plot.distanceY = - len;
                    data.getPlot().getPlotList().add(plot);
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
                    plot.time = (long)(t * data.getVibrationSpeed());
                    plot.distanceX = 0;
                    plot.distanceY = len;
                    data.getPlot().getPlotList().add(plot);
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
                    plot.time = (long)(t * data.getVibrationSpeed());
                    plot.distanceX = -len;
                    plot.distanceY = 0;
                    data.getPlot().getPlotList().add(plot);
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
                    plot.time = (long)(t * data.getVibrationSpeed());
                    plot.distanceX = len;
                    plot.distanceY = 0;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 5:
                maxLen = getWidth() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? 10 : -10;
                    plot.distanceY = 0;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 6:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = 0;
                    plot.distanceY = (t < (maxLen / 2)) ? 10 : -10;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 7:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? 10 : -10;
                    plot.distanceY = (t < (maxLen / 2)) ? -10 : 10;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 8:
                maxLen = getHeight() / 60;
                for (int t = 0 ; t < maxLen ; t ++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = (t < (maxLen / 2)) ? -10 : 10;
                    plot.distanceY = (t < (maxLen / 2)) ? 10 : -10;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 9:
                for (int t = 0; t < 360 /10 ; t++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = (float) Math.sin(t*10 * Math.PI /180.0) *20;
                    plot.distanceY = (float) Math.cos(t*10 * Math.PI /180.0) *20;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 10:
                for (int t = 0; t <  360 /10 ; t++) {
                    PlotDistance plot = new PlotDistance();
                    plot.time = (long) t * data.getVibrationSpeed();
                    plot.distanceX = (float) -Math.sin(t*10 * Math.PI /180.0) *20;
                    plot.distanceY = (float) -Math.cos(t*10 * Math.PI /180.0) *20;
                    data.getPlot().getPlotList().add(plot);
                }
                break;
            case 11:
            default:
                // no move
                PlotDistance plot = new PlotDistance();
                plot.time = data.getVibrationSpeed();
                plot.distanceX = 0;
                plot.distanceY = 0;
                data.getPlot().getPlotList().add(plot);
        }
        if (data.getVibrationLoop()) {
            reversePlot();
        }
    }
    public void reversePlot() {
        int max = data.getPlot().getPlotList().size();
        if (0 < max) {
            long latestTime = 10 + data.getPlot().getPlotList().get(max - 1).time;
            for (int i = max - 1; 0 < i; i--) {
                Plot oldPlot = data.getPlot().getPlotList().get(i);
                if (oldPlot instanceof PlotDistance) {
                    PlotDistance newPlot = new PlotDistance();
                    newPlot.time = latestTime + latestTime - oldPlot.time;
                    newPlot.distanceX = -((PlotDistance) oldPlot).distanceX;
                    newPlot.distanceY = -((PlotDistance) oldPlot).distanceY;
                    data.getPlot().getPlotList().add(newPlot);
                } else if (oldPlot instanceof PlotScale) {
                    PlotScale newPlot = new PlotScale();
                    newPlot.time = latestTime + latestTime - oldPlot.time;
                    newPlot.lastScaleFactor = 1 / ((PlotScale) oldPlot).lastScaleFactor;
                    newPlot.touchPointX = ((PlotScale) oldPlot).touchPointX;
                    newPlot.touchPointY = ((PlotScale) oldPlot).touchPointY;
                    data.getPlot().getPlotList().add(newPlot);
                }
            }
        }
    }
    public void setInitialMesh() {
        if (data.getMeshFlag() || data.getMeshMovingFlag()) {
            //
            float lenX = (getBitmapMesh() != null) ? getBitmapMesh().getWidth(): getWidth();
            float lenY = (getBitmapMesh() != null) ? getBitmapMesh().getHeight(): getHeight();
            if (lenY < lenX) {
                meshYMax = MESH_MAX;
                meshXMax = (int) (lenX * MESH_MAX / lenY);
            } else {
                meshXMax = MESH_MAX;
                meshYMax = (int) (lenY * MESH_MAX / lenX);
            }
            meshArray = new float[(meshXMax + 1) * (meshYMax + 1) * 2];
            //
            if (data.getMeshMovingFlag()) {
                setBitmapMesh(Bitmap.createBitmap(
                        getBitmapBase().getWidth(), getBitmapBase().getHeight(), Bitmap.Config.ARGB_8888));
            }
            //
        } else {
            meshXMax = MESH_MAX;
            meshYMax = MESH_MAX;
            meshArray = null;
            setBitmapMesh(null);
        }
    }
    private float speedX = 0;
    private float speedY = 0;
    public void scheduleDraw() {
        //
        //
        boolean invalidate = false;
        final long nowTime = System.currentTimeMillis();
        if (!data.getPlot().getPlotList().isEmpty()) {
            PlotIndex plotIndex = data.getPlot();
            if (plotIndex.getIndex() < 0) {
                clearVibratePlot();
            }
            invalidate = changePlotIndex(plotIndex, nowTime);
        }
        for (PlotIndex plotIndex : data.getSparkList()) {
            invalidate = changePlotIndex(plotIndex, nowTime) | invalidate;
        }
        if (invalidate) {
            draw();
        }
    }
    private boolean changePlotIndex(PlotIndex plotIndex, long nowTime) {
        boolean invalidate = false;
        //
        if (plotIndex.getIndex() < 0) {
            invalidate = true;
            changePlotIndexFirst(plotIndex,nowTime);
        } else {
            Plot plot = plotIndex.getPlotList().get(plotIndex.getIndex());
            if (plot.time + plotIndex.getTimeMoveBase() < nowTime) {
                invalidate = true;
                //
                if (plot instanceof PlotDistance) {
                    PlotDistance pd = (PlotDistance) plot;
                    speedX += pd.distanceX;
                    speedY += pd.distanceY;
                    imageViewMove(pd.distanceX, pd.distanceY);
                } else if (plot instanceof PlotScale) {
                    PlotScale ps = (PlotScale) plot;
                    imageViewScale(ps.lastScaleFactor,ps.touchPointX,ps.touchPointY);
                }
                //
                plotIndex.setIndex(plotIndex.getIndex() + 1);
                if (plotIndex.getPlotList().size() <= plotIndex.getIndex()) {
                    changePlotIndexFirst(plotIndex,nowTime);
                }
            }
        }
        //
        return invalidate;
    }
    private void changePlotIndexFirst(PlotIndex plotIndex, long nowTime) {
        plotIndex.setIndex(0);
        plotIndex.setTimeMoveBase(nowTime);
    }

    public void updateMeshArrayForBox(int isX, int isY, int ieX, int ieY) {
        float addedX = speedX * data.getMeshMovingProgress() / 100.0f;
        float addedY = speedY * data.getMeshMovingProgress() / 100.0f;
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
                float flexibility = 1.0f - (data.getMeshMovingFlexibility()/100f);
                distance = (flexibility <= distance) ? 1.0f : distance / flexibility;
                int pos = (y * (meshXMax + 1) + x) * 2;
                meshArray[pos] += addedX * distance;
                meshArray[pos + 1] += addedY * distance;
            }
        }
    }
    public void checkBitmap() {
        byte[] newImageBuffer = this.myApplication.getImageBuffer();
        if ((oldImageBuffer != null) && (oldImageBuffer == newImageBuffer)) {
            return;
        }
        oldImageBuffer = this.myApplication.getImageBuffer();
        if (oldImageBuffer == null) {
            this.myApplication.updateImageBuffer(this.getContext(),null,null);
        }
        oldImageBuffer = this.myApplication.getImageBuffer();
        try (InputStream stream = new ByteArrayInputStream(oldImageBuffer)){
            this.bitmapBase = BitmapFactory.decodeStream(stream);
            //
        } catch (Exception e) {
            this.myApplication.appendLog(this.getContext(), this.getClass().getName() + " onMyResume() failed");
            this.myApplication.appendLog(this.getContext(), e.getMessage());
        }
        if (getBitmapBase() == null) {
            return;
        }
        int bitmapX = getBitmapBase().getWidth();
        int bitmapY = getBitmapBase().getHeight();
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
        //
        //
        this.matrixNow = new Matrix();
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
        if ((next[Matrix.MSCALE_X] != data.baseFloat[Matrix.MSCALE_X])
                || (next[Matrix.MSKEW_X]  != data.baseFloat[Matrix.MSKEW_X])
                || (next[Matrix.MTRANS_X] != data.baseFloat[Matrix.MTRANS_X])
                || (next[Matrix.MSKEW_Y]  != data.baseFloat[Matrix.MSKEW_Y])
                || (next[Matrix.MSCALE_Y] != data.baseFloat[Matrix.MSCALE_Y])
                || (next[Matrix.MTRANS_Y] != data.baseFloat[Matrix.MTRANS_Y])
                || (next[Matrix.MPERSP_0] != data.baseFloat[Matrix.MPERSP_0])
                || (next[Matrix.MPERSP_1] != data.baseFloat[Matrix.MPERSP_1])
                || (next[Matrix.MPERSP_2] != data.baseFloat[Matrix.MPERSP_2])) {
            matrixNow.getValues(data.baseFloat);
            matrixNow.getValues(data.baseMovingFloat);
            data.setConcentratedX(bitmapX/2.0f);
            data.setConcentratedY(bitmapY/2.0f);
        }
    }
    private void imageViewScale(float lastScaleFactor,float touchPointX,float touchPointY) {
        if (getBitmapBase() == null) {
            return;
        }
        matrixNow.postScale(lastScaleFactor, lastScaleFactor, touchPointX, touchPointY);
    }
    private void imageViewMove(float x, float y) {
        if (getBitmapBase() == null) {
            return;
        }
        if ((x == 0.0f) && (y == 0.0f)) {
            return ;
        }
        float[] matrixNowFloat = new float[9];
        matrixNow.getValues(matrixNowFloat);
        float scaleBase = data.baseFloat[Matrix.MSCALE_X];
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

    public void onMyShowDialog() {
        final String TAG = "sample";
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()-> {
            FragmentManager fragmentManager = ((FragmentActivity)this.getContext()).getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(TAG);
            if ((fragment instanceof DialogFragment)
                    && ((DialogFragment) fragment).getDialog() != null) {
                return;
            }
            dialog.showNow(fragmentManager, TAG);
        });
    }
}
