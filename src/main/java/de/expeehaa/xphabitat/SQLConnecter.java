package de.expeehaa.xphabitat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLConnecter {

	public static String host;
	public static String port;
	public static String database;
	public static String username;
	public static String password;
	
	public static Connection con;
	
	public static void connect(){
		if(!isConnected()){
			try {
				con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
			} catch (SQLException e) {
				XPHabitat.instance.getLogger().info("SQLException: " + e.getMessage());
				return;
			}
			XPHabitat.instance.getLogger().info("Connected to database");
		}
	}
	
	public static void disconnect(){
		if(isConnected()){
			try {
				con.close();
			} catch (SQLException e) {
				XPHabitat.instance.getLogger().info("SQLException: " + e.getMessage());
				return;
			}
			XPHabitat.instance.getLogger().info("Disconnected from database");
		}
	}
	
	public static void update(String query){
		try {
			if(isConnected()){
				con.prepareStatement(query).executeUpdate();
			}
		} catch (SQLException e) {
			XPHabitat.instance.getLogger().info("SQLException: " + e.getMessage());
		}
	}
	
	public static ResultSet getResult(String query){
		try {
			if(isConnected()){
				return con.prepareStatement(query).executeQuery();
			}
		} catch (SQLException e) {
			XPHabitat.instance.getLogger().info("SQLException: " + e.getMessage());
		}
		return null;
	}
	
	public static boolean isConnected(){
		if(con == null) return false;
		return true;
	}
}
