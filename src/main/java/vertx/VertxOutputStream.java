package vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 *
 */
public class VertxOutputStream extends OutputStream {

    private final HttpServerResponse response;
    private Buffer buffer = Buffer.buffer();
    private boolean isClosed;

    VertxOutputStream(HttpServerResponse response) {
        this.response = response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        checkState();
        buffer.appendByte((byte) b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        checkState();
        buffer.appendBytes(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkState();
        if (off == 0 && len == b.length) {
            buffer.appendBytes(b);
        } else {
            buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        checkState();
        // Only flush to underlying very.x response if the content-length has been set
        if (buffer.length() > 0 && response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
            response.write(buffer);
            buffer = Buffer.buffer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // Write any remaining buffer to the vert.x response
        // Set content-length if not set yet
        if (buffer != null && buffer.length() > 0) {
            if (!response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
                response.headers().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            }
            response.write(buffer);
        }
        buffer = null;
        isClosed = true;
    }

    private void checkState() {
        if (isClosed) {
            throw new RuntimeException("Stream is closed");
        }
    }
}