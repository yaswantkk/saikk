package com.sdp3.main.controller;

import java.security.Principal;
import java.util.List;

import com.sdp3.main.entity.Request;
import com.sdp3.main.entity.User;
import com.sdp3.main.repository.RequestRepo;
import com.sdp3.main.service.EmailSenderService;
import com.sdp3.main.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
	private UserService service;

	@Autowired
	private EmailSenderService eservice;

	@Autowired
	private RequestRepo arepo;

    @ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		User user = service.getEmail(userName);
		model.addAttribute("user",user);
	}

    @GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title","Dashboard");
		return "admin/admin_dashboard";
	}

    @GetMapping("/requests")
	public String Requests(Model model) {
		List<Request> applist = arepo.findAll();
		model.addAttribute("title","Requests");
		model.addAttribute("appList", applist);
		return "admin/admin_requests";
	}
    @GetMapping("/instructors")
	public String addInstructor(Model model) {
		String role = "ROLE_TEACHER";
		List<User> u = service.findAllIntructors(role);
		model.addAttribute("ins", u);
		model.addAttribute("title","Instructors");
		return "admin/admin_instructors";
	}
}
