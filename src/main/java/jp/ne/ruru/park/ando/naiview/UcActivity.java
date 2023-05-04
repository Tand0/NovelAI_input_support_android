package jp.ne.ruru.park.ando.naiview;

/** uc activity
 * @author foobar@em.boo.jp
 */
public class UcActivity extends PromptActivity {

    /**
     * get title
     * @return title
     */
    @Override
    public String getMyTitle() {
        return this.getResources().getString(R.string.action_prompt);
    }

    /**
     * get uc
     * @return uc
     */
    @Override
    public String getText() {
        MyApplication a = (MyApplication) this.getApplication();
        return a.getUc();
    }

    /** set uc
     * @param text uc
     */
    @Override
    public void setText(String text) {
        MyApplication a = (MyApplication) this.getApplication();
        a.setUc(text);
    }

    /**
     * if prompt then true
     * @return always false
     */
    @Override
    public boolean isPrompt() {
        return false;
    }
}