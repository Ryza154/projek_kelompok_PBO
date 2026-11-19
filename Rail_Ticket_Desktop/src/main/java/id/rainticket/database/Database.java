package id.rainticket.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;

public class Database {
    private static final String URL = "jdbc:sqlite:db_rail_ticket_desktop.db";

    private Database() { }

    public static Connection connect() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("PRAGMA foreign_keys = ON");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, role TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS schedules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, train TEXT NOT NULL, origin TEXT NOT NULL, " +
                    "destination TEXT NOT NULL, travel_date TEXT NOT NULL, depart TEXT NOT NULL, " +
                    "arrive TEXT NOT NULL, price INTEGER NOT NULL, seats INTEGER NOT NULL, class_name TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, schedule_id INTEGER NOT NULL, " +
                    "passenger TEXT NOT NULL, seat TEXT NOT NULL, total INTEGER NOT NULL, status TEXT NOT NULL, " +
                    "booking_date TEXT NOT NULL, FOREIGN KEY(user_id) REFERENCES users(id), " +
                    "FOREIGN KEY(schedule_id) REFERENCES schedules(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, booking_code TEXT NOT NULL, method TEXT NOT NULL, payment_code TEXT NOT NULL, amount INTEGER NOT NULL, status TEXT NOT NULL, paid_at TEXT)");

            statement.executeUpdate("INSERT OR IGNORE INTO users(id,name,email,password,role) VALUES" +
                    "(1,'Administrator','admin@rainticket.id','admin123','ADMIN')");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS payments (id INTEGER PRIMARY KEY AUTOINCREMENT, booking_code TEXT NOT NULL, method TEXT NOT NULL, payment_code TEXT NOT NULL, amount INTEGER NOT NULL, status TEXT NOT NULL, paid_at TEXT)");

            statement.executeUpdate("INSERT OR IGNORE INTO users(id,name,email,password,role) VALUES" +
                    "(2,'Pelanggan','pelanggan@rainticket.id','user123','CUSTOMER')");
            // Data lama: ubah nama akun contoh tanpa mengubah struktur database.
            statement.executeUpdate("UPDATE users SET name='Pelanggan' WHERE id=2 AND name='Pelanggan Demo'");

            try (var result = statement.executeQuery("SELECT COUNT(*) FROM schedules")) {
                if (result.next() && result.getInt(1) < 10) {
                    String sql = "INSERT INTO schedules(train,origin,destination,travel_date,depart,arrive,price,seats,class_name) VALUES" +
                            "('Bengawan','Pasar Senen','Solo Balapan','" + LocalDate.now().plusDays(1) + "','06:00','14:10',180000,55,'Ekonomi')," +
                            "('Fajar Utama','Pasar Senen','Yogyakarta','" + LocalDate.now().plusDays(1) + "','06:40','14:20',260000,42,'Ekonomi')," +
                            "('Argo Parahyangan','Gambir','Bandung','" + LocalDate.now().plusDays(1) + "','07:30','10:25',150000,35,'Eksekutif')," +
                            "('Papandayan','Gambir','Garut','" + LocalDate.now().plusDays(1) + "','09:15','13:00',220000,30,'Eksekutif')," +
                            "('Matarmaja','Pasar Senen','Malang','" + LocalDate.now().plusDays(2) + "','10:20','01:30',240000,48,'Ekonomi')," +
                            "('Bima','Gambir','Surabaya Gubeng','" + LocalDate.now().plusDays(2) + "','17:00','04:45',590000,32,'Eksekutif')," +
                            "('Sancaka','Yogyakarta','Surabaya Gubeng','" + LocalDate.now().plusDays(2) + "','07:00','11:10',210000,40,'Ekonomi')," +
                            "('Mutiara Selatan','Bandung','Surabaya Gubeng','" + LocalDate.now().plusDays(2) + "','16:50','06:30',430000,28,'Bisnis')," +
                            "('Kertajaya','Pasar Senen','Surabaya Pasar Turi','" + LocalDate.now().plusDays(3) + "','14:00','01:10',250000,52,'Ekonomi')," +
                            "('Turangga','Bandung','Surabaya Gubeng','" + LocalDate.now().plusDays(3) + "','18:10','06:00',480000,25,'Eksekutif')," +
                            "('Pangandaran','Gambir','Banjar','" + LocalDate.now().plusDays(3) + "','09:00','14:30',190000,36,'Bisnis')";
                    statement.executeUpdate(sql);
                }
            }

            // Jadwal contoh tambahan: ditambahkan tanpa mengubah struktur database.
            addScheduleIfMissing(connection, "Ciremai", "Gambir", "Cirebon", LocalDate.now().plusDays(1).toString(), "05:40", "08:35", 135000, 40, "Ekonomi");
            addScheduleIfMissing(connection, "Tegal Bahari", "Pasar Senen", "Tegal", LocalDate.now().plusDays(1).toString(), "07:00", "10:15", 115000, 38, "Ekonomi");
            addScheduleIfMissing(connection, "Lodaya", "Bandung", "Yogyakarta", LocalDate.now().plusDays(1).toString(), "06:10", "13:20", 285000, 32, "Bisnis");
            addScheduleIfMissing(connection, "Gajayana", "Gambir", "Malang", LocalDate.now().plusDays(1).toString(), "18:30", "08:10", 650000, 28, "Eksekutif");
            addScheduleIfMissing(connection, "Jayabaya", "Pasar Senen", "Surabaya Pasar Turi", LocalDate.now().plusDays(1).toString(), "12:00", "23:20", 310000, 44, "Ekonomi");
            addScheduleIfMissing(connection, "Serayu", "Pasar Senen", "Purwokerto", LocalDate.now().plusDays(1).toString(), "09:30", "16:45", 125000, 45, "Ekonomi");
            addScheduleIfMissing(connection, "Malioboro Ekspres", "Malang", "Yogyakarta", LocalDate.now().plusDays(1).toString(), "20:00", "04:10", 230000, 30, "Eksekutif");
            addScheduleIfMissing(connection, "Parahyangan", "Gambir", "Bandung", LocalDate.now().plusDays(1).toString(), "10:00", "12:55", 170000, 36, "Bisnis");
            addScheduleIfMissing(connection, "Kahuripan", "Kiaracondong", "Blitar", LocalDate.now().plusDays(2).toString(), "17:20", "06:30", 85000, 50, "Ekonomi");
            addScheduleIfMissing(connection, "Ranggajati", "Cirebon", "Jember", LocalDate.now().plusDays(2).toString(), "08:00", "19:45", 295000, 34, "Bisnis");
            addScheduleIfMissing(connection, "Mataram", "Pasar Senen", "Solo Balapan", LocalDate.now().plusDays(2).toString(), "20:15", "04:30", 320000, 30, "Eksekutif");
            addScheduleIfMissing(connection, "Sritanjung", "Lempuyangan", "Ketapang", LocalDate.now().plusDays(3).toString(), "07:00", "18:20", 95000, 48, "Ekonomi");
        } catch (Exception exception) {
            throw new RuntimeException("Database tidak dapat disiapkan: " + exception.getMessage(), exception);
        }
    }

    private static void addScheduleIfMissing(Connection connection, String train, String origin,
            String destination, String date, String depart, String arrive, int price, int seats,
            String className) throws Exception {
        String exists = "SELECT COUNT(*) FROM schedules WHERE train=? AND travel_date=?";
        try (var check = connection.prepareStatement(exists)) {
            check.setString(1, train);
            check.setString(2, date);
            try (var result = check.executeQuery()) {
                if (result.next() && result.getInt(1) > 0) return;
            }
        }
        String insert = "INSERT INTO schedules(train,origin,destination,travel_date,depart,arrive,price,seats,class_name) VALUES(?,?,?,?,?,?,?,?,?)";
        try (var statement = connection.prepareStatement(insert)) {
            statement.setString(1, train);
            statement.setString(2, origin);
            statement.setString(3, destination);
            statement.setString(4, date);
            statement.setString(5, depart);
            statement.setString(6, arrive);
            statement.setInt(7, price);
            statement.setInt(8, seats);
            statement.setString(9, className);
            statement.executeUpdate();
        }
    }
}