package jp.ne.ruru.park.ando.naiview;

import androidx.annotation.NonNull;

public enum TextType {
    TEXT_WORD("word"),
    TEXT_SEQUENCE("Sequence"),
    TEXT_SELECT("Select"),
    TEXT_WEIGHT("Weight"),
    TEXT_OTHER("Other");

    private final String name;

    TextType(String name) {
        this.name = name;
    }
    @Override
    @NonNull
    public String toString() {
        return name;
    }
    public boolean contains(String key) {
        if (key == null) {
            return false;
        }
        return key.equals(name);
    }
    @NonNull
    public static TextType getTextType(String key) {
        if (key == null) {
            return TEXT_OTHER;
        }
        for (TextType s : TextType.values()) {
            if (key.contains(s.toString())) {
                return s;
            }
        }
        return TEXT_OTHER;
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
