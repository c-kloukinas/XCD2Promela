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

        if (ctx.unaryExpr!=null)
            s = bv.visit(ctx.unaryExpr).get(0);
        else {
            // operators are the same in XCD & Promela
            String ops = bv.getTokenString(ctx.op);
            s = bv.visit(ctx.multExpr1).get(0)
                + " " + ops
                + " " + bv.visit(ctx.unaryExpr2).get(0);
        }
        res.add(s);
        return res;
    }

}
