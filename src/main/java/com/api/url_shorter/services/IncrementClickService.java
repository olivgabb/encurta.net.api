package com.api.url_shorter.services;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.api.url_shorter.repositories.ClickDTO;
import com.api.url_shorter.repositories.GetUrlDTO;

@Service
public class IncrementClickService {
	@Autowired
	private MongoTemplate template;
    @Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	public void Increment(String shortUrl)
	{

        GetUrlDTO dto = template.findAndModify(
                Query.query(where("_id").is(shortUrl)),
                new Update().inc("clicks", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                GetUrlDTO.class,
                "Url"
        );
        
        messagingTemplate.convertAndSend(
        		"/topic/clicks", dto);
        
        
	}
}
