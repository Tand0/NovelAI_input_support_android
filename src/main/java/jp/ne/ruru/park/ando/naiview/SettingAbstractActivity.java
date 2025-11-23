package jp.ne.ruru.park.ando.naiview;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;


public abstract class SettingAbstractActivity extends AppCompatActivity {
    public abstract int getLayoutName();
    public abstract int getMaterialToolbarName();
    public abstract void onMyCreate(Bundle savedInstanceState);
    public abstract void onMyResume();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        EdgeToEdge.enable(this);
        setContentView(getLayoutName());
        MaterialToolbar toolbar = this.findViewById(getMaterialToolbarName());
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        //
        //
        onMyCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        }
        return  super.onOptionsItemSelected(menuButton);
    }
    @Override
    public void onResume() {
        super.onResume();
        //
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        MyApplication a =  (MyApplication) this.getApplication();
        load(a, preferences, viewGroup);
        //
        //
        onMyResume();
    }
    public void load(MyApplication a, SharedPreferences preferences, View view) {
        if (view instanceof DoubleSeekBarView seekBarView) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            int progress = seekBarView.getValue();
            try {
                progress = preferences.getInt(idString, progress);
                seekBarView.setValue(progress);
                a.appendLog(this, "load:" + idString + ": " + progress + " : " + seekBarView.getDisplayValue());
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof EditText editText) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            String text = editText.getText().toString();
            int inputType = editText.getInputType();
            try {
                text = preferences.getString(idString, text);
                editText.setText(text);
                if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                    text = "***";
                }
                a.appendLog(this, "load:" + idString + ": " + text);
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof SwitchCompat sm) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            boolean checked = sm.isChecked();
            try {
                checked = preferences.getBoolean(idString, checked);
                sm.setChecked(checked);
                a.appendLog(this, "load:" + idString + ": " + checked);
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof Spinner sm) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            Object item = sm.getSelectedItem();
            SpinnerAdapter sa = sm.getAdapter();
            if ((item == null) || (sa == null)) {
                return;
            }
            String preferencesText = preferences.getString(idString, item.toString());
            try {
                for (int i = 0 ; i < sa.getCount() ; i++) {
                    Object target = sa.getItem(i);
                    if (preferencesText.equals(target.toString())) {
                        sm.setSelection(i);
                        a.appendLog(this, "load:" + idString + ": " + preferencesText);
                        break;
                    }
                }
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof ViewGroup vg) {
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                load(a, preferences, vg.getChildAt(i));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        MyApplication a =  (MyApplication) this.getApplication();
        save(a, editor, viewGroup);
        editor.apply();
    }
    public void save(MyApplication a,SharedPreferences.Editor editor, View view) {
        if (view instanceof DoubleSeekBarView sb) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            int progress = sb.getValue();
            editor.putInt(idString, progress);
            a.appendLog(this, "save:" + idString + ": " + progress + " : " + sb.getDisplayValue());
        } else if (view instanceof EditText editText) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            int inputType = editText.getInputType();
            String text = editText.getText().toString();
            editor.putString(idString, text);
            if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                text = "***";
            }
            a.appendLog(this, "save:" + idString + ": " + text);
        } else if (view instanceof SwitchCompat sm) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            boolean checked = sm.isChecked();
            editor.putBoolean(idString, checked);
            a.appendLog(this, "save:" + idString + ": " + checked);
        } else if (view instanceof Spinner sm) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            Object item = sm.getSelectedItem();
            if (item == null) {
                return;
            }
            editor.putString(idString, item.toString());
            a.appendLog(this, "save:" + idString + ": " + item);
        } else if (view instanceof ViewGroup vg) {
            for (int i = 0 ; i < vg.getChildCount() ; i++) {
                save(a,editor, vg.getChildAt(i));
            }
        }
    }
    private String getIdString(MyApplication a, View view) {
        String idString = null;
        try {
            idString = view.getResources().getResourceName(view.getId());
            int index = idString.indexOf('/');
            if (0 <= index) {
                idString = idString.substring(index + 1);
            }
        } catch(android.content.res.Resources.NotFoundException e) {
            a.appendLog(this, e.getMessage());
        }
        return idString;
    }
}