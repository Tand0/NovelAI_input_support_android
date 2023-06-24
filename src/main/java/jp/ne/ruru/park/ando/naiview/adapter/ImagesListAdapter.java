package jp.ne.ruru.park.ando.naiview.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.R;

/**
 * images list adapter
 * @author foobar@em.boo.jp
 * @param <T> for item
 */
public class ImagesListAdapter<T extends UriEtc> extends ArrayAdapter<T> {


    /**
     * This is constructor.
     * @param context activity object
     * @param resource resource
     */
    public ImagesListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    /**
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param view The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.raw_images, parent, false);
        }
        ContentResolver cr = this.getContext().getContentResolver();
        //
        UriEtc etc = this.getItem(position);
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
        };
        Bitmap result = null;
        String title = "title";
        StringBuilder subTitle = new StringBuilder();
        try (Cursor cursor = cr.query(etc.uri,projection,null,null,null)) {
            if (cursor == null) {
                throw new IllegalArgumentException();
            }
            while (cursor.moveToNext()) {
                //
                int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                if (0 < dateColumn) {
                    long milliSeconds = cursor.getLong(dateColumn) * 1000L;
                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(milliSeconds);
                    subTitle = new StringBuilder(formatter.format(calendar.getTime()));
                }
                //
                int sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                if (0 < sizeColumn) {
                    long size = cursor.getLong(sizeColumn);
                    subTitle.append(" / ").append(String.format(Locale.getDefault(), "%,d", size)).append("byte");
                }
                //
                int widthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                int heightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                if (0 < widthColumn) {
                    long width = cursor.getLong(widthColumn);
                    long height = cursor.getLong(heightColumn);
                    subTitle.append(" / ").append(width).append("x").append(height);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        result = cr.loadThumbnail(etc.uri,new android.util.Size(96, 96),null);
                    } catch (IOException e) {
                        // NONE
                    }
                } else {
                    int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    long id = idColumn < 0 ? -1 : cursor.getLong(idColumn);
                    if (0 < id) {
                        result = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    }
                }
                //
                int titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                title = titleColumn < 0 ? "base image" : cursor.getString(titleColumn);
                //
            }
        } catch (IllegalArgumentException e) {
            // NONE
        }
        //
        ImageView imageView = view.findViewById(R.id.thumbnail);
        if ((imageView != null) && (result != null)) {
            imageView.setImageBitmap(result);
        }
        TextView textView = view.findViewById(R.id.thumbnail_p1);
        if (textView != null) {
            textView.setText(title);
        }
        textView = view.findViewById(R.id.thumbnail_p2);
        if (textView != null) {
            textView.setText(subTitle.toString());
        }
        return view;
    }
}
