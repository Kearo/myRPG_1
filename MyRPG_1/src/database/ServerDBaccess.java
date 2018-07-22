package database;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ServerDBaccess {
	static final String driver = "com.mysql.jdbc.Driver";
	static final String url = "jdbc:mysql://localhost:3306/myrpg_1";
	static final String	usernameDB = "asdbUser";
	static final String pwDB = "";
	
	public static String getPWhash(String userId) throws ClassNotFoundException, SQLException{
		String s = "";
		String sql = "SELECT pwhash FROM users WHERE userid = ?";
		Connection conn = null;
		
		Class.forName(driver);
		conn = DriverManager.getConnection(url, usernameDB, pwDB);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userId);
		
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			s = rs.getString("pwhash");
		}
		return s; //return null if user dosnt exists
	}
	//not used
	public static boolean setPWhash(String userID, String pw) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException{
		boolean s = false;
		String sql = "SELECT lastlogin FROM users WHERE userid = ?";
		Connection conn = null;
		
		Class.forName(driver);
		conn = DriverManager.getConnection(url, usernameDB, pwDB);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userID);
		
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			s = true;
			sql = "UPDATE users SET pwhash = ? WHERE userid = ? ";
			pstmt = conn.prepareStatement(sql);
			String hash = Cryption.generatePWHash(pw, rs.getString("lastlogin"));
			pstmt.setString(1, hash);
			pstmt.setString(2,  userID);
			pstmt.executeUpdate();
		}		
		return s;
	}

	public static boolean setLastlogin(String userID) throws ClassNotFoundException, SQLException{
		boolean s = false;
		String sql = "SELECT userid FROM users WHERE userid = ?";
		Connection conn = null;
		
		Class.forName(driver);
		conn = DriverManager.getConnection(url, usernameDB, pwDB);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userID);
		
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			s = true;
			sql = "UPDATE users SET lastlogin = ? WHERE userid = ? ";
			Date today = new Date();
			Timestamp time = new Timestamp(today.getTime());
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, time);
			pstmt.setString(2,  userID);
			pstmt.executeUpdate();
		}		
		return s; //return null if user dosnt exist
	}
	
	public static String getLastlogin(String userId) throws ClassNotFoundException, SQLException{
		String s = "";
		String sql = "SELECT lastlogin FROM users WHERE userid = ?";
		Connection conn = null;
		
		Class.forName(driver);
		conn = DriverManager.getConnection(url, usernameDB, pwDB);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userId);
		
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			s = rs.getString("lastlogin");
		}
		return s; //return null if user dosnt exist
	}
	
	public static boolean registerUser(String userID, String pw) throws ClassNotFoundException, SQLException, NoSuchAlgorithmException, InstantiationException, IllegalAccessException{
		boolean accepted = false;
		String sql = "SELECT userid FROM users WHERE userid = ?";
		Connection conn = null;
		
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection(url, usernameDB, pwDB);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userID);
		
		ResultSet rs = pstmt.executeQuery();
		if(rs.next()){
			return accepted;
		}else{
			sql = "INSERT INTO users " + "(id, userid) " + " VALUES (null, ?)";	
			pstmt = conn.prepareStatement(sql);		
			pstmt.setString(1, userID);
			pstmt.executeUpdate();	
			accepted = true;
			setPWhash(userID, pw);
		}		
		return accepted;
	}
}
