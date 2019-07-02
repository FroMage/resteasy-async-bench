package app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Path("/")
public class ResteasyVertxApp {
    static Logger logger = LoggerFactory.getLogger(ResteasyVertxApp.class.getName());

    @GET
    public String get() {
        return "Hello, world";
    }
    
    @Path("suspend")
    @GET
    public void getSuspended(@Suspended AsyncResponse response) {
        response.resume("Hello, world");
    }

    @Path("suspend-new")
    @GET
    public CompletionStage<String> getCS() {
        return CompletableFuture.completedFuture("Hello, world");
    }

    public static void main(String[] args) throws InterruptedException {
        VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.getActualResourceClasses().add(ResteasyVertxApp.class);
        deployment.start();
        
        VertxOptions options = new VertxOptions();
        int ioThreads = 2 * CpuCoreSensor.availableProcessors();
        options.setEventLoopPoolSize(ioThreads);
        Vertx vertx = Vertx.vertx(options );
        vertx.exceptionHandler(err -> {
            err.printStackTrace();
        });
        
        CountDownLatch latch = new CountDownLatch(ioThreads);
        for (int i = 0; i < ioThreads; i++) {
            VertxRequestHandler requestHandler = new VertxRequestHandler(vertx, deployment);

            vertx.createHttpServer()
            .requestHandler(requestHandler)
            .listen(8080, "localhost", event -> {
                latch.countDown();
                if (!event.succeeded()) {
                    logger.error("Unable to start your application", event.cause());
                }
            });
        }
        
        latch.await();
        logger.info("Server listening on port " + 8080);
    }
}
