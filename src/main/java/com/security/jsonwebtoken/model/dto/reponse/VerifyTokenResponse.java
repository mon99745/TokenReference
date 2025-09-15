package com.security.jsonwebtoken.model.dto.reponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyTokenResponse {
	private String jwt;
	private String resultCode;
	private String resultMsg;
}