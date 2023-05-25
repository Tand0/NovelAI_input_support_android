package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
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
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityImageBinding;

/** image activity
 * @author foobar@em.boo.jp
 */
public class ImageActivity extends AppCompatActivity {

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
        //
        binding.imageTop.setEnabled(false);
        binding.imageTop.setVisibility(View.GONE);
    }

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
            a.updateImageBuffer(this,null,"image/png");
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
            // NONE
        }
    }

    /**
     * do upscale
     */
    public void doUpscale() {
        if ((bitmapX <= 768) && (bitmapY <= 768)) {
            final MyApplication a =
                    ((MyApplication)ImageActivity.this.getApplication());
            a.execution(ImageActivity.this, MyNASI.TYPE.UPSCALE, bitmapX, bitmapY);
        } else {
            String message = String.format(
                    Locale.ENGLISH, "already big(%dx%d)", bitmapX,bitmapY);
            Toast.makeText(this , message, Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveDialogMenu() {
        binding.imageTop.setEnabled(false);
        binding.imageTop.setVisibility(View.GONE);
        //
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        String title = ImageActivity.this.getResources().getString(R.string.menu_move);
        if (0 < bitmapX) {
            title = String.format(Locale.ENGLISH, "%s(%dx%d)", title, bitmapX,bitmapY);
        }
        int anlas = a.getAnlas();
        if (0 <= anlas) {
            title = String.format(Locale.ENGLISH, "%s (anlas: %d)", title, anlas);
        }
        final String[] items = new String[] {
                ImageActivity.this.getResources().getString(R.string.generate_image),
                ImageActivity.this.getResources().getString(R.string.upscale),
                ImageActivity.this.getResources().getString(R.string.action_save_external),
                "Cancel"
        };
        new AlertDialog.Builder(ImageActivity.this)
                .setTitle(title)
                .setItems(items, (dialog,which)-> {
                    if (which == 0) {
                        a.execution(ImageActivity.this,MyNASI.TYPE.IMAGE,bitmapX,bitmapY);
                    } else if (which == 1) {
                        doUpscale();
                    } else if (which == 2) {
                        saveForASF();
                    } else if (which == 3) {
                        a.setDownloadFlag(false);
                    }
                })
                .show();
    }

    /** detector for click */
    public GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
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
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                       float velocityY) {
            int SWIPE_DISTANCE = 150;
            if (e1.getX() - e2.getX() <  (- SWIPE_DISTANCE)) {
                swipeFlag = 0;
                onMyFling();
                return true;
            } else if (SWIPE_DISTANCE < e1.getX() - e2.getX()) {
                swipeFlag = 1;
                onMyFling();
                return true;
            } else if (e1.getY() - e2.getY() <  (- SWIPE_DISTANCE)) {
                swipeFlag = 2;
                onMyFling();
                return true;
            } else if (SWIPE_DISTANCE < e1.getY() - e2.getY()) {
                swipeFlag = 3;
                onMyFling();
                return true;
            }
            return super.onFling(e1,e2,velocityX,velocityY);
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
    public static class UriEtc {
        UriEtc(Uri uri,String mime) {
            this.uri = uri;
            this.mime = mime;
        }
        public Uri uri;
        public String mime;
        @Override
        public boolean equals(Object o) {
            if (o instanceof UriEtc) {
                return this.uri.equals(((UriEtc) o).uri);
            }
            return super.equals(o);
        }
    }
    public LinkedList<UriEtc> uriEtcList = new LinkedList<>();
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
        if (uriEtcList.size() == 0) {
            ContentResolver cr = getContentResolver();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DATE_ADDED
            };
            String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";// ASC or DESC;
            try (Cursor cursor = cr.query(
                    uri, projection, null, null, sortOrder)) {

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
                    if (!uriEtcList.contains(uriEtc)) {
                        uriEtcList.add(uriEtc);
                    }
                }
            }
        }
        int max = uriEtcList.size() - 1;
        if (max < 0) {
            a.setImagePosition(-1);
            return;
        }
        int index;
        if (a.getImagePosition() < 0) {
            index = 0;
        } else if (swipeFlag == 0) {
            index = Math.max(0, a.getImagePosition() - 1);
        } else if (swipeFlag == 1) {
            index = Math.min(max, a.getImagePosition() + 1);
        } else {
            index = Math.max(0,Math.min(max, a.getImagePosition()));
        }
        if (swipeFlag == 2) {
            binding.imageTop.setEnabled(true);
            binding.imageTop.setVisibility(View.VISIBLE);
            binding.imageSeekbar.setMin(0);
            binding.imageSeekbar.setMax(max);
            binding.imageSeekbar.setOnSeekBarChangeListener(null);
            binding.imageSeekbar.setProgress(index);
            binding.imageSeekbar.setOnSeekBarChangeListener(seekListener);
            setPositionText(index,max);
            return;
        } else if (swipeFlag == 3) {
            binding.imageTop.setEnabled(false);
            binding.imageTop.setVisibility(View.GONE);
            return;
        }
        a.setImagePosition(index);
        loadForASFResult();
    }

    /**
     * display position
     * @param index position
     * @param max max
     */
    public void setPositionText(int index, int max) {
        String text = "" + index + "/" + max;
        binding.imagePosition.setText(text);
    }
    /**
     * A SeekBar is an extension of ProgressBar that adds a draggable thumb. The user can touch
     * the thumb and drag left or right to set the current progress level or use the arrow keys.
     * Placing focusable widgets to the left or right of a SeekBar is discouraged.
     */
    public SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         * @param seekBar The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range min..max where min
         *                 and max were set by setMin(int) and
         *                 setMax(int), respectively. (The default values for
         *                 min is 0 and max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setPositionText(seekBar.getProgress(),seekBar.getMax());
        }

        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //NONE
        }

        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            final MyApplication a =
                    ((MyApplication)ImageActivity.this.getApplication());
            int max = uriEtcList.size() - 1;
            if (max < 0) {
                a.setImagePosition(-1);
                return;
            }
            int position = seekBar.getProgress();
            if (uriEtcList.size() <= position) {
                return;
            }
            a.setImagePosition(position);
            loadForASFResult();
        }
    };
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
        int max = uriEtcList.size() - 1;
        if ((max <= 0) || (max < position)) {
            return;
        }
        UriEtc uriEtc = uriEtcList.get(position);

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
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            os.write(a.getImageBuffer());
            //
            a.setImagePosition(0);
            this.uriEtcList.clear();
            a.setDownloadFlag(false);
            //
        } catch (IOException e) {
            // NONE
        }
    }
}