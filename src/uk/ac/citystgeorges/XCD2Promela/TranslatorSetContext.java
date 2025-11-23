package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorSetContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (SetContext)ctx); }
    public T translate(BaseVisitor<T> bv, SetContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        s = bv.visit(ctx.val1).get(0);
        res.add(s);
        for (var val : ctx.vals) {
            s = bv.visit(val).get(0);
            res.add(s);
        }
        return res;
    }
}
