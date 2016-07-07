package itrans.navdrawertest;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import itrans.navdrawertest.Internet.VolleySingleton;

public class NearbyBusStops extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, AdapterView.OnItemClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    ImageButton btnSearchLocationClear;
    AutoCompleteTextView actvSearchLocation;
    FloatingActionButton Locatefab;

    //Google Maps stuff
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Circle BusStopRange;

    //Google place stuff
    String url;
    ArrayList<String> names;
    ArrayAdapter<String> adapter;
    String mapsApiKey = "AIzaSyBF6n8sKZwuq_kr5FXmL3k2xLO_7fz77eE";
    private String selectedPlaceId;
    ArrayList<BusStops> BusStops;

    //Internet stuff
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_bus_stops);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Locatefab = (FloatingActionButton) findViewById(R.id.Locatefab);
        btnSearchLocationClear = (ImageButton) findViewById(R.id.btnSearchLocationClear);
        actvSearchLocation = (AutoCompleteTextView) findViewById(R.id.actvSearchLocation);
        actvSearchLocation.setThreshold(1);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.nearbyMap);
        mapFragment.getMapAsync(this);

        volleySingleton = VolleySingleton.getInstance();
        requestQueue = volleySingleton.getRequestQueue();

        btnSearchLocationClear.setOnClickListener(this);
        Locatefab.setOnClickListener(this);
        actvSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() <= 3) {
                    names = new ArrayList<String>();
                    updateList(editable.toString());
                }
            }
        });
        actvSearchLocation.setOnItemClickListener(this);
    }

    public void updateList(String place) {
        String input = "";

        try {
            input = "input=" + URLEncoder.encode(place, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        String output = "json";
        String parameter = input + "&key=" + mapsApiKey;

        url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameter;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray ja = response.getJSONArray("predictions");

                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject c = ja.getJSONObject(i);
                                String description = c.getString("description");
                                names.add(description);
                            }

                            adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, names) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                                    text.setTextColor(Color.BLACK);
                                    return view;
                                }
                            };
                            actvSearchLocation.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROR: " +error, Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonObjReq);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
        }

    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSearchLocationClear:
                actvSearchLocation.setText("");
                break;
            case R.id.Locatefab:
                if (mLastLocation != null) {
                    if (BusStopRange != null){
                        BusStopRange.remove();
                    }
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(17)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    CircleOptions BusRange = new CircleOptions()
                            .center(latLng)
                            .radius(500)
                            .strokeWidth(10.0f)
                            .strokeColor(Color.RED)
                            .fillColor(0x550000FF);
                    BusStopRange = map.addCircle(BusRange);

                    findBusStops(latLng);
                }else{
                    Toast.makeText(getApplicationContext(), "Please enable location service", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void findBusStops(LatLng coordinates) {
        int radius = 500;
        Double Lat = coordinates.latitude;
        Double Lon = coordinates.longitude;
        String parameters = "location="+ Lat + "," + Lon + "&radius=" + radius + "&type=bus_station&key=" + mapsApiKey;
        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + parameters;

        JsonObjectRequest NearByBusStopRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null || response.length() > 0) {
                            try {
                                JSONArray ja = response.getJSONArray("results");

                                for (int i = 0; i < ja.length(); i++) {
                                    JSONObject c = ja.getJSONObject(i);
                                    String BusStopName = c.getString("name");

                                    JSONObject coordinates = c.getJSONObject("geometry");
                                    JSONObject coordinates1 = coordinates.getJSONObject("location");
                                    Double Lat = coordinates1.getDouble("lat");
                                    String sLat = Double.toString(Lat);
                                    Double Lon = coordinates1.getDouble("lon");
                                    String sLon = Double.toString(Lon);

                                    BusStops bus  = new BusStops();
                                    bus.setName(BusStopName);
                                    bus.setLat(sLat);
                                    bus.setLon(sLon);

                                    BusStops.add(bus);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(NearByBusStopRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nearby_bus_stops_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //For back button...
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_radius:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle("Set radius");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,  final int id) {
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String description = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(getApplicationContext(), description, Toast.LENGTH_SHORT).show();
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, description).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(PlaceBuffer places) {
                if (places.getStatus().isSuccess()) {
                    final Place myPlace = places.get(0);
                    LatLng queriedLocation = myPlace.getLatLng();
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(queriedLocation, 15));
                }
                places.release();
            }
        });
    }
}
