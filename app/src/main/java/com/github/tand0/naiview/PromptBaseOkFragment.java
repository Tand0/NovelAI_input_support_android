package com.github.tand0.naiview;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tand0.naiview.data.PromptType;
import com.github.tand0.naiview.databinding.FragmentBaseOkBinding;

public class PromptBaseOkFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentBaseOkBinding binding;

    public PromptBaseOkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentBaseOkBinding.inflate(getLayoutInflater());
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
        return binding.textPrompt;
    }
    @Override
    public TextView getTokenView() {
        return binding.tokenView;
    }

    @Override
    public PromptType getPromptType() {
        return PromptType.P_BASE_OK;
    }

}