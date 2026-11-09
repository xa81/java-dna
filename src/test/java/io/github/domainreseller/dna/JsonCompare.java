package io.github.domainreseller.dna;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comparator implementing the fixture comparison rules: an empty list equals an
 * empty object, numbers compare by value, key order does not matter, and paths in
 * {@code ignore} only need to hold a non-negative integer.
 */
final class JsonCompare {

    private JsonCompare() {
    }

    /** @return null when equal, otherwise a description of the first difference */
    static String diff(Object expected, Object actual, String path, Set<String> ignore) {
        if (ignore.contains(path)) {
            if (!(actual instanceof Number) || actual instanceof Double
                || ((Number) actual).longValue() < 0) {
                return path + ": expected a non-negative integer, got " + Json.encode(actual);
            }
            return null;
        }
        if (isEmptyContainer(expected) && isEmptyContainer(actual)) {
            return null;
        }
        if (expected instanceof Map) {
            if (!(actual instanceof Map)) {
                return path + ": expected object " + Json.encode(expected) + ", got " + Json.encode(actual);
            }
            Map<?, ?> e = (Map<?, ?>) expected;
            Map<?, ?> a = (Map<?, ?>) actual;
            if (!e.keySet().equals(a.keySet())) {
                return path + ": keys differ, expected " + e.keySet() + ", got " + a.keySet();
            }
            for (Object k : e.keySet()) {
                String d = diff(e.get(k), a.get(k), path.isEmpty() ? String.valueOf(k) : path + "." + k, ignore);
                if (d != null) {
                    return d;
                }
            }
            return null;
        }
        if (expected instanceof List) {
            if (!(actual instanceof List)) {
                return path + ": expected list " + Json.encode(expected) + ", got " + Json.encode(actual);
            }
            List<?> e = (List<?>) expected;
            List<?> a = (List<?>) actual;
            if (e.size() != a.size()) {
                return path + ": expected " + e.size() + " items, got " + a.size() + ": " + Json.encode(actual);
            }
            for (int i = 0; i < e.size(); i++) {
                String d = diff(e.get(i), a.get(i), path.isEmpty() ? String.valueOf(i) : path + "." + i, ignore);
                if (d != null) {
                    return d;
                }
            }
            return null;
        }
        if (expected instanceof Number && actual instanceof Number) {
            BigDecimal x = new BigDecimal(expected.toString());
            BigDecimal y = new BigDecimal(actual.toString());
            return x.compareTo(y) == 0 ? null : path + ": expected " + expected + ", got " + actual;
        }
        if (expected == null ? actual == null : expected.equals(actual)) {
            return null;
        }
        return path + ": expected " + Json.encode(expected) + " (" + type(expected) + "), got "
            + Json.encode(actual) + " (" + type(actual) + ")";
    }

    private static boolean isEmptyContainer(Object v) {
        return (v instanceof Map && ((Map<?, ?>) v).isEmpty()) || (v instanceof Collection && ((Collection<?>) v).isEmpty());
    }

    private static String type(Object v) {
        return v == null ? "null" : v.getClass().getSimpleName();
    }
}
