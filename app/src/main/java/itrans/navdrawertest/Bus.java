package itrans.navdrawertest;

/**
 * Created by helen_000 on 7/2/2016.
 */
public class Bus {
    private String busNo, busT, bF;

    public Bus(){
    }

    public Bus(String busNo, String busT, String bF){
        this.busNo = busNo;
        this.busT = busT;
        this.bF = bF;
    }

    public String getBusNo(){
        return busNo;
    }

    public void setBusNo(String name){
        this.busNo = name;
    }

    public String getBusT(){
        return busT;
    }

    public void setBusT(String time){
        this.busT = time;
    }

    public String getBF(){
        return bF;
    }

    public void setBF(String WCA){
        this.bF = WCA;
    }
}
