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
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            log.warn("Port number is not provided, default random");
        }
        log.info("Port:{}", port);
        String pkg = "example";
        try {
            pkg = args[1];
        } catch (Exception e) {
            log.warn("Package is not provided, default 'example'");
        }
        log.info("Test case package:{}", pkg);
        vertx.deployVerticle(WebVerticle.class, new DeploymentOptions().setInstances(1).setConfig(new JsonObject(Map.of("port", port, "pkg", pkg))));
        new SysOutToEventBus(vertx);
    }

}