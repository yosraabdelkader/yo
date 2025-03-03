package com.example.demo.controllers;

import com.example.demo.enums.enums_TP.UserRole;
import com.example.demo.services_TP.ServiceUser;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

public class loginController implements Initializable {

    private ServiceUser serviceUser;
    private String generatedOTP;
    private String generatedCaptcha;
    private DefaultKaptcha captchaProducer;
    private String userEmail;

    @FXML
    private TextField login;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField captchaField;
    @FXML
    private Button log;

    @FXML
    private Button regs;

    @FXML
    private Button forgotPasswordBtn;
    @FXML
    private ImageView captchaImageView; // ImageView for CAPTCHA

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceUser = new ServiceUser();
        setupCaptcha();
        generateCaptcha();
    }

    private void setupCaptcha() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.textproducer.char.string", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
        properties.setProperty("kaptcha.textproducer.char.length", "6");
        properties.setProperty("kaptcha.textproducer.font.size", "40");
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");

        Config config = new Config(properties);
        captchaProducer = new DefaultKaptcha();
        captchaProducer.setConfig(config);
    }

    private Image convertBufferedImageToFXImage(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateCaptcha() {
        generatedCaptcha = captchaProducer.createText();
        BufferedImage bufferedImage = captchaProducer.createImage(generatedCaptcha);

        // Convert BufferedImage to JavaFX Image
        Image captchaImage = convertBufferedImageToFXImage(bufferedImage);
        captchaImageView.setImage(captchaImage);
    }

    @FXML
    void refreshCaptcha(ActionEvent event) {
        generateCaptcha(); // Generate a new CAPTCHA
    }

    @FXML
    void login(ActionEvent event) {
        if (login.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }
        if (!captchaField.getText().equalsIgnoreCase(generatedCaptcha)) {
            showAlert("Erreur", "CAPTCHA incorrect !");
            generateCaptcha(); // Refresh CAPTCHA
            return;
        }
        String username = login.getText();
        String password = passwordField.getText();

        if (isUserDisabled(username)) {
            showAlert("Compte Désactivé", "Vous êtes désactivé par l'administrateur.");
            return;
        }

        String hashedPasswordFromDB = serviceUser.getHashedPasswordForUser(username);
        if (hashedPasswordFromDB == null) {
            showAlert("Erreur", "Nom d'utilisateur incorrect.");
            return;
        }

        if (BCrypt.checkpw(password, hashedPasswordFromDB)) {
            UserRole role = serviceUser.getRole(username);
            showAlert("Succès", "Login réussi !");

            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();

                FXMLLoader loader = new FXMLLoader();
                String fxmlFile = getFxmlFileForRole(role);
                loader.setLocation(getClass().getResource(fxmlFile));

                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage newStage = new Stage();
                newStage.setScene(scene);
                newStage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    private String getFxmlFileForRole(UserRole role) {
        switch (role) {
            case ADMIN:
                return "/Ressource-TP/AcceuilAdmin.fxml";
            case COVOITUREUR:
                return "/com/example/demo/AcceuilCovoitureur.fxml";
            case CHAUFFEUR:
                return "/com/example/demo/AcceuilChauffeur.fxml";
            case TAXI:
                return "/com/example/demo/AcceuilTaxi.fxml";
            default:
                return "/Ressource-TP/AcceuilUser.fxml";
        }
    }

    private boolean isUserDisabled(String username) {
        String status = serviceUser.getUserStatus(username);
        return "disable".equalsIgnoreCase(status);
    }

    @FXML
    void register(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ressource-TP/registreUser.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void forgotPassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Entrez votre email");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            userEmail = email.trim();

            if (!serviceUser.isEmailExists(userEmail)) {
                showAlert("Erreur", "Email non trouvé !");
            } else {
                generatedOTP = generateOTP();
                serviceUser.sendOTPEmail(userEmail, generatedOTP);
                showAlert("Succès", "Un code OTP a été envoyé à votre email.");
                showOTPDialog();
            }
        });
    }

    private void showOTPDialog() {
        TextInputDialog otpDialog = new TextInputDialog();
        otpDialog.setTitle("Entrer l'OTP");
        otpDialog.setHeaderText("Vérifiez votre email et entrez le code OTP.");
        otpDialog.setContentText("OTP:");

        Optional<String> otpResult = otpDialog.showAndWait();
        otpResult.ifPresent(enteredOTP -> {
            if (enteredOTP.equals(generatedOTP)) {
                showPasswordResetDialog();
            } else {
                showAlert("Erreur", "OTP incorrect !");
                showOTPDialog();
            }
        });
    }

    private void showPasswordResetDialog() {
        Dialog<String> resetDialog = new Dialog<>();
        resetDialog.setTitle("Réinitialisation du mot de passe");
        resetDialog.setHeaderText("Entrez votre nouveau mot de passe");

        GridPane grid = new GridPane();
        PasswordField newPassword = new PasswordField();
        PasswordField confirmPassword = new PasswordField();
        newPassword.setPromptText("Nouveau mot de passe");
        confirmPassword.setPromptText("Confirmez le mot de passe");

        grid.add(new Label("Nouveau mot de passe:"), 0, 0);
        grid.add(newPassword, 1, 0);
        grid.add(new Label("Confirmez le mot de passe:"), 0, 1);
        grid.add(confirmPassword, 1, 1);
        resetDialog.getDialogPane().setContent(grid);

        ButtonType resetButton = new ButtonType("Réinitialiser", ButtonBar.ButtonData.OK_DONE);
        resetDialog.getDialogPane().getButtonTypes().addAll(resetButton, ButtonType.CANCEL);

        resetDialog.setResultConverter(dialogButton -> dialogButton == resetButton ? newPassword.getText() : null);

        Optional<String> newPasswordResult = resetDialog.showAndWait();
        newPasswordResult.ifPresent(password -> {
            if (!password.equals(confirmPassword.getText())) {
                showAlert("Erreur", "Les mots de passe ne correspondent pas !");
                showPasswordResetDialog();
            } else {
                serviceUser.updatePassword(userEmail, password);
                showAlert("Succès", "Mot de passe changé avec succès !");
            }
        });
    }

    private String generateOTP() {
        Random random = new SecureRandom();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
