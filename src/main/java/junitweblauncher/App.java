package junitweblauncher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import junitweblauncher.web.SysOutToEventBus;
import junitweblauncher.web.WebVerticle;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(WebVerticle.class, new DeploymentOptions().setInstances(1).setConfig(new JsonObject(Map.of("port", 8080))));
        new SysOutToEventBus(vertx);
    }

}