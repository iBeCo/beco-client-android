package com.beco.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryFilterAdapter extends RecyclerView.Adapter<CategoryFilterAdapter.CategoryViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(String categoryId);
    }

    private List<BCCategory> categories;
    private OnCategorySelectedListener listener;
    private String selectedCategoryId = null;

    public CategoryFilterAdapter(List<BCCategory> categories, OnCategorySelectedListener listener) {
        this.categories = new ArrayList<>();
        
        // Add "All" option at the beginning
        BCCategory allCategory = new BCCategory();
        allCategory.setId(null);
        allCategory.setName("All");
        this.categories.add(allCategory);
        
        // Add actual categories
        if (categories != null) {
            this.categories.addAll(categories);
        }
        
        this.listener = listener;
    }

    public void setSelectedCategory(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_filter, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        BCCategory category = categories.get(position);
        boolean isSelected = (selectedCategoryId == null && category.getId() == null) ||
                           (selectedCategoryId != null && selectedCategoryId.equals(category.getId()));
        holder.bind(category, isSelected, listener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView categoryIcon;
        private TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }

        public void bind(BCCategory category, boolean isSelected, OnCategorySelectedListener listener) {
            // Set category name
            categoryName.setText(category.getName());

            // Set category icon based on iconName
            int iconResource = getCategoryIcon(category.getIconName());
            categoryIcon.setImageResource(iconResource);

            // Set selected state
            itemView.setSelected(isSelected);

            // Update colors based on selection
            if (isSelected) {
                categoryName.setTextColor(itemView.getContext().getColor(android.R.color.white));
                categoryIcon.setColorFilter(itemView.getContext().getColor(android.R.color.white));
            } else {
                categoryName.setTextColor(itemView.getContext().getColor(R.color.text_secondary));
                categoryIcon.setColorFilter(itemView.getContext().getColor(R.color.text_secondary));
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected(category.getId());
                }
            });
        }

        private int getCategoryIcon(String iconName) {
            if (iconName == null) {
                return R.drawable.ic_category_generic;
            }

            // Map iconName to appropriate drawable resource
            switch (iconName.toLowerCase()) {
                case "restaurant":
                case "food":
                case "dining":
                case "cafe":
                case "coffee":
                    return R.drawable.ic_category_restaurant;

                case "shopping":
                case "retail":
                case "store":
                case "shop":
                    return R.drawable.ic_category_shopping;

                case "services":
                case "service":
                case "bank":
                case "atm":
                case "information":
                    return R.drawable.ic_category_services;

                case "entertainment":
                case "cinema":
                case "movie":
                case "theater":
                case "games":
                    return R.drawable.ic_category_entertainment;

                default:
                    return R.drawable.ic_category_generic;
            }
        }
    }
}
