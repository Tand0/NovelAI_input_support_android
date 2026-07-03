package com.github.tand0.naiview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tand0.naiview.data.PromptType;
import com.github.tand0.naiview.databinding.FragmentBaseNgBinding;

public class PromptBaseNgFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentBaseNgBinding binding;

    public PromptBaseNgFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentBaseNgBinding.inflate(getLayoutInflater());
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
        return PromptType.P_BASE_NG;
    }

}