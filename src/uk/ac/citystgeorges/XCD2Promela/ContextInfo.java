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
    XCD_type type; // which is of type (root,comp,conn,conf,tdef,enum)
    Map<String,IdInfo> map;
    ContextInfo parent = null;
    ArrayList<ContextInfo> children;
    ContextInfo you() {// System.err.println("I'm a ContextInfo");
        return this;}
    ContextInfo() {
        this("@root", XCD_type.unknownt, false, null); // call next one
    }
    protected ContextInfo(String compUnitID, XCD_type tp, boolean is_paramp, ContextInfo myparent) {
        compilationUnitID = compUnitID;
        type = tp;
        parent = myparent;
        children = new ArrayList<ContextInfo>();
        map = new HashMap<String,IdInfo>();
        map.put(compilationUnitID,
                new IdInfo(tp
                           , is_paramp
                           , false, null // not an array, no array size
                           , false, null // no initial value
                           , compilationUnitID // what's my big name?
                           , ""     // var_prefix
                           , ""     // parent (root)
                           ));
    }

    ContextInfoComp makeContextInfoComp(String compUnitID, boolean is_paramp)
    {   ContextInfoComp res = new ContextInfoComp(compUnitID
                                                  , XCD_type.componentt
                                                  , is_paramp
                                                  , this);
        children.add(res);
        return res; }
}

class ContextInfoComp extends ContextInfo {
    LstStr paramsORvars;
    LstStr params;
    LstStr vars;
    LstStr subcomponents;
    LstStr providedprts; LstStr requiredprts;
    LstStr consumerprts; LstStr emitterprts;

    @Override
    ContextInfoComp you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoComp(String compUnitID, XCD_type tp, boolean is_paramp, ContextInfo myparent) {
        super(compUnitID, tp, is_paramp, myparent);
        BasicVisitor.myAssert(compUnitID!=null, "compUnitID is null");

        paramsORvars = new LstStr();
        params = new LstStr();
        vars = new LstStr();
        subcomponents = new LstStr();
        providedprts = new LstStr();
        requiredprts = new LstStr();
        consumerprts = new LstStr();
        emitterprts = new LstStr();
    }
}
