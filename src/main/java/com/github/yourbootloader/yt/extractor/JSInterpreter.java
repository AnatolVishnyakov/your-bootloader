package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import com.github.yourbootloader.yt.extractor.dto.Pair;
import org.apache.tomcat.util.security.Escape;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class JSInterpreter {

    private static final Map<String, BiFunction<String, String, String>> OPERATORS = new HashMap<String, BiFunction<String, String, String>>() {{
        put("\\|", (a, b) -> new BigInteger(a).or(new BigInteger(b)).toString());
        put("^", (a, b) -> new BigInteger(a).xor(new BigInteger(b)).toString());
        put("&", (a, b) -> new BigInteger(a).and(new BigInteger(b)).toString());
        put(">>", (a, b) -> new BigInteger(a).shiftRight(new BigInteger(b).intValue()).toString());
        put("<<", (a, b) -> new BigInteger(a).shiftLeft(new BigInteger(b).intValue()).toString());
        put("-", (a, b) -> new BigInteger(a).subtract(new BigInteger(b)).toString());
        put("\\+", (a, b) -> new BigInteger(a).add(new BigInteger(b)).toString());
        put("%", (a, b) -> new BigInteger(a).mod(new BigInteger(b)).toString());
        put("/", (a, b) -> new BigInteger(a).divide(new BigInteger(b)).toString());
        put("\\*", (a, b) -> new BigInteger(a).multiply(new BigInteger(b)).toString());
    }};
    private static final Map<String, BiFunction<String, String, String>> ASSIGN_OPERATORS = new HashMap<>();
    private final Map<String, Object> objects = new HashMap<>();

    static {
        OPERATORS.keySet().forEach(key -> ASSIGN_OPERATORS.put(key + "=", OPERATORS.get(key)));
        ASSIGN_OPERATORS.put("=", (a, b) -> b);
    }

    private static final String NAME_RE = "[a-zA-Z_$][a-zA-Z_$0-9]*";
    private final String jscode;

    public JSInterpreter(String jscode) {
        this.jscode = jscode;
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

            Pair<Object, Boolean> res;
            for (String stmt : code.split(";")) {
                res = this.interpretStatement(stmt, localVars, 100);
                if (res.getTwo()) {
                    break;
                }
            }
            return null;
        };
    }

    private Pair<Object, Boolean> interpretStatement(String stmt, HashMap<String, String> localVars, int allowRecursion) {
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
        return new Pair<Object, Boolean>(v, shouldAbort);
    }

    private Object interpretExpression(String expr, HashMap<String, String> localVars, int allowRecursion) {
        if (expr == null || expr.isEmpty() || expr.trim().isEmpty()) {
            return null;
        }
        expr = expr.trim();

        if (expr.startsWith("(")) {
            throw new MethodNotImplementedException("interpretExpression startWith '(' doesn't implemented!");
        }

        for (String op : ASSIGN_OPERATORS.keySet()) {
            BiFunction<String, String, String> opfunc = ASSIGN_OPERATORS.get(op);
            String format = format("(?x)" +
                    "(?<out>%s)(?:\\[(?<index>[^\\]]+?)\\])?" +
                    "\\s*%s" +
                    "(?<expr>.*)$", NAME_RE, op);
            Matcher m = Pattern.compile(format).matcher(expr);
            if (!m.find()) {
                continue;
            }

            Object rightVal = this.interpretExpression(m.group("expr"), localVars, allowRecursion - 1);
            if (m.group("index") != null) {
                char[] lvar = localVars.get(m.group("out")).toCharArray();
                Integer idx = (Integer) this.interpretExpression(m.group("index"), localVars, allowRecursion);
                char cur = lvar[idx];
                String val = opfunc.apply(String.valueOf(cur), (String) rightVal);
                lvar[idx] = val.toCharArray()[0];
                return val;
            } else {
                String cur = localVars.get(m.group("out"));
                String val = opfunc.apply(cur, String.join("", String.valueOf((char[]) rightVal)));
                localVars.put(m.group("out"), val);
                return val;
            }
        }

        if (Pattern.compile("\\d+").matcher(expr).find()) {
            return Integer.parseInt(expr);
        }

        Matcher varm = Pattern.compile(format("(?!if|return|true|false)(?<name>%s)$", NAME_RE)).matcher(expr);
        if (varm.find()) {
            return localVars.get(varm.group("name"));
        }

        // TODO json.loads
        if (expr.equals("\"\"")) {
            return "";
        }

        Matcher m = Pattern.compile(format("(?<in>%s)\\[(?<idx>.+)\\]$", NAME_RE)).matcher(expr);
        if (m.find()) {
            char[] val = localVars.get(m.group("in")).toCharArray();
            Integer idx = (Integer) this.interpretExpression(m.group("idx"), localVars, allowRecursion - 1);
            return val[idx];
        }

        m = Pattern.compile(format("(?<var>%s)(?:\\.(?<member>[^(]+)|\\[(?<member2>[^]]+)\\])\\s*(?:\\(+(?<args>[^()]*)\\))?$", NAME_RE)).matcher(expr);
        if (m.find()) {
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
        throw new MethodNotImplementedException();
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
}
