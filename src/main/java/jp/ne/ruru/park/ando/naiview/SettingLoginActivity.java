package jp.ne.ruru.park.ando.naiview;

import android.os.Bundle;


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