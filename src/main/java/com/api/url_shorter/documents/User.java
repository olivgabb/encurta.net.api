package com.api.url_shorter.documents;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.api.url_shorter.config.UserRoles;

import lombok.EqualsAndHashCode;
import lombok.Getter;


@Document(collection="users")
@Getter
@EqualsAndHashCode(of="id")
public class User implements UserDetails{

	@Id
	private String id;
	private String username;
	private String password;
	private UserRoles role;
	
	public User(String username, String password, UserRoles role)
	{
		//this.id = UUID.randomUUID().toString();
		this.username = username;
		this.password = password;
		this.role = role;
	}
	
	public User() {
	}

	public User(String id, String username, String password, UserRoles role) {
	    this.id = id;
	    this.username = username;
	    this.password = password;
	    this.role = role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if(this.role == UserRoles.ADMIN) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public @Nullable String getPassword() {
		
		return this.password;
	}

	@Override
	public String getUsername() {
		
		return this.username;
	}
	
}
