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
        boolean amIaComponentp = (thisStruct==XCDParser.TK_COMPONENT);
        T res = new T();
        SymbolTable framenow = (bv.symbolTableNow());
        SymbolTableComponent thisEnv
            = (SymbolTableComponent) framenow;
        // Both components and roles get a header file - components
        // also get an instance file.
        String header = "";

        // Collect parameters
        LstStr argList = thisEnv.compConstructs.params;
        bv.mywarning("TODO: missing code for comp parameters");
        for (String param : argList) {
            IdInfo paramInfo = bv.getIdInfo(thisEnv, param);
            bv.mywarning("Param " + param + " has translation:");
            if (paramInfo.translation.size()!=0)
                for (var v : paramInfo.translation)
                    bv.mywarning("\t" + v);
            else bv.mywarning("\tNO TRANSLATION!");
        }

        String outComponentName = amIaComponentp
            ? Names.componentHeaderName(compName)
            : Names.roleName(framenow.parent.compilationUnitID, compName);

        String outParams = "";
        int prmsz = (argList!=null) ? argList.size() : 0;
        if (prmsz!=0) {  // these seem to be ignored by the translator
            outParams
                += "/* Parameters ignored, passed through macros */ /* "
                + argList.get(0);
            if (prmsz>1)
                for (int i=1; i<prmsz; ++i)
                    outParams += ","+argList.get(i);
            outParams += " */";
        }

        // add local enums here
        String outEnums = "";
        Map<String,IdInfo> compMap = thisEnv.map;
        for (var key : compMap.keySet()) {
            var value = compMap.get(key);
            if (value.type == XCD_type.enumt) {
                bv.mywarning
                    ("UNCHARTED: There's a local enum definition here! "
                     + value.variableTypeName);
                outEnums += value.variableTypeName;
            }
        }

        // Function<String
        //     , Function<String
        //     , Function<String, String>>>
        //     res = c -> p -> m -> Names.varNameRESULT(c, p, m);
        // Collect enums
        java.util.function.BiFunction<String, SymbolTableComponent, String> def
            = (String what, SymbolTableComponent theEnv) -> {
            String out = "";
            LstStr enums = theEnv.compConstructs.enums;
            for (String name : enums) {
                IdInfo theEnum = bv.getIdInfo(name);
                String enumFullName = theEnum.translation.get(0);
                // Add include directive in the current header
                out += "#include \"" + what + enumFullName + ".h\"\n";
            }
            return out;
        };
        String outAll_enums = def.apply("TYPE_", thisEnv);
        // Collect typedefs
        java.util.function.BiFunction<String, SymbolTableComponent, String> def2
            = (String what, SymbolTableComponent theEnv) -> {
            String out = "";
            LstStr typedefs = theEnv.compConstructs.typedefs;
            for (String name : typedefs) {
                // Add include directive in the current header
                out += "#include \"" + what + name + ".h\"\n";
            }
            return out;
        };
        String outAll_typedefs = def2.apply("TYPE_", thisEnv);


        if (thisStruct==XCDParser.TK_COMPONENT)
            return TranslatorXComponent
                .translate(bv, ctx, compName, framenow, thisEnv

                           // pass computed parts and use template for
                           // the translation
                           , header
                           , outComponentName
                           , outParams
                           , outEnums);
        else {                  // I'm a role
            bv.myassert(thisStruct==XCDParser.TK_ROLE, "Not a role!");
            bv.mywarning("TODO: Need to translate roles!");
            return TranslatorXRole
                .translate(bv, ctx, compName, framenow, thisEnv);
        }
    }
}
