package io.jwt4j.lite.app.controller;

import io.jwt4j.lite.app.annotation.ControllerTest;
import io.jwt4j.lite.core.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest
@DisplayName("토큰 관리 API 테스트")
class TokenRestControllerTest {
	private static final MockHttpSession SESSION = new MockHttpSession();

	@Autowired
	private MockMvc mvc;

	private static final String claim = "{\n" +
			"  \"uniqueId\": \"1000\",\n" +
			"  \"name\" : \"test\",\n" +
			"  \"num\" : \"10\"\n" +
			"}";

	private static String jwt = "";

	@Test
	@DisplayName("토큰(JWT) 발행 테스트")
	void t01createToken() throws Exception {
		mvc.perform(post(TokenRestController.PATH + "/createToken")
						.contentType(MediaType.APPLICATION_JSON)
						.content(claim)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.jwt").isNotEmpty())
				.andDo(r -> {
					Map<String, Object> response = JsonUtil.readValueMap(r.getResponse().getContentAsString());
					jwt = (String) response.get("jwt");
				});
	}

	@Test
	@DisplayName("토큰(JWT) 검증 테스트")
	void t02verifyToken() throws Exception {
		mvc.perform(post(TokenRestController.PATH + "/verifyToken")
						.contentType(MediaType.APPLICATION_JSON)
						.content(JsonUtil.writeValueAsString(Collections.singletonMap("jwt", jwt)))
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}

	@Test
	@DisplayName("토큰(JWT)에서 클레임 추출 테스트")
	void t03extractClaimInToken() throws Exception {
		mvc.perform(post(TokenRestController.PATH + "/extractClaim")
						.contentType(MediaType.APPLICATION_JSON)
						.content(JsonUtil.writeValueAsString(Collections.singletonMap("jwt", jwt)))
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}
}
