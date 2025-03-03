package com.example.demo.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Optional;
import java.util.ResourceBundle;

import com.example.demo.models.models_TP.User;
import com.example.demo.utils.MyDataBase;
import com.example.demo.services_TP.ServiceUser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ListeUsersController implements Initializable {

    private Connection connection = MyDataBase.getInstance().getConnection();

    @FXML
    private GridPane usersGrid; // GridPane to display users

    @FXML
    private TextField filtre;

    private User selectedUser; // Track the selected user

    ObservableList<User> listeB = FXCollections.observableArrayList();

    @FXML
    public void show() {
        ServiceUser bs = new ServiceUser();
        listeB = bs.afficherTous();

        // Clear the GridPane
        usersGrid.getChildren().clear();

        int row = 0;
        int col = 0;

        for (User user : listeB) {
            // Create a VBox to represent a user card
            VBox userCard = new VBox(10);
            userCard.getStyleClass().add("user-card");
            userCard.setPadding(new Insets(10));

            // Add user details to the card
            Label idLabel = new Label("ID: " + user.getId());
            Label nameLabel = new Label("Nom: " + user.getFirstName());
            Label lastNameLabel = new Label("Prénom: " + user.getLastName());
            Label emailLabel = new Label("Email: " + user.getEmail());
            Label phoneLabel = new Label("Téléphone: " + user.getPhoneNumber());
            Label statusLabel = new Label("Statut: " + user.getStatus());

            // Check if user is blocked
            if (user.isBlocked()) {
                userCard.getStyleClass().add("blocked-user"); // Apply CSS class
            }

            // Add labels to the card
            userCard.getChildren().addAll(idLabel, nameLabel, lastNameLabel, emailLabel, phoneLabel, statusLabel);

            // Add click event to select the user
            userCard.setOnMouseClicked(event -> {
                selectedUser = user; // Set the selected user
                highlightSelectedCard(userCard); // Highlight the selected card
            });

            // Add the card to the GridPane
            usersGrid.add(userCard, col, row);

            // Update row and column indices
            col++;
            if (col > 2) { // 3 cards per row
                col = 0;
                row++;
            }
        }
    }



    @FXML
    public void handleSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String searchText = filtre.getText().trim();
            if (searchText.isEmpty()) {
                show(); // Reload all users if the search field is empty
            } else {
                ObservableList<User> filteredList = FXCollections.observableArrayList();
                boolean userFound = false;
                for (User user : listeB) {
                    // Search for name or last name
                    if ((user.getFirstName().toLowerCase().contains(searchText.toLowerCase()))
                            || (user.getLastName().toLowerCase().contains(searchText.toLowerCase()))) {
                        filteredList.add(user);
                        userFound = true;
                    }
                }
                if (!userFound) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Utilisateur non trouvé");
                    alert.setHeaderText("Aucun utilisateur ne correspond à votre recherche");
                    alert.setContentText("Veuillez essayer une autre recherche.");
                    alert.showAndWait();
                } else {
                    // Clear the GridPane and display filtered users
                    usersGrid.getChildren().clear();
                    int row = 0;
                    int col = 0;
                    for (User user : filteredList) {
                        VBox userCard = new VBox(10);
                        userCard.getStyleClass().add("user-card");
                        userCard.setPadding(new Insets(10));

                        Label idLabel = new Label("ID: " + user.getId());
                        Label nameLabel = new Label("Nom: " + user.getFirstName());
                        Label lastNameLabel = new Label("Prénom: " + user.getLastName());
                        Label emailLabel = new Label("Email: " + user.getEmail());
                        Label phoneLabel = new Label("Téléphone: " + user.getPhoneNumber());
                        Label statusLabel = new Label("Statut: " + user.getStatus());

                        userCard.getChildren().addAll(idLabel, nameLabel, lastNameLabel, emailLabel, phoneLabel, statusLabel);
                        usersGrid.add(userCard, col, row);

                        col++;
                        if (col > 2) {
                            col = 0;
                            row++;
                        }
                    }
                }
            }
        }
    }

    @FXML
    void supp(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur à supprimer !");
        } else {
            Alert confirmation = new Alert(AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
            confirmation.setHeaderText(null);
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    ServiceUser sc = new ServiceUser();
                    sc.supprimer(selectedUser.getId());
                    showAlert("Succès", "L'utilisateur a été supprimé avec succès !");
                    show(); // Refresh the GridPane
                }
            });
        }
    }

    @FXML
    public void Acceuil(ActionEvent event) {
        try {
            // Close the current window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

            // Load AcceuilAdmin.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ressource-TP/AcceuilAdmin.fxml"));
            Parent root = loader.load();

            // Show the new scene
            Scene scene = new Scene(root);
            Stage stage2 = new Stage();
            stage2.setScene(scene);
            stage2.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void bloq(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur à bloquer !");
        } else {
            Alert confirmation = new Alert(AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir bloquer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
            confirmation.setHeaderText(null);
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    ServiceUser sc = new ServiceUser();
                    sc.bloquer(selectedUser.getId());
                    showAlert("Succès", "L'utilisateur a été bloqué avec succès !");
                    show(); // Refresh the GridPane
                }
            });
        }
    }

    @FXML
    void Modifier(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur à modifier !");
        } else {
            // Show an input dialog to get the new user details
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Modifier un utilisateur");
            dialog.setHeaderText("Modifier les champs");

            // Set the default value of the input fields to the current user details
            TextField firstNameField = new TextField(selectedUser.getFirstName());
            TextField lastNameField = new TextField(selectedUser.getLastName());
            TextField phoneNumberField = new TextField(selectedUser.getPhoneNumber());

            // Add the input fields to the dialog pane
            GridPane grid = new GridPane();
            grid.add(new Label("Nom:"), 1, 1);
            grid.add(firstNameField, 2, 1);
            grid.add(new Label("Prénom:"), 1, 2);
            grid.add(lastNameField, 2, 2);
            grid.add(new Label("Téléphone:"), 1, 3);
            grid.add(phoneNumberField, 2, 3);
            dialog.getDialogPane().setContent(grid);

            // Add buttons to the dialog pane
            ButtonType modifierButtonType = new ButtonType("Modifier", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(modifierButtonType, ButtonType.CANCEL);

            // Convert the result to a user object when the modify button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == modifierButtonType) {
                    return new User(
                            firstNameField.getText(),
                            lastNameField.getText(),
                            phoneNumberField.getText()
                    );
                }
                return null;
            });

            Optional<User> result = dialog.showAndWait();
            if (result.isPresent()) {
                // Update the selected user with the new values
                selectedUser.setFirstName(result.get().getFirstName());
                selectedUser.setLastName(result.get().getLastName());
                selectedUser.setPhoneNumber(result.get().getPhoneNumber());

                // Call the service method to update the user in the database
                ServiceUser bs = new ServiceUser();
                bs.Update(selectedUser, selectedUser.getId());

                showAlert("Succès", "L'utilisateur a été modifié avec succès !");
                show(); // Refresh the GridPane
            }
        }
    }

    @FXML
    void desable(ActionEvent event) {
        if (selectedUser == null) {
            showAlert("Erreur", "Veuillez sélectionner un utilisateur à désactiver !");
        } else {
            Alert confirmation = new Alert(AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir désactiver cet utilisateur ?", ButtonType.YES, ButtonType.NO);
            confirmation.setHeaderText(null);
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    ServiceUser sc = new ServiceUser();
                    sc.modifier(selectedUser, selectedUser.getId());
                    showAlert("Succès", "L'utilisateur a été désactivé avec succès !");
                    show(); // Refresh the GridPane
                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        show(); // Populate the GridPane with users when the controller is initialized
    }

    private void highlightSelectedCard(VBox userCard) {
        // Remove highlight from all cards
        for (Node node : usersGrid.getChildren()) {
            if (node instanceof VBox) {
                node.getStyleClass().remove("selected-card");
            }
        }
        // Add highlight to the selected card
        userCard.getStyleClass().add("selected-card");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static final PseudoClass BLOQUE_PSEUDO_CLASS = PseudoClass.getPseudoClass("bloque");
}