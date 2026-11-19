package id.rainticket.ui;

import id.rainticket.model.Booking;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dialog pembayaran Rail Ticket (simulasi).
 *
 * Pilihan pembayaran dibagi dua tingkat:
 *   1. Metode   : Transfer Bank, Virtual Account, E-Wallet, QRIS
 *   2. Pilihan  : BCA, BNI, BRI, Mandiri, dan lainnya
 *
 * Tata cara pembayaran ditampilkan berurutan ke bawah, per bagian
 * (ATM, Mobile Banking, dst.). Dialog hanya mengembalikan nama metode;
 * penyimpanan ke database tetap dilakukan oleh DashboardController
 * dengan tabel dan kolom yang sama seperti sebelumnya.
 */
public class PaymentDialog {

    /** Satu bagian tata cara, misalnya "Melalui ATM BCA". */
    private record Section(String title, List<String> steps) { }

    /** Satu pilihan pembayaran, misalnya "BCA". */
    private record Option(String name, List<String> info, List<Section> sections) { }

    private static final String TRANSFER = "Transfer Bank";
    private static final String VIRTUAL_ACCOUNT = "Virtual Account";
    private static final String E_WALLET = "E-Wallet";
    private static final String QRIS = "QRIS";

    private final Booking booking;
    private final String amount;
    private final Map<String, List<Option>> catalog = new LinkedHashMap<>();

    public PaymentDialog(Booking booking) {
        this.booking = booking;
        this.amount = "Rp " + String.format("%,d", booking.getTotal());
        buildCatalog();
    }

    /** Menampilkan dialog dan mengembalikan nama metode yang dikonfirmasi (mis. "Transfer Bank - BCA"). */
    public Optional<String> showAndWait() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Pembayaran Rail Ticket");
        dialog.setHeaderText("Selesaikan pembayaran pesanan " + booking.getCode());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Konfirmasi Pembayaran");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm());

        ComboBox<String> methodBox = new ComboBox<>(FXCollections.observableArrayList(catalog.keySet()));
        ComboBox<String> optionBox = new ComboBox<>();
        methodBox.setPrefWidth(240);
        optionBox.setPrefWidth(240);
        Label optionLabel = new Label("Pilih bank / penyedia:");
        VBox detailBox = new VBox(10);

        Runnable refreshDetail = () -> showDetail(detailBox, methodBox.getValue(), optionBox.getValue());

        methodBox.setOnAction(e -> {
            String method = methodBox.getValue();
            List<String> names = new ArrayList<>();
            for (Option o : catalog.get(method)) names.add(o.name());
            optionBox.setItems(FXCollections.observableArrayList(names));
            optionBox.setValue(names.get(0));
            boolean single = names.size() == 1;
            optionBox.setVisible(!single);
            optionBox.setManaged(!single);
            optionLabel.setVisible(!single);
            optionLabel.setManaged(!single);
            refreshDetail.run();
        });
        optionBox.setOnAction(e -> { if (optionBox.getValue() != null) refreshDetail.run(); });
        methodBox.setValue(TRANSFER);
        methodBox.getOnAction().handle(null);

        Label total = new Label("Total pembayaran: " + amount);
        total.getStyleClass().add("pay-total");
        Label methodLabel = new Label("Metode pembayaran:");

        VBox content = new VBox(10, total, methodLabel, methodBox, optionLabel, optionBox, new Separator(), detailBox);
        content.setPadding(new Insets(4, 8, 4, 4));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportWidth(560);
        scroll.setPrefViewportHeight(470);
        dialog.getDialogPane().setContent(scroll);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return Optional.empty();
        return Optional.of(label(methodBox.getValue(), optionBox.getValue()));
    }

    /** Nama yang disimpan ke kolom payments.method. */
    private static String label(String method, String option) {
        return QRIS.equals(method) ? QRIS : method + " - " + option;
    }

    // ------------------------------------------------------------------
    // Tampilan detail & tata cara (disusun berurutan ke bawah)
    // ------------------------------------------------------------------

    private void showDetail(VBox box, String method, String optionName) {
        box.getChildren().clear();
        Option option = catalog.get(method).stream()
                .filter(o -> o.name().equals(optionName)).findFirst().orElse(null);
        if (option == null) return;

        Label title = new Label(method.toUpperCase() + " " + (QRIS.equals(method) ? "" : option.name() + " ") + "(SIMULASI)");
        title.getStyleClass().add("pay-title");
        box.getChildren().add(title);

        VBox info = new VBox(4);
        info.getStyleClass().add("pay-info");
        for (String line : option.info()) info.getChildren().add(wrapped(line, "pay-info-text"));
        box.getChildren().add(info);

        for (Section section : option.sections()) {
            Label heading = new Label(section.title());
            heading.getStyleClass().add("pay-section");
            box.getChildren().add(heading);
            int no = 1;
            for (String step : section.steps()) {
                Label number = new Label(String.valueOf(no++));
                number.getStyleClass().add("pay-step-no");
                number.setMinWidth(24);
                number.setAlignment(javafx.geometry.Pos.CENTER);
                HBox row = new HBox(10, number, wrapped(step, "pay-step-text"));
                row.setAlignment(javafx.geometry.Pos.TOP_LEFT);
                box.getChildren().add(row);
            }
        }

        if (QRIS.equals(method)) {
            try {
                ImageView view = new ImageView(new Image(getClass().getResourceAsStream("/images/qris-demo.png")));
                view.setFitWidth(170);
                view.setFitHeight(170);
                view.setPreserveRatio(true);
                Label note = new Label("QRIS DEMO - BUKAN QR PEMBAYARAN NYATA");
                note.getStyleClass().add("pay-warning");
                box.getChildren().addAll(view, note);
            } catch (Exception ignored) { }
        }
    }

    private static Label wrapped(String text, String styleClass) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.getStyleClass().add(styleClass);
        HBox.setHgrow(l, Priority.ALWAYS);
        return l;
    }

    // ------------------------------------------------------------------
    // Data metode pembayaran (semua nomor hanya contoh simulasi)
    // ------------------------------------------------------------------

    private void buildCatalog() {
        String code = booking.getCode();
        String digits = code.replace("RT-", "");

        // ---- Transfer Bank ----
        List<Option> transfer = new ArrayList<>();
        transfer.add(bankTransfer("BCA", "1234567890", "BCA Mobile / m-BCA", "014"));
        transfer.add(bankTransfer("BNI", "0987654321", "BNI Mobile Banking", "009"));
        transfer.add(bankTransfer("BRI", "0123456789012", "BRImo", "002"));
        transfer.add(bankTransfer("Mandiri", "1230098765432", "Livin' by Mandiri", "008"));
        transfer.add(otherBankTransfer());
        catalog.put(TRANSFER, transfer);

        // ---- Virtual Account ----
        List<Option> va = new ArrayList<>();
        va.add(virtualAccount("BCA", "39" + digits, "BCA Mobile / m-BCA", "Transfer > BCA Virtual Account"));
        va.add(virtualAccount("BNI", "8848" + digits, "BNI Mobile Banking", "Transfer > Virtual Account Billing"));
        va.add(virtualAccount("BRI", "88810" + digits, "BRImo", "Pembayaran > BRIVA"));
        va.add(virtualAccount("Mandiri", "88908" + digits, "Livin' by Mandiri", "Bayar > Multipayment"));
        va.add(virtualAccount("Permata", "8528" + digits, "PermataMobile X", "Bayar > Virtual Account"));
        catalog.put(VIRTUAL_ACCOUNT, va);

        // ---- E-Wallet ----
        List<Option> wallet = new ArrayList<>();
        for (String name : new String[]{"GoPay", "OVO", "DANA", "ShopeePay", "LinkAja"}) {
            wallet.add(eWallet(name, code));
        }
        catalog.put(E_WALLET, wallet);

        // ---- QRIS ----
        List<Option> qris = new ArrayList<>();
        qris.add(new Option("QRIS", List.of(
                "Nominal: " + amount,
                "Kode pesanan: " + code,
                "QR di bawah hanya contoh/demo dan TIDAK dapat digunakan untuk pembayaran sungguhan."),
                List.of(new Section("Tata cara pembayaran QRIS", List.of(
                        "Buka aplikasi bank atau e-wallet yang mendukung QRIS.",
                        "Pilih menu Scan / Bayar QRIS.",
                        "Pada sistem asli, pindai QRIS merchant resmi.",
                        "Periksa nama merchant dan nominal tagihan.",
                        "Masukkan PIN untuk menyelesaikan pembayaran.",
                        "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran.")))));
        catalog.put(QRIS, qris);
    }

    private Option bankTransfer(String bank, String account, String mobileApp, String bankCode) {
        List<String> info = List.of(
                "Bank tujuan: " + bank,
                "Nomor rekening: " + account,
                "Atas nama: PT Rail Ticket Indonesia",
                "Nominal: " + amount);
        Section atm = new Section("A. Melalui ATM " + bank, List.of(
                "Masukkan kartu ATM " + bank + " dan PIN Anda.",
                "Pilih menu Transfer, lalu pilih transfer ke rekening " + bank + " (sesama bank).",
                "Masukkan nomor rekening tujuan: " + account + ".",
                "Masukkan nominal sebesar " + amount + ".",
                "Periksa nama penerima (PT Rail Ticket Indonesia) dan nominal, lalu pilih Ya / Lanjut.",
                "Simpan struk sebagai bukti pembayaran.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        Section mobile = new Section("B. Melalui " + mobileApp, List.of(
                "Buka aplikasi " + mobileApp + " dan login.",
                "Pilih menu Transfer, lalu rekening " + bank + " / sesama bank.",
                "Masukkan nomor rekening tujuan: " + account + ".",
                "Masukkan nominal sebesar " + amount + ".",
                "Periksa nama penerima dan nominal, lalu konfirmasi.",
                "Masukkan PIN / OTP untuk menyelesaikan transaksi.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        return new Option(bank, info, List.of(atm, mobile));
    }

    private Option otherBankTransfer() {
        List<String> info = List.of(
                "Untuk bank selain BCA, BNI, BRI, dan Mandiri (mis. CIMB Niaga, Permata, BSI, Danamon).",
                "Bank tujuan: BCA (kode bank 014)",
                "Nomor rekening: 1234567890",
                "Atas nama: PT Rail Ticket Indonesia",
                "Nominal: " + amount + " (belum termasuk biaya transfer antarbank jika ada)");
        Section atm = new Section("A. Melalui ATM Bersama / Prima", List.of(
                "Masukkan kartu ATM bank Anda dan PIN.",
                "Pilih menu Transfer, lalu Transfer Antar Bank.",
                "Masukkan kode bank tujuan: 014 (BCA).",
                "Masukkan nomor rekening tujuan: 1234567890.",
                "Masukkan nominal sebesar " + amount + ".",
                "Periksa nama penerima dan nominal, lalu konfirmasi.",
                "Simpan struk, lalu kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        Section mobile = new Section("B. Melalui Mobile Banking bank lain", List.of(
                "Buka aplikasi mobile banking bank Anda dan login.",
                "Pilih menu Transfer ke Bank Lain / Antar Bank.",
                "Pilih bank tujuan BCA (kode 014).",
                "Masukkan nomor rekening tujuan: 1234567890.",
                "Masukkan nominal sebesar " + amount + ".",
                "Periksa nama penerima dan nominal, lalu masukkan PIN / OTP.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        return new Option("Bank Lainnya", info, List.of(atm, mobile));
    }

    private Option virtualAccount(String bank, String vaNumber, String mobileApp, String menuPath) {
        List<String> info = List.of(
                "Bank: " + bank,
                "Nomor VA: " + vaNumber,
                "Nominal: " + amount);
        Section atm = new Section("A. Melalui ATM " + bank, List.of(
                "Masukkan kartu ATM " + bank + " dan PIN Anda.",
                "Pilih menu Transfer / Pembayaran, lalu Virtual Account.",
                "Masukkan nomor VA: " + vaNumber + ".",
                "Pastikan nama tagihan dan nominal " + amount + " sudah benar.",
                "Konfirmasi dan selesaikan pembayaran.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        Section mobile = new Section("B. Melalui " + mobileApp, List.of(
                "Buka aplikasi " + mobileApp + " dan login.",
                "Pilih menu " + menuPath + ".",
                "Masukkan nomor VA: " + vaNumber + ".",
                "Pastikan nama tagihan dan nominal " + amount + " sudah benar.",
                "Masukkan PIN / OTP untuk menyelesaikan pembayaran.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        return new Option(bank, info, List.of(atm, mobile));
    }

    private Option eWallet(String name, String code) {
        List<String> info = List.of(
                "E-Wallet: " + name,
                "Kode pesanan: " + code,
                "Nominal: " + amount);
        Section steps = new Section("Tata cara pembayaran " + name, List.of(
                "Buka aplikasi " + name + " dan login.",
                "Pilih menu Bayar / Tagihan atau Transfer sesuai instruksi merchant.",
                "Untuk demo ini, gunakan kode pesanan " + code + ".",
                "Periksa nama merchant dan nominal " + amount + ".",
                "Masukkan PIN / OTP untuk menyelesaikan transaksi.",
                "Kembali ke aplikasi dan tekan Konfirmasi Pembayaran."));
        return new Option(name, info, List.of(steps));
    }
}
