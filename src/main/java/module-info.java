module org.hogwartsencriptado {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens org.hogwartsencriptado to javafx.fxml;
    exports org.hogwartsencriptado;
}