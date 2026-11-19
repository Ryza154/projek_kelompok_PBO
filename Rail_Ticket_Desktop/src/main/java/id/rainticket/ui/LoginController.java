package id.rainticket.ui;

import id.rainticket.dao.UserDAO;
import id.rainticket.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordButton;
    @FXML private Label messageLabel;
    private final UserDAO userDAO = new UserDAO();

    /** Menyamakan isi kolom password tersembunyi dan kolom password yang terlihat. */
    @FXML private void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    /** Tombol mata: tampilkan atau sembunyikan password. */
    @FXML private void togglePassword() {
        boolean show = !passwordVisibleField.isVisible();
        passwordVisibleField.setVisible(show);
        passwordVisibleField.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        togglePasswordButton.setText(show ? "🙈" : "👁");
        (show ? passwordVisibleField : passwordField).requestFocus();
        (show ? passwordVisibleField : passwordField).positionCaret(passwordField.getText().length());
    }

    @FXML private void login() {
        try {
            User user = userDAO.login(emailField.getText().trim(), passwordField.getText());
            if (user == null) { messageLabel.setText("Email atau password tidak sesuai."); return; }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Rail Ticket | " + user.getRole());
        } catch (Exception e) { messageLabel.setText("Login gagal: " + e.getMessage()); }
    }

    @FXML private void register() {
        TextInputDialog name = new TextInputDialog();
        name.setTitle("Registrasi"); name.setHeaderText("Buat akun pelanggan"); name.setContentText("Nama lengkap:");
        var n = name.showAndWait();
        if (n.isEmpty() || n.get().isBlank()) return;

        TextInputDialog email = new TextInputDialog();
        email.setTitle("Registrasi"); email.setHeaderText("Alamat email"); email.setContentText("Email:");
        var em = email.showAndWait();
        if (em.isEmpty() || em.get().isBlank()) return;

        TextInputDialog password = new TextInputDialog();
        password.setTitle("Registrasi"); password.setHeaderText("Buat password"); password.setContentText("Password:");
        var pw = password.showAndWait();
        if (pw.isEmpty() || pw.get().isBlank()) return;

        try {
            userDAO.register(n.get(), em.get(), pw.get());
            messageLabel.setText("Registrasi berhasil. Silakan login.");
        } catch (Exception e) { messageLabel.setText("Registrasi gagal. Email mungkin sudah digunakan."); }
    }
}
