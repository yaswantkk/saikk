package com.sdp3.main.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sdp3.main.entity.Request;
import com.sdp3.main.entity.User;
import com.sdp3.main.repository.RequestRepo;
import com.sdp3.main.service.EmailSenderService;
import com.sdp3.main.service.UserService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {
	
	
	@Autowired
	private UserService service;

	@Autowired
	private RequestRepo arepo;

	@Autowired
	private EmailSenderService emailserv;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@RequestMapping("/")
	public String homePage(Model model) {
		model.addAttribute("title","This is home page");
		return "index";
	}
	
	
	@RequestMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("title","This is Register page");
		model.addAttribute("user",new User());
		return "register";
	}
	
	@RequestMapping("/signin")
	public String loginPage(Model model) {
		model.addAttribute("title","This is Login page");
		return "login";
	}
	
	@PostMapping("/registerUser")
	public String registerUser(@ModelAttribute("User") User user,Model model,HttpSession session,
			RedirectAttributes ra) {
		
		User userExists = service.getEmail(user.getEmail());
		String role = user.getRole();
		
		if (userExists != null) {
			ra.addAttribute("user",new User());
			ra.addAttribute("title","This is Register page");
			ra.addFlashAttribute("message", "Failed");
			 ra.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/register";
		}else {
		
			try {
				
				if(role.equals("student")) {
					user.setRole("ROLE_USER");
				}else {
					user.setRole("ROLE_TEACHER");
				}
				user.setPassword(passwordEncoder.encode(user.getPassword()));
				this.service.saveUser(user);
				ra.addAttribute("user",new User());
				ra.addAttribute("title","This is Register page");
				ra.addFlashAttribute("flag","showAlert");
				return "redirect:/signin";
			}catch(Exception e) {
				ra.addAttribute("user",new User());
				ra.addAttribute("title","This is Register page");
				ra.addFlashAttribute("flag","showAlert");
				return "redirect:/register";
			}
		}
		
	}
	@RequestMapping("/application")
	public String showApp(Model model){
		model.addAttribute("tilte", "Application");
		return "application";
	}
	@PostMapping("/saveApplication")
	public String saveApplication(Model model,@ModelAttribute("Request") Request application,RedirectAttributes ra,HttpSession session){
		String otp = getOTP();
		String toEmail = application.getEmail();
		String body = "Please use the code "+otp+" to verify your email.";
		String subject="Verify Your Email - Learning Mangagement System";
		System.out.println("Email Service Triggered");
		emailserv.sendSimpleEmail(toEmail, body, subject);
		application.setRequestedDate(new java.util.Date());
		application.setVerified(false);
		arepo.save(application);
		session.setAttribute("otp", otp);
		session.setAttribute("email", toEmail);
		model.addAttribute("title", "Email Verification");
		return "redirect:/verifyEmail";
	}

	public String getOTP() {
		
		Random rnd = new Random();
		int number = rnd.nextInt(999999);

		return String.format("%06d", number);
	}

	@RequestMapping(value="/verifyEmail", method=RequestMethod.GET)
	public String verifyEmail(Model model) {
		return "verifyEmail";
	}
	
	@PostMapping("/verify-email-otp")
	public String verifyOtp(@RequestParam("userOtp") String userOtp, Model model,RedirectAttributes ra,HttpSession session){
		String otp = (String) session.getAttribute("otp");
		String email = (String) session.getAttribute("email");
		Request ap = arepo.findApplicantByEmail(email);
		boolean flag = false;
		if(otp.equals(userOtp)){
			flag = true;
			ap.setVerified(true);
			arepo.save(ap);
			session.invalidate();
		}
		if(flag){
			String toEmail = email;
			String body = "Dear "+ap.getFullName()+", \nPlease send your highest education details like certificates to this email for your account verification.\nTeam LMS.\nThank You.";
			String subject="Further details for Account verification - Learning Mangagement System";
			emailserv.sendSimpleEmail(toEmail, body, subject);
			return "redirect:/";
		}
		return "redirect:/application";
	}

	
}
