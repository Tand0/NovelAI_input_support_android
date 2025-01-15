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
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

    /** max upscale size */
    public static final int MAX_UPSCALE_SIZE = 768;

    /** detector for click */
    GestureDetector gestureDetector;

    /** binding */
    private ActivityImageBinding binding;

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
        gestureDetector = new GestureDetector(this,listener);
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
    /** on resume */
    @Override
    public void onResume() {
        super.onResume();
        this.onMyResume();
    }

    public int bitmapX = 0;
    public int bitmapY = 0;

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
            binding.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            a.appendLog(this, this.getClass().getName() + " onMyResume() failed");
            a.appendLog(this, e.getMessage());
        }
    }

    /** generate image */
    public void generateImage() {
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
        String imageText =  ImageActivity.this.getResources().getString(R.string.generate_image) + addText;
        String upscaleText = ImageActivity.this.getResources().getString(R.string.upscale)
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
                        generateImage();
                    } else if (which == 2) {
                        doUpscale();
                    }
                })
                .setPositiveButton(R.string.action_save_external,(dialog,which)-> saveForASF())
                .setNeutralButton(R.string.menu_cancel,(dialog,which)-> a.setDownloadFlag(false))
                .show();
    }

    /** detector for click */
    public GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent event) {
            onSaveDialogMenu();
            return super.onSingleTapUp(event);
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
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e1 == null) {
                return super.onFling(null, e2, velocityX, velocityY);
            }
            float dx = (e1.getX() - e2.getX());
            dx = dx * dx;
            float dy = e1.getY() - e2.getY();
            dy = dy * dy;
            final float SWIPE_DISTANCE = 45 * 45;
            if ((dy < SWIPE_DISTANCE)
                && (dx < SWIPE_DISTANCE)) {
                return super.onFling(null, e2, velocityX, velocityY);
            } else if (dx < dy) {
                swipeFlag = 2;
                onMyFling();
                return true;
            } else if (e1.getX() < e2.getX()) {
                swipeFlag = 0;
                onMyFling();
                return true;
            } else {
                swipeFlag = 1;
                onMyFling();
                return true;
            }
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

    public int swipeFlag = -1;
    public void onMyNextFling() {
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        if (a.getDownloadFlag()) {
            onSaveDialogMenu();
        } else {
            onMyNextNextFling();
        }
    }
    public void onMyNextNextFling() {
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
        if (swipeFlag == 0) {
            int index = a.getImagePosition() - 1;
            a.setImagePosition(index);
            loadForASFResult();
        } else if (swipeFlag == 1) {
            int index = Math.min(max, a.getImagePosition() + 1);
            a.setImagePosition(index);
            loadForASFResult();
        } else {
            int index = Math.max(0,Math.min(max, a.getImagePosition()));
            a.setImagePosition(index);
            a.appendLog(this,"Action: ImageList");
            Intent intent = new Intent(this, ImageListActivity.class);
            resultLauncher.launch(intent);
        }
    }
    ActivityResultLauncher<String> readImagesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    onMyNextFling();
                }
            });


    /**
     * on touch event
     * @param event The touch screen event being processed.
     *
     * @return if used then true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

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