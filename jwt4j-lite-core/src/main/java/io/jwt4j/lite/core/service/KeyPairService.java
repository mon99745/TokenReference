package io.jwt4j.lite.core.service;

import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.exception.TokenError;
import io.jwt4j.lite.core.exception.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyPairService {
	protected final RsaKeyGenerator rsaKeyGenerator;

	/**
	 * 키페어 생성
	 *
	 * @param keyPair 입력 맵 (PublicKey, PrivateKey 포함)
	 * @return Base58 인코딩된 키 문자열 맵
	 */
	public Map<String, Object> createKeyPair(Map<String, Object> keyPair) {
		if (keyPair == null) {
			throw new TokenException(TokenError.MISSING_KEY);
		}

		Object pubObj = keyPair.get("PublicKey");
		Object privObj = keyPair.get("PrivateKey");

		if (!(pubObj instanceof PublicKey)) {
			throw new TokenException(TokenError.INVALID_KEY_INPUT);
		}
		if (!(privObj instanceof PrivateKey)) {
			throw new TokenException(TokenError.INVALID_KEY_INPUT);
		}

		PublicKey publicKey = (PublicKey) pubObj;
		PrivateKey privateKey = (PrivateKey) privObj;

		try {
			byte[] pubEncoded = publicKey.getEncoded();
			byte[] privEncoded = privateKey.getEncoded();

			if (pubEncoded == null || privEncoded == null) {
				throw new TokenException(TokenError.KEY_ENCODING_FAILED);
			}

			String strPublicKey = Base58.encode(pubEncoded);
			String strPrivateKey = Base58.encode(privEncoded);

			Map<String, Object> strKeymap = new HashMap<>();
			strKeymap.put("publicKey", strPublicKey);
			strKeymap.put("privateKey", strPrivateKey);
			return strKeymap;
		} catch (Exception e) {
			throw new TokenException(TokenError.BASE58_ENCODING_FAILED, e);
		}
	}

	protected String getPrivateKey() {
		try {
			return Base58.encode(rsaKeyGenerator.getPrivateKey().getEncoded());
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new TokenException(TokenError.FAILED_ENCRYPT, e);
		}
	}

	protected String getPublicKey() {
		try {
			return Base58.encode(rsaKeyGenerator.getPublicKey().getEncoded());
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new TokenException(TokenError.FAILED_ENCRYPT, e);
		}
	}
}