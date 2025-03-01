package jp.ne.ruru.park.ando.naiview.miviewer;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import jp.ne.ruru.park.ando.naiview.DoubleSeekBarView;
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
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_vibration_type))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) ->{
                    settingData.setVibrationType(value);
                    customDialogView.findViewById(R.id.mi_learning_mode).setEnabled(value == 0);
                });
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
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_color_filter))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) ->{
                    settingData.setColorFilterProgress(value);
                    TextView tv = seekBarView.getTitleView();
                    tv.setTextColor(settingData.getColorFilterColor());
                    tv.setBackgroundColor(settingData.getColorFilterColor() ^ 0x00FFFFFF);
                });
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_color_filter_alpha))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setColorFilterAlpha(value));
        //
        buttonSetting(R.id.mi_add_box);
        customDialogView.findViewById(R.id.mi_reset_box).setOnClickListener(v -> settingData.getBoxList().clear());
        //
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_vibration_speed))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setVibrationSpeed(value));
        //
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_box_color))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) ->{
                    settingData.setBoxColorProgress(value);
                    TextView tv = seekBarView.getTitleView();
                    tv.setTextColor(settingData.getBoxColorColor());
                    tv.setBackgroundColor(settingData.getBoxColorColor() ^ 0x00FFFFFF);
                });
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_mesh))
                .setOnCheckedChangeListener(
                        (buttonView,isChecked) -> settingData.setMeshFlag(isChecked));
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_mesh_moving_flag))
                .setOnCheckedChangeListener((buttonView,isChecked) -> {
                    settingData.setMeshMovingFlag(isChecked);
                    customDialogView.findViewById(R.id.mi_mesh_moving_len).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_mesh_moving_flexibility).setEnabled(isChecked);
                });
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_mesh_moving_len))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setMeshMovingProgress(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_mesh_moving_flexibility))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setMeshMovingFlexibility(value));
        //
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_concentrated_flag))
                .setOnCheckedChangeListener((buttonView,isChecked) -> {
                    settingData.setConcentratedFlag(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_state).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_count).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_random_angle).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_random_line).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_line_len).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_wide).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_color).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_concentrated_alpha).setEnabled(isChecked);
                });
        buttonSetting(R.id.mi_concentrated_state);
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_count))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedCount(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_random_angle))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedRandomAngle(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_random_line))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedRandomLine(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_line_len))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedLineLen(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_wide))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedWide(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_color))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) ->{
                    settingData.setConcentratedColor(value);
                    TextView tv = seekBarView.getTitleView();
                    tv.setTextColor(settingData.getConcentratedColorColor());
                    tv.setBackgroundColor(settingData.getConcentratedColorColor() ^ 0x00FFFFFF);
                });
        //
        ((SwitchMaterial)customDialogView.findViewById(R.id.mi_sparkling_flag))
                .setOnCheckedChangeListener((buttonView,isChecked) -> {
                    settingData.setSparklingFlag(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_plot).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_plot_reset).setEnabled(isChecked
                            & (!settingData.getSparkList().isEmpty()));
                    customDialogView.findViewById(R.id.mi_sparkling_color).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_alpha).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_count).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_len).setEnabled(isChecked);
                    customDialogView.findViewById(R.id.mi_sparkling_random_len).setEnabled(isChecked);
                });
        buttonSetting(R.id.mi_sparkling_plot);
        customDialogView.findViewById(R.id.mi_sparkling_plot_reset).setOnClickListener(v -> settingData.getSparkList().clear());
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_sparkling_color))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> {
                    settingData.setSparklingColor(value);
                    TextView tv = seekBarView.getTitleView();
                    tv.setTextColor(settingData.getSparklingColorColor());
                    tv.setBackgroundColor(settingData.getSparklingColorColor() ^ 0x00FFFFFF);
                });
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_sparkling_alpha))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setSparklingAlpha(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_sparkling_count))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setSparklingCount(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_sparkling_len))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setSparklingLen(value));
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_sparkling_random_len))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setSparklingRandom(value));
        //
        ((DoubleSeekBarView)customDialogView.findViewById(R.id.mi_concentrated_alpha))
                .setOnSeekBarChangeListener((seekBarView,value,fromUser) -> settingData.setConcentratedAlpha(value));
        //
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
        DoubleSeekBarView sb = customDialogView.findViewById(R.id.mi_vibration_type);
        sb.setValue(settingData.getVibrationType());
        customDialogView.findViewById(R.id.mi_learning_mode).setEnabled(settingData.getVibrationType() == 0);
        //
        sb = customDialogView.findViewById(R.id.mi_vibration_speed);
        sb.setValue(settingData.getVibrationSpeed());
        //
        sb = customDialogView.findViewById(R.id.mi_box_color);
        sb.setValue(settingData.getBoxColorProgress());
        sb.getTitleView().setTextColor(settingData.getBoxColorColor());
        sb.getTitleView().setBackgroundColor(settingData.getBoxColorColor() ^ 0x00FFFFFF);
        //
        sw = customDialogView.findViewById(R.id.mi_color_filter_flag);
        sw.setChecked(settingData.getColorFilterFlag());
        //
        sw = customDialogView.findViewById(R.id.mi_mesh);
        sw.setChecked(settingData.getMeshFlag());
        sw = customDialogView.findViewById(R.id.mi_mesh_moving_flag);
        sw.setChecked(settingData.getMeshMovingFlag());
        sb = customDialogView.findViewById(R.id.mi_mesh_moving_len);
        sb.setValue(settingData.getMeshMovingProgress());
        sb.setEnabled(settingData.getMeshMovingFlag());
        sb = customDialogView.findViewById(R.id.mi_mesh_moving_flexibility);
        sb.setValue(settingData.getMeshMovingFlexibility());
        sb.setEnabled(settingData.getMeshMovingFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_color_filter);
        sb.setValue(settingData.getColorFilterProgress());
        sb.setEnabled(settingData.getColorFilterFlag());
        sb.getTitleView().setTextColor(settingData.getColorFilterColor());
        sb.getTitleView().setBackgroundColor(settingData.getColorFilterColor() ^ 0x00FFFFFF);
        //
        sb = customDialogView.findViewById(R.id.mi_color_filter_alpha);
        sb.setValue(settingData.getColorFilterAlpha());
        sb.setEnabled(settingData.getColorFilterFlag());
        //
        //
        sw = customDialogView.findViewById(R.id.mi_concentrated_flag);
        sw.setChecked(settingData.getConcentratedFlag());
        //
        customDialogView.findViewById(R.id.mi_concentrated_state).setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_count);
        sb.setValue(settingData.getConcentratedCount());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_random_angle);
        sb.setValue(settingData.getConcentratedRandomAngle());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_random_line);
        sb.setValue(settingData.getConcentratedRandomLine());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_line_len);
        sb.setValue((int)settingData.getConcentratedLineLen());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_wide);
        sb.setValue(settingData.getConcentratedWide());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_color);
        sb.setValue(settingData.getConcentratedColor());
        sb.setEnabled(settingData.getConcentratedFlag());
        sb.getTitleView().setTextColor(settingData.getConcentratedColorColor());
        sb.getTitleView().setBackgroundColor(settingData.getConcentratedColorColor() ^ 0x00FFFFFF);
        //
        sb = customDialogView.findViewById(R.id.mi_concentrated_alpha);
        sb.setValue(settingData.getConcentratedAlpha());
        sb.setEnabled(settingData.getConcentratedFlag());
        //
        //
        sw = customDialogView.findViewById(R.id.mi_sparkling_flag);
        sw.setChecked(settingData.getSparklingFlag());
        //
        customDialogView.findViewById(R.id.mi_sparkling_plot).setEnabled(settingData.getSparklingFlag());
        customDialogView.findViewById(R.id.mi_sparkling_plot_reset).setEnabled(
                settingData.getSparklingFlag() & (!settingData.getSparkList().isEmpty()));
        //
        sb = customDialogView.findViewById(R.id.mi_sparkling_color);
        sb.setValue(settingData.getSparklingColor());
        sb.setEnabled(settingData.getSparklingFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_sparkling_alpha);
        sb.setValue(settingData.getSparklingAlpha());
        sb.setEnabled(settingData.getSparklingFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_sparkling_count);
        sb.setValue(settingData.getSparklingCount());
        sb.setEnabled(settingData.getSparklingFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_sparkling_len);
        sb.setValue(settingData.getSparklingLen());
        sb.setEnabled(settingData.getSparklingFlag());
        //
        sb = customDialogView.findViewById(R.id.mi_sparkling_random_len);
        sb.setValue(settingData.getSparklingRandom());
        sb.setEnabled(settingData.getSparklingFlag());
    }
    protected void buttonSetting(final int id) {
        customDialogView.findViewById(id).setOnClickListener(v -> {
            if (listener != null) listener.result(id);
        });
    }
}
