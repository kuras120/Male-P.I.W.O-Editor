module malepiwo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.desktop;

    opens editor to javafx.fxml;
    exports editor;
}
