module malepiwo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens editor to javafx.fxml;
    exports editor;
}