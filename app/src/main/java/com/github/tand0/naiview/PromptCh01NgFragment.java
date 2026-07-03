package com.github.tand0.naiview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tand0.naiview.data.PromptType;
import com.github.tand0.naiview.databinding.FragmentCh01NgBinding;

public class PromptCh01NgFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentCh01NgBinding binding;

    public PromptCh01NgFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCh01NgBinding.inflate(getLayoutInflater());
        getTextPrompt().addTextChangedListener(myTextWatcher);
        super.onCreateNext();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }
    @Override
    public EditText getTextPrompt() {
        return binding.textPromptUc;
    }
    @Override
    public TextView getTokenView() {
        return binding.tokenView;
    }

    @Override
    public PromptType getPromptType() {
        return PromptType.P_CH01_NG;
    }

}