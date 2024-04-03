package com.example.demo;

import com.mongodb.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class DemoApplication {

    @Async
    @GetMapping("/getalleffects")
    @ResponseBody
    public CompletableFuture<ArrayList<Effect>> getTest() {
        System.out.println("Executing getalleffects method in thread: " + Thread.currentThread().getName());
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://sovyshka:ujdyfdnhzgjxre@cluster0.rpsi7sd.mongodb.net/?retryWrites=true&w=majority"));
        DB dataBase = mongoClient.getDB("leddb");
        DBCollection dataBaseCollection = dataBase.getCollection("visualeffects");

        BasicDBObject query = new BasicDBObject("effect", new BasicDBObject("$exists", true));
        DBCursor cursor = dataBaseCollection.find(query);

        ArrayList<Effect> effects = new ArrayList<>();

        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            String effectName = (String) document.get("name");
            String effectMode = (String) document.get("effect");
            int popularity = (int) document.get("popularity");
            String gif = (String) document.get("image");

            Effect effect = new Effect(effectName, effectMode, popularity, gif);
            effects.add(effect);
        }

        cursor.close();
        mongoClient.close();
        return CompletableFuture.completedFuture(effects);
    }

    @Async
    @GetMapping("/increasepopularity/{effectName}")
    public CompletableFuture<String> increasePopularity(@PathVariable String effectName) {
        System.out.println("Executing increasepopularity method for effectName: " + effectName + " in thread: " + Thread.currentThread().getName());
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://sovyshka:ujdyfdnhzgjxre@cluster0.rpsi7sd.mongodb.net/?retryWrites=true&w=majority"));
        DB dataBase = mongoClient.getDB("leddb");
        DBCollection dataBaseCollection = dataBase.getCollection("visualeffects");

        BasicDBObject query = new BasicDBObject("effect", effectName);
        DBObject document = dataBaseCollection.findOne(query);

        if (document != null) {
            int popularity = (int) document.get("popularity");
            popularity++;
            document.put("popularity", popularity);
            dataBaseCollection.save(document);
        }

        mongoClient.close();
        return CompletableFuture.completedFuture("OK");
    }

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
