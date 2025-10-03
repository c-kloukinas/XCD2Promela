package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ContextInfo {
    // Context - inside what are we?
    String compTypeID; // supposed to have an id, a body, and a param field.
    int portID;
    String varPrefix;
    // Above are old, kept so old code compiles - remove them eventually XXX
    String compilationUnitID; // enclosing context
    XCD_type type;		  // which is of type (comp,conn,conf,tdef,enum)
    Map<String,IdInfo> map;
    ArrayList<String> params;
    ArrayList<String> vars;
    ContextInfo(String compUnitID, XCD_type tp, boolean is_paramp){
	compilationUnitID = compUnitID;
	type = tp;
	params = new ArrayList<String>();
	vars = new ArrayList<String>();
	map = new HashMap<String,IdInfo>();
	map.put(compUnitID,
		new IdInfo(tp
			   , "component"
			   , is_paramp
			   , false, null // not an array, no array size
			   , false, null // no initial value
			   , compUnitID // what's my big name?
			   , ""	    // var_prefix
			   , ""	    // parent (root)
			   ));
    }

}
