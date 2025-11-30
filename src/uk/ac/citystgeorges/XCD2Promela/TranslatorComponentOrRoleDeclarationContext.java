package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TranslatorComponentOrRoleDeclarationContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ComponentOrRoleDeclarationContext)ctx); }
    public T translate(BaseVisitor<T> bv, ComponentOrRoleDeclarationContext ctx) {
        // bv.mywarning
        //     ("\n***Called TranslatorComponentOrRoleDeclarationContext translate!***");
        String compName = ctx.id.getText();
        int thisStruct = ctx.struct.getType();
        boolean imaComponent = thisStruct==XCDParser.TK_COMPONENT;
        T res = new T();
        SymbolTable framenow = (bv.symbolTableNow());

        if (imaComponent) {
            SymbolTableComponent thisEnv
                = (SymbolTableComponent) framenow;

            // For components we create two files - an instance and a header.
            res = new T(2);
            String header = "";
            String instance = "";
            // // Identify parameters
            // T argList =
            //     (ctx.param!=null)
            //     ? bv.visit(ctx.param)
            //     : new T()
            //     // new LstStr()        // missing
            //     ;
            LstStr argList = thisEnv.compConstructs.params;
            bv.mywarning("TODO: missing code for comp parameters");
            for (String param : argList) {
                IdInfo paramInfo = bv.getIdInfo(thisEnv, param);
                bv.mywarning("Param " + param + " has translation:");
                if (paramInfo.translation.size()!=0)
                    for (var v : paramInfo.translation)
                        bv.mywarning("\t" + v);
                else
                    bv.mywarning("\tNO TRANSLATION!");
            }

            instance += Names.componentHeaderName(compName) + "(";
            int prmsz = (argList!=null) ? argList.size() : 0;
            if (prmsz!=0) {  // these seem to be ignored by the translator
                instance
                    += "/* Parameters ignored, passed through macros */ /* "
                    + argList.get(0);
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
                    bv.mywarning
                        ("UNCHARTED: There's a local enum definition here! "
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
            if (thisEnv.type==XCD_type.compositet) {
                bv.myassert(thisEnv.type!=XCD_type.compositet
                         , "TranslatorComponentOrRoleDeclaration: "
                         + "Shouldn't be called for composite components");
                SymbolTableComposite thisCompositeEnv
                    = (SymbolTableComposite) framenow;
                // Collect sub-components
                LstStr subcomponent_types = new LstStr();
                for (var sub : thisCompositeEnv.subcomponents) {
                    String st
                        = bv.getIdInfo(thisCompositeEnv, sub).variableTypeName;
                    subcomponent_types.add(st);
                }
                String all_subcomponents = def.apply("COMPONENT_TYPE_"
                                                     , subcomponent_types);
                // Collector connectors
                LstStr subconnector_types = new LstStr();
                for (var sub : thisCompositeEnv.subconnectors) {
                    IdInfo info = bv.getIdInfo(thisCompositeEnv, sub);
                    String varName = info.variableTypeName;
                    int sz =
                        // Integer.parseInt(info.arraySz)
                        1;
                    for (int i = 0; i < sz; ++i) {
                        String st = varName + "_" + sub + "_" + i + "_ROLE_";
                        bv.mywarning("TODO: need to do this for all "
                                     + "roles of this connector");
                        subconnector_types.add(st);
                    }
                }
                String all_connectors = def.apply("CONNECTOR_TYPE_"
                                                  , subconnector_types);
                // Any sub-component instances declared?
                if (thisCompositeEnv.subcomponents.size()>0) {
                    //// It's a composite component
                    // for (var s : thisCompositeEnv.subcomponents)
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
                            bv.myWarning("\tTODO: Missing composite "
                                         + "component code!");

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
                    instance += "/* Loop to start all sub-component instances */\n"
                        + "atomic_step {\n  int "
                        + loop_offset
                        + ";\n  "
                        + loop_offset
                        + " = 0;\n";
                    for (String instance_name
                             : thisCompositeEnv.subcomponents) {
                        IdInfo info = bv.getIdInfo(thisCompositeEnv
                                                   , instance_name);
                        String component_def = info.variableTypeName;
                        boolean is_array = info.arraySz!=null;
                        ArraySizeContext sz = info.arraySz;
                        if (is_array)
                            instance += "  do\n"
                                + "  :: " + loop_offset + " < " + sz + " -> \n   ";
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

            /*
             * Add structural variables:
             *
             * bool VALUEEXIST=false
             *
             * byte COMPONENT_<compName>_VAR_PORT_<portName>_INDEX=0
             < Component_i_Port_Num(CompositeName,CompositeID,<compName>,CompInstanceID,Instance,<portName>)
             *
             * byte COMPONENT_<compName>_VAR_PORT_<portName>_CONNECTIONINDEX=0
             < Component_i_port_NUMOFCONNECTIONS(CompositeName,CompositeID,<compName>,CompInstanceID,Instance,<portName>)
             *
             * byte COMPONENT_<compName>_VAR_PORT_<portName>_CALLER[1] = ???
             * Looks like the first component of messages exchanged on channels.
             *
             * byte COMPONENT_<compName>_VAR_PORT_<portName>_ACTION_<eventOrMethodName>_RESULT = 0;
             */
            LstStr translationVarsBool = new LstStr();
            translationVarsBool.add("VALUEEXIST");
            LstStr translationVarsByte = new LstStr();
            for (var port
                     : USet.setUnion(thisEnv.compConstructs.providedprts
                                     , thisEnv.compConstructs.requiredprts
                                     , thisEnv.compConstructs.consumerprts
                                     , thisEnv.compConstructs.emitterprts)) {
                translationVarsByte.add("PORT_" + port + "_INDEX");
                translationVarsByte.add("PORT_" + port + "_CONNECTIONINDEX");
                translationVarsByte.add("PORT_" + port + "_CALLER");
                List<SymbolTable> ports
                    = thisEnv.children.stream()
                    .filter((SymbolTable x) ->
                            {return x.compilationUnitID.equals(port);})
                    .toList();
                long matches = ports.size();
                bv.myassert(matches==1
                            , "A single port should match \""
                            + port
                            + "\" inside component \""
                            + thisEnv.compilationUnitID
                            + "\" - instead we have "
                            + matches);
                SymbolTablePort thePort = (SymbolTablePort) ports.get(0);
                for (var mtd : thePort.portConstructs.basicMethodNames) {
                    Map<String, MethodStructure> sigFull
                        = thePort.portConstructs.methodOverloads.get(mtd);
                    if (sigFull!=null) {
                        List<Type> retTypes
                            = sigFull.values()
                            .stream()
                            .map( (MethodStructure ms) ->
                                  {return ms.resultType;} )
                            .distinct()
                            .toList();
                        Type ret = retTypes.get(0);

                        if (ret.equals("void")
                            || ret.equals("bool")
                            || ret.equals("bit"))
                            translationVarsBool.add("PORT_" + port
                                                    + "_ACTION_" + mtd
                                                    + "_RESULT");
                        else
                            translationVarsByte.add("PORT_" + port
                                                    + "_ACTION_" + mtd
                                                    + "_RESULT");
                    } else {
                        // bv.mywarning("sigFull is null for mtd " + mtd + " :-(")
                        ;
                    }
                }
            }

            for (var trVar : translationVarsBool) {
                bv.mywarning(trVar + " should NOT have a _PRE");
                ((EnvironmentCreationVisitor)bv)
                    .visitVarOrParamDecl
                    ("bit"
                     , trVar
                     , (ArraySizeContext)null
                     , (VariableDefaultValueContext)null
                     , true);
            }
            for (var trVar : translationVarsByte) {
                bv.mywarning(trVar + " should NOT have a _PRE");
                ((EnvironmentCreationVisitor)bv)
                    .visitVarOrParamDecl
                    ("byte"
                     , trVar
                     , (ArraySizeContext)null
                     , (VariableDefaultValueContext)null
                     , true);
            }
            for (String var : thisEnv.compConstructs.vars) {
                IdInfo info = bv.getIdInfo(thisEnv, var);
                String big = Names.varNameComponent(compName, var);
                header += "#define "
                    + Names.typeOfVarDefName(compName, var)
                    + " "
                    + info.variableTypeName + "\n";
                ArraySizeContext arrSz = info.arraySz;
                if (arrSz!=null) {
                    /* See getDataSize in XcdGenerator - seems to assume
                     * it'll be either a number or a component
                     * parameter */
                    // arrSz=("Component_i_Param_N(CompositeName,CompositeID,"
                    //        + compName
                    //        + ",CompInstanceID,Instance,"
                    //        + var + ")");
                    ;
                }

                String type = bv.component_typeof_id(big);
                String nm = big// bv.component_variable_id(big, arrSz)
                    ;
                String init = "";
                if (info.initVal!=null) {
                    bv.mywarning("TODO: If value is a component param, the following doesn't work");
                    // for component params it should be:
                    // "Component_i_Param_N(CompositeName,CompositeID,"
                    //   + compName + ",CompInstanceID,Instance,"+ var + ")"
                    // init = "=InitialValue(COMPONENT_"
                    //     + compName
                    //     + "_VAR_" + var +")";
                    // init = "="
                    //     + Names.varNameComponentInitialValue(compName, var);
                    init = "=" + info.initVal; /* wrong (?) on purpose, to
                                                  see what comes out */
                } else init = "=000"; // default value
                LstStr translation = info.translation;
                bv.myassert(translation.size()==3
                            , "Variable " + nm + " doesn't have translations for itself, its array size, and its initial value");
                String arrSzTrans = translation.get(1);
                String initTrans = translation.get(2);
                if (initTrans==null)
                    initTrans = "0";
                String pre_nm = Names.varPreName(nm);
                // both var & pre(var) have the same initial value.
                header +=
                    "#define INITIALVALUE_"
                    + nm
                    + " "
                    + initTrans
                    + "\n";

                instance += type
                    + " " + nm
                    + "[" + arrSzTrans + "] = "
                    + initTrans + ";\n";
                if (info.has_pre)
                    instance += type +
                        " " + pre_nm
                        + "[" + arrSzTrans + "] = "
                        + initTrans + ";\n";
            }

            instance += "}\n";
            // Create instance & header files
            {
                Utils.withFileWriteString("COMPONENT_TYPE_"
                                          + compName
                                          + ".h"
                                          , header);
                Utils.withFileWriteString("COMPONENT_TYPE_"
                                          + compName
                                          + "_INSTANCE.pml"
                                          , instance);
            }
            res.add(instance);
            res.add(header);
        } else {                // I'm a role
            bv.mywarning("TODO: Need to translate roles!");
        }
        return res;
    }
}
