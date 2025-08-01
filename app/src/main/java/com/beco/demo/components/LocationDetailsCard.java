package com.beco.demo.components;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.becomap.sdk.models.BCCategory;
import com.becomap.sdk.models.BCLocation;
import com.beco.demo.R;


/**
 * A reusable component for displaying location details in a bottom card.
 * Provides location information with Cancel and Navigate action buttons.
 */
public class LocationDetailsCard {

    public interface LocationDetailsListener {
        /**
         * Called when the user wants to navigate to the location.
         * @param location The location to navigate to
         */
        void onNavigateToLocation(BCLocation location);

        /**
         * Called when the card is dismissed (close or cancel).
         */
        void onCardDismissed();
    }

    private final Context context;
    private LinearLayout cardView;
    private TextView locationNameText;
    private TextView locationDescriptionText;
    private ImageView closeButton;
    private Button cancelButton;
    private Button navigateButton;

    private BCLocation currentLocation;
    private LocationDetailsListener listener;

    /**
     * Creates a new LocationDetailsCard component and inflates it into the parent container.
     *
     * @param context The context
     * @param parentContainer The parent ViewGroup to inflate the card into
     */
    public LocationDetailsCard(Context context, ViewGroup parentContainer) {
        this.context = context;

        // Inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(context);
        this.cardView = (LinearLayout) inflater.inflate(R.layout.component_location_details_card, parentContainer, false);

        // Add the inflated view to the parent container with bottom positioning
        if (parentContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM;
            cardView.setLayoutParams(params);
        }
        parentContainer.addView(cardView);

        // Find all child views
        findViews();

        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Alternative constructor that takes an existing parent view.
     * This is useful when you want to inflate into a specific container.
     *
     * @param context The context
     * @param parentContainer The parent ViewGroup to inflate the card into
     * @param attachToParent Whether to attach to parent immediately
     */
    public LocationDetailsCard(Context context, ViewGroup parentContainer, boolean attachToParent) {
        this.context = context;

        // Inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(context);
        this.cardView = (LinearLayout) inflater.inflate(R.layout.component_location_details_card, parentContainer, attachToParent);

        // Find all child views
        findViews();

        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Finds and initializes all child views from the inflated layout.
     */
    private void findViews() {
        locationNameText = cardView.findViewById(R.id.locationNameText);
        locationDescriptionText = cardView.findViewById(R.id.locationDescriptionText);
        closeButton = cardView.findViewById(R.id.closeButton);
        cancelButton = cardView.findViewById(R.id.cancelButton);
        navigateButton = cardView.findViewById(R.id.navigateButton);
    }

    /**
     * Sets the listener for card events.
     *
     * @param listener The listener to receive events
     */
    public void setListener(LocationDetailsListener listener) {
        this.listener = listener;
    }

    /**
     * Shows the card with the specified location details.
     *
     * @param location The location to display
     */
    public void showLocation(BCLocation location) {
        if (location == null) {
            Log.w("LocationDetailsCard", "Cannot show card with null location");
            return;
        }

        this.currentLocation = location;

        // Set location name
        locationNameText.setText(location.getName());

        // Set location description with fallbacks
        String description = buildLocationDescription(location);
        locationDescriptionText.setText(description);

        // Show the card with animation
        showCardWithAnimation();
    }

    /**
     * Dismisses the card with animation.
     */
    public void dismiss() {
        hideCardWithAnimation();
    }

    /**
     * Checks if the card is currently visible.
     *
     * @return true if the card is visible, false otherwise
     */
    public boolean isVisible() {
        return cardView.getVisibility() == View.VISIBLE;
    }

    /**
     * Gets the currently displayed location.
     *
     * @return The current location, or null if no location is displayed
     */
    public BCLocation getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Gets the root view of the card component.
     * This can be used for positioning or adding to layouts.
     *
     * @return The root LinearLayout of the card
     */
    public LinearLayout getView() {
        return cardView;
    }

    private void setupClickListeners() {
        // Close button click listener
        closeButton.setOnClickListener(v -> handleDismiss());

        // Cancel button click listener
        cancelButton.setOnClickListener(v -> handleDismiss());

        // Navigate button click listener
        navigateButton.setOnClickListener(v -> handleNavigate());
    }

    private void handleDismiss() {
        hideCardWithAnimation();
        if (listener != null) {
            listener.onCardDismissed();
        }
    }

    private void handleNavigate() {
        if (currentLocation != null) {
            if (listener != null) {
                listener.onNavigateToLocation(currentLocation);
            } else {
                // Fallback behavior if no listener is set
                Toast.makeText(context, "Navigate to " + currentLocation.getName(), Toast.LENGTH_SHORT).show();
            }
            // Dismiss the card after navigation
            dismiss();
        }
    }

    private String buildLocationDescription(BCLocation location) {
        // Use description if available
        String description = location.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }

        // Try to get category information
        if (location.getCategories() != null && !location.getCategories().isEmpty()) {
            BCCategory firstCategory = location.getCategories().get(0);
            if (firstCategory != null && firstCategory.getName() != null) {
                return "Category: " + firstCategory.getName();
            }
        }

        // Try amenity information
        if (location.getAmenity() != null && !location.getAmenity().trim().isEmpty()) {
            return "Amenity: " + location.getAmenity();
        }

        // Default fallback
        return "Location details";
    }

    private void showCardWithAnimation() {
        cardView.setVisibility(View.VISIBLE);
        cardView.setAlpha(0f);
        cardView.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
    }

    private void hideCardWithAnimation() {
        cardView.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                cardView.setVisibility(View.GONE);
                currentLocation = null;
            })
            .start();
    }
}
