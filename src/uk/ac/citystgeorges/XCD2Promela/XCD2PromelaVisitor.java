package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
        updateln(ctx);
        LstStr res = visitChildren(ctx);
        // mywarning("visitCompilationUnits called");
        ContextInfo framenow = env.get(env.size()-1);
        Map<String,IdInfo> map = framenow.map;
        map.forEach((key, value) -> {
                if ((value.type == XCD_type.enumt)
                    || (value.type == XCD_type.typedeft)) {
                    // System.err.println("Creating file for enum/typedef: "
                    //                    + key
                    //                    + " Value : " + value.variableTypeName);
                    Utils.withFileWriteString("TYPE_"+key+".h"
                                              , value.variableTypeName);
                }//  else {
                //     System.err.println("No, the type of \""
                //                        + key
                //                        + "(" + value.big_name + ")\" is \""
                //                        + value.type + "\" - ignored\n");
                // }
            });
        return res;
    }

    @Override
    public LstStr visitEnumDeclaration(XCDParser.EnumDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitEnumDeclaration called");
        ContextInfo framenow = env.get(env.size()-1);
        Name enumName = new Name(ctx.id.getText());
        LstStr res = new LstStr();
        Sig values = new Sig();
        String s = "";

        s = "#ifndef " + enumName.toString() + "\n"
            + "#define " + enumName.toString() + "  mtype\n"
            + "mtype = { ";
        var constants = ctx.constants;
        int size = constants.size();
        myassert(constants!=null && size>0
                 , "ERROR: An enum type must have values");
        var name1st = ctx.constant_pre.getText();
        values.add(new Type(name1st));
        s += name1st;
        for (var cnst : constants) {
            String name = cnst.getText();
            values.add(new Type(name));
            s += ", " + name;
        }
        s += " }\n#else\n#error \"Enum "
            + enumName.toString() + " defined already\"\n#endif\n";

        addIdInfo(enumName.toString()
                  , XCD_type.enumt
                  , s
                  , false
                  , false, ""
                  , false, ""
                  , enumName.toString(), ""
                  , framenow.compilationUnitID);
        // mywarning("Added enum type \"" + enumName.toString()
        //           + "\" with values " + values.toString()
        //           + " and s is \n" + s);
        for (var value : values) {
            addIdInfo(value.toString()
                      , XCD_type.enumvalt
                      , false
                      , false, ""
                      , false, ""
                      , value.toString(), ""
                      , enumName.toString());
           // mywarning("Added enum value \"" + value.toString()
           //        + "\" for enum type \"" + enumName.toString()
           //        + "\"");
        }
        res.add(s);
        return res;
    }
    @Override
    public LstStr visitTypeDefDeclaration(XCDParser.TypeDefDeclarationContext ctx) {
        updateln(ctx);
        // mywarning("visitTypeDefDeclaration called");
        ContextInfo framenow = env.get(env.size()-1);
        String newtype = ctx.newtype.getText();
        String definition = visit(ctx.existingtype).get(0);

        LstStr res = new LstStr();
        Sig values = new Sig();
        String s = "";

        s = "#ifndef " + newtype + "\n"
            + "#define " + newtype + " " + definition + "\n"
            + "#else\n#error \"Typedef "
            + newtype + " defined already\"\n#endif\n";

        addIdInfo(newtype
                  , XCD_type.typedeft
                  , s
                  , false
                  , false, ""
                  , false, ""
                  , newtype, ""
                  , framenow.compilationUnitID);
        // mywarning("Added type \"" + newtype + "\" as a newname for \""
        //           + definition + "\" and s is\n" + s);
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
            res=visitTheConfiguration(ctx);
        } else {
            res=visitChildren(ctx);
        }
        return res;
    }

    private LstStr visitTheConfiguration(XCDParser.CompilationUnitContext ctx) {
        LstStr res=null;
        ContextInfo framenow = env.get(env.size()-1); // root
        ContextInfoComp newctx
            = framenow.makeContextInfoComp("@configuration", false);
        env.add(newctx);
        res=visitChildren(ctx);
        Utils.withInputAndFileToWrite
            ("/resources/configuration.pml.template"
             , "configuration.pml"
             , (String confFileContents) -> {
                if (newctx.map.size()!=2) {
                    mywarning("Configuration should have exactly one"
                              + " component instance, but instead, it has "
                              + newctx.map.size());
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
}
