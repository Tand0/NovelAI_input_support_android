package jp.ne.ruru.park.ando.naiview;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.switchmaterial.SwitchMaterial;


public class SettingLoginActivity extends SettingAbstractActivity {
    @Override
    public int getLayoutName() {
        return R.layout.activity_setting_login;
    }
    @Override
    public int getMaterialToolbarName() {
        return R.id.setting_nai;
    }
    @Override
    public void onMyCreate(Bundle savedInstanceState) {
    }
    @Override
    public void onMyResume() {
    }
}