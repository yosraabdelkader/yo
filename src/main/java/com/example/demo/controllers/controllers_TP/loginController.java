package com.example.demo.controllers.controllers_TP;

import com.example.demo.models.User;
import com.example.demo.enums.UserRole;
import com.example.demo.Services.ServiceUser;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.*;
import static spark.Spark.*;

import com.github.scribejava.apis.FacebookApi;

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
    private static final String SCOPE = "public_profile"; // Requested permissions
    @FXML
    private Button btnFacebookLogin;
    @FXML
    private ImageView captchaImageView; // ImageView for CAPTCHA
    private static final String CLIENT_ID = "1169223098132441";
    private static final String CLIENT_SECRET = "70abce6a57ea9d729a9db147267772af"; // Replace with your Facebook App Secret
    private static final String REDIRECT_URI = "http://localhost:8081/callback"; // Redirect URI
    @FXML
    private Button facebookLoginButton;


    @FXML
    public void handleFacebookLogin() {
        // Ensure all required parameters are set
        if (CLIENT_ID == null || CLIENT_ID.isEmpty()) {
            showAlert("Error", "Facebook App ID (CLIENT_ID) is missing or invalid.");
            return;
        }
        if (CLIENT_SECRET == null || CLIENT_SECRET.isEmpty()) {
            showAlert("Error", "Facebook App Secret (CLIENT_SECRET) is missing or invalid.");
            return;
        }
        if (REDIRECT_URI == null || REDIRECT_URI.isEmpty()) {
            showAlert("Error", "Redirect URI (REDIRECT_URI) is missing or invalid.");
            return;
        }
        if (SCOPE == null || SCOPE.isEmpty()) {
            showAlert("Error", "Scope (SCOPE) is missing or invalid.");
            return;
        }

        // Create the Facebook OAuth service
        OAuth20Service service = new ServiceBuilder(CLIENT_ID)
                .apiSecret(CLIENT_SECRET)
                .callback(REDIRECT_URI)
                .defaultScope(SCOPE)
                .responseType("code") // Explicitly set the response type
                .build(FacebookApi.instance());

        // Get the authorization URL without PKCE
        String authorizationUrl = service.getAuthorizationUrl();
        System.out.println("Authorization URL: " + authorizationUrl); // Log the authorization URL

        // Open the authorization URL in the default browser
        openFacebookLoginPage(authorizationUrl);

        // Start a local server to handle the redirect URI
        startLocalServer(service);
    }

    private void openFacebookLoginPage(String authorizationUrl) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authorizationUrl));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Facebook login page.");
        }
    }
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    private void startLocalServer(OAuth20Service service) {
        try {
            port(8081); // Match the port in your REDIRECT_URI
            get("/callback", (req, res) -> {
                // Extract the authorization code from the query parameters
                String code = req.queryParams("code");
                if (code == null || code.isEmpty()) {
                    return "Error: No authorization code found in the redirect URI.";
                }
                System.out.println("Authorization Code: " + code);

                // Exchange the authorization code for an access token
                try {
                    OAuth2AccessToken accessToken = service.getAccessToken(code);
                    System.out.println("Access Token: " + accessToken.getAccessToken());

                    // Use the access token to fetch user info
                    String userInfo = fetchUserInfo(accessToken);
                    System.out.println("User Info: " + userInfo);

                    // Log the user into your app
                    loginUserWithFacebook(userInfo);

                    // Stop the server after handling the callback
                    stop();
                    return "Login successful! You can close this window.";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error: Failed to exchange authorization code for access token.";
                }
            });
            System.out.println("Local server started. Listening for redirect URI...");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to start local server. Port 8081 might be in use.");
        }
    }

    private String fetchUserInfo(OAuth2AccessToken accessToken) throws Exception {
        // Use the access token to fetch user info from Facebook's Graph API
        String url = "https://graph.facebook.com/v12.0/me?fields=id,name,email&access_token=" + accessToken.getAccessToken();
        return new java.util.Scanner(new java.net.URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();
    }

    private void loginUserWithFacebook(String userInfo) {
        // Parse the user info (e.g., JSON) and log the user into your app
        System.out.println("Logged in with Facebook: " + userInfo);

        // Parse the JSON response
        org.json.JSONObject json = new org.json.JSONObject(userInfo);
        //String email = json.optString("email", "");
        String name = json.optString("name", "");

        if (name.isEmpty()) {
            showAlert("Error", "No email found in Facebook profile.");
            return;
        }

        // Check if the user already exists in your database
        if (serviceUser.isNameExists(name)) {
            // Log in the existing user
           // UserRole role = serviceUser.getRoleByEmail(email);
            navigateToRoleSpecificScreen(UserRole.ADMIN);
        } else {
            // Register the new user
            String password = generateRandomPassword(); // Generate a random password
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            User u = new User();
            u.setEmail("name@example.com");
            u.setFirstName(name);
            u.setLastName(name);
            u.setRole(UserRole.ADMIN);
            u.setPassword(hashedPassword);
            u.setImage("");
            u.setPhoneNumber("28252525");


            // Register the user with a default role (e.g., USER)
            serviceUser.ajouter(u);
            Platform.runLater(() -> navigateToRoleSpecificScreen(UserRole.ADMIN));

            // Log in the new user
            navigateToRoleSpecificScreen(UserRole.ADMIN);
        }
    }
    private void navigateToRoleSpecificScreen(UserRole role) {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) btnFacebookLogin.getScene().getWindow();
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
        });
    }
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
}
