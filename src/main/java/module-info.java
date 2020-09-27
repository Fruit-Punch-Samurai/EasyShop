module EasyShop {
    requires javafx.fxml;
    requires javafx.controls;

    requires java.sql;

    opens rh.yahia to javafx.fxml;
    exports rh.yahia;
}