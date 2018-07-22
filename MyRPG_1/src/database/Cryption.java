package database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cryption {
	//private static final String transformation = "AES";
	//private static final String asym = "RSA";
	private static final String hashAlgo = "SHA-512";
	
	
	public static String generatePWHash(String pw, String lastlogin) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance(hashAlgo);
		byte[] txt = md.digest((pw + lastlogin).getBytes()); 
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < txt.length; i++) {
			sb.append(Integer.toString((txt[i] & 0xff) + 0x100, 16).substring(1));
		}			
		return sb.toString();
	}
}
