package com.camera.gps.camerax.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ViewPagerTitleAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<Fragment> fragments;
    private final ArrayList<String> titles;

    public ViewPagerTitleAdapter(FragmentManager fragmentManager, ArrayList<Fragment> arrayList, ArrayList<String> arrayList2) {
        super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = arrayList;
        this.titles = arrayList2;
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        return this.fragments.get(i);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return this.titles.get(i);
    }
}