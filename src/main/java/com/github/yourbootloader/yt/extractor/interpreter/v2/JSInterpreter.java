package com.github.yourbootloader.yt.extractor.interpreter.v2;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Slf4j
public class JSInterpreter {

    public static final Map<String, String> _MATCHING_PARENS = new HashMap<String, String>() {{
        put("(", ")");
        put("{", "}");
        put("[", "]");
    }};
    public static final List<String> _QUOTES = Arrays.asList("'", "\"", "/");
    public static final List<String> OP_CHARS = Arrays.asList(
            ",", "&", "/", ">", "|", ";", "?", "<", "%", "*", "!", "-", "+", "^", "="
    );

    private int namedObjectCounter = 0;

    private final String code;

    public JSInterpreter(String code) {
        this.code = code;
    }

    public List<String> separate(String expr, String delim, Integer maxSplit, List<String> skipDelims) {
        if (expr == null || expr.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        if (delim == null) {
            delim = ",";
        }

        Map<String, Integer> counters = StreamEx.of(_MATCHING_PARENS.values()).toMap(v -> 0);
        int start = 0, splits = 0, pos = 0, delimLen = delim.length() - 1;

        String inQuote = null;
        boolean escaping = false;
        int skipping = 0;

        boolean afterOp = true;
        boolean inRegexCharGroup = false;

        for (int idx = 0; idx < expr.length(); idx++) {
            String _char = Character.toString(expr.charAt(idx));
            if (isNull(inQuote)) {
                if (_MATCHING_PARENS.containsKey(_char)) {
                    String key = _MATCHING_PARENS.get(_char);
                    Integer value = counters.get(key);
                    counters.put(key, value + 1);
                } else if (counters.containsKey(_char)) {
                    counters.put(_char, counters.get(_char) - 1);
                }
            }
            if (Boolean.FALSE.equals(escaping)) {
                if (_QUOTES.contains(_char) && Arrays.asList(_char, null).contains(inQuote)) {
                    if (inQuote != null || afterOp || !_char.equals("/")) {
                        inQuote = inQuote != null && !inRegexCharGroup
                                ? null : _char;
                    }
                } else if (Objects.equals(inQuote, "/") && "[]".contains(_char)) {
                    inRegexCharGroup = Objects.equals(_char, "[");
                }
            }

            escaping = !escaping && inQuote != null && _char.equals("\\");
            afterOp = inQuote == null && (OP_CHARS.contains(_char) || _char.equals("[") || (_char.equals(" ") && afterOp));

            if (_char.charAt(0) != delim.charAt(pos) || !counters.values().isEmpty() || inQuote != null) {
                pos = skipping = 0;
                continue;
            } else if (skipping > 0) {
                skipping -= 1;
                continue;
            } else if (pos == 0 && skipDelims != null) {
                String here = expr.substring(idx);
                for (String s : skipDelims) {
                    if (here.startsWith(s) && !s.isEmpty()) {
                        skipping = s.length() - 1;
                        break;
                    }
                }
                if (skipping > 0) {
                    continue;
                }
            }
            if (pos < delimLen) {
                pos += 1;
                continue;
            }
            result.add(expr.substring(start, idx - delimLen));
            start = idx + 1;
            pos = 0;
            splits += 1;
            if (maxSplit == 1 && splits >= maxSplit) {
                break;
            }
        }

        result.add(expr.substring(start));
        return result;
    }

    public void interpretStatement(String stmt, Map<Object, Object> localVars, int allowRecursion) {
        if (allowRecursion < 0) {
            throw new RuntimeException("Recursion limit reached");
        }
        allowRecursion -= 1;


    }

    public String extractFunctionFromCode(List<String> argNames) {
        Map<String, Function<?, ?>> localVars = new HashMap<>();
        while (true) {
            Pattern pattern = Pattern.compile("function\\((?<args>[^)]*)\\)\\s*\\{");
            Matcher matcher = pattern.matcher(code);
            if (!matcher.find()) {
                break;
            }
            // TODO
            System.out.println();
        }
        throw new RuntimeException();
    }
}
