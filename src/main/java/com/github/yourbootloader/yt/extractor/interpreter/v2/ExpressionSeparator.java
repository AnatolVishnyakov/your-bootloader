package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.*;

import static com.github.yourbootloader.yt.extractor.interpreter.v2.JSInterpreter.*;

@Slf4j
public class ExpressionSeparator {

    public List<String> separate(String expr, DefaultSeparateArgs args) {
        if (expr == null || expr.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new LinkedList<>();

        String delim = args.getDelimiter();
        List<String> skipDelims = args.getSkipDelimiters();
        Integer maxSplit = args.getMaxSplit();

        Map<String, Integer> counters = StreamEx.of(_MATCHING_PARENS.values()).toMap(v -> 0);
        int start = 0, splits = 0, pos = 0, delimLen = delim.length() - 1;

        String inQuote = null;
        boolean escaping = false;
        int skipping = 0;

        boolean afterOp = true;
        boolean inRegexCharGroup = false;

        for (int idx = 0; idx < expr.length(); idx++) {
            String _char = Character.toString(expr.charAt(idx));

            if (notInQuote(inQuote)) {
                if (charInMatchingParens(_char)) {
                    String key = _MATCHING_PARENS.get(_char);
                    Integer value = counters.get(key);
                    counters.put(key, value + 1);
                } else if (charInCounters(_char, counters)) {
                    counters.put(_char, counters.get(_char) - 1);
                }
            }

            if (notEscaping(escaping)) {
                if (charInQuotes(_char) && Arrays.asList(_char, null).contains(inQuote)) {
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

            if (!_char.equals(delim) || counters.values().stream().anyMatch(v -> v != 0) || inQuote != null) {
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
            if (maxSplit != null && splits >= maxSplit) {
                break;
            }
        }

        result.add(expr.substring(start));
        return result;
    }

    private boolean charInQuotes(String _char) {
        return _QUOTES.contains(_char);
    }

    private boolean notEscaping(boolean escaping) {
        return !escaping;
    }

    private static boolean charInCounters(String symbol, Map<String, Integer> counters) {
        return counters.containsKey(symbol);
    }

    private boolean charInMatchingParens(String symbol) {
        return _MATCHING_PARENS.containsKey(symbol);
    }

    private boolean notInQuote(String inQuote) {
        return inQuote == null;
    }
}
