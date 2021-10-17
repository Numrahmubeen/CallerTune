package com.appsuite.prioritycontacts.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.appsuite.prioritycontacts.fragments.PhoneFragment;
import com.appsuite.prioritycontacts.fragments.PriorityFragment;
import com.appsuite.prioritycontacts.fragments.RecentCallsFragment;

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
