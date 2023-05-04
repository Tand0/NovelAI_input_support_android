package jp.ne.ruru.park.ando.naiview;

public class UcActivity extends PromptActivity {
    @Override
    public String getText() {
        MyApplication a = (MyApplication) this.getApplication();
        return a.getUc();
    }
    @Override
    public void setText(String text) {
        MyApplication a = (MyApplication) this.getApplication();
        a.setUc(text);
    }
    @Override
    public boolean isPrompt() {
        return false;
    }
}