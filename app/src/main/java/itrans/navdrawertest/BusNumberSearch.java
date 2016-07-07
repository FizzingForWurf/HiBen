package itrans.navdrawertest;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import itrans.navdrawertest.Internet.MyApplication;
import itrans.navdrawertest.Internet.VolleySingleton;

public class BusNumberSearch extends AppCompatActivity implements OnMapReadyCallback{

    Button homeBtn;
    JsonObjectRequest jsonObjectRequest;
    int count;
    RequestQueue requestQueue;
    int label;
    boolean dontCall;
    boolean end;
    private List<Bus> busService = new ArrayList<>();
    private RecyclerView recyclerView;
    private homeAdapter hAdapter;
    Bus bus;
    int selection;
    boolean select;
    int ending;
    public int toggle;
    MyApplication ma;
    List<String> busList;
    LatLng mLocation = new LatLng(1.326166, 103.803492);

    String lastNo;
    String busStop;
    //private String[] homeOptions;
    //private DrawerLayout mDrawerLayout;
    //private ListView mDrawerList;
    //public int count = 0;
    /*public boolean callAgain = true;
    public boolean end= false;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_number_search);
        count = -50;
        label = 1;
        dontCall = false;
        end = false;
        select = false;
        selection = 0;
        toggle = 0;
        Toolbar homeToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(homeToolBar);
        requestQueue = VolleySingleton.getInstance().getRequestQueue();
        homeBtn = (Button) findViewById(R.id.searchBus);
        recyclerView = (RecyclerView) findViewById(R.id.busServices);
        hAdapter = new homeAdapter(busService);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(hAdapter);
        ma = MyApplication.getInstance();
        busList = ma.retrieveAll(getApplicationContext());
        MapFragment mapFragment =(MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle b = getIntent().getExtras();
        if(b !=null) {
            busStop = b.getString("busStopNo");
            mLocation = b.getParcelable("busStopPt");
        }else{
            busStop = "01012";
        }

        //homeOptions = new String[]{"Apples","Bananas","Oranges"};
        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main, homeOptions));
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        homeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent searchNo = new Intent(getApplicationContext(),searchBusNo.class);
                startActivity(searchNo);
            }
        });

        homeBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent searchStop = new Intent(getApplicationContext(), searchBusStop.class);
                startActivity(searchStop);
                return true;
            }
        });

        Toast.makeText(getApplicationContext(), "Processing data...", Toast.LENGTH_SHORT).show();
        call(busStop);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new ClickListener(){
            @Override
            public void onClick(View view, int position){
                select = true;
                if(toggle==0) {
                    toggle++;
                    selection = position;
                    call(busStop);
                }else if(toggle==1){
                    toggle --;
                    selection = position;
                    call(busStop);
                }
            }
        }));
    }

    @Override
    public void onMapReady(GoogleMap map){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation,17));
        map.addMarker(new MarkerOptions().position(mLocation)
                .title("Hwa Chong")
                .snippet("HCI")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_24dp)));
    }

    public void call(String busStop){
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusArrival?BusStopID="+busStop+"&SST=True", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("Services");
                            if(select){
                                ending = selection+1;
                            } else{
                                ending = jsonArray.length();
                            }
                            for (int i = selection; i < ending; i++) {
                                JSONObject services = jsonArray.getJSONObject(i);
                                String busNo = services.getString("ServiceNo");
                                JSONObject nextBus = services.getJSONObject("NextBus");
                                String eta = nextBus.getString("EstimatedArrival");
                                String wheelC = nextBus.getString("Feature");
                                if(select && toggle == 1){
                                    JSONObject subBus = services.getJSONObject("SubsequentBus");
                                    String NEta = subBus.getString("EstimatedArrival");
                                    String NWheelC = subBus.getString("Feature");
                                    if(NEta != null) {
                                        bus = busService.get(selection);
                                        bus.setBF(NWheelC);
                                        bus.setBusT(NEta);
                                    }
                                } else if(select && toggle == 0){
                                    bus = busService.get(selection);
                                    bus.setBF(wheelC);
                                    bus.setBusT(eta);
                                }else{
                                    bus = new Bus(busNo, eta, wheelC);
                                    busService.add(bus);
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
                            }
                            if(busList.isEmpty()){
                                count = -50;
                                getBusNo();
                            }
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
        if(select){
            hAdapter.notifyItemChanged(selection);
        }else {
            hAdapter.notifyDataSetChanged();
        }
    }

    public interface ClickListener{
        void onClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private BusNumberSearch.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final BusNumberSearch.ClickListener clickListener){
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e){
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(e)){
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e){

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept){

        }
    }

    public void getBusNo(){
        count += 50;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes?$skip="+String.valueOf(count), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("value");
                            if(jsonArray.length()<50) {
                                end = true;
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject services = jsonArray.getJSONObject(i);
                                String busNo = services.getString("ServiceNo");
                                /*JSONObject nextBus = services.getJSONObject("NextBus");
                                String eta = nextBus.getString("EstimatedArrival");
                                String wheelC = nextBus.getString("Feature");
                                JSONObject subBus = services.getJSONObject("SubsequentBus");
                                String NEta = subBus.getString("EstimatedArrival");
                                String NWheelC = subBus.getString("Feature");*/
                                if(!busNo.equals(lastNo)){
                                    lastNo = busNo;
                                    ma.addToDatabase(busNo,getApplication());
                                }
                            }
                            if(!end){
                                getBusNo();
                            }else{
                                Toast.makeText(getApplicationContext(), "Done",Toast.LENGTH_LONG).show();
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

    /*private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id){
            selectItem(position);
        }
    }

    private void selectItem(int position){
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }*/
    //Insert drawer functions
}