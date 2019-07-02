package app;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VertxApp extends AbstractVerticle implements Handler<HttpServerRequest>
{
    static Logger logger = LoggerFactory.getLogger(VertxApp.class.getName());
    private HttpServer server;
    private CharSequence[] plaintextHeaders;
    private CharSequence dateString;

    private static final String HELLO_WORLD = "Hello, world!";
    private static final Buffer HELLO_WORLD_BUFFER = Buffer.factory.directBuffer(HELLO_WORLD, "UTF-8");

    private static final CharSequence RESPONSE_TYPE_PLAIN = HttpHeaders.createOptimized("text/plain");

    private static final CharSequence HEADER_SERVER = HttpHeaders.createOptimized("server");
    private static final CharSequence HEADER_DATE = HttpHeaders.createOptimized("date");
    private static final CharSequence HEADER_CONTENT_TYPE = HttpHeaders.createOptimized("content-type");
    private static final CharSequence HEADER_CONTENT_LENGTH = HttpHeaders.createOptimized("content-length");

    private static final CharSequence HELLO_WORLD_LENGTH = HttpHeaders.createOptimized("" + HELLO_WORLD.length());
    private static final CharSequence SERVER = HttpHeaders.createOptimized("vert.x");

    public static CharSequence createDateHeader() {
        return HttpHeaders.createOptimized(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()));
    }

    @Override
    public void start() throws Exception {
        int port = 8080;
        server = vertx.createHttpServer(new HttpServerOptions());
        server.requestHandler(VertxApp.this).listen(port);

        dateString = createDateHeader();
        plaintextHeaders = new CharSequence[] {
                                               HEADER_CONTENT_TYPE, RESPONSE_TYPE_PLAIN,
                                               HEADER_SERVER, SERVER,
                                               HEADER_DATE, dateString,
                                               HEADER_CONTENT_LENGTH, HELLO_WORLD_LENGTH };

    }

    @Override
    public void stop() {
        if (server != null) server.close();
    }

    @Override
    public void handle(HttpServerRequest request) {
        handlePlainText(request);
    }

    private void handlePlainText(HttpServerRequest request) {
        HttpServerResponse response = request.response();
        MultiMap headers = response.headers();
        for (int i = 0;i < plaintextHeaders.length; i+= 2) {
            headers.add(plaintextHeaders[i], plaintextHeaders[i + 1]);
        }
        response.end(HELLO_WORLD_BUFFER);
    }

    public static void main( String[] args )
    {
        Vertx vertx = Vertx.vertx();
        vertx.exceptionHandler(err -> {
            err.printStackTrace();
        });
        vertx.deployVerticle(VertxApp.class.getName(),
                             new DeploymentOptions().setInstances(VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE), event -> {
                                 if (event.succeeded()) {
                                     logger.info("Server listening on port " + 8080);
                                 } else {
                                     logger.error("Unable to start your application", event.cause());
                                 }
                             });


    }
}
