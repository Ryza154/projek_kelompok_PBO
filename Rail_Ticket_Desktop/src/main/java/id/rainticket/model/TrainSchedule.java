package id.rainticket.model;

public class TrainSchedule {
    private int id;
    private Train train;
    private Station origin;
    private Station destination;
    private String date;
    private String depart;
    private String arrive;
    private int price;
    private int seats;
    private String className;

    public TrainSchedule(int id, Train train, Station origin, Station destination,
                         String date, String depart, String arrive, int price, int seats, String className) {
        this.id = id;
        this.train = train;
        this.origin = origin;
        this.destination = destination;
        this.date = date;
        this.depart = depart;
        this.arrive = arrive;
        this.price = price;
        this.seats = seats;
        this.className = className;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }
    public Station getOrigin() { return origin; }
    public void setOrigin(Station origin) { this.origin = origin; }
    public Station getDestination() { return destination; }
    public void setDestination(Station destination) { this.destination = destination; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDepart() { return depart; }
    public void setDepart(String depart) { this.depart = depart; }
    public String getArrive() { return arrive; }
    public void setArrive(String arrive) { this.arrive = arrive; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
