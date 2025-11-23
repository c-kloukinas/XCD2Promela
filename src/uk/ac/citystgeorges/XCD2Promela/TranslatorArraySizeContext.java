package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorArraySizeContext
    extends TranslatorPrimaryContext // hack to reuse ID's translation
    implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ArraySizeContext)ctx); }

    public T translate(BaseVisitor<T> bv, ArraySizeContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        if (ctx.constant!=null)
            s = ctx.constant.getText();
        else
            s = translate_ID(bv, ctx.config_par.getText());

        res.add(s);
        return res;
    }

}
