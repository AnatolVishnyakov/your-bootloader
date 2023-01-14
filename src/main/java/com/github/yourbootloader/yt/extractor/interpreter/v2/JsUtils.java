package com.github.yourbootloader.yt.extractor.interpreter.v2;

import java.util.Map;
import java.util.function.Function;

public class JsUtils {

    static Map<String, Function<?, ?>> OPERATORS = Map.of(
//            ">>",
    );
    /*
# (op, definition) in order of binding priority, tightest first
# avoid dict to maintain order
# definition None => Defined in JSInterpreter._operator
_OPERATORS = (
    ('>>', _js_bit_op(operator.rshift)),
    ('<<', _js_bit_op(operator.lshift)),
    ('+', _js_arith_op(operator.add)),
    ('-', _js_arith_op(operator.sub)),
    ('*', _js_arith_op(operator.mul)),
    ('%', _js_mod),
    ('/', _js_div),
    ('**', _js_exp),
)

_COMP_OPERATORS = (
    ('===', operator.is_),
    ('!==', operator.is_not),
    ('==', _js_eq_op(operator.eq)),
    ('!=', _js_eq_op(operator.ne)),
    ('<=', _js_comp_op(operator.le)),
    ('>=', _js_comp_op(operator.ge)),
    ('<', _js_comp_op(operator.lt)),
    ('>', _js_comp_op(operator.gt)),
)

_LOG_OPERATORS = (
    ('|', _js_bit_op(operator.or_)),
    ('^', _js_bit_op(operator.xor)),
    ('&', _js_bit_op(operator.and_)),
)

_SC_OPERATORS = (
    ('?', None),
    ('??', None),
    ('||', None),
    ('&&', None),
)
    * */
}
