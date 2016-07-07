package itrans.navdrawertest;

import android.content.Context;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.ViewHolder> {

    private ArrayList<Buses> busList = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public AdapterRecycler(Context context){
        layoutInflater = LayoutInflater.from(context);
    }

    public void setBusList(ArrayList<Buses> busList){
        this.busList = busList;
        notifyItemRangeChanged(0, busList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_bus_recycler_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Buses currentBus = busList.get(position);
        holder.tvBusService.setText(currentBus.getBusNumber());
        holder.btnTimeLeft.setText(currentBus.getNextBusTime());
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvBusService;
        private Button btnTimeLeft;

        public ViewHolder(View itemView) {
            super(itemView);
            tvBusService = (TextView) itemView.findViewById(R.id.tvBusService);
            btnTimeLeft = (Button) itemView.findViewById(R.id.btnTimeLeft);
        }
    }
}
