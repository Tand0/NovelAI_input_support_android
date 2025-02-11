package jp.ne.ruru.park.ando.naiview.miviewer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import jp.ne.ruru.park.ando.naiview.MyApplication;
import jp.ne.ruru.park.ando.naiview.R;

public class MISettingDialog extends DialogFragment {
    private MISettingFinishListener listener;
    private View customDialogView;
    public void setMISettingFinishListener(MISettingFinishListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        customDialogView = View.inflate(getContext(), R.layout.activity_mi_settings, null);
        onCreateCustomDialogView();
        String backText = this.getResources().getString(R.string.action_back);
        String movingText = this.getResources().getString(R.string.mi_moving);
        return new AlertDialog.Builder(requireActivity())
                .setView(customDialogView)
                .setPositiveButton(movingText, (dialog, id) -> {
                    if  (listener != null) listener.result(0);
                })
                .setNeutralButton(backText, (dialog, id) -> {
                    if (listener != null) listener.result(R.id.mi_finish);
                })
                .setCancelable(false)
                .create();
    }
    public void onCreateCustomDialogView() {
        //
        buttonSetting(R.id.mi_finish);
        buttonSetting(R.id.mi_reposition);
        //
        Activity activity = this.getActivity();
        if (activity == null) {
            return;
        }
        final MIViewerData settingData = ((MyApplication)activity.getApplication()).getMIViewerData();

        //
        buttonSetting(R.id.mi_learning_mode);
        onCreateSeekBar(R.id.mi_vibration_type,(progress) -> {
            settingData.setVibrationType(progress);
            customDialogView.findViewById(R.id.mi_learning_mode).setEnabled(progress == 0);
        });
        onCreateSeekBar(R.id.mi_vibration_speed, settingData::setVibrationSpeed);
        //
        SwitchMaterial seLoop = customDialogView.findViewById(R.id.mi_vibration_loop);
        seLoop.setChecked(settingData.getVibrationLoop());
        seLoop.setOnCheckedChangeListener(
                (buttonView,isChecked) -> settingData.setVibrationLoop(isChecked));
        //
        SwitchMaterial swFilter = customDialogView.findViewById(R.id.mi_color_filter_flag);
        swFilter.setOnCheckedChangeListener(
                (buttonView,isChecked) -> {
                    settingData.setColorFilterFlag(isChecked);
                    customDialogView.findViewById(R.id.mi_color_filter).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_color_filter_alpha).setEnabled(isChecked);
                });
        onCreateSeekBar(R.id.mi_color_filter,(progress) -> {
            settingData.setColorFilterProgress(progress);
            TextView tv = customDialogView.findViewById(R.id.mi_color_filter_text);
            tv.setTextColor(settingData.getColorFilterColor());
            tv.setBackgroundColor(settingData.getColorFilterColor() ^ 0x00FFFFFF);
        });
        onCreateSeekBar(R.id.mi_color_filter_alpha, settingData::setColorFilterAlpha);
        //
        buttonSetting(R.id.mi_box);
        //
        onCreateSeekBar(R.id.mi_ai_box_color,(progress) -> {
            settingData.setBoxColorProgress(progress);
            TextView tv = customDialogView.findViewById(R.id.mi_ai_box_text);
            tv.setTextColor(settingData.getBoxColorColor());
            tv.setBackgroundColor(settingData.getBoxColorColor() ^ 0x00FFFFFF);
        });
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_ai_mesh))
                .setOnCheckedChangeListener(
                        (buttonView,isChecked) -> settingData.setMeshFlag(isChecked));
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_ai_mesh_moving_flag))
                .setOnCheckedChangeListener((buttonView,isChecked) -> {
                    settingData.setMeshMovingFlag(isChecked);
                    customDialogView.findViewById(R.id.mi_ai_mesh_moving_len).setEnabled(isChecked);
                });
        onCreateSeekBar(R.id.mi_ai_mesh_moving_len, settingData::setMeshMovingProgress);
        //
        buttonSetting(R.id.mi_reset);
    }
    @Override
    public void onResume() {
        super.onResume();
        //
        Activity activity = this.getActivity();
        if (activity == null) {
            return;
        }
        final MIViewerData settingData = ((MyApplication)activity.getApplication()).getMIViewerData();
        //
        SwitchMaterial sw = customDialogView.findViewById(R.id.mi_vibration_loop);
        sw.setChecked(settingData.getVibrationLoop());
        //
        SeekBar sb = customDialogView.findViewById(R.id.mi_vibration_type);
        sb.setProgress(settingData.getVibrationType());
        customDialogView.findViewById(R.id.mi_learning_mode).setEnabled(settingData.getVibrationType() == 0);
        //
        sb = customDialogView.findViewById(R.id.mi_vibration_speed);
        sb.setProgress(settingData.getVibrationSpeed());
        //
        sb = customDialogView.findViewById(R.id.mi_ai_box_color);
        sb.setProgress(settingData.getBoxColorProgress());
        //
        TextView tv = customDialogView.findViewById(R.id.mi_ai_box_text);
        tv.setTextColor(settingData.getBoxColorColor());
        tv.setBackgroundColor(settingData.getBoxColorColor() ^ 0x00FFFFFF);
        //
        sw = customDialogView.findViewById(R.id.mi_color_filter_flag);
        sw.setChecked(settingData.getColorFilterFlag());
        //
        sw = customDialogView.findViewById(R.id.mi_ai_mesh);
        sw.setChecked(settingData.getMeshFlag());
        sw = customDialogView.findViewById(R.id.mi_ai_mesh_moving_flag);
        sw.setChecked(settingData.getMeshMovingFlag());
        sb = customDialogView.findViewById(R.id.mi_ai_mesh_moving_len);
        sb.setProgress(settingData.getMeshMovingProgress());
        sb.setEnabled(settingData.getMeshMovingFlag());
        //
        tv = customDialogView.findViewById(R.id.mi_color_filter_text);
        tv.setTextColor(settingData.getColorFilterColor());
        tv.setBackgroundColor(settingData.getColorFilterColor() ^ 0x00FFFFFF);
        //
        sb = customDialogView.findViewById(R.id.mi_color_filter);
        sb.setProgress(settingData.getColorFilterProgress());
        sb.setEnabled(settingData.getColorFilterFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_color_filter_alpha);
        sb.setProgress(settingData.getColorFilterAlpha());
        sb.setEnabled(settingData.getColorFilterFlag());
        //
    }
    protected void buttonSetting(final int id) {
        customDialogView.findViewById(id).setOnClickListener(v -> {
            if (listener != null) listener.result(id);
        });
    }
    public interface SeekBarRunnable {
        void run(int progress);
    }
    protected void onCreateSeekBar(int seekBarId, SeekBarRunnable r) {
        SeekBar sb = customDialogView.findViewById(seekBarId);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                r.run(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
