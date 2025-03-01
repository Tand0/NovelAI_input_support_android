package jp.ne.ruru.park.ando.naiview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSeekBar;
public class DoubleSeekBarView extends android.widget.FrameLayout {
    private int mDefaultValue = 10;
    private int mMaxValue = 100;
    private int mMinValue = 0;
    private int mStep = 1;
    private int mDigit = 2;
    private AppCompatSeekBar seekbar;
    private TextView textView;
    private TextView titleView;

    public DoubleSeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }
    public DoubleSeekBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        String mTitle;
        try (TypedArray ta = context.obtainStyledAttributes(
                attrs, R.styleable.DoubleSeekBarView, defStyleAttr, defStyleRes)) {
            mMinValue = ta.getInt(R.styleable.DoubleSeekBarView_np_minValue, mMinValue);
            mMaxValue = ta.getInt(R.styleable.DoubleSeekBarView_np_maxValue, mMaxValue);
            mDefaultValue = ta.getInt(R.styleable.DoubleSeekBarView_np_defaultValue, mDefaultValue);
            mStep = ta.getInt(R.styleable.DoubleSeekBarView_np_step, mStep);
            mDigit = ta.getInt(R.styleable.DoubleSeekBarView_np_digit, mDigit);
            mTitle = ta.getString(R.styleable.DoubleSeekBarView_np_title);
            //ta.recycle();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.double_seekbar_views, this, true);
        //
        seekbar = findViewById(R.id.seekbar);
        textView = findViewById(R.id.seekbar_value);
        titleView = findViewById(R.id.seekbar_title);
        if ((seekbar == null) || (textView == null) || (titleView == null)) {
            return;
        }
        seekbar.setMax((mMaxValue - mMinValue) / mStep);
        seekbar.setProgress((mDefaultValue - mMinValue) / mStep);
        seekbar.setEnabled(this.isEnabled());
        seekbar.setOnSeekBarChangeListener(listener);
        if (mTitle == null) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(mTitle);
            titleView.setVisibility(View.VISIBLE);
        }
        updateTextView();
    }
    private void updateTextView() {
        String result = getDisplayValue();
        textView.setText(result);
    }
    public String getDisplayValue() {
        if ((textView == null) || (seekbar == null)) {
            return "**";
        }
        double dValue = (seekbar.getProgress() * mStep) + mMinValue;
        String result;
        if (0 < mDigit) {
            dValue = dValue / Math.pow(10, mDigit);
            String format = "%." + mDigit + "f";
            result = String.format(format, dValue);
        } else {
            result = "" + (int) dValue;
        }
        return result;
    }
    public TextView getTitleView() {
        return this.titleView;
    }
    public int getValue() {
        return getValue(seekbar.getProgress());
    }
    public int getValue(int progress) {
        return (progress * mStep) + mMinValue;
    }
    public void setValue(int progress) {
        seekbar.setProgress((progress - mMinValue) / mStep);
        updateTextView();
    }
    protected SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateTextView();
            if (seekBarChangeListener != null) {
                seekBarChangeListener.run(DoubleSeekBarView.this,getValue(progress),fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            updateTextView();
        }
    };
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        seekbar.setEnabled(enabled);
    }
    public interface DoubleSeekBarListener {
        void run(DoubleSeekBarView sb, int value, boolean fromUser);
    }
    private DoubleSeekBarListener seekBarChangeListener = null;
    public void setOnSeekBarChangeListener(DoubleSeekBarListener x) {
        this.seekBarChangeListener = x;
    }
}
