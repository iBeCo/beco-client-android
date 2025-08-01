package com.beco.demo.components;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.becomap.sdk.models.BCLocation;
import com.beco.demo.R;


/**
 * A reusable component for the search bar functionality.
 * Provides search input with search and map icons.
 */
public class SearchBarComponent {

    public interface SearchBarListener {
        /**
         * Called when the user wants to launch the search activity.
         */
        void onSearchRequested();

        /**
         * Called when the speech/voice search icon is clicked.
         */
        void onSpeechRequested();

        /**
         * Called when the user wants to select a source location in routing mode.
         */
        void onSourceLocationRequested();

        /**
         * Called when the user wants to select a destination location in routing mode.
         */
        void onDestinationLocationRequested();

        /**
         * Called when the user swaps source and destination locations.
         */
        void onLocationsSwapped(BCLocation newSource, BCLocation newDestination);
    }

    private final Context context;
    private LinearLayout searchContainer;
    private FrameLayout routingContainer;
    private EditText searchEditText;
    private ImageView searchIcon;
    private ImageView speechIcon;

    // Routing mode views
    private LinearLayout fromLocationContainer;
    private LinearLayout toLocationContainer;
    private TextView fromLocationText;
    private TextView toLocationText;
    private ImageView swapLocationsButton;

    private SearchBarListener listener;
    private BCLocation sourceLocation;
    private BCLocation destinationLocation;
    private boolean isRoutingMode = false;

    /**
     * Creates a new SearchBarComponent and inflates it into the parent container.
     *
     * @param context The context
     * @param parentContainer The parent ViewGroup to inflate the search bar into
     */
    public SearchBarComponent(Context context, ViewGroup parentContainer) {
        this.context = context;
        
        // Inflate both search bar layouts
        LayoutInflater inflater = LayoutInflater.from(context);
        this.searchContainer = (LinearLayout) inflater.inflate(R.layout.component_search_bar, parentContainer, false);
        this.routingContainer = (FrameLayout) inflater.inflate(R.layout.component_search_bar_routing, parentContainer, false);
        
        // Add both views to the parent container with top positioning and proper margins
        if (parentContainer instanceof FrameLayout) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.TOP;
            // Set minimal margins for tight layout
            params.setMargins(
                dpToPx(5),  // left margin
                dpToPx(16), // top margin - proper status bar clearance
                dpToPx(5),  // right margin
                0           // bottom margin
            );
            searchContainer.setLayoutParams(params);
            routingContainer.setLayoutParams(params);
        }

        // Add both views to parent, initially show search container
        parentContainer.addView(searchContainer);
        parentContainer.addView(routingContainer);
        routingContainer.setVisibility(View.GONE);
        
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
     * @param parentContainer The parent ViewGroup to inflate the search bar into
     * @param attachToParent Whether to attach to parent immediately
     */
    public SearchBarComponent(Context context, ViewGroup parentContainer, boolean attachToParent) {
        this.context = context;
        
        // Inflate the search bar layout
        LayoutInflater inflater = LayoutInflater.from(context);
        this.searchContainer = (LinearLayout) inflater.inflate(R.layout.component_search_bar, parentContainer, attachToParent);
        
        // Find all child views
        findViews();
        
        // Setup click listeners
        setupClickListeners();
    }

    /**
     * Finds and initializes all child views from the inflated layouts.
     */
    private void findViews() {
        // Search mode views
        searchEditText = searchContainer.findViewById(R.id.searchEditText);
        searchIcon = searchContainer.findViewById(R.id.searchIcon);
        speechIcon = searchContainer.findViewById(R.id.speechIcon);

        // Routing mode views
        fromLocationContainer = routingContainer.findViewById(R.id.fromLocationContainer);
        toLocationContainer = routingContainer.findViewById(R.id.toLocationContainer);
        fromLocationText = routingContainer.findViewById(R.id.fromLocationText);
        toLocationText = routingContainer.findViewById(R.id.toLocationText);
        swapLocationsButton = routingContainer.findViewById(R.id.swapLocationsButton);
    }

    /**
     * Sets the listener for search bar events.
     *
     * @param listener The listener to receive events
     */
    public void setListener(SearchBarListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the source location for routing.
     *
     * @param location The BCLocation to set as source
     */
    public void setSourceLocation(BCLocation location) {
        this.sourceLocation = location;
        updateViewMode();
        updateRoutingDisplay();
    }

    /**
     * Sets the destination location and updates the display.
     *
     * @param location The BCLocation to set as destination
     */
    public void setDestinationLocation(BCLocation location) {
        this.destinationLocation = location;
        updateViewMode();
        updateRoutingDisplay();
        updateSearchDisplay();
    }

    /**
     * Gets the current source location.
     *
     * @return The current source BCLocation or null if none set
     */
    public BCLocation getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Gets the current destination location.
     *
     * @return The current destination BCLocation or null if none set
     */
    public BCLocation getDestinationLocation() {
        return destinationLocation;
    }

    /**
     * Sets the text in the search input field directly.
     * Note: This does not update the destinationLocation object.
     *
     * @param text The text to set
     */
    public void setText(String text) {
        if (searchEditText != null) {
            searchEditText.setText(text);
        }
    }

    /**
     * Gets the current text from the search input field.
     *
     * @return The current text, or empty string if null
     */
    public String getText() {
        if (searchEditText != null) {
            return searchEditText.getText().toString();
        }
        return "";
    }

    /**
     * Resets the component to default state - clears all locations and switches to search mode.
     */
    public void reset() {
        this.sourceLocation = null;
        this.destinationLocation = null;
        this.isRoutingMode = false;

        if (searchEditText != null) {
            searchEditText.setText("");
        }

        // Switch to search mode
        searchContainer.setVisibility(View.VISIBLE);
        routingContainer.setVisibility(View.GONE);
    }

    /**
     * Clears the search input field and destination location.
     */
    public void clearText() {
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        this.destinationLocation = null;
        updateViewMode();
    }

    /**
     * Clears only the destination location without changing the text.
     */
    public void clearDestination() {
        this.destinationLocation = null;
        updateViewMode();
    }

    /**
     * Clears only the source location.
     */
    public void clearSource() {
        this.sourceLocation = null;
        updateViewMode();
    }

    /**
     * Clears focus from the search input field.
     */
    public void clearFocus() {
        if (searchEditText != null) {
            searchEditText.clearFocus();
        }
    }

    /**
     * Gets the root view of the search bar component.
     * This can be used for positioning or adding to layouts.
     *
     * @return The root LinearLayout of the search bar
     */
    public LinearLayout getView() {
        return searchContainer;
    }

    /**
     * Gets the EditText view for direct access if needed.
     *
     * @return The EditText view
     */
    public EditText getEditText() {
        return searchEditText;
    }

    private void setupClickListeners() {
        // Search EditText click listener - launch search activity
        searchEditText.setOnClickListener(v -> handleSearchRequest());

        // Search EditText focus listener - launch search activity when focused
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                handleSearchRequest();
            }
        });

        // Search icon click listener - launch search activity
        searchIcon.setOnClickListener(v -> handleSearchRequest());

        // Speech icon click listener - handle voice search
        speechIcon.setOnClickListener(v -> handleSpeechRequest());

        // Routing mode click listeners
        fromLocationContainer.setOnClickListener(v -> handleSourceLocationRequest());
        toLocationContainer.setOnClickListener(v -> handleDestinationLocationRequest());
        swapLocationsButton.setOnClickListener(v -> handleSwapLocations());
    }

    private void handleSearchRequest() {
        if (listener != null) {
            listener.onSearchRequested();
        } else {
            Log.w("SearchBarComponent", "No listener set for search request");
        }
    }

    private void handleSpeechRequest() {
        if (listener != null) {
            listener.onSpeechRequested();
        } else {
            Log.d("SearchBarComponent", "Speech icon clicked - no listener set");
        }
    }

    private void handleSourceLocationRequest() {
        if (listener != null) {
            listener.onSourceLocationRequested();
        } else {
            Log.d("SearchBarComponent", "Source location requested - no listener set");
        }
    }

    private void handleDestinationLocationRequest() {
        if (listener != null) {
            listener.onDestinationLocationRequested();
        } else {
            Log.d("SearchBarComponent", "Destination location requested - no listener set");
        }
    }

    private void handleSwapLocations() {
        if (sourceLocation != null && destinationLocation != null) {
            // Swap the locations
            BCLocation temp = sourceLocation;
            sourceLocation = destinationLocation;
            destinationLocation = temp;

            // Update display
            updateRoutingDisplay();

            // Notify listener
            if (listener != null) {
                listener.onLocationsSwapped(sourceLocation, destinationLocation);
            }
        }
    }

    /**
     * Updates the view mode based on current location state.
     */
    private void updateViewMode() {
        boolean shouldShowRouting = (sourceLocation != null && destinationLocation != null);

        if (shouldShowRouting != isRoutingMode) {
            isRoutingMode = shouldShowRouting;

            if (isRoutingMode) {
                // Switch to routing mode
                searchContainer.setVisibility(View.GONE);
                routingContainer.setVisibility(View.VISIBLE);
            } else {
                // Switch to search mode
                searchContainer.setVisibility(View.VISIBLE);
                routingContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Updates the routing display with current location names.
     */
    private void updateRoutingDisplay() {
        if (isRoutingMode && fromLocationText != null && toLocationText != null) {
            // Update source location display
            if (sourceLocation != null) {
                String sourceText = getLocationDisplayText(sourceLocation);
                fromLocationText.setText(sourceText);
            }

            // Update destination location display
            if (destinationLocation != null) {
                String destText = getLocationDisplayText(destinationLocation);
                toLocationText.setText(destText);
            }
        }
    }

    /**
     * Updates the search display with destination location.
     */
    private void updateSearchDisplay() {
        if (!isRoutingMode && destinationLocation != null && searchEditText != null) {
            String displayText = getLocationDisplayText(destinationLocation);
            searchEditText.setText(displayText);
        }
    }

    /**
     * Gets display text for a location with fallbacks.
     */
    private String getLocationDisplayText(BCLocation location) {
        if (location == null) return "";

        String displayText = location.getName();
        if (displayText == null || displayText.trim().isEmpty()) {
            displayText = "Location " + location.getId();
        }
        return displayText;
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
