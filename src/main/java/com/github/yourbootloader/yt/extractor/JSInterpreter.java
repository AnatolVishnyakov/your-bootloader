package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import com.github.yourbootloader.yt.extractor.dto.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.Escape;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
public class JSInterpreter {

    private static final Map<String, BiFunction<String, Object, Object>> OPERATORS = new LinkedHashMap<String, BiFunction<String, Object, Object>>() {{
        put("\\|", (a, b) -> new BigInteger(a).or(new BigInteger(((String) b))).toString());
        put("^", (a, b) -> new BigInteger(a).xor(new BigInteger((String) b)).toString());
        put("&", (a, b) -> new BigInteger(a).and(new BigInteger((String) b)).toString());
        put(">>", (a, b) -> new BigInteger(a).shiftRight(new BigInteger((String) b).intValue()).toString());
        put("<<", (a, b) -> new BigInteger(a).shiftLeft(new BigInteger((String) b).intValue()).toString());
        put("-", (a, b) -> new BigInteger(a).subtract(new BigInteger((String) b)).toString());
        put("\\+", (a, b) -> new BigInteger(a).add(new BigInteger((String) b)).toString());
        put("%", (a, b) -> new BigInteger(a).mod(new BigInteger((String) b)).toString());
        put("/", (a, b) -> new BigInteger(a).divide(new BigInteger((String) b)).toString());
        put("\\*", (a, b) -> new BigInteger(a).multiply(new BigInteger((String) b)).toString());
    }};
    private static final Map<String, BiFunction<String, Object, Object>> ASSIGN_OPERATORS = new HashMap<>();
    private final Map<String, Object> objects = new HashMap<>();
    private final Map<String, Function<Object, Object>> functions = new HashMap<>();

    static {
        OPERATORS.keySet().forEach(key -> ASSIGN_OPERATORS.put(key + "=", OPERATORS.get(key)));
        ASSIGN_OPERATORS.put("=", (a, b) -> b);
    }

    private static final String NAME_RE = "[a-zA-Z_$][a-zA-Z_$0-9]*";
    private final String jscode;

    public JSInterpreter(String jscode) {
        this.jscode = jscode;
    }

    public <T, R> Function<T, R> extractFunction(String funcName) {
        String _pattern = format("(?x)" +
                        "(?:function\\s+%s|[{;,]\\s*%s\\s*=\\s*function|var\\s+%s\\s*=\\s*function)\\s*" +
                        "\\((?<args>[^)]*)\\)\\s*" +
                        "\\{(?<code>[^}]+)\\}",
                funcName.replace("$", "\\$").trim(),
                funcName.replace("$", "\\$").trim(),
                funcName.replace("$", "\\$").trim()
        );
        Pattern pattern = Pattern.compile(_pattern);
        Matcher funcM = pattern.matcher(jscode);

        if (!funcM.find()) {
            throw new RuntimeException("Could not find JS function " + funcName);
        }

        String[] argnames = funcM.group("args").split(",");
        return buildFunction(argnames, funcM.group("code"));
    }

    private <T, R> Function<T, R> buildFunction(String[] argnames, String code) {
        return args -> {
            HashMap<String, Object> localVars = new HashMap<>();
            for (String argname : argnames) {
                if (!argname.isEmpty()) {
                    if (args instanceof List) {
                        localVars.put(argname, ((List<?>) args).get(0));
                    } else {
                        localVars.put(argname, args);
                    }
                }
            }

            Pair<Object, Boolean> res = null;
            for (String stmt : code.split(";")) {
                res = this.interpretStatement(stmt, localVars, 100);
                if (res.getTwo()) {
                    break;
                }
            }
            return (R) Optional.ofNullable(res)
                    .map(v -> v.getOne())
                    .map(v -> {
                        if (v instanceof String && ((String) v).chars().allMatch(Character::isDigit)) {
                            return Integer.parseInt(((String) v));
                        }
                        return v;
                    })
                    .orElse(null);
        };
    }

    private Pair<Object, Boolean> interpretStatement(String stmt, HashMap<String, Object> localVars, int allowRecursion) {
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

        Object v = this.interpretExpression(expr, localVars, allowRecursion);
        if (Pattern.compile("^-?\\d+$").matcher(String.valueOf(v)).find()) {
            v = Integer.parseInt(String.valueOf(v));
        }
        return new Pair<Object, Boolean>(v, shouldAbort);
    }

    private Object interpretExpression(String expr, HashMap<String, Object> localVars, int allowRecursion) {
        if (expr == null || expr.isEmpty() || expr.trim().isEmpty()) {
            return null;
        }
        expr = expr.trim();

        if (expr.startsWith("(")) {
            int parensCount = 0;
            Matcher m = Pattern.compile("[()]").matcher(expr);
            while (m.find()) {
                if (m.group(0).equals("(")) {
                    parensCount += 1;
                } else {
                    parensCount -= 1;
                    if (parensCount == 0) {
                        String subExpr = expr.substring(1, m.start());
                        Object subResult = this.interpretExpression(subExpr, localVars, allowRecursion);
                        String remainingExpr = expr.substring(m.end()).trim();
                        if (remainingExpr.isEmpty()) {
                            return subResult;
                        } else {
                            expr = subResult + remainingExpr;
                        }
                        break;
                    }
                }
            }
        }

        for (String op : ASSIGN_OPERATORS.keySet()) {
            BiFunction<String, Object, Object> opfunc = ASSIGN_OPERATORS.get(op);
            String format = format("(?x)" +
                    "(?<out>%s)(?:\\[(?<index>[^\\]]+?)\\])?" +
                    "\\s*%s" +
                    "(?<expr>.*)$", NAME_RE, op);
            Matcher m = Pattern.compile(format).matcher(expr);
            if (!m.find()) {
                continue;
            }

            Object rightVal = this.interpretExpression(m.group("expr"), localVars, allowRecursion - 1);
            if (m.group("index") != null) { // разбор массива
                Object lvar = localVars.get(m.group("out"));
                Integer idx = (Integer) this.interpretExpression(m.group("index"), localVars, allowRecursion);
                Integer cur = ((List<Integer>) lvar).get(idx);
                Object val = opfunc.apply(String.valueOf(cur), String.valueOf(rightVal));
                ((List<Integer>) lvar).set(idx, Integer.parseInt(((String) val)));
                return val;
            } else {
                String cur = String.valueOf(localVars.get(m.group("out")));
                Object val = opfunc.apply(cur, rightVal);
                localVars.put(m.group("out"), val);
                return val;
            }
        }

        if (expr.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(expr);
        }

        String varmPattern = format("(?!if|return|true|false)(?<name>%s)$", NAME_RE);
        if (Pattern.matches(varmPattern, expr)) {
            Matcher varm = Pattern.compile(varmPattern).matcher(expr);
            varm.find();
            return localVars.get(varm.group("name"));
        }

        try {
            JSONArray jsonArray = new JSONArray(expr);
            return jsonArray.toList();
        } catch (JSONException exc1) {
            try {
                JSONObject jsonObject = new JSONObject(expr);
                return jsonObject;
            } catch (Exception exc2) {
                // TODO json.loads
                log.error("JSON loads: {}", exc2.getMessage());
            }
        }

        Matcher m = Pattern.compile(format("(?<in>%s)\\[(?<idx>.+)\\]$", NAME_RE)).matcher(expr);
        if (m.find()) {
            Object valueFromLocalVars = localVars.get(m.group("in"));
            Object returnValue = this.interpretExpression(m.group("idx"), localVars, allowRecursion - 1);
            Integer idx = Integer.valueOf(((String) returnValue));
            if (valueFromLocalVars instanceof List) {
                return ((List<Integer>) valueFromLocalVars).get(idx);
            } else {
                char[] val = ((String) valueFromLocalVars).toCharArray();
                return val[idx];
            }
        }

        m = Pattern.compile(format("(?<var>%s)(?:\\.(?<member>[^(]+)|\\[(?<member2>[^]]+)\\])\\s*(?:\\(+(?<args>[^()]*)\\))?$", NAME_RE)).matcher(expr);
        if (m.matches()) {
            String variable = m.group("var");
            String member = m.group("member") != null
                    ? m.group("member").replaceAll("'", "").replaceAll("\"", "")
                    : m.group("member2").replaceAll("'", "").replaceAll("\"", "");
            String argstr = m.group("args");
            Object obj;
            if (localVars.containsKey(variable)) {
                obj = localVars.get(variable);
            } else {
                if (!objects.containsKey(variable)) {
                    objects.put(variable, this.extractObject(variable));
                }
                obj = objects.get(variable);
            }

            if (argstr == null) {
                if (member.equals("length")) {
                    if (obj instanceof List) {
                        return ((List<Integer>) obj).size();
                    }
                    return ((String) obj).length();
                }
            }

            List<Object> argvals = new ArrayList<>();
            if (!argstr.isEmpty()) {
                for (String v : argstr.split(",")) {
                    argvals.add(this.interpretExpression(v, localVars, allowRecursion));
                }
            }

            if (member.equals("split")) {
                return ((String) obj).toCharArray();
            }
            if (member.equals("join")) {
                throw new MethodNotImplementedException("join not implemented!");
            }
            if (member.equals("reverse")) {
                throw new MethodNotImplementedException("reverse not implemented!");
            }
            if (member.equals("slice")) {
                throw new MethodNotImplementedException("slice not implemented!");
            }
            if (member.equals("splice")) {
                throw new MethodNotImplementedException("splice not implemented!");
            }
            throw new MethodNotImplementedException("obj[member](argvals) not implemented!");
        }

        for (String op : OPERATORS.keySet()) {
            BiFunction<String, Object, Object> opfunc = OPERATORS.get(op);
            m = Pattern.compile(format("(?<x>.+?)%s(?<y>.+)", op)).matcher(expr);
            if (!m.find()) {
                continue;
            }
            Pair<Object, Boolean> res = this.interpretStatement(m.group("x"), localVars, allowRecursion - 1);
            String x = String.valueOf(res.getOne());
            if (res.getTwo()) {
                throw new RuntimeException(format("Premature left-side return of %s in %s", op, expr));
            }
            res = this.interpretStatement(m.group("y"), localVars, allowRecursion - 1);
            String y = String.valueOf(res.getOne());
            if (res.getTwo()) {
                throw new RuntimeException(format("Premature left-side return of %s in %s", op, expr));
            }
            return opfunc.apply(x, y);
        }

        m = Pattern.compile(format("^(?<func>%s)\\((?<args>[a-zA-Z0-9_$,]*)\\)$", NAME_RE)).matcher(expr);
        if (m.find()) {
            String fname = m.group("func");
            List<Integer> argvals = new ArrayList<>();
            if (m.group("args").length() > 0) {
                for (String v : m.group("args").split(",")) {
                    if (v.chars().allMatch(Character::isDigit)) {
                        argvals.add(Integer.parseInt(v));
                    } else {
                        argvals.add(Integer.parseInt(((String) localVars.get(v))));
                    }
                }
            }
            if (!functions.containsKey(fname)) {
                functions.put(fname, this.extractFunction(fname));
            }
            return functions.get(fname).apply(argvals);
        }
        throw new MethodNotImplementedException("'Unsupported JS expression " + expr);
    }

    private Object extractObject(String variable) {
        String FUNC_NAME_RE = "(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')";
        Pattern pattern = Pattern.compile(format("(?x)" +
                "(?<!this\\.)%s\\s*=\\s*{\\s*" +
                "(?<fields>(%s\\s*:\\s*function\\s*\\(.*?\\)\\s*{.*?}(?:,\\s*)?)*)" +
                "}\\s*;", Escape.htmlElementContent(variable), FUNC_NAME_RE));
        Matcher objm = pattern.matcher(jscode);
        objm.find();
        String fields = objm.group("fields");

        Matcher fieldsm = Pattern.compile(format("(?x)(?<key>%s)\\s*:\\s*function\\s*\\((?<args>[a-z,]+)\\)\\{(?<code>[^}]+)}", FUNC_NAME_RE)).matcher(fields);
        fieldsm.find();
        for (int i = 0; i < fieldsm.groupCount(); i++) {
            fieldsm.group(1);
            // TODO
        }
        return null;
    }

    public <T, R> Object callFunction(String funcName, T args) {
        Function<T, R> f = this.extractFunction(funcName);
        return f.apply(args);
    }
}