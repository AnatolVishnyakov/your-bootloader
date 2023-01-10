package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return new ExpressionSeparator().separate(expr, new DefaultSeparateArgs(delim, maxSplit, skipDelims));
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
