package com.example.demo.controllers.controllers_TP;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FXML Controller class
 */
public class AcceuilAdminController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void openModifInterface3(ActionEvent event) {
        try {
            // Fermer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            // Charger ListeDesArticles.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/ListeDesArticles.fxml"));
            Parent root = loader.load();

            // Afficher la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage2 = new Stage();
            stage2.setScene(scene);
            stage2.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void openModifInterface2(ActionEvent event) {
        try {
            // Fermer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            // Charger ajouterArticle.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/ajouterArticle.fxml"));
            Parent root = loader.load();

            // Afficher la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage2 = new Stage();
            stage2.setScene(scene);
            stage2.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openModifInterface4(ActionEvent event) {
        try {
            // Fermer la fenêtre actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            // Charger ListeUsers.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ressource-TP/ListeUsers.fxml"));
            Parent root = loader.load();

            // Afficher la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage2 = new Stage();
            stage2.setScene(scene);
            stage2.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
