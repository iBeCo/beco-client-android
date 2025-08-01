package com.beco.demo.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCMapFloor;
import com.becomap.sdk.models.BCRoute;
import com.beco.demo.R;


import java.util.List;

/**
 * Component for displaying route information in a bottom bar.
 * Shows destination, distance, estimated time, and involved floors.
 */
public class RouteInfoBarComponent {

    private static final String TAG = "RouteInfoBarComponent";

    // Track the currently selected floor for visual state
    private String selectedFloorId = null;

    public interface RouteInfoBarListener {
        void onFloorSelected(String floorId, String floorName);
        void onRouteInfoClosed();
    }

    private final Context context;
    private final ViewGroup parentContainer;
    private RouteInfoBarListener listener;

    // Views
    private LinearLayout routeInfoContainer;
    private TextView destinationText;
    private TextView distanceText;
    private TextView estimatedTimeText;
    private LinearLayout involvedFloorsSection;
    private RecyclerView floorsRecyclerView;
    private ImageView closeButton;

    // Data
    private BCRoute currentRoute;
    private FloorsAdapter floorsAdapter;
    private List<BCMapFloor> availableFloors;

    public RouteInfoBarComponent(Context context, ViewGroup parentContainer) {
        this.context = context;
        this.parentContainer = parentContainer;
        initializeComponent();
    }

    private void initializeComponent() {
        // Inflate the route info bar layout
        LayoutInflater inflater = LayoutInflater.from(context);
        routeInfoContainer = (LinearLayout) inflater.inflate(R.layout.component_route_info_bar, parentContainer, false);

        // Initialize views
        destinationText = routeInfoContainer.findViewById(R.id.destinationText);
        distanceText = routeInfoContainer.findViewById(R.id.distanceText);
        estimatedTimeText = routeInfoContainer.findViewById(R.id.estimatedTimeText);
        involvedFloorsSection = routeInfoContainer.findViewById(R.id.involvedFloorsSection);
        floorsRecyclerView = routeInfoContainer.findViewById(R.id.floorsRecyclerView);
        closeButton = routeInfoContainer.findViewById(R.id.closeButton);

        // Setup RecyclerView
        setupFloorsRecyclerView();

        // Setup close button
        setupCloseButton();

        // Add to parent container
        parentContainer.addView(routeInfoContainer);
    }

    private void setupFloorsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        floorsRecyclerView.setLayoutManager(layoutManager);
        
        floorsAdapter = new FloorsAdapter();
        floorsRecyclerView.setAdapter(floorsAdapter);
    }

    private void setupCloseButton() {
        closeButton.setOnClickListener(v -> {
            hide();
            if (listener != null) {
                listener.onRouteInfoClosed();
            }
        });
    }

    public void setListener(RouteInfoBarListener listener) {
        this.listener = listener;
    }

    public void setFloors(List<BCMapFloor> floors) {
        this.availableFloors = floors;
    }

    /**
     * Set the currently selected floor for visual highlighting
     * @param floorId The ID of the floor to highlight as selected
     */
    public void setSelectedFloor(String floorId) {
        this.selectedFloorId = floorId;
        if (floorsAdapter != null) {
            floorsAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "Selected floor set to: " + floorId);
    }

    public void showRoute(BCRoute route, String destinationName) {
        this.currentRoute = route;
        
        // Update destination
        destinationText.setText(destinationName != null ? destinationName : "Unknown Destination");
        
        // Update distance
        String distance = route.getFormattedDistance();
        distanceText.setText(distance != null ? distance : "--");
        
        // Update estimated time
        String estimatedTime = route.getFormattedTime();
        estimatedTimeText.setText(estimatedTime != null ? estimatedTime : "--");
        
        // Update involved floors
        updateInvolvedFloors(route);
        
        // Show the component
        show();
        
        Log.d(TAG, "Route info displayed for destination: " + destinationName);
    }

    private void updateInvolvedFloors(BCRoute route) {
        if (route.isMultiFloor()) {
            List<String> involvedFloors = route.getInvolvedFloors();
            if (involvedFloors != null && !involvedFloors.isEmpty()) {
                floorsAdapter.setFloors(involvedFloors);
                involvedFloorsSection.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing " + involvedFloors.size() + " involved floors");
            } else {
                involvedFloorsSection.setVisibility(View.GONE);
            }
        } else {
            involvedFloorsSection.setVisibility(View.GONE);
            Log.d(TAG, "Single floor route - hiding floors section");
        }
    }

    public void show() {
        routeInfoContainer.setVisibility(View.VISIBLE);
    }

    public void hide() {
        routeInfoContainer.setVisibility(View.GONE);
    }

    public boolean isVisible() {
        return routeInfoContainer.getVisibility() == View.VISIBLE;
    }

    // RecyclerView Adapter for floors
    private class FloorsAdapter extends RecyclerView.Adapter<FloorsAdapter.FloorViewHolder> {

        private List<String> floorIds;

        public void setFloors(List<String> floorIds) {
            this.floorIds = floorIds;
            notifyDataSetChanged();
        }

        @Override
        public FloorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_route_floor, parent, false);
            return new FloorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FloorViewHolder holder, int position) {
            String floorId = floorIds.get(position);
            holder.bind(floorId);
        }

        @Override
        public int getItemCount() {
            return floorIds != null ? floorIds.size() : 0;
        }

        class FloorViewHolder extends RecyclerView.ViewHolder {
            private TextView floorNameText;

            public FloorViewHolder(View itemView) {
                super(itemView);
                floorNameText = itemView.findViewById(R.id.floorNameText);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && floorIds != null) {
                        String floorId = floorIds.get(position);
                        String floorName = getFloorDisplayName(floorId);
                        Log.d(TAG, "Floor clicked: " + floorName + " (ID: " + floorId + ")");

                        // Update selected floor and refresh adapter
                        selectedFloorId = floorId;
                        notifyDataSetChanged();

                        if (listener != null) {
                            listener.onFloorSelected(floorId, floorName);
                        }
                    }
                });
            }

            public void bind(String floorId) {
                String displayName = getFloorDisplayName(floorId);
                floorNameText.setText(displayName);

                // Apply styling based on selection state
                boolean isSelected = floorId.equals(selectedFloorId);

                if (isSelected) {
                    // Selected floor: full green background with white text
                    floorNameText.setBackgroundResource(R.drawable.circular_floor_background_selected);
                    floorNameText.setTextColor(context.getResources().getColor(android.R.color.white, null));
                } else {
                    // Unselected floor: white background with green border and green text
                    floorNameText.setBackgroundResource(R.drawable.circular_floor_background);
                    floorNameText.setTextColor(context.getResources().getColor(R.color.becomap_primary, null));
                }

                Log.d(TAG, "Floor " + displayName + " bound - Selected: " + isSelected);
            }
        }
    }

    private String getFloorDisplayName(String floorId) {
        if (availableFloors != null) {
            for (BCMapFloor floor : availableFloors) {
                if (floor.getId().equals(floorId)) {
                    // Use short name if available, otherwise fall back to name or ID
                    String displayText = floor.getShortName();
                    if (displayText == null || displayText.trim().isEmpty()) {
                        displayText = floor.getName();
                    }
                    if (displayText == null || displayText.trim().isEmpty()) {
                        // Use elevation as fallback - similar to FloorAdapter logic
                        Double elevation = floor.getElevation();
                        if (elevation != null) {
                            if (elevation == 0.0) {
                                displayText = "GF"; // Ground Floor
                            } else if (elevation > 0) {
                                displayText = "F" + Math.round(elevation);
                            } else {
                                displayText = "B" + Math.round(Math.abs(elevation));
                            }
                        } else {
                            displayText = floorId; // Fallback to ID
                        }
                    }
                    return displayText;
                }
            }
        }
        return floorId; // Fallback to ID if floor not found
    }
}
