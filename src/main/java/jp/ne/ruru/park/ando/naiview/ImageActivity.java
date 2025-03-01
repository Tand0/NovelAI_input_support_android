package jp.ne.ruru.park.ando.naiview;


import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityImageBinding;

/** image activity
 * @author T.Ando
 */
public class ImageActivity extends AppCompatActivity {


    /** binding */
    private jp.ne.ruru.park.ando.naiview.databinding.ActivityImageBinding binding;

    /**
     * create views
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.imageView.onMyCreate();
        binding.imageView.setJump(new ImageJumper(this,binding.imageView));
    }

    /** on resume */
    @Override
    public void onResume() {
        super.onResume();
        onMyLoadBitmap();
    }

    public void onMyLoadBitmap() {
        binding.imageView.onMyLoadBitmap();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
}