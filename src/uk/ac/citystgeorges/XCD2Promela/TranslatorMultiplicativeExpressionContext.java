package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorMultiplicativeExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (MultiplicativeExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, MultiplicativeExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        /*
          Relies on (passes these to unaryExpression's visit):

          componentDeclaration compType,
          String var_prefix,
          String portid
        */

        s = bv.visit(ctx.multexpr_pre).get(0);
        int children = ctx.getChildCount();
        for (int cnt = 1; cnt < children; ++cnt) {
            var op = ((Token)ctx.getChild(cnt).getPayload()).getType();
            var expr = ctx.getChild(++cnt);
            String ops = ((op == XCDParser.TK_MULTIPLY) ? "*"
                          : (op == XCDParser.TK_DIVIDE) ? "/": "%");
            s += " "
                + ops
                + " " + bv.visit(expr).get(0);
        }

        res.add(s);
        return res;
    }

}
