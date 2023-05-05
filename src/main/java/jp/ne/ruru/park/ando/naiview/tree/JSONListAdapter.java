package jp.ne.ruru.park.ando.naiview.tree;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.R;

/**
 * json list adapter
 * @author foobar@em.boo.jp
 * @param <T> for item
 */
public class JSONListAdapter<T extends JSONObject> extends ArrayAdapter<T> {

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
        boolean expand = false;
        StringBuilder value = new StringBuilder();
        boolean isNotWord = true;
        boolean isUc = true;
        boolean isIgnore = true;
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        if (item != null) {
            Boolean expandBoolean = a.containBoolean(item,MyApplication.EXPAND);
            if (expandBoolean != null) {
                expand = expandBoolean;
            }
            value.append(String.format(Locale.getDefault(),"(%04d)", position));
            Integer level = a.containInt(item,MyApplication.LEVEL);
            if (level != null) {
                for (int i = 0 ; i < level ; i++) {
                    value.append(" | ");
                }
            }
            String text = a.containString(item,MyApplication.TEXT);
            if (text != null) {
                if (text.contains(MyApplication.TEXT_WORD)) {
                    isNotWord = false;
                } else {
                    value.append("+(").append(text).append(") ");
                }
                isUc = text.contains(MyApplication.TEXT_UC);
                isIgnore = text.contains(MyApplication.TEXT_IGNORE);
            }
            String values = a.containString(item,MyApplication.VALUES);
            if (values != null) {
                value.append(values);
            }
        }
        Button button = view.findViewById(R.id.button_cheese);
        button.setText(String.format(Locale.ENGLISH,"%d",position));
        button.setOnClickListener(this::executeButton);
        CheckBox checkBox = view.findViewById(R.id.checkbox_cheese);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(expand);
        checkBox.setOnCheckedChangeListener((buttonView,isChecked)->expand());
        checkBox.setEnabled(isNotWord);
        //

        if (! isIgnore) {
            checkBox.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            checkBox.setBackgroundColor(Color.parseColor("#F0F0F0"));
        }
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
        checkBox.setText(value.toString());
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
            ListView listView = ((AppCompatActivity) this.getContext()).findViewById(R.id.list_view);
            listView.setSelection(position);
            return executePopup(item.getItemId(),position);
        });
    }

    /**
     * press button
     * @param itemId item id
     * @param position position of list view
     * @return if execution then true
     */
    protected boolean executePopup(int itemId,int position) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        String textRaw;
        final JSONObject item;
        if (0 <= position) {
            item  = this.getItem(position);
            if (item != null) {
                textRaw = a.containString(item,MyApplication.VALUES);
                if (textRaw == null) {
                    textRaw = "";
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
            addText( item, text, position);
            return true;
        } else if (itemId == R.id.menu_tree) {
            addTree(position);
            return true;
        } else if (itemId == R.id.menu_transform) {
            transformTree(item);
            return true;
        }
        return false;
    }

    /**
     * insert or change text to json object
     * @param item json object
     * @param text insert or change text
     * @param position position
     */
    public void addText(JSONObject item,String text,int position) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        final View dialog_view = inflater.inflate(R.layout.input_daialog, null);
        EditText editText = dialog_view.findViewById(R.id.word_edit_text);
        editText.setText(text);
        new AlertDialog.Builder(this.getContext())
                .setView(dialog_view)
                .setTitle(R.string.input_word)
                .setPositiveButton("Insert",(dialog,which)-> {
                    String res = editText.getText().toString();
                    JSONObject target = new JSONObject();
                    try {
                        target.put(MyApplication.TEXT,MyApplication.TEXT_WORD);
                        target.put(MyApplication.VALUES,res);
                        target.put(MyApplication.CHILD,new JSONArray());
                        a.setCut(target);
                        this.pastJSONObject(position);
                    } catch (JSONException e) {
                        // NONE
                    }
                })
                .setNegativeButton("Change",(dialog,which)-> {
                    String res = editText.getText().toString();
                    if (!res.equals(text)) {
                        if (item != null) {
                            try {
                                item.put(MyApplication.VALUES, res);
                            } catch (JSONException e) {
                                // NONE
                            }
                            this.updateJSONArray();
                        }
                    }
                })
                .setNeutralButton("Cancel",(dialog,which)-> {})
                .show();
    }

    /**
     * add tree
     * @param position position
     */
    public void addTree(int position) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        final String[] items = {
                this.getContext().getResources().getString(R.string.menu_sequence),
                this.getContext().getResources().getString(R.string.menu_select),
                this.getContext().getResources().getString(R.string.menu_weight)
        };
        new AlertDialog.Builder(this.getContext())
                .setTitle(R.string.menu_tree)
                .setItems(items, (dialog,which)-> {
                    final String[] ss = {
                            MyApplication.TEXT_SEQUENCE,
                            MyApplication.TEXT_SELECT,
                            MyApplication.TEXT_WEIGHT};
                    JSONObject target = new JSONObject();
                    try {
                        target.put(MyApplication.TEXT,ss[which]);
                        target.put(MyApplication.VALUES,"");
                        target.put(MyApplication.CHILD,new JSONArray());
                        a.setCut(target);
                        this.pastJSONObject(position);
                    } catch (JSONException e) {
                        // NONE
                    }
                })
                .show();
    }

    /**
     * change item
     * @param item item
     */
    public void transformTree(JSONObject item) {
        if (item == null) {
            return;
        }
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        final String[] items = {
                this.getContext().getResources().getString(R.string.menu_ignore),
                this.getContext().getResources().getString(R.string.menu_not_ignore),
                this.getContext().getResources().getString(R.string.menu_enhance),
                this.getContext().getResources().getString(R.string.menu_not_enhance),
        };
        new AlertDialog.Builder(this.getContext())
                .setTitle(R.string.menu_transform)
                .setItems(items, (dialog,which)-> {
                    String text = a.containString(item,MyApplication.TEXT);
                    if (text == null) {
                        text = "";
                    }
                    String values = a.containString(item,MyApplication.VALUES);
                    if (values == null) {
                        values = "";
                    }
                    switch (which) {
                        case 0:
                        default:
                            if (! text.contains(MyApplication.TEXT_IGNORE)) {
                                text = MyApplication.TEXT_IGNORE + text;
                            }
                            break;
                        case 1:
                            if (text.contains(MyApplication.TEXT_IGNORE)) {
                                text = text.replace(MyApplication.TEXT_IGNORE,"");
                            }
                            break;
                        case 2:
                            int index = a.getEnhancePos(values) + 1;
                            values = a.getEnhanceText(values, index);
                            break;
                        case 3:
                            index = a.getEnhancePos(values) - 1;
                            values = a.getEnhanceText(values, index);
                            break;
                    }
                    try {
                        item.put(MyApplication.TEXT,text);
                        item.put(MyApplication.VALUES,values);
                    } catch (JSONException e) {
                        // NONE
                    }
                    this.updateJSONArray();
                })
                .show();

    }

    /**
     * get top of tree
     * @return top of tree
     */
    public JSONArray getTop() {
        MyApplication application =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        return application.getTop();
    }
    public void expand() {
        ListView listView = ((AppCompatActivity)this.getContext()).findViewById(R.id.list_view);
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        int count = listView.getCount();
        for (int position = 0; position < count; position++) {
            CheckBox checkBox = listView.getChildAt(position).findViewById(R.id.checkbox_cheese);
            boolean isChecked = checkBox.isChecked();
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
                    break;
                } catch (JSONException e) {
                    // NONE
                }
            }
        }
    }

    /**
     * past tree
     * @param position past position
     * @return if changed then true
     */
    public boolean pastJSONObject(int position) {
        MyApplication a =
                ((MyApplication)((AppCompatActivity) this.getContext()).getApplication());
        JSONObject cut = a.getCut();
        if (cut == null) {
            return false;
        }
        if (position < 0) {
            getTop().put(cut);
            a.setCut(null);
            return false;
        }
        JSONArray top = this.getTop();
        JSONObject item = this.getItem(position);
        String text = a.containString(item,MyApplication.TEXT);
        if (text == null) {
            return false;
        }
        boolean flag;
        try {
            if (text.contains(MyApplication.TEXT_WORD)) {
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
        ListView listView = ((AppCompatActivity)this.getContext()).findViewById(R.id.list_view);
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
            prompt.put(MyApplication.CHILD,new JSONArray());
            //
            uc.put(MyApplication.TEXT,MyApplication.TEXT_UC + MyApplication.TEXT_SEQUENCE);
            uc.put(MyApplication.VALUES,"default");
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
