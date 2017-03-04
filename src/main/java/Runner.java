import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import spring.ResourceConfigFactory;
import spring.impl.ResourceConfigFactoryImpl;
import vertx.VertxServer;

import java.util.concurrent.TimeUnit;

/**
 * Created by pabloperezgarcia on 04/03/2017.
 */
public class Runner {

    private static final String CLASSPATH_APPLICATION_CONTEXT_XML = "classpath:applicationContext.xml";
    private static final String PRESENTATION_RESOURCE = "presentation.resource";

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, InterruptedException {
        initVertxServer();
    }

    private static void initVertxServer() {
        VertxServer.config = getResourceConfig();
        DeploymentOptions options = new DeploymentOptions();
        final int port = 1981;
        options.setConfig(new JsonObject()
                .put("host", "http://0.0.0.0:")
                .put("port", port));
        buildVertxServer(port, options);
    }

    private static ResourceConfig getResourceConfig() {
        ClassPathXmlApplicationContext context = initializeApplicationContext();
        return buildResourceConfig(PRESENTATION_RESOURCE, context);
    }

    private static ResourceConfig buildResourceConfig(String resourcePackage, ClassPathXmlApplicationContext context) {
        ResourceConfigFactory resourceConfigFactory = getRequestFilterFactory(context);
        return resourceConfigFactory.build(resourcePackage, context);
    }

    private static ResourceConfigFactory getRequestFilterFactory(ClassPathXmlApplicationContext context) {
        return context
                .getBean(ResourceConfigFactoryImpl.class);
    }

    private static ClassPathXmlApplicationContext initializeApplicationContext() {
        return new ClassPathXmlApplicationContext(
                new String[]{CLASSPATH_APPLICATION_CONTEXT_XML});
    }


    private static void buildVertxServer(int port, DeploymentOptions options) {
        ObservableFuture<String> observableFuture = RxHelper.observableFuture();
        Vertx.vertx().deployVerticle(VertxServer.class.getName(), options, observableFuture.toHandler());
        observableFuture.timeout(1, TimeUnit.MINUTES)
                .toBlocking()
                .first();
        System.out.println("******************************************************");
        System.out.println("F2E started with Vertx successfully. Http Port: " + port);
        System.out.println("******************************************************");
    }
}
