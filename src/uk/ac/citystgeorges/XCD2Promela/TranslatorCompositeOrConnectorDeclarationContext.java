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

public class TranslatorCompositeOrConnectorDeclarationContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (CompositeOrConnectorDeclarationContext)ctx); }
    public T translate(BaseVisitor<T> bv, CompositeOrConnectorDeclarationContext ctx) {
        // bv.mywarning
        //     ("\n***Called TranslatorCompositeOrConnectorDeclarationContext translate!***");
        String compName = ctx.id.getText();
        int thisStruct = ctx.tp.getType();
        T res = new T();
        SymbolTable framenow = (bv.symbolTableNow());
        SymbolTableComposite thisEnv
            = (SymbolTableComposite) framenow;
        if (thisStruct==XCDParser.TK_COMPOSITE
            || thisStruct==XCDParser.TK_COMPONENT)
            return TranslatorXComposite
                .translate(bv, ctx, compName, framenow, thisEnv);
        else {                  // I'm a connector
            bv.myassert(thisStruct==XCDParser.TK_CONNECTOR, "Not a connector!");
            bv.mywarning("TODO: Need to translate connectors!");
            return TranslatorXConnector
                .translate(bv, ctx, compName, framenow, thisEnv);
        }
    }
}
