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
        if (ctx.orExpr!=null) {
            s = bv.visit(ctx.orExpr).get(0);
        } else {
            s = "( ("
                + bv.visit(ctx.orExprGuard).get(0)
                + ") ? ("
                + bv.visit(ctx.exprThen).get(0)
                + ") : ("
                + bv.visit(ctx.condExprElse).get(0)
                + ") )";
        }
        res.add(s);
        return res;
    }
}
