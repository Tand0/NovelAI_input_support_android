package com.github.tand0.naiview;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.tand0.naiview.data.PromptType;

public abstract class PromptAbstractFragment extends Fragment {

    public PromptAbstractFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void onCreateNext() {
    }

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