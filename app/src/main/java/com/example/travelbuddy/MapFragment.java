package com.example.travelbuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String GOOGLE_MAPS_API_KEY = "YOUR_API_KEY_HERE"; // Replace with your API key

    // UI Components
    private GoogleMap mMap;
    private EditText etCurrentLocation, etDestination;
    private Button btnSearchRoute, btnStartTrip, btnShareLocation;
    private TextView tvRouteInfo;

    // Location and Navigation
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng currentLocation, destinationLocation;
    private Marker currentLocationMarker, destinationMarker;
    private Polyline routePolyline;
    private List<LatLng> routePoints;

    // Trip Management
    private boolean isTripStarted = false;
    private boolean isLocationSharingEnabled = false;
    private Handler locationSharingHandler;
    private Runnable locationSharingRunnable;

    // Firebase for live location sharing
    private DatabaseReference locationRef;
    private String userId = "user123"; // Replace with actual user ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        initializeViews(view);
        setupLocationServices();
        setupMapFragment();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        etCurrentLocation = view.findViewById(R.id.et_current_location);
        etDestination = view.findViewById(R.id.et_destination);
        btnSearchRoute = view.findViewById(R.id.btn_search_route);
        btnStartTrip = view.findViewById(R.id.btn_start_trip);
        btnShareLocation = view.findViewById(R.id.btn_share_location);
        tvRouteInfo = view.findViewById(R.id.tv_route_info);

        // Initially disable trip controls
        btnStartTrip.setEnabled(false);
        btnShareLocation.setEnabled(false);
    }

    private void setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Create location request
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000) // 5 seconds
                .setFastestInterval(2000); // 2 seconds

        // Location callback for real-time updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateCurrentLocation(new LatLng(location.getLatitude(), location.getLongitude()));

                    // Share location if enabled
                    if (isLocationSharingEnabled) {
                        shareLocationWithParents(location);
                    }

                    // Update navigation if trip is started
                    if (isTripStarted) {
                        updateNavigationProgress(location);
                    }
                }
            }
        };

        // Initialize Firebase reference
        locationRef = FirebaseDatabase.getInstance().getReference("live_locations").child(userId);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        btnSearchRoute.setOnClickListener(v -> searchRoute());
        btnStartTrip.setOnClickListener(v -> startTrip());
        btnShareLocation.setOnClickListener(v -> toggleLocationSharing());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable location layer if permission granted
        if (checkLocationPermission()) {
            enableMyLocation();
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Map click listener for destination selection
        mMap.setOnMapClickListener(latLng -> {
            if (!isTripStarted) {
                setDestinationFromMap(latLng);
            }
        });
    }

    private void searchRoute() {
        String currentLocationText = etCurrentLocation.getText().toString().trim();
        String destinationText = etDestination.getText().toString().trim();

        if (currentLocationText.isEmpty() || destinationText.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both locations", Toast.LENGTH_SHORT).show();
            return;
        }

        // Geocode the addresses
        geocodeAddress(currentLocationText, true);
        geocodeAddress(destinationText, false);
    }

    private void geocodeAddress(String address, boolean isCurrentLocation) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (isCurrentLocation) {
                    currentLocation = latLng;
                    updateCurrentLocationMarker();
                } else {
                    destinationLocation = latLng;
                    updateDestinationMarker();

                    // Get route when both locations are set
                    if (currentLocation != null) {
                        getDirections();
                    }
                }
            } else {
                Toast.makeText(getContext(), "Address not found: " + address, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error", e);
            Toast.makeText(getContext(), "Error finding address", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateCurrentLocationMarker();

                        // Reverse geocode to get address
                        reverseGeocode(currentLocation, true);
                    }
                });
    }

    private void reverseGeocode(LatLng latLng, boolean isCurrentLocation) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                if (isCurrentLocation) {
                    etCurrentLocation.setText(address);
                } else {
                    etDestination.setText(address);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Reverse geocoding error", e);
        }
    }

    private void setDestinationFromMap(LatLng latLng) {
        destinationLocation = latLng;
        updateDestinationMarker();
        reverseGeocode(latLng, false);

        if (currentLocation != null) {
            getDirections();
        }
    }

    private void updateCurrentLocationMarker() {
        if (mMap != null && currentLocation != null) {
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
            }

            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
    }

    private void updateDestinationMarker() {
        if (mMap != null && destinationLocation != null) {
            if (destinationMarker != null) {
                destinationMarker.remove();
            }

            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(destinationLocation)
                    .title("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private void getDirections() {
        if (currentLocation == null || destinationLocation == null) return;

        String url = buildDirectionsUrl();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error getting directions", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    requireActivity().runOnUiThread(() -> parseDirectionsResponse(jsonData));
                }
            }
        });
    }

    private String buildDirectionsUrl() {
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + currentLocation.latitude + "," + currentLocation.longitude +
                "&destination=" + destinationLocation.latitude + "," + destinationLocation.longitude +
                "&key=" + GOOGLE_MAPS_API_KEY;
    }

    private void parseDirectionsResponse(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);

                // Extract route information
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                // Extract polyline points
                String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");
                routePoints = decodePolyline(encodedPolyline);

                // Update UI
                displayRouteInfo(distance, duration);
                drawRoute();
                btnStartTrip.setEnabled(true);

            } else {
                Toast.makeText(getContext(), "No route found", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing directions", e);
            Toast.makeText(getContext(), "Error parsing route data", Toast.LENGTH_SHORT).show();
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private void displayRouteInfo(String distance, String duration) {
        String info = "Distance: " + distance + "\nEstimated Time: " + duration;
        tvRouteInfo.setText(info);
        tvRouteInfo.setVisibility(View.VISIBLE);
    }

    private void drawRoute() {
        if (mMap != null && routePoints != null) {
            // Remove existing route
            if (routePolyline != null) {
                routePolyline.remove();
            }

            // Draw new route
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .width(8)
                    .color(getResources().getColor(android.R.color.holo_blue_dark));

            routePolyline = mMap.addPolyline(polylineOptions);

            // Adjust camera to show entire route
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLocation);
            builder.include(destinationLocation);
            LatLngBounds bounds = builder.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    private void startTrip() {
        if (!isTripStarted) {
            isTripStarted = true;
            btnStartTrip.setText("End Trip");
            btnShareLocation.setEnabled(true);

            // Start location updates
            startLocationUpdates();

            Toast.makeText(getContext(), "Trip started! Navigation active.", Toast.LENGTH_SHORT).show();
        } else {
            endTrip();
        }
    }

    private void endTrip() {
        isTripStarted = false;
        btnStartTrip.setText("Start Trip");
        btnStartTrip.setEnabled(false);

        // Stop location updates
        stopLocationUpdates();

        // Stop location sharing
        if (isLocationSharingEnabled) {
            toggleLocationSharing();
        }

        // Clear route and markers
        clearMap();

        Toast.makeText(getContext(), "Trip ended.", Toast.LENGTH_SHORT).show();
    }

    private void toggleLocationSharing() {
        if (!isLocationSharingEnabled) {
            isLocationSharingEnabled = true;
            btnShareLocation.setText("Stop Sharing");
            btnShareLocation.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));

            Toast.makeText(getContext(), "Location sharing with parents started", Toast.LENGTH_SHORT).show();
        } else {
            isLocationSharingEnabled = false;
            btnShareLocation.setText("Share Location");
            btnShareLocation.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

            // Remove location from Firebase
            locationRef.removeValue();

            Toast.makeText(getContext(), "Location sharing stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateCurrentLocation(LatLng newLocation) {
        currentLocation = newLocation;

        if (currentLocationMarker != null) {
            currentLocationMarker.setPosition(newLocation);
        }

        // Center camera on current location during navigation
        if (isTripStarted) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18));
        }
    }

    private void updateNavigationProgress(Location location) {
        // Calculate distance to destination
        if (destinationLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    destinationLocation.latitude, destinationLocation.longitude,
                    results
            );

            float distanceToDestination = results[0];

            // Check if arrived (within 50 meters)
            if (distanceToDestination < 50) {
                Toast.makeText(getContext(), "You have arrived at your destination!", Toast.LENGTH_LONG).show();
                endTrip();
            } else {
                // Update route info with remaining distance
                String remainingDistance = String.format(Locale.getDefault(), "%.1f km remaining", distanceToDestination / 1000);
                tvRouteInfo.setText(tvRouteInfo.getText() + "\n" + remainingDistance);
            }
        }
    }

    private void shareLocationWithParents(Location location) {
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("timestamp", System.currentTimeMillis());
        locationData.put("accuracy", location.getAccuracy());
        locationData.put("speed", location.getSpeed());

        // Add trip status
        locationData.put("isOnTrip", isTripStarted);
        if (destinationLocation != null) {
            locationData.put("destinationLat", destinationLocation.latitude);
            locationData.put("destinationLng", destinationLocation.longitude);
        }

        locationRef.setValue(locationData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location shared successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to share location", e));
    }

    private void clearMap() {
        if (mMap != null) {
            if (currentLocationMarker != null) {
                currentLocationMarker.remove();
                currentLocationMarker = null;
            }
            if (destinationMarker != null) {
                destinationMarker.remove();
                destinationMarker = null;
            }
            if (routePolyline != null) {
                routePolyline.remove();
                routePolyline = null;
            }
        }

        // Clear UI
        etCurrentLocation.setText("");
        etDestination.setText("");
        tvRouteInfo.setVisibility(View.GONE);

        // Reset variables
        currentLocation = null;
        destinationLocation = null;
        routePoints = null;
    }

    private void enableMyLocation() {
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required for navigation", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTripStarted) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationSharingHandler != null) {
            locationSharingHandler.removeCallbacks(locationSharingRunnable);
        }
    }
}