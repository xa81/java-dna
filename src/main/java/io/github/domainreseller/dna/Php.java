package io.github.domainreseller.dna;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PHP semantics the port relies on: the {@code ??} operator, {@code empty()},
 * scalar casts, {@code number_format}, {@code http_build_query}, {@code strtotime}
 * and friends. Kept in one place so every method of the port applies them the
 * same way php-dna does.
 */
final class Php {

    private Php() {
    }

    // ------------------------------------------------------------ array access

    /**
     * Nested lookup with {@code ??} semantics: returns {@code null} when any step
     * is missing, {@code null}, or not an array (map or list).
     */
    static Object get(Object base, Object... path) {
        Object cur = base;
        for (Object key : path) {
            if (cur == null) {
                return null;
            }
            if (cur instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) cur;
                Object next = m.get(key instanceof Integer ? String.valueOf(key) : key);
                if (next == null && key instanceof Integer) {
                    next = m.get(key);
                }
                cur = next;
            } else if (cur instanceof List) {
                List<?> l = (List<?>) cur;
                int idx;
                if (key instanceof Integer) {
                    idx = (Integer) key;
                } else if (key instanceof String && ((String) key).matches("0|[1-9][0-9]{0,8}")) {
                    idx = Integer.parseInt((String) key);
                } else {
                    return null;
                }
                cur = idx >= 0 && idx < l.size() ? l.get(idx) : null;
            } else {
                return null;
            }
        }
        return cur;
    }

    /** First non-null value, like a chain of {@code ??}. */
    static Object coalesce(Object... values) {
        for (Object v : values) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    static boolean isArray(Object v) {
        return v instanceof Map || v instanceof List;
    }

    /** Iterate the values of a PHP array (map values or list items). */
    static Collection<?> values(Object v) {
        if (v instanceof Map) {
            return ((Map<?, ?>) v).values();
        }
        if (v instanceof Collection) {
            return (Collection<?>) v;
        }
        return new ArrayList<>();
    }

    /** {@code array_map} that keeps the container shape (list stays list, map keeps keys). */
    static Object mapValues(Object v, Function<Object, Object> fn) {
        if (v instanceof Map) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                out.put(String.valueOf(e.getKey()), fn.apply(e.getValue()));
            }
            return out;
        }
        List<Object> out = new ArrayList<>();
        for (Object o : values(v)) {
            out.add(fn.apply(o));
        }
        return out;
    }

    /** {@code (array) $v}. */
    static Object toArray(Object v) {
        if (v == null) {
            return new ArrayList<>();
        }
        if (isArray(v)) {
            return v;
        }
        List<Object> l = new ArrayList<>();
        l.add(v);
        return l;
    }

    // ------------------------------------------------------------ truthiness

    /** PHP {@code empty()}. */
    static boolean empty(Object v) {
        if (v == null) {
            return true;
        }
        if (v instanceof Boolean) {
            return !((Boolean) v);
        }
        if (v instanceof Double || v instanceof Float) {
            return ((Number) v).doubleValue() == 0.0;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue() == 0L;
        }
        if (v instanceof String) {
            return ((String) v).isEmpty() || "0".equals(v);
        }
        if (v instanceof Map) {
            return ((Map<?, ?>) v).isEmpty();
        }
        if (v instanceof Collection) {
            return ((Collection<?>) v).isEmpty();
        }
        return false;
    }

    static boolean truthy(Object v) {
        return !empty(v);
    }

    // ------------------------------------------------------------ casts

    /** PHP {@code (string) $v}. */
    static String str(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof String) {
            return (String) v;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? "1" : "";
        }
        if (v instanceof Double || v instanceof Float) {
            return floatToString(((Number) v).doubleValue());
        }
        if (v instanceof Number) {
            return v.toString();
        }
        if (v instanceof Map || v instanceof Collection) {
            return "Array";
        }
        return v.toString();
    }

    /** PHP float to string conversion (precision 14, %G style). */
    static String floatToString(double d) {
        if (Double.isNaN(d)) {
            return "NAN";
        }
        if (Double.isInfinite(d)) {
            return d > 0 ? "INF" : "-INF";
        }
        if (d == 0.0) {
            return (1 / d < 0) ? "-0" : "0";
        }
        BigDecimal bd = new BigDecimal(d).round(new MathContext(14, RoundingMode.HALF_EVEN)).stripTrailingZeros();
        int exp = bd.precision() - bd.scale() - 1;
        if (exp < -4 || exp >= 14) {
            BigDecimal mantissa = bd.movePointLeft(exp).stripTrailingZeros();
            String m = mantissa.toPlainString();
            if (!m.contains(".")) {
                m = m + ".0";
            }
            return m + "E" + (exp < 0 ? "-" : "+") + Math.abs(exp);
        }
        return bd.toPlainString();
    }

    private static final Pattern NUMERIC_PREFIX =
        Pattern.compile("^[ \\t\\n\\r\\x0B\\f]*([+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?)");
    private static final Pattern NUMERIC_FULL =
        Pattern.compile("^[ \\t\\n\\r\\x0B\\f]*[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?[ \\t\\n\\r\\x0B\\f]*$");

    /** PHP {@code (int) $v}. */
    static long toInt(Object v) {
        if (v == null) {
            return 0;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1 : 0;
        }
        if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                return 0;
            }
            return (long) d;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        if (v instanceof String) {
            Matcher m = NUMERIC_PREFIX.matcher((String) v);
            if (!m.find()) {
                return 0;
            }
            String n = m.group(1);
            if (n.contains(".") || n.contains("e") || n.contains("E")) {
                return toInt(Double.parseDouble(n));
            }
            try {
                return Long.parseLong(n.startsWith("+") ? n.substring(1) : n);
            } catch (NumberFormatException e) {
                return n.startsWith("-") ? Long.MIN_VALUE : Long.MAX_VALUE;
            }
        }
        if (v instanceof Map || v instanceof Collection) {
            return empty(v) ? 0 : 1;
        }
        return 0;
    }

    /** PHP {@code (float) $v}. */
    static double toFloat(Object v) {
        if (v == null) {
            return 0.0;
        }
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1.0 : 0.0;
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v instanceof String) {
            Matcher m = NUMERIC_PREFIX.matcher((String) v);
            if (!m.find()) {
                return 0.0;
            }
            return Double.parseDouble(m.group(1));
        }
        if (v instanceof Map || v instanceof Collection) {
            return empty(v) ? 0.0 : 1.0;
        }
        return 0.0;
    }

    /** PHP {@code (bool) $v}. */
    static boolean toBool(Object v) {
        return truthy(v);
    }

    /** An int result in the narrowest boxed type (Integer when it fits, else Long). */
    static Number intValue(long v) {
        if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
            return (int) v;
        }
        return v;
    }

    /** PHP 8 {@code is_numeric()}. */
    static boolean isNumeric(Object v) {
        if (v instanceof Boolean) {
            return false;
        }
        if (v instanceof Number) {
            return true;
        }
        if (v instanceof String) {
            return NUMERIC_FULL.matcher((String) v).matches();
        }
        return false;
    }

    // ------------------------------------------------------------ strings

    private static final String TRIM_CHARS = " \t\n\r\0\u000B";

    static String trim(Object v) {
        return trim(str(v), TRIM_CHARS);
    }

    static String trim(String s, String chars) {
        return rtrim(ltrim(s, chars), chars);
    }

    static String ltrim(String s, String chars) {
        int i = 0;
        while (i < s.length() && chars.indexOf(s.charAt(i)) >= 0) {
            i++;
        }
        return s.substring(i);
    }

    static String rtrim(String s, String chars) {
        int i = s.length();
        while (i > 0 && chars.indexOf(s.charAt(i - 1)) >= 0) {
            i--;
        }
        return s.substring(0, i);
    }

    /** ASCII-only lower case, like PHP 8 {@code strtolower}. */
    static String strtolower(Object v) {
        String s = str(v);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c >= 'A' && c <= 'Z' ? (char) (c + 32) : c);
        }
        return sb.toString();
    }

    /** ASCII-only upper case, like PHP 8 {@code strtoupper}. */
    static String strtoupper(Object v) {
        String s = str(v);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c >= 'a' && c <= 'z' ? (char) (c - 32) : c);
        }
        return sb.toString();
    }

    static String ucfirst(Object v) {
        String s = str(v);
        if (s.isEmpty()) {
            return s;
        }
        char c = s.charAt(0);
        if (c >= 'a' && c <= 'z') {
            return (char) (c - 32) + s.substring(1);
        }
        return s;
    }

    /** PHP {@code strcasecmp(a, b) === 0} (ASCII case folding). */
    static boolean strcaseEquals(Object a, Object b) {
        return strtolower(a).equals(strtolower(b));
    }

    /** PHP {@code strlen} (bytes, UTF-8). */
    static int strlen(Object v) {
        return str(v).getBytes(StandardCharsets.UTF_8).length;
    }

    /** {@code number_format($v, $decimals, '.', '')}. */
    static String numberFormat(double v, int decimals) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            v = 0.0;
        }
        BigDecimal bd = new BigDecimal(Double.toString(v)).setScale(decimals, RoundingMode.HALF_UP);
        if (bd.signum() == 0) {
            bd = bd.abs();
        }
        return bd.toPlainString();
    }

    // ------------------------------------------------------------ http_build_query

    /** {@code http_build_query($data)} with PHP's defaults (RFC 1738, {@code &}). */
    static String httpBuildQuery(Map<?, ?> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> e : data.entrySet()) {
            buildQueryPart(sb, urlencode(String.valueOf(e.getKey())), e.getValue());
        }
        return sb.toString();
    }

    private static void buildQueryPart(StringBuilder sb, String encodedKey, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Map || value instanceof List) {
            if (value instanceof Map) {
                for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                    buildQueryPart(sb, encodedKey + "%5B" + urlencode(String.valueOf(e.getKey())) + "%5D", e.getValue());
                }
            } else {
                List<?> l = (List<?>) value;
                for (int i = 0; i < l.size(); i++) {
                    buildQueryPart(sb, encodedKey + "%5B" + i + "%5D", l.get(i));
                }
            }
            return;
        }
        String s;
        if (value instanceof Boolean) {
            s = ((Boolean) value) ? "1" : "0";
        } else {
            s = str(value);
        }
        if (sb.length() > 0) {
            sb.append('&');
        }
        sb.append(encodedKey).append('=').append(urlencode(s));
    }

    /** PHP {@code urlencode}: alphanumerics and {@code -_.} kept, space as {@code +}. */
    static String urlencode(String s) {
        StringBuilder sb = new StringBuilder();
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xff;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.') {
                sb.append((char) c);
            } else if (c == ' ') {
                sb.append('+');
            } else {
                sb.append('%').append(String.format("%02X", c));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ dates

    private static final Pattern ISO_DATE = Pattern.compile(
        "^[ \\t\\n\\r]*(-?\\d{4,})-(\\d{1,2})-(\\d{1,2})"
            + "(?:(?:[Tt]|[ \\t]+)(\\d{1,2}):(\\d{2})(?::(\\d{2})(?:[.,](\\d+))?)?)?"
            + "[ \\t]*(Z|z|UTC|GMT|[+-]\\d{2}(?::?\\d{2})?)?[ \\t\\n\\r]*$");
    private static final Pattern AT_TIMESTAMP = Pattern.compile("^@(-?\\d+)$");

    /**
     * {@code strtotime()} for the date shapes the gateway produces (ISO 8601
     * with optional time, fraction and zone, or {@code @timestamp}). Times
     * without a zone are read as UTC. Returns {@code null} where PHP returns
     * {@code false}.
     */
    static Long strtotime(String value) {
        if (value == null) {
            return null;
        }
        Matcher at = AT_TIMESTAMP.matcher(value.trim());
        if (at.matches()) {
            try {
                return Long.parseLong(at.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        Matcher m = ISO_DATE.matcher(value);
        if (!m.matches()) {
            return null;
        }
        try {
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int day = Integer.parseInt(m.group(3));
            int hour = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            int minute = m.group(5) != null ? Integer.parseInt(m.group(5)) : 0;
            int second = m.group(6) != null ? Integer.parseInt(m.group(6)) : 0;
            if (month < 1 || month > 12 || day > 31 || hour > 24 || minute > 59 || second > 60) {
                return null;
            }
            LocalDateTime t = LocalDateTime.of(year, month, 1, 0, 0)
                .plusDays(day - 1L)
                .plusHours(hour)
                .plusMinutes(minute)
                .plusSeconds(second);
            long epoch = t.toEpochSecond(ZoneOffset.UTC);
            String zone = m.group(8);
            if (zone != null && (zone.startsWith("+") || zone.startsWith("-"))) {
                String digits = zone.substring(1).replace(":", "");
                int oh = Integer.parseInt(digits.substring(0, 2));
                int om = digits.length() >= 4 ? Integer.parseInt(digits.substring(2, 4)) : 0;
                int offset = oh * 3600 + om * 60;
                epoch -= zone.startsWith("+") ? offset : -offset;
            }
            return epoch;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final DateTimeFormatter PHP_DATE =
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss", Locale.ROOT);

    /** {@code date('Y-m-d\TH:i:s', $ts)} in UTC. */
    static String formatDate(long epochSeconds) {
        return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC).format(PHP_DATE);
    }

    // ------------------------------------------------------------ filters

    /** {@code filter_var($v, FILTER_VALIDATE_IP, FILTER_FLAG_IPV4) !== false}. */
    static boolean isIpv4(Object v) {
        String s = str(v);
        String[] parts = s.split("\\.", -1);
        if (parts.length != 4) {
            return false;
        }
        for (String p : parts) {
            if (p.isEmpty() || p.length() > 3) {
                return false;
            }
            for (int i = 0; i < p.length(); i++) {
                if (p.charAt(i) < '0' || p.charAt(i) > '9') {
                    return false;
                }
            }
            if (p.length() > 1 && p.charAt(0) == '0') {
                return false;
            }
            if (Integer.parseInt(p) > 255) {
                return false;
            }
        }
        return true;
    }

    /** {@code filter_var($v, FILTER_VALIDATE_BOOLEAN)}: "1", "true", "on", "yes" are true. */
    static boolean filterBool(String v) {
        String s = v == null ? "" : v.trim().toLowerCase(Locale.ROOT);
        return s.equals("1") || s.equals("true") || s.equals("on") || s.equals("yes");
    }

    // ------------------------------------------------------------ builders

    /** Ordered map literal: {@code map("a", 1, "b", 2)}. */
    static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    /** Mutable list literal. */
    static List<Object> list(Object... items) {
        List<Object> l = new ArrayList<>();
        for (Object o : items) {
            l.add(o);
        }
        return l;
    }
}
