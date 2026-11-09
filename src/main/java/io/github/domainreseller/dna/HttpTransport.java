package io.github.domainreseller.dna;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The HTTP layer the client talks through. The default implementation is
 * {@link JdkHttpTransport} ({@code java.net.http.HttpClient}); tests and callers
 * with special needs (proxies, custom TLS, recording) can supply their own via
 * {@link DomainNameApi#setTransport(HttpTransport)}.
 *
 * <p>An implementation returns every HTTP response, whatever its status, and
 * throws {@link TransportException} only when no response was received
 * (timeout, DNS failure, TLS error, connection reset).</p>
 */
@FunctionalInterface
public interface HttpTransport {

    /**
     * Send one request.
     *
     * @param request the request to send
     * @return the response, for any HTTP status
     * @throws TransportException when no HTTP response was received
     */
    Response send(Request request) throws TransportException;

    /** An outgoing request. Headers keep their insertion order. */
    final class Request {
        private final String method;
        private final String url;
        private final Map<String, String> headers;
        private final String body;
        private final boolean sslVerify;
        private final int timeoutSeconds;
        private final int connectTimeoutSeconds;

        public Request(String method, String url, Map<String, String> headers, String body,
                       boolean sslVerify, int timeoutSeconds, int connectTimeoutSeconds) {
            this.method = method;
            this.url = url;
            this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
            this.body = body;
            this.sslVerify = sslVerify;
            this.timeoutSeconds = timeoutSeconds;
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        /** @return HTTP method: GET, POST, PUT or DELETE */
        public String getMethod() {
            return method;
        }

        /** @return absolute URL including the query string */
        public String getUrl() {
            return url;
        }

        /** @return request headers */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /** @return JSON body, or {@code null} for GET and DELETE */
        public String getBody() {
            return body;
        }

        /** @return whether the server certificate must be verified */
        public boolean isSslVerify() {
            return sslVerify;
        }

        /** @return total timeout in seconds */
        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        /** @return connect timeout in seconds */
        public int getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }
    }

    /** A received response. */
    final class Response {
        private final int status;
        private final String body;
        private final Map<String, String> headers;

        public Response(int status, String body) {
            this(status, body, Collections.emptyMap());
        }

        public Response(int status, String body, Map<String, String> headers) {
            this.status = status;
            this.body = body == null ? "" : body;
            this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        }

        /** @return HTTP status code */
        public int getStatus() {
            return status;
        }

        /** @return raw response body ("" when empty) */
        public String getBody() {
            return body;
        }

        /** @return response headers (lower-case names) */
        public Map<String, String> getHeaders() {
            return headers;
        }
    }

    /**
     * No HTTP response was received. {@link #getCode()} follows curl's error
     * numbers so results match php-dna: 28 timeout, 6 host not resolved,
     * 7 connection refused, 35/60 TLS, 56 connection reset/receive failure.
     */
    final class TransportException extends Exception {
        private static final long serialVersionUID = 1L;

        /** curl: operation timed out. */
        public static final int TIMEOUT = 28;
        /** curl: could not resolve host. */
        public static final int RESOLVE = 6;
        /** curl: could not connect. */
        public static final int CONNECT = 7;
        /** curl: TLS handshake failure. */
        public static final int SSL_CONNECT = 35;
        /** curl: peer certificate could not be verified. */
        public static final int SSL_CERT = 60;
        /** curl: failure receiving network data. */
        public static final int RECV = 56;

        private final int code;

        public TransportException(int code, String message) {
            super(message);
            this.code = code;
        }

        public TransportException(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        /** @return curl-style error number */
        public int getCode() {
            return code;
        }
    }
}
