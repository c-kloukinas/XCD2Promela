package uk.ac.citystgeorges.XCD2Promela;

/* serves as a single point of X (big)name definition */
public class Names {
    static String True = "true";
    static String False = "false";
    static String componentName( String comp ) {
        return "COMPONENT_"+comp; }
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

    static String paramNameComponent( String comp, String param ) {
        return "Component_i_Param_N(CompositeName,CompositeID,"
            + comp + ",CompInstanceID,Instance," + param + ")"; }
    static String paramNamePort( String comp, String param ) {
        return varNameComponent(comp, param); } // STRANGE

    static String varNameComponent( String comp, String var ) {
        return componentName(comp) + "_VAR_" + var; }
    static String varNamePort( String comp, String var ) {
        return varNameComponent(comp, var); } // STRANGE
    static String typeOfVarDefName( String comp, String var ) {
        return "TYPEOF_" + varNameComponent(comp, var); }
    static String typeOfVar( String var ) {
        return "TypeOf(" + var + ")"; }
    static String varPreName( String var ) {
        return "PRE(" + var + ")"; }
    static String varNameComponentInitialValue( String comp, String var ) {
        return "InitialValue("
            + varNameComponent(comp, var)
            + ")"; }

    static String connectorName(String x) {
        return "CONNECTOR_" + x; }

    static String xVarName( String x, String role, String var ) {
        return connectorName(x)+"_ROLE_"+role+ "_VAR_"+var; }
    static String xVarInstanceName(String x,String role,String var,String inst){
        return xVarName(x,role,var)+inst; }

    static String portActionName( String comp, String port, String act) {
        return "COMPONENT_" + comp + "_VAR_PORT_" + port + "_ACTION_" + act; }
    static String portActionNameRes( String comp, String port, String action) {
        return portActionName(comp, port, action) + "_RESULT"; }
    static String portActionNameExc( String comp, String port, String action) {
        return portActionName(comp, port, action) + "_EXCEPTION"; }
    static String portName( String comp, String port ) {
        return comp + "_" + port + "_" + Utils.ln + "_" + Utils.atchar; }

    static String Void = "XCDVOID";
    static String Bit = "bit";
    static String Byte = "byte";
    static String Short = "short";
    static String Int = "int";
}
