package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorComponentDeclarationContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ComponentDeclarationContext)ctx); }
    public T translate(BaseVisitor<T> bv, ComponentDeclarationContext ctx) {
        // bv.mywarning
        //     ("\n***Called TranslatorComponentDeclarationContext translate!***");
        String compName = ctx.id.getText();
        ContextInfoComp thisEnv = (ContextInfoComp) (bv.frameNow());

        // For components we create two files - an instance and a header.
        T res = new T(2);
        String header = "";
        String instance = "";
        // Identify parameters
        T argList =
            (ctx.param!=null)
            ? bv.visit(ctx.param)
            : new T()
            // new LstStr()        // missing
            ;
        bv.mywarning("TODO: missing code for comp parameters");

        instance += Names.componentHeaderName(compName) + "(";
        int prmsz = (argList!=null) ? argList.size() : 0;
        if (prmsz!=0) {  // these seem to be ignored by the translator
            instance += "/* Parameters ignored, passed through macros */ /* " +
                argList.get(0);
            if (prmsz>1)
             for (int i=1; i<prmsz; ++i)
                 instance += ","+argList.get(i);
            instance += " */";
        }
        // for (String var : thisEnv.paramss) {
        //     IdInfo info = getIdInfo(thisEnv, var);
        //     info.big_name = thisEnv.getParamName(var);
        // }
        instance += ")" + " {" + "\n\n" ;

        // LstStr body_res = visit(ctx.body); // Visit the component body

        // add local enums here
        Map<String,IdInfo> compMap = thisEnv.map;
        for (var key : compMap.keySet()) {
            var value = compMap.get(key);
            if (value.type == XCD_type.enumt) {
                bv.mywarning("UNCHARTED: There's a local enum definition here! "
                          + value.variableTypeName);
                instance += value.variableTypeName;
            }
        }

        bv.mywarning("\tTODO: complete the component code");
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
        String all_typedefs = def.apply("TYPE_"
                                        , thisEnv.compConstructs.typedefs);
        // Collect enums
        String all_enums = def.apply("TYPE_"
                                     , thisEnv.compConstructs.enums);
        // Collect sub-components
        LstStr subcomponent_types = new LstStr();
        for (var sub : thisEnv.subcomponents) {
            String st = bv.getIdInfo(thisEnv, sub).variableTypeName;
            subcomponent_types.add(st);
        }
        String all_subcomponents = def.apply("COMPONENT_TYPE_", subcomponent_types);
        // Collector connectors
        LstStr subconnector_types = new LstStr();
        for (var sub : thisEnv.subconnectors) {
            IdInfo info = bv.getIdInfo(thisEnv, sub);
            String varName = info.variableTypeName;
            int sz =
                // Integer.parseInt(info.arraySz)
                1;
            for (int i = 0; i < sz; ++i) {
                String st = varName + "_" + sub + "_" + i + "_ROLE_";
                bv.mywarning("TODO: need to do this for all roles of this connector");
                subconnector_types.add(st);
            }
        }
        String all_connectors = def.apply("CONNECTOR_TYPE_"
                                          , subconnector_types);
        // Any sub-component instances declared?
        if (thisEnv.subcomponents.size()>0) { // It's a composite component
            // for (var s : thisEnv.subcomponents)
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
                bv.myWarning("\tTODO: Missing composite component code!");

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

            String loop_offset = bv.newgensym(compName);
            instance += "/* Loop to start all sub-component instances */\natomic_step {\n  int " + loop_offset
                + ";\n  " +loop_offset+" = 0;\n";
            for (String instance_name : thisEnv.subcomponents) {
                IdInfo info = bv.getIdInfo(thisEnv, instance_name);
                String component_def = info.variableTypeName;
                boolean is_array = info.arraySz!=null;
                ArraySizeContext sz = info.arraySz;
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
        if (bv.hasProvidedPorts(thisEnv) || bv.hasConsumerPorts(thisEnv))
            instance += Names.componentIConstraintsName(compName)+";\n\n";

        // only if it has a port (of any kind)
        if (bv.hasPorts(thisEnv))
            instance += Names.componentRoleDataName(compName) + ";\n\n";

        // Action (i.e., port) parameters missing here
        bv.mywarning("TODO: Action/port parameters missing)");
        instance += "// missing action/port parameters\n";

        for (String var : thisEnv.compConstructs.vars) {
            IdInfo info = bv.getIdInfo(thisEnv, var);
            String big = Names.varNameComponent(compName, var);
            header += "#define "
                + Names.typeOfVarDefName(compName, var)
                + " "
                + info.variableTypeName + "\n";
            ArraySizeContext arrSz = null; // when there's no array, just a single instance
            if (info.arraySz!=null) {
                /* See getDataSize in XcdGenerator - seems to assume
                 * it'll be either a number or a component
                 * parameter */
                // arrSz=("Component_i_Param_N(CompositeName,CompositeID,"
                //        + compName
                //        + ",CompInstanceID,Instance,"
                //        + var + ")");
                arrSz = info.arraySz;
            }

            String type = bv.component_typeof_id(big);
            String nm = bv.component_variable_id(big, arrSz);
            String init = "";
            if (info.initVal!=null) {
                bv.mywarning("TODO: If value is a component param, the following doesn't work");
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
        res.add(instance);
        res.add(header);

        return res;
   }
}
