package io.github.domainreseller.dna;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small JSON reader/writer used by the library (no third-party dependency).
 *
 * <p>Decoding follows PHP's {@code json_decode($s, true)}: objects become
 * {@link LinkedHashMap}, arrays {@link ArrayList}, integers {@link Integer} (or
 * {@link Long} when they do not fit), numbers with a fraction or exponent
 * {@link Double}, plus {@link String}, {@link Boolean} and {@code null}.
 * Invalid input yields {@code null}, like PHP.</p>
 *
 * <p>Encoding writes maps as objects and lists/arrays as arrays. It is public
 * so callers can print or log the results the library returns:
 * {@code System.out.println(Json.encodePretty(api.getDetails("example.com")))}.</p>
 */
public final class Json {

    private Json() {
    }

    /** Thrown internally on malformed input; {@link #decode} turns it into {@code null}. */
    static final class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        JsonException(String message) {
            super(message);
        }
    }

    // ------------------------------------------------------------------ decode

    /**
     * Decode a JSON document. Returns {@code null} for empty or invalid input
     * (and for the literal {@code null}), like PHP's {@code json_decode}.
     *
     * @param text JSON text
     * @return Map, List, String, Integer, Long, Double, Boolean or null
     */
    public static Object decode(String text) {
        if (text == null) {
            return null;
        }
        try {
            Parser p = new Parser(text);
            p.skipWs();
            Object value = p.readValue(0);
            p.skipWs();
            if (p.pos != text.length()) {
                return null;
            }
            return value;
        } catch (JsonException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Strict variant of {@link #decode}: throws {@link IllegalArgumentException}
     * on malformed input instead of returning {@code null}.
     *
     * @param text JSON text
     * @return decoded value
     */
    public static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("JSON text is null");
        }
        try {
            Parser p = new Parser(text);
            p.skipWs();
            Object value = p.readValue(0);
            p.skipWs();
            if (p.pos != text.length()) {
                throw new JsonException("Trailing characters at offset " + p.pos);
            }
            return value;
        } catch (JsonException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    private static final class Parser {
        private final String s;
        private int pos;

        Parser(String s) {
            this.s = s;
        }

        void skipWs() {
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        Object readValue(int depth) {
            if (depth > 512) {
                throw new JsonException("Maximum depth exceeded");
            }
            if (pos >= s.length()) {
                throw new JsonException("Unexpected end of input");
            }
            char c = s.charAt(pos);
            switch (c) {
                case '{':
                    return readObject(depth);
                case '[':
                    return readArray(depth);
                case '"':
                    return readString();
                case 't':
                    expect("true");
                    return Boolean.TRUE;
                case 'f':
                    expect("false");
                    return Boolean.FALSE;
                case 'n':
                    expect("null");
                    return null;
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return readNumber();
                    }
                    throw new JsonException("Unexpected character '" + c + "' at offset " + pos);
            }
        }

        private void expect(String word) {
            if (!s.startsWith(word, pos)) {
                throw new JsonException("Expected " + word + " at offset " + pos);
            }
            pos += word.length();
        }

        private Map<String, Object> readObject(int depth) {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // {
            skipWs();
            if (s.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWs();
                if (s.charAt(pos) != '"') {
                    throw new JsonException("Expected object key at offset " + pos);
                }
                String key = readString();
                skipWs();
                if (s.charAt(pos) != ':') {
                    throw new JsonException("Expected ':' at offset " + pos);
                }
                pos++;
                skipWs();
                Object value = readValue(depth + 1);
                map.put(key, value);
                skipWs();
                char c = s.charAt(pos++);
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw new JsonException("Expected ',' or '}' at offset " + (pos - 1));
                }
            }
        }

        private List<Object> readArray(int depth) {
            List<Object> list = new ArrayList<>();
            pos++; // [
            skipWs();
            if (s.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                skipWs();
                list.add(readValue(depth + 1));
                skipWs();
                char c = s.charAt(pos++);
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw new JsonException("Expected ',' or ']' at offset " + (pos - 1));
                }
            }
        }

        private String readString() {
            pos++; // opening quote
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    char e = s.charAt(pos++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 > s.length()) {
                                throw new JsonException("Bad unicode escape");
                            }
                            try {
                                sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                            } catch (NumberFormatException ex) {
                                throw new JsonException("Bad unicode escape");
                            }
                            pos += 4;
                            break;
                        default:
                            throw new JsonException("Bad escape \\" + e);
                    }
                } else if (c < 0x20) {
                    throw new JsonException("Control character in string");
                } else {
                    sb.append(c);
                }
            }
        }

        private Object readNumber() {
            int start = pos;
            boolean isFloat = false;
            if (s.charAt(pos) == '-') {
                pos++;
            }
            if (pos >= s.length() || !Character.isDigit(s.charAt(pos))) {
                throw new JsonException("Bad number at offset " + start);
            }
            if (s.charAt(pos) == '0') {
                pos++;
            } else {
                while (pos < s.length() && isDigit(s.charAt(pos))) {
                    pos++;
                }
            }
            if (pos < s.length() && s.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                int fracStart = pos;
                while (pos < s.length() && isDigit(s.charAt(pos))) {
                    pos++;
                }
                if (pos == fracStart) {
                    throw new JsonException("Bad number at offset " + start);
                }
            }
            if (pos < s.length() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < s.length() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) {
                    pos++;
                }
                int expStart = pos;
                while (pos < s.length() && isDigit(s.charAt(pos))) {
                    pos++;
                }
                if (pos == expStart) {
                    throw new JsonException("Bad number at offset " + start);
                }
            }
            String num = s.substring(start, pos);
            if (!isFloat) {
                try {
                    long l = Long.parseLong(num);
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                        return (int) l;
                    }
                    return l;
                } catch (NumberFormatException overflow) {
                    // PHP turns integers that overflow into floats.
                    return Double.parseDouble(num);
                }
            }
            return Double.parseDouble(num);
        }

        private static boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }

    // ------------------------------------------------------------------ encode

    /**
     * Encode a value as compact JSON (UTF-8 kept as is, slashes not escaped).
     *
     * @param value Map, Collection, array, String, Number, Boolean or null
     * @return JSON text
     */
    public static String encode(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value, false, false, 0);
        return sb.toString();
    }

    /**
     * Encode a value as indented JSON, handy for printing results.
     *
     * @param value value to encode
     * @return JSON text
     */
    public static String encodePretty(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value, false, true, 0);
        return sb.toString();
    }

    /**
     * Encode the way PHP's {@code json_encode($data)} does with default flags:
     * {@code "/"} as {@code "\/"} and non-ASCII characters as {@code \\uXXXX}.
     * Used for request bodies so the wire format matches php-dna.
     */
    static String encodePhp(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value, true, false, 0);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object v, boolean php, boolean pretty, int indent) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String || v instanceof Character) {
            writeString(sb, v.toString(), php);
        } else if (v instanceof Boolean) {
            sb.append(((Boolean) v) ? "true" : "false");
        } else if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                sb.append('0');
            } else if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                sb.append(BigDecimal.valueOf(d).setScale(1).toPlainString());
            } else {
                sb.append(Double.toString(d));
            }
        } else if (v instanceof BigDecimal) {
            sb.append(((BigDecimal) v).toPlainString());
        } else if (v instanceof Number) {
            sb.append(v.toString());
        } else if (v instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) v;
            if (m.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                newline(sb, pretty, indent + 1);
                writeString(sb, String.valueOf(e.getKey()), php);
                sb.append(pretty ? ": " : ":");
                write(sb, e.getValue(), php, pretty, indent + 1);
            }
            newline(sb, pretty, indent);
            sb.append('}');
        } else if (v instanceof Collection || v instanceof Object[]) {
            Collection<?> c = v instanceof Collection ? (Collection<?>) v : java.util.Arrays.asList((Object[]) v);
            if (c.isEmpty()) {
                sb.append("[]");
                return;
            }
            sb.append('[');
            boolean first = true;
            for (Object o : c) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                newline(sb, pretty, indent + 1);
                write(sb, o, php, pretty, indent + 1);
            }
            newline(sb, pretty, indent);
            sb.append(']');
        } else {
            writeString(sb, v.toString(), php);
        }
    }

    private static void newline(StringBuilder sb, boolean pretty, int indent) {
        if (!pretty) {
            return;
        }
        sb.append('\n');
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }

    private static void writeString(StringBuilder sb, String s, boolean php) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '/':
                    sb.append(php ? "\\/" : "/");
                    break;
                default:
                    if (c < 0x20 || (php && c > 0x7f)) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
