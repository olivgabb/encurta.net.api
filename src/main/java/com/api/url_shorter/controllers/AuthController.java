package com.api.url_shorter.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.url_shorter.documents.User;
import com.api.url_shorter.repositories.AuthDTO;
import com.api.url_shorter.repositories.RegisterDTO;
import com.api.url_shorter.repositories.UserRepository;
import com.api.url_shorter.services.JWTService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5670")
public class AuthController {
	
	@Autowired
	private AuthenticationManager authManager;
	@Autowired UserRepository userRepo;
	@Autowired PasswordEncoder encoder;
	@Autowired JWTService tokenService;
	
	@PostMapping("/login")
	public ResponseEntity<Map<String,String>> login(@RequestBody @Validated AuthDTO dto)
	{
		var userPassword = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
		var auth = this.authManager.authenticate(userPassword);
		
		var token = tokenService.generateToken((User)auth.getPrincipal());
		Map<String,String> body = Map.of("token", token);
		
		return new ResponseEntity<>(body,HttpStatus.OK);
	}
	
	@PostMapping("/validate-token")
	public ResponseEntity checkToken(@RequestBody @Validated Map<String,String> req)
	{
		if(tokenService.validateToken(req.get("token")) != "") return ResponseEntity.ok().build();
		return ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/get-username")
	public ResponseEntity<Map<String,String>> getUsername()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		var body = Map.of("username", auth.getName());
		
		if(auth == null || !auth.isAuthenticated())
			return ResponseEntity.badRequest().build();
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
	
	
	
	@PostMapping("/register")
	public ResponseEntity<Map<String,String>> register(@RequestBody @Validated RegisterDTO dto)
	{
		if(this.userRepo.findByUsername(dto.username()) != null) 
		{
			System.out.println("User already exists");
			return new ResponseEntity<>(Map.of("erro", "usuario ja existe"), HttpStatus.BAD_REQUEST);
		}
		var userPassword = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
		
		
		
		String encryptedPassword = encoder.encode(dto.password());
		
		User user = new User(dto.username(), encryptedPassword, dto.role());
		this.userRepo.save(user);
		
		var auth = this.authManager.authenticate(userPassword);
		
		var token = tokenService.generateToken((User)auth.getPrincipal());
		
		
		var body = Map.of("token", token);
		
		return new ResponseEntity<>(body, HttpStatus.OK);
		
	}
}
