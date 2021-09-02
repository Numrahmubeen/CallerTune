package com.caller.tune.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.caller.tune.R;
import com.caller.tune.fragments.PhoneFragment;
import com.caller.tune.fragments.PriorityFragment;
import com.caller.tune.fragments.RecentCallsFragment;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

public class ViewPagerAdapter extends FragmentStateAdapter {


    public ViewPagerAdapter(FragmentActivity fragment) {
        super(fragment);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PriorityFragment();
            case 1:
                return new PhoneFragment();
            case 2:
                return new RecentCallsFragment();
            default:
                return null;
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
