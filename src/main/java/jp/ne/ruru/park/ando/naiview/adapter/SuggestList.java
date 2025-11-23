package jp.ne.ruru.park.ando.naiview.adapter;

import java.util.Locale;

/**
 * suggest list
 *
 * @author T.Ando
 */
public record SuggestList(String tag, int count, double confidence, String jpTag, int power) {
    public String toTextString() {
        String format;
        if (jpTag == null) {
            format = String.format(Locale.getDefault(), "%05d ", this.count);
            format += String.format(Locale.getDefault(), " %1$.4f  ", this.confidence);
        } else {
            format = String.format(Locale.getDefault(), "%05d  %s  ", this.power, this.jpTag);
        }
        return format;
    }
}
