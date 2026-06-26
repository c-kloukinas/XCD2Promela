package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;

public class TranslatorXConnector {

    static public T translate(BaseVisitor<T> bv
                              , CompositeOrConnectorDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComposite thisEnv) {
        final var Err = System.err;
        // _vars will be filled in the template
        String _connector_name = compName;
        Map<String,Integer> params = new HashMap<String,Integer>();
        String _params_pushdefs = "";
        String _params_popdefs = "";
        String _params_name_list = "";
        // _params_fictional: param list with incremental int values
        // for testing the macros
        String _params_fictional = "";
        for (int i = 0, sz = thisEnv.compConstructs.params.size(); i<sz; ) {
            String param = thisEnv.compConstructs.params.get(i);
            params.put(param, ++i);
            _params_pushdefs +=
                "`pushdef(`" + param + "',$" + i + ")'dnl\n";
            _params_popdefs +=
                "`popdef(`" + param + "')'dnl\n";
            _params_fictional += "," + i;
            _params_name_list += "," + param;
        }
        // delete initial ',', if any
        if (_params_fictional.length() != 0) {
            _params_fictional = _params_fictional.substring(1);
            _params_name_list = _params_name_list.substring(1);
        }
        _params_fictional = "(" + _params_fictional + ")";
        bv.myassert(thisEnv.compConstructs.vars==null
                    || thisEnv.compConstructs.vars.size()==0
                    , "Connector " + _connector_name
                    + " cannot have variables.");
        LstStr inlineFunctions = thisEnv.compConstructs.inlineFunctionDecls;
        LstStr assertions = thisEnv.compConstructs.translatedAssertions;
        // thisEnv.subcomponents holds the role names
        //
        // thisEnv.subconnectors holds the subconnector names
        LstStr roles = thisEnv.subcomponents;
        LstStr subconnectors = thisEnv.subconnectors;
        Map<String, LstStr> roles2portvarsInParams
            = thisEnv.roles2portvarsInParams;
        Set<String> subconnector_types = new TreeSet<String>();
        for (var subX : subconnectors) {
            IdInfo subXinfo = bv.getIdInfo(thisEnv,subX);
            Err.println("subconnector " + subX
                        + " has type " + subXinfo.variableTypeName);
            subconnector_types.add(subXinfo.variableTypeName);
        }

        String _connector_subconnectors = "";
        // include sub-connector type definitions
        for (var subXtype : subconnector_types)
            if (subXtype != "CONNECTOR_PROCEDURAL"
                && subXtype != "CONNECTOR_ASYNCHRONOUS") {
                String inc = "," + subXtype + ".pml.m4";
                _connector_subconnectors += inc;
            }
        // delete initial ',', if any
        if (_connector_subconnectors.length() != 0)
            _connector_subconnectors = _connector_subconnectors.substring(1);
        // Err.println("Getting " + _connector_subconnectors);

        String _connector_variables = "";
        String _connector_role_tests = "";
        String role_var_template = Utils.readInputFile
            ("/resources/templates/role_var_sub_template.pml.template");
        for (var _role_name : roles) {
            String role_vars = role_var_template
                .replace("$<role_name>", _role_name);
            String _connector_role_variables = "";
            {
                String cxvr // Context Connector Var Role
                    = "_context," + _connector_name
                    + ",_varname," + _role_name;
                _connector_role_tests
                    += "\n `_EVALNAME('" + cxvr
                    + "`)': _EVALNAME(" + cxvr + ")";
            }
            IdInfo role = bv.getIdInfo(thisEnv, _role_name);
            SymbolTableComponent roleST
                = (SymbolTableComponent)
                thisEnv.children.stream()
                .filter(ch -> ch.compilationUnitID.equals(_role_name))
                .findFirst()
                .orElse(null);
            bv.myassert(roleST!=null
                        , "Cannot find symbol table of role " + _role_name);
            LstStr vars = roleST.compConstructs.vars;

            for (String vari : vars) {
                IdInfo variinfo = bv.getIdInfo(roleST, vari);
                String variitype = variinfo.variableTypeName;
                ArraySizeContext variszCtx = variinfo.arraySz;
                _connector_role_variables +=
                    "\n\t" + variitype
                    + ( " _NAME(__prefixR,"
                        + vari + ")" );
                if (variszCtx!=null) {
                    String varisz = new TranslatorArraySizeContext()
                        .translate(bv, variszCtx).get(0);
                    _connector_role_variables += "[" + varisz + "]";
                }
                _connector_role_variables += ";dnl\n";
            }
            _connector_variables += role_vars
                .replace("$<connector_variables>", _connector_role_variables);

            // TODO
        }
        // _connector_variables = _connector_variables
        //     .replace("_context", "$1_" + _connector_name)
        //     .replace("_varname", "$2");

        // produce translation
        {
            final var pushdefs = _params_pushdefs;
            final var popdefs = _params_popdefs;
            final var fictionalparams = _params_fictional;
            final var paramnameslist = _params_name_list;
            final var X_subconnectors = _connector_subconnectors;
            final var X_variables = _connector_variables;
            final var X_role_tests = _connector_role_tests;
            final Function<String, String> replace_template_arguments
                = (String in) -> {
                String res = in
                .replace("$<connector_name>", _connector_name)
                .replace("$<params_pushdefs>", pushdefs)
                .replace("$<params_popdefs>", popdefs)
                .replace("$<params_fictional>", fictionalparams)
                .replace("$<connector_subconnectors>", X_subconnectors)
                .replace("$<connector_variables>", X_variables)
                .replace("$<connector_role_tests>", X_role_tests);
                if (paramnameslist.equals(""))
                    res = res.replace(",$<params_name_list>", "");
                else
                    res = res.replace("$<params_name_list>", paramnameslist);
                return res;
            };
            Utils.withInputAndFileToWrite
                ("/resources/templates/connector.pml.template"
                 , "CONNECTOR_TYPE_" + _connector_name + ".pml.m4"
                 , replace_template_arguments);
            Utils.withInputAndFileToWrite
                ("/resources/templates/z-testing-role.m4"
                 , "z-testing-role.m4"
                 , replace_template_arguments);
        }

        return new T();
    }

}
