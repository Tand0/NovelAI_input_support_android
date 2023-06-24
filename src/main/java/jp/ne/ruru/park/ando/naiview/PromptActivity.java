package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityPromptBinding;

/** prompt activity
 * @author foobar@em.boo.jp
 */
public class PromptActivity extends AppCompatActivity {

    /** binding */
    private ActivityPromptBinding binding;

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

        binding = ActivityPromptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.fromPromptToTree.setOnClickListener(view->{
            MyApplication a = (MyApplication) this.getApplication();
            a.setPrompt(binding.textPrompt.getText().toString());
            a.fromPromptToTree(getText(),isPrompt());
            String message = "OK: To Tree";
            Toast.makeText(this , message, Toast.LENGTH_SHORT).show();
            a.appendLog(this,message);
        });
        binding.fromTreeToPrompt.setOnClickListener(view->{
            MyApplication a = (MyApplication) this.getApplication();
            a.fromTreeToPrompt(isPrompt());
            binding.textPrompt.setText(getText());
            String message = "OK: From Tree";
            Toast.makeText(this , message, Toast.LENGTH_SHORT).show();
            a.appendLog(this,message);
        });

        binding.toSuggest.setOnClickListener(view->{
            MyApplication a = (MyApplication) this.getApplication();
            a.appendLog(this,"Action: Suggest");
            Intent intent = new Intent(this, SuggestActivity.class);
            int start = binding.textPrompt.getSelectionStart();
            String target = "";
            if (0 <= start) {
                int end = binding.textPrompt.getSelectionEnd();
                target = binding.textPrompt.getText().subSequence(start, end).toString();
            }
            intent.putExtra(SuggestActivity.TYPE,-1);
            intent.putExtra(SuggestActivity.TEXT,target);
            resultLauncher.launch(intent);
        });

        binding.actionImage.setOnClickListener(view->{
            MyApplication a = (MyApplication) this.getApplication();
            a.action(this,R.id.action_image);
        });
        binding.promptTitle.setText(getMyTitle());

    }

    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData == null) {
                        return;
                    }
                    String text = resultData.getStringExtra(SuggestActivity.TEXT);
                    if (text.equals("")) {
                        return;
                    }
                    String value = binding.textPrompt.getText().toString();
                    int start = binding.textPrompt.getSelectionStart();
                    int end = binding.textPrompt.getSelectionEnd();
                    String targetStart;
                    String targetEnd;
                    if (0 <= start) {
                        targetStart = value.subSequence(0,start).toString();
                        if (start < end) {
                            targetEnd = value.subSequence(end,value.length()).toString();
                        } else {
                            targetEnd = value.subSequence(start,value.length()).toString();
                        }
                    } else {
                        targetStart = value;
                        targetEnd = "";
                    }
                    targetStart = targetStart.replaceFirst("[}{)(,\\[\\]\\s]+$","");
                    targetEnd = targetEnd.replaceFirst("^[}{)(,\\[\\]\\s]+","");
                    if (!targetStart.equals("")) {
                        text = targetStart + ", " + text;
                    }
                    if (!targetEnd.equals("")) {
                        text = text + ", " + targetEnd;
                    }
                    PromptActivity.this.setText(text);
                }
            });



    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        TextView text = binding.textPrompt;
        text.setText(getText());
    }

    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
        setText(binding.textPrompt.getText().toString());
    }

    /**
     * get title
     * @return title
     */
    public String getMyTitle() {
        return this.getResources().getString(R.string.action_prompt);
    }

    /**
     * get prompt
     * @return prompt
     */
    public String getText() {
        MyApplication a = (MyApplication) this.getApplication();
        return a.getPrompt();
    }

    /**
     * set prompt
     * @param text prompt
     */
    public void setText(String text) {
        MyApplication a = (MyApplication) this.getApplication();
        a.setPrompt(text);
    }

    /**
     * if prompt then true
     * @return always true
     */
    public boolean isPrompt() {
        return true;
    }
}