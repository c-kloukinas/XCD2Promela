package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorUnaryExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (UnaryExpressionContext)ctx); }

    public T translate(BaseVisitor<T> bv, UnaryExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        /*
          Relies on (passes these to nullaryExpression's visit):

          componentDeclaration compType,
          String var_prefix,
          String portid
        */
        if (ctx.preop!=null)
            s="!";
        s += bv.visit(ctx.nullexpr).get(0);

        if (ctx.postop!=null) {
            if (ctx.postop.getType() == XCDParser.TK_INCREMENT)
                s += "++";
            else
                s += "--";
        }

        res.add(s);
        return res;
    }

}
