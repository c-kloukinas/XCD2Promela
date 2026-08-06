package uk.ac.citystgeorges.XCD2Promela;

import java.util.Map;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

class CallInfoX {
    String connectorType;
    String connectorInstance;
    String connectorInstanceSize;
    String expressionArgs;
    LstStr argExpressionList;
    LstStr argElementList;
    Map<String, LstStr> element2portArgMap;
    CallInfoX( String tp, String nm, String sz
               , String exprArgs, LstStr exprList
               , LstStr elementList, Map<String, LstStr> e2pMap) {
        connectorType = tp; connectorInstance = nm;
        connectorInstanceSize = sz ;
        expressionArgs = exprArgs; argExpressionList = exprList;
        argElementList = elementList; element2portArgMap = e2pMap;
    }
}

class IdInfo {
    private String getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String res = "";
        for (StackTraceElement el : stackTrace) {
            res += "Class: " + el.getClassName()
                + " -> Method: " + el.getMethodName() + "\n";
        }
        return res;
    }
    String traceAtRegistration;
    XCD_type type;
    String  variableTypeName = null;
    boolean is_param;
    boolean has_post = false;
    String arraySizeExpr = "";  // by default, none (means either 0 or 1).
    String arrayIterator = "_i"; // name of iterator (@id) used on the array
    VariableDefaultValueContext initVal;
    LstStr translation = new LstStr();
    // String big_name;
    // String var_prefix;
    CallInfoX callInfoX = null;
    String parent;
    EventStructure methodStructure=null; // used by events/methods only
    SymbolTable symbolTable=null;
    IdInfo(XCD_type tp, boolean is_paramp
           , VariableDefaultValueContext theInitVal
           // , String big, String prefix
           , String prnt){
        traceAtRegistration = getStackTrace();
        type = tp;
        is_param = is_paramp;
        initVal = theInitVal;
        // big_name = big;
        // var_prefix = prefix;
        parent = prnt;
    }
    SymbolTable getSB() {
        Utils.myAssertHard(symbolTable!=null
                           , "Couldn't find " + type.name()
                           + "'s symbol table");
        return symbolTable;
    }
}
