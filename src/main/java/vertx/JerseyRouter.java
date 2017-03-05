package vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.impl.RouterImpl;
import org.glassfish.jersey.server.ResourceConfig;
import spring.ApplicationContextUtils;

import java.net.URI;

/**
 * Created by pabloperezgarcia on 05/03/2017.
 *
 * This class extend the RouterImpl to route all traffic to JerseyHandler where we transform the
 * Vertx HttpServerRequest to Jersey ContainerRequest.
 *
 * In order to make Jersey handler the request to the resources, we need to provide the ResourceConfig
 * Here using the Spring applicationContext file and the package resources we create it and pass it to
 * JerseyHandler which will use to route all request
 */
public class JerseyRouter extends RouterImpl {

    private ResourceConfig rc;

    public static Router router(Vertx vertx, String applicationContext, String packageResource,
                                String host, Integer port) {
        return new JerseyRouter(vertx, applicationContext, packageResource, host, port);
    }

    private JerseyRouter(Vertx vertx, String applicationContext, String packageResource,
                         String host, Integer port) {
        super(vertx);
        this.rc = ApplicationContextUtils.getResourceConfig(applicationContext, packageResource);
        this.route().handler(BodyHandler.create());
        this.route().handler(createJerseyHandler(host, String.valueOf(port)));
    }

    private JerseyHandler createJerseyHandler(String host, String port) {
        return new JerseyHandler(URI.create(host + port + "/"), rc);
    }
}
