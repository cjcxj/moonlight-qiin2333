package com.limelight.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.limelight.AppView;
import com.limelight.R;
import com.limelight.grid.GenericGridAdapter;

/**
 * Selection Indicator Animator
 * Manages the animation and position calculation of the selection indicator
 */
public class SelectionIndicatorAnimator {

    private RecyclerView recyclerView;
    private GenericGridAdapter<?> adapter;

    // Animation configuration
    private static final int NORMAL_ANIMATION_DURATION = 200;
    private static final int SCALE_ANIMATION_DURATION = 150;
    private static final int SCROLL_WAIT_DELAY = 50;
    private static final int RETRY_DELAY = 100;

    public SelectionIndicatorAnimator() {
    }

    /**
     * Update RecyclerView and Adapter references
     */
    public void updateReferences(RecyclerView recyclerView, GenericGridAdapter<?> adapter) {
        this.recyclerView = recyclerView;
        this.adapter = adapter;
    }

    /**
     * Move selection indicator to specified position
     *
     * @param selectionIndicator The selection indicator View for the current item.
     * @param isFirstFocus Whether this is the first focus (starting from position 0)
     */
    public void moveToPosition(View selectionIndicator, boolean isFirstFocus) {
        if (selectionIndicator == null || recyclerView == null || adapter == null || positionProvider == null) {
            return;
        }

        int position = positionProvider.getCurrentPosition();
        if (!isValidPosition(position)) {
            return;
        }

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            if (isFirstFocus) {
                // First focus, position directly without animation
                setIndicatorPosition(selectionIndicator, viewHolder.itemView, false);
            } else {
                // Normal case: item is in visible area, use animation
                animateToView(selectionIndicator, viewHolder.itemView);
            }
        } else {
            // Edge case: item is not in visible area, need to scroll
            scrollToPositionAndAnimate(selectionIndicator, position);
        }
    }

    /**
     * Move selection indicator to specified position (with animation by default)
     *
     * @param selectionIndicator The selection indicator View for the current item.
     */
    public void moveToPosition(View selectionIndicator) {
        moveToPosition(selectionIndicator, false);
    }

    /**
     * Update selection indicator position (called during scrolling)
     *
     * @param selectionIndicator The selection indicator View for the current item.
     */
    public void updatePosition(View selectionIndicator) {
        if (selectionIndicator == null || recyclerView == null || adapter == null || positionProvider == null) {
            return;
        }

        int position = positionProvider.getCurrentPosition();
        if (!isValidPosition(position)) {
            return;
        }

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            setIndicatorPositionFast(selectionIndicator, viewHolder.itemView);
        }
    }

    /**
     * Fast set indicator position - dedicated for scroll updates, minimizing calculations
     *
     * @param selectionIndicator The selection indicator View for the current item.
     * @param targetView Target View
     */
    private void setIndicatorPositionFast(View selectionIndicator, View targetView) {
        // Cache dimensions to avoid repeated settings
        int targetWidth = targetView.getWidth();
        int targetHeight = targetView.getHeight();

        int indicatorWidth = targetWidth;
        int indicatorHeight = targetHeight;

        // Only update LayoutParams when dimensions change
        ViewGroup.LayoutParams params = selectionIndicator.getLayoutParams();
        if (params.width != indicatorWidth || params.height != indicatorHeight) {
            params.width = indicatorWidth;
            params.height = indicatorHeight;
            selectionIndicator.setLayoutParams(params);
        }

        selectionIndicator.setTranslationX(0);
        selectionIndicator.setTranslationY(0);
        selectionIndicator.setVisibility(View.VISIBLE);
    }

    /**
     * Check if position is valid
     */
    private boolean isValidPosition(int position) {
        return recyclerView != null &&
                adapter != null &&
                position >= 0 &&
                position < adapter.getCount();
    }

    /**
     * Animate to specified View
     * @param selectionIndicator The selection indicator View for the current item.
     * @param targetView Target View
     */
    private void animateToView(View selectionIndicator, View targetView) {
        // Check if another animation is in progress
        if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
            // If RecyclerView is scrolling, wait for scroll to complete
            recyclerView.postDelayed(() -> {
                // Recalculate position as scrolling may have changed it
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(getCurrentPosition());
                if (viewHolder != null) {
                    View newSelectionIndicator = viewHolder.itemView.findViewById(R.id.selectionIndicator);
                    if (newSelectionIndicator != null) {
                        animateToView(newSelectionIndicator, viewHolder.itemView);
                    }
                }
            }, RETRY_DELAY);
        } else {
            // Smoothly move to new position
            setIndicatorPosition(selectionIndicator, targetView, true);
        }
    }

    /**
     * Scroll to specified position and execute animation
     * @param selectionIndicator The selection indicator View for the current item.
     * @param position Target position
     */
    private void scrollToPositionAndAnimate(View selectionIndicator, int position) {
        // Temporarily hide indicator to avoid showing wrong position during scroll
        selectionIndicator.setVisibility(View.INVISIBLE);

        // Smooth scroll to specified position
        recyclerView.smoothScrollToPosition(position);

        // Use OnScrollListener to detect scroll completion
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // When scrolling stops, execute indicator animation
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Remove this temporary scroll listener
                    recyclerView.removeOnScrollListener(this);

                    // Delay briefly to ensure scroll is completely stopped, then execute indicator animation
                    recyclerView.postDelayed(() -> {
                        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                        if (viewHolder != null) {
                            View newSelectionIndicator = viewHolder.itemView.findViewById(R.id.selectionIndicator);
                            if (newSelectionIndicator != null) {
                                setIndicatorPosition(newSelectionIndicator, viewHolder.itemView, true);
                                // Add scale emphasis effect
                                addScaleAnimation(newSelectionIndicator);
                            }
                        }
                    }, SCROLL_WAIT_DELAY);
                }
            }
        };

        // Add temporary scroll listener
        recyclerView.addOnScrollListener(scrollListener);
    }

    /**
     * Set indicator position
     *
     * @param selectionIndicator The selection indicator View for the current item.
     * @param targetView    Target View
     * @param withAnimation Whether to use animation
     */
    private void setIndicatorPosition(View selectionIndicator, View targetView, boolean withAnimation) {
        // Cache dimensions to avoid repeated settings
        int targetWidth = targetView.getWidth();
        int targetHeight = targetView.getHeight();

        int indicatorWidth = targetWidth;
        int indicatorHeight = targetHeight;

        // Only update LayoutParams when dimensions change
        ViewGroup.LayoutParams params = selectionIndicator.getLayoutParams();
        if (params.width != indicatorWidth || params.height != indicatorHeight) {
            params.width = indicatorWidth;
            params.height = indicatorHeight;
            selectionIndicator.setLayoutParams(params);
        }

        // Show indicator
        selectionIndicator.setVisibility(View.VISIBLE);

        if (withAnimation) {
            // Use faster animation method
            selectionIndicator.animate()
                    .translationX(0) // [修改] X方向不偏移
                    .translationY(0) // [修改] Y方向不偏移
                    .setDuration(Math.min(NORMAL_ANIMATION_DURATION, 120)) // Further reduce animation time
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f)) // Use faster interpolator
                    .start();
        } else {
            // Set position directly, use translationX/Y for better performance
            selectionIndicator.setTranslationX(0);
            selectionIndicator.setTranslationY(0);
        }
    }

    /**
     * Add scale emphasis animation
     * @param selectionIndicator The selection indicator View for the current item.
     */
    private void addScaleAnimation(View selectionIndicator) {
        selectionIndicator.setScaleX(0.8f);
        selectionIndicator.setScaleY(0.8f);
        selectionIndicator.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(SCALE_ANIMATION_DURATION)
                .start();
    }

    /**
     * Interface for current selected position provider
     */
    public interface PositionProvider {
        int getCurrentPosition();
    }

    private PositionProvider positionProvider;

    public void setPositionProvider(PositionProvider provider) {
        this.positionProvider = provider;
    }


    /**
     * Get current selected position
     */
    private int getCurrentPosition() {
        if (positionProvider != null) {
            return positionProvider.getCurrentPosition();
        }
        return -1;
    }
}