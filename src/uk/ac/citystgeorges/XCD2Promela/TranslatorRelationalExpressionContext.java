package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorRelationalExpressionContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (RelationalExpressionContext)ctx); }
    public T translate(BaseVisitor<T> bv, RelationalExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.shiftExpr!=null)
            s = bv.visit(ctx.shiftExpr).get(0);
        else {
            // operators are the same in XCD & Promela
            String ops = bv.getTokenString(ctx.op);
            s = bv.visit(ctx.relExpr1).get(0)
                + " "
                + ops
                + " "
                + bv.visit(ctx.shiftExpr2);
        }
        res.add(s);
        return res;
    }
}
