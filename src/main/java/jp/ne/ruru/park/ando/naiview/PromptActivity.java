package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import jp.ne.ruru.park.ando.naiview.adapter.PromptFragmentAdapter;
import jp.ne.ruru.park.ando.naiview.databinding.ActivityPromptBinding;

/** prompt activity
 * @author foobar@em.boo.jp
 */
public class PromptActivity extends AppCompatActivity {

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

        ActivityPromptBinding binding = ActivityPromptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(this.getResources().getString(R.string.action_prompt));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ViewPager2 pager = binding.pager;
        PromptFragmentAdapter adapter = new PromptFragmentAdapter(this);
        pager.setAdapter(adapter);

        //
        TabLayout tabs = binding.tabLayout;
        new TabLayoutMediator(tabs, pager,
                (tab, position) -> tab.setText(
                        position == 0 ? this.getResources().getString(R.string.action_prompt)
                                : this.getResources().getString(R.string.action_uc))
        ).attach();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_prompt, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        } else if (buttonId == R.id.action_image) {
            Intent intent = new Intent(this, ImageActivity.class);
            this.startActivity( intent );
            return true;
        }
        return super.onOptionsItemSelected(menuButton);
    }

}