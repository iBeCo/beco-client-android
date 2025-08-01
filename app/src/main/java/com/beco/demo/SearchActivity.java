package com.beco.demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.becomap.sdk.models.BCCategory;
import com.becomap.sdk.models.BCLocation;
import com.becomap.sdk.models.BCMapFloor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity implements
        SearchResultsAdapter.OnLocationSelectedListener,
        CategoryFilterAdapter.OnCategorySelectedListener {

    // Intent extras
    public static final String EXTRA_SELECTED_LOCATION = "selected_location";

    // Static data storage (fallback for serialization issues)
    private static List<BCLocation> staticLocations;
    private static List<BCCategory> staticCategories;
    private static List<BCMapFloor> staticFloors;
    
    // Views
    private ImageView backButton;
    private EditText searchInput;
    private ImageView clearButton;
    private RecyclerView categoryFilterRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private LinearLayout emptyStateContainer;
    
    // Data
    private List<BCLocation> allLocations;
    private List<BCCategory> allCategories;
    private List<BCMapFloor> allFloors;
    private List<BCLocation> filteredLocations;
    private String selectedCategoryId = null;
    
    // Adapters
    private SearchResultsAdapter searchResultsAdapter;
    private CategoryFilterAdapter categoryFilterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_search);

            initializeViews();
            setupRecyclerViews();
            setupSearchFunctionality();
            loadDataFromIntent();

            Log.d("SearchActivity", "SearchActivity created successfully");
        } catch (Exception e) {
            Log.e("SearchActivity", "Error creating SearchActivity", e);
            finish(); // Close activity if there's an error
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        searchInput = findViewById(R.id.searchInput);
        clearButton = findViewById(R.id.clearButton);
        categoryFilterRecyclerView = findViewById(R.id.categoryFilterRecyclerView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        // Set up back button
        backButton.setOnClickListener(v -> finish());

        // Set up clear button
        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
            clearButton.setVisibility(View.GONE);
        });

        // Focus on search input
        searchInput.requestFocus();
    }

    private void setupRecyclerViews() {
        // Setup category filter (horizontal)
        categoryFilterRecyclerView.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        
        // Setup search results (vertical)
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSearchFunctionality() {
        // Search on text change
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button
                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                // Perform search
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Search on keyboard action
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchInput.getText().toString());
                return true;
            }
            return false;
        });
    }

    public static void setSearchData(List<BCLocation> locations, List<BCCategory> categories, List<BCMapFloor> floors) {
        staticLocations = locations;
        staticCategories = categories;
        staticFloors = floors;
    }

    private void loadDataFromIntent() {
        Log.d("SearchActivity", "Loading search data...");

        // Use static data (more reliable than Intent extras for large objects)
        allLocations = staticLocations != null ? staticLocations : new ArrayList<>();
        allCategories = staticCategories != null ? staticCategories : new ArrayList<>();
        allFloors = staticFloors != null ? staticFloors : new ArrayList<>();

        Log.d("SearchActivity", "Loaded " + allLocations.size() + " locations, " + allCategories.size() + " categories, and " + allFloors.size() + " floors");

        // Initialize filtered locations
        filteredLocations = new ArrayList<>(allLocations);

        // Setup adapters with data
        setupAdapters();

        // Initial display
        updateSearchResults();
    }

    private void setupAdapters() {
        try {
            // Category filter adapter
            if (allCategories != null && categoryFilterRecyclerView != null) {
                categoryFilterAdapter = new CategoryFilterAdapter(allCategories, this);
                categoryFilterRecyclerView.setAdapter(categoryFilterAdapter);
                Log.d("SearchActivity", "Category filter adapter set up with " + allCategories.size() + " categories");
            }

            // Search results adapter
            if (filteredLocations != null && searchResultsRecyclerView != null) {
                searchResultsAdapter = new SearchResultsAdapter(filteredLocations, allFloors, this);
                searchResultsRecyclerView.setAdapter(searchResultsAdapter);
                Log.d("SearchActivity", "Search results adapter set up with " + filteredLocations.size() + " locations");
            }
        } catch (Exception e) {
            Log.e("SearchActivity", "Error setting up adapters", e);
        }
    }

    private void performSearch(String query) {
        if (allLocations == null) return;

        // Filter locations based on search query and selected category
        filteredLocations = allLocations.stream()
            .filter(location -> {
                // Text search filter
                boolean matchesQuery = query.isEmpty() ||
                    location.getName().toLowerCase().contains(query.toLowerCase());

                // Category filter
                boolean matchesCategory = selectedCategoryId == null ||
                    locationMatchesCategory(location, selectedCategoryId);

                return matchesQuery && matchesCategory;
            })
            .collect(Collectors.toList());

        updateSearchResults();
    }

    private void updateSearchResults() {
        if (searchResultsAdapter != null) {
            searchResultsAdapter.updateLocations(filteredLocations);
        }

        // Show/hide empty state
        if (filteredLocations.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private boolean locationMatchesCategory(BCLocation location, String categoryId) {
        if (location.getCategories() == null || location.getCategories().isEmpty()) {
            return false;
        }

        for (BCCategory category : location.getCategories()) {
            if (categoryId.equals(category.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLocationSelected(BCLocation location) {
        // Return selected location ID to MainActivity (since BCLocation is not Serializable)
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SELECTED_LOCATION, location.getId());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onCategorySelected(String categoryId) {
        selectedCategoryId = categoryId;
        
        // Update category filter UI
        categoryFilterAdapter.setSelectedCategory(categoryId);
        
        // Re-filter results
        performSearch(searchInput.getText().toString());
    }
}
