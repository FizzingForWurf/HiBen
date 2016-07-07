package itrans.navdrawertest;

import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by helen_000 on 7/2/2016.
 */
public class homeAdapter extends RecyclerView.Adapter<homeAdapter.MyViewHolder> {

    private List<Bus> busServices;
    private String time;
    private long diff;
    String timeRemaining;
    MainActivity ma = new MainActivity();

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView ETA, busNum;
        public ImageView BusFeature;

        public MyViewHolder(View view){
            super(view);
            busNum = (TextView) view.findViewById(R.id.busNumber);
            ETA = (TextView) view.findViewById(R.id.busTiming);
            BusFeature = (ImageView) view.findViewById(R.id.wheelCA);
        }
    }

    public homeAdapter(List<Bus> busServices){
        this.busServices = busServices;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bus_home, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position){
        Bus bus = busServices.get(position);
        holder.busNum.setText(bus.getBusNo());
        if(bus.getBF().isEmpty()){
            holder.BusFeature.setVisibility(View.INVISIBLE);
        }else{
            holder.BusFeature.setVisibility(View.VISIBLE);
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String sst = format.format(c.getTime());
        Date eta = null;
        Date current = null;
        try {
            String[] splitString = bus.getBusT().split("T");
            splitString[1].replace("+08:00","");
            eta = format.parse(splitString[0]+" "+splitString[1]);
            current = format.parse(sst);

            diff = eta.getTime() - current.getTime();

        }catch(Exception e){
            e.printStackTrace();
        }
        new CountDownTimer(diff,1000){
            public void onTick(long millisUntilFinished){
                if(millisUntilFinished <= 30000) {
                    timeRemaining = "Arr";
                }else{
                    if(millisUntilFinished/1000 %60 >30){
                        timeRemaining = String.valueOf(millisUntilFinished/(60*1000)+1)+" min";
                    }else {
                        timeRemaining = String.valueOf(millisUntilFinished / (60 * 1000)) + " min";
                    }
                }
                holder.ETA.setText(timeRemaining);
            }

            public void onFinish(){
                holder.ETA.setText(String.valueOf(holder.getAdapterPosition()));
            }
        }.start();
    }

    @Override
    public int getItemCount(){
        return busServices.size();
    }
}
