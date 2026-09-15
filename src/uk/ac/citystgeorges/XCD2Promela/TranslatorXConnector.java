package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;

public class TranslatorXConnector {
    // first three connector macro args are the context, instance
    // name, and instance size - skip them.
    static final int xArgShift = 3;
    static final java.io.PrintStream Err = System.err;

    static public T translate(BaseVisitor<T> bv
                              , CompositeOrConnectorDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComposite thisEnv) {
        // _vars will be filled in the template
        String _x00_name = compName;
        IdInfo connectorIdInfo = bv.getIdInfo(thisEnv, _x00_name);
        String _connector_iterator = connectorIdInfo.arrayIterator;
        String _x02_param_defs
            = TranslatorCompositeOrConnectorDeclarationContext.
            collectParamDefinitions(xArgShift, true, thisEnv);

        bv.myassert(thisEnv.compConstructs.vars==null
                    || thisEnv.compConstructs.vars.size()==0
                    , "Connector " + _x00_name
                    + " cannot have variables.");
        LstStr inlineFunctions = thisEnv.compConstructs.inlineFunctionDecls;
        LstStr assertions = thisEnv.compConstructs.translatedAssertions;
        // thisEnv.subcomponents holds the role names
        //
        // thisEnv.subconnectors holds the subconnector names
        LstStr roles = thisEnv.rolesAsOrderedInParams;

        // framenow.dumpSymbolsRec();
        String _x06_variables = "";
        // // String role_var_template = Utils.readInputFile
        // //     (XCD2Promela.resourceTemplates
        // //      + "role_var_sub_template.pml.template");
        // String role_var_port_template = Utils.readInputFile
        //     (XCD2Promela.resourceTemplates
        //      + "role_var_port_sub_template.pml.template");
        //
        // Initialisation of extra guards
        //
        String _x04_action_extra_guards_initialisation = "";
        String _x05_subaction_guards = "";
        String _x07_port_action_guards_ensures = "";
        {                       // Looping on Roles
            int _rlIndex = 0;
            for (var _role_name : roles) {
                ++_rlIndex;         // m4 arguments start at $1
                IdInfo role = bv.getIdInfo(thisEnv, _role_name);
                String _roleIterator
                    = "_NAME(__prefixR," + role.arrayIterator + ")";
                // Find role's symbolTable and push it!!! Otherwise, its
                // IDs will be missing.
                SymbolTableComponent roleST
                    = (SymbolTableComponent) role.getSB(); {
                    bv.pushSymbolTable(roleST); }
                // System.err.println
                //     ("YYYYY Role "
                //      + _role_name
                //   + "'s iterator is "
                //   + _roleIterator + "\n");
                String roleIndex = "1"; // when roleArSz == bv.sizeOne
                String _roleArraySize = role.arraySizeExpr;
                Utils.myAssertHard(! _roleArraySize.equals("0")
                                   , "Role " + _role_name
                                   + " has a zero array size");
                String role_vars = Utils.readInputFile
                    (XCD2Promela.resourceTemplates
                     + "role_var_sub_template.pml.template");

                String _r02_variables = getRoleVarDefs(bv, thisEnv, roleST);
                String _r03_variable_initialisations =
                    "/* Unrolling role data initialisations using"
                    + " iterator @_NAME(__prefixR,Iterator)"
                    + " up to _EVALNAME(__prefixR,size)"
                    + " (or should it be up to"
                    + " _EVALNAME(__prefixR,sizeTotal)?)\n*/\n"
                    // actual loop header:
                    + "_forloop(_EVALNAME(__prefixR,Iterator)"
                    + ",0,_EVALNAME(__prefixR,size),dnl\n"
                    + getRoleVarInits(bv, thisEnv
                                      , _role_name
                                      , _rlIndex
                                      , _roleIterator
                                      , roleST
                                      , roleIndex
                                      , _roleArraySize);

                String _r04_ports = "";
                {               // Looping on Ports
                    int _portIndex = 0;
                    Map<String, LstStr> roles2portvarsInParams
                        = thisEnv.roles2portvarsInParams;
                    LstStr all_ports
                        = roles2portvarsInParams.get(_role_name);
                    // IMPORTANT - process ports in the order used in
                    // the parameters!
                    for (String port : all_ports) {
                        ++_portIndex;
                        _r04_ports += getRolePortData(bv, thisEnv
                                                      , _role_name
                                                      , port
                                                      , _portIndex);
                        {       // Looping on Actions-
                            IdInfo portInfo = bv.getIdInfo(port);
                            SymbolTablePort portST
                                = (SymbolTablePort) portInfo.getSB(); {
                                bv.pushSymbolTable(portST); }

                            LstStr all_actions
                                = portST.all_port_actions();
                            //
                            // port/action guards & port/action
                            // require/ensures pairs
                            //
                            // Looping on Actions -1-
                            //
                            // Initialise (empty) extra guards/ensures
                            for (String action : all_actions) {
                                _x04_action_extra_guards_initialisation
                                    +=
                                    TranslatorCompositeOrConnectorDeclarationContext
                                    .getActionExtraGuardsInitialisation
                                    (_portIndex, action);
                            }
                            // Looping on Actions -2-
                            //
                            // Actual extra guards/ensures
                            for (String action : all_actions) {
                                _x07_port_action_guards_ensures
                                    +=
                                    TranslatorCompositeOrConnectorDeclarationContext
                                    .getPortActionGuardsEnsures
                                    (bv, thisEnv
                                     , _role_name
                                     , port
                                     , _portIndex
                                     , action
                                    );
                            }
                            // Lastly (!!!) pop port's symbol table
                            // (portST)
                            { bv.popLastSymbolTable(portST); }
                        }
                    }
                }
                // Declaration of the data that the sub-role instances
                // need, to be inserted into the role's typedef.
                String _r01_subExtraRoleData
                    = TranslatorCompositeOrConnectorDeclarationContext
                    .getSubRoleData(bv
                                    , thisEnv
                                    , _role_name
                                   )
                    .replace("$<role_name>", _role_name)
                    .replace("$<rlIndex>", ""+_rlIndex)
                    .replace("$<roleArraySize>",_roleArraySize)
                    .replace("$<roleIterator>",_roleIterator)
                    .replace("$<connector_name>", _x00_name);

                _x04_action_extra_guards_initialisation
                    = _x04_action_extra_guards_initialisation
                    .replace("$<rlIndex>", ""+_rlIndex);
                _x05_subaction_guards
                    += TranslatorCompositeOrConnectorDeclarationContext
                    .getSubRoleActionGuards(bv
                                            , thisEnv
                                            , _role_name)
                    .replace("$<role_name>", _role_name)
                    .replace("$<rlIndex>", ""+_rlIndex)
                    .replace("$<roleArraySize>",_roleArraySize)
                    .replace("$<roleIterator>",_roleIterator)
                    .replace("$<connector_name>", _x00_name);
                _x07_port_action_guards_ensures
                    = _x07_port_action_guards_ensures
                    .replace("$<role_name>", _role_name)
                    .replace("$<rlIndex>", ""+_rlIndex);
                _x06_variables += role_vars
                    .replace("$<role01_subExtraRoleData>",
                             _r01_subExtraRoleData)
                    .replace("$<role02_variables>"
                             , _r02_variables)
                    .replace("$<role03_variable_initialisations>"
                             , _r03_variable_initialisations)
                    .replace("$<role04_ports>", _r04_ports)
                    // high-level role info below last (previous
                    // replacements may be using them)
                    .replace("$<role_name>", _role_name)
                    .replace("$<rlIndex>", ""+_rlIndex)
                    .replace("$<roleArraySize>",_roleArraySize)
                    .replace("$<roleIterator>",_roleIterator);
                // Lastly (!!!) pop role's symbol table (roleST)
                { bv.popLastSymbolTable(roleST); }
            }
        }

        // produce translation
        {
            var x01_subconnectors
                = TranslatorCompositeOrConnectorDeclarationContext.
                getSubconnectorIncludes(thisEnv);
            var x02_param_defs = _x02_param_defs;
            var x03_subconnectors_called
                = TranslatorCompositeOrConnectorDeclarationContext.
                getSubconnectorCalls(bv, thisEnv);
            var x04_action_extra_guards_initialisation
                = _x04_action_extra_guards_initialisation;
            var x05_subaction_guards = _x05_subaction_guards;
            var x06_variables = _x06_variables;
            var x07_port_action_guards_ensures
                = _x07_port_action_guards_ensures;
            Function<String, String> replace_template_arguments
                = (String in) -> {
                String res = in
                //// replacements in reverse order of appearance
                .replace("$<connector07_port_action_guards_ensures>"
                         , x07_port_action_guards_ensures)
                .replace("$<connector06_variables>", x06_variables)
                .replace("$<connector05_subaction_guards>"
                         , x05_subaction_guards)
                .replace("$<connector04_action_extra_guards_init>"
                         , x04_action_extra_guards_initialisation)
                .replace("$<connector03_subconnectors_called>"
                         , x03_subconnectors_called)
                // high-level connector info below last (previous
                // replacements may be using them)
                .replace("$<connector_iterator>", _connector_iterator)
                .replace("$<connector02_param_defs>", x02_param_defs)
                .replace("$<connector01_subconnectors>"
                         , x01_subconnectors)
                .replace("$<connector_name>", _x00_name)
                ;
                return res;
            };
            Utils.withInputAndFileToWrite
                (XCD2Promela.resourceTemplates + "connector.pml.template"
                 , "CONNECTOR_TYPE_" + _x00_name + ".pml.m4"
                 , replace_template_arguments);
            Utils.withInputAndFileToWrite
                (XCD2Promela.resourceTemplates + "z-testing-role.m4"
                 , "z-testing-role.m4"
                 , replace_template_arguments);
        }

        return new T();
    }

    static private
        String getRoleVarInits(BaseVisitor<T> bv
                               , SymbolTableComposite thisEnv
                               , String _role_name
                               , int _rlIndex
                               , String _roleIterator
                               , SymbolTableComponent roleST
                               , String roleIndex
                               , String _roleArraySize) {
        String _r03_variable_initialisations = "";
        LstStr vars = roleST.compConstructs.vars;

        for (String varn : vars) {
            IdInfo varinfo = bv.getIdInfo(roleST, varn);
            String vartype = varinfo.variableTypeName;
            String varsz = varinfo.arraySizeExpr;
            if (varsz.equals(""))
                varinfo.arraySizeExpr = varsz = "1";
            String varIterator =
                "_NAME(__prefixR,"
                + varn + "," + varinfo.arrayIterator + ")";
            String roleVarName = varn;
            VariableDefaultValueContext varinitCtx = varinfo.initVal;
            String rhs = "0";
            if (varinitCtx!=null) { // rhs is an exp - translate it
                rhs = new TranslatorAssignmentExpressionContext()
                    .translate(bv,varinitCtx.assignExpr).get(0);
            }
            _r03_variable_initialisations
                += "/* Unrolling role variable initialisations "
                + "using iterator @" + varIterator
                + " up to " + varsz + " */\n"
                // actual loop header:
                + "_forloop(" + varIterator
                + ",0," + varsz + ",dnl\n";
            {
                _r03_variable_initialisations
                    += "    "
                    + "_NAME(__prefixR,InstanceAtOffset)("
                    + _roleIterator + ")."
                    + roleVarName + "[" + varIterator + "] = "
                    + rhs + ";";
                if (varinfo.has_post) {
                    _r03_variable_initialisations
                        += "\n    "
                        + "_NAME(__prefixR,InstanceAtOffset)("
                        + _roleIterator + ")._post_"
                        + roleVarName + "[" + varIterator + "] = "
                        + rhs + ";";
                }
                _r03_variable_initialisations += "`'dnl\n";
            }
            // add the ending parenthesis of var's _forloop
            _r03_variable_initialisations
                += ")\n";
        }
        { // + ending parenthesis of role's _forloop
            _r03_variable_initialisations += ")dnl\n";
        }

        return _r03_variable_initialisations;
    }

    static private
        String getRoleVarDefs(BaseVisitor<T> bv
                              , SymbolTableComposite thisEnv
                              , SymbolTableComponent roleST) {
        String _r02_variables = "";
        LstStr vars = roleST.compConstructs.vars;

        for (String varn : vars) {
            IdInfo varinfo = bv.getIdInfo(roleST, varn);
            String vartype = varinfo.variableTypeName;
            String varsz = varinfo.arraySizeExpr;
            if (varsz.equals(""))
                varinfo.arraySizeExpr = varsz = "1";
            String roleVarName = varn;
            _r02_variables +=
                "\n\t" + vartype + " " + roleVarName
                + "[" + varsz + "];dnl\n";
            if (varinfo.has_post)
                _r02_variables +=
                    "\n\t" + vartype + " _post_" + roleVarName
                    + "[" + varsz + "];dnl\n" ;
        }
        return _r02_variables;
    }

    static private
        String getRolePortData(BaseVisitor<T> bv
                               , SymbolTableComposite thisEnv
                               , String _role_name
                               , String port
                               , int _portIndex
                              ) {
        String _r04_ports = "";
        String role_var_port_template = Utils.readInputFile
            (XCD2Promela.resourceTemplates
             + "role_var_port_sub_template.pml.template");
        IdInfo portInfo = bv.getIdInfo(port);
        String _portArraySize = portInfo.arraySizeExpr;
        String _portKind = "UNKNOWN";
        {
            XCD_type portKind = portInfo.type;
            switch (portKind) {
            case XCD_type.emittert, XCD_type.emittervart ->
                _portKind = "emitter";
            case XCD_type.consumert, XCD_type.consumervart ->
                _portKind = "consumer";
            case XCD_type.requiredt, XCD_type.requiredvart ->
                _portKind = "required";
            case XCD_type.providedt, XCD_type.providedvart ->
                _portKind = "provided";
            default ->
                Utils.myAssertHard(false
                                   , "Unknown port type for role/port "
                                   + _role_name + "/" + port);
            }
        }
        SymbolTablePort portST
            = (SymbolTablePort) portInfo.getSB(); {
            bv.pushSymbolTable(portST); }
        LstStr all_actions = portST.all_port_actions();
        int _actionsTotal = all_actions.size();
        _r04_ports +=
            role_var_port_template
            // high-level port info below last (previous
            // replacements may be using them)
            .replace("$<portName>", port)
            .replace("$<portIndex>", ""+_portIndex)
            .replace("$<portArraySize>", _portArraySize)
            .replace("$<portKind>", _portKind)
            .replace("$<actionsTotal>", ""+_actionsTotal);

        // Lastly (!!!) pop port's symbol table (portST)
        { bv.popLastSymbolTable(portST); }
        // }

        return _r04_ports;
    }
}
