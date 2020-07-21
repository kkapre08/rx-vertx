package com.example.vertx.rxvertx_college;

import com.example.vertx.rxvertx_college.constants.Constants;
import com.example.vertx.rxvertx_college.datasource.Departments;
import com.example.vertx.rxvertx_college.datasource.StudentDataSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.*;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;

import java.util.*;

public class MainVerticle extends AbstractVerticle {


  public static final int SUCCESS = 200;

  public static final String RESPONSE_REPLY = "Thank you!";

  public static final String FAILED_TO_PROCESS = "Failed to process the request";
  public static final String INVALID_STUDENT_REQUEST = "Invalid student request";

  RedisClient redisClient;

  EventBus eventBus;

  @Override
  public void start(Promise<Void> promise) throws Exception {

    DelpoyVerticle();

    initGlobal();

    ApplicationRouter(promise);

  }

  private void ApplicationRouter(Promise<Void> promise) {

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
      .listen(config().getInteger("http.port", 8080), result -> {
        if (result.succeeded()) {
          promise.complete();
        } else {
          promise.fail(result.cause());
        }
      });
  }

  private void initGlobal() {
    eventBus = vertx.eventBus();

    redisClient = RedisClient.create(vertx, new JsonObject());
  }

  private void DelpoyVerticle() {
    RxHelper.deployVerticle(vertx, new ReceiverVerticle()).subscribe(e -> {
        System.out.println("deployment id: "+e);
    }, err -> {
      System.out.println("deployment Error: "+err);
    });
  }

  //test method
  private  void sendMessage(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    eventBus.send("redis.consumer","Hello receiver", res -> {
      eventBus.consumer("redis.get.consumer", re -> {
        System.out.println(re.body().toString());
        serverResponse.setStatusCode(SUCCESS).end(re.body().toString());
      });
      messageResponse(serverResponse, res);
    });
  }
  //test method
  private void messageResponse(HttpServerResponse serverResponse, AsyncResult<Message<Object>> res) {
    if(res.succeeded()) {
      System.out.println("Response: "+res.result().toString());
//      serverResponse.setStatusCode(200).end(res.result().body().toString());
    } else  {
      System.out.println("Failed: "+res);
//      serverResponse.setStatusCode(500).end(res.toString());
    }
  }


  //test method
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
      serverResponse.setStatusCode(SUCCESS).end(students.toString());
    }
  }



  private void insertStudent(RoutingContext routingContext) {

    JsonObject student = routingContext.getBodyAsJson();

    String studentId = UUID.randomUUID().toString();

    student.put(Constants.STUDENT_ID, studentId);

    HttpServerResponse serverResponse = routingContext.response();

    Optional<Departments>  departmentName =
      Arrays.stream(Departments.values()).filter(e -> e.toString().equals(student.getString(Constants.DEPARTMENT_NAME))).findAny();

    if (validateStudentRequest(student, studentId, serverResponse, departmentName)) return;

    student.put(Constants.DEPARTMENT_ID, departmentName.get().department_id);

    insertStudent(routingContext, student);

  }

  private boolean validateStudentRequest(JsonObject student, String studentId, HttpServerResponse serverResponse, Optional<Departments> departmentName) {
    if (isValidStudentRequest(student, studentId, departmentName, Constants.STUDENT_DOC_TYPE, Constants.DOC_TYPE)) {

      serverResponse.setStatusCode(400).end(INVALID_STUDENT_REQUEST);

      return true;
    }
    return false;
  }


  private boolean isValidStudentRequest(JsonObject student, String studentId, Optional<Departments> departmentName, String studentDocType, String docType) {

    return (studentId == null || !studentDocType.equals(student.getString(docType)) || !departmentName.isPresent());
  }

  private void updateStudent(RoutingContext routingContext) {

    JsonObject student = routingContext.getBodyAsJson();

    String studentId = routingContext.pathParam(Constants.STUDENT_ID);

    HttpServerResponse serverResponse = routingContext.response();

    Optional<Departments>  departmentName =
      Arrays.stream(Departments.values()).filter(e -> e.toString().equals(student.getString(Constants.DEPARTMENT_NAME))).findAny();

    if (validateStudentRequest(student, studentId, serverResponse, departmentName)) return;

    student.put(Constants.DEPARTMENT_ID, departmentName.get().department_id);

    student.put(Constants.STUDENT_ID, studentId);

    redisClient.get(studentId, r -> {

      if(r.succeeded()) {

        if(r!=null && r.result()!=null) {

          insertStudent(routingContext, student);

        } else {

          serverResponse.setStatusCode(400).end("Student not found in the system");

        }
      } else if(r.failed()) {

        serverResponse.setStatusCode(500).end( "Failed to retrieve student details");
      }
    });


  }

  private void insertStudent(RoutingContext routingContext, JsonObject student) {

    eventBus.send(Constants.INSERT_STUDENT_REQUEST, student.toString(), insertRequest -> {

      eventBusRequest(insertRequest, routingContext.response());

    });

    MessageConsumer<String> consumer = eventBus.consumer(Constants.INSERT_STUDENT_RESPONSE);

    consumer.handler(responseResult -> {

      responseResult.reply(RESPONSE_REPLY);

      if (!responseResult.body().equals("500")) {

        routingContext.response().setStatusCode(SUCCESS).end(responseResult.body());

      } else {

        routingContext.response().setStatusCode(500).end("Failed to insert student");
      }

      consumer.unregister();

    });
  }

  private void getStudent(RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    String studentId = routingContext.pathParam(Constants.STUDENT_ID);

    eventBus.send(Constants.GET_ADDRESS_REQUEST,studentId, requestResult -> {

      eventBusRequest(requestResult, routingContext.response());

    });

    MessageConsumer<String> consumer = eventBus.consumer(Constants.GET_ADDRESS_RESPONSE);

    consumer.handler(responseResult -> {

        responseResult.reply(RESPONSE_REPLY);

        if(responseResult.body().toString().equals("400")) {

          routingContext.response().setStatusCode(400).end("Student id not found");

        } else {

          routingContext.response().setStatusCode(SUCCESS).end(responseResult.body().toString());

        }

        consumer.unregister();
      });

  }

  private void eventBusRequest(AsyncResult<Message<Object>> requestResult, HttpServerResponse response) {

    if (requestResult.succeeded()) {

      System.out.println(requestResult.result().body().toString());

    } else {

      response.setStatusCode(500).end(FAILED_TO_PROCESS);
    }
  }


  private void deleteKey (RoutingContext routingContext) {

    HttpServerResponse serverResponse = routingContext.response();

    String studentId = routingContext.pathParam(Constants.STUDENT_ID);

    eventBus.send(Constants.DELETE_ADDRESS_REQUEST,studentId, requestResult -> {

      eventBusRequest(requestResult, serverResponse);

      MessageConsumer<String> consumer = eventBus.consumer(Constants.DELETE_ADDRESS_RESPONSE);

      consumer.handler(responseReply -> {

        responseReply.reply(RESPONSE_REPLY);

        if(responseReply.body().toString().equals("200")) {

          serverResponse.setStatusCode(SUCCESS).end("Student deleted Successfully");

        } else {

          serverResponse.setStatusCode(500).end("Failed to delete the student record");
        }

        consumer.unregister();
      });
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
        serverResponse.setStatusCode(SUCCESS).end(e.toString());} else {
          serverResponse.setStatusCode(400).end("Value not found");
        }
//              System.out.println("Successfully received the value "+e.result());
      } else if(e.failed()){

        System.out.println("failed to fetch");
      }
    });
  }

}
