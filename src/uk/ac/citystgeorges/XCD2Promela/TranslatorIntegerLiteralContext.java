package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorIntegerLiteralContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (IntegerLiteralContext)ctx); }

    public T translate(BaseVisitor<T> bv, IntegerLiteralContext ctx) {
        bv.updateln(ctx);
        T res = new T(1);
        int chldNo = ctx.getChildCount();
        String val = ((chldNo==2)? "-" : "")
            + ctx.getChild(chldNo-1).getText();
        res.add(val);
        return res;
    }

}
