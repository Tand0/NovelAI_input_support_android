package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.ne.ruru.park.ando.naiview.databinding.FragmentPromptUcBinding;

public class PromptUcFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentPromptUcBinding binding;

    public PromptUcFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentPromptUcBinding.inflate(getLayoutInflater());
        getTextPrompt().addTextChangedListener(myTextWatcher);
        super.onCreateNext();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }
    @Override
    public Button getFromPromptToTree() {
        return binding.fromPromptToTree;
    }
    @Override
    public  Button getFromTreeToPrompt() {
        return binding.fromTreeToPrompt;
    }
    @Override
    public  Button getToSuggest() {
        return binding.toSuggest;
    }
    @Override
    public EditText getTextPrompt() {
        return binding.textPromptUc;
    }
    @Override
    public TextView getTokenView() {
        return binding.tokenView;
    }
    /**
     * get prompt
     * @return prompt
     */
    @Override
    public String getText(MyApplication a) {
        return a.getUc();
    }

    /**
     * set prompt
     * @param text prompt
     */
    @Override
    public void setText(MyApplication a, String text) {
        a.setUc(text);
    }

    /**
     * if prompt then true
     * @return always false
     */
    @Override
    public boolean isPrompt() {
        return false;
    }

    @Override
    public Button getToClear() {
        return binding.actionClear;
    }
}