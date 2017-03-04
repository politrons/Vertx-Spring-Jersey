package vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 *
 * Vertx server to run F2E using our own JerseyHandler implementation to continue using JAX-RS annotations
 */
public class VertxServer extends AbstractVerticle {

    public static ResourceConfig config;

    @Override
    public void start(Future<Void> startFuture) {
        JsonObject jsonObject = vertx.getOrCreateContext().config();
        String host = jsonObject.getString("host");
        Integer port = jsonObject.getInteger("port");
        vertx.createHttpServer()
                .requestHandler(createRouter(host, port)::accept)
                .listen(port);
        startFuture.complete();
    }

    private Router createRouter(String host, Integer port) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(createJerseyHandler(host, String.valueOf(port)));
        return router;
    }

    private JerseyHandler createJerseyHandler(String host, String port) {
        return new JerseyHandler(URI.create(host + port + "/"), config);
    }

}