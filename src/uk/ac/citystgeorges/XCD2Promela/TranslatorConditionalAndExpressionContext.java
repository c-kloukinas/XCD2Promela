package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorConditionalAndExpressionContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ConditionalAndExpressionContext)ctx); }
    public T translate(BaseVisitor<T> bv, ConditionalAndExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.bitorExpr!=null) {
            s = bv.visit(ctx.bitorExpr).get(0);
        } else {
            s = bv.visit(ctx.andExpr1).get(0)
                + "&&"
                + bv.visit(ctx.bitorExpr2).get(0);
        }
        res.add(s);
        return res;
    }
}
