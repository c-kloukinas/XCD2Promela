package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorEqualityExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (EqualityExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, EqualityExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.eqexpr_pre).get(0);
        if (ctx.eqexpr!=null) { // TK_ASSIGN
            s += "="
                + "("
                + bv.visit(ctx.eqexpr).get(0)
                + ")";
        } else if (ctx.set!=null) { // TK_IN
            var range = bv.visit(ctx.set);
            s += " XXXISINSIDEXXX("
                + range.get(0)
                + ","
                + range.get(1)
                + ")";
        }

        res.add(s);
        return res;
    }

}
