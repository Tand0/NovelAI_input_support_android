package jp.ne.ruru.park.ando.naiview.miviewer;

import android.graphics.Color;

import java.util.LinkedList;

public class MIViewerData {
    public MIViewerData() {
        this.reset();
    }
    public void reset() {
        boxColorProgress = 0;
        meshFlag = false;
        meshMovingFlag = false;
        meshMovingProgress = 50;
        vibrationType = 1;
        vibrationSpeed = 100;
        vibrationLoop = true;
        colorFilterFlag = false;
        colorFilterAlpha = 200;
    }
    private final int[] colorArray = {
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.BLUE,
            Color.CYAN,
            0xFF6B4A2B,
            0xffFFC0CB,
            0x88000000,
    };
    private int boxColorProgress;
    public int getBoxColorProgress() {
        return this.boxColorProgress;
    }
    public void setBoxColorProgress(int x) {
        this.boxColorProgress = x;
    }
    public int getBoxColorColor() {
        return colorArray[this.boxColorProgress % colorArray.length];
    }

    private boolean meshFlag;
    public boolean getMeshFlag() {
        return this.meshFlag;
    }
    public void setMeshFlag(boolean x) {
        this.meshFlag = x;
    }

    private boolean meshMovingFlag;
    public boolean getMeshMovingFlag() {
        return this.meshMovingFlag;
    }
    public void setMeshMovingFlag(boolean x) {
        this.meshMovingFlag = x;
    }

    private int meshMovingProgress;
    public int getMeshMovingProgress() {
        return this.meshMovingProgress;
    }
    public void setMeshMovingProgress(int x) {
        this.meshMovingProgress = x;
    }

    private int vibrationType;
    public int getVibrationType() {
        return this.vibrationType;
    }
    public void setVibrationType(int x) {
        this.vibrationType = x;
    }

    private boolean vibrationLoop;
    public boolean getVibrationLoop() {
        return this.vibrationLoop;
    }
    public void setVibrationLoop(boolean x) {
        this.vibrationLoop = x;
    }

    private int vibrationSpeed;
    public int getVibrationSpeed() {
        return this.vibrationSpeed;
    }
    public void setVibrationSpeed(int x) {
        this.vibrationSpeed = x;
    }

    private boolean colorFilterFlag;
    public boolean getColorFilterFlag() {
        return this.colorFilterFlag;
    }
    public void setColorFilterFlag(boolean x) {
        this.colorFilterFlag = x;
    }
    private int colorFilterProgress;
    public int getColorFilterProgress() {
        return this.colorFilterProgress;
    }
    public void setColorFilterProgress(int x) {
        this.colorFilterProgress = x;
    }
    public int getColorFilterColor() {
        return colorArray[this.colorFilterProgress % colorArray.length];
    }
    private int colorFilterAlpha;
    public void setColorFilterAlpha(int x) {
        this.colorFilterAlpha = x;
    }
    public int getColorFilterAlpha() {
        return this.colorFilterAlpha;
    }
    private final LinkedList<Plot> plotList = new LinkedList<>();
    public LinkedList<Plot> getPlotList() {
        return this.plotList;
    }
    private final LinkedList<Box> boxList = new LinkedList<>();
    public LinkedList<Box> getBoxList() {
        return this.boxList;
    }

}
