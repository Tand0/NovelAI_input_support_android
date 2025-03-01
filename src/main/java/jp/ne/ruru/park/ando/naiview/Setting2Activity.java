package jp.ne.ruru.park.ando.naiview;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;


public class Setting2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting2);
        MaterialToolbar toolbar = this.findViewById(R.id.setting_nai);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Spinner spinner = this.findViewById(R.id.prompt_model);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(sListener);
        }
        SwitchCompat sc = findViewById(R.id.setting_i2i);
        if (sc != null) {
            sc.setOnCheckedChangeListener((buttonView,check) ->changeSc(check));
        }
    }
    public AdapterView.OnItemSelectedListener sListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner)parent;
            if (position < 0) {
                return;
            }
            Object item = spinner.getAdapter().getItem(position);
            if (item == null) {
                return;
            }
            changeSm(item);
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    public void changeSm(Object newValue) {
        SwitchMaterial sm = this.findViewById(R.id.prompt_sm);
        SwitchMaterial smDyn = this.findViewById(R.id.prompt_sm_dyn);
        if ((sm != null) && (smDyn != null)) {
            boolean flag = (newValue == null) || (! (newValue.toString().contains("4")));
            sm.setEnabled(flag);
            smDyn.setEnabled(flag);
            this.findViewById(R.id.prompt_int_location_ch1_x).setEnabled(! flag);
            this.findViewById(R.id.prompt_int_location_ch1_y).setEnabled(! flag);
            this.findViewById(R.id.prompt_int_location_ch2_x).setEnabled(! flag);
            this.findViewById(R.id.prompt_int_location_ch2_y).setEnabled(! flag);
        }
    }
    public void changeSc(boolean checked) {
        findViewById(R.id.prompt_int_strength).setEnabled(checked);
        findViewById(R.id.prompt_int_noise).setEnabled(checked);
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
        Spinner spinner = this.findViewById(R.id.prompt_model);
        if (spinner != null) {
            int position = spinner.getSelectedItemPosition();
            if (0 <= position) {
                Object base = spinner.getAdapter().getItem(position);
                changeSm(base);
            }
        }
        SwitchCompat sc = findViewById(R.id.setting_i2i);
        changeSc(sc.isChecked());
    }
    public void load(MyApplication a, SharedPreferences preferences, View view) {
        if (view instanceof jp.ne.ruru.park.ando.naiview.DoubleSeekBarView) {
            jp.ne.ruru.park.ando.naiview.DoubleSeekBarView sb = (jp.ne.ruru.park.ando.naiview.DoubleSeekBarView) view;
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            int progress = sb.getValue();
            try {
                progress = preferences.getInt(idString, progress);
                sb.setValue(progress);
                a.appendLog(this, "load:" + idString + ": " + sb.getDisplayValue());
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof EditText) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            EditText editText = (EditText)view;
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
        } else if (view instanceof SwitchCompat) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            SwitchCompat sm = (SwitchCompat)view;
            boolean checked = sm.isChecked();
            try {
                checked = preferences.getBoolean(idString, checked);
                sm.setChecked(checked);
                a.appendLog(this, "load:" + idString + ": " + checked);
            } catch(ClassCastException e) {
                a.appendLog(this, e.getMessage());
            }
        } else if (view instanceof Spinner) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            Spinner sm = (Spinner)view;
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
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
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
        if (view instanceof jp.ne.ruru.park.ando.naiview.DoubleSeekBarView) {
            jp.ne.ruru.park.ando.naiview.DoubleSeekBarView sb = (jp.ne.ruru.park.ando.naiview.DoubleSeekBarView) view;
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            int progress = sb.getValue();
            editor.putInt(idString, progress);
            a.appendLog(this, "save:" + idString + ": " + progress + " : " + sb.getDisplayValue());
        } else if (view instanceof EditText) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            EditText editText = (EditText)view;
            int inputType = editText.getInputType();
            String text = editText.getText().toString();
            editor.putString(idString, text);
            if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                text = "***";
            }
            a.appendLog(this, "save:" + idString + ": " + text);
        } else if (view instanceof SwitchCompat) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            SwitchCompat sm = (SwitchCompat)view;
            boolean checked = sm.isChecked();
            editor.putBoolean(idString, checked);
            a.appendLog(this, "save:" + idString + ": " + checked);
        } else if (view instanceof Spinner) {
            String idString = getIdString(a, view);
            if (idString == null) {
                return;
            }
            Spinner sm = (Spinner)view;
            Object item = sm.getSelectedItem();
            if (item == null) {
                return;
            }
            editor.putString(idString, item.toString());
            a.appendLog(this, "save:" + idString + ": " + item);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
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