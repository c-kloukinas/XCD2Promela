package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ContextInfo {
    // Context - inside what are we?
    //    String compTypeID; // supposed to have an id, a body, and a param field.
    // int portID;
    // String varPrefix;
    // Above are old, kept so old code compiles - remove them eventually XXX
    String compilationUnitID; // enclosing context
    XCD_type type;                // which is of type (comp,conn,conf,tdef,enum)
    Map<String,IdInfo> map;
    LstStr paramsORvars;
    LstStr params;
    LstStr vars;
    LstStr subcomponents;
    LstStr providedprts; LstStr requiredprts;
    LstStr consumerprts; LstStr emitterprts;
    ContextInfo(String compUnitID, XCD_type tp, boolean is_paramp) {
        BasicVisitor.myAssert(compUnitID!=null, "compUnitID is null");
        compilationUnitID = compUnitID;
        type = tp;
        paramsORvars = new LstStr();
        params = new LstStr();
        vars = new LstStr();
        subcomponents = new LstStr();
        providedprts = new LstStr();
        requiredprts = new LstStr();
        consumerprts = new LstStr();
        emitterprts = new LstStr();
        map = new HashMap<String,IdInfo>();
        map.put(compilationUnitID,
                new IdInfo(tp
                           , "component"
                           , is_paramp
                           , false, null // not an array, no array size
                           , false, null // no initial value
                           , compilationUnitID // what's my big name?
                           , ""     // var_prefix
                           , ""     // parent (root)
                           ));
    }

}
