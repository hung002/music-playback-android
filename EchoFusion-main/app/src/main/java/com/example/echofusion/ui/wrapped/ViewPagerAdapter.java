package com.example.echofusion.ui.wrapped;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.echofusion.ui.wrapped.tabs.Tab1;
import com.example.echofusion.ui.wrapped.tabs.Tab2;
import com.example.echofusion.ui.wrapped.tabs.Tab3;
import com.example.echofusion.ui.wrapped.tabs.Tab4;
import com.example.echofusion.ui.wrapped.tabs.Tab5;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Tab1();
            case 1:
                return new Tab2();
            case 2:
                return new Tab3();
            case 3:
                return new Tab4();
            case 4:
                return new Tab5();
            default:
                return new Tab1();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
