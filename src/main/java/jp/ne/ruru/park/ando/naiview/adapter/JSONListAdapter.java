package jp.ne.ruru.park.ando.naiview.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.R;
import jp.ne.ruru.park.ando.naiview.SuggestActivity;
import jp.ne.ruru.park.ando.naiview.TreeActivity;

/**
 * json list adapter
 * @author foobar@em.boo.jp
 * @param <T> for item
 */
public class JSONListAdapter<T extends JSONObject> extends ArrayAdapter<T> {
    protected final String[] SS = {
            MyApplication.TEXT_WORD,
            MyApplication.TEXT_SEQUENCE,
            MyApplication.TEXT_SELECT,
            MyApplication.TEXT_WEIGHT};
    /**
     * This is constructor.
     * @param context activity object
     * @param resource resource
     */
    public JSONListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    /**
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param view The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.raw, parent, false);
        }
        JSONObject item = this.getItem(position);
        boolean expand;
        StringBuilder value = new StringBuilder();
        StringBuilder folderString = new StringBuilder();
        boolean isNotWord = true;
        boolean isUc = true;
        boolean isIgnore = true;
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        if (item != null) {

            Boolean expandBoolean = a.containBoolean(item, MyApplication.EXPAND);
            if (expandBoolean != null) {
                expand = expandBoolean;
            } else {
                expand = false;
            }
            Integer level = a.containInt(item,MyApplication.LEVEL);
            if (level != null) {
                for (int i = 0 ; i < level ; i++) {
                    folderString.append("    |");
                }
            }
            String text = a.containString(item,MyApplication.TEXT);
            if (text != null) {
                if (text.contains(MyApplication.TEXT_WORD)) {
                    isNotWord = false;
                }
                value.append("(").append(text).append(") ");
                isUc = text.contains(MyApplication.TEXT_UC);
                isIgnore = text.contains(MyApplication.TEXT_IGNORE);
            }
            String values = a.containString(item,MyApplication.VALUES);
            if (values != null) {
                value.append(values);
            }
        } else {
            expand = false;
        }
        Button button = view.findViewById(R.id.button_cheese);
        if (button != null) {
            button.setText(String.format(Locale.ENGLISH, "%d", position));
            button.setOnClickListener(this::executeButton);
        }
        TextView textView = view.findViewById(R.id.folder_cheese);
        if (textView != null) {
            textView.setText(folderString);
        }
        CheckBox checkBox = view.findViewById(R.id.checkbox_cheese);
        if (checkBox != null) {
            if (checkBox.isChecked() != expand) {
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(expand);
            }
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> expand(position,isChecked));
            checkBox.setEnabled(isNotWord);
            if (! isIgnore) {
                checkBox.setBackgroundColor(Color.parseColor("#FFFFFF"));
                if (isUc) {
                    if (isNotWord) {
                        checkBox.setTextColor(Color.parseColor("#008000"));
                    } else {
                        checkBox.setTextColor(Color.parseColor("#004000"));
                    }
                } else if (isNotWord) {
                    checkBox.setTextColor(Color.parseColor("#000080"));
                } else {
                    checkBox.setTextColor(Color.parseColor("#000000"));
                }
            } else {
                checkBox.setBackgroundColor(Color.parseColor("#000000"));
                checkBox.setTextColor(Color.parseColor("#F8F8F8"));
            }
            checkBox.setText(value.toString());
            checkBox.setHint(String.format(Locale.ENGLISH, "%d - expand", position));
        }
        checkBox = view.findViewById(R.id.ignore_cheese);
        if (checkBox != null) {
            if (checkBox.isChecked() != isIgnore) {
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(isIgnore);
            }
            checkBox.setText("");
            checkBox.setHint("");
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> ignoreButton(position,isChecked));
        }
        return view;
    }

    /**
     * execute button
     * @param view button view
     */
    public void executeButton(View view) {
        Button button = (Button) view;
        int position = Integer.parseInt(button.getText().toString());
        PopupMenu popup = new PopupMenu(this.getContext(), button);
        popup.getMenuInflater().inflate(R.menu.menu_tree, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {
            AppCompatActivity appCompatActivity = (AppCompatActivity)this.getContext();
            if (appCompatActivity != null) {
                ListView listView = appCompatActivity.findViewById(R.id.list_view);
                if (listView != null) {
                    listView.setSelection(position);
                }
            }
            return executePopup(item.getItemId(),position);
        });
    }

    /**
     * press button
     * @param itemId item id
     * @param position position of list view
     * @return if execution then true
     */
    protected boolean executePopup(int itemId,final int position) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        String textRaw;
        final JSONObject item;
        int ssIndex = 0;
        if (0 <= position) {
            item  = this.getItem(position);
            if (item != null) {
                textRaw = a.containString(item,MyApplication.VALUES);
                if (textRaw == null) {
                    textRaw = "";
                }
                String textKey = a.containString(item,MyApplication.TEXT);
                int i = 0;
                for (String s: SS) {
                    if (textKey.equals(s)) {
                       ssIndex = i;
                       break;
                    }
                    i++;
                }
            } else {
                textRaw = "";
            }
        } else {
            item = null;
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
        } else if (itemId == R.id.menu_add_text) {
            addText( item, text,0);
            return true;
        } else if (itemId == R.id.menu_tree) {
            addText( item, text,ssIndex);
            return true;
        } else if (itemId == R.id.menu_change_part) {
            if (item != null) {
                a.changePart(this.getContext(),item);
            }
            return true;
        }
        return false;
    }

    /**
     * insert or change text to json object
     * @param item json object
     * @param text insert or change text
     * @param targetMenuItem if text then 0 else 1
     */
    public void addText(JSONObject item,String text,int targetMenuItem) {
        TreeActivity ta = (TreeActivity)this.getContext();
        MyApplication a = (MyApplication) ta.getApplication();
        a.appendLog(ta,"Action: Suggest");
        Intent intent = new Intent(ta, SuggestActivity.class);
        intent.putExtra(SuggestActivity.TYPE,targetMenuItem);
        intent.putExtra(SuggestActivity.TEXT,text);
        resultLauncher.launch(intent);
        this.suggestPosition = item;
    }
    public JSONObject suggestPosition = null;
    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
    ActivityResultLauncher<Intent> resultLauncher = ((TreeActivity)this.getContext()).registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (suggestPosition == null) {
                    return;
                }
                if (result.getResultCode() != Activity.RESULT_OK) {
                    return;
                }
                Intent resultData  = result.getData();
                if (resultData == null) {
                    return;
                }
                String text = resultData.getStringExtra(SuggestActivity.TEXT);
                boolean isInsert = resultData.getBooleanExtra(SuggestActivity.IS_INSERT,false);
                int selectedItemPosition = resultData.getIntExtra(SuggestActivity.TYPE,-1);
                if (selectedItemPosition < 0) {
                    return;
                }
                MyApplication a =
                        ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
                if (isInsert) {
                    JSONObject target = new JSONObject();
                    try {
                        target.put(MyApplication.TEXT, SS[selectedItemPosition]);
                        target.put(MyApplication.VALUES, text);
                        JSONArray array = new JSONArray();
                        target.put(MyApplication.CHILD,array);
                        if (selectedItemPosition != 0) {
                            target.put(MyApplication.EXPAND, true);
                            JSONObject child = new JSONObject();
                            array.put(child);
                            child.put(MyApplication.TEXT, MyApplication.TEXT_WORD);
                            child.put(MyApplication.VALUES, text);
                            child.put(MyApplication.CHILD,new JSONArray());
                        }
                        a.setCut(target);
                        this.pastJSONObject(suggestPosition);
                    } catch (JSONException e) {
                        // NONE
                    }
                } else { // change
                    if (suggestPosition == null) {
                        return;
                    }
                    try {
                        String key = a.containString(suggestPosition,MyApplication.TEXT);
                        if ((key != null) && (!key.contains(MyApplication.TEXT_WORD))
                            && (selectedItemPosition != 0)) {
                            for (String s: SS) {
                                key = key.replace(s,"");
                            }
                            key = key + SS[selectedItemPosition];
                            suggestPosition.put(MyApplication.TEXT, key);
                        }
                        suggestPosition.put(MyApplication.VALUES, text);
                    } catch (JSONException e) {
                        return;
                    }
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
        if (appCompatActivity == null) {
            return;
        }
        ListView listView = appCompatActivity.findViewById(R.id.list_view);
        if (listView == null) {
            return;
        }
        MyApplication a = (MyApplication)appCompatActivity.getApplication();
        JSONObject item = this.getItem(position);
        boolean expand;
        Boolean expandBoolean = a.containBoolean(item,MyApplication.EXPAND);
        if (expandBoolean != null) {
            expand = expandBoolean;
        } else {
            expand = false;
        }
        if (expand != isChecked) {
            try {
                item.put(MyApplication.EXPAND,isChecked);
                listView.setSelection(position);
                updateJSONArray();
            } catch (JSONException e) {
                // NONE
            }
        }
    }

    /**
     * ignore button
     * @param position position
     * @param isChecked isChecked
     */
    public void ignoreButton(int position,boolean isChecked) {
        JSONObject item = this.getItem(position);
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        String text = a.containString(item,MyApplication.TEXT);
        if (text == null) {
            return;
        }
        boolean flag = false;
        if (isChecked) {
            if (!text.contains(MyApplication.TEXT_IGNORE)) {
                text = MyApplication.TEXT_IGNORE + text;
                flag = true;
            }
        } else {
            if (text.contains(MyApplication.TEXT_IGNORE)) {
                text = text.replace(MyApplication.TEXT_IGNORE, "");
                flag = true;
            }
        }
        if (flag) {
            try {
                item.put(MyApplication.TEXT, text);
                this.updateJSONArray();
            } catch (JSONException e) {
                // NONE
            }
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
        JSONArray top = this.getTop();
        String text = a.containString(item,MyApplication.TEXT);
        if (text == null) {
            return false;
        }
        boolean flag;
        try {
            Boolean expand = a.containBoolean(item,MyApplication.EXPAND);
            if (text.contains(MyApplication.TEXT_WORD)
                || (expand == null)
                || (!expand)) {
                flag = this.pastJSONObject(top,item);
            } else {
                item.put(MyApplication.EXPAND,true);
                JSONArray child = a.containJSONArray(item,MyApplication.CHILD);
                if (child == null) {
                    child =new JSONArray();
                    cut.put(MyApplication.CHILD, child);
                }
                insertObject(child,cut,0);
                flag = true;
            }
        } catch (JSONException e) {
            flag = false;
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
     * @param top top of tree
     * @param focusObject target
     * @return if changed then true
     */
    public boolean pastJSONObject(JSONArray top,JSONObject focusObject) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        JSONObject cut = a.getCut();
        if (cut == null) {
            return false;
        }
        for (int i = 0; i < top.length(); i++) {
            JSONObject item;
            try {
                item = top.getJSONObject(i);
            } catch (JSONException e) {
                continue;
            }
            if (item == focusObject) {
                insertObject(top,cut,i + 1);
                a.setCut(null);
                return true;
            }
            Boolean expand = a.containBoolean(item,MyApplication.EXPAND);
            JSONArray child = a.containJSONArray(item,MyApplication.CHILD);
            if ((expand != null)
                    && expand
                    && (child != null)) {
                boolean flag = pastJSONObject(child,focusObject);
                if (flag) {
                    return true;
                }
            }

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
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        for (int i = 0; i < top.length(); i++) {
            try {
                JSONObject item = top.getJSONObject(i);
                if (item == jsonObject) {
                    top.remove(i);
                    return true;
                }
                Boolean expand = a.containBoolean(item,MyApplication.EXPAND);
                JSONArray child = a.containJSONArray(item,MyApplication.CHILD);
                if ((expand != null)
                        && expand
                        && (child != null)) {
                    boolean flag = removeJSONObject(child,jsonObject);
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
        if (appCompatActivity == null) {
            return;
        }
        ListView listView = appCompatActivity.findViewById(R.id.list_view);
        if (listView == null) {
            return;
        }
        int position = listView.getSelectedItemPosition();
        //
        ArrayList<T> list = new ArrayList<>();
        if (this.getTop().length() == 0) {
            createDefaultTree();
        }
        updateJSONArray(list, 0, this.getTop(),null);
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
        JSONArray top = this.getTop();
        JSONObject prompt = new JSONObject();
        top.put(prompt);
        JSONObject uc = new JSONObject();
        top.put(uc);
        try {
            prompt.put(MyApplication.TEXT,MyApplication.TEXT_SEQUENCE);
            prompt.put(MyApplication.VALUES,"default");
            prompt.put(MyApplication.EXPAND,true);
            prompt.put(MyApplication.CHILD,new JSONArray());
            //
            uc.put(MyApplication.TEXT,MyApplication.TEXT_UC + MyApplication.TEXT_SEQUENCE);
            uc.put(MyApplication.VALUES,"default");
            uc.put(MyApplication.EXPAND,true);
            uc.put(MyApplication.CHILD,new JSONArray());
            //
        } catch (JSONException e) {
            // NONE
        }
    }

    /**
     * if top of tree is null then create default object
     * @param list list view data
     * @param level level of tree
     * @param target target object
     * @param unusedFlag if unused then true
     */
    @SuppressWarnings("unchecked")
    public void updateJSONArray(ArrayList<T> list, int level,Object target,Boolean unusedFlag) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        if (target instanceof JSONArray) {
            JSONArray array = (JSONArray) target;
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    updateJSONArray(list, level,obj,unusedFlag);
                } catch (JSONException e) {
                    // pass
                }
            }
        } else if (target instanceof JSONObject) {
            JSONObject item = (JSONObject) target;
            try {
                //
                item.put(MyApplication.LEVEL, level);
                //
                String text = a.containString(item,MyApplication.TEXT);
                if (text != null) {
                    boolean containUC = text.contains(MyApplication.TEXT_UC);
                    if (unusedFlag == null) {
                        unusedFlag = containUC;
                    }
                    if ((! unusedFlag) && containUC) {
                        text = text.replace(MyApplication.TEXT_UC, "");
                        item.put(MyApplication.TEXT,text);
                    } else if (unusedFlag && (! containUC)) {
                        text = MyApplication.TEXT_UC + text;
                        item.put(MyApplication.TEXT,text);
                    }
                }
                list.add((T) item);
                Boolean expand = a.containBoolean(item,MyApplication.EXPAND);
                if ((expand != null) && expand) {
                    JSONArray child = a.containJSONArray(item,MyApplication.CHILD);
                    if (child != null) {
                        updateJSONArray(list, level + 1, child, unusedFlag);
                    }
                }
            } catch (JSONException e) {
                // pass
            }
        }
    }
}
