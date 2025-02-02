package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.adapter.UriEtc;
import jp.ne.ruru.park.ando.naiview.databinding.ActivityImageBinding;

/** image activity
 * @author T.Ando
 */
public class ImageActivity extends AppCompatActivity {

    /** enum swipe flag */
    private enum SWIPE_FLAG {
        NONE,
        MOVE_BACK,
        MOVE_FORWARD,
        IMAGE_LIST,
        SAVE,
        EXPAND
    }

    /** swipe flag */
    private SWIPE_FLAG swipeFlag = SWIPE_FLAG.NONE;

    /** max upscale size */
    private static final int MAX_UPSCALE_SIZE = 768;

    /** detector for scale */
    private ScaleGestureDetector scaleGestureDetector;

    /** detector for click */
    private GestureDetector gestureDetector;

    /** binding */
    private ActivityImageBinding binding;

    /** bitmap width */
    private int bitmapX = 0;

    /** bitmap height */
    private int bitmapY = 0;

    private float[] matrixBaseValue = null;

    /**
     * create views
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        scaleGestureDetector = new ScaleGestureDetector(this, mScaleGestureDetector);
        gestureDetector = new GestureDetector(this, mSimpleOnGestureListener);
    }

    /** on resume */
    @Override
    public void onResume() {
        super.onResume();
        this.onMyResume();
    }

    /** repaint data */
    public void onMyResume() {
        MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        if (a.getImageBuffer() == null) {
            a.updateImageBuffer(this,null,null);
        }
        try (InputStream stream = new ByteArrayInputStream(a.getImageBuffer())){
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            bitmapX = bitmap.getWidth();
            bitmapY = bitmap.getHeight();
            if ((bitmapX - bitmapY) != 0) {
                if (((bitmapX - bitmapY) > 0)
                        ^ ((binding.imageView.getWidth() - binding.imageView.getHeight()) > 0) ) {
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
            binding.imageView.setBackgroundColor(Color.BLACK);
            binding.imageView.setImageBitmap(bitmap);
            //
            // clear
            matrixBaseValue = null;
            //
        } catch (Exception e) {
            a.appendLog(this, this.getClass().getName() + " onMyResume() failed");
            a.appendLog(this, e.getMessage());
        }
    }
    /**
     * on touch event
     * @param event The touch screen event being processed.
     *
     * @return if used then true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean flag1 = gestureDetector.onTouchEvent(event);
        boolean flag2 = scaleGestureDetector.onTouchEvent(event);
        return flag1 || flag2 || super.onTouchEvent(event);
    }

    private void createMatrixBaseValue(Matrix matrix) {
        matrixBaseValue = new float[9];
        matrix.getValues(matrixBaseValue);
    }
    private void imageViewScale(float lastScaleFactor,float touchPointX,float touchPointY) {
        Matrix matrix = binding.imageView.getImageMatrix();
        if (matrixBaseValue == null) {
            createMatrixBaseValue(matrix);
        }
        float[] matrixLocalValue = new float[9];
        matrix.getValues(matrixLocalValue);
        float baseScale = matrixBaseValue[Matrix.MSCALE_X];
        float localScale = matrixLocalValue[Matrix.MSCALE_X];
        float result = localScale/baseScale;
        if (((result < 0.75f) && (lastScaleFactor < 1.0f)) || ((4.0f < result) && (1.0f < lastScaleFactor))) {
            return;
        }
        matrix.postScale(lastScaleFactor, lastScaleFactor, touchPointX, touchPointY);
        binding.imageView.setBackgroundColor(Color.WHITE);
        binding.imageView.invalidate();
    }
    private boolean imageViewMove(float x, float y) {
        if ((x == 0.0f) && (y == 0.0f)) {
            return false;
        }
        Matrix matrix = binding.imageView.getImageMatrix();
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
            binding.imageView.invalidate();
        } else {
            return false;
        }
        return true;
    }

    /** generate image */
    public void doGenerateImage() {
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        a.execution(ImageActivity.this, MyNASI.REST_TYPE.IMAGE,bitmapX,bitmapY,null);
    }

    /**
     * do upscale
     */
    public void doUpscale() {
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        a.execution(ImageActivity.this, MyNASI.REST_TYPE.UPSCALE, bitmapX, bitmapY,null);
    }

    public void onSaveDialogMenu() {
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        String title = (a.getDownloadFlag() ? "*" : "")
                + ImageActivity.this.getResources().getString(R.string.menu_move)
                + "(No." + a.getImagePosition() + ")";
        if (0 < bitmapX) {
            title = String.format(Locale.ENGLISH, "%s(%dx%d)", title, bitmapX,bitmapY);
        }
        int anlas = a.getAnlas();
        if (0 <= anlas) {
            title = String.format(Locale.ENGLISH, "%s (anlas: %d)", title, anlas);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String addText;
        if (a.isSettingI2i(preferences)) {
            addText = " (i2i)";
        } else {
            addText = " (" + a.getSettingWidthXHeight(preferences) + ")";
        }
        String imageText =  ImageActivity.this.getResources().getString(R.string.generate_image_button) + addText;
        String upscaleText = ImageActivity.this.getResources().getString(R.string.upscale_button)
                + " (x" + a.getSettingScale(preferences) + ")";
        final String[] items;
        if ((bitmapX <= MAX_UPSCALE_SIZE) && (bitmapY <= MAX_UPSCALE_SIZE)) {
            items = new String[] {
                    ImageActivity.this.getResources().getString(R.string.action_back),
                    imageText,
                    upscaleText // add
            };
        } else {
            items = new String[] {
                    ImageActivity.this.getResources().getString(R.string.action_back),
                    imageText
            };
        }
        View dialogView = this.getLayoutInflater().inflate(R.layout.raw_switch, null);
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
        SwitchCompat isPromptFixedSeed = dialogView.findViewById(R.id.prompt_fixed_seed);
        isPromptFixedSeed.setChecked(a.isPromptFixedSeed(preferences));
        isPromptFixedSeed.setOnCheckedChangeListener((v,checked)-> a.setPromptFixedSeed(preferences,checked));
        a.appendLog(this, title);
        new AlertDialog.Builder(ImageActivity.this)
                .setTitle(title)
                .setView(dialogView)
                .setItems(items, (dialog,which)-> {
                    if (which == 0) {
                        finish();
                    } else if (which == 1) {
                        doGenerateImage();
                    } else if (which == 2) {
                        doUpscale();
                    }
                })
                .setPositiveButton(R.string.action_save_external,(dialog,which)-> saveForASF())
                .setNeutralButton(R.string.menu_cancel,(dialog,which)-> a.setDownloadFlag(false))
                .show();
    }


    public ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureDetector = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private float touchPointX = 0;
        private float touchPointY = 0;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            swipeFlag = SWIPE_FLAG.EXPAND;
            touchPointX = detector.getFocusX();
            touchPointY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }
        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            super.onScale(detector);
            float lastScaleFactor = detector.getScaleFactor();
            imageViewScale(lastScaleFactor, touchPointX, touchPointY);
            return true;
        }
    };

    /** detector for click */
    public GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        final float SWIPE_DISTANCE = 45.0f * 45.0f;
        private int directionFlag = 0;
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            if (swipeFlag == SWIPE_FLAG.EXPAND) {
                return super.onDown(e);
            }
            return imageViewReset();
        }
        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            if (swipeFlag == SWIPE_FLAG.EXPAND) {
                directionFlag = 0;
            } else {
                if (matrixBaseValue == null) {
                    directionFlag = 0;
                }
                if (directionFlag == 0) {
                    float dx = distanceX * distanceX;
                    float dy = distanceY * distanceY;
                    if (dx < dy) {
                        directionFlag = -1;
                    } else {
                        directionFlag = 1;
                    }
                }
                if (directionFlag < 0) {
                    distanceX = 0;
                } else {
                    distanceY = 0;
                }
            }
            return imageViewMove(distanceX,distanceY);
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            if (swipeFlag == SWIPE_FLAG.EXPAND) {
                return super.onSingleTapUp(event);
            }
            imageViewReset();
            onSaveDialogMenu();
            return true;

        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            swipeFlag = SWIPE_FLAG.NONE;
            return imageViewReset();
        }
        /**
         * on fling
         * @param e1 The first down motion event that started the fling.
         * @param e2 The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second
         *              along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second
         *              along the y axis.
         * @return if used then true
         */
        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
                               float velocityY) {
            if ((e1 == null) || (swipeFlag == SWIPE_FLAG.EXPAND)) {
                return super.onFling(null, e2, velocityX, velocityY);
            }
            float dx = e1.getX() - e2.getX();
            float dy = e1.getY() - e2.getY();
            dx = dx * dx;
            dy = dy * dy;
            if ((dy < SWIPE_DISTANCE)
                && (dx < SWIPE_DISTANCE)) {
                return imageViewReset();
            }
            jump(dx < dy,e1.getX() - e2.getX() < 0);
            return true;
        }
        private void jump(boolean isImageList,boolean isBack) {
            final MyApplication a =
                    ((MyApplication)ImageActivity.this.getApplication());
            if (a.getDownloadFlag()) {
                imageViewReset();
                swipeFlag = SWIPE_FLAG.SAVE;
                onMyFling();
                return ;
            }
            if (isImageList) {
                swipeFlag = SWIPE_FLAG.IMAGE_LIST;
                onMyFling();
                return ;
            }
            if (isBack) {
                swipeFlag = SWIPE_FLAG.MOVE_BACK;
                onMyFling();
                return ;
            }
            //if (e1.getX() > e2.getX())
            swipeFlag = SWIPE_FLAG.MOVE_FORWARD;
            onMyFling();
        }
        private boolean imageViewReset() {
            if (matrixBaseValue == null) {
                return false;
            }
            Matrix matrix = binding.imageView.getImageMatrix();
            matrix.reset();
            matrix.setValues(matrixBaseValue);
            binding.imageView.setBackgroundColor(Color.BLACK);
            binding.imageView.invalidate();
            matrixBaseValue = null;
            return true;
        }
    };
    public void onMyFling() {
        String readImagesPermission =
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                        android.Manifest.permission.READ_MEDIA_IMAGES
                        : android.Manifest.permission.READ_EXTERNAL_STORAGE;
        if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)!=
                PackageManager.PERMISSION_GRANTED) {
            readImagesPermissionLauncher.launch(readImagesPermission);
        } else {
            onMyNextFling();
        }
    }
    ActivityResultLauncher<String> readImagesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    onMyNextFling();
                }
            });
    public void onMyNextFling() {
        if (swipeFlag == SWIPE_FLAG.SAVE) {
            onSaveDialogMenu();
            return;
        }
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        a.setDownloadFlag(false);
        //
        if (a.getUriEtcList().isEmpty()) {
            //
            ContentResolver cr = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATA
            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";// ASC or DESC;
            try (Cursor cursor = cr.query(
                    uri, projection, null, null, sortOrder)) {
                if (cursor == null) {
                    throw new IllegalArgumentException("");
                }
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int mimeColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String mime = cursor.getString(mimeColumn);
                    if (!mime.contains("image")) {
                        continue;
                    }
                    Uri contentUri = ContentUris.withAppendedId(uri, id);
                    UriEtc uriEtc = new UriEtc(contentUri, mime);
                    if (!a.getUriEtcList().contains(uriEtc)) {
                        a.getUriEtcList().add(uriEtc);
                    }
                }
            } catch (IllegalArgumentException e) {
                // NONE
            }
        }
        int max = a.getUriEtcList().size() - 1;
        if (max < 0) {
            a.setImagePosition(-1);
            return;
        }
        if (swipeFlag == SWIPE_FLAG.MOVE_BACK) {
            int index = a.getImagePosition() - 1;
            a.setImagePosition(index);
            loadForASFResult();
        } else if (swipeFlag == SWIPE_FLAG.MOVE_FORWARD) {
            int index = Math.min(max, a.getImagePosition() + 1);
            a.setImagePosition(index);
            loadForASFResult();
        } else if (swipeFlag == SWIPE_FLAG.IMAGE_LIST) {
            int index = Math.max(0,Math.min(max, a.getImagePosition()));
            a.setImagePosition(index);
            a.appendLog(this,"Action: ImageList");
            Intent intent = new Intent(this, ImageListActivity.class);
            resultLauncher.launch(intent);
        }
    }
    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadForASFResult();
                }
            });


    /** load for call back.
     * Used by Storage Access Framework
     */
    private void loadForASFResult() {
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        int position = a.getImagePosition();
        if (position == -1) {
            a.setImageBuffer(null);
            onMyResume();
            return;
        } else if (position < -1) {
            a.setImagePosition(-1);
            finish();
            return;
        }
        int max = a.getUriEtcList().size() - 1;
        if ((max <= 0) || (max < position)) {
            return;
        }
        UriEtc uriEtc = a.getUriEtcList().get(position);
        MyApplication application = (MyApplication) this.getApplication();
        application.load(this,uriEtc.uri,uriEtc.mime);
        onMyResume();
    }

    /** save for call back.
     * Used by Storage Access Framework
     */
    public void saveForASF() {
        MyApplication a = (MyApplication) this.getApplication();
        //
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.ENGLISH);
        String ext = ((MyApplication)getApplication()).getMyNASI().getImageExt(a.getImageMimeType());
        String title = sdf.format(date) + ext;
        //
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(a.getImageMimeType())
                .putExtra(Intent.EXTRA_TITLE,title);
        //
        resultLauncherSave.launch(intent);
    }

    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
    ActivityResultLauncher<Intent> resultLauncherSave = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        Uri uri = resultData.getData();
                        if (uri != null) {
                            ImageActivity.this.saveForASFResult(uri);
                        }
                    }
                }
            });

    /** save for call back.
     * Used by Storage Access Framework
     */
    public void saveForASFResult(Uri uri) {
        MyApplication a = (MyApplication) this.getApplication();
        a.savingImageBuffer(this,uri);
        a.setImagePosition(0);
        a.getUriEtcList().clear();
        a.setDownloadFlag(false);

    }
}