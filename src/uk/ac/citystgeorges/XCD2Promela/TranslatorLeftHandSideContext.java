package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorLeftHandSideContext
    extends TranslatorPrimaryContext
    implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (LeftHandSideContext)ctx); }
    public T translate(BaseVisitor<T> bv, LeftHandSideContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        if (ctx.name!=null)
            s = translate_ID(bv, ctx.name.getText());
        else if (ctx.res!=null)
            s = bv.component_variable_result("ACTIONNOTKNOWN");
        else if (ctx.exc!=null)
            s = bv.component_variable_exception("ACTIONNOTKNOWN");
        else
            s = bv.visit(ctx.arrayAcc).get(0);
        res.add(s);
        return res;
    }
}
