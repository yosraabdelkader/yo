package com.example.demo.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.example.demo.enums.UserRole;
import jakarta.mail.*;
import com.example.demo.models.User;
import com.example.demo.utils.MyDataBase;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mindrot.jbcrypt.BCrypt;

public class ServiceUser {

    private Connection connection = MyDataBase.getInstance().getConnection();
    ;

    public ServiceUser() {
        // TODO Auto-generated constructor stub
    }


    public String getUserImage(int userId) {
        String query = "SELECT image FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("image"); // Retourne le chemin de l'image
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'image : " + e.getMessage());
        }
        return null; // Retourne null si aucune image trouvée
    }

    public String getUserStatus(String username) {
        String query = "SELECT status FROM user WHERE first_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username); // Utiliser le nom d'utilisateur au lieu de l'ID
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("status");
            }
        } catch (SQLException e) {
            System.err.println("Erreur d'exécution de la requête SQL : " + e.getMessage());
        }
        return null; // Retourner null si aucun résultat trouvé
    }

    public void ajouter(User user) {
            String query = "INSERT INTO user (email, roles, password, is_verified, first_name, last_name, phone_number, image, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getRole().name()); // Store enum as string
            statement.setString(3, user.getPassword());
            statement.setInt(4, 0);
            statement.setString(5, user.getFirstName());
            statement.setString(6, user.getLastName());
            statement.setString(7, user.getPhoneNumber());
            statement.setString(8, user.getImage());
            statement.setString(9, "enabled");

            statement.executeUpdate();
            String subject = "Nouvelle compte";
            String body = "Bonjour,\n\nUne nouvelle compte a été créée pour votre email.\n\nCordialement,\nL'équipe de support";
            sendEmail(user.getEmail(), subject, body);
            System.out.println("Utilisateur ajouté avec succès.");
        } catch (SQLException ex) {
            System.out.println("Erreur SQL : " + ex.getMessage());
        }
    }


    private void sendEmail(String to, String subject, String body) {
        final String username = "yosraabdelkader44@gmail.com";
        final String password = "g s t b b u o f b h z a w o p h";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + to);
        } catch (MessagingException e) {
            System.err.println("Erreur d'envoi d'email : " + e.getMessage());
        }
    }

    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isNameExists(String name) {
        String query = "SELECT COUNT(*) FROM user WHERE first_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendOTPEmail(String to, String otp) {
        final String username = "yosraabdelkader44@gmail.com";
        final String password = "g s t b b u o f b h z a w o p h";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp + "\n\nUse this to reset your password.");

            Transport.send(message);
            System.out.println("OTP sent successfully to " + to);
        } catch (MessagingException e) {
            System.err.println("Error sending OTP email: " + e.getMessage());
        }
    }


    public void updatePassword(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // Hash new password
        String query = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, hashedPassword);
            statement.setString(2, email);
            statement.executeUpdate();
            System.out.println("Password updated successfully for: " + email);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void supprimer(int id) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("ok");
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        }
    }

    public void modifier(User user, int id) {
        String query = "UPDATE user SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, "desable");
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        }
    }

    public void Update(User user, int id) {
        String query = "UPDATE user SET first_name = ? ,last_name=?,phone_number=? WHERE id = ?;";
        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getPhoneNumber());
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        }
    }

    public ObservableList<User> afficherTous() {
        ObservableList<User> users = FXCollections.observableArrayList();

        try {
            String query = "SELECT * FROM user";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String email = resultSet.getString("email");
                String roleString = resultSet.getString("roles"); // Get role as String
                UserRole role = UserRole.fromString(roleString);  // Convert to Enum
                String password = resultSet.getString("password");
                String firstname = resultSet.getString("first_name");
                String lastname = resultSet.getString("last_name");
                String phonenumber = resultSet.getString("phone_number");
                String image = resultSet.getString("image");
                String status = resultSet.getString("status");
                boolean bloque = resultSet.getInt("bloque") == 1;

                User user = new User(id, email, role, password, firstname, lastname, phonenumber, image, status);
                user.setBlocked(bloque);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error executing SQL query: " + e.getMessage());
        }
        return users;
    }


    public boolean login(String username, String password) {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isAuthenticated = false;

        try {
            // Etape 1 : se connecter � la base de donn�es

            // Etape 2 : Pr�parer la requ�te SQL
            String sql = "SELECT * FROM user WHERE first_name = ? AND password = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Etape 3 : Ex�cuter la requ�te SQL
            rs = stmt.executeQuery();

            // Etape 4 : V�rifier si l'utilisateur existe et que le mot de passe correspond
            if (rs.next()) {
                isAuthenticated = true;
            }
        } catch (SQLException e) {
            // G�rer les erreurs de connexion � la base de donn�es
            e.printStackTrace();
        }


        return isAuthenticated;
    }

    public User rechercherUserParId(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM user WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("roles"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setImage(rs.getString("image"));
                user.setStatus(rs.getString("status"));
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String getHashedPasswordForUser(String username) {
        String hashedPassword = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT password FROM user WHERE first_name = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hashedPassword = rs.getString("password");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }

    public void bloquer(int userId) {
        try {
            String query = "UPDATE user SET bloque = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            System.out.println("bloquerr");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public UserRole getRole(String username) {
        String query = "SELECT roles FROM user WHERE first_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return UserRole.fromString(rs.getString("roles"));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }



}
