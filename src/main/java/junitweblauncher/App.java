package junitweblauncher;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import junitweblauncher.web.SysOutToEventBus;
import junitweblauncher.web.WebVerticle;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

@Slf4j
public class App {
    public static void main(String[] args) {
        configureLogback();
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

        int parallelSize = 1;
        try {
            parallelSize = Integer.parseInt(args[2]);
        } catch (Exception e) {
            log.warn("Parallel run size is not provided, default 1, no parallel run");
        }
        log.info("Parallel run size:{}", parallelSize);

        vertx.deployVerticle(WebVerticle.class, new DeploymentOptions().setInstances(parallelSize).setConfig(new JsonObject(Map.of("port", port, "pkg", pkg))));
        new SysOutToEventBus(vertx);
    }

    @SneakyThrows
    private static void configureLogback() {
        // Get the logger context from SLF4J
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Reset any previous configuration
        context.reset();

        // Create the configurator to apply the new web-launcher-logback.xml
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);

        URL configFileUrl = App.class.getClassLoader().getResource("web-launcher-logback.xml");
        assert configFileUrl != null;
        configurator.doConfigure(configFileUrl);
    }
}