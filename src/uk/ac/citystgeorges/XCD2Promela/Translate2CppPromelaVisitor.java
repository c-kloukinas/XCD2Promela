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

public class Translate2CppPromelaVisitor extends BaseVisitor {

    /**
     * Constructs that create their own environment.
     */

    @Override public Void visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) { return visitChildren(ctx); }

    @Override public Void visitConnectorDeclaration(XCDParser.ConnectorDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitRoleDeclaration(XCDParser.RoleDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitRolePortvar(XCDParser.RolePortvarContext ctx) { return visitChildren(ctx); }

    @Override public Void visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitComponentPort(XCDParser.ComponentPortContext ctx) { return visitChildren(ctx); }

    @Override public Void visitMethodSignature(XCDParser.MethodSignatureContext ctx) { return visitChildren(ctx); }

    @Override public Void visitEventSignature(XCDParser.EventSignatureContext ctx) { return visitChildren(ctx); }

    @Override public Void visitInlineFunctionDeclaration(XCDParser.InlineFunctionDeclarationContext ctx) { return visitChildren(ctx); }


    /**
     * Constructs that do NOT create their own environment.
     *
     * However, they introduce new names into the current environment.
     */

    @Override public Void visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) { return visitChildren(ctx); }

    @Override public Void visitFormalParameter(XCDParser.FormalParameterContext ctx) { return visitChildren(ctx); }

    @Override public Void visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) { return visitChildren(ctx); }

    /*
     * The configuration is treated here
     */
    @Override
    public Void visitCompilationUnit(XCDParser.CompilationUnitContext ctx) {
        return visitChildren(ctx);
        // Void res=null;
        // if (ctx.config!=null) {
        //     res=visitTheConfiguration(ctx);
        // } else {
        //     res=visitChildren(ctx);
        // }
        // return res;
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

    @Override public Void visitConnectorParameter(XCDParser.ConnectorParameterContext ctx) { return visitChildren(ctx); }

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
    protected ContextInfoComp getContext4Comp(String nm) {
        ContextInfoComp res = null; // rootContext;
        return res;
    }

    protected IdInfo getIdInfo(String id) {
        int last = env.size()-1;
        IdInfo res = null;
        while (res == null && last > -1) {
            Map<String,IdInfo> the_map = env.get(last).map;
            if (the_map.containsKey(id))
                res=the_map.get(id);
            --last;
        }
        myassert(res!=null, "Symbol \""+id+"\" not found");
        return res;
    }

}
