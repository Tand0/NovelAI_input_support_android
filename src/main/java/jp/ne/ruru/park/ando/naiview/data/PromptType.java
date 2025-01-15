package jp.ne.ruru.park.ando.naiview.data;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import jp.ne.ruru.park.ando.naiview.R;

public enum PromptType {
    P_BASE_OK("prompt_type_base_ok"),
    P_BASE_NG("prompt_type_base_ng"),
    P_CH01_OK("prompt_type_ch1_ok"),
    P_CH01_NG("prompt_type_ch1_ng"),
    P_CH02_OK("prompt_type_ch2_ok"),
    P_CH02_NG("prompt_type_ch2_ng");


    private final String name;

    PromptType(String name) {
        this.name = name;
    }
    @Override
    @NonNull
    public String toString() {
        return name;
    }
    public String toStringJson() {
        switch (this) {
            case P_BASE_NG:
                return "u0";
            case P_CH01_OK:
                return "c1";
            case P_CH01_NG:
                return "u1";
            case P_CH02_OK:
                return "c2";
            case P_CH02_NG:
                return "u2";
            case P_BASE_OK:
            default:
        }
        return "c0";
    }
    public int getIdShort() {
        switch (this) {
            case P_BASE_NG:
                return R.string.prompt_type_base_ng;
            case P_CH01_OK:
                return R.string.prompt_type_ch1_ok;
            case P_CH01_NG:
                return R.string.prompt_type_ch1_ng;
            case P_CH02_OK:
                return R.string.prompt_type_ch2_ok;
            case P_CH02_NG:
                return R.string.prompt_type_ch2_ng;
            case P_BASE_OK:
            default:
        }
        return R.string.prompt_type_base_ok;
    }
    public int getIdLong() {
        switch (this) {
            case P_BASE_NG:
                return R.string.prompt_type_base_ng_long;
            case P_CH01_OK:
                return R.string.prompt_type_ch1_ok_long;
            case P_CH01_NG:
                return R.string.prompt_type_ch1_ng_long;
            case P_CH02_OK:
                return R.string.prompt_type_ch2_ok_long;
            case P_CH02_NG:
                return R.string.prompt_type_ch2_ng_long;
            case P_BASE_OK:
            default:
        }
        return R.string.prompt_type_base_ok_long;
    }
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        return key.equals(name);
    }
    public boolean isNegative() {
        switch (this) {
            case P_BASE_NG:
            case P_CH01_NG:
            case P_CH02_NG:
                return true;
            default:
                return false;
        }
    }
    static PromptType getType(JSONObject item) {
        String pType = Data.containString(item, Data.P_TYPE);
        String tType = Data.containString(item, Data.T_TYPE);
        if (pType == null) {
            if ((tType == null) || tType.contains("uc-")) {
                return P_BASE_NG;
            }
            return P_BASE_OK;
        }
        switch (pType) {
            case "u0":
                return P_BASE_NG;
            case "c1":
                return P_CH01_OK;
            case "u1":
                return P_CH01_NG;
            case "c2":
                return P_CH02_OK;
            case "u2":
                return P_CH02_NG;
            case "c0":
            default:
        }
        return P_BASE_OK;
    }
}
