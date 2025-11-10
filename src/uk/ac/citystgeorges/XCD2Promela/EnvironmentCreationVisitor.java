package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

// Void is final unfortunately :-(
// class MyReturnType extends Void { public Void() {super();} }

public class EnvironmentCreationVisitor extends BaseVisitor {

    /**
     * Constructs that create their own environment.
     */

    @Override public Void visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) {
        String compilationUnitID = "@root"; // root
        // initialise env, so that "result", etc. are known IDs
        myassert(env.size()==0, "Expected no environments!");
        env.add(rootContext);
        LstStr kwords = new LstStr();
        // kwords.add("\\result");
        // kwords.add("\\exception");
        // kwords.add("\\nothing");
        // kwords.add("\\in");
        for (String kword : kwords )
            { addIdInfo(kword
                        , XCD_type.resultt
                        , kword
                        , false
                        , (ArraySizeContext)null
                        , (Variable_initialValueContext)null
                        , kword, ""
                        , compilationUnitID); }

        return visitChildren(ctx);
    }

    // private Void registerNewEnvironment(String name, ParserRuleContext ctx) {
    //     return registerNewEnvironment(name, ctx, XCD_type.unknownt);
    // }
    private Void registerNewEnvironment(String name, ParserRuleContext ctx
                                        , XCD_type tp
                                        , ArraySizeContext arraySize
                                        , ContextInfo newctx) {
        updateln(ctx);
        ContextInfo framenow = env.get(env.size()-1);
        // Add new context to the childer of the current one.
        // framenow.children.add(newctx);
        /* NO! The newctx has already been registered by
           makeContext...!
        */

        // Add the name to the current environment
        addIdInfo(name, tp, false
                  , arraySize // roles, portvars, ports, sub-component/connector
                  , null, "", "", framenow.compilationUnitID);

        // push new environment context
        env.add(newctx);

        // Add new names in the new context
        Void res = visitChildren(ctx);

        // pop new environment context
        int last = env.size()-1;
        ContextInfo lastctx = env.get(last);
        myassert(newctx == lastctx, "Context not the last element");
        env.remove(last);   // should match what was added
        return res;
    }
    @Override public Void visitConnectorDeclaration(XCDParser.ConnectorDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoConn newctx
            = framenow.makeContextInfoConn(ctx.id.getText(), ctx, false);
        var res = registerNewEnvironment(ctx.id.getText(), ctx
                                         , XCD_type.connectort
                                         , null // there's no size part
                                         , newctx);

        /* Check that the set of roles and the set of roles listed in
         * the parameters are the same.
         */
        // mywarning("There are " + newctx.roles.size() + " roles defined "
        //           + "and "
        //           + newctx.roles2portvarsInParams.keySet().size()
        //           + " roles used");
        Set<String> rolesDefined = Set.copyOf(newctx.roles);
        {
            Set<String> rolesInParams = newctx.roles2portvarsInParams.keySet();
            var rolesDefinedMinusRolesInParams
                = USet.setDifference(rolesDefined, rolesInParams);
            myassert(rolesDefinedMinusRolesInParams.size()==0
                     , "Defined roles unused in the parameters list: "
                     + rolesDefinedMinusRolesInParams);
            var rolesInParamsMinusRolesDefined
                = USet.setDifference(rolesInParams, rolesDefined);
            myassert(rolesInParamsMinusRolesDefined.size()==0
                     , "Undefined roles in the parameters list: "
                     + rolesInParamsMinusRolesDefined);
        }
        /* For each role, check that the set of its port variables is
         * the same as the set of its port variables listed in the
         * parameters.
         */
        for (var rl : rolesDefined) {
            // find role's environment context
            List<ContextInfo> rolesContext
                = newctx.children.stream()
                .filter(cld -> cld.compilationUnitID.equals(rl))
                .collect(Collectors.toList());
            // System.err.println("has : " + newctx.children.size());
            // for (var el : newctx.children) {
            //     System.err.println("env: " + el);
            // }
            // for (var el : rolesContext) {
            //     System.err.println("Matching env: " + el);
            // }
            myassert(rolesContext.size()==1
                     , "Role " + rl
                     + " has been defined " + rolesContext.size() + " times");
            Set<String> portVarsUsed
                = Set.copyOf(newctx.roles2portvarsInParams.get(rl));
            ContextInfoConnRole roleEnv
                = (ContextInfoConnRole) (rolesContext.get(0));
            Set<? extends String> portVarsDefined
                = USet.setUnion(roleEnv.providedprts
                                , roleEnv.consumerprts
                                , roleEnv.requiredprts
                                , roleEnv.emitterprts);
            {
                Set<? extends String> portVarsDefinedMinusPortVarsUsed
                    = USet.setDifference(portVarsDefined, portVarsUsed);
                myassert(portVarsDefinedMinusPortVarsUsed.size()==0
                     , "Defined port variables unused in the parameters list: "
                     + portVarsDefinedMinusPortVarsUsed);
                Set<? extends String> portVarsUsedMinusPortVarsDefined
                    = USet.setDifference(portVarsUsed, portVarsUsed);
                myassert(portVarsUsedMinusPortVarsDefined.size()==0
                         , "Undefined port variables in the parameters list: "
                         + portVarsUsedMinusPortVarsDefined);
            }
        }
        return res;
    }

    @Override public Void visitRoleDeclaration(XCDParser.RoleDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ((ContextInfoConn)framenow).roles.add(ctx.id.getText());
        ContextInfoConnRole newctx
            = framenow.makeContextInfoConnRole(ctx.id.getText(), ctx, false);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.rolet
                                      , ctx.size
                                      , newctx);
    }

    @Override public Void visitRolePortvar(XCDParser.RolePortvarContext ctx) {
        updateln(ctx);
        var framenow = (ContextInfoConnRole) env.get(env.size()-1);
        String compUnitId = framenow.compilationUnitID;
        int flag
            = ((ctx.provided!=null)?8:0)
            + ((ctx.required!=null)?4:0)
            + ((ctx.consumer!=null)?2:0)
            + ((ctx.emitter !=null)?1:0);
        XCD_type tp = XCD_type.unknownt;//emittert,consumert,requiredt,providedt
        LstStr ports = null;
        String portName = null;
        XCDParser.ArraySizeContext thesz = null;
        switch (flag) {
        case 1:
            tp = XCD_type.emittervart;
            portName = ctx.emitter.id.getText();
            thesz = ctx.emitter.size;
            ports = framenow.emitterprts;
            break;
        case 2:
            tp = XCD_type.consumervart;
            portName = ctx.consumer.id.getText();
            thesz = ctx.consumer.size;
            ports = framenow.consumerprts;
            break;
        case 4:
            tp = XCD_type.requiredvart;
            portName = ctx.required.id.getText();
            thesz = ctx.required.size;
            ports = framenow.requiredprts;
            break;
        case 8:
            tp = XCD_type.providedvart;
            portName = ctx.provided.id.getText();
            thesz = ctx.provided.size;
            ports = framenow.providedprts;
            break;
        }
        myassert(tp!=XCD_type.unknownt
                 && portName!=null
                 // && thesz != null
                 && ports != null, "Error: unknown kind of port");
        boolean is_arrayp = thesz!=null;
        ArraySizeContext array_sz = thesz;
        // String big_name = Names.portName(compUnitId, portName);
        // addIdInfo(portName
        //           , tp
        //           // , "portType"
        //           , false
        //           , array_sz
        //           , (Variable_initialValueContext) null
        //           , big_name, ""
        //           , compUnitId);
        ports.add(portName);

        ContextInfoConnRolePort newctx
            = framenow.makeContextInfoConnRolePort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, array_sz, newctx);
    }

    @Override public Void visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoComp newctx
            = framenow.makeContextInfoComp(ctx.id.getText(), ctx, false);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.componentt
                                      , null // there's no size part
                                      , newctx);
    }

    @Override public Void visitComponentPort(XCDParser.ComponentPortContext ctx) {
        updateln(ctx);
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        String compUnitId = framenow.compilationUnitID;
        int flag
            = ((ctx.provided!=null)?8:0)
            + ((ctx.required!=null)?4:0)
            + ((ctx.consumer!=null)?2:0)
            + ((ctx.emitter !=null)?1:0);
        XCD_type tp = XCD_type.unknownt;//emittert,consumert,requiredt,providedt
        LstStr ports = null;
        String portName = null;
        XCDParser.ArraySizeContext thesz = null;
        switch (flag) {
        case 1:
            tp = XCD_type.emittert;
            portName = ctx.emitter.id.getText();
            thesz = ctx.emitter.size;
            ports = framenow.emitterprts;
            break;
        case 2:
            tp = XCD_type.consumert;
            portName = ctx.consumer.id.getText();
            thesz = ctx.consumer.size;
            ports = framenow.consumerprts;
            break;
        case 4:
            tp = XCD_type.requiredt;
            portName = ctx.required.id.getText();
            thesz = ctx.required.size;
            ports = framenow.requiredprts;
            break;
        case 8:
            tp = XCD_type.providedt;
            portName = ctx.provided.id.getText();
            thesz = ctx.provided.size;
            ports = framenow.providedprts;
            break;
        }
        myassert(tp!=XCD_type.unknownt
                 && portName!=null
                 // && thesz != null
                 && ports != null, "Error: unknown kind of port");
        boolean is_arrayp = thesz!=null;
        ArraySizeContext array_sz = thesz;
        // String big_name = Names.portName(compUnitId, portName);
        // addIdInfo(portName
        //           , tp
        //           // , "portType"
        //           , false
        //           , array_sz
        //           , (Variable_initialValueContext) null
        //           , big_name, ""
        //           , compUnitId);
        ports.add(portName);

        ContextInfoCompPort newctx
            = framenow.makeContextInfoCompPort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, array_sz, newctx);
    }

    @Override public Void visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoMethod newctx
            = framenow.makeContextInfoMethod(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.methodt
                                      , null // there's no size part
                                      , newctx);
    }

    @Override public Void visitEventSignature(XCDParser.EventSignatureContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoEvent newctx
            = framenow.makeContextInfoEvent(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.eventt
                                      , null // there's no size part
                                      , newctx);
    }

    @Override public Void visitInlineFunctionDeclaration(XCDParser.InlineFunctionDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoFunction newctx
            = framenow.makeContextInfoFunction(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.functiont
                                      , null // there's no size part
                                      , newctx);
    }


    /**
     * Constructs that do NOT create their own environment.
     *
     * However, they introduce new names into the current environment.
     */

    @Override public Void visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitEnumDeclaration called");
        ContextInfo framenow = env.get(env.size()-1);
        Name enumName = new Name(ctx.id.getText());
        framenow.enums.add(""+enumName);
        Sig values = new Sig();

        var constants = ctx.constants;
        int size = constants.size();
        myassert(constants!=null && size>0
                 , "ERROR: An enum type must have values");
        var name1st = ctx.constant_pre.getText();
        values.add(new Type(name1st));
        for (var cnst : constants) {
            String name = cnst.getText();
            values.add(new Type(name));
        }

        addIdInfo(enumName.toString()
                  , XCD_type.enumt
                  , ""
                  , false
                  , null
                  , null
                  , enumName.toString(), ""
                  , framenow.compilationUnitID);
        // mywarning("Added enum type \"" + enumName.toString()
        //           + "\" with values " + values.toString()
        //           + " and s is \n" + s);
        for (var value : values) {
            addIdInfo(value.toString()
                      , XCD_type.enumvalt
                      , false
                      , null
                      , null
                      , value.toString(), ""
                      , enumName.toString());
            // mywarning("Added enum value \"" + value.toString()
            //        + "\" for enum type \"" + enumName.toString()
            //        + "\"");
        }
        return defaultResult();
    }

    @Override public Void visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitTypeDefDeclaration called");
        ContextInfo framenow = env.get(env.size()-1);
        String newtype = ctx.newtype.getText();
        framenow.typedefs.add(newtype);

        addIdInfo(newtype
                  , XCD_type.typedeft
                  , ""
                  , false
                  , null
                  , null
                  , newtype, ""
                  , framenow.compilationUnitID);
        // mywarning("Added type \"" + newtype + "\" as a newname for \""
        //           + definition + "\" and s is\n" + s);
        return defaultResult();
    }


    @Override public Void visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) {
        updateln(ctx);
        String varName = ctx.id.getText();
        DataTypeContext dtype = ctx.type; // int, byte, bool, void, ID(long name)
        ArraySizeContext array_sz = ctx.size;
        Variable_initialValueContext initVal = ctx.initval;
        Void res = visitPrimitiveVariableOrParamDeclaration(dtype
                                                            , varName
                                                            , array_sz
                                                            , initVal
                                                            , true);
        return res;
    }
    @Override public Void visitFormalParameter(XCDParser.FormalParameterContext ctx) {
        updateln(ctx);
        String varName = ctx.id.getText();
        // dtype: int, byte, bool, void, ID(long name)
        DataTypeContext dtype = ctx.type;
        ArraySizeContext array_sz = ctx.size;
        Variable_initialValueContext initVal = ctx.initval;
        Void res = visitPrimitiveVariableOrParamDeclaration(dtype
                                                            , varName
                                                            , array_sz
                                                            , initVal
                                                            , false);
        return res;
    }
    private Void visitPrimitiveVariableOrParamDeclaration(DataTypeContext dtype
                                                          , String varName
                                                          , ArraySizeContext array_sz
                                                          , Variable_initialValueContext initVal
                                                          , boolean isVar) {
        var framenow = (ContextInfo) env.get(env.size()-1);
        String compUnitId = framenow.compilationUnitID;
        XCD_type tp = framenow.type;

        addIdInfo(varName
                  , (isVar) ? XCD_type.vart : XCD_type.paramt
                  , dtype.getText()
                  , !isVar
                  , array_sz
                  , initVal
                  , "", ""
                  , compUnitId);
        LstStr paramsOrvars = null;
        if (tp==XCD_type.componentt) {
            var theEnv = (ContextInfoComp)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
        } else if (tp==XCD_type.connectort) {
            ((ContextInfoConn)framenow).vars.add(varName);
            var theEnv = (ContextInfoConn)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
        } else if (tp==XCD_type.rolet) {
            var theEnv = (ContextInfoConnRole)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
        } else {
            myassert((tp==XCD_type.componentt) && (tp==XCD_type.rolet)
                     , (isVar?"Variable ":"Parameter ")
                     + varName
                     + " declaration appears in an unexpected context "
                     + tp);
        }
        paramsOrvars.add(varName);
        return defaultResult();
    }

    @Override public Void visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) {
        updateln(ctx);
        Token tk = (Token) ctx.elType;
        var framenow = env.get(env.size()-1).you();
        String compUnitId = framenow.compilationUnitID;
        String instance_name = ctx.id.getText();
        if (tk.getType() == XCDParser.TK_COMPONENT) { // sub-component instance
            ArraySizeContext sz = ctx.size;
            String component_def = ctx.userdefined.getText();

            visit(ctx.params);
            addIdInfo(instance_name
                      , XCD_type.componentt
                      , component_def
                      , false
                      , sz
                      , null // no init value
                      , "", ""    // big_name, prefix -- unused
                      , compUnitId);
            if (!(framenow instanceof ContextInfoComp)) {
                myassert(false
                         , "COMPONENT:\nCompilationUnitID=\""
                         + framenow.compilationUnitID
                         + "\"\nType=\"" + framenow.type
                         + "\"\nParent=\"" + framenow.parent
                         + "\"\n# of Children=\"" + framenow.children.size()
                         + "\"\n"
                         + "Configuration is \"" + instance_name
                         + "\" of type \"" + component_def + "\"\n");
            } else {
                ((ContextInfoComp)framenow).subcomponents.add(instance_name);
                // mywarning(framenow.compilationUnitID
                //        + "'s subcomponent of type "
                //           + instance_name);
            }
        } else if (tk.getType() == XCDParser.TK_CONNECTOR) { // connector element
            ArraySizeContext sz = ctx.connsize;
            String connector_def
                = (ctx.userdefined!=null)
                ? ctx.userdefined.getText()
                : ctx.basicConn.getText();
            // String params = visit(ctx.conn_params).get(0);
            addIdInfo(instance_name
                      , XCD_type.connectort
                      , connector_def
                      , false
                      , sz
                      , null    // no init value
                      , "", ""  // big_name, prefix -- unused
                      , compUnitId);

            if (framenow instanceof ContextInfoComp) {
                ((ContextInfoComp)framenow).subconnectors.add(instance_name);
                // mywarning(framenow.compilationUnitID
                //        + "'s subcomponent of type "
                //           + instance_name);
            } else if (framenow instanceof ContextInfoConn) {
                ((ContextInfoConn)framenow).subconnectors.add(instance_name);
            } else {
                myassert(false
                         , "CONNECTOR:\nCompilationUnitID=\"" + framenow.compilationUnitID
                         + "\"\nType=\"" + framenow.type
                         + "\"\nParent=\"" + framenow.parent
                         + "\"\n# of Children=\"" + framenow.children.size()
                         + "\"\n"
                         + "Configuration is \"" + instance_name
                         + "\" of type \"" + connector_def + "\"\n");
            }
        } else {myassert(false, "Unknown element type inside component");}

        return defaultResult();
    }
    /*
     * The configuration is treated here
     */
    @Override
    public Void visitCompilationUnit(XCDParser.CompilationUnitContext ctx) {
        Void res=null;
        if (ctx.config!=null) {
            res=visitTheConfiguration(ctx);
        } else {
            res=visitChildren(ctx);
        }
        return res;
    }
    private Void visitTheConfiguration(XCDParser.CompilationUnitContext ctx) {
        ContextInfo framenow = env.get(env.size()-1); // root
        ContextInfoComp newctx
            = framenow.makeContextInfoComp("@configuration", ctx, false);
        env.add(newctx);
        Void res=visitChildren(ctx);
        Utils.withInputAndFileToWrite
            ("/resources/configuration.pml.template"
             , "configuration.pml"
             , (String confFileContents) -> {
                if (newctx.map.size()!=2) {
                    mywarning("Configuration should have exactly one"
                              + " component instance, but instead, it has "
                              + (newctx.map.size()-1));
                    for (var key : newctx.map.keySet()) {
                        var val=newctx.map.get(key);
                        System.err.println("Instance \"" + key
                                           + "\" of component type \""
                                           + val.variableTypeName + "\"\n");
                    }
                    myassert(false, "");
                }
                myassert(newctx.subcomponents.size()==1
                         , "Configuration should have exactly one"
                         + " component, but instead it has "
                         + newctx.subcomponents.size());
                String instName = newctx.subcomponents.get(0);
                IdInfo compTypeInfo = newctx.map.get(instName);
                String compType = compTypeInfo.variableTypeName;
                myassert(compTypeInfo.type == XCD_type.componentt
                         , "Configuration instance type is not a component"
                         + "Instance \"" + instName
                         + "\" of component type \"" + compType
                         + "\"");
                String out = confFileContents
                    .replace("$<compType>", compType);
                return out;
            });
        int last = env.size()-1;
        ContextInfo lastctx = env.get(last);
        myassert(newctx == lastctx, "Context not the last element");
        env.remove(last);   // should match what was added
        return res;
    }

    @Override public Void visitConnectorParameter(XCDParser.ConnectorParameterContext ctx) {
        if (ctx.prim_param!=null) // let formalParameter treat it
            return visitChildren(ctx);

        String roleName = ctx.role.getText();
        ArraySizeContext sz = ctx.size;
        LstStr portParamNames = new LstStr();
        portParamNames.add(ctx.pv_pre.getText());
        for (var pv : ctx.pvs) {
            portParamNames.add(pv.getText());
        }
        // At a latter point, the roleName should match some role
        // inside the connector, and each of its portParamNames should
        // match all that role's port variables.
        //
        // Here we'll just enter these into the environment, so we can
        // check later on that they're correct.
        //
        // Why are we forcing people to define the role/portVars
        // twice? [cause we're mean!]
        ContextInfoConn framenow = (ContextInfoConn) (env.get(env.size()-1));
        framenow.roles2portvarsInParams.put(roleName, portParamNames);
        return defaultResult();
    }

    /**
     * Constructs that do NOT create their own environment.
     *
     * Neither do they introduce new names into the current environment.
     *
     * Therefore, these can be safely ignored at this phase, where
     * we're creating/populating environments.
     */

    // @Override public Void visitVariableDeclaration(XCDParser.VariableDeclarationContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorBody(XCDParser.ConnectorBodyContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorBody_Element(XCDParser.ConnectorBody_ElementContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRoleBody(XCDParser.RoleBodyContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRoleBody_Element(XCDParser.RoleBody_ElementContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPortvar(XCDParser.EmitterPortvarContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPortvar(XCDParser.ConsumerPortvarContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPortvar(XCDParser.RequiredPortvarContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPortvar(XCDParser.ProvidedPortvarContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPortvar_event(XCDParser.EmitterPortvar_eventContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPortvar_event(XCDParser.ConsumerPortvar_eventContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPortvar_method(XCDParser.RequiredPortvar_methodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPortvar_method(XCDParser.ProvidedPortvar_methodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPortvar_complexmethod(XCDParser.ProvidedPortvar_complexmethodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPv_InteractionContract(XCDParser.EmitterPv_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPv_InteractionContract(XCDParser.ConsumerPv_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPv_InteractionContract(XCDParser.RequiredPv_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPv_InteractionContract(XCDParser.ProvidedPv_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPvcomplex_InteractionContract(XCDParser.ProvidedPvcomplex_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPv_InteractionConstraint(XCDParser.EmitterPv_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPv_InteractionConstraint(XCDParser.ConsumerPv_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPv_InteractionConstraint(XCDParser.RequiredPv_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPv_InteractionConstraint(XCDParser.ProvidedPv_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComponentBody(XCDParser.ComponentBodyContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComponentBody_Element(XCDParser.ComponentBody_ElementContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPort(XCDParser.EmitterPortContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPort(XCDParser.ConsumerPortContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPort(XCDParser.RequiredPortContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPort(XCDParser.ProvidedPortContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPort_event(XCDParser.EmitterPort_eventContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPort_event(XCDParser.ConsumerPort_eventContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPort_method(XCDParser.RequiredPort_methodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPort_method(XCDParser.ProvidedPort_methodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComplex_providedPort_method(XCDParser.Complex_providedPort_methodContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComplex_providedPort_functionalContract_Res(XCDParser.Complex_providedPort_functionalContract_ResContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComplex_provided_InteractionContract_Res(XCDParser.Complex_provided_InteractionContract_ResContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComplex_providedPort_functionalContract_Req(XCDParser.Complex_providedPort_functionalContract_ReqContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitComplex_provided_InteractionContract_Req(XCDParser.Complex_provided_InteractionContract_ReqContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterRequired_InteractionContract(XCDParser.EmitterRequired_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerProvided_InteractionContract(XCDParser.ConsumerProvided_InteractionContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterRequired_InteractionConstraint(XCDParser.EmitterRequired_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerProvided_InteractionConstraint(XCDParser.ConsumerProvided_InteractionConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPort_functionalContract(XCDParser.EmitterPort_functionalContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPort_functionalContract(XCDParser.RequiredPort_functionalContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPort_functionalContract(XCDParser.ConsumerPort_functionalContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPort_functionalContract(XCDParser.ProvidedPort_functionalContractContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEmitterPort_functionalConstraint(XCDParser.EmitterPort_functionalConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRequiredPort_functionalConstraint(XCDParser.RequiredPort_functionalConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConsumerPort_functionalConstraint(XCDParser.ConsumerPort_functionalConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitProvidedPort_functionalConstraint(XCDParser.ProvidedPort_functionalConstraintContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitCombinationKeyword(XCDParser.CombinationKeywordContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitAssertDeclaration(XCDParser.AssertDeclarationContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitArraySize(XCDParser.ArraySizeContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitArrayIndex(XCDParser.ArrayIndexContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitVariable_initialValue(XCDParser.Variable_initialValueContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConditionalStatement(XCDParser.ConditionalStatementContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitPostStatement(XCDParser.PostStatementContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConditionalExpression(XCDParser.ConditionalExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitSetExpression(XCDParser.SetExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitEqualityExpression(XCDParser.EqualityExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitTernaryExpression(XCDParser.TernaryExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRelationExpression(XCDParser.RelationExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitAdditiveExpression(XCDParser.AdditiveExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitMultiplicativeExpression(XCDParser.MultiplicativeExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitUnaryExpression(XCDParser.UnaryExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitNullaryExpression(XCDParser.NullaryExpressionContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitRange(XCDParser.RangeContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitArgumentList(XCDParser.ArgumentListContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorParameterList(XCDParser.ConnectorParameterListContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorArgumentList(XCDParser.ConnectorArgumentListContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorIndex(XCDParser.ConnectorIndexContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorArgument_pv(XCDParser.ConnectorArgument_pvContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitFormalParameters(XCDParser.FormalParametersContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitBasicConnectorType(XCDParser.BasicConnectorTypeContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitIntegerLiteral(XCDParser.IntegerLiteralContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitParamArgument(XCDParser.ParamArgumentContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitDataType(XCDParser.DataTypeContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorArgument(XCDParser.ConnectorArgumentContext ctx) { return visitChildren(ctx); }

    /**
     * Miscellaneous helper functions
     */

}
