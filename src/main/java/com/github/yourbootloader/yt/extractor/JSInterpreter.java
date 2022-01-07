package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.extractor.dto.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class JSInterpreter {
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
                res = this.interpretStatement(stmt, localVars);
                if (res.getTwo()) {
                    break;
                }
            }
            return res.getOne().stream().collect(Collectors.joining(""));
        };
    }

    private Pair<List<String>, Boolean> interpretStatement(String stmt, HashMap<String, String> localVars) {
        return null;
    }
}
