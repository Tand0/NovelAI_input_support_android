package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityMainSettingBinding;

/** main activity
 * @author T.Ando
 */
public class MainSettingActivity extends AppCompatActivity {


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
        ActivityMainSettingBinding binding = ActivityMainSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        addButton(viewGroup);
        //
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
        if (a.action(MainSettingActivity.this,v.getId())) {
            if (v instanceof Button) {
                String text = ((Button)v).getText().toString();
                a.appendLog(this,"Action: " + text);
            }
        }
    }


    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

}