package com.example.employeemanagement.auth;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {

	private static final int ITERATIONS = 120_000;
	private static final int SALT_BYTES = 16;
	private static final int HASH_BITS = 256;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

	private final SecureRandom secureRandom = new SecureRandom();

	public String hash(String password) {
		byte[] salt = new byte[SALT_BYTES];
		secureRandom.nextBytes(salt);
		byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);

		return ITERATIONS + ":"
				+ Base64.getEncoder().encodeToString(salt) + ":"
				+ Base64.getEncoder().encodeToString(hash);
	}

	public boolean verify(String password, String storedHash) {
		String[] parts = storedHash.split(":");
		if (parts.length != 3) {
			return false;
		}

		int iterations = Integer.parseInt(parts[0]);
		byte[] salt = Base64.getDecoder().decode(parts[1]);
		byte[] expectedHash = Base64.getDecoder().decode(parts[2]);
		byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations);

		return MessageDigest.isEqual(expectedHash, actualHash);
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
