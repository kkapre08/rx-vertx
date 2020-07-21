package com.example.vertx.rxvertx_college;

import com.example.vertx.rxvertx_college.constants.Constants;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.redis.RedisClient;


public class ReceiverVerticle extends AbstractVerticle  {

  RedisClient redisClient;

  EventBus eventBus;

  public static final String REQUEST_RECEIVED = "%S Request received for :%S";

  @Override
  public void start(Promise<Void> promise) throws Exception {

    redisClient = RedisClient.create(vertx, new JsonObject());

    eventBus = vertx.eventBus();

    InitHandlers();

  }

  private void InitHandlers() {
    eventBus.consumer(Constants.GET_ADDRESS_REQUEST, e-> {

      e.reply(String.format(REQUEST_RECEIVED,"GET",e.body().toString()));

      getStudent(e.body().toString());
    });

    eventBus.consumer(Constants.DELETE_ADDRESS_REQUEST, e-> {

      e.reply(String.format(REQUEST_RECEIVED,"DELETE",e.body().toString()));

      deleteStudent(e.body().toString());

    });

    eventBus.consumer(Constants.INSERT_STUDENT_REQUEST, e-> {

      e.reply(String.format(REQUEST_RECEIVED,"INSERT",e.body().toString()));

      insertStudent(e.body().toString());

    });
  }

  private void getStudent(String studentId) {

    redisClient.get(studentId,getResult -> {

      String result = "";

      if(getResult.succeeded() && getResult.result()!=null) {

        result = getResult.result().toString();

      } else {

        result = "400";
      }

      sendResponse(result, Constants.GET_ADDRESS_RESPONSE);

    });
  }

  private void deleteStudent(String studentId) {

    redisClient.del(studentId, deleteResult -> {
      String result = "";

      if(deleteResult.succeeded() && deleteResult.result()!=null) {
        result = "200";
      } else if(deleteResult.failed()) {
        result = "400";
      }
      else {
        result = "500";
      }

      sendResponse(result, Constants.DELETE_ADDRESS_RESPONSE);
    });
  }

  private void insertStudent(String studentJson) {

      JsonObject student = new JsonObject(studentJson);

      redisClient.set(student.getString("studentId"),student.toString(), r -> {

        String result = "";

        if (r.succeeded()) {

          result = studentJson;

        } else if(r.failed()) {

          result = "500";

        }

        sendResponse(result, Constants.INSERT_STUDENT_RESPONSE);

      });
  }

  private void sendResponse(String result, String Address) {

    eventBus.send(Address,result, res -> {

      if(res.succeeded()) {

        System.out.println(Constants.SUCCESSFULLY_RESPONDED);

      } else {

        System.out.println(Constants.RESPOND_FAILED);
      }
    });
  }

}
