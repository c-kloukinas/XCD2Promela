package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorConditionalExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ConditionalExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, ConditionalExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.condexpr1).get(0);
        int children = ctx.getChildCount();
        for (int cnt = 1; cnt < children; ++cnt) {
            var op = ((Token)ctx.getChild(cnt).getPayload()).getType();
            var expr = ctx.getChild(++cnt);
            String ops = "";
            switch (op) {
            case XCDParser.TK_OR:
                ops = "||";
                break;
            case XCDParser.TK_AND:
                ops = "&&";
                break;
            case XCDParser.TK_SEMICOLON: // ?!?!?!?
                ops = "XXX_UNKNOWN_SEMICOLON_OPERATOR_XXX";
                bv.myassert(op!=XCDParser.TK_SEMICOLON
                            , "Cannot translate a `;' operator");
                break;
            }
            s += " "
                + ops
                + " " + bv.visit(expr).get(0);
        }

        res.add(s);
        return res;
    }

}
