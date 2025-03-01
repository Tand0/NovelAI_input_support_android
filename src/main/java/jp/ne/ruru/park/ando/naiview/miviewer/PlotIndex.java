package jp.ne.ruru.park.ando.naiview.miviewer;

import java.util.LinkedList;

public class PlotIndex {
    private int index = 0;
    public int getIndex() {
        return this.index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    private long timeMoveBase = 0;
    public long getTimeMoveBase() {
        return this.timeMoveBase;
    }
    public void setTimeMoveBase(long timeMoveBase) {
        this.timeMoveBase = timeMoveBase;
    }
    private final LinkedList<Plot> plotList = new LinkedList<>();
    public LinkedList<Plot> getPlotList() {
        return this.plotList;
    }
}
