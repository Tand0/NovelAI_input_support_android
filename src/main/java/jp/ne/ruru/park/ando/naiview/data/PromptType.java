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
        return switch (this) {
            case P_BASE_NG -> "u0";
            case P_CH01_OK -> "c1";
            case P_CH01_NG -> "u1";
            case P_CH02_OK -> "c2";
            case P_CH02_NG -> "u2";
            default -> "c0";
        };
    }
    public int getIdShort() {
        return switch (this) {
            case P_BASE_NG -> R.string.prompt_type_base_ng;
            case P_CH01_OK -> R.string.prompt_type_ch1_ok;
            case P_CH01_NG -> R.string.prompt_type_ch1_ng;
            case P_CH02_OK -> R.string.prompt_type_ch2_ok;
            case P_CH02_NG -> R.string.prompt_type_ch2_ng;
            default -> R.string.prompt_type_base_ok;
        };
    }
    public int getIdLong() {
        return switch (this) {
            case P_BASE_NG -> R.string.prompt_type_base_ng_long;
            case P_CH01_OK -> R.string.prompt_type_ch1_ok_long;
            case P_CH01_NG -> R.string.prompt_type_ch1_ng_long;
            case P_CH02_OK -> R.string.prompt_type_ch2_ok_long;
            case P_CH02_NG -> R.string.prompt_type_ch2_ng_long;
            default -> R.string.prompt_type_base_ok_long;
        };
    }
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        return key.equals(name);
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
        return switch (pType) {
            case "u0" -> P_BASE_NG;
            case "c1" -> P_CH01_OK;
            case "u1" -> P_CH01_NG;
            case "c2" -> P_CH02_OK;
            case "u2" -> P_CH02_NG;
            default -> P_BASE_OK;
        };
    }
}
