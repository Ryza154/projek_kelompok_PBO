package id.rainticket.dao;

import id.rainticket.database.Database;
import id.rainticket.model.*;
import java.sql.*;

public class UserDAO {
    public User login(String email, String password) throws Exception {
        String sql = "SELECT * FROM users WHERE email=? AND password=?";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, email);
            p.setString(2, password);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                if ("ADMIN".equals(r.getString("role")))
                    return new Admin(r.getInt("id"), r.getString("name"), r.getString("email"), r.getString("password"));
                return new Customer(r.getInt("id"), r.getString("name"), r.getString("email"), r.getString("password"));
            }
        }
    }

    public void register(String name, String email, String password) throws Exception {
        try (Connection c = Database.connect();
             PreparedStatement p = c.prepareStatement("INSERT INTO users(name,email,password,role) VALUES(?,?,?,'CUSTOMER')")) {
            p.setString(1, name);
            p.setString(2, email);
            p.setString(3, password);
            p.executeUpdate();
        }
    }

    /** Menghapus akun pelanggan beserta pesanan dan pembayarannya. Mengembalikan jumlah pesanan yang ikut terhapus. */
    public int deleteCustomer(int userId) throws Exception {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement pay = c.prepareStatement(
                         "DELETE FROM payments WHERE booking_code IN (SELECT 'RT-' || printf('%06d', id) FROM bookings WHERE user_id=?)");
                 PreparedStatement book = c.prepareStatement("DELETE FROM bookings WHERE user_id=?");
                 PreparedStatement user = c.prepareStatement("DELETE FROM users WHERE id=? AND role='CUSTOMER'")) {
                pay.setInt(1, userId); pay.executeUpdate();
                book.setInt(1, userId); int bookings = book.executeUpdate();
                user.setInt(1, userId); user.executeUpdate();
                c.commit();
                return bookings;
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }
}
