package io.github.domainreseller.dna;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Default {@link HttpTransport} on {@code java.net.http.HttpClient}. Two clients
 * are built lazily: one verifying TLS (the default) and one that does not, used
 * only after {@code setSslVerification(false)}.
 */
public final class JdkHttpTransport implements HttpTransport {

    private volatile HttpClient verifying;
    private volatile HttpClient insecure;
    private volatile int clientConnectTimeout = -1;

    @Override
    public Response send(Request request) throws TransportException {
        HttpClient client = client(request.isSslVerify(), request.getConnectTimeoutSeconds());
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(request.getUrl()))
            .timeout(Duration.ofSeconds(request.getTimeoutSeconds()));
        for (Map.Entry<String, String> h : request.getHeaders().entrySet()) {
            b.header(h.getKey(), h.getValue());
        }
        HttpRequest.BodyPublisher publisher = request.getBody() == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(request.getBody(), StandardCharsets.UTF_8);
        b.method(request.getMethod(), publisher);

        try {
            HttpResponse<byte[]> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
            Map<String, String> headers = new LinkedHashMap<>();
            for (Map.Entry<String, List<String>> e : resp.headers().map().entrySet()) {
                headers.put(e.getKey().toLowerCase(java.util.Locale.ROOT), String.join(", ", e.getValue()));
            }
            return new Response(resp.statusCode(), new String(resp.body(), StandardCharsets.UTF_8), headers);
        } catch (HttpConnectTimeoutException e) {
            throw new TransportException(TransportException.TIMEOUT,
                "Connection timed out after " + request.getConnectTimeoutSeconds() * 1000 + " milliseconds", e);
        } catch (HttpTimeoutException e) {
            throw new TransportException(TransportException.TIMEOUT,
                "Operation timed out after " + request.getTimeoutSeconds() * 1000 + " milliseconds", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException(TransportException.RECV, "Request interrupted", e);
        } catch (IOException e) {
            throw classify(e, request.getUrl());
        } catch (IllegalArgumentException e) {
            throw new TransportException(3, "URL rejected: " + e.getMessage(), e);
        }
    }

    private static TransportException classify(IOException e, String url) {
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (IllegalArgumentException ex) {
            host = url;
        }
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException || t instanceof UnresolvedAddressException) {
                return new TransportException(TransportException.RESOLVE, "Could not resolve host: " + host, e);
            }
            if (t instanceof SSLHandshakeException) {
                String msg = String.valueOf(t.getMessage());
                int code = msg.contains("certificate") || msg.contains("PKIX") ? TransportException.SSL_CERT
                    : TransportException.SSL_CONNECT;
                return new TransportException(code, "SSL error: " + msg, e);
            }
            if (t instanceof SSLException) {
                return new TransportException(TransportException.SSL_CONNECT, "SSL error: " + t.getMessage(), e);
            }
            if (t instanceof ConnectException) {
                return new TransportException(TransportException.CONNECT,
                    "Failed to connect to " + host + ": " + (t.getMessage() == null ? "Connection refused" : t.getMessage()), e);
            }
        }
        return new TransportException(TransportException.RECV,
            "Failure when receiving data from the peer" + (e.getMessage() == null ? "" : ": " + e.getMessage()), e);
    }

    private HttpClient client(boolean verify, int connectTimeoutSeconds) {
        if (connectTimeoutSeconds != clientConnectTimeout) {
            synchronized (this) {
                verifying = null;
                insecure = null;
                clientConnectTimeout = connectTimeoutSeconds;
            }
        }
        if (verify) {
            HttpClient c = verifying;
            if (c == null) {
                synchronized (this) {
                    if (verifying == null) {
                        verifying = base(connectTimeoutSeconds).build();
                    }
                    c = verifying;
                }
            }
            return c;
        }
        HttpClient c = insecure;
        if (c == null) {
            synchronized (this) {
                if (insecure == null) {
                    insecure = base(connectTimeoutSeconds).sslContext(trustAllContext()).build();
                }
                c = insecure;
            }
        }
        return c;
    }

    private static HttpClient.Builder base(int connectTimeoutSeconds) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            .followRedirects(HttpClient.Redirect.NEVER);
    }

    /**
     * Accept any certificate and skip host name checks. An
     * {@link X509ExtendedTrustManager} is used on purpose: the JDK then leaves
     * endpoint identification to it, so the host name is not checked either.
     */
    private static SSLContext trustAllContext() {
        try {
            TrustManager tm = new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, new SecureRandom());
            return ctx;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create TLS context", e);
        }
    }
}
