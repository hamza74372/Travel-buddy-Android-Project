package com.example.travelbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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

        try {
            viewPager = view.findViewById(R.id.viewPager);
            dotsLayout = view.findViewById(R.id.dotsLayout);

            if (viewPager == null || dotsLayout == null) {
                throw new IllegalStateException("Required views not found in layout");
            }

            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(images);
            viewPager.setAdapter(imageSliderAdapter);

            setupCardViews(view);
            view.post(() -> addDots(0));

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    currentPage = position;
                    addDots(position);
                }
            });

            startAutoScroll();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return view;
    }

    private void setupCardViews(View rootView) {
        try {
            CardView findBuddiesCard = rootView.findViewById(R.id.findBuddiesCard);
            CardView planTripCard = rootView.findViewById(R.id.aiTripPlannerCard);
            CardView startTripCard = rootView.findViewById(R.id.startTripCard);
            CardView chatWithBuddiesCard = rootView.findViewById(R.id.chatWithBuddiesCard);
            CardView savedPicturesCard = rootView.findViewById(R.id.savePicturesCard);

            if (findBuddiesCard != null) {
                findBuddiesCard.setOnClickListener(v -> {
                    if (isAdded()) {
                        startActivity(new Intent(requireActivity(), CreatePostActivity.class));
                    }
                });
            }

            if (planTripCard != null) {
                planTripCard.setOnClickListener(v -> {
                    if (isAdded()) {
                        startActivity(new Intent(requireActivity(), AiPlanTripActivity.class));
                    }
                });
            }

            if (startTripCard != null) {
                startTripCard.setOnClickListener(v -> {
                    if (isAdded() && getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToMapFragment();
                    }
                });
            }

            if (chatWithBuddiesCard != null) {
                chatWithBuddiesCard.setOnClickListener(v -> {
                    if (isAdded() && getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToChatFragment();
                    }
                });
            }

            if (savedPicturesCard != null) {
                savedPicturesCard.setOnClickListener(v -> {
                    if (isAdded()) {
                        startActivity(new Intent(requireActivity(), SavePicturesActivity.class));
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error setting up cards: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void startAutoScroll() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager != null && isAdded() && !isDetached()) {
                    currentPage = (currentPage + 1) % images.length;
                    viewPager.setCurrentItem(currentPage, true);
                    handler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };
        handler.postDelayed(runnable, AUTO_SCROLL_DELAY);
    }

    private void addDots(int position) {
        try {
            if (dotsLayout == null || !isAdded() || getContext() == null) return;

            dotsLayout.removeAllViews();
            ImageView[] dots = new ImageView[images.length];

            int size = (int) (getResources().getDisplayMetrics().density * 8); // 8dp

            for (int i = 0; i < images.length; i++) {
                dots[i] = new ImageView(requireContext());
                dots[i].setImageDrawable(ContextCompat.getDrawable(requireContext(),
                        i == position ? R.drawable.active_dot : R.drawable.inactive_dot));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(8, 0, 8, 0);
                dotsLayout.addView(dots[i], params);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error adding dots: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (runnable != null) {
            handler.postDelayed(runnable, AUTO_SCROLL_DELAY);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        viewPager = null;
        dotsLayout = null;
    }
}
