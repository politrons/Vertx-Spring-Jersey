import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import vertx.JerseyRouter;

/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 * <p>
 * Vertx server to run F2E using our own JerseyRouter and JerseyHandler implementation
 * to use JAX-RS annotations in our resources
 */
public class VertxServer extends AbstractVerticle {

    private static final String CLASSPATH_APPLICATION_CONTEXT_XML = "classpath:applicationContext.xml";
    private static final String PRESENTATION_RESOURCE = "presentation.resource";

    @Override
    public void start(Future<Void> startFuture) {
        JsonObject jsonObject = vertx.getOrCreateContext().config();
        String host = jsonObject.getString("host");
        Integer port = jsonObject.getInteger("port");
        vertx.createHttpServer()
                .requestHandler(JerseyRouter.router(vertx, CLASSPATH_APPLICATION_CONTEXT_XML,
                        PRESENTATION_RESOURCE, host, port)::accept)
                .listen(port);
        startFuture.complete();
    }

}