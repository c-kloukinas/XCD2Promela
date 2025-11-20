package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorPostStatementContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (PostStatementContext)ctx); }

    public T translate(BaseVisitor<T> bv, PostStatementContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        if (ctx.skip!=null) {
            s = "skip;";
            res.add(s);
            return res;
        }

        s = bv.visit(ctx.postExpr).get(0) + ";";
        for (var pe : ctx.postExprs) {
            s += " " + bv.visit(pe).get(0) + ";";
        }
        res.add(s);
        return res;
    }

}
