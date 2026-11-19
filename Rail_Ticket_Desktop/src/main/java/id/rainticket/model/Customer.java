package id.rainticket.model;

public class Customer extends User {
    public Customer(int id, String name, String email, String password) {
        super(id, name, email, password, "CUSTOMER");
    }

    @Override
    public String dashboardInfo() {
        return "Dashboard Pelanggan - Cari perjalanan dan kelola tiket Anda.";
    }
}
