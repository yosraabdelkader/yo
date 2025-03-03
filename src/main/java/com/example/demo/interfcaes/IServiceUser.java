package com.example.demo.interfcaes;

import java.util.List;

import com.example.demo.models.models_TP.User;

public interface IServiceUser {
	
	void ajouter(User user);

	void supprimer(int id);

	void modifier(User user, int id);

	List<User> afficherTous();

	User rechercherUserParId(int id);

}
