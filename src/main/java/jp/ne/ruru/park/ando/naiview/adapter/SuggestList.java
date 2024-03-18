package jp.ne.ruru.park.ando.naiview.adapter;

/**
 * suggest list
 * @author T.Ando
 */
public class SuggestList {
    public SuggestList(String tag,int count,double confidence) {
        this.tag = tag;
        this.count = count;
        this.confidence = confidence;
    }
    public final String tag;
    public final int count;
    public final double confidence;

}
