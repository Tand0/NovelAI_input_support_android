package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/** main activity
 * @author T.Ando
 */
public class MainActivity extends AppCompatActivity {

    /** binding */
    private ActivityMainBinding binding;

    /**
     * on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //
        // load to the top
        MyApplication a = (MyApplication) this.getApplication();
        a.loadInternal(this);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        //
        binding.actionSettings.setOnClickListener(this::myAction);
        binding.actionPrompt.setOnClickListener(this::myAction);
        binding.actionTree.setOnClickListener(this::myAction);
        binding.actionPolicy.setOnClickListener(this::myAction);
        binding.actionImage.setOnClickListener(this::myAction);
        //
        binding.subscription.setOnClickListener(this::myAction);
        binding.actionCreateAccount.setOnClickListener(this::myAction);
        //
        //
        if ((action != null) && (type != null)
                && type.startsWith("image/") ) {
            if (Intent.ACTION_SEND.equals(action)) {
                handleSendImage(intent);
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        } else if (buttonId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity( intent );
            return true;
        }
        return  super.onOptionsItemSelected(menuButton);
    }

    /**
     * click event for button
     * @param v view
     */
    public void myAction(View v) {
        MyApplication a = (MyApplication) this.getApplication();
        if (a.action(MainActivity.this,v.getId())) {
            if (v instanceof Button) {
                String text = ((Button)v).getText().toString();
                a.appendLog(this,"Action: " + text);
            }
        }
    }

    /**
     * event for other application
     * @param intent intent
     */
    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String mime = intent.getType();
        if (imageUri != null) {
            MyApplication a = (MyApplication) this.getApplication();
            a.load(this,imageUri,mime);
        }
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
        MyApplication application = (MyApplication) this.getApplication();
        binding.textLog.setText(application.getLog());
        binding.scrollView.fullScroll(View.FOCUS_DOWN);
    }
}