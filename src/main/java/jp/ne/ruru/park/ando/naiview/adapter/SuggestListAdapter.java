package jp.ne.ruru.park.ando.naiview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.ne.ruru.park.ando.naiview.R;
import jp.ne.ruru.park.ando.naiview.SuggestActivity;

/**
 * suggest list adapter
 * @author T.Ando
 * @param <T> for item
 */
public class SuggestListAdapter<T extends SuggestList> extends ArrayAdapter<T> {


    /**
     * This is constructor.
     * @param context activity object
     * @param resource resource
     */
    public SuggestListAdapter(@NonNull Context context, int resource) {
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
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.raw_suggest, parent, false);
        }
        TextView textView = view.findViewById(R.id.list_suggest_p1);
        SuggestList suggestList = getItem(position);
        if (suggestList == null) {
            suggestList = new SuggestList("",0,0.0, null, 0);
        }
        if (textView != null) {
            textView.setText(suggestList.toTextString());
        }
        Button button = view.findViewById(R.id.list_suggest_p3);
        if (button != null) {
            button.setText(suggestList.tag);
            button.setOnClickListener(x->{
                String text = ((TextView)x).getText().toString();
                ((SuggestActivity)this.getContext()).update(text);
            });
        }
        return view;
    }
}
