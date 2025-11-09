module org.hogwartsencriptado {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.hogwartsencriptado to javafx.fxml;
    exports org.hogwartsencriptado;
}