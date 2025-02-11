package jp.ne.ruru.park.ando.naiview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class DoubleSeekBarPreference extends Preference {
    private int mDefaultValue = 10;
    private int mMaxValue = 100;
    private int mMinValue = 0;
    private int mStep = 1;
    private int mDigit = 2;
    private String mTitle = null;
    private AppCompatSeekBar seekbar;
    private TextView textView;

    public DoubleSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        try (TypedArray ta = context.obtainStyledAttributes(
                attrs, R.styleable.Double_SeekBar_attrs, defStyleAttr, defStyleRes)) {
            mMinValue = ta.getInt(R.styleable.Double_SeekBar_attrs_np_minValue, mMinValue);
            mMaxValue = ta.getInt(R.styleable.Double_SeekBar_attrs_np_maxValue, mMaxValue);
            mDefaultValue = ta.getInt(R.styleable.Double_SeekBar_attrs_np_defaultValue, mDefaultValue);
            newValue = mDefaultValue;
            mStep = ta.getInt(R.styleable.Double_SeekBar_attrs_np_step, mStep);
            mDigit = ta.getInt(R.styleable.Double_SeekBar_attrs_np_digit, mDigit);
            mTitle = ta.getString(R.styleable.Double_SeekBar_attrs_np_title);
            //ta.recycle();
        }
        setLayoutResource(R.layout.double_picker_layout);
    }

    @Nullable
    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        mDefaultValue = a.getInt(index,mDefaultValue);
        return mDefaultValue;
    }
    private int newValue;
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        //
        newValue = getPersistedInt(mDefaultValue);
    }
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false);
        seekbar = (AppCompatSeekBar)holder.findViewById(R.id.seekbar);
        textView = (TextView)holder.findViewById(R.id.seekbar_value);
        TextView titleView = (TextView)holder.findViewById(R.id.seekbar_title);
        if ((seekbar == null) || (textView == null) || (titleView == null)) {
            return;
        }
        seekbar.setMax((mMaxValue - mMinValue) / mStep);
        seekbar.setProgress((newValue - mMinValue) / mStep);
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
    public void updateTextView() {
        if ((textView == null) || (seekbar == null)) {
            return;
        }
        double dValue = (seekbar.getProgress() * mStep) + mMinValue;
        dValue = dValue / Math.pow(10,mDigit);
        String format = "%."+mDigit+"f";
        String result = String.format(format, dValue);
        textView.setText(result);
    }

    SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateTextView();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            updateTextView();
            if (seekBar == null) {
                return;
            }
            int mValue = (seekBar.getProgress() * mStep) + mMinValue;
            persistInt(mValue);
        }
    };
}
