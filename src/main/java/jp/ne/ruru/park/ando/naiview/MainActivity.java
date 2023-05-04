package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import jp.ne.ruru.park.ando.naiview.databinding.ActivityMainBinding;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //
        binding.actionSettings.setOnClickListener(this::myAction);
        binding.actionPrompt.setOnClickListener(this::myAction);
        binding.actionUc.setOnClickListener(this::myAction);
        binding.actionTree.setOnClickListener(this::myAction);
        binding.actionPolicy.setOnClickListener(this::myAction);
        binding.actionImage.setOnClickListener(this::myAction);
        //
        binding.generateImage.setOnClickListener(this::myAction);
        binding.subscription.setOnClickListener(this::myAction);
        //
        //
        if ((action != null) && (type != null)
                && type.startsWith("image/") ) {
            if (Intent.ACTION_SEND.equals(action)) {
                handleSendImage(intent);
            }
        }

    }

    public void myAction(View v) {
        MyApplication a = (MyApplication) this.getApplication();
        if (a.action(MainActivity.this,v.getId())) {
            if (v instanceof Button) {
                String text = ((Button)v).getText().toString();
                a.appendLog(this,"Action: " + text);
            }
        }
    }
    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String mime = intent.getType();
        if (imageUri != null) {
            MyApplication a = (MyApplication) this.getApplication();
            a.load(this,imageUri,mime);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        this.onMyResume();
    }
    public void onMyResume() {
        TextView text = binding.textLog;
        MyApplication application = (MyApplication) this.getApplication();
        text.setText(application.getLog());
    }

}