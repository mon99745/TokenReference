package com.example.demo.controller;

import com.example.demo.util.ByteUtil;
import com.example.demo.config.RsaKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Slf4j
@org.springframework.web.bind.annotation.RestController
public class RestController {

	RsaKeyGenerator rsaKeyGenerator = new RsaKeyGenerator("C:/git-personal/demo/files/", "RSA", 2048);

	/**
	 * PK base58 암/복호화
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	@GetMapping("base58/key")
	public String base58() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("value = " + ByteUtil.objectToBytes(rsaKeyGenerator.getPublicKey()));

		byte[] publicKey = ByteUtil.objectToBytes(rsaKeyGenerator.getPublicKey());
		System.out.println("publicKey = " + publicKey);

		// Base58 인코딩
		String encodedData = Base58.encode(ByteUtil.objectToBytes(publicKey));

		System.out.println("Original Data: " + publicKey);
		System.out.println("Base58 Encoded: " + encodedData);

		// Base58 디코딩
		byte[] decodedBytes = Base58.decode(encodedData);
		String decodedData = new String(decodedBytes);

		System.out.println("Base58 Decoded: " + decodedData);


		return "Success";
	}

	/**
	 * JSON 데이터 파싱
	 * @return
	 */
	@GetMapping("json")
	public String readJson(){

		// JSON 파일 경로 설정
		String filePath = "example/claim.json";

		// JSON 파일 읽기
		try {
			ClassPathResource resource = new ClassPathResource(filePath);
			byte[] jsonData = FileCopyUtils.copyToByteArray(resource.getInputStream());
			String jsonString = new String(jsonData, StandardCharsets.UTF_8);

			// JSON 파싱
			System.out.println(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "Success";
	}

	/**
	 * JSON 데이터를 Base58 암호화
	 * JWT - Header
	 * @param strData
	 * @return
	 * @throws IOException
	 */
	@PostMapping("base58/json")
	public String encryptJsonWithBas58(@RequestBody String strData) throws IOException {
		byte[] byteData = ByteUtil.objectToBytes(strData);
		String encData = Base58.encode(byteData);
		System.out.println("encData = " + encData);

		return "Success";
	}

	/**
	 * JSON 데이터를 개인키로 암호화
	 * for 서명
	 * @param strData
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	@PostMapping("encryptPrv")
	public String encryptJsonWithPrivateKey(@RequestBody String strData) throws NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, InvalidKeyException {
		// String 문자열을 개인키로 암호화
		String encData = rsaKeyGenerator.encryptPrvRSA(strData);
		System.out.println("encData = " + encData);

		return "Success";

	}

	/**
	 * Header 데이터를 개인키로 암호화
	 * @param header
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	@PostMapping("signature")
	public String signature(String header) throws NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, InvalidKeyException {
		String signature = rsaKeyGenerator.encryptPrvRSA(header);
		System.out.println("signature = " + signature);

		return "Success";
	}

	/**
	 * JWT 토큰 발행
	 * @param header
	 * @param payload
	 * @param signature
	 * @return
	 */
	@PostMapping("jwt")
	public String jwt(String header, String payload, String signature){
		String jws = header + payload + signature;
		System.out.println("jws = " + jws);
		return "Success";
	}

}
