package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorAssertDeclarationContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (AssertDeclarationContext)ctx); }

    public T translate(BaseVisitor<T> bv, AssertDeclarationContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        s = bv.visit(ctx.cond).get(0); // The assertion
        // Can appear inside: role, component
        var framenow = bv.frameNow();
        bv.myassert(framenow.type == XCD_type.componentt
                    || framenow.type == XCD_type.rolet
                    , "Assertion appears inside non-supported construct "
                    + framenow.type);
        if (framenow.type==XCD_type.componentt)
            ((ContextInfoComp) framenow)
                .compConstructs.translatedAssertions.add(s);
        else if (framenow.type==XCD_type.rolet)
            ((ContextInfoConnRole) framenow)
                .compConstructs.translatedAssertions.add(s);

        return res;
    }

}
