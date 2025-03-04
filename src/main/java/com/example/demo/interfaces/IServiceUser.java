package com.example.demo.interfaces;

import java.util.List;

import com.example.demo.models.User;

public interface IServiceUser {
	
	void ajouter(User user);

	void supprimer(int id);

	void modifier(User user, int id);

	List<User> afficherTous();

	User rechercherUserParId(int id);

}
