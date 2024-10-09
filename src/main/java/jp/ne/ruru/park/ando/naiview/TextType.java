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
    @NonNull
    public String toString() {
        return name;
    }
}
