package app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.core.ResteasyDeploymentImpl;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.undertow.Undertow;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Path("/")
public class ResteasyUndertowApp {
    static Logger logger = LoggerFactory.getLogger(ResteasyUndertowApp.class.getName());

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

    public static void main(String[] args) {
        UndertowJaxrsServer server = new UndertowJaxrsServer();
        
        ResteasyDeployment deployment = new ResteasyDeploymentImpl();
        deployment.getActualResourceClasses().add(ResteasyUndertowApp.class);
        deployment.setApplication(new Application() {});
        deployment.start();

        server.deploy(deployment);

        server.start(Undertow.builder()
                     // same as vertx
                     .setIoThreads(2 * CpuCoreSensor.availableProcessors())
                     .setWorkerThreads(20)
                     .addHttpListener(8080, "localhost"));
        System.out.println("Server started on localhost 8080");
    }
}
