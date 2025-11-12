package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorRangeContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (RangeContext)ctx); }

    public T translate(BaseVisitor<T> bv, RangeContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.minvar).get(0);
        res.add(s);
        s = bv.visit(ctx.maxvar).get(0);
        res.add(s);
        return res;
    }

}
