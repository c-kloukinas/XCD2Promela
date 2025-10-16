package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.*;

import org.antlr.v4.runtime.*;

public abstract class ComponentVisitor extends BasicVisitor {

    @Override
    public LstStr visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) {
        updateln(ctx);
        String compName = ctx.id.getText();
        ContextInfo framenow = env.get(env.size()-1);
        ContextInfoComp newctx
            = framenow.makeContextInfoComp(compName, false);
        env.add(newctx);
        // For components we create two files - an instance and a header.
        LstStr res = new LstStr(2);
        String header = "";
        String instance = "";
        LstStr argList = visit(ctx.param); // Identify parameters

        instance += Names.componentHeaderName(compName) + "(";
        int prmsz = argList.size();
        if (prmsz!=0) {  // these seem to be ignored by the translator
            instance += "/* Parameters ignored, passed through macros */ /* " +
                argList.get(0);
            if (prmsz>1)
             for (int i=1; i<prmsz; ++i)
                 instance += ","+argList.get(i);
            instance += " */";
        }
        for (String var : newctx.paramsORvars) {
            IdInfo info = getIdInfo(var);
            info.type = XCD_type.paramt;
            info.is_param = true;
            info.big_name = Names.paramName(compName, var);
        }
        newctx.params.addAll(newctx.paramsORvars);
        newctx.paramsORvars = new LstStr();
        instance += ")" + " {" + "\n\n" ;

        LstStr body_res = visit(ctx.body); // Visit the component body
        for (String var : newctx.paramsORvars) { // Identify variables
            IdInfo info = getIdInfo(var);
            info.type = XCD_type.vart;
            info.is_param = false;
        }
        newctx.vars.addAll(newctx.paramsORvars);
        newctx.paramsORvars = new LstStr();

                mywarning("\tTODO: complete the component code");
        // Any sub-component instances declared?
        if (newctx.subcomponents.size()>0) {
            String loop_offset = newgensym(compName);
            instance += "/* Loop to start all sub-component instances */\natomic_step {\n  int " + loop_offset
                + ";\n  " +loop_offset+" = 0;\n";
            for (String instance_name : newctx.subcomponents) {
                IdInfo info = getIdInfo(instance_name);
                String component_def = info.variableTypeName;
                boolean is_array = info.is_array;
                String sz = info.arraySz;
                if (is_array)
                    instance += "  do\n"
                        + "  :: "+ loop_offset + " < " + sz + " -> \n   ";
                instance +=
                    Names
                    .componentRunInstanceName(component_def
                                              , instance_name
                                              , ((is_array)
                                                 ? (loop_offset+ "++")
                                                 : "0"))
                    + "\n";
                if (is_array)
                    instance += "  :: else -> break;\n  od;\n"
                        + "  " + loop_offset + " = 0;\n";
            }
            instance += "}\n\n";
        }

        // only if it has a provided/consumer port
        if (hasProvidedPorts(newctx) || hasConsumerPorts(newctx))
            instance += Names.componentIConstraintsName(compName)+";\n\n";

        // only if it has a port (of any kind)
        if (hasPorts(newctx))
            instance += Names.componentRoleDataName(compName) + ";\n\n";

        // Action (i.e., port) parameters missing here
        mywarning("TODO: Action/port parameters missing)");
        instance += "// missing action/port parameters\n";

        for (String var : newctx.vars) {
            IdInfo info = getIdInfo(var);
            String big = info.big_name;
            header += "#define "
                + Names.typeOfVarDefName(compName, var)
                + " "
                + info.variableTypeName + "\n";
            String arrSz = "1"; // when there's no array, just a single instance
            if (info.is_array) {
                /* See getDataSize in XcdGenerator - seems to assume
                 * it'll be either a number or a component
                 * parameter */
                // arrSz=("Component_i_Param_N(CompositeName,CompositeID,"
                //        + compName
                //        + ",CompInstanceID,Instance,"
                //        + var + ")");
                arrSz = info.arraySz;
            }

            String type = component_typeof_id(big);
            String nm = component_variable_id(big, arrSz);
            String init = "";
            if (info.has_initVal) {
                mywarning("TODO: If value is a component param, the following doesn't work");
                // for component params it should be "Component_i_Param_N(CompositeName,CompositeID,"+ compName + ",CompInstanceID,Instance,"+ var + ")"
                // init = "=InitialValue(COMPONENT_"
                //     + compName
                //     + "_VAR_" + var +")";
                init = "="+info.initVal; /* wrong (?) on purpose, to
                                            see what comes out */
            } else init = "=000"; // default value
            String pre_nm = Names.varPreName(nm);
            instance += type + " " + nm + init +";\n";
            instance += type + " " + pre_nm + init +";\n";
        }

        instance += "}\n";
        // Create instance & header files
        { try (FileWriter inst
               = XCD2Promela.mynewoutput("COMPONENT_TYPE_"+compName+"_INSTANCE.pml");
               FileWriter hedr
               = XCD2Promela.mynewoutput("COMPONENT_TYPE_"+compName+".h")) {
                hedr.write(header);
                inst.write(instance);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }}
        int last = env.size()-1;
        ContextInfo lastctx = env.get(last);
        myassert(newctx == lastctx, "Context not the last element");
        env.remove(last);       // should match what was added
        res.add(instance);
        res.add(header);
        return res;
    }

    /*
     * The need for this method to avoid copying the same code in each
     * of the four port types, points to two solutions - 1) move the
     * common parts of the grammar rules to a new rule; 2) use
     * generics in this method to avoid using reflection.
     *
     * The 2nd way didn't work, at least as described in
     * https://stackoverflow.com/questions/9141960/generic-class-that-accepts-either-of-two-types
     * (factories for just the specific types we want to consider -
     * the type of ctx is still `T extends ParserRuleContext', so
     * reflection is still needed (probably would have worked in C++,
     * where for each type a different instantiation is created).
     *
     * So to avoid both code duplication and reflection, we need to
     * change the grammar itself.
     */
    class SomePort<T extends ParserRuleContext> {
        T ctx;
        XCD_type tp;
        LstStr ports;
        private SomePort(T actx, XCD_type atp, LstStr somePorts) {
            ctx = actx; tp = atp; ports = somePorts; } // use below factories
        /* Extra ComponentVisitor param in the factories for the
         * reason explained here (inner class needs an instance of the
         * enclosing class:
         * https://stackoverflow.com/questions/10301907/why-do-i-get-non-static-variable-this-cannot-be-referenced-from-a-static-contex
         */
        static SomePort<XCDParser.EmitterPortContext>
            makeNew(ComponentVisitor cv, XCDParser.EmitterPortContext ctx, XCD_type tp, LstStr p) {
            return cv.new SomePort<XCDParser.EmitterPortContext>(ctx, tp, p); }
        static SomePort<XCDParser.ConsumerPortContext>
            makeNew(ComponentVisitor cv, XCDParser.ConsumerPortContext ctx, XCD_type tp, LstStr p) {
            return cv.new SomePort<XCDParser.ConsumerPortContext>(ctx, tp, p); }
        static SomePort<XCDParser.RequiredPortContext>
            makeNew(ComponentVisitor cv, XCDParser.RequiredPortContext ctx, XCD_type tp, LstStr p) {
            return cv.new SomePort<XCDParser.RequiredPortContext>(ctx, tp, p); }
        static SomePort<XCDParser.ProvidedPortContext>
            makeNew(ComponentVisitor cv, XCDParser.ProvidedPortContext ctx, XCD_type tp, LstStr p) {
            return cv.new SomePort<XCDParser.ProvidedPortContext>(ctx, tp, p); }

    LstStr visitSomePort(// ParserRuleContext ctx, XCD_type tp, LstStr ports
                         ) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";          // Source
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        String compUnitId = framenow.compilationUnitID;

        try {
            // Class ctxcl = ctx.getClass();
            // Field fldid = ctxcl.getDeclaredField("id");
            // Object theid = fldid.get(ctx);
            // Class<?> fldcl = theid.getClass();
            // Method getText = fldcl.getDeclaredMethod("getText");
            // String portName = (String) (getText.invoke(theid));
            String portName = ctx.id.getText();

            // Field fldsz = ctxcl.getDeclaredField("size");
            // Object thesz = fldsz.get(ctx);
            var thesz = ctx.size;
            boolean is_arrayp = thesz!=null;
            String array_sz = (is_arrayp)
                ? visit((ParserRuleContext)thesz).get(0)
                : "1";

            String big_name = Names.portName(compUnitId, portName);
            addIdInfo(portName
                      , tp
                      // , "portType"
                      , false
                      , is_arrayp, array_sz
                      , false, ""
                      , big_name, ""
                      , compUnitId);
            ports.add(portName);
            ContextInfoCompPort newctx
                = framenow.makeContextInfoCompPort(portName, tp, false);
            env.add(newctx);
            // Lot's missing!!!

            int last = env.size()-1;
            ContextInfo lastctx = env.get(last);
            myassert(newctx == lastctx, "Context not the last element");
            env.remove(last);   // should match what was added
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return visitChildren(ctx); }
    }
    @Override
    public LstStr visitEmitterPort(XCDParser.EmitterPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return SomePort.makeNew(this, ctx, XCD_type.emittert, framenow.emitterprts).visitSomePort(); }
    @Override
    public LstStr visitConsumerPort(XCDParser.ConsumerPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return SomePort.makeNew(this, ctx, XCD_type.consumert, framenow.consumerprts).visitSomePort(); }
    @Override
    public LstStr visitRequiredPort(XCDParser.RequiredPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return SomePort.makeNew(this, ctx, XCD_type.requiredt, framenow.requiredprts).visitSomePort(); }
    @Override
    public LstStr visitProvidedPort(XCDParser.ProvidedPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return SomePort.makeNew(this, ctx, XCD_type.providedt, framenow.providedprts).visitSomePort(); }

    @Override
    public LstStr visitEventSignature(XCDParser.EventSignatureContext ctx) {
        /*
          One may wish to overload events(/methods).

          So we need to use the full event(/method) signature, i.e.,
          <name, param type...>. The parameter names are not part of
          the signature. Neither are the return type (methods) or any
          exceptions the method can throw (events don't return
          anything by definition, so cannot throw exceptions either),
          since exceptions are just a kind of (abnormal) return value.

          To represent these, we need:
          1) Name=String
          2) Type=String
          3) Sig=Pair<Name,Tuple<Type...>> // event/method name, param types...
          4) EventOverloads=Map<Name,Map<Sig>> // it's an error if a
                                               // sig is defined more
                                               // than once

          Also:
          5) FullSig=Tuple<Name // event/method name
                          , Tuple<<Pair<Type, Name>... // param types & names
                                  , Type // result type
                                  , Tuple<Type...>> // exception types
        */
        return visitChildren(ctx); }
    @Override
    public LstStr visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        /* Check comment in visitEventSignature() about overloading
         * and required types to support it. */
        return visitChildren(ctx); }

    @Override
    public LstStr visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";          // Source

        Token tk = (Token) ctx.elType;
        if (tk.getType() == XCDParser.TK_COMPONENT) { // sub-component instance
            var framenow = env.get(env.size()-1).you();
            String compUnitId = framenow.compilationUnitID;
            String sz = (ctx.size!=null) ? ctx.size.getText() : "1";
            String component_def = ctx.userdefined.getText();
            String instance_name = ctx.id.getText();
            addIdInfo(instance_name
                      , XCD_type.componentt
                      , component_def
                      , false
                      , (ctx.size!=null), sz
                      , false, "" // no init value
                      , "", ""    // big_name, prefix -- unused
                      , compUnitId);
            if (!(framenow instanceof ContextInfoComp)) {
                myassert(false
                         , "CompilationUnitID=\"" + framenow.compilationUnitID
                         + "\"\nType=\"" + framenow.type
                         + "\"\nParent=\"" + framenow.parent
                         + "\"\n# of Children=\"" + framenow.children.size()
                         + "\"\n"
                         + "Configuration is \"" + instance_name
                         + "\" of type \"" + component_def + "\"\n");
            } else {
                ((ContextInfoComp)framenow).subcomponents.add(instance_name);
            }
            res.add(s);
            return res;
        } else if (tk.getType() == XCDParser.TK_CONNECTOR) { // connector element

        } else {myassert(false, "Unknown element type inside component");}

        return visitChildren(ctx);
    }

    @Override
    public LstStr visitFormalParameters(XCDParser.FormalParametersContext ctx) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";

        if (ctx.par_pre!=null) {
            var resrec = visitChildren(ctx);
            int len1=(resrec!=null)?resrec.size():-1;
            int len2=ctx.pars.size()+1;
            myassert(len1 == len2 || len1==-1,
                     "Number of parameters doesn't match ("+len1+") vs ("+len2+")");

            //     s += visit(ctx.par_pre).get(0);
            //     for (var p : ctx.pars)
            //  s += ","+visit(p).get(0);
            s += resrec.get(0);
            resrec.remove(0);
            for (var p : resrec)
                s += "," + p;
            // System.err.println(" FormalParameters: "+s);
            res.add(s);
        }
        return res;
    }

    @Override
    public LstStr
        visitFormalParameter(XCDParser.FormalParameterContext ctx) {
        updateln(ctx);
        String nm = visit(ctx.prim_param).get(0);
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        var compId = framenow.compilationUnitID;
        var info = getIdInfo(nm);
        // A parameter's big name is:
        // // info.big_name = "Component_i_Param_N(CompositeName,CompositeID,"
        // //     + compId
        // //     + ",CompInstanceID,Instance,"
        // //     + nm + ")";
        // info.big_name = nm;     // Params don't need big names, they're fine.
        info.big_name = Names.paramName(compId, nm);
        var res = new LstStr();
        // System.err.println(" FormalParameter: \""+nm+"\" nm=\""+nm+"\"");
        // mywarning("MUSTFIX: Ignores type, array, initial value) - start from the bottom and move up...");
        String s = info.variableTypeName + " " + info.big_name;
        if (info.is_array) {
            mywarning("DANGER: parameter \"" + nm + "\" on line " + ctx.getSourceInterval().toString() + " is an array - good luck!");
            s += info.arraySz; }
        // mywarning("MUSTFIX: Ignores initial value) - start from the bottom and move up...");
        if (info.has_initVal) {
            mywarning("DANGER: parameter \"" + nm + "\" on line " + ctx.getSourceInterval().toString() + " has an initial value - good luck!");
            s += "=" + info.initVal; }

        framenow.params.add(nm);

        // System.err.println("Formal param: " + s);
        res.add(s);
        return res;
    }

    @Override public LstStr visitActualParameters(XCDParser.ActualParametersContext ctx) { return visitChildren(ctx); }
    @Override public LstStr visitActualParameter(XCDParser.ActualParameterContext ctx) { return visitChildren(ctx); }

    @Override public LstStr
        visitVariable_initialValue(XCDParser.Variable_initialValueContext ctx) {
        updateln(ctx);
        LstStr res = new LstStr();
        String s = "";

        if (ctx.itrue!=null)
            s = Names.True;
        else if (ctx.ifalse!=null)
            s = Names.False;
        else if (ctx.inum!=null)
            s = ctx.inum.getText();
        else {                    // ID
            mywarning("DANGER: Seems to assume that only a component parameter can be used as an initial value"); // see getInitialValue
            s = getIdInfo(ctx.icons.getText()).big_name;
        }

        res.add(s);
        return res;
    }

    @Override
    public LstStr visitArraySize(XCDParser.ArraySizeContext ctx) {
        updateln(ctx);
        mywarning("\t\tTODO: Array size should be a general expr normally, now a number or an ID only");
        LstStr res = new LstStr();
        String s = "";

        if (ctx.constant!=null)
            s = ctx.constant.getText();
        else
            s = getIdInfo(ctx.config_par.getText()).big_name;

        res.add(s);
        return res;
    }

    @Override public LstStr
        visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) {
        updateln(ctx);
        LstStr res = new LstStr();
        String varName = ctx.id.getText();
        String s = varName;
        String dtype = visit(ctx.type).get(0); // int, byte, bool, void, ID(long name)
        // if (dtype.equals("int")
        //     || dtype.equals("byte")
        //     || dtype.equals("bool")
        //     || dtype.equals("void"))
        //     {}
        boolean is_arrayp = ctx.size!=null;
        String array_sz = "1";  /* all variables are treated as arrays
                                 * (of size 1), to simplify
                                 * expressions */
        if (is_arrayp) {
            mywarning("DANGER: \"" + varName + "\" is an array");
            array_sz = visit(ctx.size).get(0);
        }
        boolean has_initValp = ctx.op!=null;
        String initVal = "0";   /* Zero seems like a good initial
                                 * value for all variables, given the
                                 * basic types we have */
        if (has_initValp) {
            mywarning("DANGER: \"" + varName + "\" has an initial value");
            initVal = visit(ctx.initval).get(0);
        }
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        String compUnitId = framenow.compilationUnitID;
        XCD_type tp = framenow.type;
        String bigname = "";
        if (tp == XCD_type.componentt) {
            bigname = Names.varName(compUnitId, varName);
        } else if (tp == XCD_type.rolet) {
// connector Customer_Cashier(Customer{pay}, Cashier{customer}) {
//   role Customer{
//     bool dummyVariable3 := true;
//
// "CONNECTOR_" + Customer_Cashier + _conn1_0 + "_ROLE_" + Customer + "_VAR_" + dummyVariable3
            String myConnector = getIdInfo(compUnitId).parent;
            bigname = Names.xVarInstanceName(myConnector
                // // "conn1" is a connector instance, "0" is its array index
                // + "MISSING_CONNECTOR_INSTANCE_UNKNOWN"
                                             , myConnector
                                             , varName
                // "conn1" is a connector instance, "0" is its array index
                                             , "MISSING_CONNECTOR_INSTANCE_UNKNOWN");
        } else
            myassert(false
                     , "Unknown context of variable declaration: "
                     + "frame parent=\"" + framenow.compilationUnitID
                     + "\" type=\"" + framenow.type + "\"");

        addIdInfo(varName
                  , XCD_type.vart
                  , dtype
                  , false /* not a param, right? Let's assume so and correct it when we know more about it */
                  , is_arrayp, array_sz
                  , has_initValp, initVal
                  , bigname, ""
                  , compUnitId);
        framenow.paramsORvars.add(varName);
        res.add(s);
        return res;
    }

    @Override
    public LstStr visitDataType(XCDParser.DataTypeContext ctx) {
        updateln(ctx);
        LstStr res = new LstStr();
        String s = "";
        if (ctx.basic!=null) {
            Token tk = (Token) ctx.basic;
            if (tk.getType() == XCDParser.TK_INTEGER)
                s = Names.Short;
            else if (tk.getType() == XCDParser.TK_BYTE)
                s = Names.Byte;
            else if (tk.getType() == XCDParser.TK_BOOL)
                s = Names.Bit;
            else if (tk.getType() == XCDParser.TK_VOID)
                s = Names.Void;
            // System.err.println("Basic type: "+ctx.basic.getText() +" "+s);
        } else                  // ID
            s = getIdInfo(ctx.getText()).big_name;
        res.add(s);
        // System.err.println("TYPE READ: " + s);
        return res;
    }

    boolean isComposite(ContextInfoComp info)
    { return info.subcomponents.size()!=0; }
    boolean hasProvidedPorts(ContextInfoComp info)
    { return info.providedprts.size()!=0; }
    boolean hasRequiredPorts(ContextInfoComp info)
    { return info.requiredprts.size()!=0; }
    boolean hasEmitterPorts(ContextInfoComp info)
    { return info.emitterprts.size()!=0; }
    boolean hasConsumerPorts(ContextInfoComp info)
    { return info.consumerprts.size()!=0; }
    boolean hasPorts(ContextInfoComp info)
    { return hasProvidedPorts(info) || hasRequiredPorts(info)
            || hasEmitterPorts(info) || hasConsumerPorts(info); }

String component_typeof_id(String var) { return Names.typeOfVar(var);
    // "TypeOf("+ var + ")";
}
    String component_variable_id(String var, String index)
    { return var + "[" + index + "]"; }
}
