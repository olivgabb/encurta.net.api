package com.api.url_shorter.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.api.url_shorter.documents.User;

public interface UserRepository extends MongoRepository<User, String>{
	UserDetails findByUsername(String username);
}
