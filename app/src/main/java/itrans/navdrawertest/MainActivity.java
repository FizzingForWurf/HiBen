package itrans.navdrawertest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.view.ActionMode;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    //Google Maps stuff
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LatLng LastLatLng;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private Marker mCurrLocationMarker, addedMarker;


    TextView tvtesting;
    ListView lvDestinations;
    NavigationView navigationView;

    private boolean isOneSwitchChecked = false;
    private boolean isNearDestinationInitially = false;
    private boolean hasArrived = false;
    private boolean startingLocationService = false;
    private int positionOfActivatedSwitch = -1;

    //tracking location for alarm
    private String returnedTitle = null;
    private String returnedLatLong = null;
    private String returnedRadius = null;
    private String returnedRingTone = null;
    private float distance;

    //Contextual Action Bar
    ActionMode actionMode;
    int number;
    //Alarm stuff
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("iTrans");

        tvtesting = (TextView) findViewById(R.id.tvtesting);

        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        statusCheck();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        lvDestinations = (ListView) findViewById(R.id.lvDestinations);
        lvDestinations.setOnItemLongClickListener(this);
        lvDestinations.setOnItemClickListener(this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton AddDestinationFab = (FloatingActionButton) findViewById(R.id.AddDestinationFab);
        AddDestinationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lvDestinations.getCount() >= 11) {
                    Toast.makeText(MainActivity.this, "Maximum number of alarms reached", LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(MainActivity.this, AddDestination.class);
                    startActivity(intent);
                }
            }
        });
        System.out.println(positionOfActivatedSwitch);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Toast.makeText(MainActivity.this, position + "clicked", Toast.LENGTH_SHORT).show();
        Switch alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
        if (alarmSwitch.isChecked()){
            alarmSwitch.setChecked(false);
            isOneSwitchChecked = false;
            returnedTitle = null;
            returnedLatLong = null;
            returnedRadius = null;
            returnedRingTone = null;
            System.out.println(positionOfActivatedSwitch);
            View v = lvDestinations.getChildAt(positionOfActivatedSwitch);
            TextView tvDistance = (TextView) v.findViewById(R.id.tvAlarmDistance);
            tvDistance.setText("Distance left:");
            positionOfActivatedSwitch = -1;
            System.out.println(positionOfActivatedSwitch);
        }else{
            if (!isOneSwitchChecked) {
                positionOfActivatedSwitch = position;
                alarmSwitch.setChecked(true);
                isOneSwitchChecked = true;
                if (!startingLocationService){
                    startingLocationService = true;
                    startLocationUpdates();
                }

                myDBAdapter db = new myDBAdapter(this);
                db.open();
                number = positionOfActivatedSwitch + 1;
                String numberInList = Integer.toString(number);
                returnedTitle = db.getTitle(numberInList);
                returnedLatLong = db.getLatLng(numberInList);
                returnedRadius = db.getRadius(numberInList);
                returnedRingTone = db.getRingTone(numberInList);
                db.close();

                AddDestinationMarkerOnMap(returnedLatLong, returnedRadius);

                float distanceleftInMeters = checkDistanceFromDestination();
                float distanceleftInKm = distanceleftInMeters/1000;
                BigDecimal result;
                result = round(distanceleftInKm, 2);

                float radius = Float.parseFloat(returnedRadius);
                if (distance <= radius) {
                    isNearDestinationInitially = true;
                    Toast.makeText(getApplicationContext(), "You are already near your destination!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Starting " + returnedTitle + " alarm...", Toast.LENGTH_SHORT).show();
                }

                View something = lvDestinations.getChildAt(positionOfActivatedSwitch); //adapterView.getPositionForView(view)
                System.out.println(positionOfActivatedSwitch);
                TextView tvDistance = (TextView) something.findViewById(R.id.tvAlarmDistance);
                String hi = "Distance left: " + result + "km";
                tvDistance.setText(hi);

                if (isNearDestinationInitially){
                    isNearDestinationInitially = false;
                    //trigger alarm and set switch to off
                    alarmSwitch.setChecked(false);
                    positionOfActivatedSwitch = -1;
                    isOneSwitchChecked = false;
                    returnedTitle = null;
                    returnedLatLong = null;
                    returnedRadius = null;
                    returnedRingTone = null;
                    tvDistance.setText("Distance left:");
                    System.out.println(positionOfActivatedSwitch);
                }
            }else{
                alarmSwitch.setChecked(false);
                positionOfActivatedSwitch = number - 1;
                System.out.println(positionOfActivatedSwitch);
                Toast.makeText(getApplicationContext(), "You can only set one alarm.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public float checkDistanceFromDestination(){
        if (LastLatLng != null) {
            //LatLng for set destination
            String[] latANDlong = returnedLatLong.split(",");
            double latitudeDestination = Double.parseDouble(latANDlong[0]);
            double longitudeDestination = Double.parseDouble(latANDlong[1]);

            //LatLng for current location
            double currentLatitude = LastLatLng.latitude;
            double currentLongitude = LastLatLng.longitude;

            Location currentLocation = new Location("Current Location");
            currentLocation.setLatitude(currentLatitude);
            currentLocation.setLongitude(currentLongitude);

            Location locationDestination = new Location("Destination");
            locationDestination.setLatitude(latitudeDestination);
            locationDestination.setLongitude(longitudeDestination);

            distance = currentLocation.distanceTo(locationDestination);
        }else{
            Toast.makeText(getApplicationContext(), "Please turn on location services", Toast.LENGTH_SHORT).show();
        }
        return distance;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

        actionMode = MainActivity.this.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                actionMode.setTitle("selected");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        return false;
    }

    public void statusCheck(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled || !network_enabled) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,  final int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        }

    private void populateListViewFromDatabase() {
        myDBAdapter db = new myDBAdapter(this);
        db.open();
        Cursor c = db.retrieveAllEntriesCursor();

        String[] from = {myDBAdapter.ENTRY_TITLE, myDBAdapter.ENTRY_DESTINATION};
        int[] to = {R.id.tvAlarmTitle, R.id.tvAlarmDestination};

        SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(this,R.layout.custom_alarm_destination_row,c,from,to);
        db.close();
        lvDestinations.setAdapter(myCursorAdapter);
    }

    private void toggleVisibility()	{
        if(lvDestinations.getCount()>0) 	{
            tvtesting.setVisibility(View.GONE);
            lvDestinations.setVisibility(View.VISIBLE);
        }
        else {
            lvDestinations.setVisibility(View.GONE);
            tvtesting.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        checkPlayServices();
        isOneSwitchChecked = false;
        populateListViewFromDatabase();
        toggleVisibility();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void AddDestinationMarkerOnMap(String receivedLatLng, String alertRadius) {
        map.clear();
        int radius = Integer.parseInt(alertRadius);

        String[] latANDlong =  receivedLatLng.split(",");
        double latitude = Double.parseDouble(latANDlong[0]);
        double longitude = Double.parseDouble(latANDlong[1]);
        LatLng selectedLocation = new LatLng(latitude, longitude);

        map.addMarker(new MarkerOptions()
                .position(selectedLocation)
                .icon(BitmapDescriptorFactory.defaultMarker()));

        map.addCircle(new CircleOptions()
                .center(selectedLocation)
                .radius(radius)
                .fillColor(0x550000FF)
                .strokeColor(Color.BLUE)
                .strokeWidth(10.0f));

        int zoomLevel;
        double zoomRadius = radius + radius / 2;
        double scale = zoomRadius / 500;
        zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));

        if (LastLatLng != null) {
            map.addMarker(new MarkerOptions()
                    .position(LastLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker()));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(LastLatLng);
            builder.include(selectedLocation);
            LatLngBounds bounds = builder.build();
            int zoom_padding = 130;
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoom_padding));
        }else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, zoomLevel));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //startLocationUpdates();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
            switch (id) {
                case R.id.action_settings:
                    return true;
            }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_nearbyBus){
            Intent i = new Intent(MainActivity.this, NearbyBusStops.class);
            startActivity(i);

        }else if (id == R.id.nav_busStopsSearch){
            Intent i = new Intent(MainActivity.this, BusNumberSearch.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        LastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(LastLatLng);
        markerOptions.title("You are here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(LastLatLng)
                .zoom(15)
                //.tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mCurrLocationMarker = map.addMarker(markerOptions);

        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }

        if (returnedRadius != null) {
            distance = checkDistanceFromDestination();

            float distanceleftInKm = distance/1000;
            BigDecimal result;
            result = round(distanceleftInKm, 2);
            System.out.println(positionOfActivatedSwitch);
            View view = lvDestinations.getChildAt(positionOfActivatedSwitch);
            TextView alarmDistance = (TextView) view.findViewById(R.id.tvAlarmDistance);
            alarmDistance.setText("Distance left: " + result + "kmgglbkb");
            float radius = Float.parseFloat(returnedRadius);
            hasArrived = (distance <= radius);
        }

        //check if arriving
        if (hasArrived){
            System.out.println(positionOfActivatedSwitch);
            View view = lvDestinations.getChildAt(positionOfActivatedSwitch);
            Switch alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
            TextView alarmDistance = (TextView) view.findViewById(R.id.tvAlarmDistance);
            alarmDistance.setText("");
            alarmSwitch.setChecked(false);
            positionOfActivatedSwitch = -1;
            isOneSwitchChecked = false;
            returnedTitle = null;
            returnedLatLong = null;
            returnedRadius = null;
            returnedRingTone = null;
            hasArrived = false;
            System.out.println(positionOfActivatedSwitch);
            startAlarm();
        }
    }

    public void startAlarm(){
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), Settings.System.DEFAULT_RINGTONE_URI);
        Uri currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext().getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        final Ringtone currentRingtone = RingtoneManager.getRingtone(getApplicationContext(), currentRintoneUri);
        Toast.makeText(getApplicationContext(), ringtone.getTitle(getApplicationContext())+" and " +
                "Current Ringtone:"+currentRingtone.getTitle(getApplicationContext()), Toast.LENGTH_SHORT).show();
        currentRingtone.play();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have arrived!")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,  final int id) {
                        currentRingtone.stop();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//
//        }
//    }


}
