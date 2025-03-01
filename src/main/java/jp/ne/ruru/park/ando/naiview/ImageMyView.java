package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.miviewer.MIViewerActivity;

/** my image view
 * @author T.Ando
 */
public class ImageMyView extends androidx.appcompat.widget.AppCompatImageView implements ImageJumperCallBack {

    private final Activity activity;
    final MyApplication a;
    /** max upscale size */
    private static final int MAX_UPSCALE_SIZE = 768;

    private float[] matrixBaseValue = null;

    /** bitmap width */
    private int bitmapX = 0;

    /** bitmap height */
    private int bitmapY = 0;

    /** detector for click */
    private GestureDetector gestureDetector;

    /** detector for scale */
    private ScaleGestureDetector scaleGestureDetector;

    private ImageJumper jumper;

    public ImageMyView(@NonNull Context context) {
        this(context, null);
    }
    public ImageMyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.activity = (Activity)getContext();
        this.a = ((MyApplication)this.activity.getApplication());
    }
    public void setJump(ImageJumper jumper) {
        this.jumper = jumper;
    }
    /** repaint data */
    public void onMyCreate() {
        scaleGestureDetector = new ScaleGestureDetector(activity, mScaleGestureDetector);
        gestureDetector = new GestureDetector(activity, mSimpleOnGestureListener);
    }
    /**
     * on touch event
     * @param event The touch screen event being processed.
     *
     * @return if used then true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean flag1 = false;
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            flag1 = mSimpleOnGestureListener.onUp(event);
        }
        boolean flag2 = gestureDetector.onTouchEvent(event);
        boolean flag3 = scaleGestureDetector.onTouchEvent(event);
        boolean flagAll =  flag1 || flag2 || flag3 || super.onTouchEvent(event);
        if (flagAll) performClick();
        return flagAll;
    }
    @Override
    public boolean performClick() {
        return super.performClick();
    }
    /** repaint data */
    public void onMyLoadBitmap() {
        if (a.getImageBuffer() == null) {
            a.updateImageBuffer(activity,null,null);
        }
        try (InputStream stream = new ByteArrayInputStream(a.getImageBuffer())){
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            bitmapX = bitmap.getWidth();
            bitmapY = bitmap.getHeight();
            if ((bitmapX - bitmapY) != 0) {
                if (((bitmapX - bitmapY) > 0)
                        ^ ((this.getWidth() - this.getHeight()) > 0) ) {
                    float degrees;
                    if ((bitmapX - bitmapY) > 0) {
                        degrees = -90;
                    } else {
                        degrees = 90;
                    }
                    Matrix m = new Matrix();
                    m.setRotate(degrees,bitmapX,bitmapY);
                    bitmap = Bitmap.createBitmap(bitmap,0,0,bitmapX,bitmapY,m,true);
                }
            }
            this.setImageBitmap(bitmap);
            invalidateImageView();
            //
            // clear
            matrixBaseValue = null;
            //
        } catch (Exception e) {
            a.appendLog(activity, this.getClass().getName() + " onMyResume() failed");
            a.appendLog(activity, e.getMessage());
        }
    }
    public ScaleGestureDetector.OnScaleGestureListener mScaleGestureDetector = new ScaleGestureDetector.OnScaleGestureListener() {
        private float touchPointX = 0;
        private float touchPointY = 0;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            jumper.swipeFlag = ImageJumper.SWIPE_FLAG.EXPAND;
            touchPointX = detector.getFocusX();
            touchPointY = detector.getFocusY();
            return true;
        }
        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            //EMPTY
        }
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            float lastScaleFactor = detector.getScaleFactor();
            imageViewScale(lastScaleFactor, touchPointX, touchPointY);
            return true;
        }
    };
    /** detector for click */
    public OnGestureListener mSimpleOnGestureListener = new OnGestureListener() {
        private float directionX = 0;
        private float directionY = 0;
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            if (jumper.swipeFlag == ImageJumper.SWIPE_FLAG.EXPAND) {
                return false;
            }
            directionX = 0;
            directionY = 0;
            return true;
        }
        @Override
        public void onShowPress(@NonNull MotionEvent e) {
        }
        @Override
        public void onLongPress(@NonNull MotionEvent e) {
        }
        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            if (jumper.swipeFlag == ImageJumper.SWIPE_FLAG.EXPAND) {
                directionX = 0;
                directionY = 0;
            } else {
                if ((directionX == 0) && (directionY == 0)) {
                    float dx = distanceX * distanceX;
                    float dy = distanceY * distanceY;
                    if (dx < dy) {
                        distanceX = 0;
                        directionY = distanceY;
                    } else {
                        directionX = distanceX;
                        distanceY = 0;
                    }
                } else {
                    if (directionX != 0) {
                        directionX += distanceX;
                        distanceY = 0;
                    } else {
                        distanceX = 0;
                        directionY += distanceY;
                    }
                }
            }
            return imageViewMove(distanceX,distanceY);
        }
        public boolean onUp(@NonNull MotionEvent e) {
            if (jumper.swipeFlag == ImageJumper.SWIPE_FLAG.EXPAND) {
                return false; // Use onDoubleTapEvent() instead
            }
            jumper.jump(directionX,directionY);
            return true;
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            return false; // Use onUp() instead
        }
        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
                               float velocityY) {
            return false; // Use onUp() instead
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            if (jumper.swipeFlag != ImageJumper.SWIPE_FLAG.EXPAND) {
                return false; // Use onUp() instead
            }
            jumper.swipeFlag = ImageJumper.SWIPE_FLAG.NONE;
            return onClearImage();
        }
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            return false; // Use onUp() instead
        }
        @Override
        public boolean onDoubleTapEvent(@NonNull MotionEvent e) {
            return false; // Use onUp() instead
        }
    };


    private void invalidateImageView() {
        int color = Color.BLACK;
        if (jumper.swipeFlag == ImageJumper.SWIPE_FLAG.EXPAND) {
            color = Color.WHITE;
        }
        this.setBackgroundColor(color);
        this.invalidate();
    }



    private void createMatrixBaseValue(Matrix matrix) {
        matrixBaseValue = new float[9];
        matrix.getValues(matrixBaseValue);
    }
    private void imageViewScale(float lastScaleFactor,float touchPointX,float touchPointY) {
        Matrix matrix = this.getImageMatrix();
        if (matrixBaseValue == null) {
            createMatrixBaseValue(matrix);
        }
        float[] matrixLocalValue = new float[9];
        matrix.getValues(matrixLocalValue);
        float baseScale = matrixBaseValue[Matrix.MSCALE_X];
        float localScale = matrixLocalValue[Matrix.MSCALE_X];
        float result = localScale / baseScale;
        if (((result < 0.75f) && (lastScaleFactor < 1.0f)) || ((4.0f < result) && (1.0f < lastScaleFactor))) {
            return;
        }
        matrix.postScale(lastScaleFactor, lastScaleFactor, touchPointX, touchPointY);
        invalidateImageView();
    }
    private boolean imageViewMove(float x, float y) {
        if ((x == 0.0f) && (y == 0.0f)) {
            return false;
        }
        Matrix matrix = this.getImageMatrix();
        if (matrixBaseValue == null) {
            createMatrixBaseValue(matrix);
        }
        float[] matrixLocalValue = new float[9];
        matrix.getValues(matrixLocalValue);
        float baseScale = matrixBaseValue[Matrix.MSCALE_X];
        float localScale = matrixLocalValue[Matrix.MSCALE_X];
        if (0 != localScale) {
            x = x * localScale / baseScale;
            y = y * localScale / baseScale;
            matrix.postTranslate(-x,-y);
            invalidateImageView();
        } else {
            return false;
        }
        return true;
    }

    /** generate image */
    public void doGenerateImage() {
        a.execution(activity, MyNASI.REST_TYPE.IMAGE,bitmapX,bitmapY,null);
        invalidateImageView();
    }

    /**
     * do upscale
     */
    public void doUpscale() {
        a.execution(activity, MyNASI.REST_TYPE.UPSCALE, bitmapX, bitmapY,null);
        invalidateImageView();
    }
    public void onMyShowDialog() {
        String title = (a.getDownloadFlag() ? "*" : "")
                + this.getResources().getString(R.string.menu_move)
                + "(No." + a.getImagePosition() + ")";
        if (0 < bitmapX) {
            title = String.format(Locale.ENGLISH, "%s(%dx%d)", title, bitmapX,bitmapY);
        }
        int anlas = a.getAnlas();
        if (0 <= anlas) {
            title = String.format(Locale.ENGLISH, "%s (anlas: %d)", title, anlas);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.raw_switch, null);
        Button actionBackButton = dialogView.findViewById(R.id.action_back);
        Button generateImageButton = dialogView.findViewById(R.id.generate_image_button);
        Button upscaleButton = dialogView.findViewById(R.id.upscale_button);
        String imageText = activity.getResources().getString(R.string.generate_image_button)
                + (a.isSettingI2i(preferences) ? " (i2i)" : " (" + a.getSettingWidthXHeight(preferences) + ")");
        generateImageButton.setText(imageText);
        String upscaleText = activity.getResources().getString(R.string.upscale_button)
                + " (x" + a.getSettingScale(preferences) + ")";
        upscaleButton.setText(upscaleText);
        //
        generateImageButton.setEnabled(a.isUnlocked());
        upscaleButton.setEnabled((a.isUnlocked()) && (bitmapX <= MAX_UPSCALE_SIZE) && (bitmapY <= MAX_UPSCALE_SIZE));
        //
        SwitchCompat isUseTree = dialogView.findViewById(R.id.setting_use_tree);
        if (a.getChangePartItem() != null) {
            isUseTree.setText(this.getResources().getString(R.string.menu_change_part));
        } else {
            isUseTree.setText(this.getResources().getString(R.string.setting_use_tree));
        }
        isUseTree.setChecked(a.isUseTree(preferences));
        isUseTree.setOnCheckedChangeListener((v,checked)-> a.setUseTree(preferences,checked));
        //
        //
        SwitchCompat isPromptFixedSeed = dialogView.findViewById(R.id.prompt_fixed_seed);
        isPromptFixedSeed.setChecked(a.isPromptFixedSeed(preferences));
        isPromptFixedSeed.setOnCheckedChangeListener((v,checked)-> a.setPromptFixedSeed(preferences,checked));
        a.appendLog(activity, title);
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save_external,(dialog,which)-> jumper.saveForASF())
                .setNeutralButton(R.string.menu_cancel,(dialog,which)-> a.setDownloadFlag(false))
                .create();
        actionBackButton.setOnClickListener((v)-> {alertDialog.dismiss(); activity.finish();});
        generateImageButton.setOnClickListener((v)-> {alertDialog.dismiss(); doGenerateImage();});
        upscaleButton.setOnClickListener((v)-> {alertDialog.dismiss(); doUpscale();});
        alertDialog.show();
    }

    public interface OnGestureListener extends GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
        boolean onUp(@NonNull MotionEvent e);
    }
    public boolean onClearImage() {
        if (matrixBaseValue == null) {
            return false;
        }
        Matrix matrix = this.getImageMatrix();
        matrix.reset();
        matrix.setValues(matrixBaseValue);
        this.setBackgroundColor(Color.BLACK);
        invalidateImageView();
        matrixBaseValue = null;
        return true;
    }

    public void onMyDownSelect() {
        jumper.swipeFlag = ImageJumper.SWIPE_FLAG.NONE;
        String movingModeText = "Action: " + activity.getResources().getString(R.string.mi_moving_mode);
        final MyApplication a =
                ((MyApplication)activity.getApplication());
        a.appendLog(activity,movingModeText);
        Toast.makeText(activity,R.string.mi_moving_mode,Toast.LENGTH_SHORT).show();
        //
        Intent intent = new Intent(activity, MIViewerActivity.class);
        activity.startActivity( intent );
        //
    }
}