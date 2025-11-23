package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorInclusiveOrExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (InclusiveOrExpressionContext)ctx); }
    public T translate(BaseVisitor<T> bv, InclusiveOrExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.bitxorExpr!=null) {
            s = bv.visit(ctx.bitxorExpr).get(0);
        } else {
            s = bv.visit(ctx.bitorExpr1).get(0)
                + "|"
                + bv.visit(ctx.bitxorExpr2).get(0);
        }
        res.add(s);
        return res;
    }

}
