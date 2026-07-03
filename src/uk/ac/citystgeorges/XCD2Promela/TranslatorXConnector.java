package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;

public class TranslatorXConnector {

    static public T translate(BaseVisitor<T> bv
                              , CompositeOrConnectorDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComposite thisEnv) {
        final var Err = System.err;
        // _vars will be filled in the template
        String _connector_name = compName;
        Map<String,Integer> params = new HashMap<String,Integer>();
        String _params_pushdefs = "";
        String _params_popdefs = "";
        String _params_name_list = "";
        String _params_name_real_list = "";
        // _params_fictional: param list with incremental int values
        // for testing the macros
        String _params_fictional = "";
        for (int i = 0, sz = thisEnv.compConstructs.params.size(); i<sz; ) {
            String param = thisEnv.compConstructs.params.get(i);
            param = Names.paramNameConnector(_connector_name,param);
            params.put(param, ++i);
            _params_pushdefs +=
                "`pushdef(" + param + ",$" + i + ")'dnl\n";
            _params_popdefs +=
                "`popdef(" + param + ")'dnl\n";
            _params_fictional += "," + i;
            _params_name_list += "," + param;
            _params_name_real_list += ",$" + i;
        }
        // delete initial ',', if any
        if (_params_fictional.length() != 0) {
            _params_fictional = _params_fictional.substring(1);
            _params_name_list = _params_name_list.substring(1);
            _params_name_real_list = _params_name_real_list.substring(1);
        }
        _params_fictional = "(" + _params_fictional + ")";
        bv.myassert(thisEnv.compConstructs.vars==null
                    || thisEnv.compConstructs.vars.size()==0
                    , "Connector " + _connector_name
                    + " cannot have variables.");
        LstStr inlineFunctions = thisEnv.compConstructs.inlineFunctionDecls;
        LstStr assertions = thisEnv.compConstructs.translatedAssertions;
        // thisEnv.subcomponents holds the role names
        //
        // thisEnv.subconnectors holds the subconnector names
        LstStr roles = thisEnv.subcomponents;
        LstStr subconnectors = thisEnv.subconnectors;
        Map<String, LstStr> roles2portvarsInParams
            = thisEnv.roles2portvarsInParams;
        Set<String> subconnector_types = new TreeSet<String>();
        for (var subX : subconnectors) {
            IdInfo subXinfo = bv.getIdInfo(thisEnv,subX);
            Err.println("subconnector " + subX
                        + " has type " + subXinfo.variableTypeName);
            subconnector_types.add(subXinfo.variableTypeName);
        }

        String _connector_subconnectors = "";
        // include sub-connector type definitions
        for (var subXtype : subconnector_types)
            if (subXtype != "CONNECTOR_PROCEDURAL"
                && subXtype != "CONNECTOR_ASYNCHRONOUS") {
                String inc = "," + subXtype + ".pml.m4";
                _connector_subconnectors += inc;
            }
        // delete initial ',', if any
        if (_connector_subconnectors.length() != 0)
            _connector_subconnectors = _connector_subconnectors.substring(1);
        // Err.println("Getting " + _connector_subconnectors);

        // framenow.dumpSymbolsRec();
        String _connector_variables = "";
        // String _connector_role_tests = "";
        String role_var_template = Utils.readInputFile
            ("/resources/templates/role_var_sub_template.pml.template");
        for (var _role_name : roles) {
            IdInfo role = bv.getIdInfo(thisEnv, _role_name);
            String roleIterator = "_NAME(__prefixR," + role.arrayIterator + ")";
            // find role's symbolTable and push it!!!
            {                   // otherwise, its id's will be missing.
                SymbolTableComponent roleSTB = null;
                int offset = 0;
                int childrenSz = framenow.children.size();
                while (roleSTB==null
                       && offset<childrenSz) {
                    SymbolTable child = framenow.children.get(offset);
                    if (child.compilationUnitID.equals(_role_name))
                        roleSTB = (SymbolTableComponent) child;
                    else
                        ++offset;
                }
                Utils.myAssertHard(roleSTB!=null
                                   , "Couldn't find symbol table of role "
                                   + _role_name);
                bv.pushSymbolTable(roleSTB);
            }
            System.err.println
                ("YYYYY Role "
                 + _role_name + "'s iterator is " + roleIterator + "\n");
            String roleIndex = "1"; // when roleArSz == bv.sizeOne
            ArraySizeContext roleArSz = role.arraySz;
            Utils.myAssertHard(roleArSz!=null && roleArSz!=bv.sizeZero
                               , "Role "+_role_name+" has a zero array size");
            String _roleArraySize =
                // new TranslatorArraySizeContext()
                //     .translate(bv,roleArSz).get(0)
                bv.visit(roleArSz.arraySz).get(0);
            // System.err.println("YYYYYY roleArSz: "+_roleArraySize+"\n");

            String roleVarInitialisationsUnrolled
                = "/** Initialising role "
                + _role_name + " **/\n"

                + "/* Unrolling initialisations using iterator @"
                + roleIterator + " */\n"
                + "d_step {\n"
                + "_forloop(" + roleIterator
                + ",0," + _roleArraySize + ",dnl\n\n";

            String role_vars = role_var_template
                .replace("$<role_name>", _role_name)
                .replace("$<roleArraySize>",_roleArraySize);
            String _role_variables = "";
            SymbolTableComponent roleST
                = (SymbolTableComponent)
                thisEnv.children.stream()
                .filter(ch -> ch.compilationUnitID.equals(_role_name))
                .findFirst()
                .orElse(null);
            bv.myassert(roleST!=null
                        , "Cannot find symbol table of role " + _role_name);
            LstStr vars = roleST.compConstructs.vars;

            for (String varn : vars) {
                IdInfo varinfo = bv.getIdInfo(roleST, varn);
                String vartype = varinfo.variableTypeName;
                ArraySizeContext varszCtx = varinfo.arraySz;
                String varIterator =
                    "_NAME(__prefixR,"
                    + varn + "," + varinfo.arrayIterator + ")";
            System.err.println
                ("ZZZZZ Variable "
                 + varn + "'s iterator is " + varIterator + "\n");
               String roleVarName =
                    //"_EVALNAME(__prefixR," + varn + ")";
                    varn;
                Utils.myAssertHard(varszCtx!=null
                                   , "Role var " + varn + " ("
                                   + roleVarName + ") has no array size");
                VariableDefaultValueContext varinitCtx = varinfo.initVal;
                String rhs = "0";
                if (varinitCtx!=null) { // rhs is an exp - translate it
                    // rhs = bv.visit(varinitCtx).get(0);
                    rhs = new TranslatorAssignmentExpressionContext()
                        .translate(bv,varinitCtx.assignExpr).get(0);
                }
                String varsz =
                    // // bv.visit(varszCtx).get(0);
                    // new TranslatorArraySizeContext()
                    // .translate(bv,varszCtx).get(0)
                    bv.visit(varszCtx.arraySz).get(0);
                // System.err.println("ZZZZZZ varsz: "+varsz+"\n");
                _role_variables +=
                    "\n\t" + vartype + " " + roleVarName
                    + "[" + varsz + "];dnl\n";
                if (varinfo.has_post)
                    _role_variables +=
                        "\n\t" + vartype + " _post_" + roleVarName
                        + "[" + varsz + "];dnl\n" ;
                roleVarInitialisationsUnrolled
                    += "_forloop(" + varIterator
                    + ",0," + varsz + ",dnl\n\n";
                {
                    roleVarInitialisationsUnrolled
                        += "        "
                        + "__prefixR[" + roleIterator + "]."
                        + roleVarName + "[" + varIterator + "] = "
                        + rhs + ";\n";
                    if (varinfo.has_post) {
                        roleVarInitialisationsUnrolled
                            += "        "
                            + "__prefixR[" + roleIterator + "]._post_"
                            + roleVarName + "[" + varIterator + "] = "
                            + rhs + ";\n";
                    }
                }
                // add the ending parenthesis of var's _forloop
                roleVarInitialisationsUnrolled
                    += ")\n";
            }
            { // + ending parenthesis of role's _forloop & bracket of d_step
                roleVarInitialisationsUnrolled
                    += ")\n}\n";
            }
            _connector_variables += role_vars
                .replace("$<role_variables>",
                         _role_variables)
                .replace("$<role_variable_initialisations>"
                         , roleVarInitialisationsUnrolled);

            // TODO


            // Lastly (!!!) pop role's symbol table (roleSTB)
            bv.popLastSymbolTable();
        }
        // _connector_variables = _connector_variables
        //     .replace("_context", "$1_" + _connector_name)
        //     .replace("_varname", "$2");

        // produce translation
        {
            final var pushdefs = _params_pushdefs;
            final var popdefs = _params_popdefs;
            final var fictionalparams = _params_fictional;
            final var paramnameslist = _params_name_list;
            final var paramnamesreallist = _params_name_real_list;
            final var X_subconnectors = _connector_subconnectors;
            final var X_variables = _connector_variables;
            final Function<String, String> replace_template_arguments
                = (String in) -> {
                String res = in
                .replace("$<connector_name>", _connector_name)
                .replace("$<params_pushdefs>", pushdefs)
                .replace("$<params_popdefs>", popdefs)
                .replace("$<params_fictional>", fictionalparams)
                .replace("$<connector_subconnectors>", X_subconnectors)
                .replace("$<connector_variables>", X_variables)
                ;
                if (paramnameslist.equals(""))
                    res =res
                        .replace(",$<params_name_list>", "")
                        .replace(",$<params_name_real_list>", "");
                else
                    res = res
                        .replace("$<params_name_list>", paramnameslist)
                        .replace("$<params_name_real_list>", paramnamesreallist);
                return res;
            };
            Utils.withInputAndFileToWrite
                ("/resources/templates/connector.pml.template"
                 , "CONNECTOR_TYPE_" + _connector_name + ".pml.m4"
                 , replace_template_arguments);
            Utils.withInputAndFileToWrite
                ("/resources/templates/z-testing-role.m4"
                 , "z-testing-role.m4"
                 , replace_template_arguments);
        }

        return new T();
    }

}
