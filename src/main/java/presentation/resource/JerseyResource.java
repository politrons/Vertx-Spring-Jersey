package presentation.resource;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import utils.RestConstants;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 *
 * A simple Jersey resource initialized with Spring and using JAX-RS annotation
 *
 */
@Component
@Singleton
@Path("/vertxMeetJersey")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Produces(RestConstants.PRODUCES_MEDIA_TYPES)
@Consumes(RestConstants.CONSUMES_MEDIA_TYPES)
public class JerseyResource {

    @GET
    public void read(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume("Spring + Jersey + Vertx playing together");
    }

}
