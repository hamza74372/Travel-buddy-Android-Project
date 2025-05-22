package com.example.travelbuddy;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

public class HomeFragment extends Fragment {

    private int[] images = {
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3,
            R.drawable.image4
    };

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private Handler handler = new Handler();
    private Runnable runnable;

    private int currentPage = 0;
    private final int AUTO_SCROLL_DELAY = 4000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        dotsLayout = view.findViewById(R.id.dotsLayout);

        ImageAdapter adapter = new ImageAdapter(images);
        viewPager.setAdapter(adapter);

        // Initialize dots after layout is drawn
        view.post(() -> addDots(0));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                addDots(position);
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                currentPage = (currentPage + 1) % images.length;
                viewPager.setCurrentItem(currentPage, true);
                handler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        };
        handler.postDelayed(runnable, AUTO_SCROLL_DELAY);

        return view;
    }

    private void addDots(int position) {
        if (getContext() == null) return;

        dotsLayout.removeAllViews();
        ImageView[] dots = new ImageView[images.length];

        int size = (int) (getResources().getDisplayMetrics().density * 6); // 6dp size

        for (int i = 0; i < images.length; i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageResource(i == position ? R.drawable.active_dot : R.drawable.inactive_dot);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}
