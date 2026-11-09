package io.github.domainreseller.dna;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Test transport: serves queued responses in order and records every request. */
final class FakeTransport implements HttpTransport {

    final Deque<Object> queue = new ArrayDeque<>();
    final List<Request> requests = new ArrayList<>();

    FakeTransport respond(int status, String body) {
        queue.add(new Response(status, body));
        return this;
    }

    FakeTransport fail(int curlCode, String message) {
        queue.add(new TransportException(curlCode, message));
        return this;
    }

    @Override
    public Response send(Request request) throws TransportException {
        requests.add(request);
        Object next = queue.poll();
        if (next == null) {
            throw new AssertionError("Fake transport ran out of responses at request #" + requests.size()
                + ": " + request.getMethod() + " " + request.getUrl());
        }
        if (next instanceof TransportException) {
            throw (TransportException) next;
        }
        return (Response) next;
    }
}
