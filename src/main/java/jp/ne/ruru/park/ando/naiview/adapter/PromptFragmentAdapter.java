package jp.ne.ruru.park.ando.naiview.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import jp.ne.ruru.park.ando.naiview.PromptAbstractFragment;
import jp.ne.ruru.park.ando.naiview.PromptFragment;
import jp.ne.ruru.park.ando.naiview.PromptUcFragment;

/**
 * prompt fragment adapter
 */
public class PromptFragmentAdapter extends FragmentStateAdapter {

    /**
     * prompt fragment adapter constructor
     * @param fragment fragment
     */
    public PromptFragmentAdapter(FragmentActivity fragment) {
        super(fragment);
    }

    /**
     * create fragment
     * @param position fragment position
     * @return fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new PromptFragment();
        } else {
            fragment = new PromptUcFragment();
        }
        return fragment;
    }

    /**
     * get item count
     * @return item count
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}