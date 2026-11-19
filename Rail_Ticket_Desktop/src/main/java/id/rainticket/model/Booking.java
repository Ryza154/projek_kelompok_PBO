package id.rainticket.model;

public class Booking extends Ticket {
    private Customer customer;
    private TrainSchedule schedule;
    private String bookingDate;

    public Booking(String code, Customer customer, TrainSchedule schedule, String passengerName,
                   String seat, int total, String status, String bookingDate) {
        super(code, passengerName, seat, total, status);
        this.customer = customer;
        this.schedule = schedule;
        this.bookingDate = bookingDate;
    }

    @Override
    public String getTicketType() {
        return "Tiket Kereta Rail Ticket";
    }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public TrainSchedule getSchedule() { return schedule; }
    public void setSchedule(TrainSchedule schedule) { this.schedule = schedule; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    // Static Nested Class: tidak membutuhkan objek Booking untuk membuat kode tiket.
    public static class TicketCode {
        public static String create(int id) {
            return "RT-" + String.format("%06d", id);
        }
    }

    // Inner Class: boarding pass terikat pada satu objek Booking.
    public class BoardingPass {
        public String print() {
            return "RAIL TICKET\nKode: " + getCode() +
                    "\nPenumpang: " + getPassengerName() +
                    "\nKereta: " + schedule.getTrain().getName() +
                    "\nRute: " + schedule.getOrigin().getName() + " → " + schedule.getDestination().getName() +
                    "\nTanggal: " + schedule.getDate() +
                    "\nJam: " + schedule.getDepart() + " - " + schedule.getArrive() +
                    "\nKelas: " + schedule.getClassName() +
                    "\nKursi: " + getSeat() +
                    "\nStatus: " + getStatus();
        }
    }
}
