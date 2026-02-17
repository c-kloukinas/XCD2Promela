package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TranslatorComponentOrRoleDeclarationContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ComponentOrRoleDeclarationContext)ctx); }
    public T translate(BaseVisitor<T> bv, ComponentOrRoleDeclarationContext ctx) {
        // bv.mywarning
        //     ("\n***Called TranslatorComponentOrRoleDeclarationContext translate!***");
        String compName = ctx.id.getText();
        int thisStruct = ctx.struct.getType();
        T res = new T();
        SymbolTable framenow = (bv.symbolTableNow());

        if (thisStruct==XCDParser.TK_COMPONENT) {
            SymbolTableComponent thisEnv
                = (SymbolTableComponent) framenow;

            return TranslatorXComponent
                .translate(bv, ctx, compName, framenow, thisEnv);
        } else {                // I'm a role
            bv.myassert(thisStruct==XCDParser.TK_ROLE, "Not a role!");
            bv.mywarning("TODO: Need to translate roles!");
            SymbolTableComponent thisEnv
                = (SymbolTableComponent) framenow;

            return TranslatorXRole
                .translate(bv, ctx, compName, framenow, thisEnv);
        }
    }
}
