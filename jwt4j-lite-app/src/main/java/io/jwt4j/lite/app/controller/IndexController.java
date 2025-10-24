package io.jwt4j.lite.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class IndexController {
	@GetMapping("/")
	public String rootRedirect() {
		return "redirect:/api";
	}

	@GetMapping("/api")
	public String apiRedirect() {
		return "redirect:/swagger-ui/index.html";
	}
}