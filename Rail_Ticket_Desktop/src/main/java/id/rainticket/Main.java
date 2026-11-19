package id.rainticket;

import id.rainticket.database.Database;
import id.rainticket.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Database.initialize();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Rail Ticket | Sistem Pemesanan Tiket Kereta");
        stage.setMinWidth(950);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
