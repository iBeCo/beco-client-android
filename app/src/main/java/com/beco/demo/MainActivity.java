package com.beco.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.becomap.sdk.BCMapView;
import com.becomap.sdk.exceptions.BCLocationNotFoundException;
import com.becomap.sdk.exceptions.BCMapException;
import com.becomap.sdk.listeners.BCMapViewListener;
import com.becomap.sdk.listeners.BCRouteListener;
import com.becomap.sdk.models.BCBuilding;
import com.becomap.sdk.models.BCCategory;
import com.becomap.sdk.models.BCInitErrorCode;
import com.becomap.sdk.models.BCLocation;
import com.becomap.sdk.models.BCMapFloor;
import com.becomap.sdk.models.BCMapOptions;
import com.becomap.sdk.models.BCMapViewOptions;
import com.becomap.sdk.models.BCRoute;
import com.becomap.sdk.models.BCRouteErrorCode;
import com.becomap.sdk.models.BCSite;
import com.beco.demo.components.FloorSwitcherComponent;
import com.beco.demo.components.LocationDetailsCard;
import com.beco.demo.components.RouteInfoBarComponent;
import com.beco.demo.components.SearchBarComponent;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Logging tag
    private static final String TAG = "MainActivity";

    // Configuration constants
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String SITE_IDENTIFIER = "site-identifier";

    // Views
    private FrameLayout contentContainer;
    private BCMapView mapView;
    private LinearLayout loadingOverlay;

    // Components
    private SearchBarComponent searchBarComponent;
    private FloorSwitcherComponent floorSwitcherComponent;
    private LocationDetailsCard locationDetailsCard;
    private RouteInfoBarComponent routeInfoBarComponent;

    // Data
    private List<BCMapFloor> floors;
    private BCBuilding firstBuilding;
    private List<BCLocation> cachedLocations;
    private List<BCCategory> cachedCategories;

    // Activity result launcher for search
    private ActivityResultLauncher<Intent> searchActivityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupActivityResultLauncher();
        setupMapView();
        loadMap();
    }

    private void initializeViews() {
        // Initialize main views
        contentContainer = findViewById(R.id.contentContainer);
        mapView = findViewById(R.id.mapView);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Initialize components
        initializeSearchBarComponent();
        initializeFloorSwitcherComponent();
        initializeLocationDetailsCard();
        initializeRouteInfoBarComponent();
    }

    private void initializeSearchBarComponent() {
        // Create the search bar component - it will inflate its own layout
        searchBarComponent = new SearchBarComponent(this, contentContainer);

        // Set up the listener
        searchBarComponent.setListener(new SearchBarComponent.SearchBarListener() {
            @Override
            public void onSearchRequested() {
                launchSearchActivity();
            }

            @Override
            public void onSpeechRequested() {
                // TODO: Implement voice search functionality
                Log.d(TAG, "Speech/Voice search requested");
                // For now, just launch the search activity
                launchSearchActivity();
            }

            @Override
            public void onSourceLocationRequested() {
                // Launch search activity to select source location
                Log.d(TAG, "Source location selection requested");
                launchSearchActivity();
            }

            @Override
            public void onDestinationLocationRequested() {
                // Launch search activity to select destination location
                Log.d(TAG, "Destination location selection requested");
                launchSearchActivity();
            }

            @Override
            public void onLocationsSwapped(BCLocation newSource, BCLocation newDestination) {
                Log.d(TAG, "Locations swapped - Source: " +
                    (newSource != null ? newSource.getName() : "null") +
                    ", Destination: " + (newDestination != null ? newDestination.getName() : "null"));
                // TODO: Update map display or routing if needed
            }
        });
    }

    private void initializeFloorSwitcherComponent() {
        // Create the floor switcher component - it will inflate its own layout
        floorSwitcherComponent = new FloorSwitcherComponent(this, contentContainer);

        // Set up the listener
        floorSwitcherComponent.setListener(new FloorSwitcherComponent.FloorSwitcherListener() {
            @Override
            public void onFloorSelected(BCMapFloor floor) {
                try {
                    mapView.selectFloor(floor);
                    Log.d(TAG, "Floor selected: " + floor.getName());
                } catch (BCMapException e) {
                    Log.e(TAG, "Error selecting floor", e);
                }
            }
        });
    }

    private void initializeLocationDetailsCard() {
        // Get the content container to add the card to
        FrameLayout contentContainer = findViewById(R.id.contentContainer);

        // Create the component - it will inflate its own layout
        locationDetailsCard = new LocationDetailsCard(this, contentContainer);

        // Set up the listener
        locationDetailsCard.setListener(new LocationDetailsCard.LocationDetailsListener() {
            @Override
            public void onNavigateToLocation(BCLocation location) {
                // The destination is already set from the map selection (onLocationsSelected)
                // Just clear any existing source to ensure clean state
                searchBarComponent.clearSource();

                // Launch search to select the source location
                Log.d(TAG, "Navigation requested for destination: " + location.getName() + ". Launching search for source.");
                launchSearchActivity();
            }

            @Override
            public void onCardDismissed() {
                // Clear the search bar
                searchBarComponent.clearText();

                // Clear the selection on the map
                try {
                    mapView.clearSelection();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear map selection", e);
                }
            }
        });
    }

    private void initializeRouteInfoBarComponent() {
        routeInfoBarComponent = new RouteInfoBarComponent(this, contentContainer);
        routeInfoBarComponent.setListener(new RouteInfoBarComponent.RouteInfoBarListener() {
            @Override
            public void onFloorSelected(String floorId, String floorName) {
                Log.d(TAG, "Floor selected from route info: " + floorName + " (ID: " + floorId + ")");

                // Find the floor object by ID and switch to it
                try {
                    BCSite site = mapView.getSite();

                    if (site == null || site.getBuildings() == null) {
                        Log.w(TAG, "Site data not available for floor switching");
                        Toast.makeText(MainActivity.this, "Map data not ready", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BCMapFloor targetFloor = null;

                    // Search for the floor in all buildings
                    for (BCBuilding building : site.getBuildings()) {
                        if (building.getFloors() != null) {
                            targetFloor = building.getFloors().stream()
                                    .filter(floor -> floorId.equals(floor.getId()))
                                    .findFirst()
                                    .orElse(null);

                            if (targetFloor != null) {
                                break; // exit the loop as we found the floor
                            }
                        }
                    }

                    if (targetFloor != null) {
                        mapView.selectFloor(targetFloor);
                        Log.d(TAG, "Successfully switched to floor: " + floorName);
                        Toast.makeText(MainActivity.this, "Switched to " + floorName, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Floor not found: " + floorId);
                        Toast.makeText(MainActivity.this, "Floor not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to switch to floor: " + floorName, e);
                    Toast.makeText(MainActivity.this, "Failed to switch floor", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onRouteInfoClosed() {
                Log.d(TAG, "Route info bar closed");

                // Clear the search bar component and reset to initial view
                searchBarComponent.reset();

                // Clear the route from the map
                try {
                    mapView.clearAllRoutes();
                    Log.d(TAG, "Routes cleared from map");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear routes from map", e);
                }

                // Clear map selection
                try {
                    mapView.clearSelection();
                    Log.d(TAG, "Map selection cleared");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to clear map selection", e);
                }
            }
        });

        // Set floors data if available
        if (floors != null) {
            routeInfoBarComponent.setFloors(floors);
        }
    }

    private void setupActivityResultLauncher() {
        searchActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedLocationId = result.getData()
                            .getStringExtra(SearchActivity.EXTRA_SELECTED_LOCATION);

                    if (selectedLocationId != null) {
                        handleLocationSelection(selectedLocationId);
                    }
                }
            }
        );
    }

    private void handleLocationSelection(String locationId) {
        Log.d(TAG, "Location selected from search: " + locationId);

        // Find the location object from cached locations
        if (cachedLocations != null) {
            for (BCLocation location : cachedLocations) {
                if (locationId.equals(location.getId())) {
                    try {
                        // First, select and highlight the location on the map
                        mapView.selectLocation(location);
                        Log.d(TAG, "Selected location: " + location.getName());

                        // Check if we're in routing mode (destination location already set)
                        BCLocation destinationLocation = searchBarComponent.getDestinationLocation();
                        if (destinationLocation != null && !destinationLocation.getId().equals(location.getId())) {
                            // We're selecting a source for routing (destination already exists)
                            searchBarComponent.setSourceLocation(location);
                            Log.d(TAG, "Routing mode: Source=" + location.getName() +
                                ", Destination=" + destinationLocation.getName());

                            // Automatically trigger route calculation
                            calculateRoute(location, destinationLocation);

                            // Don't show location details card in routing mode
                            // The search bar will automatically switch to routing view
                        } else {
                            // Normal single location selection
                            searchBarComponent.setDestinationLocation(location);

                            // Show the location details card
                            locationDetailsCard.showLocation(location);
                        }

                        return;
                    } catch (BCLocationNotFoundException e) {
                        Log.e(TAG, "Location not found in map cache: " + e.getMessage());
                        return;
                    } catch (BCMapException e) {
                        Log.e(TAG, "Failed to select/focus on location", e);
                        return;
                    }
                }
            }
        }

        Log.w(TAG, "Location not found in cached locations: " + locationId);
    }

    private void launchSearchActivity() {
        // Only launch if data is available
        if (cachedLocations == null || cachedCategories == null) {
            Log.w(TAG, "Search data not yet available");
            return;
        }

        Log.d(TAG, "Launching search activity with " + cachedLocations.size() + " locations, " + cachedCategories.size() + " categories, and " + (floors != null ? floors.size() : 0) + " floors");

        // Use static data approach to avoid Intent size limitations
        SearchActivity.setSearchData(cachedLocations, cachedCategories, floors);

        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchActivityLauncher.launch(searchIntent);

        // Clear focus from search input to prevent keyboard issues
        searchBarComponent.clearFocus();
    }

    private void setupMapView() {
        mapView.setListener(new BCMapViewListener() {
            @Override
            public void onRenderComplete(BCSite site) {
                // Log current site from mapView
                BCSite currentSite = mapView.getSite();
                Log.d(TAG, "Map Render Complete - Current site from mapView: " +
                    (currentSite != null ? currentSite.getSiteName() : "null"));

                // Hide loading overlay
                hideLoader();

                // Get first building and floors
                if (site.getBuildings() != null && !site.getBuildings().isEmpty()) {
                    firstBuilding = site.getBuildings().get(0);
                    floors = firstBuilding.getFloors();

                    Log.d(TAG, "Found " + (floors != null ? floors.size() : 0) + " floors");
                }

                // Create demo location
                createDemoLocation();
            }

            @Override
            public void onInitError(BCInitErrorCode errorCode) {
                Log.e(TAG, "Init Error: " + errorCode.getCode() + " - " + errorCode.getMessage());
            }

            @Override
            public void onFloorChanged(BCMapFloor floor) {
                Log.d(TAG, "Floor Changed to: " + floor.getName());
                // Update the floor switcher component
                if (floorSwitcherComponent != null) {
                    floorSwitcherComponent.updateSelectedFloor(floor);
                }
            }

            @Override
            public void onViewChange(BCMapViewOptions viewOptions, long timestamp) {
                Log.d(TAG, "=== VIEW CHANGE EVENT ===");
                Log.d(TAG, "Timestamp: " + timestamp);

                if (viewOptions != null) {
                    // Log center coordinates
                    if (viewOptions.getCenter() != null && viewOptions.getCenter().size() >= 2) {
                        double latitude = viewOptions.getCenter().get(0);
                        double longitude = viewOptions.getCenter().get(1);
                        Log.d(TAG, "Center: [" + latitude + ", " + longitude + "]");
                    }

                    // Log zoom level
                    if (viewOptions.getZoom() != null) {
                        Log.d(TAG, "Zoom: " + viewOptions.getZoom());
                    }

                    // Log bearing (rotation)
                    if (viewOptions.getBearing() != null) {
                        Log.d(TAG, "Bearing: " + viewOptions.getBearing() + "°");
                    }

                    // Log pitch (tilt)
                    if (viewOptions.getPitch() != null) {
                        Log.d(TAG, "Pitch: " + viewOptions.getPitch() + "°");
                    }

                    // Log formatted summary
                    Log.d(TAG, "View Summary: Zoom=" + viewOptions.getZoom() +
                        ", Bearing=" + viewOptions.getBearing() + "°" +
                        ", Pitch=" + viewOptions.getPitch() + "°");

                } else {
                    Log.w(TAG, "View options are null");
                }

                Log.d(TAG, "========================");
            }

            @Override
            public void onLocationsSelected(List<BCLocation> locations, long timestamp) {
                Log.d(TAG, "Locations Selected: " + locations.size() + " locations at timestamp " + timestamp);

                // Log details of selected locations
                for (int i = 0; i < locations.size(); i++) {
                    BCLocation location = locations.get(i);
                    Log.d(TAG, "Location " + i + ": " + location.getId() +
                        (location.getName() != null ? " (" + location.getName() + ")" : ""));
                }

                // Handle location selection - show card for the first location
                if (!locations.isEmpty()) {
                    BCLocation primaryLocation = locations.get(0);
                    Log.d(TAG, "Primary selected location: " + primaryLocation.getId());

                    // Check if we're in routing mode - don't overwrite destination if we are
                    BCLocation currentDestination = searchBarComponent.getDestinationLocation();
                    BCLocation currentSource = searchBarComponent.getSourceLocation();
                    boolean isInRoutingMode = (currentDestination != null && currentSource != null);

                    if (!isInRoutingMode) {
                        // Normal mode - update the search bar with the selected location
                        searchBarComponent.setDestinationLocation(primaryLocation);

                        // Show the location details card
                        locationDetailsCard.showLocation(primaryLocation);
                    }
                    // Routing mode - don't overwrite destination, preserve existing state
                } else {
                    // No locations selected - dismiss card if it's showing
                    if (locationDetailsCard.isVisible()) {
                        locationDetailsCard.dismiss();
                    }
                }
            }

            @Override
            public void onAppDataLoaded() {
                Log.d(TAG, "All app data loaded");

                // Cache the data for search functionality
                cachedCategories = mapView.getCategories();
                cachedLocations = mapView.getLocations();

                if (cachedCategories != null) {
                    Log.d(TAG, "Categories cached: " + cachedCategories.size());
                }

                if (cachedLocations != null) {
                    Log.d(TAG, "Locations cached: " + cachedLocations.size());
                }

                // Log available amenity types
                List<String> amenityTypes = mapView.getAvailableAmenityTypes();
                Log.d(TAG, "Available amenity types: " + amenityTypes.toString());

                // Show floor switcher if floors are available
                if (floors != null && !floors.isEmpty()) {
                    floorSwitcherComponent.setFloors(floors);
                    floorSwitcherComponent.show();

                    // Update with first floor as selected
                    if (!floors.isEmpty()) {
                        floorSwitcherComponent.updateSelectedFloor(floors.get(0));
                    }

                    // Set floors data for route info bar component
                    if (routeInfoBarComponent != null) {
                        routeInfoBarComponent.setFloors(floors);
                    }
                }

                // Search functionality is now ready
                Log.d(TAG, "Search functionality is now available");
            }
        });

        // Setup route listener
        setupRouteListener();
    }

    private void createDemoLocation() {
        if (firstBuilding != null && floors != null && !floors.isEmpty()) {
            BCLocation location = new BCLocation();
            location.setId("dummy");
            location.setName("Demo Spot");
            location.setType(com.becomap.sdk.models.BCLocationType.TENANT);
            BCMapFloor firstFloor = floors.get(0);
            location.setFloorId(firstFloor.getId());
        }
    }

    private void hideLoader() {
        if (loadingOverlay != null) {
            // Fade out animation
            loadingOverlay.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        loadingOverlay.setVisibility(View.GONE);
                        // Show content (map and search bar)
                        showContent();
                    })
                    .start();
        }
    }

    private void showContent() {
        if (contentContainer != null) {
            contentContainer.setVisibility(View.VISIBLE);
            // Fade in animation
            contentContainer.setAlpha(0f);
            contentContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }



    private void loadMap() {
        // Initialize map
        BCMapOptions.Init initOptions = BCMapOptions.Init.builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setSiteIdentifier(SITE_IDENTIFIER)
                .build();

        BCMapOptions.SiteOptions siteOptions = BCMapOptions.SiteOptions.builder()
                .setBackgroundColor("#E5E5E5")
                .build();


        mapView.initialiseMap(initOptions, siteOptions);
    }

    private void changeViewport() throws BCMapException {
        BCMapViewOptions options = new BCMapViewOptions();
        options.setCenter(Arrays.asList(12.9716, 77.5946));
        options.setZoom(19.0);
        options.setBearing(0.0);
        options.setPitch(45.0);

        mapView.setViewport(options);
    }

    private void resetViewport() throws BCMapException {
        mapView.resetDefaultViewport();
    }

    private void updateZoomLevel(double zoom) throws BCMapException {
        mapView.updateZoom(zoom);
    }

    private void updateBearing(double bearing) throws BCMapException {
        mapView.updateBearing(bearing);
    }

    private void updatePitch(double pitch) throws BCMapException {
        mapView.updatePitch(pitch);
    }

    // ================================================================================================
    // ROUTE FUNCTIONALITY
    // ================================================================================================

    private void setupRouteListener() {
        mapView.setRouteListener(new BCRouteListener() {
            @Override
            public void onRouteCalculated(List<BCRoute> routes) {
                Log.d(TAG, "Route calculated successfully!");
                Log.d(TAG, "Received " + (routes != null ? routes.size() : 0) + " routes");

                // Process and log route details
                if (routes != null && !routes.isEmpty()) {

                    // Show route info bar for the first route
                    BCRoute primaryRoute = routes.get(0);
                    String destinationName = searchBarComponent.getDestinationLocation() != null ?
                        searchBarComponent.getDestinationLocation().getName() : "Unknown Destination";

                    runOnUiThread(() -> {
                        routeInfoBarComponent.showRoute(primaryRoute, destinationName);
                    });

                    // Automatically display the first route segment on the map
                    try {
                        Log.d(TAG, "Displaying first route segment on map...");
                        mapView.showRoute(0); // Show the first route segment (index 0)
                        Log.d(TAG, "✅ First route segment displayed on map successfully");

                        // Log route details for debugging
                        Log.d(TAG, "Route details - Distance: " + primaryRoute.getFormattedDistance() +
                              ", Time: " + primaryRoute.getFormattedTime() +
                              ", Steps: " + (primaryRoute.getSteps() != null ? primaryRoute.getSteps().size() : 0));

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Failed to display route on map", e);
                        // Don't show error to user as route info is still available
                        // The route info bar will still be shown with route details
                    }

                } else {
                    Log.w(TAG, "No routes received or routes list is empty");
                }
            }

            @Override
            public void onError(BCRouteErrorCode errorCode) {
                Log.e(TAG, "Route calculation failed: " + errorCode.getCode() + " - " + errorCode.getMessage());

                // Show user-friendly error message based on error code
                runOnUiThread(() -> {
                    String userMessage;
                    switch (errorCode) {
                        case NO_ROUTE_FOUND:
                            userMessage = "No route found between selected locations. Please try different locations.";
                            break;
                        case INVALID_PARAMETERS:
                            userMessage = "Invalid route parameters. Please check your selections.";
                            break;
                        case ROUTE_DATA_CORRUPTION:
                            userMessage = "Route data is corrupted. Please try again.";
                            break;
                        case ROUTE_CONTROLLER_UNAVAILABLE:
                            userMessage = "Route system is not ready. Please try again.";
                            break;
                        default:
                            userMessage = "Route calculation failed: " + errorCode.getMessage();
                            break;
                    }

                    Toast.makeText(MainActivity.this, userMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void calculateRoute(BCLocation startLocation, BCLocation endLocation) {
        Log.d(TAG, "Calculating route from '" + startLocation.getName() +
            "' to '" + endLocation.getName() + "'");

        try {
            // Show loading message
            Toast.makeText(this, "Calculating route...", Toast.LENGTH_SHORT).show();

            // Calculate route with no waypoints and default options
            mapView.getRoute(startLocation, endLocation, null, null);

            Log.d(TAG, "Route calculation initiated successfully");

        } catch (BCMapException e) {
            Log.e(TAG, "Map operation failed during route calculation", e);
            Toast.makeText(this, "Route calculation failed. Please try again.",
                Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

}