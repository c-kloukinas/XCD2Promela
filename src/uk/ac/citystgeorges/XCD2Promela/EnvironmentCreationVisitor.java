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

class EnvironmentCreationVisitor
    extends BaseVisitor<uk.ac.citystgeorges.XCD2Promela.T> {

    /*
      See:
      https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor.html#defaultResult()
      https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor.html#aggregateResult(T,T)
    */
    @Override
    protected T defaultResult() {return new T();}
    @Override
    protected T aggregateResult​(T aggregate, T nextResult) {
        if (nextResult!=null)
            for (var s : nextResult)
                aggregate.add(s);
        return aggregate;
    }

   /**
     * Constructs that create their own environment.
     */

    @Override public T visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) {
        String compilationUnitID = "@root"; // root
        // initialise env, so that "result", etc. are known IDs
        myassert(getEnv().size()==0, "Expected no environments!");
        getEnv().add(rootContext);
        LstStr kwords = new LstStr();
        // kwords.add(keywordResult);
        // kwords.add(keywordException);
        // kwords.add(keywordNothing);
        // kwords.add("\\in");
        for (String kword : kwords )
            { addIdInfo(kword
                        , XCD_type.resultt
                        , kword
                        , false
                        , (ArraySizeContext)null
                        , (Variable_initialValueContext)null
                        , compilationUnitID); }

        return visitChildren(ctx);
    }

    // private T registerNewEnvironment(String name, ParserRuleContext ctx) {
    //     return registerNewEnvironment(name, ctx, XCD_type.unknownt);
    // }
    private T registerNewEnvironment(String name, ParserRuleContext ctx
                                     , XCD_type tp
                                     , ArraySizeContext arraySize
                                     , ContextInfo newctx) {
        return registerNewEnvironment(name, ctx, tp, arraySize, newctx, null);
    }
    private T registerNewEnvironment(String name, ParserRuleContext ctx
                                     , XCD_type tp
                                     , ArraySizeContext arraySize
                                     , ContextInfo newctx
                                     , TranslatorI tr) {
        updateln(ctx);
        ContextInfo framenow = frameNow();
        /*
         * // Add new context to the childer of the current one.
         * // framenow.children.add(newctx);
         *
         * NO! The newctx has already been registered by
         * makeContext...!
         */

        // Add the name to the current environment
        addIdInfo(name, tp, false
                  , arraySize // roles, portvars, ports, sub-component/connector
                  , null, framenow.compilationUnitID);

        // push new environment context
        getEnv().add(newctx);

        // Add new names in the new context
        T res = visitChildren(ctx); // These results are ignored

        // mywarning("***Currently env has size: "
        //           + getEnv().size()
        //           + " name is: " + name);
        if (tr!=null)
            res = tr.translate(this, ctx);

        // pop new environment context
        popLastContext(newctx);
        return res;
    }
    @Override public T visitConnectorDeclaration(XCDParser.ConnectorDeclarationContext ctx) {
        ContextInfo framenow = frameNow();
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

    @Override public T visitRoleDeclaration(XCDParser.RoleDeclarationContext ctx) {
        ContextInfo framenow = frameNow();
        ((ContextInfoConn)framenow).roles.add(ctx.id.getText());
        ContextInfoConnRole newctx
            = framenow.makeContextInfoConnRole(ctx.id.getText(), ctx, false);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.rolet
                                      , ctx.size
                                      , newctx);
    }

    @Override public T visitRolePortvar(XCDParser.RolePortvarContext ctx) {
        updateln(ctx);
        var framenow = (ContextInfoConnRole) frameNow();
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
        //           , compUnitId);
        ports.add(portName);

        ContextInfoConnRolePort newctx
            = framenow.makeContextInfoConnRolePort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, array_sz, newctx);
    }

    @Override public T visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) {
        ContextInfo framenow = frameNow();
        String compName = ctx.id.getText();
        ContextInfoComp newctx
            = framenow.makeContextInfoComp(compName, ctx, false);
        var tr = new TranslatorComponentDeclarationContext();
        // mywarning("***visitComponentDeclaration: Currently env has size: "
        //           + getEnv().size()
        //           + " name is: " + compName);
        return registerNewEnvironment(compName, ctx
                                      , XCD_type.componentt
                                      , null // there's no size part
                                      , newctx
                                      , tr);
    }

    @Override public T visitComponentPort(XCDParser.ComponentPortContext ctx) {
        updateln(ctx);
        var framenow = (ContextInfoComp) frameNow();
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
        //           , compUnitId);
        ports.add(portName);

        ContextInfoCompPort newctx
            = framenow.makeContextInfoCompPort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, array_sz, newctx);
    }

    @Override public T visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        ContextInfo framenow = frameNow();
        ContextInfoMethod newctx
            = framenow.makeContextInfoMethod(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.methodt
                                      , null // there's no size part
                                      , newctx);
    }

    @Override public T visitEventSignature(XCDParser.EventSignatureContext ctx) {
        ContextInfo framenow = frameNow();
        ContextInfoEvent newctx
            = framenow.makeContextInfoEvent(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.eventt
                                      , null // there's no size part
                                      , newctx);
    }

    @Override public T visitInlineFunctionDeclaration(XCDParser.InlineFunctionDeclarationContext ctx) {
        ContextInfo framenow = frameNow();
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

    @Override public T visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitEnumDeclaration called");
        ContextInfo framenow = frameNow();
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

        IdInfo enumTypeInfo = addIdInfo(enumName.toString()
                                        , XCD_type.enumt
                                        , ""
                                        , false
                                        , null
                                        , null
                                        , framenow.compilationUnitID);
        enumTypeInfo.translation.add(Names.enumTypeName(enumName.toString()));
        // mywarning("Added enum type \"" + enumName.toString()
        //           + "\" with values " + values.toString()
        //           + " and s is \n" + s);
        for (var value : values) {
            IdInfo valInfo = addIdInfo(value.toString()
                                       , XCD_type.enumvalt
                                       , false
                                       , null
                                       , null
                                       , enumName.toString());
            valInfo.translation.add(Names.enumValueName(value.toString()));
            // mywarning("Added enum value \"" + value.toString()
            //        + "\" for enum type \"" + enumName.toString()
            //        + "\"");
        }
        return defaultResult();
    }

    @Override public T visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitTypeDefDeclaration called");
        ContextInfo framenow = frameNow();
        String newtype = ctx.newtype.getText();
        framenow.typedefs.add(newtype);

        addIdInfo(newtype
                  , XCD_type.typedeft
                  , ""
                  , false
                  , null
                  , null
                  , framenow.compilationUnitID);
        // mywarning("Added type \"" + newtype + "\" as a newname for \""
        //           + definition + "\" and s is\n" + s);
        return defaultResult();
    }


    @Override public T visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) {
        updateln(ctx);
        String varName = ctx.id.getText();
        DataTypeContext dtype = ctx.type; // int, byte, bool, void, ID(long name)
        ArraySizeContext array_sz = ctx.size;
        Variable_initialValueContext initVal = ctx.initval;
        T res = visitPrimitiveVariableOrParamDeclaration(dtype
                                                            , varName
                                                            , array_sz
                                                            , initVal
                                                            , true);
        return res;
    }
    @Override public T visitFormalParameter(XCDParser.FormalParameterContext ctx) {
        updateln(ctx);
        String varName = ctx.id.getText();
        // dtype: int, byte, bool, void, ID(long name)
        DataTypeContext dtype = ctx.type;
        ArraySizeContext array_sz = ctx.size;
        Variable_initialValueContext initVal = ctx.initval;
        T res = visitPrimitiveVariableOrParamDeclaration(dtype
                                                            , varName
                                                            , array_sz
                                                            , initVal
                                                            , false);
        return res;
    }
    private T visitPrimitiveVariableOrParamDeclaration(DataTypeContext dtype
                                                       , String varName
                                                       , ArraySizeContext array_sz
                                                       , Variable_initialValueContext initVal
                                                       , boolean isVar) {
        var framenow = (ContextInfo) frameNow();
        String compUnitId = framenow.compilationUnitID;
        XCD_type tp = framenow.type;

        IdInfo idinfo = addIdInfo(varName
                                  , (isVar) ? XCD_type.vart : XCD_type.paramt
                                  , dtype.getText()
                                  , !isVar
                                  , array_sz
                                  , initVal
                                  , compUnitId);
        LstStr paramsOrvars = null;
        String trans = "";
        if (tp==XCD_type.componentt) {
            var theEnv = (ContextInfoComp)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
            trans = isVar
                ? Names.varNameComponent(theEnv.compilationUnitID, varName)
                : Names.paramNameComponent(theEnv.compilationUnitID, varName);
        } else if (tp==XCD_type.connectort) {
            ((ContextInfoConn)framenow).vars.add(varName);
            var theEnv = (ContextInfoConn)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
            myassert(!isVar, "Connectors cannot have variables of their own");
            trans = isVar
                ? Names.xVarName(theEnv.compilationUnitID
                                 , "UNKNOWNROLE"
                                 , varName)
                : varName;
        } else if (tp==XCD_type.rolet) {
            var theEnv = (ContextInfoConnRole)framenow;
            paramsOrvars = isVar ? theEnv.vars : theEnv.params;
            trans = isVar
                ? Names.xVarName(theEnv.parent.compilationUnitID
                                 , theEnv.compilationUnitID
                                 , varName)
                : varName;
        } else {
            myassert((tp==XCD_type.componentt) && (tp==XCD_type.rolet)
                     , (isVar?"Variable ":"Parameter ")
                     + varName
                     + " declaration appears in an unexpected context "
                     + tp);
        }
        paramsOrvars.add(varName);
        idinfo.translation.add(trans);
        return defaultResult();
    }

    @Override public T visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) {
        updateln(ctx);
        Token tk = (Token) ctx.elType;
        var framenow = frameNow().you();
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
    public T visitCompilationUnit(XCDParser.CompilationUnitContext ctx) {
        T res=null;
        if (ctx.config!=null) {
            res=visitTheConfiguration(ctx);
        } else {
            res=visitChildren(ctx);
        }
        return res;
    }
    private T visitTheConfiguration(XCDParser.CompilationUnitContext ctx) {
        ContextInfo framenow = frameNow(); // root
        ContextInfoComp newctx
            = framenow.makeContextInfoComp("@configuration", ctx, false);
        getEnv().add(newctx);
        T res=visitChildren(ctx);
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
                    myassert(false
                             , "Assertion failed"
                             + " - check previous warning for details");
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
        int last = getEnv().size()-1;
        ContextInfo lastctx = getEnv().get(last);
        myassert(newctx == lastctx, "Context not the last element");
        getEnv().remove(last);   // should match what was added
        return res;
    }

    @Override public T visitConnectorParameter(XCDParser.ConnectorParameterContext ctx) {
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
        ContextInfoConn framenow = (ContextInfoConn) frameNow();
        framenow.roles2portvarsInParams.put(roleName, portParamNames);
        return defaultResult();
    }

    /**
     * Constructs that do NOT create their own environment.
     *
     * Neither do they introduce new names into the current environment.
     *
     * Therefore, these can be safely ignored at this phase, where
     * we're creating/populating environments. We'll be using their
     * default behaviour instead (i.e., just visit children).
     */

    @Override
    public T visitConditionalExpression(XCDParser.ConditionalExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorConditionalExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitSetExpression(XCDParser.SetExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorEqualityExpressionContext();
        res = tr.translate(this, ctx.setexpr_var);
        return res;
    }

    @Override
    public T visitRange(XCDParser.RangeContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorRangeContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitEqualityExpression(XCDParser.EqualityExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorEqualityExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitTernaryExpression(XCDParser.TernaryExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorTernaryExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitRelationalExpression(XCDParser.RelationalExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorRelationalExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitAdditiveExpression(XCDParser.AdditiveExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorAdditiveExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitMultiplicativeExpression(XCDParser.MultiplicativeExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorMultiplicativeExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitUnaryExpression(XCDParser.UnaryExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorUnaryExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitNullaryExpression(XCDParser.NullaryExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorNullaryExpressionContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitIntegerLiteral(XCDParser.IntegerLiteralContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorIntegerLiteralContext();
        res = tr.translate(this, ctx);
        return res;
    }
    /**
     * Miscellaneous helper functions
     */

}
