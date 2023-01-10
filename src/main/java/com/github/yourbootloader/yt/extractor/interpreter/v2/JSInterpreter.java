package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.StatementResultDto;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;

import java.util.*;
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
    private final Map<?, ?> objects;
    private final Map<?, ?> functions;

    public JSInterpreter(String code) {
        this.code = code;
        this.objects = new HashMap<>();
        this.functions = new HashMap<>();
    }

    public List<String> separate(String expr, String delim, Integer maxSplit, List<String> skipDelims) {
        return new ExpressionSeparator().separate(expr, new DefaultSeparateArgs(delim, maxSplit, skipDelims));
    }

    public List<String> separateAtParen(String expr, String delim) {
        if (delim == null) {
            delim = _MATCHING_PARENS.get(expr.substring(0, 1));
        }
        List<String> separated = separate(expr, delim, null, null);
        if (separated.size() > 2) {
            throw new RuntimeException("No terminating paren " + delim + " in " + expr.substring(0, 100));
        }
        return Arrays.asList(
                separated.get(0).substring(1).replaceAll("^[ \t]+|[ \t]+$", ""),
                separated.get(1).replaceAll("^[ \t]+|[ \t]+$", "")
        );
    }

    public StatementResultDto interpretStatement(String stmt, Map<Object, Object> localVars, int allowRecursion) {
        if (allowRecursion < 0) {
            throw new RuntimeException("Recursion limit reached");
        }
        allowRecursion -= 1;

        boolean shouldReturn = false;
        LinkedList<String> subStatements = (LinkedList<String>) separate(stmt, ";", null, null); // TODO return emptyList
        String expr = stmt = subStatements.pop().strip();
//        for (String subStmt : subStatements) {
//            StatementResultDto result = this.interpretStatement(subStmt, localVars, allowRecursion);
//            if (result.isShouldReturn()) {
//                return result;
//            }
//        }

        Matcher m = match("(?<var>(?:var|const|let)\\s)|return(?:\\s+|(?=[\"'])|$)|(?<throw>throw\\s+)", stmt);
        if (m.find()) {
            expr = stmt.substring(m.group(0).length()).strip();
            if (m.group("throw") != null) {
                throw new RuntimeException("raise JS_Throw(self.interpret_expression(expr, local_vars, allow_recursion))");
            }
            shouldReturn = m.group("var") == null;
        }
        if (expr == null) {
            return new StatementResultDto(null, shouldReturn);
        }
        System.out.println();
        return null;
    }

    private Matcher match(@Language("RegExp") String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(text);
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
