package jp.ne.ruru.park.ando.naiview.miviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import jp.ne.ruru.park.ando.naiview.ImageActivity;
import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.R;

public class MIViewerActivity extends AppCompatActivity {

    private MISurfaceView imageView;
    private final MISettingDialog dialog = new MISettingDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mi_viewer);
        //
        dialog.setMISettingFinishListener(result->{
            if (result == R.id.mi_finish) {
                Toast.makeText(MIViewerActivity.this,R.string.mi_back_text,Toast.LENGTH_LONG).show();
                MIViewerActivity.this.finish();
            } else {
                dialog.dismissNow();
                //
                imageView.changeStateStart(result);
            }
        });
        //
        final MIViewerData settingData = ((MyApplication)this.getApplication()).getMIViewerData();
        imageView = this.findViewById(R.id.image_view);
        imageView.setSettingData(settingData);
        imageView.setFinishListener(this::showDialog);
    }
    public static final String TAG = "sample";
    protected void showDialog() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(()-> {
            FragmentManager fragmentManager = MIViewerActivity.this.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(TAG);
            if ((fragment instanceof DialogFragment)
                    && ((DialogFragment) fragment).getDialog() != null) {
                return;
            }
            dialog.showNow(fragmentManager, TAG);
        });
    }

    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        this.onMyResume();
    }
    /**
     * repaint data
     */
    public void onMyResume() {
        MyApplication a =
                ((MyApplication) MIViewerActivity.this.getApplication());
        if (a.getImageBuffer() == null) {
            a.updateImageBuffer(this,null,null);
        }
        try (InputStream stream = new ByteArrayInputStream(a.getImageBuffer())){
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            imageView.setImageBitMap(bitmap);
            //
        } catch (Exception e) {
            a.appendLog(this, this.getClass().getName() + " onMyResume() failed");
            a.appendLog(this, e.getMessage());
        }
    }
}