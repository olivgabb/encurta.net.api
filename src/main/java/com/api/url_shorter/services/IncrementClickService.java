package com.api.url_shorter.services;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class IncrementClickService {
	@Autowired
	private MongoTemplate template;
	
	public void Increment(String shortUrl)
	{

        Document result = template.findAndModify(
                Query.query(where("_id").is(shortUrl)),
                new Update().inc("clicks", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Document.class,
                "Url"
        );
	}
}
