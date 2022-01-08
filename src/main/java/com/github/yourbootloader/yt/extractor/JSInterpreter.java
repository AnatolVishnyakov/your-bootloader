package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import com.github.yourbootloader.yt.extractor.dto.Pair;
import org.apache.tomcat.util.security.Escape;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class JSInterpreter {

    private static final Map<String, BiFunction<String, String, String>> OPERATORS = new HashMap<String, BiFunction<String, String, String>>() {{
        put("|", (a, b) -> new BigInteger(a).or(new BigInteger(b)).toString());
        put("^", (a, b) -> new BigInteger(a).xor(new BigInteger(b)).toString());
        put("&", (a, b) -> new BigInteger(a).and(new BigInteger(b)).toString());
        put(">>", (a, b) -> new BigInteger(a).shiftRight(new BigInteger(b).intValue()).toString());
        put("<<", (a, b) -> new BigInteger(a).shiftLeft(new BigInteger(b).intValue()).toString());
        put("-", (a, b) -> new BigInteger(a).subtract(new BigInteger(b)).toString());
        put("+", (a, b) -> new BigInteger(a).add(new BigInteger(b)).toString());
        put("%", (a, b) -> new BigInteger(a).mod(new BigInteger(b)).toString());
        put("/", (a, b) -> new BigInteger(a).divide(new BigInteger(b)).toString());
        put("\\*", (a, b) -> new BigInteger(a).multiply(new BigInteger(b)).toString());
    }};
    private static final Map<String, BiFunction<String, String, String>> ASSIGN_OPERATORS = new HashMap<>();

    static {
        OPERATORS.keySet().forEach(key -> ASSIGN_OPERATORS.put(key + "=", OPERATORS.get(key)));
        ASSIGN_OPERATORS.put("=", (a, b) -> b);
    }

    private static final String NAME_RE = "[a-zA-Z_$][a-zA-Z_$0-9]*";

    public JSInterpreter(String jscode) {
    }

    public Function<String, String> extractFunction(String jscode, String funcName) {
        Pattern pattern = Pattern.compile(format("(?x)" +
                "(?:function\\s+%s|[{;,]\\s*%s\\s*=\\s*function|var\\s+%s\\s*=\\s*function)\\s*" +
                "\\((?<args>[^)]*)\\)\\s*" +
                "\\{(?<code>[^}]+)\\}", funcName.trim(), funcName.trim(), funcName.trim()));
        Matcher funcM = pattern.matcher(jscode);

        if (!funcM.find()) {
            throw new RuntimeException("JSInterpreter doesnt extract function!");
        }

        String[] argnames = funcM.group("args").split(",");
        return buildFunction(argnames, funcM.group("code"));
    }

    private Function<String, String> buildFunction(String[] argnames, String code) {
        return args -> {
            HashMap<String, String> localVars = new HashMap<>();
            for (String argname : argnames) {
                localVars.put(argname, args);
            }

            Pair<List<String>, Boolean> res = null;
            for (String stmt : code.split(";")) {
                res = this.interpretStatement(stmt, localVars, 100);
                if (res.getTwo()) {
                    break;
                }
            }
            return res.getOne().stream().collect(Collectors.joining(""));
        };
    }

    private Pair<List<String>, Boolean> interpretStatement(String stmt, HashMap<String, String> localVars, int allowRecursion) {
        if (allowRecursion < 0) {
            throw new RuntimeException("Recursion limit reached");
        }

        boolean shouldAbort = false;
        stmt = stmt.trim();
        Matcher stmtm = Pattern.compile("var\\s").matcher(stmt);
        String expr;
        if (stmtm.find()) {
            expr = stmt.substring(stmtm.group(0).length());
        } else {
            Matcher returnm = Pattern.compile("return(?:\\s+|$)").matcher(stmt);
            if (returnm.find()) {
                expr = stmt.substring(returnm.group(0).length());
                shouldAbort = true;
            } else {
                expr = stmt;
            }
        }

        List<String> v = this.interpretExpression(expr, localVars, allowRecursion);
        return new Pair<>(v, shouldAbort);
    }

    private <T> List<T> interpretExpression(String expr, HashMap<String, String> localVars, int allowRecursion) {
        if (expr == null) {
            return null;
        }
        expr = expr.trim();
        if (expr.isEmpty()) {
            return null;
        }

        if (expr.startsWith("(")) {
            throw new MethodNotImplementedException("interpretExpression startWith '(' doesn't implemented!");
        }

        for (String op : ASSIGN_OPERATORS.keySet()) {
            BiFunction<String, String, String> opfunc = ASSIGN_OPERATORS.get(op);
            Matcher matcher = Pattern.compile("'(?x)\\n" +
                    "                (?<out>[a-zA-Z_$][a-zA-Z_$0-9]*)(?:\\[(?<index>[^\\]]+?)\\])?\\n" +
                    "                \\s*\\|=\\n" +
                    "                (?<expr>.*)$'").matcher(expr);
            boolean b = matcher.find();
            Matcher m = Pattern.compile(format("(?x)\\n" +
                    "(?<out>%s)(?:\\[(?<index>[^\\]]+?)\\])?\\n" +
                    "\\s*%s\\n" +
                    "(?<expr>.*)$", NAME_RE, Escape.htmlElementContent(op))).matcher(expr);
            if (!m.find()) {
                continue;
            }

            List<String> rightVal = this.interpretExpression(m.group("expr"), localVars, allowRecursion - 1);
            if (m.group("index") != null) {

            } else {
                String cur = localVars.get(m.group("out"));
                String val = opfunc.apply(cur, String.join("", rightVal));
                localVars.put(m.group("out"), val);
                return (List<T>) Arrays.asList(val);
//                cur = local_vars.get(m.group('out'))
//                val = opfunc(cur, right_val)
//                local_vars[m.group('out')] = val
//                return val
            }
        }

        if (Pattern.compile("-?\\d+(\\.\\d+)?").matcher(expr).find()) {
            return (List<T>) Arrays.asList(Integer.parseInt(expr));
        }


        Matcher varm = Pattern.compile(format("(?!if|return|true|false)(?<name>%s)$", NAME_RE)).matcher(expr);
        if (varm.find()) {
            return (List<T>) Arrays.asList(localVars.get(varm.group("name")));
        }

        // TODO json.loads

        Matcher m = Pattern.compile(format("(?<in>%s)\\[(?<idx>.+)\\]$", NAME_RE)).matcher(expr);
        if (m.find()) {
            String val = localVars.get(m.group("in"));
            List<Object> idx = this.interpretExpression(m.group("idx"), localVars, allowRecursion - 1);
            throw new MethodNotImplementedException();
        }

        m = Pattern.compile(format("(?<var>%s)(?:\\.(?<member>[^(]+)|\\[(?<member2>[^]]+)\\])\\s*(?:\\(+(?<args>[^()]*)\\))?$", NAME_RE)).matcher(expr);
        if (m.find()) {
            String variable = m.group("var");
            String member = m.group("member") != null
                    ? m.group("member").replaceAll("'", "").replaceAll("\"", "")
                    : m.group("member2").replaceAll("'", "").replaceAll("\"", "");
            String argstr = m.group("args");
            String obj;
            if (localVars.containsKey(variable)) {
                obj = localVars.get(variable);
            } else {
                throw new MethodNotImplementedException();
            }

            if (argstr == null || argstr.isEmpty()) {
                if (member.equals("length")) {
//                    return obj.length();
                }
            }
            System.out.println();
        }
        return null;
    }
}
