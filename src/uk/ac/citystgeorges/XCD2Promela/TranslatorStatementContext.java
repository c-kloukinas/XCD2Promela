package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorStatementContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (StatementContext)ctx); }
    public T translate(BaseVisitor<T> bv, StatementContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.assgn!=null) {
            s = bv.visit(ctx.assgn).get(0);
        } else if (ctx.skip!=null) {
            s = "skip;\n";
        } else if (ctx.assert!=null) {
            s = bv.visit(ctx.assert).get(0);
        }
        res.add(s);
        return res;
    }
}
