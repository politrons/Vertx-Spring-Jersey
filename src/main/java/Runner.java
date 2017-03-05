import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created by pabloperezgarcia on 04/03/2017.
 *
 */
public class Runner {

    public static void main(String[] args) throws InstantiationException,
            IllegalAccessException, InterruptedException {
        initVertxServer();
    }

    private static void initVertxServer() {
        DeploymentOptions options = new DeploymentOptions();
        final int port = 8888;
        options.setConfig(new JsonObject()
                .put("host", "http://0.0.0.0:")
                .put("port", port));
        buildVertxServer(options);
    }

    private static void buildVertxServer(DeploymentOptions options) {
        ObservableFuture<String> observableFuture = RxHelper.observableFuture();
        Vertx.vertx().deployVerticle(VertxServer.class.getName(), options, observableFuture.toHandler());
        observableFuture.timeout(1, TimeUnit.MINUTES)
                .toBlocking()
                .first();
        System.out.println("**********************************");
        System.out.println("Vertx server started successfully");
        System.out.println("**********************************");
    }
}
