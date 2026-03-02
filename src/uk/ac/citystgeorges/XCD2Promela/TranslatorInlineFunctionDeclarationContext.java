package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorInlineFunctionDeclarationContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (InlineFunctionDeclarationContext)ctx); }
    public T translate(BaseVisitor<T> bv, InlineFunctionDeclarationContext ctx) {
        bv.updateln(ctx);
        T res = new T(2);
        String prms = "";

        var params = ctx.params;
        if (params.par_pre != null) {
            prms += params.par_pre.id.getText();
            if (params.pars!=null)
                for (var par : params.pars)
                    prms += ", " + par.id.getText();
        }

        var def = bv.visit(ctx.inline);

        res.add(prms);
        res.add(" ( " + def.get(0) + " )");
        return res;
    }
}
