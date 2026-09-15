package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
        String compName = (ctx.id!=null
                           ? ctx.id.getText()
                           : (ctx.async!=null
                              ? "async" : "proc"));
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

    /*
     * argShift: when do actual parameters start?
     */
    static public
        String collectParamDefinitions(int argShift
                                       , boolean amIaConnectorP
                                       , SymbolTableComposite thisEnv) {
        String _x02_param_defs = "";
        Map<String,Integer> params = new HashMap<String,Integer>();
        // String _params_name_list = "";
        // String _params_name_real_list = "";
        // // _params_fictional: param list with incremental int values
        // // for testing the macros
        // String _params_fictional = "";
        for (int i = 0, sz = thisEnv.compConstructs.params.size()
                 ; i<sz
                 ; ) {
            String param = thisEnv.compConstructs.params.get(i);
            // param = Names.paramNameConnector(_x00_name,param);
            param = "_NAME(__prefix"
                + (amIaConnectorP ? 'X' : 'C')
                + ",PARAM_" + param + ")";
            params.put(param, ++i);
            // "+ argShift": first three connector macro args are the
            // context, instance name, and instance size - skip them.
            int argNo = i + argShift;
            _x02_param_defs +=
                // Evaluate connector parameters, so they're values
                // instead of expressions.
                "define(" + param + ",`eval($" + argNo + ")')dnl\n";
            // _params_fictional += ((i==1)?"":",") + i;
            // _params_name_list += ((i==1)?"":",") + param;
            // _params_name_real_list += ((i==1)?"$":",$") + i;
        }
        // bv.mywarning("_params_fictional = " + _params_fictional
        //              + "\n_params_name_list = " + _params_name_list
        //              + "\n_params_name_real_list = " + _params_name_real_list);
        return _x02_param_defs;
        // var paramnameslist = _params_name_list;
        // var paramnamesreallist = _params_name_real_list;
        // var fictionalparams = _params_fictional;

        // {
        //     boolean replaceCommaToo = false;
        //     if (paramnameslist.equals("")) {
        //         replaceCommaToo = true;
        //         Utils.myAssertHard
        //             (paramnamesreallist.equals("")
        //              , "Real list should be empty but it is \""
        //              + paramnamesreallist + "\"");
        //     }
        //     res = res.replace((replaceCommaToo ? "," : "")
        //                       + "$<params_name_list>"
        //                       , paramnameslist)
        //              .replace((replaceCommaToo ? "," : "")
        //                       + "$<params_name_real_list>"
        //                       , paramnamesreallist);
        // }
    }

    /*
     * NOTE: __subConnCtx must be defined!!!
     */
    static public
        String getSubconnectorCalls(BaseVisitor<T> bv
                                    , SymbolTableComposite thisEnv) {
        String _x03_subconnectors_called = "";
        LstStr subconnectors = thisEnv.subconnectors;
        for (var subX : subconnectors) {
            IdInfo subXinfo = bv.getIdInfo(thisEnv,subX);
            String subXtype = subXinfo.variableTypeName;
            // Err.println("subconnector " + subX
            //             + " has type " + subXtype);
            /// Get the call info, to get the exprArgs, etc.
            CallInfoX subconnCallInfo = subXinfo.callInfoX;
            Utils.myAssertHard(subconnCallInfo!=null
                               , "No CallInfo for " + subX);
            var xInstance = subconnCallInfo.connectorInstance;
            var xInstanceSize = subconnCallInfo.connectorInstanceSize;
            var exprArgsIfAny = subconnCallInfo.expressionArgs;
            exprArgsIfAny = ( exprArgsIfAny.equals("")
                              ? ""
                              : ("," + exprArgsIfAny) );
            _x03_subconnectors_called
                += "_" + subXtype
                + ( "(__subConnCtx"
                    + "," + xInstance
                    + "," + xInstanceSize
                    + exprArgsIfAny
                    + ")dnl sub-connector call\n" );
        }
        return _x03_subconnectors_called;
    }

    static public
        String getSubconnectorIncludes(SymbolTableComposite thisEnv) {
        String _x01_subconnectors = "";
        LstStr subconnectors = thisEnv.subconnectors;
        Set<String> subconnector_types = thisEnv.subconnector_types;
        {
            boolean is1stIteration = true;
            // include sub-connector type definitions
            for (var subXtype : subconnector_types) {
                String inc
                    = (is1stIteration ? "" : ",")
                    + "CONNECTOR_TYPE_" + subXtype
                    + ".pml.m4";
                _x01_subconnectors += inc;
                is1stIteration = false;
            }
        }
        // Err.println("Getting " + _x01_subconnectors);
        return _x01_subconnectors;
    }

    /// Declaration of the data that the sub-role instances need, to
    /// be inserted into the element's typedef.
    static public
        String getSubRoleData(BaseVisitor<T> bv
                              , SymbolTableComposite thisEnv
                              , String _role_name
                             ) {
        String _r01_subExtraRoleData = "";
        /// for each sub-connector role that this role is bound:
        ///
        // find the sub-connector info in ST's elementBindings.
        ElementBindings bindingsOfRole
            = thisEnv.elementBindings
            .getOrDefault(_role_name, new ElementBindings());

        for (ElementInfo binding : bindingsOfRole.bindings) {
            String _subConnType = binding.connectorTypeName;
            String _subConnVarName = binding.connectorInstName;
            String _subConnVarSize = binding.connectorSizeExpr;
            String _subConnRoleAssumedIndex
                = binding.elementIndex;
            String _subConnRoleFullName
                =
                "__roleId(__subConnCtx"
                + "," + _subConnType
                + "," + _subConnVarName
                + "," + _subConnRoleAssumedIndex + ")";
            String subConnRoleAssumedSize
                = binding.elementSizeExpr;
            String extraRoleData = Utils.readInputFile
                (XCD2Promela.resourceTemplates
                 + "subconnector_role_var_sub_template.pml.template")

                .replace("$<subConnType>",    _subConnType)
                .replace("$<subConnVarName>", _subConnVarName)
                .replace("$<subConnVarSize>", _subConnVarSize)

                .replace("$<subConnRoleFullName>", _subConnRoleFullName)
                .replace("$<subConnRoleAssumedIndex>", _subConnRoleAssumedIndex);
            _r01_subExtraRoleData += extraRoleData;

            /// (1) add the sub-role's typedef to this role's typedef
            ///
            /// sub-connector var declaration: `_subConnVarDecl'
            ///
            /// _NAME(__roleId(_subConnCtx
            ///                ,$<subConnType>
            ///                ,$<subConnVarName>
            ///                ,$<subConnRoleAssumedIndex>)
            ///       ,vardecl)($<subConnType>
            ///                 ,$<subConnVarName>
            ///                 ,$<subConnVarSize>) <- what should this be?
            ///
            /// I am passing either a whole role array (of some size
            /// N, potentially of size 1) or an element of that role
            /// array (by definition array of size 1).
            ///
            /// The argument role array must match in size the
            /// parameter role array, so their sizes must match as
            /// well.
            ///
            /// Therefore, ...
        }

        return _r01_subExtraRoleData;
    }

    /// Declaration of the data that the sub-role instances need, to
    /// be inserted into the element's typedef.
    static public
        String getSubRoleActionGuards(BaseVisitor<T> bv
                                      , SymbolTableComposite thisEnv
                                      , String _role_name) {
        String _x05_subaction_guards = "";
        /// for each sub-connector role that this role is bound:
        ///
        // find the sub-connector info in ST's elementBindings.
        ElementBindings bindingsOfRole
            = thisEnv.elementBindings
            .getOrDefault(_role_name, new ElementBindings());
        for (ElementInfo binding : bindingsOfRole.bindings) {
            String _subConnType = binding.connectorTypeName;
            String _subConnVarName = binding.connectorInstName;
            String _subConnVarSize = binding.connectorSizeExpr;
            String _subConnRoleAssumedIndex
                = binding.elementIndex;
            String _subConnRoleFullName
                =
                "__roleId(__subConnCtx"
                + "," + _subConnType
                + "," + _subConnVarName
                + "," + _subConnRoleAssumedIndex + ")";
            // String subConnRoleAssumedSize
            //     = binding.elementSizeExpr;
            Map<String, PortInfo> subConnRolePortArgs
                = binding.elementPortArgs;
            for(Map.Entry<String,PortInfo> entry
                    : subConnRolePortArgs.entrySet()) {
                String portName = entry.getKey();
                int _portIndex = -1;
                {
                    Map<String, LstStr> roles2portvarsInParams
                        = thisEnv.roles2portvarsInParams;
                    LstStr all_ports
                        = roles2portvarsInParams.get(_role_name);
                    _portIndex = all_ports.indexOf(portName);
                    Utils.myAssertHard(_portIndex >= 0
                               , "Cannot find port " + portName
                               + " in the ports of role " + _role_name);
                    ++_portIndex; // port indices start from 1, not 0.
                }
                PortInfo portPInfo = entry.getValue();
                String portKind = portPInfo.portKind;
                String _subConnPortIndex = portPInfo.portIndex;
                String portSize = portPInfo.portSizeExpr;
                /*
                 * Loop over all actions
                 *
                 * ASSUMPTION: Each sub-connector uses a *subset* of
                 * the actions of the connector!!!
                 *
                 * That is, a sub-connector does *NOT* impose
                 * constraints on actions that the connector does not
                 * know about.
                 */
                IdInfo portInfo = bv.getIdInfo(portName);
                SymbolTablePort portST
                    = (SymbolTablePort) portInfo.getSB();
                LstStr all_actions = portST.all_port_actions();
                for (String _actionName : all_actions )
                    _x05_subaction_guards +=
                        Utils.readInputFile
                        (XCD2Promela.resourceTemplates
                         + "subconnector_role_var_port_action_sub_template.pml.template")
                        .replace("$<subConnPortIndex>",_subConnPortIndex)
                        .replace("$<portIndex>", ""+_portIndex)
                        .replace("$<actionName>", _actionName);
            }
            /// sub-connector context: `_subConnCtx' =
            ///
            /// _NAME(__connectorId(_context,$<connector_name>,_varname)
            ///       ,$<subConnVarName>)
            _x05_subaction_guards
                = _x05_subaction_guards
                .replace("$<subConnType>",    _subConnType)
                .replace("$<subConnVarName>", _subConnVarName)
                .replace("$<subConnVarSize>", _subConnVarSize)

                .replace("$<subConnRoleFullName>"
                         , _subConnRoleFullName)
                .replace("$<subConnRoleAssumedIndex>"
                         , _subConnRoleAssumedIndex);
        }

        return _x05_subaction_guards;
    }


    static public
        String getActionExtraGuardsInitialisation(int _portIndex
                                                  , String action) {
        String _x04_action_extra_guards_initialisation
            = Utils.readInputFile
            (XCD2Promela.resourceTemplates
             + "role_var_port_action_guard_init_sub_template.pml.template")
            .replace("$<actionName>", action)
            .replace("$<portIndex>", ""+_portIndex);
        return _x04_action_extra_guards_initialisation;
    }

    static public
        String getPortActionGuardsEnsures(BaseVisitor<T> bv
                                          , SymbolTableComposite thisEnv
                                          , String _role_name
                                          , String port
                                          , int _portIndex
                                          , String action) {
        String _x07_port_action_guards_ensures = "";
        IdInfo actionInfo = bv.getIdInfo(action);
        SymbolTableMethod actionST
            = (SymbolTableMethod) actionInfo.getSB();
        /* _x07_pa01_guard is the DISJUNCTION of all x_constrsAllows
           guard cases. It'll be CONJOINED with the other interaction
           constraints. */
        String _x07_pa01_guard = "";
        // role action constraints
        String _x07_pa02_ensures = "";
        {
            LstStr x_constrsAllows
                = Utils.nonNullCopy(actionST.methodStructure
                                    .x_constraintsAllows
                                    , LstStr.class);
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
                for (int sz=x_constrsAllows.size()
                         , prfxi=0, i=0
                         ; i<sz
                         ; prfxi = 1, ++i) {
                    var x_allows = x_constrsAllows.get(i);
                    _x07_pa01_guard +=
                        prefix[prfxi] + x_allows + ")";
                    var x_ensures = x_constrsEnsures.get(i);
                    _x07_pa02_ensures +=
                        " :: (" + x_allows + ") -> " + x_ensures + "; ";
                }
                _x07_pa01_guard
                    = "(" + _x07_pa01_guard + ")";
                _x07_pa02_ensures
                    = "if" + _x07_pa02_ensures
                    + (
                       " :: else -> "
                       + "assert(false); "
                       +  "/* incomplete action guards: r/p/a = "
                       + _role_name + "/" + port + "/" + action
                       + " */ fi"
                      );
            } else {
                _x07_pa01_guard = "true";
            }
        }
        _x07_port_action_guards_ensures += Utils.readInputFile
            (XCD2Promela.resourceTemplates
             + "role_var_port_action_sub_template.pml.template")
            .replace("$<actionName>", action)
            .replace("$<portIndex>", ""+_portIndex)
            .replace("$<portName>", port)
            .replace("$<connector07_port_action01_guard>"
                     , _x07_pa01_guard)
            .replace("$<connector07_port_action02_ensures>"
                     , _x07_pa02_ensures);

        // {
        //     bv.mywarning("action " + action
        //                  + "\n\tguards are "
        //                  + _x07_pa01_guard
        //                  + "\n\tguard-ensure pairs are "
        //                  + _x07_pa02_ensures
        //                  );
        // }
        //

        return _x07_port_action_guards_ensures;
    }

    static final java.io.PrintStream Err = System.err;

}
