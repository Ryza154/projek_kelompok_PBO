package id.rainticket.model;

public interface PaymentMethod {
    boolean pay(Booking booking);
    String getPaymentName();
}
