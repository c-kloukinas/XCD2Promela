package uk.ac.citystgeorges.XCD2Promela;

import java.util.Map;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

class CallInfo {
    String functionName;
    LstStr argExprList;
    LstStr argRoleList;
    Map<String, LstStr> role2portArgMap;
    CallInfo( String nm, LstStr exprList
              , LstStr roleList, Map<String, LstStr> r2pMap) {
        functionName = nm; argExprList = exprList;
        argRoleList = roleList; role2portArgMap = r2pMap;
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
    ArraySizeContext arraySz;
    String arrayIterator = "_i"; // name of iterator (@id) used on the array
    VariableDefaultValueContext initVal;
    LstStr translation = new LstStr();
    // String big_name;
    // String var_prefix;
    CallInfo callInfo = null;
    String parent;
    EventStructure methodStructure=null; // used by events/methods only
    SymbolTable symbolTable=null;
    IdInfo(XCD_type tp, boolean is_paramp
           , ArraySizeContext arraySize
           , VariableDefaultValueContext theInitVal
           // , String big, String prefix
           , String prnt){
        traceAtRegistration = getStackTrace();
        type = tp;
        is_param = is_paramp;
        arraySz = arraySize;
        initVal = theInitVal;
        // big_name = big;
        // var_prefix = prefix;
        parent = prnt;

        Utils.myAssertHard(arraySize!=null // || BaseVisitor.sizeZero==null
                           , "Array size is null");
    }
    SymbolTable getSB() {
        Utils.myAssertHard(symbolTable!=null
                           , "Couldn't find " + type.name()
                           + "'s symbol table");
        return symbolTable;
    }
}
