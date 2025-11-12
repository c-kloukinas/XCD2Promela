package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorAdditiveExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (AdditiveExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, AdditiveExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.addexpr_pre).get(0);
        int children = ctx.getChildCount();
        for (int cnt = 1; cnt < children; ++cnt) {
            var op = ((Token)ctx.getChild(cnt).getPayload()).getType();
            var expr = ctx.getChild(++cnt);
            String ops = ((op == XCDParser.TK_SUM) ? "+"
                          : (op == XCDParser.TK_SUBTRACT) ? "-": "UNKNOWNOPERAND");
            s += " "
                + ops
                + " " + bv.visit(expr).get(0);
        }

        res.add(s);
        return res;
    }

}
