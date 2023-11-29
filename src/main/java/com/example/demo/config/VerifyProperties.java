package com.example.demo.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 검증 설정
 */
@Data
@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerifyProperties {

	/**
	 * 키 페어 경로
	 */
	@Value("${keyPair.path}")
	protected String path;

	/**
	 * 키 페어 생성 알고리즘
	 */
	@Value("${keyPair.algorithm}")
	protected String algorithm;

	/**
	 * 키 페어 크기
	 */
	@Value("${keyPair.keySize}")
	protected int keySize;

	/**
	 * 설정 정보
	 */
	@Getter
	@Setter
	private static VerifyProperties instance = new VerifyProperties();

}
