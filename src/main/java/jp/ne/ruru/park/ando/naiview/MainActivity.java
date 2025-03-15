package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
        //
        // version information
        try {
            String name = this.getPackageName();
            PackageManager pm = this.getPackageManager();
            PackageInfo info = pm.getPackageInfo(name, PackageManager.GET_META_DATA);
            a.appendLog(this, "Version:" + info.versionCode + " / " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            a.appendLog(this, e.getMessage());
        }

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        //
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        addButton(viewGroup);
        //
        //
        if ((action != null) && (type != null)
                && type.startsWith("image/") ) {
            if (Intent.ACTION_SEND.equals(action)) {
                handleSendImage(intent);
            }
        }

    }
    public void addButton(View view) {
        if (view instanceof Button) {
            view.setOnClickListener(this::myAction);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                addButton(vg.getChildAt(i));
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
        MyApplication a = (MyApplication) this.getApplication();
        return a.action(this, buttonId) || super.onOptionsItemSelected(menuButton);
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