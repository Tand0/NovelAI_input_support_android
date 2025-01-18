package jp.ne.ruru.park.ando.naiview;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import jp.ne.ruru.park.ando.naiview.adapter.PromptFragmentAdapter;
import jp.ne.ruru.park.ando.naiview.data.Data;
import jp.ne.ruru.park.ando.naiview.data.PromptType;
import jp.ne.ruru.park.ando.naiview.databinding.ActivityPromptBinding;

/** prompt activity
 * @author T.Ando
 */
public class PromptActivity extends AppCompatActivity {
    /** binding */
    private ActivityPromptBinding binding;

    private ViewPager2 pager;
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
        binding = ActivityPromptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(this.getResources().getString(PromptType.P_BASE_OK.getIdShort()));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        pager = binding.pager;
        PromptFragmentAdapter adapter = new PromptFragmentAdapter(this);
        pager.setAdapter(adapter);
        //
        TabLayout tabs = binding.tabLayout;
        TabLayoutMediator manager = new TabLayoutMediator(tabs, pager, (tab, position) -> {
                int id = PromptFragmentAdapter.getPositionToPromptType(position).getIdShort();
                tab.setText(this.getResources().getString(id));
            }
        );
        manager.attach();
        //
        final MyApplication a = (MyApplication) this.getApplication();
        //
        binding.fromTreeToPrompt.setOnClickListener(view-> {
            adapter.actionSave();
            a.fromTreeToPrompt();
            adapter.actionLoad();
        });
        //
        binding.actionClear.setOnClickListener(view-> {
            a.setChangePartItem(null); // clear data..
            onResumePart(); // change button
        });
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
    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        onResumePart();
    }
    public void onResumePart() {
        MyApplication a = (MyApplication) this.getApplication();
        if (a == null) {
            return;
        }
        Button treeToPrompt = binding.fromTreeToPrompt;
        Button menuChangePart = binding.actionClear;
        if (a.getChangePartItem() == null) {
            String text = this.getResources().getString(R.string.setting_use_tree);
            treeToPrompt.setText(text);
            menuChangePart.setVisibility(View.GONE);
        } else {
            String text = this.getResources().getString(R.string.menu_change_part);
            treeToPrompt.setText(text);
            menuChangePart.setVisibility(View.VISIBLE);
        }
        //
        PromptType promptTarget;
        if (a.getChangePartItem() == null) {
            int target0 = a.getPromptValue(PromptType.P_BASE_OK).length();
            int target1 = a.getPromptValue(PromptType.P_CH01_OK).length();
            int target2 = a.getPromptValue(PromptType.P_CH02_OK).length();
            if ((target1 <= target0) && (target2 <= target0)) {
                promptTarget = PromptType.P_BASE_OK;
            } else if (target2 <= target1) {
                promptTarget = PromptType.P_CH01_OK;
            } else {
                promptTarget = PromptType.P_CH02_OK;
            }
        } else {
            Data cutData = new Data(a.getChangePartItem());
            promptTarget = cutData.getPromptType();
        }
        for (int i = 0 ; i < PromptType.values().length ; i++) {
            if (promptTarget.equals(PromptFragmentAdapter.getPositionToPromptType(i))) {
                pager.setCurrentItem(i, false);
                break;
            }
        }
    }
}