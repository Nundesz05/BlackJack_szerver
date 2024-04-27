module com.example.szerver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.szerver to javafx.fxml;
    exports com.example.szerver;
}