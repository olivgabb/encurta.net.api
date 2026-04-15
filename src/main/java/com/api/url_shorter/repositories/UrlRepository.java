package com.api.url_shorter.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.api.url_shorter.documents.UrlEntity;

public interface UrlRepository extends MongoRepository<UrlEntity, String>{

}
