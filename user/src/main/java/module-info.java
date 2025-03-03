module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires qrgen;
    requires itextpdf;
    requires java.desktop;

    requires org.apache.poi.poi;
    requires javafx.media;
    requires javafx.web;
    requires twilio;
    requires org.controlsfx.controls;
    requires stripe.java;
    requires fontawesomefx;
    requires jakarta.mail;
    requires jbcrypt;
    requires opencv;
    requires kaptcha;

    opens com.example.demo.controllers to javafx.fxml;
exports com.example.demo.controllers;
    opens com.example.demo.models.models_TP to javafx.base;  // Permet l'accès via réflexion
    exports com.example.demo;
    opens com.example.demo.enums.enums_TP to javafx.base;
    exports com.example.demo.tests;
}


