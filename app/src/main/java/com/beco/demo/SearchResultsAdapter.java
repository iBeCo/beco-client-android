package com.beco.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCLocation;
import com.becomap.sdk.models.BCMapFloor;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.LocationViewHolder> {

    public interface OnLocationSelectedListener {
        void onLocationSelected(BCLocation location);
    }

    private List<BCLocation> locations;
    private List<BCMapFloor> floors;
    private OnLocationSelectedListener listener;

    public SearchResultsAdapter(List<BCLocation> locations, List<BCMapFloor> floors, OnLocationSelectedListener listener) {
        this.locations = locations;
        this.floors = floors;
        this.listener = listener;
    }

    public void updateLocations(List<BCLocation> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        BCLocation location = locations.get(position);
        holder.bind(location, listener);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView locationName;
        private TextView locationDescription;
        private TextView locationDistance;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.locationName);
            locationDescription = itemView.findViewById(R.id.locationDescription);
            locationDistance = itemView.findViewById(R.id.locationDistance);
        }

        public void bind(BCLocation location, OnLocationSelectedListener listener) {
            // Set location name
            locationName.setText(location.getName());

            // Set location description
            String description = location.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                locationDescription.setText(description);
                locationDescription.setVisibility(View.VISIBLE);
            } else {
                // Fallback to category info if no description
                String fallbackInfo = buildLocationFallbackInfo(location);
                locationDescription.setText(fallbackInfo);
                locationDescription.setVisibility(View.VISIBLE);
            }

            // Hide distance for now (could be calculated later)
            locationDistance.setVisibility(View.GONE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocationSelected(location);
                }
            });
        }

        private String buildLocationFallbackInfo(BCLocation location) {
            StringBuilder info = new StringBuilder();

            // Add categories if available
            if (location.getCategories() != null && !location.getCategories().isEmpty()) {
                String categoryName = location.getCategories().get(0).getName();
                if (categoryName != null && !categoryName.isEmpty()) {
                    info.append(categoryName);
                }
            }

            // Add floor if available
            if (location.getFloorId() != null && !location.getFloorId().isEmpty()) {
                if (info.length() > 0) {
                    info.append(" • ");
                }
                String floorDisplayName = getFloorDisplayNameFromOuter(location.getFloorId());
                info.append("Floor: ").append(floorDisplayName);
            }

            // Add type if available
            if (location.getType() != null) {
                if (info.length() > 0) {
                    info.append(" • ");
                }
                info.append(location.getType().toString());
            }

            return info.length() > 0 ? info.toString() : "No description available";
        }

        /**
         * Helper method to access the floor display name from the outer class.
         */
        private String getFloorDisplayNameFromOuter(String floorId) {
            return SearchResultsAdapter.this.getFloorDisplayName(floorId);
        }
    }

    /**
     * Gets the display name for a floor (short name, name, or fallback based on elevation).
     * This matches the logic used in FloorAdapter and MainActivity.
     */
    private String getFloorDisplayName(String floorId) {
        if (floors == null || floorId == null) {
            return floorId; // Fallback to floor ID if no floor data available
        }

        // Find the floor by ID
        for (BCMapFloor floor : floors) {
            if (floorId.equals(floor.getId())) {
                // Use the same logic as FloorAdapter for consistency
                String displayText = floor.getShortName();
                if (displayText == null || displayText.trim().isEmpty()) {
                    displayText = floor.getName();
                }
                if (displayText == null || displayText.trim().isEmpty()) {
                    // Use elevation as fallback - positive for upper floors, negative for basements
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
                        displayText = "?"; // Unknown floor
                    }
                }
                return displayText;
            }
        }

        // Floor not found, return the ID as fallback
        return floorId;
    }
}
