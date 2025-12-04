package uk.ac.citystgeorges.XCD2Promela;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

class IdInfo {
    XCD_type type;
    String  variableTypeName = null;
    boolean is_param;
    boolean has_post = false;
    ArraySizeContext arraySz;
    VariableDefaultValueContext initVal;
    LstStr translation = new LstStr();
    // String big_name;
    // String var_prefix;
    String parent;
    EventStructure methodStructure=null; // used by events/methods only
    IdInfo(XCD_type tp, boolean is_paramp
           , ArraySizeContext arraySize
           , VariableDefaultValueContext theInitVal
           // , String big, String prefix
           , String prnt){
        type = tp;
        is_param = is_paramp;
        arraySz = arraySize;
        initVal = theInitVal;
        // big_name = big;
        // var_prefix = prefix;
        parent = prnt;
    }
}
