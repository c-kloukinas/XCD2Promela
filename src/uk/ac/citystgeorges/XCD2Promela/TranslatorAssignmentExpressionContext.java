package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorAssignmentExpressionContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (AssignmentExpressionContext)ctx); }
    public T translate(BaseVisitor<T> bv, AssignmentExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.condExpr!=null) {
            s = bv.visit(ctx.condExpr).get(0);
        } else {
            s = bv.visit(ctx.assgnmnt).get(0);
        }
        res.add(s);
        return res;
    }
}
