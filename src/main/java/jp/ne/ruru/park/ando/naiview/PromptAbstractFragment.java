package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

public abstract class PromptAbstractFragment extends Fragment {

    public PromptAbstractFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    protected void onCreateNext() {
        getFromPromptToTree().setOnClickListener(view->{
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            MyApplication a = (MyApplication) activity.getApplication();
            setText(a,getTextPrompt().getText().toString());
            a.fromPromptToTree(getText(a),isPrompt());
            String message = "OK: To Tree";
            Toast.makeText(getActivity() , message, Toast.LENGTH_SHORT).show();
            a.appendLog(getActivity(),message);
        });
        getFromTreeToPrompt().setOnClickListener(view-> actionFromTreeToPrompt());

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
            intent.putExtra(SuggestActivity.TYPE,TextType.TEXT_OTHER.toString());
            intent.putExtra(SuggestActivity.TEXT,target);
            resultLauncher.launch(intent);
        });

        getToClear().setOnClickListener(view->{
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            MyApplication a = (MyApplication) activity.getApplication();
            if (a == null) {
                return;
            }
            //
            // clear
            a.changePart(PromptAbstractFragment.this.getContext(),null);
            //
            // change button
            onResumePart();
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
                    String text = resultData.getStringExtra(SuggestActivity.TEXT);
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
     * Action FromTreeToPrompt
     */
    public void actionFromTreeToPrompt() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        MyApplication a = (MyApplication) activity.getApplication();
        //
        // update application
        setText(a,getTextPrompt().getText().toString());
        a.fromTreeToPrompt(getContext(),isPrompt());
        getTextPrompt().setText(getText(a));
    }

    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        this.onResumePart();
    }
    public void onResumePart() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        MyApplication a = (MyApplication) activity.getApplication();
        getTextPrompt().setText(getText(a));
        //
        Button treeToPrompt = this.getFromTreeToPrompt();
        Button menuChangePart = this.getToClear();
        if (a.getChangePartItem() == null) {
            String text = PromptAbstractFragment.this.getResources().getString(R.string.setting_use_tree);
            treeToPrompt.setText(text);
            menuChangePart.setVisibility(View.GONE);
        } else {
            String text = PromptAbstractFragment.this.getResources().getString(R.string.menu_change_part);
            treeToPrompt.setText(text);
            menuChangePart.setVisibility(View.VISIBLE);
        }
    }
    /**
     * on pause
     */
    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        MyApplication a = (MyApplication) activity.getApplication();
        setText(a, getTextPrompt().getText().toString());
    }

    public abstract Button getFromPromptToTree();
    public abstract Button getFromTreeToPrompt();
    public abstract Button getToSuggest();
    public abstract Button getToClear();

    public abstract EditText getTextPrompt();

    public abstract TextView getTokenView();
    /**
     * get prompt from application
     * @return prompt
     */
    public abstract String getText(MyApplication a);

    /**
     * set prompt to application
     * @param text prompt
     */
    public abstract void setText(MyApplication a, String text);

    /**
     * if prompt then true
     * @return always true
     */
    public abstract boolean isPrompt();


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
            String index = "token=" + ans.length + " / 255";
            getTokenView().setText(index);
        }
    };
}