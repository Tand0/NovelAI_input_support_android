package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.ne.ruru.park.ando.naiview.databinding.ActivitySuggestBinding;
import jp.ne.ruru.park.ando.naiview.adapter.SuggestList;
import jp.ne.ruru.park.ando.naiview.adapter.SuggestListAdapter;

/** tree activity
 * @author foobar@em.boo.jp
 */
public class SuggestActivity extends AppCompatActivity {
    /** tree adapter */
    private SuggestListAdapter<SuggestList> adapter;

    private ActivitySuggestBinding binding;

    public static final String TYPE = "SuggestActivity.type";
    public static final String IS_INSERT = "SuggestActivity.is_insert";
    public static final String TEXT = "SuggestActivity.TEXT";


    /** on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //
        MyApplication a =
                ((MyApplication)this.getApplication());
        //
        binding.wordEditEnhance.setOnClickListener((v)->{
            String values = getPrompt();
            int index = a.getEnhancePos(values) + 1;
            values = a.getEnhanceText(values, index);
            update(values);
        });
        binding.wordEditNotEnhance.setOnClickListener((v)->{
            String values = getPrompt();
            int index = a.getEnhancePos(values) - 1;
            values = a.getEnhanceText(values, index);
            update(values);
        });
        binding.wordEditFromTree.setOnClickListener(view->{
            a.fromTreeToPrompt(true);
            List<String> listString  = a.fromTreeList(a.getTop(),true);
            List<SuggestList> list = new ArrayList<>();
            for (String string : listString) {
                list.add(new SuggestList(string, 1, 0.0));
            }
            updateAdapter(list);
        });
        binding.wordEditInsert.setOnClickListener((v)->wordEditBack(true));
        binding.wordEditChange.setOnClickListener((v)->wordEditBack(false));
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        int selectedItemPosition = intent.getIntExtra(TYPE, -1);
        if (selectedItemPosition < 0) {
            binding.wordEditSpinner.setEnabled(false);
            binding.wordEditSpinner.setVisibility(View.GONE);
            binding.wordEditChange.setEnabled(false);
            binding.wordEditChange.setVisibility(View.GONE);
        } else {
            binding.wordEditSpinner.setEnabled(true);
            binding.wordEditSpinner.setVisibility(View.VISIBLE);
            binding.wordEditSpinner.setSelection(selectedItemPosition);
            binding.wordEditChange.setEnabled(true);
            binding.wordEditChange.setVisibility(View.VISIBLE);
        }
        update(intent.getStringExtra(TEXT));
        //
        binding.suggestTags.setOnClickListener(this::suggestTags);
        //
        adapter = new SuggestListAdapter<>(this, android.R.layout.simple_list_item_1);
        binding.suggestListView.setAdapter(adapter);
        //

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuButton);
    }
    public void wordEditBack(boolean isInsert) {
        Intent intent = getIntent();
        intent.putExtra(TYPE,binding.wordEditSpinner.getSelectedItemPosition());
        intent.putExtra(IS_INSERT,isInsert);
        intent.putExtra(TEXT,getPrompt().trim());

        SuggestActivity.this.setResult(Activity.RESULT_OK,intent);
        SuggestActivity.this.finish();
    }

    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    public void update(String text) {
        binding.wordEditText.setText(text);
    }
    public String getPrompt() {
        return binding.wordEditText.getText().toString();
    }
    public void updateAdapter(List<SuggestList> list) {
        adapter.clear();
        adapter.addAll(list);
    }

    public void suggestTags(View view) {
        MyApplication a =
                ((MyApplication)this.getApplication());
        String wordRaw = getPrompt()
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "")
                .trim()
                .replaceFirst("\\s*:\\d+\\s*$","");
        if (wordRaw.equals("")) {
            a.appendLog(this,"suggest==null");
            return;
        }
        a.execution(this, MyNASI.TYPE.SUGGEST_TAGS,0,0,wordRaw);
    }

    public void suggestTagsResponse(String string) {
        MyApplication a =
                ((MyApplication)this.getApplication());
        try {
            JSONObject top = new JSONObject(string);
            List<SuggestList> list = new ArrayList<>();
            JSONArray array = a.containJSONArray(top,"tags");
            for (int i = 0; i < array.length() ; i++) {
                Object object = array.get(i);
                String tag = a.containString(object,"tag");
                Integer count = a.containInt(object,"count");
                Double confidence = a.containDouble(object,"confidence");
                if ((tag == null) || (count == null) || (confidence == null)) {
                    continue;
                }
                list.add(new SuggestList(tag,count,confidence));
            }
            updateAdapter(list);
        } catch (JSONException e) {
            a.appendLog(this,e.getMessage());
        }

    }
}