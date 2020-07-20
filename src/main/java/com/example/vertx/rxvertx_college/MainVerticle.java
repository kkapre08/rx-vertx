//package com.example.vertx.rxvertx_college;
//
////import io.vertx.core.AbstractVerticle;
//import io.vertx.reactivex.core.AbstractVerticle;
//import io.vertx.core.Future;
//import io.vertx.core.Promise;
//import io.vertx.rxjava.ext.jdbc.JDBCClient;
//
//public class MainVerticle extends AbstractVerticle {
//
//  
//	 private JDBCClient dbClient;
//	
//	 
////	 private Future<Void> prepareDatabase() {
////		 
////		 
////		 
////		 
////		 
////	 }
//	 
//	 @Override
//	 public void start(Promise<Void> startPromise) throws Exception {
//		  
//		  
//	    vertx.createHttpServer().requestHandler(req -> {
//	      req.response()
//	        .putHeader("content-type", "text/plain")
//	        .end("Hello from Vert.x!");
//	    }).listen(8888, http -> {
//	      if (http.succeeded()) {
//	        startPromise.complete();
//	        System.out.println("HTTP server started on port 8888");
//	      } else {
//	        startPromise.fail(http.cause());
//	      }
//	    });
//	  }
//}

package com.example.vertx.rxvertx_college;

import com.example.vertx.rxvertx_college.datasource.Departments;
import com.example.vertx.rxvertx_college.datasource.StudentDataSource;
//import io.vertx.core.Future;x
//import io.vertx.core.Future;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
//import io.vertx.example.util.Runner;
import io.vertx.reactivex.core.*;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.RedisConnection;
//import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.client.RedisOptions;

import java.util.*;

/*
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MainVerticle extends AbstractVerticle {


  RedisClient redisClient;

  EventBus eventBus;

  @Override
  public void start(Promise<Void> promise) throws Exception {

    RxHelper.deployVerticle(vertx, new ReceiverVerticle()).subscribe(e -> {
        System.out.println("deployment id: "+e);
    }, err -> {
      System.out.println("deployment Error: "+err);
    });

    eventBus = vertx.eventBus();

    redisClient = RedisClient.create(vertx, new JsonObject());

    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());

    router.get("/students/:studentId").handler(this::getStudent);

    router.get("/init-db").handler(this::initRedisDB);

    router.get("/message").handler(this::sendMessage);

    router.delete("/students/:studentId").handler(this::deleteKey);

    router.post("/students").handler(this::insertStudent);

    router.put("/students/:studentId").handler(this::updateStudent);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(// Retrieve the port from the configuration,
      // default to 8080.
      config().getInteger("http.port", 8080),
      result -> {
        if (result.succeeded()) {
          promise.complete();
//          fut.complete();
        } else {
          promise.fail(result.cause());
//          fut.fail(result.cause());
        }
      });
  }

  private  void sendMessage(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    eventBus.send("redis.consumer","Hello receiver", res -> {
      eventBus.consumer("redis.get.consumer", re -> {
        System.out.println(re.body().toString());
        serverResponse.setStatusCode(200).end(re.body().toString());
      });
      messageResponse(serverResponse, res);
    });
  }

  private void messageResponse(HttpServerResponse serverResponse, AsyncResult<Message<Object>> res) {
    if(res.succeeded()) {
      System.out.println("Response: "+res.result().toString());
//      serverResponse.setStatusCode(200).end(res.result().body().toString());
    } else  {
      System.out.println("Failed: "+res);
//      serverResponse.setStatusCode(500).end(res.toString());
    }
  }

  public void initRedisDB(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    JsonArray students = StudentDataSource.getStudents();

    System.out.println("Students: "+students);

    for (Object student: students) {

      JsonObject jsonObject = (JsonObject) student;

      redisClient.set(jsonObject.getString("studentId"),jsonObject.toString(), r -> {

          if(r.succeeded()) {

            System.out.println("Student data initialized");

          } else if(r.failed()) {

            System.out.println("Student data initialization failed");

          }
      });
      serverResponse.setStatusCode(200).end(students.toString());
    }
  }


  private void insertStudent(RoutingContext routingContext) {

    JsonObject student = routingContext.getBodyAsJson();

    String studentId = UUID.randomUUID().toString();

    student.put("studentId", studentId);

    HttpServerResponse serverResponse = routingContext.response();

    Optional<Departments>  departmentName =
      Arrays.stream(Departments.values()).filter(e -> e.toString().equals(student.getString("DepartmentName"))).findAny();

//    validations before inserting students
    if(studentId==null || !"STUDENT".equals(student.getString("docType"))
      || !departmentName.isPresent()) {

      serverResponse.setStatusCode(400).end("Invalid student request");

    } else {
      student.put("DepartmentID", departmentName.get().department_id);

      redisClient.set(student.getString("studentId"),student.toString(), r -> {
        if (r.succeeded()) {
          serverResponse.setStatusCode(200).end(student.toString());
        } else if(r.failed()) {
          serverResponse.setStatusCode(500).end("Failed to insert the student");
        }
      });

    }
  }

  private void updateStudent(RoutingContext routingContext) {

    JsonObject student = routingContext.getBodyAsJson();

    String studentId = routingContext.pathParam("studentId");

    HttpServerResponse serverResponse = routingContext.response();

    Optional<Departments>  departmentName =
      Arrays.stream(Departments.values()).filter(e -> e.toString().equals(student.getString("DepartmentName"))).findAny();

//    validations before inserting students
    if(studentId==null || !"STUDENT".equals(student.getString("docType"))
      || !departmentName.isPresent()) {

      serverResponse.setStatusCode(400).end("Invalid student request");

    } else {

      student.put("DepartmentID", departmentName.get().department_id);

      student.put("studentId", studentId);

      redisClient.get(studentId, r -> {

        if(r.succeeded()) {

          if(r!=null && r.result()!=null) {

            redisClient.set(student.getString("studentId"),student.toString(), u -> {

              if (u.succeeded()) {
                serverResponse.setStatusCode(200).end(student.toString());
              } else if(u.failed()) {
                serverResponse.setStatusCode(500).end("Failed to insert the student");
              }

            });

          } else {

            serverResponse.setStatusCode(400).end("Student not found in the system");

            System.out.println("Student not found in the system");
          }
        } else if(r.failed()) {
          serverResponse.setStatusCode(500).end( "Failed to retrieve student details");
        }
      });

    }
  }

  private void getStudent(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    String studentId = routingContext.pathParam("studentId");

    eventBus.send("get.consumer",studentId, requestResult -> {
      if(requestResult.succeeded()) {
        System.out.println(requestResult.result().body().toString());
      } else {
        routingContext.response().setStatusCode(500).end("Failed to process the request");
      }
    });

    MessageConsumer<String> consumer = eventBus.consumer("get.consumer.response");

    consumer.handler(responseResult -> {
      responseResult.reply("Thank you!");
      if(responseResult.body().toString().equals("400")) {
        routingContext.response().setStatusCode(400).end("Student id not found");
        } else {
        routingContext.response().setStatusCode(200).end(responseResult.body().toString());
        }
      consumer.unregister();
      });

//    redisClient.get(studentId, r -> {
//      RedisResponseHandler.getStudent(serverResponse, r);
//    });

  }


  private void deleteKey (RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    String studentId = routingContext.pathParam("studentId");

    redisClient.del(studentId, r -> {
      if(r.succeeded()) {
        if(r!=null && r.result()!=null) {
          System.out.println("Successfully deleted the Key" + r.result());
          serverResponse.setStatusCode(200).end("Student deleted successfully");
        } else {
          serverResponse.setStatusCode(400).end("Student with the provided key not found");
        }
      } else if(r.failed()) {
          serverResponse.setStatusCode(500).end("Failed to execute the delete operation");
      }
    });
  }


  //not used in current code
  private  RedisConnection getConnection(HttpServerResponse serverResponse) {

    List<String> result = new ArrayList<>();

    Redis.createClient(vertx, new RedisOptions())
      .connect(onConnect -> {
        if (onConnect.succeeded()) {
          RedisConnection client = onConnect.result();

          System.out.println("Connection successful");

          RedisAPI redis = RedisAPI.api(client);

          redis.append("mykey-1","myvalue-1",e -> {
              if(e.succeeded()) {
                System.out.println("successfully stored");
              } else if(e.failed()) {
                System.out.println("failed to store");
              }
          });

          getKeyValue(serverResponse, redis);

          redis.del(Arrays.asList("mykey-1"), e -> {
            if(e.succeeded()) {
              System.out.println("Successfully deleted the key");
            } else if(e.failed()) {
              System.out.println("failed to delete");
            }
          });

        }else if(onConnect.failed()) {
          System.out.println("Connection failed"+onConnect.cause().fillInStackTrace());
        }
      });

      System.out.println(result);

    return null;
  }

  //not used in current code
  private void getKeyValue(HttpServerResponse serverResponse, RedisAPI redis) {
    redis.get("mykey1", e-> {
      if(e.succeeded()) {
        if(e!=null && e.result()!=null) {
//              result.add(e.result().toString());
        System.out.println("Value found: "+e);
        serverResponse.setStatusCode(200).end(e.toString());} else {
          serverResponse.setStatusCode(400).end("Value not found");
        }
//              System.out.println("Successfully received the value "+e.result());
      } else if(e.failed()){

        System.out.println("failed to fetch");
      }
    });
  }

}
