package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorRelationalExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (RelationalExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, RelationalExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.relexpr_pre).get(0);
        int children = ctx.getChildCount();
        for (int cnt = 1; cnt < children; ++cnt) {
            var opToken = ((Token)ctx.getChild(cnt).getPayload());
            var op = opToken.getType();
            var expr = ctx.getChild(++cnt);
            String ops = "";
            switch (op) {
            case XCDParser.TK_LESS:
                ops = "<";
                break;
            case XCDParser.TK_GREATER:
                ops = ">";
                break;
            case XCDParser.TK_GREATEROREQUAL:
                ops = ">=";
                break;
            case XCDParser.TK_LESSTHANOREQUAL:
                ops = "<=";
                break;
            case XCDParser.TK_EQUAL:
                ops = "==";
                break;
            case XCDParser.TK_NOTEQUAL:
                ops = "!=";
                break;
            default:
                bv.myassert(false
                            , "Unknown operator in relational expression:"
                            + opToken.getText());
            }
            s += " "
                + ops
                + " " + bv.visit(expr).get(0);
        }

        res.add(s);
        return res;
    }

}
