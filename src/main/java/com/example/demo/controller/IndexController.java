package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

public class IndexController {
	@GetMapping("/")
	public String index(Model model){
		return "templates/index.html";
	}
}
