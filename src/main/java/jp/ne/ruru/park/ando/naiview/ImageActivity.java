package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                    ImageActivity.this.getResources().getString(R.string.action_save),
                    "Cancel"
            };
            new AlertDialog.Builder(ImageActivity.this)
                    .setTitle(title)
                    .setItems(items, (dialog,which)-> selectResult(which))
                    .show();
            return super.onDoubleTap(event);
        }
    };

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
    private void loadForASFResult(Uri imageUri,String mime) {
        MyApplication application = (MyApplication) this.getApplication();
        application.load(this,imageUri,mime);
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
        } catch (IOException e) {
            // NONE
        }
    }
}