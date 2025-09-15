package com.security.jsonwebtoken.model.dto.reponse;

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