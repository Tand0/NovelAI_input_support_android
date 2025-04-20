package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.adapter.UriEtc;

public class ImageJumper {
    private final ComponentActivity activity;
    private final ImageJumperCallBack callback;
    //
    public final float SWIPE_DISTANCE = 45.0f * 45.0f;
    private final ActivityResultLauncher<Intent> resultImageListLauncher;
    private final ActivityResultLauncher<Intent> resultLauncherSave;
    private final ActivityResultLauncher<String> readImagesPermissionLauncher;

    /** enum swipe flag */
    public enum SWIPE_FLAG {
        NONE,
        MOVE_BACK,
        MOVE_FORWARD,
        IMAGE_LIST,
        SHOW_DIALOG,
        EXPAND,
        DOWN_SELECT
    }

    /** swipe flag */
    protected SWIPE_FLAG swipeFlag = SWIPE_FLAG.NONE;
    public ImageJumper(@NonNull ComponentActivity activity, @NonNull ImageJumperCallBack callback) {
        this.activity = activity;
        this.callback = callback;
        //
        resultImageListLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadForASFResult();
                    }
                });
        //
        resultLauncherSave = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent resultData  = result.getData();
                        if (resultData  != null) {
                            Uri uri = resultData.getData();
                            if (uri != null) {
                                this.saveForASFResult(uri);
                            }
                        }
                    }
                });

        readImagesPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) {
                        onMyNextFling();
                    }
                });
    }

    private float getDirectionSquared(float sqrt) {
        return sqrt * sqrt;
    }

    public void jump(float directionX, float directionY) {
        final float dx = getDirectionSquared(directionX);
        final float dy = getDirectionSquared(directionY);
        final SWIPE_FLAG state;
        final MyApplication a = (MyApplication)activity.getApplication();
        if (a.getDownloadFlag()) {
            state = SWIPE_FLAG.SHOW_DIALOG;
        } else if ((dy < SWIPE_DISTANCE) && (dx < SWIPE_DISTANCE)) {
            state = SWIPE_FLAG.SHOW_DIALOG;
        } else {
            state = dx < dy ?
                    (directionY < 0 ? SWIPE_FLAG.IMAGE_LIST : SWIPE_FLAG.DOWN_SELECT) :
                    (directionX < 0 ? SWIPE_FLAG.MOVE_BACK : SWIPE_FLAG.MOVE_FORWARD);
        }
        jump(state);
    }
    public void jump(final SWIPE_FLAG state) {
        try {
            switch (state) {
                case SHOW_DIALOG:
                case IMAGE_LIST:
                case MOVE_BACK:
                case MOVE_FORWARD:
                    callback.onClearImage();
                    swipeFlag = state;
                    onMyFling();
                    break;
                case DOWN_SELECT:
                    callback.onMyDownSelect();
                    break;
                default:
                    break;
            }
        } catch (IllegalStateException e) {
            final MyApplication a =
                    ((MyApplication)activity.getApplication());
            a.appendLog(activity,e.getMessage());
        }
    }
    private void onMyFling() {
        String readImagesPermission =
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
                        android.Manifest.permission.READ_MEDIA_IMAGES
                        : android.Manifest.permission.READ_EXTERNAL_STORAGE;
        if (activity.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES)!=
                PackageManager.PERMISSION_GRANTED) {
            try {
                readImagesPermissionLauncher.launch(readImagesPermission);
            } catch (IllegalStateException e) {
                final MyApplication a =
                        ((MyApplication)activity.getApplication());
                a.appendLog(activity,e.getMessage());
            }
        } else {
            onMyNextFling();
        }
    }

    private void onMyNextFling() {
        if (swipeFlag == SWIPE_FLAG.SHOW_DIALOG) {
            callback.onMyShowDialog();
            return;
        }
        final MyApplication a =
                ((MyApplication)activity.getApplication());
        a.setDownloadFlag(false);
        //
        if (a.getUriEtcList().isEmpty()) {
            //
            ContentResolver cr = activity.getContentResolver();
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
            a.appendLog(activity,"Action: ImageList");
            Toast.makeText(activity,R.string.action_image_list,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, ImageListActivity.class);
            resultImageListLauncher.launch(intent);
        }
    }

    /** load for call back.
     * Used by Storage Access Framework
     */
    private void loadForASFResult() {
        final MyApplication a =((MyApplication)activity.getApplication());
        int position = a.getImagePosition();
        if (position == -1) {
            a.setImageBuffer(null);
            callback.onMyLoadBitmap();
            return;
        } else if (position < -1) {
            a.setImagePosition(-1);
            activity.finish();
            return;
        }
        int max = a.getUriEtcList().size() - 1;
        if ((max <= 0) || (max < position)) {
            return;
        }
        UriEtc uriEtc = a.getUriEtcList().get(position);
        MyApplication application = (MyApplication) activity.getApplication();
        application.load(activity,uriEtc.uri,uriEtc.mime);
        callback.onMyLoadBitmap();
    }
    /** save for call back.
     * Used by Storage Access Framework
     */
    protected void saveForASF() {
        MyApplication a = (MyApplication) activity.getApplication();
        //
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.ENGLISH);
        String ext = a.getMyNASI().getImageExt(a.getImageMimeType());
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
    private void saveForASFResult(Uri uri) {
        MyApplication a = (MyApplication) activity.getApplication();
        a.savingImageBuffer(activity,uri);
        a.setImagePosition(0);
        a.getUriEtcList().clear();
        a.setDownloadFlag(false);
    }

}