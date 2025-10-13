package com.jsonwebtoken.core.config;

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
	protected final VerifyProperties verifyProperties;

	@Override
	public void afterPropertiesSet() {
		try {
			if (!keyFileCheck()) {
				createKeyFile();
			} else {
				log.info("RSA key already exists and uses it.");
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new IllegalStateException("RSA key initialization failed", e);
		}
	}

	/**
	 * 키 파일이나 폴더가 존재하는지 체크하는 메소드
	 */
	private boolean keyFileCheck() {
		File folder = new File(verifyProperties.getPath());
		if (!folder.exists()) return false;

		String[] files = {
				verifyProperties.getPath() + "public.pem",
				verifyProperties.getPath() + "private.pem"
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
	private void createKeyFile() throws IOException, NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(verifyProperties.getAlg());
		keyPairGenerator.initialize(verifyProperties.getKeySize());
		KeyPair keyPair = keyPairGenerator.genKeyPair();

		File folder = new File(verifyProperties.getPath());
		if (!folder.exists()) folder.mkdirs();

		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) f.delete();
		}

		Map<String, byte[]> keys = new LinkedHashMap<>();
		keys.put("public.pem", keyPair.getPublic().getEncoded());
		keys.put("private.pem", keyPair.getPrivate().getEncoded());

		for (Map.Entry<String, byte[]> entry : keys.entrySet()) {
			File file = new File(folder, entry.getKey());
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(Base58.encode(entry.getValue()).getBytes(StandardCharsets.UTF_8));
			}
			log.info("{} has been created.", entry.getKey());
		}
	}

	/**
	 * 키 페어 생성하는 메소드
	 */
	public Map<String, Object> createKey() {
		Map<String, Object> keyMap = new HashMap<>();
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(verifyProperties.getAlg());
			keyPairGenerator.initialize(verifyProperties.getKeySize());
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			keyMap.put("PublicKey", keyPair.getPublic());
			keyMap.put("PrivateKey", keyPair.getPrivate());
		} catch (NoSuchAlgorithmException e) {
			log.error("KeyPair generation failed", e);
		}
		return keyMap;
	}


	/**
	 * 키를 받아 복호화헤서 리턴하는 메소드
	 */
	public PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = Base58.decode(privateKey);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(verifyProperties.getAlg());
		PrivateKey pk = keyFactory.generatePrivate(spec);
		return pk;
	}

	/**
	 * 키를 받아 복호화헤서 리턴하는 메소드
	 */
	public PublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] bytes = Base58.decode(publicKey);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(verifyProperties.getAlg());
		PublicKey pk = keyFactory.generatePublic(spec);
		return pk;
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성 (for serverKey)
	 */
	public PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (!keyFileCheck()) {
			createKeyFile();
		}
		byte[] bytes = Files.readAllBytes(Paths.get(verifyProperties.getPath() + "private.pem"));
		bytes = Base58.decode(new String(bytes, StandardCharsets.UTF_8));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(verifyProperties.getAlg());
		PrivateKey pk = keyFactory.generatePrivate(spec);
		return pk;
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성 (for serverKey)
	 */
	public PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (!keyFileCheck()) {
			createKeyFile();
		}
		byte[] bytes = Files.readAllBytes(Paths.get(verifyProperties.getPath() + "public.pem"));
		bytes = Base58.decode(new String(bytes, StandardCharsets.UTF_8));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(verifyProperties.getAlg());
		PublicKey pk = keyFactory.generatePublic(spec);
		return pk;
	}

	/**
	 * public 키로 암호화
	 */
	public String encryptPubRSA(String plainText, String publicKey) throws NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		PublicKey pubKey = getPublicKey(publicKey);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] bytePlain = cipher.doFinal(plainText.getBytes());
		String encrypted = Base64.getEncoder().encodeToString(bytePlain);
		return encrypted;
	}

	/**
	 * private 키로 복호화
	 */
	public String decryptPrvRSA(String encrypted, String privateKey) throws NoSuchAlgorithmException,
			InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		PrivateKey prvKey = getPrivateKey(privateKey);
		Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		byte[] byteEncrypted = Base64.getDecoder().decode(encrypted.getBytes());
		cipher2.init(Cipher.DECRYPT_MODE, prvKey);
		byte[] bytePlain = cipher2.doFinal(byteEncrypted);
		String decrypted = new String(bytePlain, StandardCharsets.UTF_8);
		return decrypted;
	}

	/**
	 * private 키로 암호화
	 */
	public String encryptPrvRSA(String plainText, String privateKey) throws NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		PrivateKey prvKey = getPrivateKey(privateKey);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, prvKey);
		byte[] bytePlain = cipher.doFinal(plainText.getBytes());
		String encrypted = Base64.getEncoder().encodeToString(bytePlain);
		return encrypted;
	}

	/**
	 * public 키로 복호화
	 */
	public String decryptPubRSA(String encrypted, String publicKey) throws NoSuchAlgorithmException,
			InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {

		PublicKey pubKey = getPublicKey(publicKey);
		Cipher cipher2 = Cipher.getInstance(("RSA/ECB/PKCS1Padding"));
		String fixedEncrypted = encrypted.trim().replaceAll("\\s+", "");
		byte[] byteEncrypted = Base64.getDecoder().decode(fixedEncrypted);
		cipher2.init(Cipher.DECRYPT_MODE, pubKey);
		byte[] bytePlain = cipher2.doFinal(byteEncrypted);
		String decrypted = new String(bytePlain, StandardCharsets.UTF_8);
		return decrypted;
	}
}