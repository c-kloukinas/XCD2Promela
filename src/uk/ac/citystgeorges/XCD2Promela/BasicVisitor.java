package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public abstract
    class BasicVisitor
    extends AbstractParseTreeVisitor<LstStr> implements XCDVisitor<LstStr>
//  extends XCDBaseVisitor< LstStr >
{
    // @Override abstract public LstStr visitConnectorDeclaration(XCDParser.ConnectorDeclarationContext ctx);

    /*
      See:
      https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor.html#defaultResult()
      https://www.antlr.org/api/Java/org/antlr/v4/runtime/tree/AbstractParseTreeVisitor.html#aggregateResult(T,T)
    */
    @Override
    protected LstStr defaultResult() {return new LstStr();}
    @Override
    protected LstStr aggregateResult​(LstStr aggregate, LstStr nextResult) {
	if (nextResult!=null)
	    for (String s : nextResult)
		aggregate.add(s);
	return aggregate;
    }

    @Override
    public LstStr visitNullaryExpression(XCDParser.NullaryExpressionContext ctx) {
	/*
	  Relies on (passes these to nullaryExpression's visit):

	  componentDeclaration compType,
	  String var_prefix,
	  String portid
	*/
	LstStr res = new LstStr(1);
	String s = "";
	LstStr argList = new LstStr();

	if (ctx.number != null) { // NUM
	    res = visit(ctx.number);
	    return res;
	} else if (ctx.var_withpar != null) {
	    if (ctx.pre != null)
		s = "PRE";
		s += "(" + visit(ctx.var_withpar).get(0) + ")";
	} else if (ctx.trueToken != null)
	    s = "true";
	else if (ctx.falseToken != null)
	    s = "false";
	else if (ctx.at != null) {
	    var framenow = env.get(env.size()-1);
	    var compTypeid = framenow.compTypeID;
	    var portid = framenow.portID;
	    var var_prefix = framenow.varPrefix;
	    // var idInfo = framenow.map.get(id);
	    if (var_prefix.contains("COMPONENT"))
		s += var_prefix
		    + "PORT_"
		    + portid
		    + "_INDEX";
	    else
		s += "COMPONENT_"
		    + "COMPONENTPREFIX_VAR_PORT_"
		    + portid
		    + "_INDEX";
	} else if (ctx.varid != null) {
	    var varid = ctx.varid.getText();
	    if (is_enumConstant(varid)) {
		s = varid;
	    } else {
		if (isVarComponentParam(varid)){
		    s = nameOfVarComponentParam(varid);
		} else if (isVarConnectorParam(varid)) {
		    s = nameOfVarConnectorParam(varid);
		} else if (varid.contains("result")) {
		    s = component_variable_result("ACTIONNOTKNOWN");
		} else if (varid.toLowerCase().contains("exception")) {
		    s = varid;
		} else {
		    var framenow = env.get(env.size()-1);
		    var var_prefix = framenow.varPrefix;
		    s = var_prefix + varid;
		}
		if (ctx.varindex != null) {
		    s += "["
///			+ visit_conditionalExpression(ctx.varindex.index).get(0)
			+ visit(ctx.varindex).get(0) // why .index??? XXX
			+ "]";
		} else {
		    if(!(varid.toLowerCase().contains("result")
			 || varid.toLowerCase().contains("exception")))
			s += "[0]";
		}
	    }
	    // s = "UNKNOWN" + ctx.varid.getText();
	    // if (ctx.varindex != null)
	    // 	s += visit(ctx.varindex).get(0);

	} else if (ctx.inline_id != null)
	    s = "UNKNOWN" + ctx.inline_id.getText() + visit(ctx.inline_args).get(0);
	else if (ctx.result != null)
	    s += component_variable_result("ACTIONNOTKNOWN");
	else if (ctx.xcd_exception != null)
	    s = component_variable_exception("ACTIONNOTKNOWN");
	else
	    myassert(false, "Unknown case of nullaryExpression");


	System.out.println("Translation is: " + s);
	res.add(s);
	return res;
    }

    @Override
    public LstStr visitUnaryExpression(XCDParser.UnaryExpressionContext ctx) {
	/*
	  Relies on (passes these to nullaryExpression's visit):

	  componentDeclaration compType,
	  String var_prefix,
	  String portid
	*/
	LstStr res = new LstStr(1);
	String s = "";

	if (ctx.preop!=null)
	    s="!";
	s += visit(ctx.nullexpr).get(0);

	if (ctx.postop!=null) {
	    if (ctx.postop.getType() == XCDParser.TK_INCREMENT)
		s += "++";
	    else
		s += "--";
	}
	System.out.println("Translation is: " + s);
	res.add(s);
	return res;
    }

    @Override
    public LstStr visitMultiplicativeExpression(XCDParser.MultiplicativeExpressionContext ctx) {
	/*
	  Relies on (passes these to unaryExpression's visit):

	  componentDeclaration compType,
	  String var_prefix,
	  String portid
	*/
	LstStr res = new LstStr(1);
	String s = null;

	s = visit(ctx.multexpr_pre).get(0);
	int children = ctx.getChildCount();
	for (int cnt = 1; cnt < children; ++cnt) {
	    var op = ((Token)ctx.getChild(cnt).getPayload()).getType();
	    var expr = ctx.getChild(++cnt);
	    String ops = ((op == XCDParser.TK_MULTIPLY) ? "*"
			  : (op == XCDParser.TK_DIVIDE) ? "/": "%");
	    s += " "
		+ ops
		+ " " + visit(expr).get(0);
	}
	// System.out.println("Translation is: " + s);
	res.add(s);
	return res;
    }

    private LstStr visitNullaryExpression_index(XCDParser.NullaryExpressionContext ctx) {
	/* XXX: originally only number or expr with par was considered.

	   Was it because the other cases depend on their context?

	   If so, can we split them into N rules, to use each in a
	   specific context?

	   Alternatively, can we produce N translations for them and
	   then choose the appropriate one higher up?
	 */
	LstStr res = new LstStr(1);
	String s = null;
	// def String visit_nullaryExpression_index(String atrep,nullaryExpression nullaryExpr) {
	// 	var String output = "";
	// 	if (nullaryExpr.number != null) {
	// 		if (nullaryExpr.number.valueZero != null)
	// 			output = "0";
	// 		if (nullaryExpr.number.valueUnsigned != null)
	// 			output = "" + nullaryExpr.number.valueUnsigned;
	// 	}
	// 	if (nullaryExpr.at != null) {
	// 		output = atrep;
	// 	}
	//     if (nullaryExpr.pre == null && nullaryExpr.var_withpar != null)
	// 		output = output + "(" +  visit_additiveExpression_index(atrep, nullaryExpr.var_withpar.condexpr1.relexpr_pre)  + ")";
	// 	return output;
	// }

	if (ctx.number != null) { // NUM
	    res = visit(ctx.number);
	    return res;
	} else if (ctx.var_withpar != null) {
	    if (ctx.pre != null)
		s = "UNKNOWNconditionalExpression";
	    else {
		s = "(" + visit(ctx.var_withpar).get(0) + ")";
	    }
	} else if (ctx.trueToken != null)
	    s = "UNKNOWNtrue";
	else if (ctx.falseToken != null)
	    s = "UNKNOWNfalse";
	else if (ctx.at != null)
	    s = "UNKNOWN@";
	else if (ctx.varid != null) {
	    s = "UNKNOWN" + ctx.varid.getText();
	    if (ctx.varindex != null)
		s += visit(ctx.varindex).get(0);
	} else if (ctx.inline_id != null)
	    s = "UNKNOWN" + ctx.inline_id.getText() + visit(ctx.inline_args).get(0);
	else if (ctx.result != null)
	    s = "UNKNOWNresult";
	else if (ctx.xcd_exception != null)
	    s = "UNKNOWNexception";
	else
	    myassert(false, "Unknown case of nullaryExpression");
	res.add(s);

	//	System.err.println("Translation is: " + s);
	return res;
    }

    @Override
    public LstStr visitIntegerLiteral(XCDParser.IntegerLiteralContext ctx) {
	int chldNo = ctx.getChildCount();
	String val = ((chldNo==2)? "-" : "")
	    + ctx.getChild(chldNo-1).getText();
	LstStr res = new LstStr(1);
	res.add(val);
	return res;
    }

    private String component_variable_result(String actionid){
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compTypeID;
	var portid = framenow.portID;
	return "COMPONENT_" + compTypeid + "_VAR_PORT_" + portid + "_ACTION_" + actionid + "_RESULT";
    }
    private String component_variable_exception(String actionid) {
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compTypeID;
	var portid = framenow.portID;
	return "COMPONENT_" + compTypeid + "_VAR_PORT_" + portid + "_ACTION_" + actionid + "_EXCEPTION" ;
    }

    private boolean isVarComponentParamOld(String variable) {
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compTypeID;
	var portid = framenow.portID;
	var idInfo = framenow.map.get(variable);

	// if(compType != null) {
	//     if(compType.param != null){
	// 	if(compType.param.par_pre != null){
	// 	    if(compType.param.par_pre.prim_param.id == variable)
	// 		return true;
	// 	}
	// 	for ( par : compType.param.pars ){
	// 	    if (par.prim_param.id == variable)
	// 		return true;
	// 	}
   	//     }
	// }
	return false;
    }
    boolean isVarComponentParam(String id) {
	var idInfo = getIdInfo(id);
	return (idInfo.type == XCD_type.componentpart);
    }
    String nameOfVarComponentParam(String id) {
	// var idInfo = getIdInfo(id);
	var framenow = env.get(env.size()-1);
	var idInfo = framenow.map.get(id);
	var compTypeid = framenow.compTypeID;
	var portid = framenow.portID;
	var var_prefix = framenow.varPrefix;
	return ("Component_i_Param_N(CompositeName,CompositeID,"
		+ compTypeid	// compType.id
		+ ",CompInstanceID,Instance,"
		+ id		// varid
		+ ")");
    }
    boolean is_enumConstant(String id) {
	var idInfo = getIdInfo(id);
	return (idInfo.type == XCD_type.enumt);
		// var boolean is_enum = false;
		// for (compilationUnit : root.elements) {
		// 	if (compilationUnit.glob_enum != null) {
		// 		if (compilationUnit.glob_enum.constant_pre != null) {
		// 			if (id == compilationUnit.glob_enum.constant_pre)
		// 				is_enum = true;
		// 		}
		// 		for (constant : compilationUnit.glob_enum.constants) {
		// 			if (id == constant)
		// 				is_enum = true;
		// 		}
		// 	}
		// }
		// return is_enum;

    }
    boolean isVarConnectorParam(String id) { // supposedely also considers var_prefix
	var idInfo = getIdInfo(id);
	// var framenow = env.get(env.size()-1);
	// var idInfo = framenow.map.get(id);
	// var compTypeid = framenow.compTypeID;
	// var portid = framenow.portID;
	// var var_prefix = framenow.varPrefix;
	return (idInfo.type == XCD_type.connectorpart);
    }
    String nameOfVarConnectorParam(String id) { // supposedly also considers var_prefix
	var framenow = env.get(env.size()-1);
	var idInfo = framenow.map.get(id);
	var compTypeid = framenow.compTypeID;
	var portid = framenow.portID;
	var var_prefix = framenow.varPrefix;
	String s = "UNKNOWN" + idInfo.big_name;

	// var String output = "";
	// var String connIns_i = "";
        // for(element: root.elements){
	//     if(element.connector != null ){
	// 	if(element.connector.params.par_pre.prim_param != null &&
	// 	   element.connector.params.par_pre.prim_param.id == variable
	// 	   ){
	// 	    connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
	// 	    output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
	// 	}
	// 	else{
	// 	    for(par: element.connector.params.pars){
	//              	if(par.prim_param!= null && par.prim_param.id == variable){
	// 		    connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
	//                     output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
	//              	}
	// 	    }
	// 	}
	//     }
        // }


	return s;
    }

    void myassert(boolean cond, String msg) {
	assert cond : msg ; if (!cond) throw new RuntimeException(msg);}
    void mywarning(String msg) {System.err.println(msg);}

    ArrayList<ContextInfo> env = new ArrayList<ContextInfo>();
    IdInfo addIdInfo(String symbol
		     , XCD_type tp, String stype, boolean is_paramp
		     , boolean is_arrayp, String arraySize
		     , boolean has_initValp, String initVal
		     , String big_name, String var_prefix
		     , String parentId) {
	var mapsTo = new IdInfo(tp, stype, is_paramp
				, is_arrayp, arraySize
				, has_initValp, initVal
				, big_name, var_prefix
				, parentId);
	var currentMap = env.get(env.size()-1).map;
	myassert(!currentMap.containsKey(symbol), "Symbol \""+symbol+"\" already in the map");
	currentMap.put(symbol, mapsTo);
	return mapsTo;
    }
    IdInfo getIdInfo(String id) {
	int last = env.size()-1;
	IdInfo res = null;
	while (res == null && last > -1) {
	    Map<String,IdInfo> the_map = env.get(last).map;
	    if (the_map.containsKey(id))
		res=the_map.get(id);
	    --last;
	}
	myassert(res!=null, "Symbol \""+id+"\" not found");
	return res;
    }
}
