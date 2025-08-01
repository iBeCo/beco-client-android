package com.beco.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCMapFloor;

import java.util.List;

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder> {
    
    private List<BCMapFloor> floors;
    private BCMapFloor selectedFloor;
    private OnFloorSelectedListener listener;
    
    public interface OnFloorSelectedListener {
        void onFloorSelected(BCMapFloor floor);
    }
    
    public FloorAdapter(List<BCMapFloor> floors, OnFloorSelectedListener listener) {
        this.floors = floors;
        this.listener = listener;
        if (floors != null && !floors.isEmpty()) {
            this.selectedFloor = floors.get(0);
        }
    }
    
    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor, parent, false);
        return new FloorViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        BCMapFloor floor = floors.get(position);
        holder.bind(floor, floor.equals(selectedFloor));
    }
    
    @Override
    public int getItemCount() {
        return floors != null ? floors.size() : 0;
    }
    
    public void setSelectedFloor(BCMapFloor floor) {
        this.selectedFloor = floor;
        notifyDataSetChanged();
    }
    
    public BCMapFloor getSelectedFloor() {
        return selectedFloor;
    }
    
    class FloorViewHolder extends RecyclerView.ViewHolder {
        private TextView floorText;
        private View itemView;
        
        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.floorText = itemView.findViewById(R.id.floorText);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    BCMapFloor floor = floors.get(position);
                    setSelectedFloor(floor);
                    listener.onFloorSelected(floor);
                }
            });
        }
        
        public void bind(BCMapFloor floor, boolean isSelected) {
            // Display floor name, short name, or fallback
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
            floorText.setText(displayText);
            
            // Update appearance based on selection
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.selected_floor_background);
                floorText.setTextColor(itemView.getContext().getColor(android.R.color.white));
            } else {
                itemView.setBackgroundResource(R.drawable.floor_item_background);
                floorText.setTextColor(itemView.getContext().getColor(R.color.text_primary));
            }
        }
    }
}
