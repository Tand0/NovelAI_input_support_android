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
        meshMovingFlexibility = 50;
        vibrationType = 1;
        vibrationSpeed = 100;
        vibrationLoop = true;
        colorFilterFlag = false;
        colorFilterProgress = 1;
        colorFilterAlpha = 40;
        //
        concentratedFlag = true;
        concentratedColor = 1;
        concentratedAlpha = 110;
        concentratedCount = 10;
        concentratedRandomAngle = 20;
        concentratedRandomLine = 150;
        concentratedLineLen = 400;
        concentratedWide = 5;
        //
        sparklingFlag = false;
        sparklingColor = 1;
        sparklingAlpha = 50;
        sparklingCount = 5;
        sparklingLen = 2;
        sparklingRandom = 20;
        //
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
    private int meshMovingFlexibility;
    public int getMeshMovingFlexibility() {
        return this.meshMovingFlexibility;
    }
    public void setMeshMovingFlexibility(int x) {
        this.meshMovingFlexibility = x;
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

    private final PlotIndex plotIndex = new PlotIndex();
    public PlotIndex getPlot() {
        return this.plotIndex;
    }
    private final LinkedList<PlotIndex> sparkList = new LinkedList<>();
    public LinkedList<PlotIndex> getSparkList() {
        return this.sparkList;
    }
    private final LinkedList<Box> boxList = new LinkedList<>();
    public LinkedList<Box> getBoxList() {
        return this.boxList;
    }
    public final float[] baseFloat = new float[9];
    public final float[] baseMovingFloat = new float[9];

    private float concentratedX;
    public float getConcentratedX() {
        return this.concentratedX;
    }
    public void setConcentratedX(float concentratedX) {
        this.concentratedX = concentratedX;
    }
    private float concentratedY;
    public float getConcentratedY() {
        return this.concentratedY;
    }
    public void setConcentratedY(float concentratedY) {
        this.concentratedY = concentratedY;
    }

    private boolean concentratedFlag;
    public boolean getConcentratedFlag() {
        return this.concentratedFlag;
    }
    public void setConcentratedFlag(boolean concentratedFlag) {
        this.concentratedFlag = concentratedFlag;
    }

    private int concentratedRandomAngle; // 1..20
    public int getConcentratedRandomAngle() {
        return this.concentratedRandomAngle;
    }
    public void setConcentratedRandomAngle(int concentratedRandomAngle) {
        this.concentratedRandomAngle = concentratedRandomAngle;
    }
    private int concentratedRandomLine; // 0..200
    public int getConcentratedRandomLine() {
        return this.concentratedRandomLine;
    }
    public void setConcentratedRandomLine(int concentratedRandomLine) {
        this.concentratedRandomLine = concentratedRandomLine;
    }
    private int concentratedCount; // 5..20
    public int getConcentratedCount() {
        return this.concentratedCount;
    }
    public void setConcentratedCount(int concentratedCount) {
        this.concentratedCount = concentratedCount;
    }
    private float concentratedLineLen; // 10..800
    public float getConcentratedLineLen() {
        return this.concentratedLineLen;
    }
    public void setConcentratedLineLen(float concentratedLineLen) {
        this.concentratedLineLen = concentratedLineLen;
    }
    private int concentratedWide;  // 1to40
    public int getConcentratedWide() {
        return this.concentratedWide;
    }
    public void setConcentratedWide(int concentratedWide) {
        this.concentratedWide = concentratedWide;
    }
    private int concentratedColor;
    public int getConcentratedColor() {
        return this.concentratedColor;
    }
    public void setConcentratedColor(int concentratedColor) {
        this.concentratedColor = concentratedColor;
    }
    public int getConcentratedColorColor() {
        return colorArray[this.concentratedColor % colorArray.length];
    }
    private int concentratedAlpha;
    public int getConcentratedAlpha() {
        return this.concentratedAlpha;
    }
    public void setConcentratedAlpha(int concentratedAlpha) {
        this.concentratedAlpha = concentratedAlpha;
    }

    boolean sparklingFlag;
    public boolean getSparklingFlag() {
        return this.sparklingFlag;
    }
    public void setSparklingFlag(boolean sparklingFlag) {
        this.sparklingFlag = sparklingFlag;
    }
    private int sparklingColor;
    public int getSparklingColor() {
        return this.sparklingColor;
    }
    public void setSparklingColor(int sparklingColor) {
        this.sparklingColor = sparklingColor;
    }
    public int getSparklingColorColor() {
        return colorArray[this.sparklingColor % colorArray.length];
    }
    private int sparklingAlpha;
    public int getSparklingAlpha() {
        return this.sparklingAlpha;
    }
    public void setSparklingAlpha(int sparklingAlpha) {
        this.sparklingAlpha = sparklingAlpha;
    }
    private int sparklingCount;
    public int getSparklingCount() {
        return this.sparklingCount;
    }
    public void setSparklingCount(int sparklingCount) {
        this.sparklingCount = sparklingCount;
    }
    private int sparklingLen;
    public int getSparklingLen() {
        return this.sparklingLen;
    }
    public void setSparklingLen(int sparklingLen) {
        this.sparklingLen = sparklingLen;
    }
    private int sparklingRandom;
    public int getSparklingRandom() {
        return this.sparklingRandom;
    }
    public void setSparklingRandom(int sparklingRandom) {
        this.sparklingRandom = sparklingRandom;
    }
}
