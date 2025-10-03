package uk.ac.citystgeorges.XCD2Promela;

public class IdInfo {
    XCD_type type;
    String sType;
    boolean is_param;
    boolean is_array;
    String arraySz;
    boolean has_initVal;
    String initVal;
    String big_name;
    String var_prefix;
    String parent;
    IdInfo(XCD_type tp, String stype, boolean is_paramp
	   , boolean is_arrayp, String arraySize
	   , boolean has_initValp, String theInitVal
	   , String big, String prefix
	   , String prnt){
	type = tp;
	sType = stype;
	is_param = is_paramp;
	is_array = is_arrayp;
	arraySz = arraySize;
	has_initVal = has_initValp;
	initVal = theInitVal;
	big_name = big;
	var_prefix = prefix;
	parent = prnt;
    }
}
