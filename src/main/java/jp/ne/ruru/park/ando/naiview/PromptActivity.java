package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityPromptBinding;

public class PromptActivity extends AppCompatActivity {

    private ActivityPromptBinding binding;

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
            Toast.makeText(this , message, Toast.LENGTH_LONG).show();
            a.appendLog(this,message);
        });
        binding.fromTreeToPrompt.setOnClickListener(view->{
            MyApplication a = (MyApplication) this.getApplication();
            a.fromTreeToPrompt(isPrompt());
            binding.textPrompt.setText(getText());
            String message = "OK: From Tree";
            Toast.makeText(this , message, Toast.LENGTH_LONG).show();
            a.appendLog(this,message);
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        TextView text = binding.textPrompt;
        text.setText(getText());
    }
    @Override
    public void onPause() {
        super.onPause();
        setText(binding.textPrompt.getText().toString());
    }
    public String getText() {
        MyApplication a = (MyApplication) this.getApplication();
        return a.getPrompt();
    }
    public void setText(String text) {
        MyApplication a = (MyApplication) this.getApplication();
        a.setPrompt(text);
    }
    public boolean isPrompt() {
        return true;
    }
}