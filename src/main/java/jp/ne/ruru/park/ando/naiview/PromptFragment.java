package jp.ne.ruru.park.ando.naiview;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.ne.ruru.park.ando.naiview.databinding.FragmentPromptBinding;


public class PromptFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentPromptBinding binding;

    public PromptFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        binding = jp.ne.ruru.park.ando.naiview.databinding.FragmentPromptBinding.inflate(getLayoutInflater());
        getTextPrompt().addTextChangedListener(super.myTextWatcher);
        super.onCreate(savedInstanceState);
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
    public Button getFromTreeToPrompt() {
        return binding.fromTreeToPrompt;
    }
    @Override
    public Button getToSuggest() {
        return binding.toSuggest;
    }

    @Override
    public EditText getTextPrompt() {
        return binding.textPrompt;
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
       return a.getPrompt();
    }

    /**
     * set prompt
     * @param text prompt
     */
    @Override
    public void setText(MyApplication a, String text) {
        a.setPrompt(text);
    }

    /**
     * if prompt then true
     * @return always true
     */
    @Override
    public boolean isPrompt() {
        return true;
    }

}