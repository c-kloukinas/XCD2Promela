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
        s = "assert(" + bv.visit(ctx.cond).get(0) + ");"; // The assertion
        // Can appear inside: methodt actions
        var framenow = bv.symbolTableNow();
        bv.myassert(// framenow.type == XCD_type.compositet
                    // || framenow.type == XCD_type.connectort
                    // || framenow.type == XCD_type.componentt
                    // || framenow.type == XCD_type.rolet
                    // ||
                    framenow.type == XCD_type.methodt
                    || framenow.type == XCD_type.eventt
                    , "Assertions can appear only inside method/event actions"
                    + " - current symbol table is of type "
                    + framenow.type);
        res.add(s);
        return res;
    }

}
