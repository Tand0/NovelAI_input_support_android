package jp.ne.ruru.park.ando.naiview.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.graphics.Color;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.data.Data;
import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.MyNASI;
import jp.ne.ruru.park.ando.naiview.data.PromptType;
import jp.ne.ruru.park.ando.naiview.R;
import jp.ne.ruru.park.ando.naiview.SuggestActivity;
import jp.ne.ruru.park.ando.naiview.TreeActivity;
import jp.ne.ruru.park.ando.naiview.data.TextType;

/**
 * json list adapter
 * @author T.Ando
 * @param <T> for item
 */
public class JSONListAdapter<T extends JSONObject> extends ArrayAdapter<T> {
    /**
     * This is constructor.
     *
     * @param context  activity object
     * @param resource resource
     */
    public JSONListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    /**
     * @param position The position of the item within the adapter's data set of the item whose view
     *                 we want.
     * @param view     The old view to reuse, if possible. Note: You should check that this view
     *                 is non-null and of an appropriate type before using. If it is not possible to convert
     *                 this view to display the correct data, this method can create a new view.
     *                 Heterogeneous lists can specify their number of view types, so that this View is
     *                 always of the right type (see {@link #getViewTypeCount()} and
     *                 {@link #getItemViewType(int)}).
     * @param parent   The parent that this view will eventually be attached to
     * @return view
     */
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        //
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.raw, parent, false);
        }
        JSONObject item = this.getItem(position);
        boolean expand;
        StringBuilder value = new StringBuilder();
        StringBuilder folderString = new StringBuilder();
        MyApplication a =
                ((MyApplication) ((AppCompatActivity) this.getContext()).getApplication());
        //
        Data itemData = new Data(item);
        expand = itemData.getExpand();
        int level = itemData.getLevel();
        for (int i = 0; i < level; i++) {
            folderString.append("    |");
        }
        value.append("(").append(itemData.getTextType()).append(") ");
        value.append(itemData.getValue());
        Button button = view.findViewById(R.id.button_cheese);
        if (button != null) {
            button.setText(String.format(Locale.ENGLISH, "%d", position));
            button.setOnClickListener(this::executeButton);
        }
        TextView textView = view.findViewById(R.id.folder_cheese);
        if (textView != null) {
            textView.setText(folderString);
        }
        boolean isNotWord = ! TextType.WORD.equals(itemData.getTextType());
        CheckBox checkBox = view.findViewById(R.id.checkbox_cheese);
        if (checkBox != null) {
            if (checkBox.isChecked() != expand) {
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(expand);
            }
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> expand(position, isChecked));
            checkBox.setEnabled(isNotWord);
            boolean darkFlag = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (a.getResources().getConfiguration().isNightModeActive()) {
                    darkFlag = true;
                }
            }
            int backgroundColor;
            int textColor;
            int baseColor;
            int character1Color;
            int character2Color;
            if (!darkFlag) {
                backgroundColor = Color.parseColor("#FFFFFF");
                textColor = Color.parseColor("#000000");
                baseColor = Color.parseColor("#0000C0");
                character1Color = Color.parseColor("#008000");
                character2Color = Color.parseColor("#004060");
            } else {
                backgroundColor = Color.parseColor("#000000");
                textColor = Color.parseColor("#FFFFFF");
                baseColor = Color.parseColor("#CFCFFF");
                character1Color = Color.parseColor("#FFCFFF");
                character2Color = Color.parseColor("#FFCF3F");
            }
            if (! itemData.getIgnore()) {
                PromptType itemPrompt = itemData.getPromptType();
                int color;
                if (! isNotWord) {
                    color = textColor;
                } else if ((PromptType.P_BASE_OK.equals(itemPrompt))
                        || (PromptType.P_BASE_NG.equals(itemPrompt))) {
                    color = baseColor;
                } else if ((PromptType.P_CH01_OK.equals(itemPrompt))
                        || (PromptType.P_CH01_NG.equals(itemPrompt))) {
                    color = character1Color;
                } else {
                    color = character2Color;
                }
                checkBox.setBackgroundColor(backgroundColor);
                checkBox.setTextColor(color);
            } else {
                checkBox.setBackgroundColor(textColor);
                checkBox.setTextColor(backgroundColor);
            }
            checkBox.setText(value.toString());
            checkBox.setHint(String.format(Locale.ENGLISH, "%d - expand", position));
        }
        CheckBox ignoreBox = view.findViewById(R.id.ignore_cheese);
        if (ignoreBox != null) {
            if (ignoreBox.isChecked() != itemData.getIgnore()) {
                ignoreBox.setOnCheckedChangeListener(null);
                ignoreBox.setChecked(itemData.getIgnore());
            }
            ignoreBox.setText("");
            ignoreBox.setHint("");
            ignoreBox.setOnCheckedChangeListener((buttonView, isChecked) -> ignoreButton(position, isChecked));
        }
        return view;
    }

    /**
     * execute button
     *
     * @param view button view
     */
    public void executeButton(View view) {
        Button button = (Button) view;
        int position = Integer.parseInt(button.getText().toString());
        PopupMenu popup = new PopupMenu(this.getContext(), button);
        Menu menu = popup.getMenu();
        popup.getMenuInflater().inflate(R.menu.menu_tree, menu);
        MyApplication a =
                ((MyApplication) ((AppCompatActivity) this.getContext()).getApplication());
        //
        MenuItem pastItem = menu.findItem(R.id.menu_past);
        pastItem.setVisible(a.getCut() != null);
        //
        MenuItem titleItem = menu.findItem(R.id.menu_prompt_type);
        Data positionData = new Data(this.getItem(position));
        int id = positionData.getPromptType().getIdLong();
        String title = a.getResources().getString(id);
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        titleItem.setTitle(s);
        //
        //
        //
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            AppCompatActivity appCompatActivity = (AppCompatActivity) this.getContext();
            ListView listView = appCompatActivity.findViewById(R.id.list_view);
            if (listView != null) {
                listView.setSelection(position);
            }
            return executePopup(item.getItemId(), position);
        });
    }

    /**
     * press button
     *
     * @param itemId   item id
     * @param position position of list view
     * @return if execution then true
     */
    protected boolean executePopup(int itemId, final int position) {
        MyApplication a =
                ((MyApplication) ((AppCompatActivity) this.getContext()).getApplication());
        String textRaw;
        final JSONObject item;
        final Data itemData;
        if (0 <= position) {
            item = this.getItem(position);
            itemData = new Data(item);
            if (item != null) {
                textRaw = itemData.getValue();
            } else {
                textRaw = "";
            }
        } else {
            item = null;
            itemData = new Data(null);
            textRaw = "";
        }
        final String text = textRaw;
        //
        if (itemId == R.id.menu_cut) {
            if (this.cutJSONObject(position)) {
                this.updateJSONArray();
            }
            return true;
        } else if (itemId == R.id.menu_past) {
            if (this.pastJSONObject(position)) {
                this.updateJSONArray();
            }
            return true;
        } else if (itemId == R.id.menu_edit) {
            addText(item, text, itemData.getTextType());
            return true;
        } else if (itemId == R.id.menu_add_text_detail) {
            addText(item, text, TextType.WORD);
            return true;
        } else if (itemId == R.id.menu_add_sequence_detail) {
            addText(item, text, TextType.SEQUENCE);
            return true;
        } else if (itemId == R.id.menu_add_select_detail) {
            addText(item, text, TextType.SELECT);
            return true;
        } else if (itemId == R.id.menu_add_weight_detail) {
            addText(item, text, TextType.WEIGHT);
            return true;
        } else if (itemId == R.id.menu_change_part) {
            if (item != null) {
                a.setChangePartItem(item);
                a.fromTreeToPrompt();
                a.action(this.getContext(), R.id.action_prompt);
            }
            return true;
        }
        return false;
    }

    /**
     * insert or change text to json object
     *
     * @param item           json object
     * @param text           insert or change text
     * @param targetMenuItem menu item
     */
    public void addText(JSONObject item, String text, TextType targetMenuItem) {
        TreeActivity ta = (TreeActivity) this.getContext();
        MyApplication a = (MyApplication) ta.getApplication();
        a.appendLog(ta, "Action: Suggest");
        Intent intent = new Intent(ta, SuggestActivity.class);
        intent.putExtra(SuggestActivity.SUG_T_TYPE, targetMenuItem.toString());
        intent.putExtra(SuggestActivity.SUG_VALUE, text);
        resultLauncher.launch(intent);
        this.suggestPosition = item;
    }

    public JSONObject suggestPosition = null;
    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
    ActivityResultLauncher<Intent> resultLauncher = ((TreeActivity) this.getContext()).registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (suggestPosition == null) {
                    return;
                }
                if (result.getResultCode() != Activity.RESULT_OK) {
                    return;
                }
                Intent resultData = result.getData();
                if (resultData == null) {
                    return;
                }
                String text = resultData.getStringExtra(SuggestActivity.SUG_VALUE);
                boolean isInsert = resultData.getBooleanExtra(SuggestActivity.SUG_IS_INSERT, false);
                TextType changedType = TextType.getTypeString(resultData.getStringExtra(SuggestActivity.SUG_T_TYPE));
                if (TextType.OTHER.equals(changedType)) {
                    return;
                }
                MyApplication a =
                        ((MyApplication) ((AppCompatActivity) this.getContext()).getApplication());
                if (isInsert) {
                    JSONObject target = new JSONObject();
                    Data targetData = new Data(target);
                    targetData.setIgnore(false);
                    targetData.setPromptType(PromptType.P_BASE_OK); // dummy
                    targetData.setTextType(changedType);
                    targetData.setValue(text);
                    targetData.setChild(new JSONArray());
                    if (!TextType.WORD.equals(changedType)) {
                        // this is folder
                        targetData.setExpand(true);
                    }
                    a.setCut(target);
                    this.pastJSONObject(suggestPosition);
                } else { // change
                    if (suggestPosition == null) {
                        return;
                    }
                    Data suggestedData = new Data(suggestPosition);
                    suggestedData.setTextType(changedType);
                    if (TextType.WORD.equals(changedType)) {
                        suggestedData.setChild(new JSONArray()); // clear
                    }
                    suggestedData.setValue(text);
                    this.updateJSONArray();
                }
            });

    /**
     * get top of tree
     * @return top of tree
     */
    public JSONArray getTop() {
        MyApplication application =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        return application.getTop();
    }
    public void expand(int position,boolean isChecked) {
        AppCompatActivity appCompatActivity = (AppCompatActivity)this.getContext();
        ListView listView = appCompatActivity.findViewById(R.id.list_view);
        if (listView == null) {
            return;
        }
        Data itemData = new Data(this.getItem(position));
        if (itemData.getExpand() != isChecked) {
            itemData.setExpand(isChecked);
            listView.setSelection(position);
            updateJSONArray();
        }
    }

    /**
     * ignore button
     * @param position position
     * @param isChecked isChecked
     */
    public void ignoreButton(int position,boolean isChecked) {
        JSONObject item = this.getItem(position);
        Data itemData = new Data(item);
        boolean ignore = itemData.getIgnore();
        if (isChecked != ignore) {
            itemData.setIgnore(isChecked);
            itemData.setTextType(itemData.getTextType());
            updateJSONArray();
        }
    }
    /**
     * past tree
     * @param position past position
     * @return if changed then true
     */
    public boolean pastJSONObject(int position) {
        if (position < 0) {
            return false;
        }
        JSONObject item = this.getItem(position);
        return pastJSONObject(item);
    }

    /**
     * past tree
     * @param item past position
     * @return if changed then true
     */
    public boolean pastJSONObject(JSONObject item) {
        MyApplication a =
                ((MyApplication) ((AppCompatActivity) this.getContext()).getApplication());
        JSONObject cut = a.getCut();
        if (cut == null) {
            return false;
        }
        Data itemData = new Data(item);
        boolean flag;
        boolean expand = itemData.getExpand();
        if (TextType.WORD.equals(itemData.getTextType())
                || (!expand)) {
            flag = this.pastJSONObject(this.getTop(),item,null);
        } else {
            itemData.setExpand(true);
            JSONArray child = itemData.getChild();
            insertObject(child,cut,0);
            Data cutData = new Data(cut);
            cutData.changePromptType(itemData.getPromptType());
            flag = true;
        }
        if (flag) {
            a.setCut(null);
            updateJSONArray();
        }
        return flag;
    }

    /**
     *  insert object.
     * Implemented because there is no insert in JSONArray
     * @param child JSONArray object
     * @param cut insert target
     * @param index insert index
     */
    public void insertObject(JSONArray child,Object cut,int index) {
        ArrayList<Object> list = new ArrayList<>();
        try {
            for (int i = 0 ; i < child.length() ; i++) {
                list.add(child.get(i));

            }
            if (index < list.size()) {
                list.add(index, cut);
            } else {
                list.add(cut);
            }
            while (0 < child.length()) {
                child.remove(0);
            }
            for (int i = 0 ; i < list.size() ; i++) {
                child.put(list.get(i));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * past JSONObject
     * @param array top of tree
     * @param focusObject target
     * @return if changed then true
     */
    public boolean pastJSONObject(JSONArray array,JSONObject focusObject,PromptType targetPrompt) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        JSONObject cut = a.getCut();
        if (cut == null) {
            return false;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject item;
            try {
                item = array.getJSONObject(i);
            } catch (JSONException e) {
                continue;
            }
            Data itemData = new Data(item);
            if (item == focusObject) {
                insertObject(array,cut,i + 1);
                Data cutData = new Data(cut);
                cutData.changePromptType(targetPrompt);
                a.setCut(null);
                return true;
            }
            if (! itemData.getExpand()) {
                continue;
            }
            PromptType nextPrompt = itemData.getPromptType();
            boolean flag = pastJSONObject(itemData.getChild(),focusObject,nextPrompt);
            if (! flag) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * delete object from tree
     * @param position position of list view
     * @return  if changed then true
     */
    public boolean cutJSONObject(int position) {
        if (position < 0) {
            return false;
        }
        JSONArray top = this.getTop();
        JSONObject jsonObject = this.getItem(position);
        boolean flag = removeJSONObject(top,jsonObject);
        if (flag) {
            MyApplication application =
                    ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
            application.setCut(jsonObject);
            this.updateJSONArray();
        }
        return flag;
    }

    /**
     * delete object from tree
     * @param top top of the tree
     * @param jsonObject target object
     * @return if changed then true
     */
    public boolean removeJSONObject(JSONArray top,JSONObject jsonObject) {
        for (int i = 0; i < top.length(); i++) {
            try {
                JSONObject item = top.getJSONObject(i);
                if (item == jsonObject) {
                    top.remove(i);
                    return true;
                }
                Data itemData = new Data(item);
                if (itemData.getExpand()) {
                    boolean flag = removeJSONObject(itemData.getChild(),jsonObject);
                    if (flag) {
                        return true;
                    }
                }
            } catch (JSONException e) {
                // pass
            }
        }
        return false;
    }

    /**
     * create list view data from tree information
     */
    public void updateJSONArray() {
        AppCompatActivity appCompatActivity= (AppCompatActivity)this.getContext();
        ListView listView = appCompatActivity.findViewById(R.id.list_view);
        if (listView == null) {
            return;
        }
        int position = listView.getSelectedItemPosition();
        //
        createDefaultTree();
        //
        ArrayList<T> list = new ArrayList<>();
        updateJSONArray(list, 0, this.getTop());
        this.clear();
        this.addAll(list);
        //
        int max = listView.getChildCount();
        if (0 <= position) {
            position = Math.min(position,max - 1);
            listView.setSelection(position);
        }
    }

    /**
     * if top of tree is null then create default object
     */
    public void createDefaultTree() {
        Data topData = new Data(this.getTop());
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        for (PromptType promptType: PromptType.values()) {
            boolean hit = false;
            for (JSONObject item : topData) {
                Data itemData = new Data(item);
                if (promptType.equals(itemData.getPromptType())) {
                    hit = true; // hit
                    break;
                }
            }
            if (hit) {
                continue;
            }
            String defaultString = a.getApplicationContext().getResources().getString(promptType.getIdLong());
            JSONArray childArray = new JSONArray();
            //
            JSONObject target = new JSONObject();
            this.getTop().put(target);
            //
            Data targetData = new Data(target);
            targetData.setTextType(TextType.SEQUENCE);
            targetData.setPromptType(promptType);
            targetData.setValue(defaultString);
            targetData.setExpand(true);
            targetData.setChild(childArray);
            String[] strings;
            switch (promptType) {
                case P_BASE_OK:
                    strings = MyNASI.DEFAULT_PROMPT.split(",");
                    break;
                case P_BASE_NG:
                    strings = MyNASI.DEFAULT_PROMPT_UC.split(",");
                    break;
                default:
                    strings = new String[]{};
            }
            for (String split : strings) {
                JSONObject child = new JSONObject();
                Data childData = new Data(child);
                childData.setTextType(TextType.WORD);
                childData.setPromptType(promptType);
                childData.setValue(split.trim());
                childData.setExpand(false);
                childData.setChild(new JSONArray());
                childArray.put(child);
            }
        }
    }

    /**
     * if top of tree is null then create default object
     * @param list list view data
     * @param level level of tree
     * @param target target object
     */
    @SuppressWarnings("unchecked")
    public void updateJSONArray(ArrayList<T> list, int level,Object target) {
        Data itemData = new Data(target);
        if (target instanceof JSONArray) {
            for (JSONObject obj : itemData) {
                updateJSONArray(list, level,obj);
            }
        } else if (target instanceof JSONObject) {
            itemData.setLevel(level);
            list.add((T)target);
            boolean expand = itemData.getExpand();
            if (expand) {
                JSONArray child = itemData.getChild();
                updateJSONArray(list, level + 1, child);
            }
        }
    }
}
