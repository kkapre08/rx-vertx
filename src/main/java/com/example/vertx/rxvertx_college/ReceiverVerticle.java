package com.example.vertx.rxvertx_college;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.redis.RedisClient;

import java.util.ArrayList;
import java.util.List;

public class ReceiverVerticle extends AbstractVerticle  {

  RedisClient redisClient;

  EventBus eventBus;

  @Override
  public void start(Promise<Void> promise) throws Exception {

    redisClient = RedisClient.create(vertx, new JsonObject());

    eventBus = vertx.eventBus();

    eventBus.consumer("redis.consumer", e -> {

        System.out.println(e.body().toString());

        e.reply("Received the request");

    });

    eventBus.consumer("get.consumer", e-> {
      e.reply("Request received for "+e.body().toString());
      getStudent(e.body().toString());
    });

  }

  private void getStudent(String studentId) {

    redisClient.get(studentId,handler -> {

      String result = "";

      if(handler.succeeded() && handler.result()!=null) {
        result = handler.result().toString();
      } else {
        result = "400";
      }

      eventBus.send("get.consumer.response",result,res -> {
        if(res.succeeded()) {
          System.out.println("Successfully responded back");
        } else {
          System.out.println("Failed to respond back");
        }
      });

    });
  }

}
