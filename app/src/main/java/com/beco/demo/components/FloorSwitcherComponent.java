package com.beco.demo.components;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCMapFloor;
import com.beco.demo.FloorAdapter;
import com.beco.demo.R;


import java.util.List;

/**
 * A reusable component for the floor switcher functionality.
 * Provides floor selection with expandable/collapsible floor list.
 */
public class FloorSwitcherComponent implements FloorAdapter.OnFloorSelectedListener {

    public interface FloorSwitcherListener {
        /**
         * Called when a floor is selected.
         * @param floor The selected floor
         */
        void onFloorSelected(BCMapFloor floor);
    }

    private final Context context;
    private LinearLayout floorSelectorContainer;
    private RecyclerView floorRecyclerView;
    private LinearLayout selectedFloorContainer;
    private TextView selectedFloorText;

    private FloorAdapter floorAdapter;
    private List<BCMapFloor> floors;
    private boolean isExpanded = false;
    private FloorSwitcherListener listener;

    /**
     * Creates a new FloorSwitcherComponent and inflates it into the parent container.
     *
     * @param context The context
     * @param parentContainer The parent ViewGroup to inflate the floor switcher into
     */
    public FloorSwitcherComponent(Context context, ViewGroup parentContainer) {
        this.context = context;
        
        // Inflate the floor switcher layout
        LayoutInflater inflater = LayoutInflater.from(context);
        this.floorSelectorContainer = (LinearLayout) inflater.inflate(R.layout.component_floor_switcher, parentContainer, false);
        
        // Add the inflated view to the parent container with bottom-left positioning
        if (parentContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.START;
            // Set proper margins
            params.setMargins(
                dpToPx(16), // left margin
                0,          // top margin
                0,          // right margin
                dpToPx(32)  // bottom margin
            );
            floorSelectorContainer.setLayoutParams(params);
        }
        parentContainer.addView(floorSelectorContainer);
        
        // Find all child views
        findViews();
        
        // Setup click listeners and RecyclerView
        setupViews();
    }

    /**
     * Alternative constructor that takes an existing parent view.
     * This is useful when you want to inflate into a specific container.
     *
     * @param context The context
     * @param parentContainer The parent ViewGroup to inflate the floor switcher into
     * @param attachToParent Whether to attach to parent immediately
     */
    public FloorSwitcherComponent(Context context, ViewGroup parentContainer, boolean attachToParent) {
        this.context = context;
        
        // Inflate the floor switcher layout
        LayoutInflater inflater = LayoutInflater.from(context);
        this.floorSelectorContainer = (LinearLayout) inflater.inflate(R.layout.component_floor_switcher, parentContainer, attachToParent);
        
        // Find all child views
        findViews();
        
        // Setup click listeners and RecyclerView
        setupViews();
    }

    /**
     * Finds and initializes all child views from the inflated layout.
     */
    private void findViews() {
        floorRecyclerView = floorSelectorContainer.findViewById(R.id.floorRecyclerView);
        selectedFloorContainer = floorSelectorContainer.findViewById(R.id.selectedFloorContainer);
        selectedFloorText = floorSelectorContainer.findViewById(R.id.selectedFloorText);
    }

    /**
     * Sets up the RecyclerView and click listeners.
     */
    private void setupViews() {
        // Set up RecyclerView with vertical layout
        floorRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Set up click listener for floor selector
        selectedFloorContainer.setOnClickListener(v -> toggleExpansion());
    }

    /**
     * Sets the listener for floor switcher events.
     *
     * @param listener The listener to receive events
     */
    public void setListener(FloorSwitcherListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the list of floors to display.
     *
     * @param floors The list of floors
     */
    public void setFloors(List<BCMapFloor> floors) {
        this.floors = floors;
        
        // Create and set adapter
        floorAdapter = new FloorAdapter(floors, this);
        floorRecyclerView.setAdapter(floorAdapter);
    }

    /**
     * Shows the floor switcher with animation.
     */
    public void show() {
        if (floorSelectorContainer != null) {
            floorSelectorContainer.setVisibility(View.VISIBLE);
            // Fade in animation
            floorSelectorContainer.setAlpha(0f);
            floorSelectorContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    /**
     * Hides the floor switcher with animation.
     */
    public void hide() {
        if (floorSelectorContainer != null) {
            floorSelectorContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> floorSelectorContainer.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * Checks if the floor switcher is currently visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return floorSelectorContainer != null && floorSelectorContainer.getVisibility() == View.VISIBLE;
    }

    /**
     * Checks if the floor list is currently expanded.
     *
     * @return true if expanded, false otherwise
     */
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * Updates the selected floor display.
     *
     * @param floor The floor to display as selected
     */
    public void updateSelectedFloor(BCMapFloor floor) {
        if (floor != null && selectedFloorText != null) {
            // Display floor short name, name, or fallback
            String displayText = floor.getShortName();
            if (displayText == null || displayText.trim().isEmpty()) {
                displayText = floor.getName();
            }
            if (displayText == null || displayText.trim().isEmpty()) {
                displayText = "Floor " + floor.getId();
            }
            selectedFloorText.setText(displayText);
        }
    }

    /**
     * Gets the root view of the floor switcher component.
     * This can be used for positioning or adding to layouts.
     *
     * @return The root LinearLayout of the floor switcher
     */
    public LinearLayout getView() {
        return floorSelectorContainer;
    }

    /**
     * Toggles the expansion state of the floor list.
     */
    private void toggleExpansion() {
        if (isExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * Expands the floor list with animation.
     */
    private void expand() {
        floorRecyclerView.setVisibility(View.VISIBLE);
        isExpanded = true;

        // Vertical slide animation (slide up from bottom)
        floorRecyclerView.setTranslationY(floorRecyclerView.getHeight());
        floorRecyclerView.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    /**
     * Collapses the floor list with animation.
     */
    private void collapse() {
        floorRecyclerView.animate()
                .translationY(floorRecyclerView.getHeight())
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    floorRecyclerView.setVisibility(View.GONE);
                    floorRecyclerView.setTranslationY(0);
                    floorRecyclerView.setAlpha(1f);
                })
                .start();

        isExpanded = false;
    }

    /**
     * Implementation of FloorAdapter.OnFloorSelectedListener
     */
    @Override
    public void onFloorSelected(BCMapFloor floor) {
        // Update the display
        updateSelectedFloor(floor);
        
        // Collapse the list
        collapse();
        
        // Notify listener
        if (listener != null) {
            listener.onFloorSelected(floor);
        } else {
            Log.w("FloorSwitcherComponent", "No listener set for floor selection");
        }
    }

    /**
     * Converts dp to pixels based on device density.
     *
     * @param dp The dp value to convert
     * @return The equivalent pixel value
     */
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.getResources().getDisplayMetrics()
        );
    }
}
