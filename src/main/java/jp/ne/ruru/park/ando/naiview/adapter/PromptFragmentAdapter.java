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
        PromptAbstractFragment target = switch (position) {
            case 1 -> new PromptCh01OkFragment();
            case 2 -> new PromptCh02OkFragment();
            case 3 -> new PromptBaseNgFragment();
            case 4 -> new PromptCh01NgFragment();
            case 5 -> new PromptCh02NgFragment();
            default -> new PromptBaseOkFragment();
        };
        promptAbstractFragment[position] = target;
        return target;
    }
    public static PromptType getPositionToPromptType(int position) {
        return switch (position) {
            case 1 -> PromptType.P_CH01_OK;
            case 2 -> PromptType.P_CH02_OK;
            case 3 -> PromptType.P_BASE_NG;
            case 4 -> PromptType.P_CH01_NG;
            case 5 -> PromptType.P_CH02_NG;
            default -> PromptType.P_BASE_OK;
        };
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