package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorUnaryExpressionNotPlusMinusContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (UnaryExpressionNotPlusMinusContext)ctx); }
    public T translate(BaseVisitor<T> bv, UnaryExpressionNotPlusMinusContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.prim!=null) {
            s=bv.visit(ctx.prim).get(0);
        } else {
            // operators are the same in XCD & Promela
            String ops = bv.getTokenString(ctx.op);
            s = ops + bv.visit(ctx.unaryExpr).get(0);
        }
        res.add(s);
        return res;
    }
}
