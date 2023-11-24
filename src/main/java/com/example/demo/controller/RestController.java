package com.example.demo.controller;

import com.example.demo.util.ByteUtil;
import com.example.demo.config.RsaKeyGenerator;
import org.bitcoinj.core.Base58;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@org.springframework.web.bind.annotation.RestController
public class RestController {

	RsaKeyGenerator rsaKeyGenerator = new RsaKeyGenerator("C:/git-personal/demo/files/", "RSA", 2048);

	/**
	 * JWS 토큰 발행
	 * @param claim
	 * @return
	 * @throws IOException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	@PostMapping("createJws")
	public String createJws(@RequestBody String claim) throws IOException, NoSuchPaddingException,
			IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException,
			InvalidKeyException {

		String header = "";
		String payload = "";
		String signature = "";

		// Base58 로 암호화 - 위변조 방지
		byte[] byteData = ByteUtil.stringToBytes(claim);
		header = Base58.encode(byteData);
		System.out.println("header = " + header);

		// 서명 - 부인방지
		signature = rsaKeyGenerator.encryptPrvRSA(header);
		System.out.println("signature = " + signature);


		String jws = header + "." + payload + "." + signature;
		System.out.println("jws = " + jws);
		return jws;
	}

	/**
	 * JWS 토큰 검증
	 * @param jws
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws IOException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	@PostMapping("verifyJws")
	public ResponseEntity<Object> verifyJws(@RequestBody String jws) throws NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, InvalidKeyException {

		String header = "";
		String payload = "";
		String signature = "";

		String[] splitArray = jws.split("\\.");

		for (int i = 0; i < splitArray.length; i++) {
			if (i == 0) {
				header = splitArray[i];
			} else if (i == 1) {
				payload = splitArray[i];
			} else if (i == 2) {
				signature = splitArray[i];
			}
		}

		byte[] headerByteData = Base58.decode(header);
		header = ByteUtil.bytesToString(headerByteData);

		signature = rsaKeyGenerator.decryptPubRSA(signature);
		byte[] signatureByteData = Base58.decode(signature);
		signature = ByteUtil.bytesToString(signatureByteData);

		if(header.equals(signature)){
			String successMessage = "검증 성공하였습니다.";
			return new ResponseEntity<>(successMessage, HttpStatus.OK);
		} else {
			String errorMessage = "검증 실패하였습니다.";
			return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
		}
	}

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
//	@GetMapping("json")
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
//	@PostMapping("base58/json")
	public String encryptJsonWithBas58(@RequestBody String strData) throws IOException {
		byte[] byteData = ByteUtil.objectToBytes(strData);
		String encData = Base58.encode(byteData);
		System.out.println("encData = " + encData);

		return encData;
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
//	@PostMapping("encryptPrv")
	public String encryptJsonWithPrivateKey(@RequestBody String strData) throws NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, InvalidKeyException {
		// String 문자열을 개인키로 암호화
		String encData = rsaKeyGenerator.encryptPrvRSA(strData);
		System.out.println("encData = " + encData);

		return encData;
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
//	@PostMapping("signature")
	public String signature(String header) throws NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, InvalidKeySpecException, IOException, BadPaddingException, InvalidKeyException {
		String signature = rsaKeyGenerator.encryptPrvRSA(header);
		System.out.println("signature = " + signature);

		return signature;
	}

	/**
	 * JWT 토큰 발행 테스트
	 * @param header
	 * @param payload
	 * @param signature
	 * @return
	 */
//	@PostMapping("jws")
	public String jws(String header, String payload, String signature){
		String jws = header + "." + payload + "." + signature;
		System.out.println("jws = " + jws);
		return jws;
	}
}
