package com.api.url_shorter.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection="Url")
public class UrlEntity {
	@Id
	private String shortUrl;
	private String originalUrl;
	private Instant createdAt;
	private Long clicks; 
	private String user_id;
	
	public UrlEntity()
	{
		this.clicks=(long) 0;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public String getOriginalUrl() {
		return originalUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}
	public Long getClicks() {
		return clicks;
	}
	public void setClicks(Long clicks) {
		this.clicks = clicks;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	
	
	
}
