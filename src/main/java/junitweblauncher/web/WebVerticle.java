package junitweblauncher.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import junitweblauncher.launcher.LauncherAdapterImpl;
import junitweblauncher.launcher.TestMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebVerticle extends AbstractVerticle {
    private static final String EVENT_ADDRESS = "console_messages_created";

    @Override
    public void start() {
        startHttpServer();
    }

    private void startHttpServer() {
        HttpServer server = vertx.createHttpServer();
        server.webSocketHandler(this::onWebSocketConnected);

        Router router = Router.router(vertx);
        router.route().handler(StaticHandler.create("web"));
        router.route().handler(BodyHandler.create());
        router.route("/api/list-test-methods").handler(this::listTestMethods);
        router.route("/api/run-test-methods").handler(this::runTestMethods);

        String logMsg = """
                WebVerticle started, instance={}
                http://localhost:#{port}/
                http://localhost:#{port}/list-test-methods
                http://localhost:#{port}/run-test-methods
                """;

        server.requestHandler(router).listen(config().getInteger("port"))
                .onSuccess(httpServer -> log.info(logMsg.replace("#{port}", String.valueOf(httpServer.actualPort())), Integer.toHexString(this.hashCode())))
                .onFailure(err -> log.error("failed to start task queue support.", err));
    }

    private void listTestMethods(io.vertx.ext.web.RoutingContext routingContext) {
        // get the package name
        List<String> packageName = routingContext.queryParam("package");
        log.info("listTestMethods: package={}", packageName);
        List<TestMethod> testMethods = new LauncherAdapterImpl().listTestMethods(!packageName.isEmpty() ? packageName.getFirst() : "");
        routingContext.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(testMethods));
    }

    private void runTestMethods(io.vertx.ext.web.RoutingContext routingContext) {
        LauncherAdapterImpl launcherAdapter = new LauncherAdapterImpl();
        List<String> testMethods = routingContext.body().asJsonObject().getJsonArray("testMethods").getList();
        launcherAdapter.runTestMethods(testMethods);

        routingContext.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(Map.of("status", "ok")));
    }

    private void onWebSocketConnected(ServerWebSocket webSocket) {
        webSocket.writeTextMessage("Welcome to the web console!");
        vertx.eventBus().consumer(EVENT_ADDRESS, message -> {
            webSocket.writeTextMessage((String) message.body()); // redirect the message to websocket (web)
        });
    }
}
