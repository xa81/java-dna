package io.github.domainreseller.dna;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PHP semantics, JSON codec and the no-network utilities. */
class UtilitiesTest {

    private final DomainNameApi api = new DomainNameApi("r", "k");

    @Test
    void isTrTLD() {
        assertTrue(api.isTrTLD("example.com.tr"));
        assertTrue(api.isTrTLD("EXAMPLE.TR"));
        assertFalse(api.isTrTLD("example.trade"));
        assertFalse(api.isTrTLD("tr"));
        assertFalse(api.isTrTLD(""));
    }

    @Test
    void validateContactFillsAndNormalises() {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("FirstName", "Ada 2nd!");
        c.put("LastName", "   ");
        c.put("Country", "germany");
        c.put("City", "Berlin");
        c.put("State", "");
        c.put("Phone", "0 (532) 374-33-88");
        c.put("PhoneCountryCode", "+90");
        c.put("Fax", "");
        c.put("FaxCountryCode", "49");
        Map<String, Object> v = api.validateContact(c);
        assertEquals("Ada nd", v.get("FirstName"));
        assertEquals("Ada nd", v.get("LastName"), "empty last name copies the first name");
        assertEquals("DE", v.get("Country"));
        assertEquals("Berlin", v.get("State"), "state falls back to the city");
        assertEquals("5323743388", v.get("Phone"));
        assertEquals("90", v.get("PhoneCountryCode"));
        assertEquals("", v.get("Fax"));
        assertEquals("", v.get("FaxCountryCode"), "no country code on an empty fax");
        assertEquals("Addres yok", v.get("AddressLine1"));
        assertEquals("34000", v.get("ZipCode"));
        assertEquals("Ada 2nd!", c.get("FirstName"), "input is not mutated");

        Map<String, Object> noState = api.validateContact(Collections.singletonMap("City", "Ankara"));
        assertEquals("Ankara", noState.get("State"), "missing state falls back to the city, not ISTANBUL");
    }

    @Test
    void validateContactKeepsUnicodeLetters() {
        Map<String, Object> v = api.validateContact(Collections.singletonMap("FirstName", "  Çağrı  Öztürk-O'Neil "));
        assertEquals("Çağrı Öztürk-O'Neil", v.get("FirstName"));
    }

    @Test
    void normalizePhone() {
        assertArrayEquals(new String[]{"5323743388", "90"}, DNARest.normalizePhone("+90 532 374 33 88", "90"));
        assertArrayEquals(new String[]{"5323743388", "90"}, DNARest.normalizePhone("905323743388", ""));
        assertArrayEquals(new String[]{"5323743388", "90"}, DNARest.normalizePhone("905323743388", "90"));
        assertArrayEquals(new String[]{"2025550100", "1"}, DNARest.normalizePhone("001 202 555 0100", "1"));
        assertArrayEquals(new String[]{"", "44"}, DNARest.normalizePhone("", "+44"));
        assertArrayEquals(new String[]{"5323743388", "90"}, DNARest.normalizePhone("0532 374 33 88", ""));
        // a "+" number with its own, different calling code keeps it
        assertArrayEquals(new String[]{"2025550100", "1"}, DNARest.normalizePhone("+1 (202) 555-0100", "90"));
        assertArrayEquals(new String[]{"2071234567", "44"}, DNARest.normalizePhone("+44 20 7123 4567", "90"));
        assertArrayEquals(new String[]{"501234567", "994"}, DNARest.normalizePhone("+994 50 123 45 67", ""));
        assertEquals("", DNARest.callingCodePrefix("0123"));
        assertEquals("7", DNARest.callingCodePrefix("79161234567"));
    }

    @Test
    void phpCasts() {
        assertEquals("601.64", Php.str(601.64));
        assertEquals("0", Php.str(0.0));
        assertEquals("100", Php.str(100.0));
        assertEquals("1.0E+25", Php.str(1e25));
        assertEquals("1.0E-5", Php.str(0.00001));
        assertEquals("0.3", Php.str(0.1 + 0.2));
        assertEquals("1", Php.str(true));
        assertEquals("", Php.str(false));
        assertEquals("", Php.str(null));
        assertEquals(12, Php.toInt("12abc"));
        assertEquals(1000, Php.toInt("1e3"));
        assertEquals(0, Php.toInt("abc"));
        assertEquals(3, Php.toInt(3.9));
        assertEquals(11.99, Php.toFloat("11.99"), 0.0);
        assertTrue(Php.isNumeric("12"));
        assertTrue(Php.isNumeric(" 1.5e3 "));
        assertFalse(Php.isNumeric("12a"));
        assertFalse(Php.isNumeric(""));
        assertTrue(Php.empty("0"));
        assertTrue(Php.empty(Collections.emptyList()));
        assertFalse(Php.empty("0.0"));
    }

    @Test
    void numberFormat() {
        assertEquals("11.9900", Php.numberFormat(11.99, 4));
        assertEquals("1.01", Php.numberFormat(1.005, 2));
        assertEquals("0.00", Php.numberFormat(-0.001, 2));
        assertEquals("1250.50", Php.numberFormat(1250.5, 2));
    }

    @Test
    void httpBuildQuery() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", "x y");
        m.put("b", true);
        m.put("c", false);
        m.put("d", null);
        m.put("e", Arrays.asList(1, 2));
        m.put("f", Collections.singletonMap("k", "v~*"));
        assertEquals("a=x+y&b=1&c=0&e%5B0%5D=1&e%5B1%5D=2&f%5Bk%5D=v%7E%2A", Php.httpBuildQuery(m));
    }

    @Test
    void strtotime() {
        assertEquals("2027-03-15T10:20:30", Php.formatDate(Php.strtotime("2027-03-15T10:20:30")));
        assertEquals("2027-03-15T10:20:30", Php.formatDate(Php.strtotime("2027-03-15 10:20:30.1234567")));
        assertEquals("2027-03-15T07:20:30", Php.formatDate(Php.strtotime("2027-03-15T10:20:30+03:00")));
        assertEquals("2027-03-15T10:20:30", Php.formatDate(Php.strtotime("2027-03-15T10:20:30Z")));
        assertEquals("2027-03-15T00:00:00", Php.formatDate(Php.strtotime("2027-03-15")));
        assertEquals("0001-01-01T00:00:00", Php.formatDate(Php.strtotime("0001-01-01T00:00:00")));
        assertEquals("2027-03-02T00:00:00", Php.formatDate(Php.strtotime("2027-02-30")), "overflowing days roll over like PHP");
        assertNull(Php.strtotime("not-a-date"));
        assertNull(Php.strtotime("2027-13-01"));
        assertEquals(Long.valueOf(0L), Php.strtotime("@0"));
    }

    @Test
    void ipv4Validation() {
        assertTrue(Php.isIpv4("192.0.2.10"));
        assertFalse(Php.isIpv4("192.0.2.256"));
        assertFalse(Php.isIpv4("01.2.3.4"));
        assertFalse(Php.isIpv4("2001:db8::1"));
    }

    @Test
    void jsonDecodeLikePhp() {
        Object v = Json.decode("{\"i\":1,\"l\":12345678901,\"f\":1.0,\"e\":1e2,\"s\":\"\\u00fc\\/\",\"n\":null,\"a\":[true,false]}");
        assertNotNull(v);
        Map<?, ?> m = (Map<?, ?>) v;
        assertEquals(1, m.get("i"));
        assertEquals(12345678901L, m.get("l"));
        assertEquals(1.0, m.get("f"));
        assertEquals(100.0, m.get("e"));
        assertEquals("ü/", m.get("s"));
        assertTrue(m.containsKey("n"));
        assertEquals(Arrays.asList(true, false), m.get("a"));
        assertNull(Json.decode(""));
        assertNull(Json.decode("null"));
        assertNull(Json.decode("<html>"));
        assertNull(Json.decode("{\"a\":1} x"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse("{"));
    }

    @Test
    void jsonEncode() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("s", "a/ü\"");
        m.put("f", 2.0);
        m.put("e", Collections.emptyMap());
        m.put("l", Collections.emptyList());
        assertEquals("{\"s\":\"a/ü\\\"\",\"f\":2.0,\"e\":{},\"l\":[]}", Json.encode(m));
        assertEquals("{\"s\":\"a\\/\\u00fc\\\"\",\"f\":2.0,\"e\":{},\"l\":[]}", Json.encodePhp(m));
        assertTrue(Json.encodePretty(m).contains("\n    \"s\": "));
    }

    @Test
    void comparatorCatchesDifferences() {
        List<Object> empty = Collections.emptyList();
        assertNull(JsonCompare.diff(empty, Collections.emptyMap(), "", Collections.emptySet()));
        assertNull(JsonCompare.diff(1, 1.0, "", Collections.emptySet()));
        assertNotNull(JsonCompare.diff("11.99", "11.9900", "", Collections.emptySet()));
        assertNotNull(JsonCompare.diff("true", true, "", Collections.emptySet()));
        assertNotNull(JsonCompare.diff(Php.map("a", 1), Php.map("a", 1, "b", 2), "", Collections.emptySet()));
        assertNull(JsonCompare.diff(Php.map("d", 5), Php.map("d", 300), "", Collections.singleton("d")));
        assertNotNull(JsonCompare.diff(Php.map("d", 5), Php.map("d", -1), "", Collections.singleton("d")));
    }
}
