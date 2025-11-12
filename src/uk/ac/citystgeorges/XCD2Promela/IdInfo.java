package uk.ac.citystgeorges.XCD2Promela;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

class IdInfo {
    XCD_type type;
    String  variableTypeName = "";
    boolean is_param;
    ArraySizeContext arraySz;
    Variable_initialValueContext initVal;
    LstStr translation = new LstStr();
    // String big_name;
    // String var_prefix;
    String parent;
    IdInfo(XCD_type tp, boolean is_paramp
           , ArraySizeContext arraySize
           , Variable_initialValueContext theInitVal
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
