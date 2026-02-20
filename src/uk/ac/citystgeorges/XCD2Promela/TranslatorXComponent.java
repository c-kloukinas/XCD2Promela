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

public class TranslatorXComponent {

    static public T translate(BaseVisitor<T> bv
                              , ComponentOrRoleDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComponent thisEnv
                              , String header
                              , String outComponentHeaderName
                              , String outParams
                              , String outEnums) {
        // For components we create two files - an instance and a header.
        T res = new T(2);
        String instance = "";
        // // Identify parameters
        // T argList =
        //     (ctx.param!=null)
        //     ? bv.visit(ctx.param)
        //     : new T()
        //     // new LstStr()        // missing
        //     ;

        instance += outComponentHeaderName + "(";
        instance += outParams + ")" + " {" + "\n\n" ;
        instance += outEnums;

        bv.mywarning("\tTODO: complete the component code");

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
        translationVarsBool
            .add("VALUEEXIST");
        LstStr translationVarsByte = new LstStr();
        for (var port
                 : USet.setUnion(thisEnv.compConstructs.providedprts
                                 , thisEnv.compConstructs.requiredprts
                                 , thisEnv.compConstructs.consumerprts
                                 , thisEnv.compConstructs.emitterprts)) {
            translationVarsByte.add(Names.portName(port
                                                   + "_INDEX"));
            translationVarsByte.add(Names.portName(port
                                                   + "_CONNECTIONINDEX"));
            translationVarsByte.add(Names.portName(port
                                                   + "_CALLER"));
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
            PortConstructs portConstructs = thePort.portConstructs;
            for (var mtd : portConstructs.basicMethodNames) {
                Map<Sig, MethodStructure> sigFull
                    = portConstructs
                    .methodOverloads.get(new Name(mtd));
                // bv.mywarning("port: " + thePort.compilationUnitID
                //              + " DUMP: for method " + mtd
                //              + " " + sigFull);
                if (sigFull!=null) {
                    List<Type> retTypes
                        = sigFull.values()
                        .stream()
                        .map( (MethodStructure ms) ->
                              {return ms.resultType;} )
                        .distinct()
                        .toList();
                    Type ret = retTypes.get(0);
                    String newVarName
                        = Names.portName(port
                                         + "_"
                                         + Names.actionName(mtd
                                                            + "_RESULT"));
                    if (ret.equals("void")
                        || ret.equals("bool")
                        || ret.equals("bit"))
                        translationVarsBool
                            .add(newVarName);
                    else
                        translationVarsByte
                            .add(newVarName);
                } else {
                    portConstructs
                        .methodOverloads
                        .forEach( (k,v)
                                  -> {bv.mywarning("\n\tMap[" + k + "]=" +v);} );
                    bv.myassert(false, "sigFull is null for mtd " + mtd + " :-(")
                        ;
                }
            }
        }

        for (var trVar : translationVarsBool) {
            // bv.mywarning(trVar + " should NOT have a _POST");
            ((EnvironmentCreationVisitor)bv)
                .visitVarOrParamDecl
                ("bit"
                 , trVar
                 , (ArraySizeContext)null
                 , (VariableDefaultValueContext)null
                 , true);
        }
        for (var trVar : translationVarsByte) {
            // bv.mywarning(trVar + " should NOT have a _POST");
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
            String post_nm = Names.varPostName(nm);
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
            if (info.has_post)
                instance += type +
                    " " + post_nm
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

        return res;
    }

}
