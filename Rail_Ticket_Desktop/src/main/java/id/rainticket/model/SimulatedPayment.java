package id.rainticket.model;

public class SimulatedPayment implements PaymentMethod {
    @Override
    public boolean pay(Booking booking) {
        // Pembayaran hanya simulasi untuk kebutuhan praktikum.
        return booking != null && booking.getTotal() > 0;
    }

    @Override
    public String getPaymentName() {
        return "Pembayaran Simulasi Rail Ticket";
    }
}
