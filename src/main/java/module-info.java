module org.hogwartsencriptado {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    // SLF4J (obligatorio para usar Logger/LoggerFactory)
    requires org.slf4j;
    opens org.hogwartsencriptado to javafx.fxml;
    exports org.hogwartsencriptado;
}