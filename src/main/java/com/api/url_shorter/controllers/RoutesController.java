package com.api.url_shorter.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.url_shorter.documents.UrlEntity;
import com.api.url_shorter.documents.User;
import com.api.url_shorter.repositories.UrlRepository;
import com.api.url_shorter.services.Base62;
import com.api.url_shorter.services.CounterService;
import com.api.url_shorter.services.IncrementClickService;
import com.mongodb.client.MongoCollection;

import java.time.Instant;




@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:5670")
public class RoutesController {
	@Autowired
	private UrlRepository repo;
	@Autowired
	private CounterService counter;
	@Autowired
	private MongoTemplate template;
	@Autowired
	private IncrementClickService clickService;
	
	
	
	private String getOriginalUrl(@PathVariable String shortUrl)
	{
		
		Query query = new Query(Criteria.where("_id").is(shortUrl));
		query.fields().include("originalUrl").exclude("_id");
		UrlEntity result = template.findOne(
				query, UrlEntity.class, "Url");
		
		return result.getOriginalUrl();
	}
	
	
	
	@GetMapping("/{url}")
	public ResponseEntity<Void> redirect(@PathVariable String url)
	{
		HttpHeaders headers = new HttpHeaders();
		String originalUrl = getOriginalUrl(url);
		
		clickService.Increment(url);
		
		headers.setLocation(URI.create(originalUrl));
		
		return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
	}
	
	@PostMapping("/api/insert")
	public ResponseEntity<Map<String,String>> insertUrl(@RequestBody Map<String,String> reqParam)
	{
		UrlEntity url = new UrlEntity();
		String longUrl = reqParam.get("originalUrl");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		var user = (User)auth.getPrincipal();
		
		
		
		url.setShortUrl(shortenUrl(longUrl));
		url.setOriginalUrl(longUrl);
		url.setCreatedAt(Instant.now());
		url.setUser_id(user.getID());
		repo.save(url);
		
		Map<String, String> body = Map.of("shortUrl", url.getShortUrl());
		
		return new ResponseEntity<>(body, HttpStatus.OK);
		
	}
	
	
	@GetMapping("/api/get-urls")
	public ResponseEntity<Map<String,List<String>>> getOwner()
	{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		var user = (User)auth.getPrincipal();
		
		Query query = new Query(Criteria.where("user_id").is(user.getID()));
		
		
		
		
		List<UrlEntity> urls = template.find(query, UrlEntity.class, "Url");
		
		List<String> url_id = new ArrayList<String>();
		
		for(int i = 0; i < urls.toArray().length; i++)
		{
			url_id.add(urls.get(i).getShortUrl());
		}
		var body = Map.of("urls", url_id);
		
		
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
	

	public String shortenUrl(String longUrl)
	{
		long id = counter.incrementId();
		
		String shortUrl =  Base62.encode(id);
		
		return shortUrl;
		
	}
}
