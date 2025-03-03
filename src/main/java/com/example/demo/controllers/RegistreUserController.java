package com.example.demo.controllers;

import java.io.File;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.demo.models.models_TP.User;
import com.example.demo.enums.enums_TP.UserRole;
import com.example.demo.services_TP.ServiceUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class RegistreUserController implements Initializable {

    @FXML
    private Label nom;
    @FXML
    private Button btnAjouter;
    @FXML
    private TextField urlTF;
    @FXML
    private ComboBox<String> roleComboBox; // Role selection ComboBox
    private File file;

    private boolean isValid(String email) {
        String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isvalid(String num) {
        return num.matches("\\d{8}") && !"12345678".equals(num);
    }

    @FXML
    private Button btnChoisir;
    @FXML
    private ImageView imageview;
    @FXML
    private TextField txtemail;
    @FXML
    private TextField txtnom1;
    @FXML
    private TextField txtnom2;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordField2;
    @FXML
    private TextField txtnumT;
    @FXML
    private Stage stage;

    @FXML
    void ajouter(ActionEvent event) {
        boolean test = false;
        String nom, email, nom2, pass, num_t, selectedRole;
        nom = txtnom1.getText();
        nom2 = txtnom2.getText();
        email = txtemail.getText();
        num_t = txtnumT.getText();
        pass = passwordField.getText();
        String pass2 = passwordField2.getText();
        selectedRole = roleComboBox.getValue(); // Get the selected role

        if (nom.isEmpty() || nom2.isEmpty() || email.isEmpty() || num_t.isEmpty()
                || urlTF.getText().isEmpty() || pass.isEmpty() || pass2.isEmpty() || selectedRole == null) {
            showAlert("Oups", "Veuillez remplir tous les champs !");
            return;
        }

        if (!isValid(email)) {
            showAlert("Erreur", "Veuillez entrer une adresse email valide !");
            return;
        }

        if (!isvalid(num_t)) {
            showAlert("Erreur", "Veuillez entrer un numéro valide !");
            return;
        }

        if (!pass.equals(pass2)) {
            showAlert("Erreur", "Les mots de passe ne correspondent pas !");
            return;
        }

        User nouvelleUser = new User();
        nouvelleUser.setFirstName(nom);
        nouvelleUser.setLastName(nom2);
        nouvelleUser.setEmail(email);
        nouvelleUser.setPhoneNumber(num_t);
        nouvelleUser.setImage(file.getAbsolutePath());
        nouvelleUser.setRole(UserRole.valueOf(selectedRole)); // Set role using enum

        // Hash password before storing
        String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
        nouvelleUser.setPassword(hashedPassword);

        // Save the user
        ServiceUser serviceUser = new ServiceUser();
        serviceUser.ajouter(nouvelleUser);

        showAlert("Succès", "Utilisateur enregistré avec succès !");

        try {
            // Close the registration window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            // Redirect to login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ressource-TP/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage2 = new Stage();
            stage2.setScene(scene);
            stage2.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void importer(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        File fichierSelectionne = fileChooser.showOpenDialog(stage);

        if (fichierSelectionne != null) {
            urlTF.setText(fichierSelectionne.getName());
            file = fichierSelectionne;
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        roleComboBox.getItems().addAll("ADMIN", "COVOITUREUR", "CHAUFFEUR", "TAXI");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
