package jp.ne.ruru.park.ando.naiview.miviewer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.R;

public class MIViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mi_viewer);
    }
    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroy() {
        Toast.makeText(this,R.string.mi_back_text,Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}