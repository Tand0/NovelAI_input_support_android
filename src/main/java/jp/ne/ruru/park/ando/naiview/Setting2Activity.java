package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
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
        sc = findViewById(R.id.character_reference_image);
        if (sc != null) {
            sc.setOnCheckedChangeListener((buttonView,check) ->changeCharacterReferenceImage(check));
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
        //
        sc = findViewById(R.id.character_reference_image);
        changeCharacterReferenceImage(sc.isChecked());
    }
    public void changeSc(boolean checked) {
        findViewById(R.id.prompt_int_strength).setEnabled(checked);
        findViewById(R.id.prompt_int_noise).setEnabled(checked);
        // onのときcriは打てない
        findViewById(R.id.character_reference_image).setEnabled(! checked);
    }
    public void changeCharacterReferenceImage(boolean checked) {
        findViewById(R.id.style_aware).setEnabled(checked);
        findViewById(R.id.character_reference_image_fidelity).setEnabled(checked);
        // onのときi2iは打てない
        findViewById(R.id.setting_i2i).setEnabled(! checked);
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