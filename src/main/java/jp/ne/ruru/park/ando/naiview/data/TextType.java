package jp.ne.ruru.park.ando.naiview.data;

import androidx.annotation.NonNull;

import org.json.JSONObject;


public enum TextType {
    WORD("word"),
    SEQUENCE("Sequence"),
    SELECT("Select"),
    WEIGHT("Weight"),
    OTHER("Other");


    private final String name;

    TextType(String name) {
        this.name = name;
    }
    @Override
    @NonNull
    public String toString() {
        return name;
    }
    public String toStringJson() {
        return toString();
    }
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        return key.equals(name);
    }
    @NonNull
    static TextType getType(JSONObject item) {
        String key = Data.containString(item, Data.T_TYPE);
        return getTypeString(key);
    }
    public static TextType getTypeString(String key) {
        if (key == null) {
            return OTHER;
        }
        for (TextType s : TextType.values()) {
            if (key.contains(s.toString())) {
                return s;
            }
        }
        return OTHER;
    }
    public int getIndex() {
        int i = 0;
        for (TextType s : TextType.values()) {
            if (s.toString().equals(name)) {
                return i;
            }
            i++;
        }
        return 0;
    }
}
