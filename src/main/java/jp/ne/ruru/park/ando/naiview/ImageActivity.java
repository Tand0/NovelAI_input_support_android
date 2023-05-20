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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    ActivityResultLauncher<Intent> resultLauncherLoad = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        ImageActivity.this.loadForASFResult(resultData.getData(),resultData.getType());
                    }
                }
            });

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
            a.updateImageBuffer(this,null,"image/png");
        }
        try (InputStream stream = new ByteArrayInputStream(a.getImageBuffer())){
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            binding.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            // NONE
        }
    }

    /** detector for click */
    public GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            final MyApplication a =
                    ((MyApplication)ImageActivity.this.getApplication());
            String title = ImageActivity.this.getResources().getString(R.string.menu_move);
            int anlas = a.getAnlas();
            if (0 <= anlas) {
                title = String.format(Locale.ENGLISH, "%s (anlas: %d)", title, anlas);
            }
            final String[] items = {
                    ImageActivity.this.getResources().getString(R.string.generate_image),
                    ImageActivity.this.getResources().getString(R.string.action_load),
                    ImageActivity.this.getResources().getString(R.string.action_save_external),
                    "Cancel"
            };
            new AlertDialog.Builder(ImageActivity.this)
                    .setTitle(title)
                    .setItems(items, (dialog,which)-> selectResult(which))
                    .show();
            return super.onDoubleTap(event);
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
    public int swipeFlag = 0;
    public void onMyNextFling() {
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
        final MyApplication a =
                ((MyApplication)ImageActivity.this.getApplication());
        if (uriEtcList.size() == 0) {
            a.setImagePosition(-1);
            return;
        }
        int index;
        if (swipeFlag == 0) {
            index = a.getImagePosition() - 1;
        } else if (swipeFlag == 1) {
            index = a.getImagePosition() + 1;
        } else {
            index = (int) (Math.random() * uriEtcList.size());
        }
        index = (index < 0) ? uriEtcList.size() -1 : ((uriEtcList.size() <= index) ? 0 : index);
        a.setImagePosition(index);
        UriEtc uriEtc = uriEtcList.get(a.getImagePosition());
        String message = "Index:" + (a.getImagePosition()+1) + "/" + uriEtcList.size() + " URI:" + uriEtc.uri.toString();
        Toast.makeText(this , message, Toast.LENGTH_SHORT).show();
        loadForASFResult(uriEtc.uri,uriEtc.mime);
    }
    ActivityResultLauncher<String> readImagesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    onMyNextFling();
                }
            });

    /** callback detector for click.
     * Used by Storage Access Framework
     * @param which action id
     */
    public void selectResult(int which) {
        if (which == 0) {
            final MyApplication a =
                    ((MyApplication)ImageActivity.this.getApplication());
            a.execution(ImageActivity.this,MyNASI.TYPE.IMAGE);
        } else if (which == 1) {
            load();
        } else if (which == 2) {
            saveForASF();
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
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /** load image */
    public void load() {
        Intent load = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        load.addCategory(Intent.CATEGORY_OPENABLE);
        load.setType("image/png");
        resultLauncherLoad.launch(load);
    }

    /** load for call back.
     * Used by Storage Access Framework
     */
    private void loadForASFResult(Uri uri,String mime) {
        MyApplication application = (MyApplication) this.getApplication();
        application.load(this,uri,mime);
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
            a.setImagePosition(0);
            this.uriEtcList.clear();
        } catch (IOException e) {
            // NONE
        }
    }
}