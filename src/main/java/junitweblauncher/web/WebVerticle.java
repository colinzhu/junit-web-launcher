package junitweblauncher.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
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

        router.route("/listCases").handler(this::listCases);
        router.route("/runCases").handler(this::runCases);
//        router.route("/taskqueue/*").subRouter(new PaymentCreateHandler(vertx, pool, TaskQueueService.taskQueue(vertx)).get());

        String logMsg = """
                WebVerticle started, instance={}
                http://localhost:#{port}/
                http://localhost:#{port}/listCases
                http://localhost:#{port}/runCases
                """;

        server.requestHandler(router).listen(config().getInteger("port"))
                .onSuccess(httpServer -> log.info(logMsg.replace("#{port}", String.valueOf(httpServer.actualPort())), Integer.toHexString(this.hashCode())))
                .onFailure(err -> log.error("failed to start task queue support.", err));
    }

    private void listCases(io.vertx.ext.web.RoutingContext routingContext) {
        List<TestMethod> testMethods = new LauncherAdapterImpl().listCases();
        routingContext.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(testMethods));
    }

    private void runCases(io.vertx.ext.web.RoutingContext routingContext) {
        LauncherAdapterImpl launcherAdapter = new LauncherAdapterImpl();
        List<TestMethod> testMethods = launcherAdapter.listCases();
        launcherAdapter.runCases(testMethods);

        routingContext.response().putHeader("content-type", "application/json")
                .end(Json.encodePrettily(Map.of("status", "ok")));
    }

    private void onWebSocketConnected(ServerWebSocket webSocket) {
        webSocket.writeTextMessage("Welcome to the web console!");
        vertx.eventBus().consumer(EVENT_ADDRESS, message -> {
            webSocket.writeTextMessage((String) message.body()); // redirect the message to websocket (web)
        });
//        webSocket.textMessageHandler(this::onMessageReceived);
    }
//
//    private void onMessageReceived(String msg) {
//        log.info("Message received: {}", msg);
//        if (msg.startsWith("startParams=")) {
//            if (isTaskRunning) {
//                log.info("One task is still running, cannot trigger to run now.");
//                return;
//            }
//            isTaskRunning = true;
//            String[] params = msg.replace("startParams=", "").split(" ");
//            vertx.executeBlocking(promise -> {
//                try {
//                    task.accept(params);
//                } catch (Exception e) {
//                    promise.fail(e);
//                }
//                promise.complete();
//            }).onSuccess(v -> {
//                log.info("executeBlock success");
//                isTaskRunning = false;
//            }).onFailure(err -> {
//                log.error("executeBlock error", err);
//                isTaskRunning = false;
//            });
//        }
//    }

}
