package uk.ac.citystgeorges.XCD2Promela;

/* serves as a single point of X (big)name definition */
class Names {
    static String True = "true";
    static String False = "false";
    static String componentName( String comp ) {
        return "COMPONENT_"+comp; }
    static String portName( String port ) {
        return "PORT_" + port; }
    static String methodName( String method ) {
        return "METHOD_" + method; }
    static String actionName( String action ) {
        return "ACTION_" + action; }
    static String componentPortName( String comp, String port ) {
        return componentName(comp + "_" + portName(port)); }
    static String componentHeaderName( String comp ) {
        return "proctype instance_name(CompositeName,CompositeID,"
            + comp + ",CompInstanceID,Instance)"; }
    static String componentInstanceName( String comp, String inst, String ind) {
        return "instance_name(CompositeName,CompositeID,"
            + comp
            + "," + inst
            + "," + ind
            + ")";}
    static String componentRunInstanceName(String comp,String inst,String ind) {
        return "  run "+ componentInstanceName(comp, inst, ind) + "();"; }
    static String componentIConstraintsName(String comp) {
        return "Component_i_c_code(CompositeName,CompositeID,"
            + comp
            + ",CompInstanceID, Instance)"; }
    static String componentRoleDataName(String comp) {
        return "Component_i_roleData("
            + comp
            + ",CompInstanceID, Instance)"; }

    static String paramName( String comp, String param ) {
        return "PARAM_" + param; }
    static String paramNameComponent( String comp, String param ) {
        return "Component_i_Param_N(CompositeName,CompositeID,"
            + comp + ",CompInstanceID,Instance," + param + ")"; }
    static String paramNamePort( String comp, String param ) {
        // return varNameComponent(comp, param); // STRANGE
        return componentName(comp) + paramName(comp, param); }
    static String paramNameAction( String comp, String port, String action, String param ) {
        // return varNameComponent(comp, param); // STRANGE
        return componentPortName(comp, port)
            + "_" + actionName(action)
            + "_PARAM_" + param; }

    static String varNameComponent( String comp, String var ) {
        return componentName(comp) + "_VAR_" + var; }
    static String varNamePort( String comp, String port, String var ) {
        return varNameComponent(comp
                                ,  portName(port + "_" + var)); } // STRANGE
        // return varNameComponent(comp + "_" + portName(port)
        //                         , var); } // STRANGE

    static String varNameMethod( String comp, String port, String method
                                , String var ) {
        return varNamePort(comp, port
                           , methodName(method + "_" + var));
        // return varNamePort(comp, port + "_" + methodName(method),  var);
    }
    static String varNameAction( String comp, String port, String method
                                , String var ) {
        return varNamePort(comp, port
                           , actionName(method + "_" + var));
        // return varNamePort(comp, port + "_" + actionName(method), var);
    }
    static String varNameRESULT( String comp, String port, String method ) {
        return varNameAction(comp, port, method, "RESULT");
    }
    static String varNameEXCEPTION( String comp, String port, String method ) {
        return varNameAction(comp, port, method, "EXCEPTION");
    }

    static String typeOfVarDefName( String comp, String var ) {
        return "TYPEOF_" + varNameComponent(comp, var); }
    static String typeOfVar( String var ) {
        return "TypeOf(" + var + ")"; }
    static String varPostName( String var ) {
        return "POST(" + var + ")"; }
    static String varNameComponentInitialValue( String comp, String var ) {
        return "InitialValue("
            + varNameComponent(comp, var)
            + ")"; }

    static String connectorName(String x) {
        return "CONNECTOR_" + x; }
    static String connProcedural() {
        return "CONNECTOR_PROCEDURAL"; }
    static String connAsynchronous() {
        return "CONNECTOR_ASYNCHRONOUS"; }
    static String roleName( String x, String role ) {
        return connectorName(x)+"_ROLE_"+role; }

    static String paramNameConnector( String x, String var ) {
        return connectorName(x) + "_PARAM_"+var; }
    static String paramNameRole( String x, String role, String var ) {
        return roleName(x, role) + "_PARAM_"+var; }
    static String varNameRole( String x, String role, String var ) {
        return roleName(x, role) + "_VAR_"+var; }
    static String varInstanceNameRole(String x,String role,String var,String inst){
        return varNameRole(x,role,var)+inst; }// really? Not "[" + inst + "]" ?

    static String portActionName( String comp, String port, String act) {
        // return  componentName(comp) + "_VAR_PORT_" + port + "_ACTION_" + act;
        return  componentPortName(comp
                                  , port
                                  + "_"
                                  + actionName(act)); }
    static String portActionNameRes( String comp, String port, String action) {
        return portActionName(comp, port, action) + "_RESULT"; }
    static String portActionNameExc( String comp, String port, String action) {
        return portActionName(comp, port, action) + "_EXCEPTION"; }
    static String portName( String comp, String port ) {
        return comp + "_" + port + "_" + Utils.ln + "_" + Utils.atchar; }

    static String enumGlobalTypeName( String nm ) {
        return "ENUMT_" + nm; }
    static String enumGlobalValueName( String nm ) {
        return "ENUMV_" + nm; }

    static String enumCompTypeName( String comp, String nm ) {
        return componentName( comp + "_" + enumGlobalTypeName(nm) ); }
    static String enumCompValueName( String comp, String nm ) {
        return componentName( comp + "_" + enumGlobalValueName(nm) ); }

    static String enumConnTypeName( String conn, String nm ) {
        return connectorName(conn + "_" + enumGlobalTypeName(nm) ); }
    static String enumConnValueName( String conn, String nm ) {
        return connectorName(conn + "_" + enumGlobalTypeName(nm) ); }

    static String enumRoleTypeName( String conn, String role, String nm ) {
        return roleName(conn, role + "_" + enumGlobalTypeName(nm) ); }
    static String enumRoleValueName( String conn, String role, String nm ) {
        return roleName(conn, role + "_" + enumGlobalTypeName(nm) ); }

    static String typedefGlobalTypeName( String nm ) {
        return "TYPEDEF_" + nm; }

    static String typedefCompTypeName( String comp, String nm ) {
        return componentName( comp + "_" + typedefGlobalTypeName(nm) ); }

    static String typedefConnTypeName( String conn, String nm ) {
        return connectorName(conn + "_" + typedefGlobalTypeName(nm) ); }

    static String typedefRoleTypeName( String conn, String role, String nm ) {
        return roleName(conn, role + "_" + typedefGlobalTypeName(nm) ); }

    static String exceptionName( String nm ) {
        return "EXCEPTION_" + nm; }

    static String Void = "XCDVOID";
    static String Bit = "bit";
    static String Byte = "byte";
    static String Short = "short";
    static String Int = "int";
}
