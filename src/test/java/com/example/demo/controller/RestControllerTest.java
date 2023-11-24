package com.example.demo.controller;


import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
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
	private static String encData = "5wX24gGwoapzavxLxduxLckpH5Wx1BLrdgoebHf9FJRQrYQ1QVCXGzm9w6tLfPgeWQcqEFEmqCj4Jsv6g";

	/**
	 * Signature
	 */
	private static String signData = "aLJON4wPZ+79ePLBe6CIDT5KPsIEVeTKvhAstUJLE9YrBEBL+4/4ZKehZJwvGZWuxjnaugQ05w" +
			"09pvmUrlL0MM9COB1IiMT2I8STmpRGHLo3pnrjh+An9PSIaRFstT4EnrWxNQuvn6eEIttbwxLeKurPo5TpnLegOwWR5MJzimNtQ" +
			"M+KKkaFlsc8ADj+e6033ZAdNClTHDe/jZXmtGDei/kcDA4Ofw3jN/539LyrnEvJ+leSNFtTi+xY0+7aL5zZxz+e4iIET9T2z5Z5" +
			"NckiF8r/+du7gBjbRjZiN+I1W2OMSV5XGu5FvjWQ9JnzCjjNPs35PJtoXaJcblNBbzmLrA==";

	/**
	 * JWT
	 */
	private static String jws = "5wX24gGwoapzavxLxduxLckpH5Wx1BLrdgoebHf9FJRQrYQ1QVCXGzm9w6tLfPgeWQcqEFEmqCj4Jsv6" +
			"g..aLJON4wPZ+79ePLBe6CIDT5KPsIEVeTKvhAstUJLE9YrBEBL+4/4ZKehZJwvGZWuxjnaugQ05w09pvmUrlL0MM9COB1IiMT2I" +
			"8STmpRGHLo3pnrjh+An9PSIaRFstT4EnrWxNQuvn6eEIttbwxLeKurPo5TpnLegOwWR5MJzimNtQM+KKkaFlsc8ADj+e6033ZAdN" +
			"ClTHDe/jZXmtGDei/kcDA4Ofw3jN/539LyrnEvJ+leSNFtTi+xY0+7aL5zZxz+e4iIET9T2z5Z5NckiF8r/+du7gBjbRjZiN+I1W" +
			"2OMSV5XGu5FvjWQ9JnzCjjNPs35PJtoXaJcblNBbzmLrA==";

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

	@Test
	void B_verifyJws() throws Exception {
		mvc.perform(post("/verifyJws")
						.content(jws)
						.session(SESSION))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
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