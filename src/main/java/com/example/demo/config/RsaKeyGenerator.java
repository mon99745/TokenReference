package com.example.demo.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
@Getter
@Configuration
public class RsaKeyGenerator implements InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(RsaKeyGenerator.class);

	private final String path;
	private final String algorithm;
	private final int keySize;

	public RsaKeyGenerator(@Value("${keyPair.path}") String path,
						   @Value("${keyPair.algorithm}") String algorithm, @Value("${keyPair.keySize}") int keySize) {
		this.path = path;
		this.algorithm = algorithm;
		this.keySize = keySize;
	}

	@Override
	public void afterPropertiesSet() throws NoSuchAlgorithmException, IOException {
		if (!keyFileCheck()) {
			createKeyFile();
		}else{
			LOGGER.info("RSA 키가 존재하여 기존 키를 활용합니다.");
		}
	}

	/**
	 * 키 파일이나 폴더가 존재하는지 체크하는 메소드
	 */
	private boolean keyFileCheck() {
		File folder = new File(this.path);
		if (!folder.exists()) {
			return false;
		} else {
			String[] files = new String[] { this.path + "public.pem", this.path + "private.pem" };
			for (String f : files) {
				File file = new File(f);
				if (!file.exists())
					return false;
			}
		}
		return true;
	}

	/**
	 * 키 파일을 생성하는 메소드, 무조건 파일을 모두 새로 생성
	 */
	private void createKeyFile() throws IOException, NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(this.algorithm);
		keyPairGenerator.initialize(this.keySize);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		Key[] keys = new Key[] { keyPair.getPublic(), keyPair.getPrivate() };
		FileOutputStream fos = null;
		try {
			File folder = new File(this.path);
			if (!folder.exists()){
				folder.mkdir();
			}
			File[] files = folder.listFiles();
			for (File f : files) {
				f.delete();
			}
			for (Key key : keys) {
				String path = null;
				if (key.equals(keyPair.getPublic())) {
					path = this.path + "public.pem";
				} else {
					path = this.path + "private.pem";
				}
				File file = new File(path);
				fos = new FileOutputStream(file);
				fos.write(key.getEncoded());
				LOGGER.info("RSA 키를 새로 생성하였습니다.");
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (fos != null) {
				fos.close();
				fos.flush();
			}
		}
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성
	 */
	public PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (!keyFileCheck()) {
			createKeyFile();
		}
		byte[] bytes = Files.readAllBytes(Paths.get(this.path + "private.pem"));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
		PrivateKey pk = keyFactory.generatePrivate(spec);
		return pk;
	}

	/**
	 * 키 파일을 읽어 리턴하는 메소드, 없을 경우 새로 생성
	 */
	public PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		if (!keyFileCheck()) {
			createKeyFile();
		}
		byte[] bytes = Files.readAllBytes(Paths.get(this.path + "public.pem"));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(this.algorithm);
		PublicKey pk = keyFactory.generatePublic(spec);
		return pk;
	}

	/**
	 * public 키로 암호화
	 */
	public String encryptPubRSA(String plainText) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		PublicKey publicKey = getPublicKey();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] bytePlain = cipher.doFinal(plainText.getBytes());
		String encrypted = Base64.getEncoder().encodeToString(bytePlain);
		return encrypted;
	}

	/**
	 * private 키로 복호화
	 */
	public String decryptPrvRSA(String encrypted) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		PrivateKey privateKey = getPrivateKey();
		Cipher cipher2 = Cipher.getInstance("RSA");
		byte[] byteEncrypted = Base64.getDecoder().decode(encrypted.getBytes());
		cipher2.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] bytePlain = cipher2.doFinal(byteEncrypted);
		String decrypted = new String(bytePlain, "utf-8");
		return decrypted;
	}

	/**
	 * private 키로 암호화
	 */
	public String encryptPrvRSA(String plainText) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		PrivateKey privateKey = getPrivateKey();
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		byte[] bytePlain = cipher.doFinal(plainText.getBytes());
		String encrypted = Base64.getEncoder().encodeToString(bytePlain);
		return encrypted;
	}

	/**
	 * private 키로 복호화
	 */
	public String decryptPubRSA(String encrypted) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		PublicKey publicKey = getPublicKey();
		Cipher cipher2 = Cipher.getInstance("RSA");
		byte[] byteEncrypted = Base64.getDecoder().decode(encrypted.getBytes());
		cipher2.init(Cipher.DECRYPT_MODE, publicKey);
		byte[] bytePlain = cipher2.doFinal(byteEncrypted);
		String decrypted = new String(bytePlain, "utf-8");
		return decrypted;
	}
}
