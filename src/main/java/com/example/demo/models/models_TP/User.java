package com.example.demo.models.models_TP;

import com.example.demo.enums.enums_TP.UserRole;

public class User {

	public User(String firstName, String lastName, String phoneNumber) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", role=" + role + ", password=" + password + ", firstName="
				+ firstName + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber + ", image=" + image
				+ ", status=" + status + "]";
	}

	private int id;
    private String email;
    private UserRole role;  // Change from String to UserRole
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String image;
    private String status;
    private boolean blocked ;
    ;

    public User(int id, String email, UserRole role, String password, String firstName, String lastName,
			String phoneNumber, String image, String status) {
		this.id = id;
		this.email = email;
		this.role = role;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.image = image;
		this.status = status;
	}

    public User() {
		// TODO Auto-generated constructor stub
	}


	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getters and Setters
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setRole(String role) {
        this.role = UserRole.fromString(role);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlocked() {
        return this.blocked;
    }


}

