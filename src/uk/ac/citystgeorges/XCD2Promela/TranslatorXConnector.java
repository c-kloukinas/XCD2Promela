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
                "pushdef(" + param + ",$" + i + ")dnl\n";
            _params_popdefs +=
                "popdef(" + param + ")dnl\n";
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
        final String role_var_template = Utils.readInputFile
            ("/resources/templates/role_var_sub_template.pml.template");
        final String role_var_port_action_template = Utils.readInputFile
            ("/resources/templates/role_var_port_action_sub_template.pml.template");
        String _connector_role_port_action_guards = "";
        for (var _role_name : roles) {
            IdInfo role = bv.getIdInfo(thisEnv, _role_name);
            String roleIterator = "_NAME(__prefixR," + role.arrayIterator + ")";
            // Find role's symbolTable and push it!!! Otherwise, its
            // IDs will be missing.
            SymbolTableComponent roleST
                = (SymbolTableComponent) role.getSB(); {
                bv.pushSymbolTable(roleST); }
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
                + ",0,_CAT(" + _roleArraySize + "),dnl\n\n";

            String role_vars = role_var_template
                .replace("$<role_name>", _role_name)
                .replace("$<roleArraySize>","_CAT("+_roleArraySize+")");
            String _role_variables = "";
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
                    + ",0,_CAT(" + varsz + "),dnl\n\n";
                {
                    roleVarInitialisationsUnrolled
                        += "        "
                        + "__prefixRP[" + roleIterator + "]."
                        + roleVarName + "[" + varIterator + "] = "
                        + rhs + ";\n";
                    if (varinfo.has_post) {
                        roleVarInitialisationsUnrolled
                            += "        "
                            + "__prefixRP[" + roleIterator + "]._post_"
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
            //
            // port/action guards & port/action require/ensures pairs
            //
            _connector_role_port_action_guards = ""; // reset guards
            final LstStr all_ports = roleST.all_element_ports();
            for (String port : all_ports) {
                // find port's symbol table
                IdInfo portInfo = bv.getIdInfo(port);
                SymbolTablePort portST
                    = (SymbolTablePort) portInfo.getSB(); {
                    bv.pushSymbolTable(portST); }
                final LstStr all_actions = portST.all_port_actions();
                for (String action : all_actions) {
                    IdInfo actionInfo = bv.getIdInfo(action);
                    SymbolTableMethod actionST
                        = (SymbolTableMethod) actionInfo.getSB();
                    /* _port_action_guard is the DISJUNCTION of all
                       x_constrsAllows guard cases. It'll be CONJOINED
                       with the other interaction constraints. */
                    String _port_action_guard = "";
                    // role action constraints
                    LstStr x_constrsAllows
                        = Utils.nonNullCopy(actionST.methodStructure
                                            .x_constraintsAllows
                                            , LstStr.class);
                    String _port_action_ensures = "";
                    LstStr x_constrsEnsures
                        = Utils.nonNullCopy(actionST.methodStructure
                                            .x_constraintsEnsures
                                            , LstStr.class);
                    /* non-role action constraints - all checked to be
                       null during AST visiting */
                    // LstStr x_constrsAccepts
                    //     = actionST.methodStructure.x_constraintsAccepts;
                    // LstStr x_constrsWaits
                    //     = actionST.methodStructure.x_constraintsWaits;
                    // //
                    // LstStr f_constrsWhen
                    //     = actionST.methodStructure.f_constraintsWhen;
                    // LstStr f_constrsWEnsures
                    //     = actionST.methodStructure.f_constraintsWEnsures;
                    // LstStr f_constrsRequires
                    //     = actionST.methodStructure.f_constraintsRequires;
                    // LstStr f_constrsREnsures
                    //     = actionST.methodStructure.f_constraintsREnsures;
                    /* Not necessarily true, some role port actions
                       only listed to allow inter-role port binding */
                    // // Should have some...
                    // Utils.myAssertHard
                    //     (x_constrsAllows!=null
                    //      || x_constrsEnsures!=null
                    //      , "Role " + _role_name
                    //      + ", port " + port
                    //      + ", action " + action
                    //   + " has no Allows/Ensures constraints");

                    Utils.myAssertHard
                        (x_constrsAllows.size() == x_constrsEnsures.size()
                         , "Role " + _role_name
                         + ", port " + port
                         + ", action " + action
                         + " has " + x_constrsAllows.size() + " constructs but "
                         + x_constrsEnsures.size() + " constructs");
                    if (0 < x_constrsAllows.size()) {
                        String prefix[] = { "(", " || (" };
                        for (int sz=x_constrsAllows.size(), prfxi=0, i=0;
                             i<sz;
                             prfxi = 1, ++i) {
                            var x_allows = x_constrsAllows.get(i);
                            _port_action_guard +=
                                prefix[prfxi] + x_allows + ")";
                            var x_ensures = x_constrsEnsures.get(i);
                            _port_action_ensures +=
                                " :: (" + x_allows + ") -> " + x_ensures + "; ";
                        }
                        _port_action_guard
                            = "(" + _port_action_guard + ")";
                        _port_action_ensures
                            = "if" + _port_action_ensures
                            + (
                               " :: else -> "
                               + "assert(false); "
                               +  "/* incomplete action guards: r/p/a = "
                               + _role_name + "/" + port + "/" + action
                               + " */ fi; "
                               );
                    } else {
                        _port_action_guard = "true";
                    }

                    _connector_role_port_action_guards +=
                        role_var_port_action_template
                        .replace("$<portName>", port)
                        .replace("$<actionName>", action)
                        .replace("$<port_action_guard>",_port_action_guard)
                        .replace("$<port_action_ensures>",_port_action_ensures);

                    // {
                    //     bv.mywarning("action " + action
                    //                  + "\n\tguards are "
                    //                  + _port_action_guard
                    //                  + "\n\tguard-ensure pairs are "
                    //                  + _port_action_ensures
                    //                  );
                    // }
                    //
                }


                // Lastly (!!!) pop port's symbol table (portST)
                { bv.popLastSymbolTable(portST); }
            }
            _connector_variables = _connector_variables
                .replace("$<connector_role_port_action_guards>"
                         , _connector_role_port_action_guards);
            // Lastly (!!!) pop role's symbol table (roleST)
            { bv.popLastSymbolTable(roleST); }
        }
        // _connector_variables = _connector_variables
        //     .replace("_context", "$1_" + _connector_name)
        //     .replace("_varname", "$2");

        // produce translation
        {
            final var paramnameslist = _params_name_list;
            final var paramnamesreallist = _params_name_real_list;
            final var X_subconnectors = _connector_subconnectors;
            final var X_variables = _connector_variables;
            final var pushdefs = _params_pushdefs;
            final var popdefs = _params_popdefs;
            final var fictionalparams = _params_fictional;
            final Function<String, String> replace_template_arguments
                = (String in) -> {
                String res = in
                .replace("$<connector_name>", _connector_name)
                .replace("$<connector_subconnectors>", X_subconnectors)
                .replace("$<connector_variables>", X_variables)
                ;
                // do the following last! (they appear inside X_variables)
                res = res
                .replace("$<params_pushdefs>", pushdefs)
                .replace("$<params_popdefs>", popdefs)
                .replace("$<params_fictional>", fictionalparams)
                ;
                res = res       // once more, with extra feeling!
                .replace("$<connector_name>", _connector_name)
                .replace("$<connector_subconnectors>", X_subconnectors)
                .replace("$<connector_variables>", X_variables)
                ;
                if (paramnameslist.equals(""))
                    res = res
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
