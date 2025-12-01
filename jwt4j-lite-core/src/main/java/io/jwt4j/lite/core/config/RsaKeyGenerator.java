package io.jwt4j.lite.core.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.jwt4j.lite.core.exception.TokenError;
import io.jwt4j.lite.core.exception.TokenException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Primary
@Component
@RequiredArgsConstructor
@Configuration
public class RsaKeyGenerator implements InitializingBean {
	protected final TokenProperties tokenProperties;

	private String PUBLIC_KEY_NAME = "public.pem";
	private String PRIVATE_KEY_NAME = "private.pem";

	@Override
	public void afterPropertiesSet() {
		if (!keyFileCheck()) {
			log.info("Generate RSA keys");
			createKeyFile();
		} else {
			log.info("RSA keys already exists and uses it.");
		}
	}

	/**
	 * 키 파일이나 폴더가 존재하는지 체크하는 메소드
	 */
	private boolean keyFileCheck() {
		File folder = new File(tokenProperties.getPath());
		if (!folder.exists()) return false;

		String[] files = {
				tokenProperties.getPath() + PUBLIC_KEY_NAME,
				tokenProperties.getPath() + PRIVATE_KEY_NAME
		};

		for (String f : files) {
			File file = new File(f);
			if (!file.exists()) return false;
		}
		return true;
	}

	/**
	 * 키 파일을 생성하는 메소드, 무조건 파일을 모두 새로 생성
	 */
	private void createKeyFile() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(tokenProperties.getAlg());
			keyPairGenerator.initialize(tokenProperties.getKeySize());
			KeyPair keyPair = keyPairGenerator.genKeyPair();

			File folder = new File(tokenProperties.getPath());
			if (!folder.exists()) folder.mkdirs();

			File[] files = folder.listFiles();
			if (files != null) {
				for (File f : files) f.delete();
			}

			Map<String, byte[]> keys = new LinkedHashMap<>();
			keys.put(PUBLIC_KEY_NAME, keyPair.getPublic().getEncoded());
			keys.put(PRIVATE_KEY_NAME, keyPair.getPrivate().getEncoded());

			for (Map.Entry<String, byte[]> entry : keys.entrySet()) {
				File file = new File(folder, entry.getKey());
				try (FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(Base58.encode(entry.getValue()).getBytes(StandardCharsets.UTF_8));
				}
				log.info("{} has been created.", entry.getKey());
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new TokenException(TokenError.KEY_GENERATION_FAILED, e);
		}
	}

	/**
	 * 키 페어 생성하는 메소드
	 */
	public Map<String, Object> createKey() {
		Map<String, Object> keyMap = new HashMap<>();
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(tokenProperties.getAlg());
			keyPairGenerator.initialize(tokenProperties.getKeySize());
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			keyMap.put("PublicKey", keyPair.getPublic());
			keyMap.put("PrivateKey", keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);
		}
		return keyMap;
	}


	/**
	 * 키를 받아 복호화헤서 리턴하는 메소드
	 */
	public PrivateKey getPrivateKey(String privateKey) {
		try {
			byte[] decoded = Base58.decode(privateKey);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			return KeyFactory.getInstance(tokenProperties.getAlg())
					.generatePrivate(spec);

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_KEY_INPUT, e);

		} catch (NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);

		} catch (InvalidKeySpecException e) {
			throw new TokenException(TokenError.INVALID_PRIVATE_KEY_SPEC, e);
		}
	}

	/**
	 * 키를 받아 복호화헤서 리턴하는 메소드
	 */
	public PublicKey getPublicKey(String publicKey) {
		try {
			byte[] decoded = Base58.decode(publicKey);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			return KeyFactory.getInstance(tokenProperties.getAlg())
					.generatePublic(spec);

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_KEY_INPUT, e);

		} catch (NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);

		} catch (InvalidKeySpecException e) {
			throw new TokenException(TokenError.INVALID_PUBLIC_KEY_SPEC, e);
		}
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성 (for serverKey)
	 */
	public PrivateKey getPrivateKey() {
		try {
			if (!keyFileCheck()) createKeyFile();

			byte[] encoded = Files.readAllBytes(Paths.get(tokenProperties.getPath() + PRIVATE_KEY_NAME));
			byte[] decoded = Base58.decode(new String(encoded, StandardCharsets.UTF_8));

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			return KeyFactory.getInstance(tokenProperties.getAlg())
					.generatePrivate(spec);

		} catch (IOException e) {
			throw new TokenException(TokenError.PRIVATE_KEY_LOAD_FAILED, e);

		} catch (NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);

		} catch (InvalidKeySpecException e) {
			throw new TokenException(TokenError.INVALID_PRIVATE_KEY_SPEC, e);
		}
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성 (for serverKey)
	 */
	public PublicKey getPublicKey() {
		try {
			if (!keyFileCheck()) createKeyFile();

			byte[] encoded = Files.readAllBytes(Paths.get(tokenProperties.getPath() + PUBLIC_KEY_NAME));
			byte[] decoded = Base58.decode(new String(encoded, StandardCharsets.UTF_8));

			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			return KeyFactory.getInstance(tokenProperties.getAlg())
					.generatePublic(spec);

		} catch (IOException e) {
			throw new TokenException(TokenError.PUBLIC_KEY_LOAD_FAILED, e);

		} catch (NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);

		} catch (InvalidKeySpecException e) {
			throw new TokenException(TokenError.INVALID_PUBLIC_KEY_SPEC, e);
		}
	}

	/**
	 * private 키로 암호화
	 */
	public String encryptPrvRSA(String plainText, String privateKey) {
		try {
			PrivateKey prvKey = getPrivateKey(privateKey);

			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, prvKey);
			byte[] bytePlain = cipher.doFinal(plainText.getBytes());

			return Base64.getEncoder().encodeToString(bytePlain);

		} catch (InvalidKeyException e) {
			throw new TokenException(TokenError.INVALID_PRIVATE_KEY, e);

		} catch (BadPaddingException | IllegalBlockSizeException e) {
			throw new TokenException(TokenError.ENCRYPTION_FAILED, e);

		} catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);
		}
	}

	/**
	 * public 키로 복호화
	 */
	public String decryptPubRSA(String encrypted, String publicKey) {
		String normalized = encrypted.trim().replaceAll("\\s+", "");
		byte[] decoded;

		try {
			decoded = Base64.getDecoder().decode(normalized);

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_ENCRYPTED_TEXT, e);
		}

		try {
			PublicKey pub = getPublicKey(publicKey);

			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, pub);

			byte[] decrypted = cipher.doFinal(decoded);
			return new String(decrypted, StandardCharsets.UTF_8);

		} catch (InvalidKeyException e) {
			throw new TokenException(TokenError.INVALID_PUBLIC_KEY, e);

		} catch (BadPaddingException | IllegalBlockSizeException e) {
			throw new TokenException(TokenError.DECRYPTION_FAILED, e);

		} catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
			throw new TokenException(TokenError.RSA_ALGORITHM_NOT_FOUND, e);
		}
	}
}