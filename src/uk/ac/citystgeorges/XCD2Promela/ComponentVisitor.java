package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

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
        LstStr argList = (ctx.param!=null)?visit(ctx.param):new LstStr(); // Identify parameters

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
            info.big_name = newctx.getParamName(var);
        }
        newctx.params.addAll(newctx.paramsORvars);
        newctx.paramsORvars = new LstStr();
        instance += ")" + " {" + "\n\n" ;

        LstStr body_res = visit(ctx.body); // Visit the component body

        // add local enums here
        Map<String,IdInfo> compMap = newctx.map;
        // compMap.forEach((key, value) -> {
        //         if (value.type == XCD_type.enumt) {
        //             instance += value.variableTypeName;
        //         }
        //     });
        for (var key : compMap.keySet()) {
            var value = compMap.get(key);
            if (value.type == XCD_type.enumt) {
                mywarning("UNCHARTED: There's a local enum definition here! "
                          + value.variableTypeName);
                instance += value.variableTypeName;
            }
        }

        for (String var : newctx.paramsORvars) { // Identify variables
            IdInfo info = getIdInfo(var);
            info.type = XCD_type.vart;
            info.is_param = false;
        }
        newctx.vars.addAll(newctx.paramsORvars);
        newctx.paramsORvars = new LstStr();

        mywarning("\tTODO: complete the component code");
        java.util.function.BiFunction<String, LstStr, String> def
            = (String what, LstStr namelist) -> {
            String out = "";
            for (String name : namelist) {
                out += "#ifndef "+ what + name + "\n"
                    + "#define " + what + name + "\n"
                    + "#include \"" + what + name + ".h\"\n"
                    + "#endif\n";
            }
            return out;
        };
        // Collect typedefs
        String all_typedefs = def.apply("TYPE_", newctx.typedefs);
        // Collect enums
        String all_enums = def.apply("TYPE_", newctx.enums);
        // Collect sub-components
        LstStr subcomponent_types = new LstStr();
        for (var sub : newctx.subcomponents) {
            String st = getIdInfo(sub).variableTypeName;
            subcomponent_types.add(st);
        }
        String all_subcomponents = def.apply("COMPONENT_TYPE_", subcomponent_types);
        // Collector connectors
        String all_connectors = // def.apply("CONNECTOR_TYPE_", newctx.connectorInstances)
            "";
        // Any sub-component instances declared?
        if (newctx.subcomponents.size()>0) { // It's a composite component
            // for (var s : newctx.subcomponents)
            //     mywarning("Component " + compName
            //               + " has subcomponent instance " + s);
            // mywarning("XXXX: " + all_subcomponents);
            Utils.withInputAndFileToWrite
            ("/resources/composite-component.pml.template"
             , compName + "_COMPOSITE.h"
             , (String confFileContents) -> {
                String typedefsOrEnums = all_typedefs + all_enums;
                String composite_subType_HeaderFileOutput
                    = all_subcomponents + all_connectors;
                String composite_channelList = "";
                String subComponent_connectorActionArgsAndPvIndex_defines = "";
                String component_chans_param_ports_refinedICs_connectedPorts_sumOfConnectedPorts_roleDataUpdates_roleDataDecls_compCode_Instances = "";
                mywarning("\tTODO: Missing composite component code!");

                // XXX

                if (!typedefsOrEnums.equals(""))
                    typedefsOrEnums += "\n";
                String out=confFileContents
                    .replace("FOR$<typedefsOrEnums>", typedefsOrEnums)
                    .replace("$<compName>", compName)
                    .replace("$<composite_subType_HeaderFileOutput>"
                             , composite_subType_HeaderFileOutput)
                    .replace("FOR$<composite_channelList>"
                             , composite_channelList)
                    .replace("FOR$<subComponent_connectorActionArgsAndPvIndex_defines>"
                             , subComponent_connectorActionArgsAndPvIndex_defines)
                    .replace("FOR$<component_chans_param_ports_refinedICs_connectedPorts_sumOfConnectedPorts_roleDataUpdates_roleDataDecls_compCode_Instances>"
                             , component_chans_param_ports_refinedICs_connectedPorts_sumOfConnectedPorts_roleDataUpdates_roleDataDecls_compCode_Instances);
                return out;
            });

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
                init = "=" + Names.varNameComponentInitialValue(compName, var);
            } else init = "=000"; // default value
            String pre_nm = Names.varPreName(nm);
            instance += type + " " + nm + init +";\n";
            instance += type + " " + pre_nm + init +";\n";
        }

        instance += "}\n";
        // Create instance & header files
        {
            Utils.withFileWriteString("COMPONENT_TYPE_"+compName+".h"
                                      , header);
            Utils.withFileWriteString("COMPONENT_TYPE_"+compName+"_INSTANCE.pml"
                                      , instance);
        }
        int last = env.size()-1;
        ContextInfo lastctx = env.get(last);
        myassert(newctx == lastctx, "Context not the last element");
        env.remove(last);       // should match what was added
        res.add(instance);
        res.add(header);
        return res;
    }

    @Override
    public LstStr visitComponentPort(XCDParser.ComponentPortContext ctx) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";          // Source
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
        String array_sz = (is_arrayp)
            ? visit(thesz).get(0)
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
        res = visitChildren(ctx);

        int last = env.size()-1;
        ContextInfo lastctx = env.get(last);
        myassert(newctx == lastctx, "Context not the last element");
        env.remove(last);   // should match what was added
        return res;
    }
    @Override
    public LstStr visitEmitterPort(XCDParser.EmitterPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return visitChildren(ctx); }
    @Override
    public LstStr visitEmitterPort_event(XCDParser.EmitterPort_eventContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        // Need to visit the eventSignature *before* the constraints!
        LstStr res = visit(ctx.port_event);
        if (ctx.icontract!=null) res.addAll(visit(ctx.icontract));
        if (ctx.fcontract!=null) res.addAll(visit(ctx.fcontract));
        return res;
    }
    @Override
    public LstStr visitConsumerPort_event(XCDParser.ConsumerPort_eventContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        // Need to visit the eventSignature *before* the constraints!
        LstStr res = visit(ctx.port_event);
        if (ctx.icontract!=null) res.addAll(visit(ctx.icontract));
        if (ctx.fcontract!=null) res.addAll(visit(ctx.fcontract));
        return res;
 }
    @Override
    public LstStr visitRequiredPort_method(XCDParser.RequiredPort_methodContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        // Need to visit the methodSignature *before* the constraints!
        LstStr res = visit(ctx.port_method);
        if (ctx.icontract!=null) res.addAll(visit(ctx.icontract));
        if (ctx.fcontract!=null) res.addAll(visit(ctx.fcontract));
        return res;
 }
    @Override
    public LstStr visitProvidedPort_method(XCDParser.ProvidedPort_methodContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        // Need to visit the methodSignature *before* the constraints!
        LstStr res = visit(ctx.port_method);
        if (ctx.icontract!=null) res.addAll(visit(ctx.icontract));
        if (ctx.fcontract!=null) res.addAll(visit(ctx.fcontract));
        return res;
 }

    @Override
    public LstStr visitConsumerPort(XCDParser.ConsumerPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return visitChildren(ctx); }
    @Override
    public LstStr visitRequiredPort(XCDParser.RequiredPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return visitChildren(ctx); }
    @Override
    public LstStr visitProvidedPort(XCDParser.ProvidedPortContext ctx) {
        var framenow = (ContextInfoComp) env.get(env.size()-1);
        return visitChildren(ctx); }



    @Override
    public LstStr visitEventSignature(XCDParser.EventSignatureContext ctx) {
        // mywarning("visitEventSignature!!!");
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
          4) EventOverloads=Map<Name,Map<Sig, FullSig>> // it's an error if a
                                               // sig is defined more
                                               // than once

          Also:
          5) FullSig=Tuple<Name // event/method name
                          , Tuple<<Pair<Type, Name>... // param types & names
                                  , Type // result type
                                  , Tuple<Type...>> // exception types
        */
        Name event = new Name(ctx.id.getText());
        LstStr params = visit(ctx.params);
        // System.err.println("Params (size=" + params.size() + ") are \"" + params.toString() + "\"\n");
        Sig sig = new Sig();
        SeqOfNameTypePairs fullsig = new SeqOfNameTypePairs();
        int i=0;
        while (params.size()-i>0) {
            NameTypePair nt = new NameTypePair( new Name(params.get(i+0))
                                                , new Type(params.get(i+1)) );
            sig.add(nt.type);
            fullsig.add(nt);
            i+=2;
        }
        // System.err.println("Params (size=" + params.size() + ") are \"" + params.toString() + "\"\n"
        //                    + "Sig is \"" + sig.toString() + "\"\n"
        //                    + "Fullsig is \"" + fullsig.toString() + "\"\n");
        LstStr res = new LstStr();
        res.add(sig.toString());
        return res; }
    @Override
    public LstStr visitMethodSignature(XCDParser.MethodSignatureContext ctx) {
        /* Check comment in visitEventSignature() about overloading
         * and required types to support it. */
        // mywarning("visitMethodSignature!!!");
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
          4) EventOverloads=Map<Name,Map<Sig, FullSig>> // it's an error if a
                                               // sig is defined more
                                               // than once

          Also:
          5) FullSig=Tuple<Name // event/method name
                          , Tuple<<Pair<Type, Name>... // param types & names
                                  , Type // result type
                                  , Tuple<Type...>> // exception types
        */
        Name event = new Name(ctx.id.getText());
        LstStr params = visit(ctx.params);
        Sig sig = new Sig();
        SeqOfNameTypePairs fullsig = new SeqOfNameTypePairs();
        int i=0;
        while (params.size()-i>0) {
            NameTypePair nt = new NameTypePair( new Name(params.get(i+0))
                                                , new Type(params.get(i+1)) );
            sig.add(nt.type);
            fullsig.add(nt);
            i+=2;
        }
        // System.err.println("Params (size=" + params.size() + ") are \"" + params.toString() + "\"\n"
        //                    + "Sig is \"" + sig.toString() + "\"\n"
        //                    + "Fullsig is \"" + fullsig.toString() + "\"\n");
        LstStr res = new LstStr();
        res.add(sig.toString());
        return res; }

    @Override
    public LstStr visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";          // Source

        Token tk = (Token) ctx.elType;
        var framenow = env.get(env.size()-1).you();
        String compUnitId = framenow.compilationUnitID;
        String instance_name = ctx.id.getText();
        if (tk.getType() == XCDParser.TK_COMPONENT) { // sub-component instance
            String sz = (ctx.size!=null) ? ctx.size.getText() : "1";
            String component_def = ctx.userdefined.getText();
            String params = visit(ctx.params).get(0);
            mywarning("visitElementVariableDeclaration: I ignore component instance parameters");
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
                // mywarning(framenow.compilationUnitID
                //        + "'s subcomponent of type "
                //           + instance_name);
            }
        } else if (tk.getType() == XCDParser.TK_CONNECTOR) { // connector element
            String sz = (ctx.connsize!=null) ? visit(ctx.connsize).get(0) : "1";
            String connector_def
                = (ctx.userdefined!=null)
                ? ctx.userdefined.getText()
                : visit(ctx.basicConn).get(0);
            String params = visit(ctx.conn_params).get(0);
            addIdInfo(instance_name
                      , XCD_type.connectort
                      , connector_def
                      , false
                      , (ctx.connsize!=null), sz
                      , false, "" // no init value
                      , "", ""    // big_name, prefix -- unused
                      , compUnitId);
            mywarning("visitElementVariableDeclaration: I ignore connector instances");

        } else {myassert(false, "Unknown element type inside component");}

        res.add(s);
        return res;
    }

    @Override
    public LstStr visitFormalParameters(XCDParser.FormalParametersContext ctx) {
        updateln(ctx);
        var res = new LstStr();
        String s = "";

        if (ctx.par_pre!=null) {
            var resrec = visitChildren(ctx);
            // int len1=(resrec!=null)?resrec.size():-1;
            // int len2=ctx.pars.size()+1;
            // myassert(len1 == len2 || len1==-1,
            //          "Number of parameters doesn't match ("+len1+") vs ("+len2+")");

            // //     s += visit(ctx.par_pre).get(0);
            // //     for (var p : ctx.pars)
            // //  s += ","+visit(p).get(0);
            // s += resrec.get(0);
            // resrec.remove(0);
            // for (var p : resrec)
            //     s += "," + p;
            // // System.err.println(" FormalParameters: "+s);
            // res.add(s);
            res = resrec;
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
        info.big_name = framenow.getParamName(nm);
        var res = new LstStr();
        // System.err.println(" FormalParameter: \""+nm+"\" nm=\""+nm+"\"");
        // mywarning("MUSTFIX: Ignores type, array, initial value) - start from the bottom and move up...");
        // String s = info.variableTypeName + " " + info.big_name;
        res.add(info.variableTypeName);
        String s = info.big_name;
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

    @Override public LstStr visitActualParameters(XCDParser.ActualParametersContext ctx) {
        LstStr res = new LstStr();
        String s = "";
        if (ctx.arg_pre!=null) {
            s += visit(ctx.arg_pre).get(0);
            for (var another : ctx.args)
                s += visit(another);
        }
        res.add(s);
        return res; }
    @Override public LstStr visitActualParameter(XCDParser.ActualParameterContext ctx) {
        LstStr res = new LstStr();
        String s = ((ctx.id!=null)
                    ? ctx.id.getText()
                    : ((ctx.constant!=null)
                       ? ctx.constant.getText()
                       : "XCD@Expression"));
        res.add(s);
        return res; }

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
            bigname = Names.varNameComponent(compUnitId, varName);
        } else if ( (tp == XCD_type.emittert)
                    || (tp == XCD_type.consumert)
                    || (tp == XCD_type.requiredt)
                    || (tp == XCD_type.providedt) ) {
            bigname = Names.varNamePort(compUnitId, varName);
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
