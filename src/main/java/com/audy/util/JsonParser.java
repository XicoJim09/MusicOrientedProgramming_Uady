package com.audy.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight recursive-descent JSON parser for AUDY.
 * Converts JSON strings into native Java objects (Map, List, String, Number, Boolean, null).
 * No external dependencies required — demonstrates encapsulation and single-responsibility (RNF-05).
 */
public class JsonParser {
    private final String input;
    private int pos;

    private JsonParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    // --- Public API ---

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        return new JsonParser(json.trim()).readValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        return (result instanceof Map) ? (Map<String, Object>) result : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object result = parse(json);
        return (result instanceof List) ? (List<Object>) result : new ArrayList<>();
    }

    // --- Convenience accessors ---

    public static String getString(Map<String, Object> map, String key) {
        if (map == null) return "";
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    public static int getInt(Map<String, Object> map, String key, int defaultVal) {
        if (map == null) return defaultVal;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultVal;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getArray(Map<String, Object> map, String key) {
        if (map == null) return new ArrayList<>();
        Object val = map.get(key);
        return (val instanceof List) ? (List<Object>) val : new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        if (map == null) return new LinkedHashMap<>();
        Object val = map.get(key);
        return (val instanceof Map) ? (Map<String, Object>) val : new LinkedHashMap<>();
    }

    // --- Recursive-descent parser ---

    private Object readValue() {
        skipWhitespace();
        if (pos >= input.length()) return null;
        char c = input.charAt(pos);
        switch (c) {
            case '{': return readObject();
            case '[': return readArray();
            case '"': return readString();
            case 't': case 'f': return readBoolean();
            case 'n': return readNull();
            default:  return readNumber();
        }
    }

    private Map<String, Object> readObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            Object value = readValue();
            map.put(key, value);
            skipWhitespace();
            if (peek() == ',') { pos++; } else break;
        }
        skipWhitespace();
        expect('}');
        return map;
    }

    private List<Object> readArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            list.add(readValue());
            skipWhitespace();
            if (peek() == ',') { pos++; } else break;
        }
        skipWhitespace();
        expect(']');
        return list;
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '\\' && pos < input.length()) {
                char esc = input.charAt(pos++);
                switch (esc) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 <= input.length()) {
                            sb.append((char) Integer.parseInt(input.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                        break;
                    default: sb.append(esc); break;
                }
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Number readNumber() {
        int start = pos;
        if (pos < input.length() && input.charAt(pos) == '-') pos++;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        boolean isFloat = false;
        if (pos < input.length() && input.charAt(pos) == '.') {
            isFloat = true; pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            isFloat = true; pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        }
        String numStr = input.substring(start, pos);
        if (isFloat) return Double.parseDouble(numStr);
        try { return Integer.parseInt(numStr); }
        catch (NumberFormatException e) { return Long.parseLong(numStr); }
    }

    private Boolean readBoolean() {
        if (input.startsWith("true", pos))  { pos += 4; return true;  }
        if (input.startsWith("false", pos)) { pos += 5; return false; }
        throw new RuntimeException("Invalid boolean at position " + pos);
    }

    private Object readNull() {
        if (input.startsWith("null", pos)) { pos += 4; return null; }
        throw new RuntimeException("Invalid null at position " + pos);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) pos++;
    }

    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    private void expect(char expected) {
        skipWhitespace();
        if (pos >= input.length() || input.charAt(pos) != expected) {
            throw new RuntimeException("Expected '" + expected + "' at position " + pos
                    + " but found '" + (pos < input.length() ? input.charAt(pos) : "EOF") + "'");
        }
        pos++;
    }
}
