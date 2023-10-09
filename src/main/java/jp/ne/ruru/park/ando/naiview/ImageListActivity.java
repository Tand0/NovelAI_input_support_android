package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.adapter.ImagesListAdapter;
import jp.ne.ruru.park.ando.naiview.adapter.UriEtc;
import jp.ne.ruru.park.ando.naiview.databinding.ActivityImageListBinding;

/** tree activity
 * @author foobar@em.boo.jp
 */
public class ImageListActivity extends AppCompatActivity {

    private ActivityImageListBinding binding;
    /** on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        binding = ActivityImageListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        ImagesListAdapter<UriEtc> adapter = new ImagesListAdapter<>(this, android.R.layout.simple_list_item_1);
        //
        final MyApplication a =
                ((MyApplication)this.getApplication());
        adapter.addAll(a.getUriEtcList());
        binding.imagesListView.setAdapter(adapter);
        final int position = Math.max(0, a.getImagePosition());
        final int max = a.getUriEtcList().size();

        binding.imagesListView.setOnItemClickListener(
                (parent, view, imagePosition, id) -> {
                    a.setImagePosition(imagePosition);
                    Intent intent = getIntent();
                    this.setResult(Activity.RESULT_OK,intent);
                    this.finish();
                });
        binding.imagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // N ONE
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int position = binding.imagesListSeekbar.getProgress();
                if ((firstVisibleItem <= position)
                        && (position < (firstVisibleItem + visibleItemCount))) {
                    return;
                }
                setText(firstVisibleItem,max - 1);
                binding.imagesListSeekbar.setProgress(firstVisibleItem);
            }
        });
        binding.imagesListSeekbar.setProgress(0);
        binding.imagesListSeekbar.setMax(max - 1);
        binding.imagesListSeekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            setText(progress, max - 1);
                        }
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        binding.imagesListView.setSelection(progress);
                    }
                });
        if ((0 <= position) && (position < max)) {
            binding.imagesListView.setSelection(position);
            binding.imagesListSeekbar.setProgress(position);
            setText(position,max - 1);
        }
        //
    }
    public void setText(int progress,int max) {
        String str = String.format(Locale.US, "%d/%d",progress,max);
        binding.imagesListText.setText(str);
    }
}