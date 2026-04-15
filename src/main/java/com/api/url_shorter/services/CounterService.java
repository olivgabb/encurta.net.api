package com.api.url_shorter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import org.bson.Document;



@Service
public class CounterService {
	
	 @Autowired
	 private MongoTemplate mongoTemplate;

	    public long incrementId() {

	        Document result = mongoTemplate.findAndModify(
	                Query.query(where("_id").is("counter")),
	                new Update().inc("seq", 1),
	                FindAndModifyOptions.options().returnNew(true).upsert(true),
	                Document.class,
	                "id_counter" // 👈 NOME DA COLLECTION (IMPORTANTE)
	        );
	        Number seq = (Number)result.get("seq");
	        return seq.longValue();
	    }
}
