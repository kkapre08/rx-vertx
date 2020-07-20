package com.example.vertx.rxvertx_college;

import io.vertx.core.AsyncResult;
import io.vertx.reactivex.core.http.HttpServerResponse;

public class RedisResponseHandler {
  public static void getStudent(HttpServerResponse serverResponse, AsyncResult<String> r) {

    if(r.succeeded()) {
      if(r!=null && r.result()!=null) {
        serverResponse.setStatusCode(200).end(r.result());
      } else {
        serverResponse.setStatusCode(400).end( "Student not found");
      }
    } else if(r.failed()) {
      serverResponse.setStatusCode(500).end( "Failed to retrieve student details");
    }
  }
}
