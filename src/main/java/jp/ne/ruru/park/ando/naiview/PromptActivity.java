package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
        binding.promptTitle.setText(getMyTitle());
    }

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