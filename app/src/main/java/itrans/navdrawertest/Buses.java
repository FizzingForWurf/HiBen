package itrans.navdrawertest;

public class Buses {

    private int busNumber;
    private String busStatus;
    private String busOperator;
    private int nextBusTime;
    private String nextBusPassengers;
    private String nextBusFeature;
    private int subBusTime;
    private String subBusPassengers;

    public Buses(){

    }

    public Buses(int busNumber, int nextBusTime){
        this.busNumber = busNumber;
        this.nextBusTime = nextBusTime;
    }

    public int getBusNumber(){
        return busNumber;
    }

    public void setBusNumber(int busNumber){
        this.busNumber = busNumber;
    }

    public String getBusStatus() {
        return busStatus;
    }

    public void setBusStatus(String busStatus) {
        this.busStatus = busStatus;
    }

    public String getBusOperator() {
        return busOperator;
    }

    public void setBusOperator(String busOperator) {
        this.busOperator = busOperator;
    }

    public int getNextBusTime() {
        return nextBusTime;
    }

    public void setNextBusTime(int nextBusTime) {
        this.nextBusTime = nextBusTime;
    }

    public String getNextBusPassengers() {
        return nextBusPassengers;
    }

    public void setNextBusPassengers(String nextBusPassengers) {
        this.nextBusPassengers = nextBusPassengers;
    }

    public String getNextBusFeature() {
        return nextBusFeature;
    }

    public void setNextBusFeature(String nextBusFeature) {
        this.nextBusFeature = nextBusFeature;
    }

    public int getSubBusTime() {
        return subBusTime;
    }

    public void setSubBusTime(int subBusTime) {
        this.subBusTime = subBusTime;
    }

    public String getSubBusPassengers() {
        return subBusPassengers;
    }

    public void setSubBusPassengers(String subBusPassengers) {
        this.subBusPassengers = subBusPassengers;
    }

    @Override
    public String toString(){
        return "Bus no. " + busNumber + "\n" +
                //"Bus operator " + busOperator + "\n" +
                //"Bus status " + busStatus + "\n" +
                "Bus arrival timing " + nextBusTime + "\n" /*+*/;
                //"Bus load " + nextBusPassengers + "\n" +
                //"Bus feature " +nextBusFeature + "\n" +
                //"Sub bus timing " + subBusTime + "\n" +
                //"Sub bus load " + subBusPassengers;
    }
}
