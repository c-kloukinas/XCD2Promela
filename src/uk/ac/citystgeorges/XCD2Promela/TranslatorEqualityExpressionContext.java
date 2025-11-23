package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorStatementContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (EqualityExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, EqualityExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        if (ctx.relExpr!=null)
            s = bv.visit(ctx.relExpr).get(0);
        else if (ctx.op!=null) { // ==, !=
            // operators are the same in XCD & Promela
            String ops = bv.getTokenString(ctx.op);
            s = bv.visit(ctx.eqExpr1).get(0)
                + " "
                + ops
                + " "
                + bv.visit(ctx.relExpr2).get(0);
        } else if (ctx.inRange!=null) {
            var eqExpr1=bv.visit(ctx.eqExpr1).get(0);
            var theRange=bv.visit(ctx.theRange);
            s = "( ("
                + eqExpr1
                + "<="
                + theRange.get(0)
                + ") && ("
                + eqExpr1
                + "<="
                + theRange.get(1)
                + ") )";
        } else { // inSet
            var eqExpr1=bv.visit(ctx.eqExpr1).get(0);
            var theSet=bv.visit(ctx.theSet);
            int sz = theSet.size();
            if (sz==0)
                s = "false";    // nothing is an element of the empty set
            else {
                s = "( ("
                    + eqExpr1
                    + "=="
                    + theSet.get(0)
                    + ")";
                for (int i=1; i<sz; ++i) {
                    s += " || ("
                        + eqExpr1
                        + "=="
                        + theSet.get(i)
                        + ")";
                }
                s += " )";
            }
        }
        res.add(s);
        return res;
    }
}
