package com.security.jsonwebtoken.model.dto.reponse;

import com.security.jsonwebtoken.model.dto.Claims;
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