package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.*;
/*
  ArrayList<String> res = new ArrayList<String>();
  String s = "";

  res.add(s);
  return res;

  var framenow = env.get(env.size()-1);
  var symbolInfo = getIdInfo(symbol);
*/

public class XCD2PromelaVisitor extends ConnectorVisitor {
    XCD2PromelaVisitor() {
        String compilationUnitID = "@root"; // root
        // initialise env, so that "result", etc. are known IDs
        env = new ArrayList<ContextInfo>();
        env.add(new ContextInfo());
        String array_sz = "";
        String initVal = "";
        String var_prefix = "";
        // String kword = "\\result";
        LstStr kwords = new LstStr();
        kwords.add("\\result");
        kwords.add("\\exception");
        kwords.add("\\nothing");
        kwords.add("\\in");
        for (String kword : kwords )
            { String bigname = kword;
                addIdInfo(kword
                          , XCD_type.resultt
                          , kword
                          , false
                          , false, array_sz // array?
                          , false, initVal  // initial value?
                          , bigname, var_prefix
                          , compilationUnitID); }
    }

    /*
      Always empty methods
    */
    @Override
    public LstStr visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) { return visitChildren(ctx); }

    @Override
    public LstStr visitCompilationUnit(XCDParser.CompilationUnitContext ctx) {
        LstStr res=null;
        if (ctx.config!=null) {
            ContextInfo framenow = env.get(env.size()-1); // root
            ContextInfoComp newctx
                = framenow.makeContextInfoComp("@configuration", false);
            env.add(newctx);
            res=visitChildren(ctx);
            try (InputStream in
                 = XCD2Promela.class.getResourceAsStream("/resources/configuration.pml.template");
                 BufferedReader reader
                 = new BufferedReader(new InputStreamReader(in));
                 FileWriter theConfig
                 = XCD2Promela.mynewoutput("configuration.pml")) {
                if (newctx.map.size()!=2) {
                    mywarning("Configuration should have exactly one component instance - instead, it has " + newctx.map.size());
                    for (var key : newctx.map.keySet()) {
                        var val=newctx.map.get(key);
                        System.err.println("Instance \"" + key
                                           + "\" of component type \"" + val.variableTypeName
                                           + "\"\n");
                    }
                    myassert(false, "");
                }
                myassert(newctx.subcomponents.size()==1
                         , "Configuration should have exactly one component,"
                         + " instead it has "
                         + newctx.subcomponents.size());
                String instName = newctx.subcomponents.get(0);
                IdInfo compTypeInfo = newctx.map.get(instName);
                String compType = compTypeInfo.variableTypeName;
                myassert(compTypeInfo.type == XCD_type.componentt
                         , "Configuration instance type is not a component"
                         + "Instance \"" + instName
                         + "\" of component type \"" + compType
                         + "\"");
                String out = reader.lines() // Stream<String>
                    .collect(Collectors.joining("\n")) // String
                    .replace("$<compType>", compType);
                theConfig.write(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            res=visitChildren(ctx);
        }
        return res;
    }

}
