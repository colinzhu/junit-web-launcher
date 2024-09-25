package junitweblauncher.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import junitweblauncher.launcher.LauncherAdapter;
import junitweblauncher.launcher.LauncherAdapterImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebVerticle extends AbstractVerticle {
    private static final String EVENT_ADDRESS = "console_messages_created";
    LauncherAdapterImpl launcherAdapter = new LauncherAdapterImpl();

    @Override
    public void start() {
        startHttpServer();
    }

    private void startHttpServer() {
        HttpServer server = vertx.createHttpServer();
        server.webSocketHandler(this::onWebSocketConnected);

        Router router = Router.router(vertx);
        router.route("/log/*").handler(StaticHandler.create("./log").setDirectoryListing(true));
        router.route("/report/*").handler(StaticHandler.create("./allure-reports").setCachingEnabled(false));
        router.route().handler(StaticHandler.create("web"));
        router.route().handler(BodyHandler.create());
        router.route("/api/list-test-methods").handler(this::listTestMethods);
        router.route("/api/run-test-methods").handler(this::runTestMethods);

        String logMsg = """
                WebVerticle started, instance={}
                http://localhost:#{port}/
                """;

        server.requestHandler(router).listen(config().getInteger("port"))
                .onSuccess(httpServer -> log.info(logMsg.replace("#{port}", String.valueOf(httpServer.actualPort())), Integer.toHexString(this.hashCode())))
                .onFailure(err -> log.error("failed to start web server.", err));
    }

    private void listTestMethods(io.vertx.ext.web.RoutingContext routingContext) {
        List<String> packageParam = routingContext.queryParam("package");
        String pkg = packageParam.isEmpty() ? config().getString("pkg") : packageParam.getFirst();

        List<String> listTypeParam = routingContext.queryParam("listType");
        String listType = listTypeParam.isEmpty() ? "class" : listTypeParam.getFirst();

        List<LauncherAdapter.TestItem> testItems = new LauncherAdapterImpl().listTestItems(pkg, listType);
        routingContext.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(Map.of("package", pkg, "availableTestItems", testItems)));
    }

    private void runTestMethods(io.vertx.ext.web.RoutingContext routingContext) {
        List<String> testMethods = routingContext.body().asJsonObject().getJsonArray("testMethods").getList();
        String runId = Instant.now().toString().replace(":", "-");
        System.setProperty("runId", runId);
        vertx.executeBlocking(() -> launcherAdapter.runTestMethods(testMethods))
                .onSuccess(res -> {
                    log.info("test methods executed successfully");
                    routingContext.response().putHeader("content-type", "application/json")
                            .end(Json.encodePrettily(res));
                })
                .onFailure(err -> {
                    log.error("failed to execute test methods", err);
                    routingContext.response().putHeader("content-type", "application/json")
                            .setStatusCode(500)
                            .end(Json.encodePrettily(Map.of("status", "error", "runId", runId, "message", err.getMessage())));

                });
    }

    private void onWebSocketConnected(ServerWebSocket webSocket) {
        webSocket.writeTextMessage("Welcome to JUnit Web Launcher!");
        vertx.eventBus().consumer(EVENT_ADDRESS, message -> {
            webSocket.writeTextMessage((String) message.body()); // redirect the message to websocket (web)
        });
    }
}
