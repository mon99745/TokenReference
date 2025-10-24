package io.jwt4j.lite.core.model.dto.reponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExtractClaimResponse {
	private String jwt;
	private Object claims;
	private String resultCode;
	private String resultMsg;
}