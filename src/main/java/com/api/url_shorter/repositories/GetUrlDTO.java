package com.api.url_shorter.repositories;

public record GetUrlDTO(String shortUrl, String longUrl, Long clicks) {
	public GetUrlDTO(String shortUrl, String longUrl, Long clicks)
	{
		this.shortUrl = shortUrl;
		this.longUrl = longUrl;
		this.clicks = clicks;
	}
}
