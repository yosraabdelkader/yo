package com.example.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

 
public class MyDataBase {

    private static MyDataBase instance;

	
	 private static final String URL = "jdbc:mysql://localhost:3306/yosra_javafx";
	 private static final String USER = "root";
	 private static final String PASSWORD = "";

	 private Connection cnx;

	    private MyDataBase() {
	        try {
	            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
	            System.out.println("Connected!");
	        } catch (SQLException ex) {
	            System.err.println(ex.getMessage());
	        }
	    }

	    public static MyDataBase getInstance() {
	        if (instance == null) {
	            instance = new MyDataBase();
	        }
	        return instance;
	    }

	    public Connection getConnection() {
	        return cnx;
	    }


	    public void closeConnection() {
	        try {
	            cnx.close();
	            System.out.println("Connection closed!");
	        } catch (SQLException ex) {
	            System.err.println(ex.getMessage());
	        }
	    }
}
 	