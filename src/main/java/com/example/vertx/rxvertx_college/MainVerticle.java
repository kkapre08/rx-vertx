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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
//import io.vertx.example.util.Runner;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.reactivex.Single;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.RedisConnection;
//import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.client.RedisOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import io.vertx.redis.RedisClient;

/*
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MainVerticle extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
//  public static void main(String[] args) {
//    Runner.runExample(Client.class);
//  }

  @Override
  public void start(Future<Void> fut) throws Exception {

//    getConnection();

    RedisClient redisClient = RedisClient.create(vertx, new JsonObject());

    redisClient.set("mykey-2","myvalue-2", e -> {
        if(e.succeeded()) {
          System.out.println("successfully stored value 2");
        } else if(e.failed()) {
          System.out.println("failed to store value 2");
        }
    });

    Router router = Router.router(vertx);

    router.route("/hello").handler(e -> {
      HttpServerResponse serverResponse = e.response();
      getConnection(serverResponse);
    });

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(// Retrieve the port from the configuration,
      // default to 8080.
      config().getInteger("http.port", 8080),
      result -> {
        if (result.succeeded()) {
          fut.complete();
        } else {
          fut.fail(result.cause());
        }
      });
  }

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
