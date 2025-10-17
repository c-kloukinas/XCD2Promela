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
    public LstStr visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) {
        LstStr res = visitChildren(ctx);
        ContextInfo framenow = env.get(env.size()-1);
        Map<String,IdInfo> map = framenow.map;
        map.forEach((key, value) -> {
                if ((value.type == XCD_type.enumt)
                    || (value.type == XCD_type.typedeft)) {
                    System.err.println("Creating file for enum/typedef: "
                                       + key
                                       + " Value : " + value.variableTypeName);
                    try (FileWriter typeFile
                         = XCD2Promela.mynewoutput("TYPE_"+key+".h")) {
                        typeFile.write(value.variableTypeName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        return res;
    }

    @Override
    public LstStr visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) {
        updateln(ctx);
        ContextInfo framenow = env.get(env.size()-1);
        Name enumName = new Name(ctx.id.getText());
        LstStr res = new LstStr();
        Sig values = new Sig();
        String s = "";

        s = "#ifndef " + enumName.toString() + "\n"
            + "#define " + enumName.toString() + "  mtype\n"
            + "mtype = {\n\t";
        var constants = ctx.constants;
        int size = constants.size();
        myassert(constants!=null && size>0
                 , "ERROR: An enum type must have values");
        var name1st = constants.get(0).getText();
        values.add(new Type(name1st));
        s += name1st;
        int i=1;
        while (i<size) {
            String name = constants.get(i++).getText();
            values.add(new Type(name));
            s += ", " + name;
        }
        s += " }\n#endif\n";

        addIdInfo(enumName.toString()
                  , XCD_type.enumt
                  , s
                  , false
                  , false, ""
                  , false, ""
                  , enumName.toString(), ""
                  , framenow.compilationUnitID);
        for (var value : values)
            addIdInfo(value.toString()
                      , XCD_type.enumt
                      , false
                      , false, ""
                      , false, ""
                      , value.toString(), ""
                      , enumName.toString());
        res.add(s);
        return res;
    }
    @Override
    public LstStr visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) {
        updateln(ctx);
        ContextInfo framenow = env.get(env.size()-1);
        String typedefOf = ctx.replacementOf.getText();
        String definition = ctx.id.getText();
        LstStr res = new LstStr();
        Sig values = new Sig();
        String s = "";

        s = "#ifndef " + typedefOf + "\n"
            + "#define " + typedefOf + " " + definition + "\n"
            + "#endif\n";

        addIdInfo(typedefOf
                  , XCD_type.typedeft
                  , s
                  , false
                  , false, ""
                  , false, ""
                  , typedefOf, ""
                  , framenow.compilationUnitID);
        res.add(s);
        return res;
    }
    /*
     * The configuration is treated here
     */
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
