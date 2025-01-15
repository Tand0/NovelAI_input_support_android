package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import jp.ne.ruru.park.ando.naiview.data.PromptType;
import jp.ne.ruru.park.ando.naiview.databinding.FragmentCh02NgBinding;

public class PromptCh02NgFragment extends PromptAbstractFragment {

    /** binding */
    private FragmentCh02NgBinding binding;

    public PromptCh02NgFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCh02NgBinding.inflate(getLayoutInflater());
        getTextPrompt().addTextChangedListener(myTextWatcher);
        super.onCreateNext();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
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

    @Override
    public PromptType getPromptType() {
        return PromptType.P_CH02_NG;
    }

}