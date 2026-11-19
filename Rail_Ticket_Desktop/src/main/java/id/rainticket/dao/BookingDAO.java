package id.rainticket.dao;

import id.rainticket.database.Database;
import id.rainticket.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public int getAvailableSeats(int scheduleId, int totalSeats) throws Exception {
        String sql = "SELECT COUNT(*) FROM bookings WHERE schedule_id=? AND status<>'DIBATALKAN'";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, scheduleId);
            try (ResultSet r = p.executeQuery()) {
                int booked = r.next() ? r.getInt(1) : 0;
                return Math.max(0, totalSeats - booked);
            }
        }
    }

    public boolean isSeatBooked(int scheduleId, String seat) throws Exception {
        String sql = "SELECT COUNT(*) FROM bookings WHERE schedule_id=? AND upper(seat)=upper(?) AND status<>'DIBATALKAN'";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, scheduleId);
            p.setString(2, seat.trim());
            try (ResultSet r = p.executeQuery()) {
                return r.next() && r.getInt(1) > 0;
            }
        }
    }

    public void create(int userId, int scheduleId, String passenger, String seat, int total) throws Exception {
        String sql = "INSERT INTO bookings(user_id,schedule_id,passenger,seat,total,status,booking_date) VALUES(?,?,?,?,?,'MENUNGGU',?)";
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                if (isSeatBooked(c, scheduleId, seat)) {
                    throw new IllegalStateException("Kursi " + seat + " sudah dipesan. Silakan pilih kursi lain.");
                }
                int capacity;
                try (PreparedStatement check = c.prepareStatement("SELECT seats FROM schedules WHERE id=?")) {
                    check.setInt(1, scheduleId);
                    try (ResultSet r = check.executeQuery()) {
                        if (!r.next()) throw new IllegalStateException("Jadwal tidak ditemukan.");
                        capacity = r.getInt(1);
                    }
                }
                int booked;
                try (PreparedStatement check = c.prepareStatement(
                        "SELECT COUNT(*) FROM bookings WHERE schedule_id=? AND status<>'DIBATALKAN'")) {
                    check.setInt(1, scheduleId);
                    try (ResultSet r = check.executeQuery()) { booked = r.next() ? r.getInt(1) : 0; }
                }
                if (booked >= capacity) throw new IllegalStateException("Maaf, seluruh kursi pada jadwal ini sudah penuh.");
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setInt(1, userId); p.setInt(2, scheduleId); p.setString(3, passenger);
                    p.setString(4, seat.trim().toUpperCase()); p.setInt(5, total);
                    p.setString(6, LocalDate.now().toString());
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private boolean isSeatBooked(Connection c, int scheduleId, String seat) throws Exception {
        String sql = "SELECT COUNT(*) FROM bookings WHERE schedule_id=? AND upper(seat)=upper(?) AND status<>'DIBATALKAN'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, scheduleId); p.setString(2, seat.trim());
            try (ResultSet r = p.executeQuery()) { return r.next() && r.getInt(1) > 0; }
        }
    }

    public List<Booking> findByUser(int userId) throws Exception { return find("WHERE b.user_id=?", userId); }
    public List<Booking> findAll() throws Exception { return find("", 0); }

    private List<Booking> find(String condition, int userId) throws Exception {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*,u.name,u.email,u.password,s.train,s.origin,s.destination,s.travel_date,s.depart,s.arrive,s.price,s.seats,s.class_name " +
                "FROM bookings b JOIN users u ON b.user_id=u.id JOIN schedules s ON b.schedule_id=s.id " +
                condition + " ORDER BY b.id DESC";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            if (condition.contains("?")) p.setInt(1, userId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    Customer customer = new Customer(r.getInt("user_id"), r.getString("name"), r.getString("email"), r.getString("password"));
                    TrainSchedule schedule = new TrainSchedule(r.getInt("schedule_id"), new Train(0, r.getString("train")),
                            new Station(r.getString("origin"), ""), new Station(r.getString("destination"), ""),
                            r.getString("travel_date"), r.getString("depart"), r.getString("arrive"),
                            r.getInt("price"), r.getInt("seats"), r.getString("class_name"));
                    String code = Booking.TicketCode.create(r.getInt("id"));
                    list.add(new Booking(code, customer, schedule, r.getString("passenger"), r.getString("seat"),
                            r.getInt("total"), r.getString("status"), r.getString("booking_date")));
                }
            }
        }
        return list;
    }

    public void updateStatus(String code, String status) throws Exception {
        int id = Integer.parseInt(code.replace("RT-", ""));
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("UPDATE bookings SET status=? WHERE id=?")) {
            p.setString(1, status); p.setInt(2, id); p.executeUpdate();
        }
    }

    /** Menghapus pesanan beserta data pembayarannya (tabel payments) tanpa mengubah struktur database. */
    public void delete(String code) throws Exception {
        int id = Integer.parseInt(code.replace("RT-", ""));
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement pay = c.prepareStatement("DELETE FROM payments WHERE booking_code=?");
                 PreparedStatement book = c.prepareStatement("DELETE FROM bookings WHERE id=?")) {
                pay.setString(1, code); pay.executeUpdate();
                book.setInt(1, id); book.executeUpdate();
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }
}
