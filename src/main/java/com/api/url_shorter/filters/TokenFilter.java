package com.api.url_shorter.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.url_shorter.repositories.UserRepository;
import com.api.url_shorter.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class TokenFilter extends OncePerRequestFilter {

	
	@Autowired
	JWTService jwtService;
	@Autowired
	UserRepository userRepo;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var token = this.getToken(request);
		if(token!=null)
		{
			var login= jwtService.validateToken(token);
			UserDetails user = userRepo.findByUsername(login);
			
			var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		filterChain.doFilter(request, response);
	}
	
	private String getToken(HttpServletRequest req)
	{
		var token = req.getHeader("Authorization");
		
		if(token==null) return null;
		
		return token.replace("Bearer ", "");
	}

}
