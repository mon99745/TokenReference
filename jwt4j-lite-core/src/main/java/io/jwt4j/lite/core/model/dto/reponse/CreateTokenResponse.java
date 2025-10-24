package io.jwt4j.lite.core.model.dto.reponse;

import io.jwt4j.lite.core.model.dto.Claims;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTokenResponse {
	private Claims.PublicClaim claims;
	private String jwt;
	private String resultCode;
	private String resultMsg;
}