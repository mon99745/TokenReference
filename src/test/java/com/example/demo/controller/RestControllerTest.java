package com.example.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@WebMvcTest(RestController.class)
class RestControllerTest {
	private static final MockHttpSession SESSION = new MockHttpSession();

	/**
	 * claim
	 */
	private static String claim = "{\n" +
			"  \"uniqueId\": \"1000\",\n" +
			"  \"name\" : \"test\",\n" +
			"  \"num\" : \"10\"\n" +
			"}";

	/**
	 * Header
	 */
	private static String encData = "2AMZFXZo3dkKWhwEEEDzUwawXt1qYjrw2HtnX" +
			"ucWioP8DxKVGhYVQyLRHpv1Am56LkQPzVcKoJhTc85qRbuUGjU6v5i";

	/**
	 * Signature
	 */
	private static String signData = "acPB/W98eBOJe8Gmc9MgIBzE1qgkTFZHR2Z" +
			"SFZD8wpPZFfbVxqQUCv05U4jbA0z5pt+Utqw10EW8oTb/XVA5jX/fYgLlvCk" +
			"dmbe8ixxEmubWntjKZiQ8qS+SM8ueyiJIOFFhC5J3TR7wVCNDqrueeybNXPu" +
			"MXBPJ4oP4lGVyVEvbtrN2/t1CzvtYZFcz4q6SUP4RFJiJp+ltKt8GJhxDZ77" +
			"kfGG99aAPbdOEaiegsRLDJ/Aab7o5jDIwifR7oZ4kZPeBH+8ONcI6soxnq5D" +
			"ha9ZDKkAeCClEBWkw/l6KpjUZisnvTarKbNg3DXmHx8A+Iv4H0ENSFWQ/z3m" +
			"cFcXasw==";

	/**
	 * JWT
	 */
	private static String jws = "2AMZFXZo3dkKWhwEEEDzUwawXt1qYjrw2HtnXucWi" +
			"oP8DxKVGhYVQyLRHpv1Am56LkQPzVcKoJhTc85qRbuUGjU6v5i..acPB/W98e" +
			"BOJe8Gmc9MgIBzE1qgkTFZHR2ZSFZD8wpPZFfbVxqQUCv05U4jbA0z5pt+Utq" +
			"w10EW8oTb/XVA5jX/fYgLlvCkdmbe8ixxEmubWntjKZiQ8qS+SM8ueyiJIOFF" +
			"hC5J3TR7wVCNDqrueeybNXPuMXBPJ4oP4lGVyVEvbtrN2/t1CzvtYZFcz4q6S" +
			"UP4RFJiJp+ltKt8GJhxDZ77kfGG99aAPbdOEaiegsRLDJ/Aab7o5jDIwifR7o" +
			"Z4kZPeBH+8ONcI6soxnq5Dha9ZDKkAeCClEBWkw/l6KpjUZisnvTarKbNg3DX" +
			"mHx8A+Iv4H0ENSFWQ/z3mcFcXasw==";

	@Autowired
	private MockMvc mvc;

	@Test
	void A_createJws() throws Exception {
		mvc.perform(post("/createJws")
						.content(claim)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty())
				.andDo(r -> jws = r.getResponse().getContentAsString());

	}
//	@Test
	void A_base58() throws Exception {
		mvc.perform(get("/base58/key")
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}

//	@Test
	void B_readJson() throws Exception {
		mvc.perform(get("/json")
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}

//	@Test
	void C_readJson() throws Exception {
		mvc.perform(post("/base58/json")
						.content(claim)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty())
				.andDo(r -> encData = r.getResponse().getContentAsString());
	}

//	@Test
	void D_encryptPrv() throws Exception {
		mvc.perform(post("/encryptPrv")
						.content(claim)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}

//	@Test
	void E_signature() throws Exception {
		mvc.perform(post("/signature")
						.param("header",claim)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty())
				.andDo(r -> signData = r.getResponse().getContentAsString());
	}

//	@Test
	void F_jws() throws Exception {
		mvc.perform(post("/jws")
						.param("header", encData)
						.param("payload", "")
						.param("signature", signData)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty())
				.andDo(r -> jws = r.getResponse().getContentAsString());
	}
}