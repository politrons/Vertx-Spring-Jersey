package vertx;

import io.netty.buffer.ByteBufInputStream;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.internal.JerseyRequestTimeoutHandler;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.StatusType;


/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 * <p>
 * This Handler class it´s the glue between Jersey facade and Vertx
 * <p>
 * We will intercept the Vertx RoutingContext and we will mutate to a Jersey request.
 * Also for the response we will create a ContainerResponseWriter through the Vertx HttpServerResponse.
 */
public class JerseyHandler implements Handler<RoutingContext> {

    private final static DefaultSecurityContext DEFAULT_SECURITY_CONTEXT = new DefaultSecurityContext();

    private final URI uri;
    private final ApplicationHandler jerseyHandler;

    JerseyHandler(URI uri, ResourceConfig jerseyConfig) {
        this.uri = uri;
        this.jerseyHandler = new ApplicationHandler(jerseyConfig);
    }

    /**
     * Handle of Vertx route, all request will be routed into this handle.
     *
     * @param context Represents the context for the handling of a request in Vert.x-Web.
     */
    @Override
    public void handle(final RoutingContext context) {
        final ContainerRequest jerseyRequest = createJerseyRequest(uri, context.request());
        jerseyRequest.setEntityStream(new ByteBufInputStream(context.getBody().getByteBuf()));
        jerseyRequest.setWriter(new ResponseWriter(context.request().response()));
        jerseyHandler.handle(jerseyRequest);
    }

    /**
     * Create a ContainerRequest to be interpreted by ApplicationHandler of Jersey
     */
    private ContainerRequest createJerseyRequest(URI baseUri, HttpServerRequest request) {
        final ContainerRequest result = new ContainerRequest(baseUri, baseUri.resolve(request.uri()),
                request.method().name(), DEFAULT_SECURITY_CONTEXT, new MapPropertiesDelegate());
        copyHttpHeadersFromTo(request, result);
        return result;
    }

    /**
     * Copy the headers from vertx request to jersey request
     */
    private void copyHttpHeadersFromTo(HttpServerRequest from, ContainerRequest to) {
        final MultivaluedMap<String, String> headers = to.getHeaders();
        from.headers().forEach(source -> headers.putSingle(source.getKey(), source.getValue()));
    }

    /**
     * A Request-scoped I/O container response writer.
     * <p>
     * I/O container sends a new instance of the response writer with every request as part of the call to the Jersey application
     */
    private static class ResponseWriter implements ContainerResponseWriter {

        private final HttpServerResponse response;
        private final JerseyRequestTimeoutHandler requestTimeoutHandler;

        ResponseWriter(HttpServerResponse response) {
            this.response = response;
            this.requestTimeoutHandler = new JerseyRequestTimeoutHandler(this, new DefaultEventExecutor());
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse rc) throws ContainerException {
            setStatus(rc);
            addHeaders(rc);
            return new VertxOutputStream(response);
        }

        private void addHeaders(ContainerResponse responseContext) {
            final MultivaluedMap<String, String> sourceHeaders = responseContext.getStringHeaders();
            final MultiMap responseHeaders = response.headers();
            sourceHeaders.entrySet().forEach(entry -> responseHeaders.add(entry.getKey(), entry.getValue()));
        }

        private void setStatus(ContainerResponse responseContext) {
            final StatusType statusInfo = responseContext.getStatusInfo();
            response.setStatusCode(statusInfo.getStatusCode());
            response.setStatusMessage(statusInfo.getReasonPhrase());
            response.setChunked(responseContext.isChunked());
        }

        @Override
        public void commit() {
            end();
        }

        @Override
        public void failure(Throwable error) {
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
            response.setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase());
            commit();
            rethrow(error);
        }

        @Override
        public boolean suspend(long time, TimeUnit unit, TimeoutHandler handler) {
            return requestTimeoutHandler.suspend(time, unit, handler);
        }

        @Override
        public void setSuspendTimeout(long time, TimeUnit unit) throws IllegalStateException {
            requestTimeoutHandler.setSuspendTimeout(time, unit);
        }

        @Override
        public boolean enableResponseBuffering() {
            return true;
        }

        /**
         * Commits the response and logs a warning message.
         * <p>
         * This method should be called by the container at the end of the
         * handle method to make sure that the ResponseWriter was committed.
         */
        private void end() {
            response.end();
        }

        /**
         * Rethrow the original exception as required by JAX-RS, 3.3.4
         *
         * @param error throwable to be re-thrown
         */
        private void rethrow(Throwable error) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new ContainerException(error);
            }
        }
    }


    private static class DefaultSecurityContext implements SecurityContext {

        static final Principal PRINCIPAL = new Principal() {
            @Override
            public String getName() {
                return "<not authorized>";
            }

            @Override
            public String toString() {
                return "Principal [" + getName() + "";
            }
        };

        @Override
        public Principal getUserPrincipal() {
            return PRINCIPAL;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return "NO_AUTH";
        }
    }
}