package jp.ne.ruru.park.ando.naiview.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import jp.ne.ruru.park.ando.naiview.PromptAbstractFragment;
import jp.ne.ruru.park.ando.naiview.PromptBaseOkFragment;
import jp.ne.ruru.park.ando.naiview.PromptBaseNgFragment;
import jp.ne.ruru.park.ando.naiview.PromptCh01NgFragment;
import jp.ne.ruru.park.ando.naiview.PromptCh01OkFragment;
import jp.ne.ruru.park.ando.naiview.PromptCh02NgFragment;
import jp.ne.ruru.park.ando.naiview.PromptCh02OkFragment;
import jp.ne.ruru.park.ando.naiview.data.PromptType;

/**
 * prompt fragment adapter
 */
public class PromptFragmentAdapter extends FragmentStateAdapter {

    private final PromptAbstractFragment[] promptAbstractFragment;
    /**
     * prompt fragment adapter constructor
     * @param fragment fragment
     */
    public PromptFragmentAdapter(FragmentActivity fragment) {
        super(fragment);
        promptAbstractFragment = new PromptAbstractFragment[getItemCount()];
    }

    /**
     * create fragment
     * @param position fragment position
     * @return fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PromptAbstractFragment target;
        switch (position) {
            case 1:
                target = new PromptCh01OkFragment();
                break;
            case 2:
                target = new PromptCh02OkFragment();
                break;
            case 3:
                target = new PromptBaseNgFragment();
                break;
            case 4:
                target = new PromptCh01NgFragment();
                break;
            case 5:
                target = new PromptCh02NgFragment();
                break;
            case 0:
            default:
                target = new PromptBaseOkFragment();
                break;
        }
        promptAbstractFragment[position] = target;
        return target;
    }
    public static PromptType getPositionToPromptType(int position) {
        PromptType target;
        switch (position) {
            case 1:
                target = PromptType.P_CH01_OK;
                break;
            case 2:
                target = PromptType.P_CH02_OK;
                break;
            case 3:
                target = PromptType.P_BASE_NG;
                break;
            case 4:
                target = PromptType.P_CH01_NG;
                break;
            case 5:
                target = PromptType.P_CH02_NG;
                break;
            case 0:
            default:
                target =  PromptType.P_BASE_OK;
                break;
        }
        return target;
    }


    /**
     * get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return PromptType.values().length;
    }

    public void actionSave() {
        for (PromptAbstractFragment fragment: promptAbstractFragment) {
            if (fragment == null) {
                continue;
            }
            //
            fragment.onSave();
        }
    }
    public void actionLoad() {
        for (PromptAbstractFragment fragment: promptAbstractFragment) {
            if (fragment == null) {
                continue;
            }
            //
            fragment.onLoad();
        }
    }
}