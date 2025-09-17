package com.jsonwebtoken.core.model.dto.reponse;

import com.jsonwebtoken.core.model.dto.Claims;
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