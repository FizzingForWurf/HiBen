package itrans.navdrawertest;

import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class AddDestination extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnMapReadyCallback{

    private GoogleMap map;
    private Marker myMapMarker;
    private Circle myCirleRadius;
    private LatLng selectedLocation;

    //Testing geocoding:
    private String geoCodedAddress;

    String finalLatLong;
    EditText etTitle;
    CardView cvDestination, cvRingTone;
    ImageView ivPickMap;
    Button btnDone, btnCancel;
    TextView tvDestination, tvRadiusIndicator, tvCurrentRingTone;
    SeekBar radiusSeekbar;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int TONE_PICKER = 3;

    private int progressChange = 0;
    private double radius = 0;
    private int finalRadius = 1100;
    private String entryRadius = "1100";
    private String selectedRingTone = "content://settings/system/ringtone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_destination);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMapAddDestination);
        mapFragment.getMapAsync(this);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ivPickMap = (ImageView) findViewById(R.id.ivPickMap);
        etTitle = (EditText) findViewById(R.id.etTitle);
        cvDestination = (CardView) findViewById(R.id.cvDestination);
        cvRingTone = (CardView) findViewById(R.id.cvRingTone);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnDone = (Button) findViewById(R.id.btnDone);
        tvDestination = (TextView) findViewById(R.id.tvDestination);
        tvRadiusIndicator = (TextView) findViewById(R.id.tvRadiusIndicator);
        tvCurrentRingTone = (TextView) findViewById(R.id.tvCurrentRingTone);
        radiusSeekbar = (SeekBar) findViewById(R.id.radiusSeekbar);

        btnCancel.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        ivPickMap.setOnClickListener(this);
        cvDestination.setOnClickListener(this);
        cvRingTone.setOnClickListener(this);
        radiusSeekbar.setOnSeekBarChangeListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //For back button...
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivPickMap:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    Intent placeIntent = builder.build(AddDestination.this);
                    startActivityForResult(placeIntent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cvDestination:
                try {
                    Intent searchIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
                    startActivityForResult(searchIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnCancel:
                this.finish();
                break;
            case R.id.btnDone:
                if (tvDestination.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please select your destination before proceeding", Toast.LENGTH_SHORT).show();
                }else if (etTitle.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Please enter a title before proceeding", Toast.LENGTH_SHORT).show();
                }else {
                    String addTitle = etTitle.getText().toString();
                    String addDestination = tvDestination.getText().toString();
                    try {
                        myDBAdapter inputDestination = new myDBAdapter(AddDestination.this);
                        inputDestination.open();
                        inputDestination.insertEntry(addTitle, addDestination, finalLatLong, entryRadius, selectedRingTone);
                        inputDestination.close();
                    } catch (Exception e) {
                        e.printStackTrace();

                    } finally {
                        Toast.makeText(getApplicationContext(), "Destination entry added!", Toast.LENGTH_SHORT).show();
                    }
                    this.finish();
                }
                    break;
            case R.id.cvRingTone:
                final Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(AddDestination.this, RingtoneManager.TYPE_ALARM);
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                startActivityForResult(intent, TONE_PICKER);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK){
                Place chosenDestination = PlacePicker.getPlace(this, data);
                String latlong = String.format("%s", chosenDestination.getLatLng());
                String address = String.format("%s", chosenDestination.getAddress());
                if (address.equals("")) {
                    tvDestination.setText(latlong);
//                    geocodeCoordinates(latlong);
//                    tvDestination.setText(geoCodedAddress);
                }else {
                    tvDestination.setText(address);
                }
                processlatlng(latlong);
            }
        }
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place searchedDestination = PlaceAutocomplete.getPlace(this, data);
                String latlong = String.format("%s", searchedDestination.getLatLng());
                String address = String.format("%s", searchedDestination.getAddress());
                tvDestination.setText(address);

                processlatlng(latlong);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            }
        }
        if (requestCode == TONE_PICKER) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Ringtone ringTone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                if (uri != null) {
                    String NameOfRingTone = ringTone.getTitle(getApplicationContext());
                    selectedRingTone = uri.toString();
                    tvCurrentRingTone.setText(NameOfRingTone);
                }
            }
        }
    }

//    private String geocodeCoordinates(String latlong){
//        String rawlatlong = latlong.substring(latlong.indexOf("(")+1,latlong.indexOf(")"));
//        String[] latANDlong =  rawlatlong.split(",");
//        double Lat = Double.parseDouble(latANDlong[0]);
//        double Lon = Double.parseDouble(latANDlong[1]);
//        Geocoder gc = new Geocoder(AddDestination.this, Locale.getDefault());
//
//        try {
//            List<Address> addresses = gc.getFromLocation(Lat,Lon,1);
//            StringBuilder builderString = new StringBuilder();
//            if (addresses.size() > 0){
//                Address address = addresses.get(0);
//                for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
//                    builderString.append(address.get)
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return geoCodedAddress;
//    }

    private void processlatlng(String latlong) {
        String rawlatlong = latlong.substring(latlong.indexOf("(")+1,latlong.indexOf(")"));
        finalLatLong = rawlatlong;
        String[] latANDlong =  rawlatlong.split(",");
        double latitude = Double.parseDouble(latANDlong[0]);
        double longitude = Double.parseDouble(latANDlong[1]);
        selectedLocation = new LatLng(latitude, longitude);

        changeMapLocation(selectedLocation);
    }

    private void changeMapLocation(LatLng latlng) {

        if (myMapMarker != null) {
            myMapMarker.remove();
        }

        createRadiusVisualisation(selectedLocation, finalRadius);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, getZoomLevel(myCirleRadius)));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        myMapMarker = map.addMarker(markerOptions);
    }

    private void createRadiusVisualisation(LatLng location, int finalRadius) {

        if (myCirleRadius != null){
            myCirleRadius.remove();
        }

        CircleOptions co = new CircleOptions();
        co.center(location);
        co.radius(finalRadius);
        co.fillColor(0x550000FF);
        co.strokeColor(Color.BLUE);
        co.strokeWidth(10.0f);
        myCirleRadius = map.addCircle(co);
    }

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            DecimalFormat df = new DecimalFormat("####0.00");
            progressChange = progress;
            radius = 50 + (progressChange * 19.5);
            finalRadius = (int) radius;
            if (radius >= 1000) {
                tvRadiusIndicator.setText(df.format(radius / 1000) + "km");
            } else {
                tvRadiusIndicator.setText(finalRadius + "m");
            }
        if (selectedLocation != null){
            createRadiusVisualisation(selectedLocation, finalRadius);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, getZoomLevel(myCirleRadius)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //Toast.makeText(getApplicationContext(),"seek bar progress:"+progressChange, Toast.LENGTH_SHORT).show();
        radius = 50 + (progressChange * 19.5);
        finalRadius = (int) radius;
        entryRadius = Integer.toString(finalRadius);
    }
}
