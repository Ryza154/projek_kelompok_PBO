package id.rainticket.ui;

import id.rainticket.dao.*;
import id.rainticket.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {
    @FXML private VBox menuBox;
    @FXML private VBox contentBox;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;

    private User currentUser;
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    public void setUser(User user) {
        currentUser = user;
        userLabel.setText(user.getName());
        roleLabel.setText(user.getRole().equals("ADMIN") ? "ADMINISTRATOR" : "PELANGGAN");
        menuBox.getChildren().clear();

        if (user instanceof Admin) {
            addMenu("Dashboard", this::adminHome);
            addMenu("Kelola Jadwal", this::manageSchedules);
            addMenu("Data Pemesanan", this::adminBookings);
            addMenu("Data Pelanggan", this::customers);
            adminHome();
        } else {
            addMenu("Beranda", this::customerHome);
            addMenu("Cari Tiket", this::searchTickets);
            addMenu("Pesanan Saya", this::myBookings);
            customerHome();
        }
        addMenu("Keluar", this::logout);
    }

    private void addMenu(String title, Runnable action) {
        Button button = new Button(title);
        button.getStyleClass().add("menu-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        menuBox.getChildren().add(button);
    }

    private void heading(String title, String subtitle) {
        contentBox.getChildren().clear();
        Label h = new Label(title); h.getStyleClass().add("page-title");
        Label s = new Label(subtitle); s.getStyleClass().add("muted");
        contentBox.getChildren().addAll(h, s);
    }

    private VBox panel(String title, String text) {
        VBox box = new VBox(8); box.getStyleClass().add("panel");
        Label h = new Label(title); h.getStyleClass().add("section-title");
        Label body = new Label(text); body.setWrapText(true);
        box.getChildren().addAll(h, body); return box;
    }

    private Label metric(String value, String title) {
        Label label = new Label(value + "\n" + title);
        label.getStyleClass().add("metric");
        return label;
    }

    private int count(String table) {
        try (var c = id.rainticket.database.Database.connect();
             var s = c.createStatement(); var r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return r.next() ? r.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private void adminHome() {
        heading("Dashboard Admin", currentUser.dashboardInfo());
        HBox stats = new HBox(12, metric(String.valueOf(count("schedules")), "Jadwal Kereta"),
                metric(String.valueOf(count("bookings")), "Total Pemesanan"),
                metric(String.valueOf(count("users") - 1), "Akun Pengguna"));
        contentBox.getChildren().addAll(stats, panel("Selamat datang, " + currentUser.getName(),
                "Kelola jadwal perjalanan, data pelanggan, dan transaksi pemesanan melalui menu di sebelah kiri."));
        try { contentBox.getChildren().add(buildBookingTable(bookingDAO.findAll(), true)); }
        catch (Exception e) { showAlert(e.getMessage()); }
    }

    private void customerHome() {
        heading("Rail Ticket", currentUser.dashboardInfo());
        contentBox.getChildren().addAll(
                panel("Perjalanan Lebih Mudah, Tiket dalam Genggaman!",
                        "Pesan tiket kereta dengan mudah. Cari stasiun asal dan tujuan, tentukan tanggal, pilih kelas, lalu pesan kursi."),
                new HBox(12, metric(String.valueOf(countUserBookings()), "Pesanan Saya"),
                        metric("24/7", "Akses Pemesanan")));
        Button search = new Button("Cari Tiket Sekarang  →"); search.getStyleClass().add("primary");
        search.setOnAction(e -> searchTickets()); contentBox.getChildren().add(search);
    }

    private int countUserBookings() {
        try { return bookingDAO.findByUser(currentUser.getId()).size(); }
        catch (Exception e) { return 0; }
    }

    private void searchTickets() {
        heading("Cari Tiket Kereta", "Pilih rute, tanggal keberangkatan, dan kelas kereta.");
        TextField origin = new TextField(); origin.setPromptText("Stasiun asal");
        TextField destination = new TextField(); destination.setPromptText("Stasiun tujuan");
        DatePicker date = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<String> classBox = new ComboBox<>(FXCollections.observableArrayList("Semua Kelas", "Ekonomi", "Bisnis", "Eksekutif"));
        classBox.setValue("Semua Kelas");
        Button find = new Button("Cari Jadwal"); find.getStyleClass().add("primary");
        HBox form = new HBox(10, origin, destination, date, classBox, find);
        form.getStyleClass().add("search-row"); contentBox.getChildren().add(form);
        Label countLabel = new Label("Pilih tanggal dan tekan Cari Jadwal untuk melihat perjalanan.");
        countLabel.getStyleClass().add("muted");
        VBox results = new VBox(12); contentBox.getChildren().addAll(countLabel, results);

        find.setOnAction(e -> {
            results.getChildren().clear();
            try {
                List<TrainSchedule> schedules = scheduleDAO.search(origin.getText(), destination.getText(),
                        date.getValue() == null ? "" : date.getValue().toString(), classBox.getValue());
                countLabel.setText(schedules.size() + " jadwal ditemukan");
                if (schedules.isEmpty()) {
                    results.getChildren().add(panel("Jadwal tidak ditemukan",
                            "Belum ada jadwal untuk rute, tanggal, atau kelas tersebut. Coba ubah pencarian."));
                    return;
                }
                for (TrainSchedule schedule : schedules) {
                    int available = bookingDAO.getAvailableSeats(schedule.getId(), schedule.getSeats());
                    VBox card = panel(schedule.getTrain().getName() + "  •  " + schedule.getClassName(),
                            "Rute: " + schedule.getOrigin().getName() + " → " + schedule.getDestination().getName() +
                            "\nTanggal: " + schedule.getDate() +
                            "\nBerangkat: " + schedule.getDepart() + "     Tiba: " + schedule.getArrive() +
                            "\nHarga: Rp " + String.format("%,d", schedule.getPrice()) +
                            "     Kursi tersedia: " + available + " dari " + schedule.getSeats());
                    Button book = new Button(available == 0 ? "Kursi Penuh" : "Pilih & Pesan");
                    book.getStyleClass().add(available == 0 ? "outline" : "primary");
                    book.setDisable(available == 0);
                    book.setOnAction(ev -> createBooking(schedule));
                    card.getChildren().add(book);
                    results.getChildren().add(card);
                }
            } catch (Exception ex) { showAlert("Pencarian gagal: " + ex.getMessage()); }
        });
        find.fire();
    }

    private void createBooking(TrainSchedule schedule) {
        TextInputDialog passenger = new TextInputDialog(currentUser.getName());
        passenger.setTitle("Data Penumpang");
        passenger.setHeaderText("Nama sesuai identitas");
        passenger.setContentText("Nama:");
        var name = passenger.showAndWait();
        if (name.isEmpty() || name.get().isBlank()) return;

        try {
            int available = bookingDAO.getAvailableSeats(schedule.getId(), schedule.getSeats());
            if (available <= 0) {
                showAlert("Maaf, seluruh kursi pada jadwal ini sudah penuh. Silakan pilih jadwal lain.");
                searchTickets();
                return;
            }

            while (true) {
                TextInputDialog seat = new TextInputDialog("A1");
                seat.setTitle("Pilih Kursi");
                seat.setHeaderText("Kursi tersedia: " + available + " dari " + schedule.getSeats()
                        + "\nMasukkan nomor kursi (contoh A1, A2, B1). Kursi yang sudah dipesan tidak dapat dipilih.");
                seat.setContentText("Nomor kursi:");
                var seatNo = seat.showAndWait();
                if (seatNo.isEmpty()) return;
                String chosen = seatNo.get().trim().toUpperCase();
                if (!chosen.matches("[A-Z][0-9]{1,2}")) {
                    showAlert("Format kursi tidak sesuai. Contoh yang benar: A1, A2, atau B1.");
                    continue;
                }
                if (bookingDAO.isSeatBooked(schedule.getId(), chosen)) {
                    showAlert("Kursi " + chosen + " sudah dipesan. Silakan pilih nomor kursi lain.");
                    continue;
                }
                bookingDAO.create(currentUser.getId(), schedule.getId(), name.get().trim(), chosen, schedule.getPrice());
                showAlert("Pemesanan berhasil dibuat. Kursi: " + chosen + "\nStatus: MENUNGGU PEMBAYARAN.");
                myBookings();
                return;
            }
        } catch (Exception e) {
            showAlert("Pemesanan gagal: " + e.getMessage());
            searchTickets();
        }
    }

    private void myBookings() {
        heading("Pesanan Saya", "Lihat riwayat transaksi dan e-ticket perjalanan.");
        try {
            List<Booking> bookings = bookingDAO.findByUser(currentUser.getId());
            contentBox.getChildren().add(buildBookingTable(bookings, false));
        } catch (Exception e) { showAlert(e.getMessage()); }
    }

    private void adminBookings() {
        heading("Data Pemesanan", "Admin dapat menghapus data pemesanan atau memproses refund.");
        try { contentBox.getChildren().add(buildBookingTable(bookingDAO.findAll(), true)); }
        catch (Exception e) { showAlert(e.getMessage()); }
    }

    private TableView<Booking> buildBookingTable(List<Booking> bookings, boolean admin) {
        TableView<Booking> table = new TableView<>();
        TableColumn<Booking, String> code = new TableColumn<>("Kode");
        code.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getCode()));
        TableColumn<Booking, String> passenger = new TableColumn<>("Penumpang");
        passenger.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getPassengerName()));
        TableColumn<Booking, String> train = new TableColumn<>("Kereta");
        train.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getSchedule().getTrain().getName()));
        TableColumn<Booking, String> route = new TableColumn<>("Rute");
        route.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(
                v.getValue().getSchedule().getOrigin().getName() + " → " + v.getValue().getSchedule().getDestination().getName()));
        TableColumn<Booking, String> date = new TableColumn<>("Tanggal");
        date.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getSchedule().getDate()));
        TableColumn<Booking, String> seat = new TableColumn<>("Kursi");
        seat.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getSeat()));
        TableColumn<Booking, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getStatus()));
        table.getColumns().addAll(code, passenger, train, route, date, seat, status);
        if (admin) {
            Map<String, String> paymentStatus = loadPaymentStatus();
            TableColumn<Booking, String> pay = new TableColumn<>("Pembayaran");
            pay.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(
                    paymentStatus.getOrDefault(v.getValue().getCode(), "-")));
            table.getColumns().add(pay);
        }
        table.setItems(FXCollections.observableArrayList(bookings));
        table.setPrefHeight(350);

        if (!admin) {
            Button pay = new Button("Bayar Pesanan");
            Button cancelBooking = new Button("Batalkan Tiket");
            Button ticket = new Button("Lihat E-ticket");
            pay.getStyleClass().add("primary"); ticket.getStyleClass().add("outline");
            cancelBooking.getStyleClass().add("danger");
            pay.setOnAction(e -> {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) { showAlert("Pilih pesanan terlebih dahulu."); return; }
                if ("DIBATALKAN".equals(selected.getStatus())) { showAlert("Tiket ini sudah dibatalkan."); return; }
                if ("LUNAS".equals(selected.getStatus())) { showAlert("Pesanan ini sudah dibayar."); return; }
                showPaymentDialog(selected);
            });
            cancelBooking.setOnAction(e -> {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) { showAlert("Pilih tiket yang ingin dibatalkan."); return; }
                if ("DIBATALKAN".equals(selected.getStatus())) { showAlert("Tiket sudah pernah dibatalkan."); return; }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Konfirmasi Pembatalan");
                confirm.setHeaderText("Batalkan tiket " + selected.getCode() + "?");
                confirm.setContentText("Rute: " + selected.getSchedule().getOrigin().getName() + " → " +
                        selected.getSchedule().getDestination().getName() + "\nTotal: Rp " +
                        String.format("%,d", selected.getTotal()) +
                        "\n\nStatus tiket akan menjadi DIBATALKAN. Jika sudah dibayar, pengembalian dana mengikuti proses refund (simulasi) dari admin.");
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    try {
                        bookingDAO.updateStatus(selected.getCode(), "DIBATALKAN");
                        try (var c = id.rainticket.database.Database.connect();
                             var ps = c.prepareStatement("UPDATE payments SET status='REFUND_DIPROSES' WHERE booking_code=? AND status='LUNAS'")) {
                            ps.setString(1, selected.getCode()); ps.executeUpdate();
                        }
                        showAlert("Tiket berhasil dibatalkan. Kode: " + selected.getCode() +
                                "\nStatus: DIBATALKAN\nJika pembayaran sudah lunas, refund ditandai REFUND_DIPROSES (simulasi).");
                        myBookings();
                    } catch (Exception ex) { showAlert("Pembatalan gagal: " + ex.getMessage()); }
                }
            });
            ticket.setOnAction(e -> {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) { showAlert("Pilih pesanan terlebih dahulu."); return; }
                if (!"LUNAS".equals(selected.getStatus())) { showAlert("E-ticket dapat dicetak setelah pembayaran berstatus LUNAS."); return; }
                showAlert(selected.getTicketType() + "\n\n" + selected.new BoardingPass().print());
                try { java.io.File pdf = new id.rainticket.service.TicketPdfService().create(selected); showAlert("Kartu tiket PDF berhasil dibuat di: " + pdf.getAbsolutePath()); if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(pdf); }
                catch (Exception ex) { showAlert("Gagal membuat PDF: " + ex.getMessage()); }
            });
            contentBox.getChildren().add(new HBox(10, pay, ticket, cancelBooking));
        } else {
            Button delete = new Button("Hapus"); delete.getStyleClass().add("danger");
            Button refund = new Button("Refund"); refund.getStyleClass().add("outline");
            delete.setOnAction(e -> deleteBooking(table));
            refund.setOnAction(e -> refundSelected(table));
            contentBox.getChildren().add(new HBox(10, delete, refund));
        }
        return table;
    }

    private void showPaymentDialog(Booking booking) {
        // Tampilan metode & tata cara ada di PaymentDialog; database tetap memakai tabel "payments".
        String methodName = new PaymentDialog(booking).showAndWait().orElse(null);
        if (methodName == null) return;
        try {
            PaymentMethod payment = new SimulatedPayment();
            if (!payment.pay(booking)) return;
            String paymentCode = methodName.equals("QRIS")
                    ? "QR-RT-" + booking.getCode() : "PAY-RT-" + booking.getCode();
            try (var c = id.rainticket.database.Database.connect();
                 var ps = c.prepareStatement("INSERT INTO payments(booking_code,method,payment_code,amount,status) VALUES(?,?,?,?, 'LUNAS')")) {
                ps.setString(1, booking.getCode()); ps.setString(2, methodName);
                ps.setString(3, paymentCode); ps.setInt(4, booking.getTotal()); ps.executeUpdate();
            }
            bookingDAO.updateStatus(booking.getCode(), "LUNAS");
            showAlert("Pembayaran simulasi berhasil.\nMetode: " + methodName +
                    "\nKode: " + paymentCode + "\nE-ticket sudah dapat dicetak.");
            myBookings();
        } catch (Exception ex) { showAlert("Pembayaran gagal: " + ex.getMessage()); }
    }

    /** Status pembayaran terbaru per kode pesanan (tabel payments, tanpa mengubah strukturnya). */
    private Map<String, String> loadPaymentStatus() {
        Map<String, String> map = new HashMap<>();
        try (var c = id.rainticket.database.Database.connect();
             var st = c.createStatement();
             var rs = st.executeQuery("SELECT booking_code, status FROM payments ORDER BY id")) {
            while (rs.next()) map.put(rs.getString(1), rs.getString(2));
        } catch (Exception ignored) { }
        return map;
    }

    /** Refund oleh admin: pembayaran LUNAS / REFUND_DIPROSES menjadi REFUND_SELESAI, tiket DIBATALKAN. */
    private void refundSelected(TableView<Booking> table) {
        Booking selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Pilih pesanan yang akan direfund."); return; }
        String payStatus = loadPaymentStatus().get(selected.getCode());
        if (payStatus == null) { showAlert("Pesanan " + selected.getCode() + " belum dibayar, tidak ada dana yang bisa direfund."); return; }
        if ("REFUND_SELESAI".equals(payStatus)) { showAlert("Pesanan " + selected.getCode() + " sudah direfund."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Refund");
        confirm.setHeaderText("Refund pesanan " + selected.getCode() + "?");
        confirm.setContentText("Penumpang: " + selected.getPassengerName() +
                "\nNominal refund: Rp " + String.format("%,d", selected.getTotal()) +
                "\n\nStatus pembayaran menjadi REFUND_SELESAI dan tiket menjadi DIBATALKAN (simulasi).");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            try (var c = id.rainticket.database.Database.connect();
                 var ps = c.prepareStatement("UPDATE payments SET status='REFUND_SELESAI' WHERE booking_code=? AND status IN ('LUNAS','REFUND_DIPROSES')")) {
                ps.setString(1, selected.getCode()); ps.executeUpdate();
            }
            bookingDAO.updateStatus(selected.getCode(), "DIBATALKAN");
            showAlert("Refund berhasil diproses.\nKode: " + selected.getCode() +
                    "\nNominal: Rp " + String.format("%,d", selected.getTotal()) + "\nStatus: REFUND_SELESAI");
            adminBookings();
        } catch (Exception ex) { showAlert("Refund gagal: " + ex.getMessage()); }
    }

    /** Admin menghapus data pemesanan (beserta data pembayarannya). */
    private void deleteBooking(TableView<Booking> table) {
        Booking selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Pilih pesanan yang akan dihapus."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Pemesanan");
        confirm.setHeaderText("Hapus pesanan " + selected.getCode() + "?");
        confirm.setContentText("Penumpang: " + selected.getPassengerName() +
                "\nStatus: " + selected.getStatus() +
                "\n\nData pemesanan dan data pembayarannya akan dihapus permanen.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            bookingDAO.delete(selected.getCode());
            adminBookings();
        } catch (Exception ex) { showAlert("Gagal menghapus pesanan: " + ex.getMessage()); }
    }

    private void manageSchedules() {
        heading("Kelola Jadwal Kereta", "Tambah, ubah, dan hapus jadwal perjalanan.");
        Button add = new Button("+ Tambah Jadwal"); add.getStyleClass().add("primary");
        add.setOnAction(e -> scheduleForm(null)); contentBox.getChildren().add(add);
        try {
            TableView<TrainSchedule> table = new TableView<>();
            addScheduleColumn(table, "ID", s -> String.valueOf(s.getId()));
            addScheduleColumn(table, "Kereta", s -> s.getTrain().getName());
            addScheduleColumn(table, "Asal", s -> s.getOrigin().getName());
            addScheduleColumn(table, "Tujuan", s -> s.getDestination().getName());
            addScheduleColumn(table, "Tanggal", TrainSchedule::getDate);
            addScheduleColumn(table, "Berangkat", TrainSchedule::getDepart);
            addScheduleColumn(table, "Tiba", TrainSchedule::getArrive);
            addScheduleColumn(table, "Harga", s -> String.valueOf(s.getPrice()));
            addScheduleColumn(table, "Kelas", TrainSchedule::getClassName);
            table.setItems(FXCollections.observableArrayList(scheduleDAO.findAll())); table.setPrefHeight(390);
            contentBox.getChildren().add(table);
            Button edit = new Button("Edit Jadwal"), delete = new Button("Hapus Jadwal");
            edit.setOnAction(e -> { if (table.getSelectionModel().getSelectedItem() != null) scheduleForm(table.getSelectionModel().getSelectedItem()); });
            delete.getStyleClass().add("danger");
            delete.setOnAction(e -> {
                TrainSchedule selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                try { scheduleDAO.delete(selected.getId()); manageSchedules(); }
                catch (Exception ex) { showAlert("Jadwal tidak dapat dihapus jika sudah memiliki pemesanan."); }
            });
            contentBox.getChildren().add(new HBox(10, edit, delete));
        } catch (Exception e) { showAlert(e.getMessage()); }
    }

    private void addScheduleColumn(TableView<TrainSchedule> table, String title,
                                   java.util.function.Function<TrainSchedule, String> getter) {
        TableColumn<TrainSchedule, String> column = new TableColumn<>(title);
        column.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(getter.apply(v.getValue())));
        column.setPrefWidth(105); table.getColumns().add(column);
    }

    private void scheduleForm(TrainSchedule old) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(old == null ? "Tambah Jadwal" : "Edit Jadwal");
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(9);
        String[] labels = {"Nama kereta", "Stasiun asal", "Stasiun tujuan", "Tanggal YYYY-MM-DD",
                "Berangkat HH:MM", "Tiba HH:MM", "Harga", "Jumlah kursi", "Kelas"};
        String[] values = old == null
                ? new String[]{"Kereta Baru", "Gambir", "Bandung", LocalDate.now().plusDays(1).toString(), "08:00", "14:00", "250000", "40", "Ekonomi"}
                : new String[]{old.getTrain().getName(), old.getOrigin().getName(), old.getDestination().getName(),
                old.getDate(), old.getDepart(), old.getArrive(), String.valueOf(old.getPrice()), String.valueOf(old.getSeats()), old.getClassName()};
        TextField[] fields = new TextField[9];
        for (int i = 0; i < fields.length; i++) { fields[i] = new TextField(values[i]); grid.addRow(i, new Label(labels[i]), fields[i]); }
        dialog.getDialogPane().setContent(grid); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                TrainSchedule schedule = new TrainSchedule(old == null ? 0 : old.getId(),
                        new Train(0, fields[0].getText()), new Station(fields[1].getText(), ""),
                        new Station(fields[2].getText(), ""), fields[3].getText(), fields[4].getText(),
                        fields[5].getText(), Integer.parseInt(fields[6].getText()),
                        Integer.parseInt(fields[7].getText()), fields[8].getText());
                scheduleDAO.save(schedule); manageSchedules();
            } catch (Exception e) { showAlert("Data tidak valid: " + e.getMessage()); }
        }
    }

    private void customers() {
        heading("Data Pelanggan", "Daftar akun pelanggan Rail Ticket.");
        TableView<String[]> table = new TableView<>();
        String[] labels = {"ID", "Nama", "Email"};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(labels[i]);
            column.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue()[index]));
            table.getColumns().add(column);
        }
        try (var c = id.rainticket.database.Database.connect();
             var s = c.createStatement();
             var r = s.executeQuery("SELECT id,name,email FROM users WHERE role='CUSTOMER'")) {
            while (r.next()) table.getItems().add(new String[]{r.getString(1), r.getString(2), r.getString(3)});
        } catch (Exception e) { showAlert(e.getMessage()); }
        table.setPrefHeight(400);

        Button delete = new Button("Hapus Pelanggan"); delete.getStyleClass().add("danger");
        delete.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showAlert("Pilih pelanggan yang akan dihapus."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Hapus Pelanggan");
            confirm.setHeaderText("Hapus pelanggan " + selected[1] + "?");
            confirm.setContentText("Email: " + selected[2] +
                    "\n\nAkun beserta seluruh pesanan dan pembayarannya akan dihapus permanen.");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            try {
                int bookings = new UserDAO().deleteCustomer(Integer.parseInt(selected[0]));
                showAlert("Pelanggan " + selected[1] + " berhasil dihapus." +
                        (bookings > 0 ? "\n" + bookings + " pesanan miliknya ikut dihapus." : ""));
                customers();
            } catch (Exception ex) { showAlert("Gagal menghapus pelanggan: " + ex.getMessage()); }
        });
        contentBox.getChildren().addAll(delete, table);
    }

    private void logout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) contentBox.getScene().getWindow();
            stage.setScene(scene); stage.setTitle("Rail Ticket | Login");
        } catch (Exception e) { showAlert(e.getMessage()); }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null); alert.showAndWait();
    }
}
