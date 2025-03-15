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


public class Setting2Activity extends SettingAbstractActivity {
    @Override
    public int getLayoutName() {
        return R.layout.activity_setting2;
    }
    @Override
    public int getMaterialToolbarName() {
        return R.id.setting_nai;
    }
    @Override
    public void onMyCreate(Bundle savedInstanceState) {
        Spinner spinner = this.findViewById(R.id.prompt_model);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(sListener);
        }
        SwitchCompat sc = findViewById(R.id.setting_i2i);
        if (sc != null) {
            sc.setOnCheckedChangeListener((buttonView,check) ->changeSc(check));
        }
    }
    @Override
    public void onMyResume() {
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
    public void changeSc(boolean checked) {
        findViewById(R.id.prompt_int_strength).setEnabled(checked);
        findViewById(R.id.prompt_int_noise).setEnabled(checked);
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
}