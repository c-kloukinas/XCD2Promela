package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

// Void is final unfortunately :-(
// class MyReturnType extends Void { public Void() {super();} }

public class EnvironmentCreationVisitor extends XCDBaseVisitor<Void> {

    /**
     * Constructs that create their own environment.
     */

    @Override public Void visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) {
        String compilationUnitID = "@root"; // root
        // initialise env, so that "result", etc. are known IDs
        env = new ArrayList<ContextInfo>();
        env.add(new ContextInfo());
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
                                        , XCD_type tp, ContextInfo newctx) {
        updateln(ctx);
        ContextInfo framenow = env.get(env.size()-1);
        // push new environment context
        env.add(newctx);

        // Add new names in the current context
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
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.connectort, newctx);
    }

    @Override public Void visitRoleDeclaration(XCDParser.RoleDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoConnRole newctx
            = framenow.makeContextInfoConnRole(ctx.id.getText(), ctx, false);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.rolet, newctx);
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
        String big_name = Names.portName(compUnitId, portName);
        addIdInfo(portName
                  , tp
                  // , "portType"
                  , false
                  , array_sz
                  , (Variable_initialValueContext) null
                  , big_name, ""
                  , compUnitId);
        ports.add(portName);

        ContextInfoCompPort newctx
            = framenow.makeContextInfoCompPort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, newctx);
    }

    @Override public Void visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoComp newctx
            = framenow.makeContextInfoComp(ctx.id.getText(), ctx, false);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.componentt, newctx);
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
        String big_name = Names.portName(compUnitId, portName);
        addIdInfo(portName
                  , tp
                  // , "portType"
                  , false
                  , array_sz
                  , (Variable_initialValueContext) null
                  , big_name, ""
                  , compUnitId);
        ports.add(portName);

        ContextInfoCompPort newctx
            = framenow.makeContextInfoCompPort(portName, ctx, tp, false);
        return registerNewEnvironment(portName, ctx, tp, newctx);
    }

    @Override public Void visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoMethod newctx
            = framenow.makeContextInfoMethod(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.methodt, newctx);
    }

    @Override public Void visitEventSignature(XCDParser.EventSignatureContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoEvent newctx
            = framenow.makeContextInfoEvent(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.eventt, newctx);
    }

    @Override public Void visitInlineFunctionDeclaration(XCDParser.InlineFunctionDeclarationContext ctx) {
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoFunction newctx
            = framenow.makeContextInfoFunction(ctx.id.getText(), ctx);
        return registerNewEnvironment(ctx.id.getText(), ctx
                                      , XCD_type.functiont, newctx);
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

        Sig values = new Sig();
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


    @Override public Void visitVariableDeclaration(XCDParser.VariableDeclarationContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitActualParameter(XCDParser.ActualParameterContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitConnectorParameter(XCDParser.ConnectorParameterContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitConnectorArgument(XCDParser.ConnectorArgumentContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitFormalParameter(XCDParser.FormalParameterContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    @Override public Void visitDataType(XCDParser.DataTypeContext ctx) {
        mywarning("TODO: complete me");
        return visitChildren(ctx);
    }

    /**
     * Constructs that do NOT create their own environment.
     */

    // @Override public Void visitCompilationUnit(XCDParser.CompilationUnitContext ctx) { return visitChildren(ctx); }

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

    // @Override public Void visitActualParameters(XCDParser.ActualParametersContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorParameterList(XCDParser.ConnectorParameterListContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorArgumentList(XCDParser.ConnectorArgumentListContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorIndex(XCDParser.ConnectorIndexContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitConnectorArgument_pv(XCDParser.ConnectorArgument_pvContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitFormalParameters(XCDParser.FormalParametersContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitBasicConnectorType(XCDParser.BasicConnectorTypeContext ctx) { return visitChildren(ctx); }

    // @Override public Void visitIntegerLiteral(XCDParser.IntegerLiteralContext ctx) { return visitChildren(ctx); }

    static public void updateln(Tree ctx) {Utils.updateln(ctx); }
    static public String newgensym() { return Utils.newgensym(null); }
    static public String newgensym(String pref) {return Utils.newgensym(pref);}
    public static void myAssert(boolean cond, String msg) {
        Utils.myAssert(cond, msg); }
    public void myassert(boolean cond, String msg) {
        Utils.util.myassert(cond,msg); }
    public static void myWarning(String msg) { Utils.myWarning(msg); }
    public void mywarning(String msg) { Utils.util.mywarning(msg); }

    ArrayList<ContextInfo> env = new ArrayList<ContextInfo>();
    IdInfo addIdInfo(String symbol
                     , XCD_type tp, String varTypeName, boolean is_paramp
                     , ArraySizeContext arraySize
                     , Variable_initialValueContext initVal
                     , String big_name, String var_prefix
                     , String parentId) {
        IdInfo res
            = addIdInfo(symbol, tp, is_paramp, arraySize
                        , initVal, big_name, var_prefix, parentId);
        res.variableTypeName = varTypeName;
        return res; }
    IdInfo addIdInfo(String symbol
                     , XCD_type tp, boolean is_paramp
                     , ArraySizeContext arraySize
                     , Variable_initialValueContext initVal
                     , String big_name, String var_prefix
                     , String parentId) {
        var newInfo = new IdInfo(tp, is_paramp
                                 , arraySize
                                 , initVal
                                 , big_name, var_prefix
                                 , parentId);
        var currentMap = env.get(env.size()-1).map;
        if (currentMap.containsKey(symbol)) {
            IdInfo info = currentMap.get(symbol);
            boolean matches = (info.type == newInfo.type)
                // && (info.sType.equals(newInfo.sType))
                && (info.is_param == newInfo.is_param)
                && ( (info.arraySz==null && newInfo.arraySz==null)
                     || (info.arraySz!=null && newInfo.arraySz!=null
                         && (info.arraySz.equals(newInfo.arraySz))) )
                && ( (info.initVal==null && newInfo.initVal==null)
                     || (info.initVal!=null && newInfo.initVal!=null
                         && (info.initVal.equals(newInfo.initVal))) )
                && (info.big_name.equals(newInfo.big_name))
                && (info.parent.equals(newInfo.parent));
            if (!matches) {
                mywarning("Symbol \"" +symbol+"\" already in the map"
                          + "\n" + info.type + " vs " + newInfo.type
                          // + "\n" + info.sType + " vs " + newInfo.sType
                          + "\n" + info.is_param + " vs " + newInfo.is_param
                          + "\n" + info.arraySz + " vs " + newInfo.arraySz
                          + "\n" + info.initVal + " vs " + newInfo.initVal
                          + "\n" + info.big_name + " vs " + newInfo.big_name
                          + "\n" + info.parent + " vs " + newInfo.parent);
                myassert(false, "Symbol \""+symbol+"\" already in the map");
            } else
                mywarning("Symbol \"" +symbol+"\" aldeary in the map - input visited twice!");

        } else {
            currentMap.put(symbol, newInfo); }
        return newInfo;
    }
    IdInfo getIdInfo(String id) {
        int last = env.size()-1;
        IdInfo res = null;
        while (res == null && last > -1) {
            Map<String,IdInfo> the_map = env.get(last).map;
            if (the_map.containsKey(id))
                res=the_map.get(id);
            --last;
        }
        /*
         * Is it an enum/typedef defined globally?
         */
        // if (null==res) {
        //     ContextInfo root = env.get(0);
        //     ArrayList<ContextInfo> rootchildren = root.children;
        //     boolean found=false;
        //     for (ContextInfo chld : rootchildren) { // same id could
        //                                             // have been
        //                                             // defined in
        //                                             // multiple
        //                                             // children?
        //         var the_map = chld.map;
        //         if (the_map.containsKey(id))
        //             if (!found) {
        //                 res=the_map.get(id); found=true;
        //             } else
        //                 myassert(false, "Symbol \""+id+"\" defined in multiple children of the root context");
        //     }
        // }
        myassert(res!=null, "Symbol \""+id+"\" not found");
        return res;
    }

}
