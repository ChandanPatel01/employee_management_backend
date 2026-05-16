package com.example.employeemanagement.auth;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class PasswordService {

	private static final int HASH_BITS = 256;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

	private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

	public String hash(String password) {
		return bcrypt.encode(password);
	}

	public boolean verify(String password, String storedHash) {
		if (storedHash == null || storedHash.isBlank()) {
			return false;
		}

		if (isBcryptHash(storedHash)) {
			return bcrypt.matches(password, storedHash);
		}

		return verifyLegacyPbkdf2(password, storedHash);
	}

	public boolean needsRehash(String storedHash) {
		return !isBcryptHash(storedHash);
	}

	private boolean isBcryptHash(String storedHash) {
		return storedHash != null
				&& (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"));
	}

	private boolean verifyLegacyPbkdf2(String password, String storedHash) {
		String[] parts = storedHash.split(":");
		if (parts.length != 3) {
			return false;
		}

		try {
			int iterations = Integer.parseInt(parts[0]);
			byte[] salt = Base64.getDecoder().decode(parts[1]);
			byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
			byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations);

			return MessageDigest.isEqual(expectedHash, actualHash);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
			return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
		} catch (Exception exception) {
			throw new IllegalStateException("Could not hash password", exception);
		}
	}
}
