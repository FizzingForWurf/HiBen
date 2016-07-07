package itrans.navdrawertest;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import itrans.navdrawertest.Internet.MyApplication;
import itrans.navdrawertest.Internet.VolleySingleton;

/**
 * Created by helen_000 on 6/27/2016.
 */
public class searchBusNo extends AppCompatActivity implements OnMapReadyCallback{
    JsonObjectRequest jsonObjectRequest;
    int count;
    int listCount;
    RequestQueue requestQueue;
    boolean dontCall;
    boolean end;
    MyApplication ma;
    List<String> busList;
    private List<Integer> allBusStops = new ArrayList<>();
    private List<Double> singleCoordinates = new ArrayList<>();
    private List<List<Double>> busCoordinates = new ArrayList<>();
    int lastCode;
    GoogleMap nMap;
    LatLng nLocation;
    LatLng singapore = new LatLng(1.358464, 103.818040);
    String query;
    private String eta;
    boolean done;
    public Marker selectedMarker;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_busno);
        end = false;
        done = false;
        listCount = 0;
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        ma = MyApplication.getInstance();
        busList = ma.retrieveAll(getApplicationContext());
        for(int i=0;i<busList.size();i++) {
            if(busList.get(i).equals("96")) {
                count = (int) Math.round(i*1.1)*50;
                Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();

            }
        }
        MapFragment nmapfragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.busNoMap));
        nmapfragment.getMapAsync(this);

            /*if(bus.equals("67")){
                count = busList.indexOf(bus)*100-50;
                Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();
            }*/


        Toolbar busNToolBar = (Toolbar) findViewById(R.id.busNoToolBar);
        setSupportActionBar(busNToolBar);
        Intent intent = getIntent();
        /*if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchBus(query);
        }*/

        ActionBar bnab = getSupportActionBar();
        bnab.setDisplayHomeAsUpEnabled(true);
        busStops();
    }

    @Override
    public void onMapReady(GoogleMap map){
        nMap = map;
        nMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker;
                getETA(marker.getTitle());
                return false;
            }
        });

        nMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker){
                Intent sendBusStop  = new Intent(searchBusNo.this, BusNumberSearch.class);
                Bundle b = new Bundle();
                b.putString("busStopNo",marker.getTitle());
                b.putParcelable("busStopPt",marker.getPosition());
                sendBusStop.putExtras(b);
                startActivity(sendBusStop);
            }
        });
    }
    /*Toast.makeText(getApplicationContext(), "List created", Toast.LENGTH_SHORT).show();
                                for(allBuses bus:allBuses){
                                    if(bus.getBusNo().equals("171")){
                                        count = allBuses.indexOf(bus)*50-50;
                                        Toast.makeText(getApplicationContext(), String.valueOf(count), Toast.LENGTH_LONG).show();
                                    }
                                }
                                count = 0;
                                if(count<0){
                                    Toast.makeText(getApplicationContext(), "This bus does not exist", Toast.LENGTH_LONG).show();
                                }*/

    public void busStops(){
        dontCall = false;
        count += 50;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes?$skip="+String.valueOf(count), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("value");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject services = jsonArray.getJSONObject(i);
                                String busNo = services.getString("ServiceNo");
                                if(busNo.equals("96")){
                                    String busStopCode = services.getString("BusStopCode");
                                    if(!busStopCode.matches("(A|B|C|P|N|E|T|S).*")){
                                        Integer busStop = Integer.parseInt(busStopCode);
                                        allBusStops.add(busStop);
                                    }
                                    end = true;
                                }else if(end){
                                    dontCall = true;

                                }

                            }
                            if(!dontCall){
                                busStops();
                            }else{
                                Toast.makeText(getApplicationContext(), "Finished at " + String.valueOf(count), Toast.LENGTH_LONG).show();
                                Collections.sort(allBusStops);
                                count = -50;
                                for(int i=allBusStops.size()-1;i>=0;i--){
                                    if(i==allBusStops.size()-1 || allBusStops.get(i)!=lastCode) {
                                        lastCode = allBusStops.get(i);
                                    }else{
                                        allBusStops.remove(i);
                                    }
                                }
                                Toast.makeText(getApplicationContext(), String.valueOf(allBusStops), Toast.LENGTH_LONG).show();
                                setMarkers();
                            }
                                /*
                                    end = true;
                                }else if(end){
                                    callAgain = true;
                                }*/
                            //JSONObject services = jsonArray.getJSONObject(i);
                            //String busNo = services.getString("ServiceNo");
                            // JSONObject nextBus = services.getJSONObject("NextBus");
                            //String eta = nextBus.getString("EstimatedArrival");
                            // String wheelC = nextBus.getString("Feature");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");
                        Toast.makeText(getApplicationContext(), "That did not work:(", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AccountKey", "3SnRYzr/X0eKp2HvwTYtmg==");
                headers.put("UniqueUserID", "0bf7760d-15ec-4a1b-9c82-93562fcc9798");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void setMarkers(){
        dontCall = false;
        count += 50;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip="+String.valueOf(count), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("value");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject services = jsonArray.getJSONObject(i);
                                String busCode = services.getString("BusStopCode");
                                if(listCount>=allBusStops.size()){
                                    dontCall = true;
                                }else{
                                    if(String.valueOf(allBusStops.get(listCount)).length()<5){
                                        query = "0"+String.valueOf(allBusStops.get(listCount));
                                    }else{
                                        query = String.valueOf(allBusStops.get(listCount));
                                    }
                                    if(query.equals(busCode)){
                                        singleCoordinates = new ArrayList<>();
                                        Double Latitude = services.getDouble("Latitude");
                                        Double Longitude = services.getDouble("Longitude");
                                        singleCoordinates.add(Latitude);
                                        singleCoordinates.add(Longitude);
                                        busCoordinates.add(singleCoordinates);
                                        listCount++;
                                    }
                                }

                            }
                            if(!dontCall){
                                setMarkers();
                            }else{
                                Toast.makeText(getApplicationContext(), String.valueOf(busCoordinates.size()), Toast.LENGTH_LONG).show();
                                placeMarkers();
                            }
                                /*
                                    end = true;
                                }else if(end){
                                    callAgain = true;
                                }*/
                            //JSONObject services = jsonArray.getJSONObject(i);
                            //String busNo = services.getString("ServiceNo");
                            // JSONObject nextBus = services.getJSONObject("NextBus");
                            //String eta = nextBus.getString("EstimatedArrival");
                            // String wheelC = nextBus.getString("Feature");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");
                        Toast.makeText(getApplicationContext(), "That did not work:(", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AccountKey", "3SnRYzr/X0eKp2HvwTYtmg==");
                headers.put("UniqueUserID", "0bf7760d-15ec-4a1b-9c82-93562fcc9798");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public void placeMarkers(){
        count = 0;
        nMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singapore,10));
        for(List<Double> coordinates:busCoordinates) {
            nLocation = new LatLng(coordinates.get(0),coordinates.get(1));
            nMap.addMarker(new MarkerOptions()
                    .position(nLocation)
                    .title(busStopName(allBusStops.get(count)))
                    .snippet("")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_24dp)));
            count++;
        }
        Toast.makeText(getApplicationContext(),"Repeated for "+String.valueOf(count),Toast.LENGTH_LONG).show();
    }

    public String busStopName(int nameCode){
        if(String.valueOf(nameCode).length()<5){
            query = "0"+String.valueOf(nameCode);
        }else{
            query = String.valueOf(nameCode);
        }
        return query;
    }


    public void getETA(String stopCode){
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusArrival?BusStopID=" + stopCode + "&SST=True", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("Services");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject services = jsonArray.getJSONObject(i);
                                String busNo = services.getString("ServiceNo");
                                /*JSONObject nextBus = services.getJSONObject("NextBus");
                                String eta = nextBus.getString("EstimatedArrival");
                                String wheelC = nextBus.getString("Feature");
                                JSONObject subBus = services.getJSONObject("SubsequentBus");
                                String NEta = subBus.getString("EstimatedArrival");
                                String NWheelC = subBus.getString("Feature");*/
                                if (busNo.equals("96")) {
                                    JSONObject nextBus = services.getJSONObject("NextBus");
                                    eta = nextBus.getString("EstimatedArrival");
                                    Toast.makeText(getApplicationContext(),eta,Toast.LENGTH_SHORT).show();
                                    selectedMarker.setSnippet(eta);
                                    selectedMarker.hideInfoWindow();
                                    selectedMarker.showInfoWindow();
                                }
                            }
                                /*
                                    end = true;
                                }else if(end){
                                    callAgain = true;
                                }*/
                            //JSONObject services = jsonArray.getJSONObject(i);
                            //String busNo = services.getString("ServiceNo");
                            // JSONObject nextBus = services.getJSONObject("NextBus");
                            //String eta = nextBus.getString("EstimatedArrival");
                            // String wheelC = nextBus.getString("Feature");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");
                        Toast.makeText(getApplicationContext(), "That did not work:(", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AccountKey", "3SnRYzr/X0eKp2HvwTYtmg==");
                headers.put("UniqueUserID", "0bf7760d-15ec-4a1b-9c82-93562fcc9798");
                headers.put("accept", "application/json");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
    /*private class getETA extends AsyncTask<String, Void, String> {

        protected String doInBackground(String...stopCode) {

            return eta;
        }

        protected void onProgressUpdate(){}

        protected void onPostExecute(String result){
            selectedMarker.setSnippet(result);
        }
    }*/
}
