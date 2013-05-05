package net.svcret.ejb.util;

import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * From implementation found here:
 * http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
 */
public class Password {
	// The higher the number of iterations the more
	// expensive computing the hash is for us
	// and also for a brute force attack.
	private static final int iterations = 10 * 1024;
	private static final int saltLen = 32;
	private static final int desiredKeyLen = 256;

	/**
	 * Computes a salted PBKDF2 hash of given plaintext password suitable for
	 * storing in a database. Empty passwords are not supported.
	 */
	public static String getStrongHash(String password) throws Exception {
		byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
		// store the salt with the password
		return Base64.encodeBase64String(salt) + "$" + strongHash(password, salt);
	}

	public static String getWeakHash(String password) throws Exception {
		byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
		// store the salt with the password
		return Base64.encodeBase64String(salt) + "$" + weakHash(password, salt);
	}

	/**
	 * Checks whether given plaintext password corresponds to a stored salted
	 * hash of the password.
	 */
	public static boolean checkStrongHash(String password, String stored) throws Exception {
		String[] saltAndPass = stored.split("\\$");
		if (saltAndPass.length != 2)
			return false;
		String hashOfInput = strongHash(password, Base64.decodeBase64(saltAndPass[0]));
		return hashOfInput.equals(saltAndPass[1]);
	}

	public static boolean checkWeakHash(String password, String stored) throws Exception {
		String[] saltAndPass = stored.split("\\$");
		if (saltAndPass.length != 2)
			return false;
		String hashOfInput = weakHash(password, Base64.decodeBase64(saltAndPass[0]));
		return hashOfInput.equals(saltAndPass[1]);
	}

	// using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
	// cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
	private static String strongHash(String password, byte[] salt) throws Exception {
		if (password == null || password.length() == 0)
			throw new IllegalArgumentException("Empty passwords are not supported.");
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, iterations, desiredKeyLen));
		return Base64.encodeBase64String(key.getEncoded());
	}
	
	
	private static String weakHash(String password, byte[] salt) throws Exception {
		if (password == null || password.length() == 0)
			throw new IllegalArgumentException("Empty passwords are not supported.");
		return Base64.encodeBase64String(DigestUtils.sha512(password + salt));
//		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//		SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, weakIterations, desiredKeyLen));
//		return Base64.encodeBase64String(key.getEncoded());
	}

	
	public static void main(String[] args) throws Exception {
		
		String password = "239ZhsfaiofdaioH@@#@4";
		
		long start = System.currentTimeMillis();
		int passes = 300;
		for (int i = 0; i < passes; i++) {
			if (i % 100 == 0) {
				System.out.println("Pass " + i);
			}
			getStrongHash(password);
		}
		double strongDelay = (System.currentTimeMillis() - start) / passes;

		start = System.currentTimeMillis();
		for (int i = 0; i < passes; i++) {
			if (i % 100 == 0) {
				System.out.println("Pass " + i);
			}
			getWeakHash(password);
		}
		double weakDelay = (System.currentTimeMillis() - start) / passes;

		System.out.println("Strong Hash: " + strongDelay + "ms / pass");
		System.out.println("Weak Hash: " + weakDelay + "ms / pass");
		
	}
}