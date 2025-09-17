package com.jsonwebtoken.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 공개키/개인키, 키 페어
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPair {
	/**
	 * 공개키(publicKey)
	 */
	protected String publicKey;

	/**
	 * 개인키(privateKey)
	 */
	protected String privateKey;
}