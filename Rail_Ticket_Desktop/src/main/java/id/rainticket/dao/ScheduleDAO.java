package id.rainticket.dao;

import id.rainticket.database.Database;
import id.rainticket.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    public List<TrainSchedule> search(String origin, String destination) throws Exception {
        return search(origin, destination, "", "Semua Kelas");
    }

    // Method Overloading: nama sama, parameter berbeda.
    public List<TrainSchedule> search(String origin, String destination, String date) throws Exception {
        return search(origin, destination, date, "Semua Kelas");
    }

    public List<TrainSchedule> search(String origin, String destination, String date, String className) throws Exception {
        List<TrainSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE lower(origin) LIKE ? AND lower(destination) LIKE ? " +
                "AND travel_date LIKE ? AND (?='Semua Kelas' OR class_name=?) ORDER BY travel_date,depart";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + origin.toLowerCase() + "%");
            p.setString(2, "%" + destination.toLowerCase() + "%");
            p.setString(3, date == null || date.isBlank() ? "%" : date);
            p.setString(4, className);
            p.setString(5, className);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
        }
        return list;
    }

    public List<TrainSchedule> findAll() throws Exception {
        return search("", "", "", "Semua Kelas");
    }

    public void save(TrainSchedule s) throws Exception {
        String sql = s.getId() == 0
                ? "INSERT INTO schedules(train,origin,destination,travel_date,depart,arrive,price,seats,class_name) VALUES(?,?,?,?,?,?,?,?,?)"
                : "UPDATE schedules SET train=?,origin=?,destination=?,travel_date=?,depart=?,arrive=?,price=?,seats=?,class_name=? WHERE id=?";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            if (s.getId() != 0) p.setInt(10, s.getId());
            p.executeUpdate();
        }
    }

    public void delete(int id) throws Exception {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("DELETE FROM schedules WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, TrainSchedule s) throws Exception {
        p.setString(1, s.getTrain().getName()); p.setString(2, s.getOrigin().getName());
        p.setString(3, s.getDestination().getName()); p.setString(4, s.getDate());
        p.setString(5, s.getDepart()); p.setString(6, s.getArrive()); p.setInt(7, s.getPrice());
        p.setInt(8, s.getSeats()); p.setString(9, s.getClassName());
    }

    private TrainSchedule map(ResultSet r) throws Exception {
        return new TrainSchedule(r.getInt("id"), new Train(0, r.getString("train")),
                new Station(r.getString("origin"), ""), new Station(r.getString("destination"), ""),
                r.getString("travel_date"), r.getString("depart"), r.getString("arrive"),
                r.getInt("price"), r.getInt("seats"), r.getString("class_name"));
    }
}
