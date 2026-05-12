package com.example.employeemanagement.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final long expiresInSeconds;

	public JwtService(
			ObjectMapper objectMapper,
			@Value("${app.jwt.secret:change-this-development-secret-to-a-long-random-value}") String secret,
			@Value("${app.jwt.expiration-minutes:120}") long expirationMinutes) {
		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.expiresInSeconds = expirationMinutes * 60;
	}

	public String createToken(AppUser user) {
		long now = Instant.now().getEpochSecond();
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> payload = Map.of(
				"sub", user.getEmail(),
				"uid", user.getId(),
				"name", user.getName(),
				"iat", now,
				"exp", now + expiresInSeconds);

		String encodedHeader = encodeJson(header);
		String encodedPayload = encodeJson(payload);
		String signingInput = encodedHeader + "." + encodedPayload;

		return signingInput + "." + sign(signingInput);
	}

	public JwtClaims validate(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new JwtValidationException("Invalid token");
		}

		String signingInput = parts[0] + "." + parts[1];
		String expectedSignature = sign(signingInput);
		if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
			throw new JwtValidationException("Invalid token signature");
		}

		Map<String, Object> payload = decodeJson(parts[1]);
		Object expiresAt = payload.get("exp");
		if (!(expiresAt instanceof Number number) || number.longValue() < Instant.now().getEpochSecond()) {
			throw new JwtValidationException("Token expired");
		}

		Object subject = payload.get("sub");
		Object userId = payload.get("uid");
		if (!(subject instanceof String email) || email.isBlank() || !(userId instanceof Number id)) {
			throw new JwtValidationException("Invalid token claims");
		}

		return new JwtClaims(id.longValue(), email);
	}

	public long getExpiresInSeconds() {
		return expiresInSeconds;
	}

	private String encodeJson(Map<String, Object> json) {
		try {
			return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(json));
		} catch (Exception exception) {
			throw new IllegalStateException("Could not encode JWT", exception);
		}
	}

	private Map<String, Object> decodeJson(String encodedJson) {
		try {
			byte[] decoded = URL_DECODER.decode(encodedJson);
			return objectMapper.readValue(decoded, new TypeReference<>() {
			});
		} catch (Exception exception) {
			throw new JwtValidationException("Invalid token payload");
		}
	}

	private String sign(String signingInput) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
			return URL_ENCODER.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Could not sign JWT", exception);
		}
	}

	public record JwtClaims(long userId, String email) {
	}

	public static class JwtValidationException extends RuntimeException {

		public JwtValidationException(String message) {
			super(message);
		}
	}
}
