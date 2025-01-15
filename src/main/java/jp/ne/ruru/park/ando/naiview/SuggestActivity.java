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

import jp.ne.ruru.park.ando.naiview.data.Data;
import jp.ne.ruru.park.ando.naiview.data.TextType;
import jp.ne.ruru.park.ando.naiview.databinding.ActivitySuggestBinding;
import jp.ne.ruru.park.ando.naiview.adapter.SuggestList;
import jp.ne.ruru.park.ando.naiview.adapter.SuggestListAdapter;

/** tree activity
 * @author T.Ando
 */
public class SuggestActivity extends AppCompatActivity {
    /** tree adapter */
    private SuggestListAdapter<SuggestList> adapter;

    private ActivitySuggestBinding binding;

    public static final String SUG_IS_INSERT = "SuggestActivity.is_insert";
    public static final String SUG_VALUE = "SuggestActivity.VALUE";
    public static final String SUG_T_TYPE = "SuggestActivity.T_TYPE";


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
        binding.wordEditInsert.setOnClickListener((v)->wordEditBack(true));
        binding.wordEditChange.setOnClickListener((v)->wordEditBack(false));
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String sTType = intent.getStringExtra(SUG_T_TYPE);
        TextType selectedTItem = TextType.getTypeString(sTType);
        if (TextType.OTHER.equals(selectedTItem)) {
            binding.wordEditSpinner.setEnabled(false);
            binding.wordEditSpinner.setVisibility(View.GONE);
            binding.wordEditChange.setEnabled(false);
            binding.wordEditChange.setVisibility(View.GONE);
        } else {
            binding.wordEditSpinner.setEnabled(true);
            binding.wordEditSpinner.setVisibility(View.VISIBLE);
            binding.wordEditSpinner.setSelection(selectedTItem.getIndex());
            binding.wordEditChange.setEnabled(true);
            binding.wordEditChange.setVisibility(View.VISIBLE);
        }
        update(intent.getStringExtra(SUG_VALUE));
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
        int position = binding.wordEditSpinner.getSelectedItemPosition();
        if ((position < 0) || (TextType.values().length <= position)) {
            position = 0;
        }
        intent.putExtra(SUG_T_TYPE,TextType.values()[position].toString());
        intent.putExtra(SUG_IS_INSERT,isInsert);
        intent.putExtra(SUG_VALUE,getPrompt().trim());

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
        if (wordRaw.isEmpty()) {
            a.appendLog(this,"suggest==null");
            return;
        }
        a.execution(this, MyNASI.REST_TYPE.SUGGEST_TAGS,0,0,wordRaw);
    }
    public void suggestTagsResponse(String string) {
        MyApplication a =
                ((MyApplication)this.getApplication());

        List<SuggestList> list = new ArrayList<>();

        JSONArray array;
        try {
            JSONObject top = new JSONObject(string);
            array = Data.containJSONArray(top,"tags");
        } catch (JSONException e) {
            try {
                array = new JSONArray(string);
            } catch (JSONException ex) {
                array = new JSONArray();
                a.appendLog(this,"suggestTagsResponse parse error");
                a.appendLog(this,ex.getMessage());
            }
        }
        try {
             for (int i = 0; i < array.length() ; i++) {
                Object object = array.get(i);
                String tag = Data.containString(object,"tag");
                Integer count = Data.containInt(object,"count");
                Double confidence = Data.containDouble(object,"confidence");
                String jpTag = Data.containString(object,"jp_tag");
                String enTag = Data.containString(object,"en_tag");
                Integer power = Data.containInt(object,"power");
                if ((tag != null) && (count != null) && (confidence != null)) {
                    list.add(new SuggestList(tag,count,confidence, null, 0));
                } else if ((jpTag != null) && (enTag != null) && (power != null)) {
                    list.add(new SuggestList(enTag,0,0, jpTag, power));
                }
            }
        } catch (JSONException e) {
            a.appendLog(this,"suggestTagsResponse parse get error");
            a.appendLog(this,e.getMessage());
        }
        if (list.isEmpty()) {
            // this is empty response
            list.add(new SuggestList("null",0,0,null, 0));
        }
        updateAdapter(list);
    }
}