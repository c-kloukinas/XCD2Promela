package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;

public abstract class ComponentVisitor extends BasicVisitor {

    @Override public LstStr
	visitVariable_initialValue(XCDParser.Variable_initialValueContext ctx) {
	LstStr res = new LstStr();
	String s = "";

	if (ctx.itrue!=null)
	    s = "true";
	else if (ctx.ifalse!=null)
	    s = "false";
	else if (ctx.inum!=null)
	    s = ctx.inum.getText();
	else 			// ID
	    s = getIdInfo(ctx.icons.getText()).big_name;

	res.add(s);
	return res;
    }

    @Override
    public LstStr visitArraySize(XCDParser.ArraySizeContext ctx) {
	mywarning("TODO: Array size should be a general expr normally, now a number or an ID only");
	LstStr res = new LstStr();
	String s = "";

	if (ctx.constant!=null)
	    s = ctx.constant.getText();
	else
	    s = getIdInfo(ctx.config_par.getText()).big_name;

	res.add(s);
	return res;
    }

    @Override public LstStr
	visitPrimitiveVariableDeclaration(XCDParser.PrimitiveVariableDeclarationContext ctx) {
	LstStr res = new LstStr();
	String varName = ctx.id.getText();
	String s = varName;
	String dtype = visit(ctx.type).get(0); // int, byte, bool, void, ID(long name)

	boolean is_arrayp = ctx.size!=null;
	String array_sz = "";
	if (is_arrayp) {
	    array_sz = visit(ctx.size).get(0);
	}
	boolean has_initValp = ctx.op!=null;
	String initVal = "";
	if (has_initValp) {
	    initVal = visit(ctx.initval).get(0);
	}
	var framenow = env.get(env.size()-1);
	String compUnitId = framenow.compilationUnitID;
	XCD_type tp = framenow.type;
	String bigname = "";
	if (tp == XCD_type.componentt) {
	    bigname = "COMPONENT_" + compUnitId + "_VAR_" + varName;
	} else if (tp == XCD_type.rolet) {
// connector Customer_Cashier(Customer{pay}, Cashier{customer}) {
//   role Customer{
//     bool dummyVariable3 := true;
//
// "CONNECTOR_" + Customer_Cashier + _conn1_0 + "_ROLE_" + Customer + "_VAR_" + dummyVariable3
	    String myConnector = getIdInfo(compUnitId).parent;
	    bigname = "CONNECTOR_" + myConnector
		// // "conn1" is a connector instance, "0" is its array index
		// + "MISSING_CONNECTOR_INSTANCE_UNKNOWN"
		+ "_ROLE_" + myConnector
		+ "_VAR_" + varName
		// "conn1" is a connector instance, "0" is its array index
		+ "MISSING_CONNECTOR_INSTANCE_UNKNOWN"
		;
	} else
	    myassert(false
		     , "Unknown context of variable declaration: "
		     + "frame parent=\"" + framenow.compilationUnitID
		     + "\" type=\"" + framenow.type + "\"");

	addIdInfo(varName
		  , XCD_type.unknownt
		  , dtype
		  , false /* not a param, right? */
		  , is_arrayp, array_sz
		  , has_initValp, initVal
		  , bigname, ""
		  , compUnitId);
	res.add(s);
	return res;
    }

    @Override
    public LstStr visitDataType(XCDParser.DataTypeContext ctx) {
	LstStr res = new LstStr();
	String s = "";
	if (ctx.basic!=null) {
	    Token tk = (Token) ctx.basic;
	    if (tk.getType() == XCDParser.TK_INTEGER)
		s = "XCDINT";
	    else if (tk.getType() == XCDParser.TK_BYTE)
		s = "XCDBYTE";
	    else if (tk.getType() == XCDParser.TK_BOOL)
		s = "XCDBOOL";
	    else if (tk.getType() == XCDParser.TK_VOID)
		s = "XCDVOID";
	    // System.err.println("Basic type: "+ctx.basic.getText() +" "+s);
	} else			// ID
	    s = getIdInfo(ctx.getText()).big_name;
	res.add(s);
	// System.err.println("TYPE READ: " + s);
	return res;
    }

    @Override
    public LstStr visitComponentDeclaration(XCDParser.ComponentDeclarationContext ctx) {
	String compId = ctx.id.getText();
	ContextInfo newctx
	    = new ContextInfo(compId, XCD_type.componentt, false);
	env.add(newctx);
	// For components we create two files - an instance and a header.
	LstStr res = new LstStr(2);
	String header = "";
	String instance = "";
	LstStr argList = visit(ctx.param);

	String compName = ctx.id.getText();
	instance += "proctype instance_name(CompositeName,CompositeID,"
	    + compName + ",CompInstanceID,Instance)(";
	int prmsz = argList.size();
	if (prmsz>01)
	    instance += argList.get(0);
	if (prmsz>1)
	    for (int i=1; i<prmsz; ++i)
		instance += ","+argList.get(i);
	instance += ") {" + "\n" ;

	instance += "Component_i_c_code(CompositeName,CompositeID,"
	    + compName
	    + ",CompInstanceID, Instance);\n\n"
	    + "Component_i_roleData("
	    + compName
	    + ",CompInstanceID, Instance);\n\n";

	instance += "}\n";
	// Create instance & header files
	try (FileWriter inst
	     = new FileWriter("COMPONENT_TYPE_"+compId+"_INSTANCE.pml");
	     FileWriter hdr
	     = new FileWriter("COMPONENT_TYPE_"+compId+".h")) {
	    hdr.write(header);
	    inst.write(instance);
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	int last = env.size()-1;
	ContextInfo lastctx = env.get(last);
	myassert(newctx == lastctx, "Context not the last element");
	env.remove(env.size()-1); // should match what was added
	res.add(instance);
	res.add(header);
	return res;
    }

    @Override
    public LstStr visitFormalParameters(XCDParser.FormalParametersContext ctx) {
	var res = new LstStr();
	String s = "";

	if (ctx.par_pre!=null) {
	    var resrec = visitChildren(ctx);
	    int len1=(resrec!=null)?resrec.size():-1;
	    int len2=ctx.pars.size()+1;
	    myassert(len1 == len2 || len1==-1,
		     "Number of parameters doesn't match ("+len1+") vs ("+len2+")");

	    //     s += visit(ctx.par_pre).get(0);
	    //     for (var p : ctx.pars)
	    // 	s += ","+visit(p).get(0);
	    s += resrec.get(0);
	    resrec.remove(0);
	    for (var p : resrec)
		s += "," + p;
	}
	// System.err.println(" FormalParameters: "+s);
	res.add(s);
	return res;
    }

    @Override
    public LstStr
	visitFormalParameter(XCDParser.FormalParameterContext ctx) {
	String nm = visit(ctx.prim_param).get(0);
	var framenow = env.get(env.size()-1);
	var compId = framenow.compilationUnitID;
	var info = getIdInfo(nm);
	// A parameter's big name is:
	info.big_name = "Component_i_Param_N(CompositeName,CompositeID,"
	    + compId
	    + ",CompInstanceID,Instance,"
	    + nm + ")";
	var res = new LstStr();
	// System.err.println(" FormalParameter: \""+nm+"\" nm=\""+nm+"\"");
	// mywarning("MUSTFIX: Ignores type, array, initial value) - start from the bottom and move up...");
	String s = info.sType + " " + info.big_name;
	if (info.is_array)
	    s += info.arraySz;
	// mywarning("MUSTFIX: Ignores initial value) - start from the bottom and move up...");
	if (info.has_initVal)
	    s += "=" + info.initVal;

	framenow.params.add(nm);

	// System.err.println("Formal param: " + s);
	res.add(s);
	return res;
    }

    @Override public LstStr visitActualParameters(XCDParser.ActualParametersContext ctx) { return visitChildren(ctx); }
    @Override public LstStr visitActualParameter(XCDParser.ActualParameterContext ctx) { return visitChildren(ctx); }

}
