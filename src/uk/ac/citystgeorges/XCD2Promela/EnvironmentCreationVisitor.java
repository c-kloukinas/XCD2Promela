package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
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

    @Override
    String component_variable_id(String var, ArraySizeContext index)
    { return var
            + "["
            + ((index==null)
               ? "1"
               : visit(index).get(0))
            + "]"; }

   /**
     * Constructs that create their own environment.
     */

    @Override public T visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) {
        String compilationUnitID = "@root"; // root
        // initialise env, so that "result", etc. are known IDs
        myassert(getSTbl().size()==0, "Expected no environments!");
        getSTbl().add(rootContext);
        LstStr kwords = new LstStr();
        // kwords.add(keywordResult);
        // kwords.add(keywordException);
        // kwords.add(keywordSkip);
        // kwords.add(keywordIn);
        /* Add basic types in the list of key words, so that
         * translate_ID can find them */
        kwords.add(keywordVoid);
        kwords.add(keywordBool);
        kwords.add(keywordByte);
        kwords.add(keywordShort);
        kwords.add(keywordInteger);
        for (String kword : kwords )
            { addIdInfo(kword
                        , XCD_type.typet
                        , kword
                        , false
                        , (ArraySizeContext)null
                        , (VariableDefaultValueContext)null
                        , compilationUnitID); }

        return visitChildren(ctx);
    }

    // private T registerNewEnvironment(String name, ParserRuleContext ctx) {
    //     return registerNewEnvironment(name, ctx, XCD_type.unknownt);
    // }
    private T registerNewEnvironment(String name, ParserRuleContext ctx
                                     , XCD_type tp
                                     , ArraySizeContext arraySize
                                     , SymbolTable newctx) {
        return registerNewEnvironment(name, ctx, tp, arraySize, newctx, null);
    }
    private T registerNewEnvironment(String name, ParserRuleContext ctx
                                     , XCD_type tp
                                     , ArraySizeContext arraySize
                                     , SymbolTable newctx
                                     , TranslatorI tr) {
        return registerNewEnvironment(name, ctx, tp, arraySize, newctx, tr
                                      , (ParserRuleContext context)
                                      -> {return visitChildren(context);});
    }
    private T registerNewEnvironment(String name, ParserRuleContext ctx
                                     , XCD_type tp
                                     , ArraySizeContext arraySize
                                     , SymbolTable newctx
                                     , TranslatorI tr
                                     , Function<ParserRuleContext, T> vis) {
        updateln(ctx);
        SymbolTable framenow = symbolTableNow();
        /*
         * // Add new context to the childer of the current one.
         * // framenow.children.add(newctx);
         *
         * NO! The newctx has already been registered by
         * makeContext...!
         */

        // Add the name to the current environment
        globalIdInfo = addIdInfo(name, tp, false
                                 // roles,portvars,ports,sub-component/connector
                                 , arraySize
                                 , null, framenow.compilationUnitID);

        // push new environment context
        pushSymbolTable(newctx);

        // Add new names in the new context
        T res = defaultResult();
        /* Methods/events treat their children out-of-order. */
        if (newctx.type!=XCD_type.methodt
            && newctx.type!=XCD_type.eventt) {
            res = vis.apply(ctx); // These results are ignored
        }

        // mywarning("***Currently env has size: "
        //           + getSTbl().size()
        //           + " name is: " + name);
        if (tr!=null)
            res = tr.translate(this, ctx);

        // pop new environment context
        popLastSymbolTable(newctx);
        return res;
    }

    @Override public T visitCompositeOrConnectorDeclaration(XCDParser.CompositeOrConnectorDeclarationContext ctx) {
        SymbolTable framenow = symbolTableNow();
        XCD_type myType
            = (ctx.tp.getType()==XCDParser.TK_CONNECTOR)
            ? XCD_type.connectort
            : XCD_type.compositet; // may have used TK_COMPOSITE or TK_COMPONENT
        mySyntaxCheck(myType!=XCD_type.connectort
                      ||
                      (ctx.cparams==null
                       && ctx.xparams!=null)
                      , "A connector expects as arguments a list of its roles"
                      + " and their port variables");
        mySyntaxCheck(myType!=XCD_type.compositet
                      ||
                      (ctx.cparams!=null
                       && ctx.xparams==null)
                      , "A composite does not accept a list of roles"
                      + " and their port variables as parameters");

        SymbolTableComposite newctx
            = framenow.makeSymbolTableComposite(ctx.id.getText(), ctx
                                                , myType, false);
        TranslatorI tr = new TranslatorCompositeOrConnectorDeclarationContext();
        var res = registerNewEnvironment(ctx.id.getText(), ctx
                                         , myType
                                         // there's no size part
                                         , (ArraySizeContext) null
                                         , newctx, tr);

        if (myType==XCD_type.connectort) {
            /* Check that the set of roles and the set of roles listed in
             * the parameters are the same.
             */
            // mywarning("There are " + newctx.roles.size() + " roles defined "
            //           + "and "
            //           + newctx.roles2portvarsInParams.keySet().size()
            //           + " roles used");
            Set<String> rolesDefined
                = Set.copyOf(newctx.subcomponents);
            // elements.stream()
            //  .filter(el -> el.role!=null)
            //  .map(el -> el.role).toList());
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
                List<SymbolTable> rolesContext
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
                SymbolTableComponent roleEnv
                    = (SymbolTableComponent) (rolesContext.get(0));
                Set<? extends String> portVarsDefined
                    = USet.setUnion(roleEnv.compConstructs.providedprts
                                    , roleEnv.compConstructs.consumerprts
                                    , roleEnv.compConstructs.requiredprts
                                    , roleEnv.compConstructs.emitterprts);
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
        } else {
            myassert(myType==XCD_type.compositet
                     , "Current declaration is not a composite.");
        }

        return res;
    }

    @Override public T visitComponentOrRoleDeclaration(XCDParser.ComponentOrRoleDeclarationContext ctx) {
        updateln(ctx);
        XCD_type myType
            = (ctx.struct.getType()==XCDParser.TK_COMPONENT)
            ? XCD_type.componentt
            : XCD_type.rolet;
        String myName = ctx.id.getText();
        SymbolTable framenow = symbolTableNow();
        mySyntaxCheck(myType!=XCD_type.componentt
                      || framenow.type==XCD_type.roott
                      , "Component "
                      + myName
                      + " must be defined at the global context"
                      + " - parent is "
                      + printFrame(framenow));
        mySyntaxCheck(myType!=XCD_type.rolet
                      || framenow.type==XCD_type.connectort
                      , "Role must be defined inside connectors"
                      + " - parent is "
                      + printFrame(framenow));
        TranslatorI tr = null;
        if (myType==XCD_type.rolet)
            ((SymbolTableComposite)framenow).subcomponents.add(myName);
        // if (myType==XCD_type.componentt) // do this for roles as well.
        tr = new TranslatorComponentOrRoleDeclarationContext();
        SymbolTable newctx
            = framenow.makeSymbolTableComponent(myName, ctx
                                                , myType , false);
        return registerNewEnvironment(myName, ctx
                                      , myType
                                      , (ArraySizeContext) null
                                      , newctx, tr);
    }

    @Override public T visitPortDeclaration(XCDParser.PortDeclarationContext ctx) {
        updateln(ctx);
        var framenow = symbolTableNow();
       // TK_EMITTER/CONSUMER/REQUIRED/PROVIDED
        int myTypeOfPort=ctx.type.getType();
        boolean imVariable=(ctx.valOrVar.getType()==XCDParser.TK_PORTVAR);
        mySyntaxCheck(!imVariable
                      || framenow.type==XCD_type.rolet
                      , "Only roles can have port variables "
                      + "(components have ports) "
                      + printFrame(framenow));
        mySyntaxCheck(imVariable
                      || framenow.type==XCD_type.componentt
                      , "Only components can have ports "
                      + "(roles have port variables) "
                      + printFrame(framenow));
        XCD_type myType
            = (imVariable)
            ? portTypeToken2PortVarType.get(myTypeOfPort)
            : portTypeToken2PortType   .get(myTypeOfPort);
        String portName = ctx.id.getText();
        XCDParser.ArraySizeContext thesz = ctx.size;
        LstStr portList
            = ((SymbolTableComponent)framenow).getPortList(myTypeOfPort);

        myassert(myType!=null
                 && portName!=null
                 && portList != null, "Error: unknown kind of port");
        // addIdInfo(portName
        //           , tp
        //           // , "portType"
        //           , false
        //           , array_sz
        //           , (VariableDefaultValueContext) null
        //           , compUnitId);
        portList.add(portName);

        SymbolTablePort newctx
            = framenow.makeSymbolTablePort(portName, ctx, myType, false);
        return registerNewEnvironment(portName, ctx, myType, thesz, newctx);
    }

    @Override public T visitMethodContract(XCDParser.MethodContractContext ctx) {
        updateln(ctx);
        T res = visit(ctx.port_method);
        if (ctx.icontract!=null)
            res.addAll(visit(ctx.icontract));
        if (ctx.fcontract!=null)
            res.addAll(visit(ctx.fcontract));
        popLastSymbolTable();   // pop duplicate method frame
        return res;
    }

    @Override public T visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        updateln(ctx);
        var myParentFrame = symbolTableNow();
        boolean imaMethod
            = (myParentFrame.type==XCD_type.providedt)
            || (myParentFrame.type==XCD_type.requiredt)
            || (myParentFrame.type==XCD_type.providedvart)
            || (myParentFrame.type==XCD_type.requiredvart);
        mySyntaxCheck(imaMethod || (ctx.rettype==null)
                      , "Events cannot have a return type");
        mySyntaxCheck(imaMethod || (ctx.exc_pre==null)
                      , "Events cannot throw exceptions");
        var myGrandparentFrame = myParentFrame.parent;
        boolean imaInAComponent = myGrandparentFrame.type==XCD_type.componentt;
        myassert(imaMethod
                 || (myParentFrame.type==XCD_type.emittert)
                 || (myParentFrame.type==XCD_type.consumert)
                 || (myParentFrame.type==XCD_type.emittervart)
                 || (myParentFrame.type==XCD_type.consumervart)
                 , "Method: Not defined inside a port?!?!"
                 + printFrameAndItsParent(myParentFrame, myGrandparentFrame));
        myassert(imaInAComponent
                 || myGrandparentFrame.type==XCD_type.rolet
                 , "Method: Not defined inside a component/role?!?!?"
                 + printFrameAndItsParent(myParentFrame, myGrandparentFrame));
        String eventNameStr = ctx.id.getText();
        Name eventName = new Name(eventNameStr);
        SymbolTablePort myCompParent = (SymbolTablePort)myParentFrame;
        LstStr methodOrEventNames = null;
        if (imaMethod)
            methodOrEventNames
                = myCompParent.portConstructs
                .basicMethodNames;
        else
            methodOrEventNames
                = myCompParent.portConstructs
                .basicEventNames;
        methodOrEventNames.add(eventNameStr);
        // Construct and register new environment for event/method
        SymbolTable newctx
            = myParentFrame.makeSymbolTableMethod(eventNameStr
                                                  , (imaMethod
                                                     ? XCD_type.methodt
                                                     : XCD_type.eventt)
                                                  , ctx);
        T res = registerNewEnvironment(eventNameStr
                                       , ctx
                                       , (imaMethod
                                          ? XCD_type.methodt
                                          : XCD_type.eventt)
                                       , null // there's no size part
                                       , newctx);
        /* registerNewEnvironment pops the context - re-push it so
         * that interaction/functional constraints will be processed
         * inside it */
        pushSymbolTable(newctx);
        if (ctx.params!=null)
            res = visit(ctx.params);
        else
            res = defaultResult();

        // visit exceptions
        if (ctx.exc_pre!=null) {
            /* register exceptions in the method/event and the global
             * namespace */
            LstStr exceptions
                = ((SymbolTableMethod)newctx).methodConstructs.exceptions;
            List<Token> excs = new ArrayList<Token>();
            // Treat 1st exception
            excs.add(ctx.exc_pre);
            excs.addAll(ctx.excs);
            for (var excT : excs) {
                String exc = excT.getText();
                rootContext.allExceptions.add(exc);
                /* add exceptions in the environment of this
                 * method/event */
                IdInfo idinfo = addIdInfo(exc
                                          , XCD_type.exceptiont
                                          , false
                                          , (ArraySizeContext)null
                                          , (VariableDefaultValueContext)null
                                          , newctx.compilationUnitID);
                idinfo.translation.add(Names.exceptionName(exc));
            }
        }

        var thisEventFrame = (SymbolTableMethod) symbolTableNow();
        // mywarning("XXX: " +
        //           printFrameItsParentAndItsGrandparent(thisEventFrame
        //                                             , myParentFrame
        //                                             , myGrandparentFrame));

        Sig theSig = new Sig();
        SeqOfTypeNamePairs theSigWithNames = new SeqOfTypeNamePairs();
        int sz = res.size();
        for (int i=0; i<sz-1; ++i) {
            Type theType = new Type(res.get(i));
            Name theName = new Name(res.get(++i));
            theSig.add(theType);
            theSigWithNames.addPair(theType, theName);
        }
        MethodStructure theEventM = null;
        EventStructure  theEventE = null;
        LstStr exceptions = null;
        if (imaMethod) {
            T ret = defaultResult();
            ret = visit(ctx.rettype);
            theEventM
                = new MethodStructure(eventName, theSig
                                      , theSigWithNames
                                      , exceptions
                                      , new Type(ret.get(0)));
            // store it in your symbol table too
            thisEventFrame.methodStructure = theEventM;
            // store it in your IdInfo too - how?
            globalIdInfo.methodStructure = theEventM;
            // mywarning("sigFull is not null for method " + eventName
            //           + " return type is " + ret.get(0));
            // mywarning("DUMP: " + theEventM);
        } else {
            theEventE
                = new EventStructure (eventName
                                      , theSig, theSigWithNames
                                      , exceptions);
            // store it in your symbol table too
            thisEventFrame.methodStructure = theEventE;
            // store it in your IdInfo too - how?
            globalIdInfo.methodStructure = theEventE;
        }
        // Map<Name, Map<String, EventStructure>> eventOverloads
        var theMapE
            = myCompParent.portConstructs.eventOverloads;
        var theMapM
            = myCompParent.portConstructs.methodOverloads;
        var framenow = symbolTableNow();
        String msg = printFrameItsParentAndItsGrandparent(framenow
                                                          , framenow.parent
                                                          , myParentFrame);
        myassert
            ((imaMethod && theMapM!=null)
             || (!imaMethod && theMapE!=null)
             , (imaMethod ? "Method" : "Event")
             + ": Expected to be called as a grandchild "
             + "of a component/connector\nInstead, " + msg);

        var eventNm =
            // eventNameStr
            eventName
            ;
        if (imaMethod) {
            if (!theMapM.containsKey(eventNm))
                theMapM.put(eventNm
                            , new HashMap<Sig, MethodStructure>());
            mySyntaxCheck(!theMapM.get(eventNm).containsKey(theSig)
                          , "Cannot overload the same signature" + theSig);
            // The slot has a map, so add the actual event structure inside it
            theMapM.get(eventNm).put(theSig, theEventM);
            // mywarning("port: " + myCompParent.compilationUnitID
            //           + " Placed " + eventNm + " with sig " + theEventM);
            // mywarning("Method structure entered "
            //           + ((SymbolTablePort) (symbolTableNow().parent))
            //           .portConstructs.methodOverloads
            //           .get(eventNm).get(theSig) );
        } else {
            if (!theMapE.containsKey(eventNm))
                theMapE.put(eventNm
                            , new HashMap<Sig, EventStructure >());
            mySyntaxCheck(!theMapE.get(eventNm).containsKey(theSig)
                          , "Cannot overload the same signature" + theSig);
            // The slot has a map, so add the actual event structure inside it
            theMapE.get(eventNm).put(theSig, theEventE);
            // mywarning("port: " + myCompParent.compilationUnitID
            //           + " Placed " + eventNm + " with sig " + theEventE);
            // mywarning("Event structure entered "
            //           + ((SymbolTablePort) (symbolTableNow().parent))
            //           .portConstructs.eventOverloads
            //           .get(eventNm).get(theSig) );
        }
        globalSeqOfTypeNamePairs = new SeqOfTypeNamePairs();
        return res;
    }

    @Override public T visitInlineFunctionDeclaration(XCDParser.InlineFunctionDeclarationContext ctx) {
        SymbolTable framenow = symbolTableNow();
        String functionName = ctx.id.getText();
        mywarning("TODO: InlineFunctionDeclaration: MUST COMPLETE (root, composite, component can all declare inline functions!");
        return defaultResult();

        // LstStr inlineFunctionDecls
        //     = ((framenow.type==XCD_type.componentt)
        //        ? ((SymbolTableComponent)framenow).compConstructs.inlineFunctionDecls
        //        : ((framenow.type==XCD_type.rolet)
        //           ?((SymbolTableComponent)framenow).compConstructs.inlineFunctionDecls
        //           : null));
        // myassert(inlineFunctionDecls!=null
        //          , "visitInlineFunctionDeclaration: framenow ("
        //          +  framenow.compilationUnitID
        //          + ") is of unsupported type "
        //          + framenow.type);
        // inlineFunctionDecls.add(functionName);
        // SymbolTableFunction newctx
        //     = framenow.makeSymbolTableFunction(ctx.id.getText(), ctx);
        // return registerNewEnvironment(ctx.id.getText(), ctx
        //                               , XCD_type.functiont
        //                               , null // there's no size part
        //                               , newctx);
    }


//     /**
//      * Constructs that do NOT create their own environment.
//      *
//      * However, they introduce new names into the current environment.
//      */

    @Override public T visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitEnumDeclaration called");
        SymbolTable framenow = symbolTableNow();
        Name enumName = new Name(ctx.id.getText());
        CommonConstructs theCommonConstructs = null;
        if (framenow==rootContext)
            theCommonConstructs = ((SymbolTableRoot)framenow).commonConstructs;
        else if (framenow.type==XCD_type.compositet
                 || framenow.type==XCD_type.connectort)
            theCommonConstructs
                = ((SymbolTableComposite)framenow).compConstructs;
        else if (framenow.type==XCD_type.componentt
                 || framenow.type==XCD_type.rolet)
            theCommonConstructs
                = ((SymbolTableComponent)framenow).compConstructs;
        else
            myassert(false
                     , "Enum inside unsupported construct " + framenow.type);
        theCommonConstructs.enums.add(""+enumName);
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
        // Are you global, inside a component, or inside a role?
        int globOrCompOrRole = 0; // Global
        String myComp = "";
        String myConn = "";
        String myRole = "";
        if (framenow.type==XCD_type.roott && framenow==rootContext) {
            globOrCompOrRole = 0;
        } else if (framenow.type==XCD_type.compositet
                   || framenow.type==XCD_type.componentt) {
            globOrCompOrRole = 1; // Inside a composite/component
            myComp = framenow.compilationUnitID;
        } else if (framenow.type==XCD_type.connectort
                   || framenow.type==XCD_type.rolet) {
            globOrCompOrRole = 2; // Inside a connector/role
            myRole = framenow.compilationUnitID;
            myConn = framenow.parent.compilationUnitID;
        } else
            myassert(false, "Enum " + enumName
                     + " is inside a construct " + framenow.type
                     + " that doesn't support enums");

        String en = enumName.toString();

        IdInfo enumTypeInfo = addIdInfo(en
                                        , XCD_type.enumt
                                        , "" // valuesAsAString - a hack...
                                        , false
                                        , null
                                        , null
                                        , framenow.compilationUnitID);

        String enumFullName
            = (globOrCompOrRole==0)
            ? Names.enumGlobalTypeName(en)
            : (globOrCompOrRole==1
               ? Names.enumCompTypeName(myComp, en)
               : Names.enumRoleTypeName(myConn, myRole, en));
        enumTypeInfo.translation
            .add(enumFullName);
        // mywarning("Added enum type \"" + enumName.toString()
        //           + "\" with values " + values.toString()
        //           + " and s is \n" + s);
        LstStr translatedValues = new LstStr();
        for (var value : values) {
            String vl = value.toString();
            IdInfo valInfo = addIdInfo(vl
                                       , XCD_type.enumvalt
                                       , false
                                       , null
                                       , null
                                       , enumName.toString());
            valInfo.translation
                .add(globOrCompOrRole==0
                     ? Names.enumGlobalValueName(vl)
                     : (globOrCompOrRole==1
                        ? Names.enumCompValueName(myComp, vl)
                        : Names.enumRoleValueName(myConn, myRole, vl)));
            translatedValues.add(valInfo.translation.get(0));
            // mywarning("Added enum value \"" + value.toString()
            //        + "\" for enum type \"" + enumName.toString()
            //        + "\"");
        }
        final String valuesAsAString
            = translatedValues.stream().collect(Collectors.joining(", "));
        enumTypeInfo.variableTypeName = valuesAsAString;

        // produce translation
        Utils.withInputAndFileToWrite
                    ("/resources/enum.h.template"
                     , "TYPE_" + enumFullName + ".h"
                     , (String confFileContents) -> {
                return confFileContents
                    .replace("$<name>", enumFullName)
                    .replace("FOR$<values>", valuesAsAString);
                    });
        return defaultResult();
    }

    @Override public T visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitTypeDefDeclaration called");
        SymbolTable framenow = symbolTableNow();
        String newtype = ctx.newtype.getText();
        String definition = ctx.existingtype.getText();
        CommonConstructs theCommonConstructs = null;
        if (framenow==rootContext)
            theCommonConstructs
                = ((SymbolTableRoot)framenow).commonConstructs;
        else if (framenow.type==XCD_type.compositet
                   || framenow.type==XCD_type.connectort)
            theCommonConstructs
                = ((SymbolTableComposite)framenow).compConstructs;
        else if (framenow.type==XCD_type.componentt
                   || framenow.type==XCD_type.rolet)
            theCommonConstructs
                = ((SymbolTableComponent)framenow).compConstructs;
        else
            myassert(false
                     , "Typedef inside unknown construct " + framenow.type);
        theCommonConstructs.typedefs.add(newtype);

        // Are you global, inside a component, or inside a role?
        int globOrCompOrRole = 0; // Global
        String myComp = "";
        String myConn = "";
        String myRole = "";
        if (framenow.type==XCD_type.roott && framenow==rootContext) {
            globOrCompOrRole = 0;
        } else if (framenow.type==XCD_type.compositet
                   || framenow.type==XCD_type.componentt) {
            globOrCompOrRole = 1; // Inside a composite/component
            myComp = framenow.compilationUnitID;
        } else if (framenow.type==XCD_type.connectort
                   || framenow.type==XCD_type.rolet) {
            globOrCompOrRole = 2; // Inside a connector/role
            myRole = framenow.compilationUnitID;
            myConn = framenow.parent.compilationUnitID;
        }
        String typedefFullName
            = (globOrCompOrRole==0)
            ? Names.typedefGlobalTypeName(newtype)
            : (globOrCompOrRole==1
               ? Names.typedefCompTypeName(myComp, newtype)
               : Names.typedefRoleTypeName(myConn, myRole, newtype));

        IdInfo typedefIdInfo
            = addIdInfo(newtype
                        , XCD_type.typedeft
                        , typedefFullName // a hack
                        , false
                        , null
                        , null
                        , framenow.compilationUnitID);
        mywarning("Added type \"" + newtype + "\" (\"" + typedefFullName
                  + "\") as a newname for \"" + definition + "\"");
        String basicTranslation
            = new TranslatorPrimaryContext()
            .translate_ID(this, definition);
        typedefIdInfo
            .translation
            .add(basicTranslation);

        // produce translation
        Utils.withInputAndFileToWrite
                    ("/resources/typedef.h.template"
                     , "TYPE_" + typedefFullName + ".h"
                     , (String confFileContents) -> {
                return confFileContents
                    .replace("$<name>", typedefFullName)
                    .replace("$<definition>", basicTranslation);
                    });
        return defaultResult();
    }

    @Override public T visitVarDecl(XCDParser.VarDeclContext ctx) {
        updateln(ctx);
        String varName = ctx.id.getText();
        DataTypeContext dtypeCtx = ctx.type; // int, byte, bool, void, ID(long name)
        String dtype = visit(dtypeCtx).get(0);
        // always non-null for user-defined variables/parameters
        ArraySizeContext array_sz
            = // (ctx.size!=null)
            // ? ctx.size
            // : singleElementArraySize
            ctx.size            // parameters: we don't want to add a
                                // default size
            ;
        VariableDefaultValueContext initVal = ctx.initval;
        T res = visitVarOrParamDecl(dtypeCtx
                                    , varName
                                    , array_sz
                                    , initVal
                                    , !readingParams);
        if (!readingParams) {
            IdInfo idinfo = getIdInfo(varName);
            SymbolTable framenow = symbolTableNow();
            if(framenow.type!=XCD_type.methodt
               && framenow.type!=XCD_type.eventt)
                idinfo.has_post = true;
            else
                idinfo.type=XCD_type.mparamt;
        }
        return res;
    }

    public T visitVarOrParamDecl(DataTypeContext dtype
                                 , String varName
                                 , ArraySizeContext array_sz
                                 , VariableDefaultValueContext initVal
                                 , boolean isVar) {
        return visitVarOrParamDecl(dtype.getText()
                                   , varName
                                   , array_sz
                                   , initVal
                                   , isVar);
    }

    public T visitVarOrParamDecl(String dtype
                                 , String varName
                                 , ArraySizeContext array_sz
                                 , VariableDefaultValueContext initVal
                                 , boolean isVar) {
        var framenow = (SymbolTable) symbolTableNow();
        String compUnitId = framenow.compilationUnitID;
        XCD_type tp = framenow.type;

        IdInfo idinfo = addIdInfo(varName
                                  , (isVar
                                     ? XCD_type.vart
                                     : XCD_type.paramt)
                                  , dtype
                                  , !isVar
                                  , array_sz
                                  , initVal
                                  , compUnitId);
        if (tp==XCD_type.methodt
            || tp==XCD_type.eventt
            || tp==XCD_type.functiont) {
            T res = new T();
            res.add(dtype); res.add(varName);
            // mywarning("visitVarOrParamDecl: Have added "
            //           + varName
            //           + " into the current environment, with type "
            //           + dtype
            //           + " as a "
            //           + (readingParams?"parameter":"variable"));
            idinfo.translation.add(varName);
            return res;
        } else {
            // mywarning("visitVarOrParamDecl: Not inside a method/etc. "
            //           + varName
            //           + " ignoring type "
            //           + dtype);
            LstStr paramsOrvars = null;
            String trans = "";
            CompositeConstructs theEnvCompConstructs = null;
            if (framenow instanceof SymbolTableComposite) {
                theEnvCompConstructs
                    = ((SymbolTableComposite)framenow).compConstructs;
            } else if (framenow instanceof SymbolTableComponent) {
                theEnvCompConstructs
                    = ((SymbolTableComponent)framenow).compConstructs;
            }
            paramsOrvars = isVar
                ? theEnvCompConstructs.vars
                : theEnvCompConstructs.params;

            if (tp==XCD_type.compositet
                || tp==XCD_type.connectort) {
                var theEnv = (SymbolTableComposite)framenow;
                paramsOrvars = isVar
                    ? theEnv.compConstructs.vars
                    : theEnv.compConstructs.params;
                mySyntaxCheck(!isVar
                              , "Composite components and connectors cannot"
                              + " have primitive variables");
                trans = ((tp==XCD_type.compositet)
                         ? Names.paramNameComponent(theEnv.compilationUnitID
                                                    , varName)
                         : Names.paramNameConnector(theEnv.compilationUnitID
                                                    , varName));
            } else if (tp==XCD_type.componentt
                       || tp==XCD_type.rolet) {
                var theEnv = (SymbolTableComponent)framenow;
                trans = ((tp==XCD_type.componentt)
                         ? (isVar
                            ? Names.varNameComponent(theEnv.compilationUnitID
                                                     , varName)
                            : Names.paramNameComponent(theEnv.compilationUnitID
                                                       , varName))
                         : (isVar
                            ?Names.varNameRole(theEnv.parent.compilationUnitID
                                               , theEnv.compilationUnitID
                                               , varName)
                            :Names.paramNameRole(theEnv.parent.compilationUnitID
                                                 , theEnv.compilationUnitID
                                                 , varName)));
            } else {
                myassert(false
                         , (isVar?"Variable ":"Parameter ")
                         + varName
                         + " declaration appears in an unexpected context "
                         + tp);
            }
            paramsOrvars.add(varName);
            idinfo.translation.add(trans);
        }

        if (array_sz!=null) {
            T res = visit(array_sz);
            if (res.size()!=0) {
                idinfo.translation.add(res.get(0));
                // mywarning("VarDecl: " + varName
                //           + " has arraySz " + res.get(0));
            } else {
                idinfo.translation.add("UNKNOWN_ARRAY_SZ_TRANSLATION");
                myassert(false
                         , "VarDecl: " + varName
                         + " has arraySz UNKNOWN_ARRAY_SZ_TRANSLATION");
            }
        } else {
            idinfo.translation.add("1");
            // mywarning("VarDecl: " + varName
            //           + " has arraySz 1");
        }
        if (initVal!=null) {
            T res = visit(initVal);
            if (res.size()!=0) {
                idinfo.translation.add(res.get(0));
                // mywarning("VarDecl: " + varName
                //           + " has initVal " + res.get(0));
            } else {
                idinfo.translation.add("UNKNOWN_INITVAL_TRANSLATION");
                myassert(false
                         , "VarDecl: " + varName
                         + " has initVal UNKNOWN_INITVAL_TRANSLATION");
            }
        } else {
            idinfo.translation.add(null);
                // mywarning("VarDecl: " + varName
                //           + " has initVal null");
        }
        return defaultResult();
    }

    @Override public T visitElementVarDecl(XCDParser.ElementVarDeclContext ctx) {
        updateln(ctx);
        Token tk = (Token) ctx.elType;
        var framenow = symbolTableNow().you();
        String compUnitId = framenow.compilationUnitID;
        String instance_name = ctx.id.getText();
        if (tk.getType() == XCDParser.TK_COMPONENT // sub-component instance
            || tk.getType() == XCDParser.TK_COMPOSITE) {
            ArraySizeContext sz = ctx.size;
            String component_def = ctx.userdefined.getText();

            if (ctx.params!=null)
                visit(ctx.params);
            addIdInfo(instance_name
                      , XCD_type.componentt
                      , component_def
                      , false
                      , sz
                      , null // no init value
                      , compUnitId);
            if (!(framenow instanceof SymbolTableComposite)) {
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
                ((SymbolTableComposite)framenow).subcomponents.add(instance_name);
                // mywarning(framenow.compilationUnitID
                //        + "'s subcomponent of type "
                //           + instance_name);
            }
        } else if (tk.getType() == XCDParser.TK_CONNECTOR) { // connector element
            ArraySizeContext sz = ctx.connsize;
            String connector_def
                = (ctx.userdefined!=null)
                ? ctx.userdefined.getText()
                : (ctx.basicConnProc!=null
                   ? Names.connProcedural()
                   : Names.connAsynchronous());
            // String params = visit(ctx.conn_params).get(0);
            addIdInfo(instance_name
                      , XCD_type.connectort
                      , connector_def
                      , false
                      , sz
                      , null    // no init value
                      , compUnitId);

            if (framenow instanceof SymbolTableComposite) {
                ((SymbolTableComposite)framenow).subconnectors.add(instance_name);
                // mywarning(framenow.compilationUnitID
                //        + "'s subcomponent of type "
                //           + instance_name);
            } else if (framenow instanceof SymbolTableComposite) {
                ((SymbolTableComposite)framenow).subconnectors.add(instance_name);
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
        SymbolTable framenow = symbolTableNow(); // root
        SymbolTableComposite newctx
            = framenow.makeSymbolTableComposite("@configuration", ctx
                                                , XCD_type.configt, false);
        getSTbl().add(newctx);
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
                // if (newctx.subcomponents.size()!=1) {
                //     mywarning("Configuration should have exactly one"
                //               + " component instance, but instead, it has "
                //               + (newctx.subcomponents.size()));
                //     for (var key : newctx.map.keySet()) {
                //         var val=newctx.map.get(key);
                //         System.err.println("Instance \"" + key
                //                            + "\" of component type \""
                //                            + val.variableTypeName + "\"\n");
                //     }
                // }
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
        int last = getSTbl().size()-1;
        SymbolTable lastctx = getSTbl().get(last);
        myassert(newctx == lastctx, "Context not the last element");
        getSTbl().remove(last);   // should match what was added
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
        SymbolTableComposite framenow = (SymbolTableComposite) symbolTableNow();
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
    public T visitAssertDeclaration(XCDParser.AssertDeclarationContext ctx) {
        updateln(ctx);
        var tr = new TranslatorAssertDeclarationContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitDataType(XCDParser.DataTypeContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorDataTypeContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitConditionalExpression(XCDParser.ConditionalExpressionContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorConditionalExpressionContext();
        res = tr.translate(this, ctx);
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
    public T visitSet(XCDParser.SetContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorSetContext();
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

    @Override
    public T visitUnaryExpressionNotPlusMinus(XCDParser.UnaryExpressionNotPlusMinusContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorUnaryExpressionNotPlusMinusContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitPrimary(XCDParser.PrimaryContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorPrimaryContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitALiteral(XCDParser.ALiteralContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorALiteralContext();
        res = tr.translate(this, ctx);
        return res;
    }

    @Override
    public T visitArraySize(XCDParser.ArraySizeContext ctx) {
        updateln(ctx);
        T res = visitChildren(ctx);
        var tr = new TranslatorArraySizeContext();
        res = tr.translate(this, ctx);
        return res;
    }

    /**
     * In these cases order of visiting children matters - have to
     * visit the method/event definition *before* its
     * interaction/functional constraints, so that the method/event
     * parameter names are known symbols.
     */

    private T visitEventOrMethodOutOfOrder(ParserRuleContext eventOrMethod
                              , ParserRuleContext ic1
                              , ParserRuleContext fc1
                              , ParserRuleContext ic2
                              , ParserRuleContext fc2) {
        updateln(eventOrMethod);
        var framebefore = symbolTableNow();
        String msgbef = "visitEventOrMethodOutOfOrder: starting with "
            + printFrameAndItsParent(framebefore, framebefore.parent);
        myassert(framebefore.type==XCD_type.emittert
                 || framebefore.type==XCD_type.consumert
                 || framebefore.type==XCD_type.requiredt
                 || framebefore.type==XCD_type.providedt
                 || framebefore.type==XCD_type.emittervart
                 || framebefore.type==XCD_type.consumervart
                 || framebefore.type==XCD_type.requiredvart
                 || framebefore.type==XCD_type.providedvart
                 , msgbef);
        T res = visit(eventOrMethod);
        var framenow = symbolTableNow();
        String msg = "visitEventOrMethodOutOfOrder: current "
            + printFrameAndItsParent(framenow, framenow.parent);
        myassert(framenow.type==XCD_type.methodt
                 || framenow.type==XCD_type.eventt
                 , msg);

        if (ic1!=null) res = aggregateResult(res, visit(ic1));
        if (fc1!=null) res = aggregateResult(res, visit(fc1));
        if (ic2!=null) res = aggregateResult(res, visit(ic2));
        if (fc2!=null) res = aggregateResult(res, visit(fc2));

        /* Event environment had been pushed twice, to ensure that
         * interaction/functional constraints are treated inside it */
        popLastSymbolTable();
        return res;
    }

    // @Override
    // public T visitEmitterPortvar_event(XCDParser.EmitterPortvar_eventContext ctx) {
    //     return visitEventOrMethodOutOfOrder(ctx.event_sig
    //                                         , ctx.icontract, null, null, null);
    // }

    // @Override
    // public T visitRequiredPortvar_method(XCDParser.RequiredPortvar_methodContext ctx) {
    //     return visitEventOrMethodOutOfOrder(ctx.method_sig
    //                                         , ctx.icontract, null, null, null);
    // }

    // @Override
    // public T visitProvidedPort_method(XCDParser.ProvidedPort_methodContext ctx) {
    //     return visitEventOrMethodOutOfOrder(ctx.port_method
    //                                         , ctx.icontract, ctx.fcontract
    //                                         , null, null);
    // }

    /**
     * These need a simple translation.
     */

    @Override public T visitCompositeElement(XCDParser.CompositeElementContext ctx) {
        readingParams=false;
        T res = visitChildren(ctx);
        readingParams=true;
        return res;
    }

    @Override
    public T visitComponentElement(XCDParser.ComponentElementContext ctx) {
        readingParams=false;
        T res = visitChildren(ctx);
        readingParams=true;
        return res;
    }

    @Override public T visitArgumentList(XCDParser.ArgumentListContext ctx) { return visitChildren(ctx); }

    @Override public T visitFunctionInvocation(XCDParser.FunctionInvocationContext ctx) { return visitChildren(ctx); }


    @Override public T visitStatement(XCDParser.StatementContext ctx) {
        updateln(ctx);
        var tr = new TranslatorStatementContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitAssignment(XCDParser.AssignmentContext ctx) {
        updateln(ctx);
        var tr = new TranslatorAssignmentContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitAssignmentExpression(XCDParser.AssignmentExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorAssignmentExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitConditionalOrExpression(XCDParser.ConditionalOrExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorConditionalOrExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitConditionalAndExpression(XCDParser.ConditionalAndExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorConditionalAndExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitInclusiveOrExpression(XCDParser.InclusiveOrExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorInclusiveOrExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitExclusiveOrExpression(XCDParser.ExclusiveOrExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorExclusiveOrExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitAndExpression(XCDParser.AndExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorAndExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitShiftExpression(XCDParser.ShiftExpressionContext ctx) {
        updateln(ctx);
        var tr = new TranslatorShiftExpressionContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitVariableDefaultValue(XCDParser.VariableDefaultValueContext ctx) { return visitChildren(ctx); }

    @Override public T visitConnectorParameterList(XCDParser.ConnectorParameterListContext ctx) { return visitChildren(ctx); }
    @Override public T visitConnectorArgumentList(XCDParser.ConnectorArgumentListContext ctx) { return visitChildren(ctx); }
    @Override public T visitConnectorArgument(XCDParser.ConnectorArgumentContext ctx) { return visitChildren(ctx); }

    @Override public T visitComponentInteractionConstraint(XCDParser.ComponentInteractionConstraintContext ctx) {
        SymbolTable framenow = symbolTableNow();
        SymbolTable parent = framenow.parent;
        SymbolTable grandParent = parent.parent;
        mySyntaxCheck(grandParent.type==XCD_type.componentt
                      , "Component interaction constraints can only be"
                      + " used in component port methods - instead use\n"
                      + "\t\"( allows: guard; ensures: statements; )+\"");
        EventStructure methodStructure = null;
        if (framenow instanceof SymbolTableMethod)
            methodStructure = ((SymbolTableMethod)framenow).methodStructure;
        else
            myassert(false, "Current symbol table is not of a method type.");
        T resAccepts = defaultResult();
        T resWaits = defaultResult();
        if (ctx.accept!=null) {
            resAccepts = visit(ctx.accept);
            mySyntaxCheck(   parent.type==XCD_type.consumert
                          || parent.type==XCD_type.providedt
                          , "Accepts constraints can only be used in a"
                          + " consumer or provided port - instead use\n"
                          + "\t\"( when: guard; )?\"");
            methodStructure.x_constraintsAccepts = resAccepts;
        }
        if (ctx.wait!=null) {
            resWaits = visit(ctx.wait);
            methodStructure.x_constraintsWaits = resWaits;
        }
        return defaultResult();
    }
    @Override public T visitRoleInteractionConstraint(XCDParser.RoleInteractionConstraintContext ctx) {
        SymbolTable framenow = symbolTableNow();
        SymbolTable parent = framenow.parent;
        SymbolTable grandParent = parent.parent;
        mySyntaxCheck(grandParent.type==XCD_type.rolet
                      , "Role interaction constraints can only be"
                      + " used in role port methods - instead use\n"
                      + "\t\"( accepts: domain-guard; )? ( waits: guard; )?\"");
        EventStructure methodStructure = null;
        if (framenow instanceof SymbolTableMethod)
            methodStructure = ((SymbolTableMethod)framenow).methodStructure;
        else
            myassert(false, "Current symbol table is not of a method type.");
        T resAllows = defaultResult();
        T resEnsures = defaultResult();
        if (ctx.allows!=null && ctx.allows.size()!=0) {
            for (var cons : ctx.allows) {
                resAllows.add(visit(cons).get(0));
            }
            methodStructure.x_constraintsAllows = resAllows;
            for (var cons : ctx.alEnsures) {
                resEnsures.add(visit(cons).get(0));
            }
            methodStructure.x_constraintsEnsures = resEnsures;
        }
        return defaultResult();
    }
    @Override public T visitGeneralFunctionalContract(XCDParser.GeneralFunctionalContractContext ctx) {
        SymbolTable framenow = symbolTableNow();
        SymbolTable parent = framenow.parent;
        SymbolTable grandParent = parent.parent;
        mySyntaxCheck(grandParent.type==XCD_type.componentt
                      , "Functional constraints can only be used in"
                      + " component port methods.\n"
                      + "Roles only have interaction constraints\n"
                      + "\t\"( allows: guard; ensures: statements; )+\"");
        mySyntaxCheck(ctx.wGuards.size()==0 // !wGuards -> ...
                      || (   parent.type==XCD_type.emittert
                          || parent.type==XCD_type.requiredt)
                      , "Can only use \"(when: guard ; ensures: statements;)*\""
                      + " in emitter and required ports - use\n"
                      + "\"(requires: guard; ensures: statements;)*\" instead"

                      // + "\nwGuards==null: "
                      // + (ctx.wGuards==null) + ctx.wGuards.size()
                      // + "\nparent.type==XCD_type.emittert: "
                      // + (parent.type==XCD_type.emittert)
                      // + "\nparent.type==XCD_type.requiredt: "
                      // + (parent.type==XCD_type.requiredt)
                      // + "\nFrames are: "
                      // + printFrameItsParentAndItsGrandparent(framenow
                      //                                        , parent
                      //                                        , grandParent)
                      );
        mySyntaxCheck(ctx.rGuards.size()==0 // !rGuards -> ...
                      || (   parent.type==XCD_type.consumert
                          || parent.type==XCD_type.requiredt
                          || parent.type==XCD_type.providedt)
                      , "Can only use \"(requires: guard ; ensures: statements;)*\""
                      + " in consumer, required and provided ports - use\n"
                      + "\"(when: guard; ensures: statements;)*\" instead"

                      // + "\nrGuards==null: "
                      // + (ctx.rGuards==null) + ctx.rGuards.size()
                      // + "\nparent.type==XCD_type.consumert: "
                      // + (parent.type==XCD_type.consumert)
                      // + "\nparent.type==XCD_type.requiredt: "
                      // + (parent.type==XCD_type.requiredt)
                      // + "\nparent.type==XCD_type.providedt: "
                      // + (parent.type==XCD_type.providedt)
                      // + "\nFrames are: "
                      // + printFrameItsParentAndItsGrandparent(framenow
                      //                                        , parent
                      //                                        , grandParent)
                      );
        EventStructure methodStructure = null;
        if (framenow instanceof SymbolTableMethod)
            methodStructure = ((SymbolTableMethod)framenow).methodStructure;
        else
            myassert(false, "Current symbol table is not of a method type.");
        T reswGuards = defaultResult();
        T reswEnsures = defaultResult();
        T resrGuards = defaultResult();
        T resrEnsures = defaultResult();
        if (ctx.wGuards!=null) {
            for (var cons : ctx.wGuards)
                reswGuards.add(visit(cons).get(0));
            methodStructure.f_constraintsWhen = reswGuards;
            for (var cons : ctx.wEnsures) {
                // setup assignable parameter list for emitters/producers
                globalSeqOfTypeNamePairs = new SeqOfTypeNamePairs();
                globalSeqOfTypeNamePairs.addAll( methodStructure.full_sig );
                reswEnsures.add(visit(cons).get(0));
                if (globalSeqOfTypeNamePairs.size()!=0) {
                    // some parameters have not been assigned yet
                    mywarning("These parameters have not been assigned:");
                    for (var v : globalSeqOfTypeNamePairs) {
                        mywarning("\t" + v.name + " of type " + v.type);
                    }
                    // var debug = symbolTableNow();
                    // mywarning
                    //     (printFrameItsParentAndItsGrandparent
                    //      (debug
                    //       , debug.parent
                    //       , debug.parent.parent));
                    mySyntaxCheck
                        (globalSeqOfTypeNamePairs.size()==0
                         , "\n\t*** Each ensures clause should provide values for all event/method parameters."
                         + "\n\t*** To choose any valid value, use range/set assignment, e.g., "
                         + globalSeqOfTypeNamePairs.get(0).name
                         + " "
                         + keywordIn
                         + " [0, <typeUpperBound>] (here \""
                         + globalSeqOfTypeNamePairs.get(0).type
                         + "\")");

                }
            }
            methodStructure.f_constraintsWEnsures = reswEnsures;
        }
        if (ctx.rGuards!=null) {
            for (var cons : ctx.rGuards)
                resrGuards.add(visit(cons).get(0));
            methodStructure.f_constraintsRequires = resrGuards;
            for (var cons : ctx.rEnsures)
                resrEnsures.add(visit(cons).get(0));
            methodStructure.f_constraintsREnsures = resrEnsures;
        }
        return defaultResult();
    }

    @Override public T visitStatements(XCDParser.StatementsContext ctx) {
        // too simple to create a translator - just separating
        // statements with a ';'
        T res = defaultResult();
        String s = "\t" + visit(ctx.stmt1).get(0);
        for (var stmt : ctx.stmts) {
            s += ";\n\t" + visit(stmt).get(0);
        }
        mywarning("XXX: Read the following code: \n" + s);
        res.add(s);
        return res;
    }

    /**
     * Nothing to be done for these; default behaviour suffices - here
     * for completion.
     */
    @Override public T visitLeftHandSide(XCDParser.LeftHandSideContext ctx) {
        updateln(ctx);
        var tr = new TranslatorLeftHandSideContext();
        T res = tr.translate(this, ctx);
        return res;
    }

    @Override public T visitArrayAccess(XCDParser.ArrayAccessContext ctx) {
        T name = defaultResult();
        // at this point I don't know if I'm looking for an LHS or an RHS
        name.addAll( getAssignableName( ctx.arrayName.getText() ) );
        myassert(name.size()==1
                 , "ArrayAccess: The name translated to more than one thing!"
                 + name.size());
        T expr = visit(ctx.arrIndex);
        if (expr.size()!=1)
            mywarning("ArrayAccess: The expression has "
                      + expr.size()
                      + " elements!");
        T res = defaultResult();
        res.add(name.get(0)
                     + "["
                     + expr.get(0)
                     + "]");
        return res;
    }

    @Override public T visitArrayIndex(XCDParser.ArrayIndexContext ctx) { return visitChildren(ctx); }

    @Override public T visitExpression(XCDParser.ExpressionContext ctx) { return visitChildren(ctx); }

    @Override public T visitFormalParameters(XCDParser.FormalParametersContext ctx) { return visitChildren(ctx); }

    @Override public T visitGeneralInteractionContract(XCDParser.GeneralInteractionContractContext ctx) { return visitChildren(ctx); }
        /**
         * {@inheritDoc}
         *
         * <p>The default implementation returns the result of calling
         * {@link #visitChildren} on {@code ctx}.</p>
         */
    @Override public T visitConnectorArgument_pv(XCDParser.ConnectorArgument_pvContext ctx) { return visitChildren(ctx); }
    /**
     * Miscellaneous helper functions
     */
    @Override
    T getAssignableName(String name) {
        T res = defaultResult();
        var tr = new TranslatorPrimaryContext();
        String s = tr.translate_ID(this, name);
        IdInfo sRecord = getIdInfo(name);
        if (globalAssignableName) {
            myassert((sRecord.type==XCD_type.mparamt)
                     || (sRecord.type==XCD_type.vart)
                     , "LeftHandSide: How can one assign into \""
                     + name
                     + "\" (" + s + ") ? " + sRecord.type);
            if (sRecord.type==XCD_type.vart
                && sRecord.has_post)
                s = Names.varPostName(s);
        }
        if (sRecord.type==XCD_type.mparamt) {
            // could this be a method parameter?
            // SymbolTableMethod framenow = (SymbolTableMethod) symbolTableNow();
            if (globalSeqOfTypeNamePairs.size()!=0) {
                // mywarning("Parameter "
                //           + name
                //           + " found - removing it when size: "
                //           + globalSeqOfTypeNamePairs.size());
                globalSeqOfTypeNamePairs
                    .removeIf( x -> x.name.toString().equals(name) );
                // mywarning("Now globalSeqOfTypeNamePairs has size: "
                //           + globalSeqOfTypeNamePairs.size());
            }
        } else {
            myassert(!globalAssignableName
                     || (sRecord.type==XCD_type.vart)
                     , "LHS " + name + " (" + s
                     + ") is of type " + sRecord.type);
        }
        res.add( s );
        return res;
    }

    boolean readingParams = true; // TODO: check it's used correctly!
    // used by registerNewEnvironment to pass the IdInfo created back
    // to the current visitor method
    IdInfo globalIdInfo = null;

    private String printFrame(SymbolTable fr) {
        return "frame " + fr.compilationUnitID + " of type " + fr.type;
    }
    private String printFrameAndItsParent(SymbolTable fr, SymbolTable frP) {
        return printFrame(fr)
            + ", parent " + printFrame(frP);
    }
    private String printFrameItsParentAndItsGrandparent(SymbolTable fr
                                                        , SymbolTable frP
                                                        , SymbolTable frG) {
        return printFrameAndItsParent(fr, frP)
            + ", grandparent " + printFrame(frG);
    }

    static private ArraySizeContext makeSingleElementArray() {
        if (singleElementArraySize!=null)
            return singleElementArraySize;
        org.antlr.v4.runtime.CharStream input
            = org.antlr.v4.runtime.CharStreams.fromString("byte foo [1];");
        // create a lexer that feeds off of input CharStream
        XCDLexer lexer
            = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        org.antlr.v4.runtime.CommonTokenStream tokens
            = new org.antlr.v4.runtime.CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser
            = new XCDParser(tokens);
        // begin parsing at "varDecl" parse rule
        VarDeclContext tree
            = (VarDeclContext) parser.varDecl();
        return tree.size;
    }
    final static ArraySizeContext singleElementArraySize
        = makeSingleElementArray();

    final static Map<Integer, XCD_type> portTypeToken2PortType
        = makePortTypeToken2PortType();
    final static Map<Integer, XCD_type> portTypeToken2PortVarType
        = makePortTypeToken2PortVarType();
    static private Map<Integer, XCD_type> makePortTypeToken2PortType() {
        Map<Integer, XCD_type> map = new HashMap<Integer, XCD_type>();
        map.put(XCDParser.TK_EMITTER , XCD_type.emittert);
        map.put(XCDParser.TK_CONSUMER, XCD_type.consumert);
        map.put(XCDParser.TK_PROVIDED, XCD_type.providedt);
        map.put(XCDParser.TK_REQUIRED, XCD_type.requiredt);
        return map;
    }
    static private Map<Integer, XCD_type> makePortTypeToken2PortVarType() {
        Map<Integer, XCD_type> map = new HashMap<Integer, XCD_type>();
        map.put(XCDParser.TK_EMITTER , XCD_type.emittervart);
        map.put(XCDParser.TK_CONSUMER, XCD_type.consumervart);
        map.put(XCDParser.TK_PROVIDED, XCD_type.providedvart);
        map.put(XCDParser.TK_REQUIRED, XCD_type.requiredvart);
        return map;
    }
}
