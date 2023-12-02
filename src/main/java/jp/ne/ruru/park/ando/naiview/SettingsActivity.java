package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import jp.ne.ruru.park.ando.naiview.databinding.ActivitySettingsBinding;


/** setting activity
 * @author foobar@em.boo.jp
 */
public class SettingsActivity extends AppCompatActivity {

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
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton) {
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        } else if (buttonId == R.id.action_tree) {
            Intent intent = new Intent(this, TreeActivity.class);
            this.startActivity( intent );
            return true;
        }
        return super.onOptionsItemSelected(menuButton);
    }
    /**
     * Settings fragment
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        /**
         * on create preferences
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         *                           this is the state.
         * @param rootKey            If non-null, this preference fragment should be rooted at the
         *                           PreferenceScreen with this key.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

}