package party.treesquaredcode.android.makerhawaii;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import party.treesquaredcode.android.flipanimation.FlipAnimation;
import party.treesquaredcode.android.makerhawaii.api.ApiClient;
import party.treesquaredcode.android.makerhawaii.api.MakerSpace;
import party.treesquaredcode.android.makerhawaii.util.Formatting;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    Location userLocation;
    boolean canGetLocation;
    Map<Marker, MakerSpace> makerMapping = new HashMap<>();
    Map<MakerSpace, Marker> makerInverseMapping = new HashMap<>();

    View mapViewContainer;
    View listViewContainer;
    boolean displayingList;
    Button button;

    Adapter adapter;
    RecyclerView recyclerView;

    Marker lastMarker;

    View searchContainer;

    List<MakerSpaceWithProximity> makerSpaceWithProximityList = new ArrayList<>();
    List<MakerSpaceWithProximity> searchResultList = new ArrayList<>();

    SearchAdapter searchAdapter;
    RecyclerView searchRecyclerView;

    EditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchContainer = findViewById(R.id.search_container);
        searchAdapter = new SearchAdapter();
        searchRecyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(null, LinearLayoutManager.VERTICAL, false));
        searchRecyclerView.setAdapter(searchAdapter);
        findViewById(R.id.search_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearch();
            }
        });

        mapViewContainer = findViewById(R.id.map);
        listViewContainer = findViewById(R.id.list_container);
        button = (Button) findViewById(R.id.button);

        searchField = (EditText) findViewById(R.id.search_field);

        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    showSearch(searchField.getText().toString());
                    View view = MapsActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    return true;

                }
                return false;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (displayingList) {
                    showMap();
                } else {
                    showList();
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new Adapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(null, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = locationManager.getProviders(true);
        String providerToUse = null;
        for (String provider : providers) {
            Log.d("HERPDERP", "provider: " + provider);
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                providerToUse = provider;
            }
        }

        if (providerToUse != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            } else {
                userLocation = locationManager.getLastKnownLocation(providerToUse);
                Log.d("HERPDERP", "got user location: (" + userLocation.getLatitude() + ", " + userLocation.getLongitude() + ")");
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (userLocation != null) {
            LatLng userLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLng, 12.0f));
        }


        ApiClient.getSharedInstance().getMakerSpaceList(new ApiClient.SuccessListener<List<MakerSpace>>() {
            @Override
            public void onSuccess(List<MakerSpace> makerSpaces) {
                Log.d("HERPDERP", "success!");
                for (MakerSpace makerSpace : makerSpaces) {
                    Log.d("HERPDERP", "maker space with name: " + makerSpace.getName() + ", website: " + makerSpace.getWebsite() + ", address: " + makerSpace.getLocation().getAddress() + " (" + makerSpace.getLocation().getLat() + ", " + makerSpace.getLocation().getLng() + ")");
                    try {
                        double lat = Double.valueOf(makerSpace.getLocation().getLat());
                        double lng = Double.valueOf(makerSpace.getLocation().getLng());
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(makerSpace.getName()));
                        makerMapping.put(marker, makerSpace);
                        makerInverseMapping.put(makerSpace, marker);
                    } catch (NumberFormatException e) {
                        //no-op
                    }
                }
                if (userLocation != null) {
                    List<MakerSpaceWithProximity> makerSpaceWithProximityList = new ArrayList<MakerSpaceWithProximity>();
                    for (MakerSpace makerSpace : makerSpaces) {
                        makerSpaceWithProximityList.add(new MakerSpaceWithProximity(makerSpace, userLocation));
                    }
                    Collections.sort(makerSpaceWithProximityList, new Comparator<MakerSpaceWithProximity>() {
                        @Override
                        public int compare(MakerSpaceWithProximity lhs, MakerSpaceWithProximity rhs) {
                            float lhsProx = lhs.proximity;
                            float rhsProx = rhs.proximity;
                            if (lhsProx < rhsProx) {
                                return -1;
                            } else if (rhsProx < lhsProx) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                    MapsActivity.this.makerSpaceWithProximityList = makerSpaceWithProximityList;
                    adapter.notifyDataSetChanged();
                }
            }
        }, new ApiClient.FailureListener() {
            @Override
            public void onFailure() {
                Log.d("HERPDERP", "failure!");
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                MakerSpace makerSpace = makerMapping.get(marker);
                if (makerSpace != null) {
                    showMakerSpaceDetail(makerSpace);
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                lastMarker = marker;
                return false;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        } else {
            mMap.setMyLocationEnabled(true);
        }
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    void showMap() {
        FlipAnimation
                .animation()
                .withOutView(listViewContainer)
                .withInView(mapViewContainer)
                .withDurationMillis(500L)
                .withDirection(FlipAnimation.Direction.RIGHT)
                .withAnimationListener(new FlipAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(FlipAnimation flipAnimation) {
                        button.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(FlipAnimation flipAnimation) {
                        button.setEnabled(true);
                        button.setText("list");
                        displayingList = false;
                    }

                    @Override
                    public void onAnimationFailed() {

                    }
                })
                .run();
    }

    void showMapAtMakerSpaceWithLatLng(final MakerSpace makerSpace, final double lat, final double lng) {
        FlipAnimation
                .animation()
                .withOutView(listViewContainer)
                .withInView(mapViewContainer)
                .withDurationMillis(500L)
                .withDirection(FlipAnimation.Direction.RIGHT)
                .withAnimationListener(new FlipAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(FlipAnimation flipAnimation) {
                        button.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(FlipAnimation flipAnimation) {
                        if (lastMarker != null) {
                            lastMarker.hideInfoWindow();
                        }
                        button.setEnabled(true);
                        button.setText("list");
                        displayingList = false;
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                        Marker marker = makerInverseMapping.get(makerSpace);
                        if (marker != null) {
                            marker.showInfoWindow();
                            lastMarker = marker;
                        }
                    }

                    @Override
                    public void onAnimationFailed() {

                    }
                })
                .run();
    }

    void showList() {
        FlipAnimation
                .animation()
                .withOutView(mapViewContainer)
                .withInView(listViewContainer)
                .withDurationMillis(500L)
                .withDirection(FlipAnimation.Direction.LEFT)
                .withAnimationListener(new FlipAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(FlipAnimation flipAnimation) {
                        button.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(FlipAnimation flipAnimation) {
                        button.setEnabled(true);
                        button.setText("map");
                        displayingList = true;
                    }

                    @Override
                    public void onAnimationFailed() {

                    }
                })
                .run();
    }

    void showMakerSpaceDetail(MakerSpace makerSpace) {
        Intent intent = new Intent(MapsActivity.this, MakerSpaceDetailActivity.class);
        intent.putExtra(MakerSpaceDetailActivity.NAME_EXTRA_ID, makerSpace.getName());
        intent.putExtra(MakerSpaceDetailActivity.ADDRESS_EXTRA_ID, makerSpace.getLocation().getAddress());
        intent.putExtra(MakerSpaceDetailActivity.CLASSES_EXTRA_ID, makerSpace.getClasses());
        intent.putExtra(MakerSpaceDetailActivity.HOW_TO_VISIT_EXTRA_ID, makerSpace.getHowToVisit());
        intent.putExtra(MakerSpaceDetailActivity.MEMBERSHIP_INFO_EXTRA_ID, makerSpace.getMembershipInfo());
        intent.putExtra(MakerSpaceDetailActivity.WEBSITE_EXTRA_ID, makerSpace.getWebsite());
        startActivity(intent);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MakerSpaceWithProximity makerSpaceWithProximity;
        TextView titleText;
        TextView proximityText;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            proximityText = (TextView) itemView.findViewById(R.id.proximity_text);
            itemView.findViewById(R.id.detail_text).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMakerSpaceDetail(makerSpaceWithProximity.makerSpace);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMapAtMakerSpaceWithLatLng(makerSpaceWithProximity.makerSpace, makerSpaceWithProximity.lat, makerSpaceWithProximity.lng);
                }
            });
        }

        void bind(MakerSpaceWithProximity makerSpaceWithProximity) {
            this.makerSpaceWithProximity = makerSpaceWithProximity;
            titleText.setText(makerSpaceWithProximity.makerSpace.getName());
            proximityText.setText(Formatting.distanceString(makerSpaceWithProximity.proximity));
        }
    }

    class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(MapsActivity.this).inflate(R.layout.layout__list_row, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(makerSpaceWithProximityList.get(position));
        }

        @Override
        public int getItemCount() {
            return makerSpaceWithProximityList.size();
        }
    }

    static class MakerSpaceWithProximity {
        MakerSpace makerSpace;
        float proximity;
        double lat;
        double lng;

        public MakerSpaceWithProximity(MakerSpace makerSpace, Location userLocation) {
            this.makerSpace = makerSpace;
            float results[] = new float[1];
            try {
                lat = Double.valueOf(makerSpace.getLocation().getLat());
                lng = Double.valueOf(makerSpace.getLocation().getLng());
                Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), lat, lng, results);
                this.proximity = results[0];
            } catch (NumberFormatException e) {

            }
        }
    }

    List<MakerSpaceWithProximity> filteredSpaces(List<MakerSpaceWithProximity> makerSpaceWithProximityList, String query) {
        String[] queryWords = query.split("\\s");
        List<MakerSpaceWithProximity> result = new ArrayList<>();
        if (queryWords.length == 0) {
            return result;
        }
        for (MakerSpaceWithProximity makerSpaceWithProximity : makerSpaceWithProximityList) {
            String normName = makerSpaceWithProximity.makerSpace.getName().toLowerCase();
            boolean matches = false;
            for (String queryWord : queryWords) {
                if (normName.contains(queryWord.toLowerCase())) {
                    matches = true;
                }
            }
            if (matches) {
                result.add(makerSpaceWithProximity);
            }
        }
        return result;
    }

    void hideSearch() {
        searchContainer.setVisibility(View.GONE);
    }

    void showSearch(String searchQuery) {
        searchContainer.setVisibility(View.VISIBLE);
        searchResultList = filteredSpaces(makerSpaceWithProximityList, searchQuery);
        searchAdapter.notifyDataSetChanged();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        MakerSpaceWithProximity makerSpaceWithProximity;
        TextView titleText;
        TextView proximityText;

        public SearchViewHolder(View itemView) {
            super(itemView);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            proximityText = (TextView) itemView.findViewById(R.id.proximity_text);
            itemView.findViewById(R.id.detail_text).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSearch();
                    showMakerSpaceDetail(makerSpaceWithProximity.makerSpace);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSearch();
                    showMapAtMakerSpaceWithLatLng(makerSpaceWithProximity.makerSpace, makerSpaceWithProximity.lat, makerSpaceWithProximity.lng);
                }
            });
        }

        void bind(MakerSpaceWithProximity makerSpaceWithProximity) {
            this.makerSpaceWithProximity = makerSpaceWithProximity;
            titleText.setText(makerSpaceWithProximity.makerSpace.getName());
            proximityText.setText(Formatting.distanceString(makerSpaceWithProximity.proximity));
        }
    }

    class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
        @Override
        public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SearchViewHolder(LayoutInflater.from(MapsActivity.this).inflate(R.layout.layout__list_row, parent, false));
        }

        @Override
        public void onBindViewHolder(SearchViewHolder holder, int position) {
            holder.bind(searchResultList.get(position));
        }

        @Override
        public int getItemCount() {
            return searchResultList.size();
        }
    }
}
