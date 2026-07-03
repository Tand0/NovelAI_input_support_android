package com.github.tand0.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import com.github.tand0.naiview.data.TextType;
import com.github.tand0.naiview.databinding.ActivitySuggestBinding;

/** tree activity
 * @author T.Ando
 */
public class SuggestActivity extends AppCompatActivity {

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
}