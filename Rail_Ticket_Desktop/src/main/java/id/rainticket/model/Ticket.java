package id.rainticket.model;

public abstract class Ticket {
    private String code;
    private String passengerName;
    private String seat;
    private int total;
    private String status;

    public Ticket(String code, String passengerName, String seat, int total, String status) {
        this.code = code;
        this.passengerName = passengerName;
        this.seat = seat;
        this.total = total;
        this.status = status;
    }

    public abstract String getTicketType();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public String getSeat() { return seat; }
    public void setSeat(String seat) { this.seat = seat; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
