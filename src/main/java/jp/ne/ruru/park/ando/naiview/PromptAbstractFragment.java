package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import jp.ne.ruru.park.ando.naiview.data.PromptType;
import jp.ne.ruru.park.ando.naiview.data.TextType;

public abstract class PromptAbstractFragment extends Fragment {

    public PromptAbstractFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void onCreateNext() {
        getToSuggest().setOnClickListener(view->{
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            MyApplication a = (MyApplication) activity.getApplication();
            a.appendLog(getActivity(),"Action: Suggest");
            Intent intent = new Intent(getActivity(), SuggestActivity.class);
            int start = getTextPrompt().getSelectionStart();
            String target = "";
            if (0 <= start) {
                int end = getTextPrompt().getSelectionEnd();
                target = getTextPrompt().getText().subSequence(start, end).toString();
            }
            intent.putExtra(SuggestActivity.SUG_T_TYPE, TextType.OTHER.toString());
            intent.putExtra(SuggestActivity.SUG_VALUE,target);
            resultLauncher.launch(intent);
        });
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
                    String text = resultData.getStringExtra(SuggestActivity.SUG_VALUE);
                    if ((text == null) || text.isEmpty()) {
                        return;
                    }
                    String value = getTextPrompt().getText().toString();
                    int start = getTextPrompt().getSelectionStart();
                    int end = getTextPrompt().getSelectionEnd();
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
                    targetStart = targetStart.replaceFirst("[{(\\[,\\s]+$","");
                    targetEnd = targetEnd.replaceFirst("^[})\\],\\s]+","");
                    if (!targetStart.isEmpty()) {
                        text = targetStart + ", " + text;
                    }
                    if (!targetEnd.isEmpty()) {
                        text = text + ", " + targetEnd;
                    }
                    Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    MyApplication a = (MyApplication) activity.getApplication();
                    setText(a, text);
                    getTextPrompt().setText(text);
                }
            });
    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        this.onLoad();
    }
    public void onLoad() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        MyApplication a = (MyApplication) activity.getApplication();
        if (a == null) {
            return;
        }
        getTextPrompt().setText(getText(a));
    }
    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
        onSave();
    }
    public void onSave() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        MyApplication a = (MyApplication) activity.getApplication();
        setText(a, getTextPrompt().getText().toString());
    }

    public abstract Button getToSuggest();

    public abstract EditText getTextPrompt();

    public abstract TextView getTokenView();
    /**
     * get prompt
     * @return prompt
     */
    public String getText(MyApplication a) {
        return a.getPromptValue(getPromptType());
    }

    /**
     * set prompt
     * @param text prompt
     */
    public void setText(MyApplication a, String text) {
        a.setPromptValue(getPromptType(), text);
    }

    /**
     * get prompt type
     * @return always prompt type
     */
    public abstract PromptType getPromptType();


    protected final TextWatcher myTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //EMPTY
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //EMPTY
        }

        @Override
        public void afterTextChanged(Editable s) {
            String[] ans = s.toString().replaceAll("[^0-9a-zA-Z]"," ").split("\\s+");
            int id = getPromptType().getIdLong();
            String index = "token= " + ans.length + "/255:" + getResources().getString(id);
            getTokenView().setText(index);
        }
    };
}