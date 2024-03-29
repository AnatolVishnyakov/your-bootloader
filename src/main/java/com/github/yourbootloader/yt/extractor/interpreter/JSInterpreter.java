package com.github.yourbootloader.yt.extractor.interpreter;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import com.github.yourbootloader.yt.extractor.dto.Pair;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

// TODO Требуется отрефакторить на использование
//  более внятной структуры данных + дженериков
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
    private static final String NAME_RE = "[a-zA-Z_$][a-zA-Z_$0-9]*";
    private static final Map<Character, Character> MATCHING_PARENS = new HashMap<Character, Character>() {{
        put('(', ')');
        put('{', '}');
        put('[', ']');
    }};

    static {
        OPERATORS.keySet().forEach(key -> ASSIGN_OPERATORS.put(key + "=", OPERATORS.get(key)));
        ASSIGN_OPERATORS.put("=", (a, b) -> b);
    }

    private final Map<String, Object> objects = new HashMap<>();
    private final Map<String, Function<Object, Object>> functions = new HashMap<>();
    private final String jscode;
    private int namedObjectCounter = 0;

    public JSInterpreter(String jscode) {
        this.jscode = jscode;
    }

    public <T, R> String namedObject(Map<String, Function<T, R>> namespace, Function<T, R> obj) {
        namedObjectCounter += 1;
        String name = String.format("__youtube_dl_jsinterp_obj%s", namedObjectCounter);
        namespace.put(name, obj);
        return name;
    }

    public static String separate(String expr, String delim, Integer maxSplit) {
        if (expr == null || expr.isEmpty()) {
            return "";
        }
        if (delim == null) {
            delim = ",";
        }

        Map<Character, Integer> counters = MATCHING_PARENS.values().stream().collect(Collectors.toMap(s -> s, v -> 0));
        int start = 0;
        int splits = 0;
        int pos = 0;
        int delimLen = delim.length() - 1;

        for (int idx = 0; idx < expr.length(); idx++) {
            char symbol = expr.charAt(idx);
            if (MATCHING_PARENS.containsKey(symbol)) {
                counters.computeIfPresent(MATCHING_PARENS.get(symbol), (c, v) -> v + 1);
            } else if (counters.containsKey(symbol)) {
                counters.computeIfPresent(MATCHING_PARENS.get(symbol), (c, v) -> v - 1);
            }
            if (symbol != delim.charAt(pos) || !counters.isEmpty()) {
                pos = 0;
                continue;
            } else if (pos != delimLen) {
                pos += 1;
                continue;
            }
            start = idx + 1;
            pos = 0;

            if (maxSplit != 0 && splits >= maxSplit) {
                break;
            }
        }
        return expr.substring(start);
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
            for (int i = 0; i < argnames.length; i++) {
                String argname = argnames[i];
                if (!argname.isEmpty()) {
                    if (args instanceof List) {
                        localVars.put(argname, ((List<?>) args).get(i));
                    } else if (args instanceof String) {
                        localVars.put(argname, new StringBuilder(((String) args)));
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

    private String getReturnValue(Object v) {
        if (v instanceof char[]) {
            String res = String.copyValueOf(((char[]) v));
            return res;
        } else {
            return String.valueOf(v);
        }
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
        log.debug("Statement: {} Value: {}", stmt, getReturnValue(v));
        return new Pair<Object, Boolean>(v, shouldAbort);
    }

    private Object interpretExpression(String expr, HashMap<String, Object> localVars, int allowRecursion) {
        if (expr == null || expr.isEmpty() || expr.trim().isEmpty() || expr.equals("\"\"")) {
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
                Object index = this.interpretExpression(m.group("index"), localVars, allowRecursion);
                Integer idx = index instanceof Integer
                        ? (Integer) index
                        : Integer.parseInt(((String) index));
                String cur;
                if (lvar instanceof List<?>) {
                    cur = String.valueOf(((List<Integer>) lvar).get(idx));
                } else if (lvar instanceof char[]) {
                    cur = String.valueOf(((char[]) lvar)[idx]);
                } else if (lvar instanceof StringBuilder) {
                    cur = String.valueOf(((StringBuilder) lvar).charAt(idx));
                } else {
                    throw new MethodNotImplementedException();
                }
                Object val = opfunc.apply(cur, String.valueOf(rightVal));
                if (lvar instanceof List<?>) {
                    ((List<Integer>) lvar).set(idx, Integer.parseInt(((String) val)));
                } else if (lvar instanceof char[]) {
                    ((char[]) lvar)[idx] = ((String) val).toCharArray()[0];
                } else {
                    ((StringBuilder) lvar).deleteCharAt(idx);
                    ((StringBuilder) lvar).insert(idx, ((String) val));
                }
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
            }
        }

        Matcher m = Pattern.compile(format("(?<in>%s)\\[(?<idx>.+)\\]$", NAME_RE)).matcher(expr);
        if (m.find()) {
            Object valueFromLocalVars = localVars.get(m.group("in"));
            Object returnValue = this.interpretExpression(m.group("idx"), localVars, allowRecursion - 1);
            Integer idx = Integer.valueOf((String.valueOf(returnValue)));
            if (valueFromLocalVars instanceof List) {
                return ((List<Integer>) valueFromLocalVars).get(idx);
            } else if (valueFromLocalVars instanceof char[]) {
                return String.valueOf(((char[]) valueFromLocalVars)[idx]);
            } else {
                return ((StringBuilder) valueFromLocalVars).charAt(idx);
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
                    if (obj instanceof char[]) {
                        return ((char[]) obj).length;
                    }
                    return ((StringBuilder) obj).length();
                }
            }

            List<Object> argvals = new ArrayList<>();
            if (!argstr.isEmpty()) {
                for (String v : argstr.split(",")) {
                    argvals.add(this.interpretExpression(v, localVars, allowRecursion));
                }
            } else {
                argvals.add(0);
                argvals.add(1);
            }

            if (member.equals("split")) {
                log.debug("\n\nsplit");
                return obj;
            }
            if (member.equals("join")) {
                log.debug("\n\njoin");
                return argstr.replaceAll("\"", "") + ((StringBuilder) obj).toString();
            }
            if (member.equals("reverse")) {
                log.debug("\n\nreverse");
                StringBuilder reverse = ((StringBuilder) obj).reverse();
                return reverse;
            }
            if (member.equals("slice")) {
                log.debug("\n\nslice");
                String slice = ((StringBuilder) obj).substring(((Integer) argvals.get(0)));
                return new StringBuilder(slice);
            }
            if (member.equals("splice")) {
                log.debug("\n\nsplice");
                int index = ((Integer) argvals.get(0));
                int howMany = ((Integer) argvals.get(1));

                if (obj instanceof StringBuilder) {
                    List<Object> res = new ArrayList<>();
                    for (int i = index; i < Math.min(index + howMany, ((StringBuilder) obj).length()); i++) {
                        res.add(((StringBuilder) obj).charAt(i));
                        ((StringBuilder) obj).deleteCharAt(i);
                    }
                    return res;
                }
                throw new MethodNotImplementedException("splice not implemented!");
            }
            Function<Object, String> function = (Function<Object, String>) ((Map<String, Object>) obj).get(member);
            return function.apply(argvals);
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
            List<Object> argvals = new ArrayList<>();
            if (m.group("args").length() > 0) {
                for (String v : m.group("args").split(",")) {
                    if (v.chars().allMatch(Character::isDigit)) {
                        argvals.add(Integer.parseInt(v));
                    } else if (localVars.get(v) instanceof StringBuilder) {
                        argvals.add(localVars.get(v));
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

    private Map<String, Object> extractObject(String objname) {
        String FUNC_NAME_RE = "(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')";
        String s = format("(?x)" +
                "(?<!this\\.)%s\\s*=\\s*\\{\\s*" +
                "(?<fields>(%s\\s*:\\s*function\\s*\\(.*?\\)\\s*\\{.*?}(?:,\\s*)?)*)" +
                "}\\s*;", Pattern.quote(objname), FUNC_NAME_RE);
        Pattern pattern = Pattern.compile(s);
        log.debug("Extract object: {}", s);
        Matcher objm = pattern.matcher(jscode);
        if (!objm.find()) {
            throw new IllegalStateException();
        }

        String fields = objm.group("fields");

        Map<String, Object> obj = new HashMap<>();
        Matcher fieldsm = Pattern.compile(format("(?x)(?<key>%s)\\s*:\\s*function\\s*\\((?<args>[a-z,]+)\\)\\{(?<code>[^}]+)}", FUNC_NAME_RE)).matcher(fields);
        while (fieldsm.find()) {
            String[] argnames = fieldsm.group("args").split(",");
            Function<Object, Object> code = this.buildFunction(argnames, fieldsm.group("code"));
            String key = fieldsm.group("key");
            obj.put(removeQuotes(key), code);
        }

        return obj;
    }

    private String removeQuotes(String key) {
        return key.replaceAll("\"", "")
                .replaceAll("'", "");
    }

    public <T, R> Object callFunction(String funcName, T args) {
        Function<T, R> f = this.extractFunction(funcName);
        return f.apply(args);
    }
}
