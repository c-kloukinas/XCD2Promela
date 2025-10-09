package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public abstract
    class BasicVisitor
//    extends AbstractParseTreeVisitor<LstStr> implements XCDVisitor<LstStr>
    extends XCDBaseVisitor< LstStr >
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

    //// Visitor methods
    @Override
    public LstStr visitNullaryExpression(XCDParser.NullaryExpressionContext ctx) {
	updateln(ctx);
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
	    var compTypeid = framenow.compilationUnitID;
	    // var idInfo = getIdInfo(id);
	    // var var_prefix = idInfo.varPrefix;
	    // // var portid = framenow.portID;
	    // var portid = "UNKNOWN";
	    // if (var_prefix.contains("COMPONENT"))
	    // 	s += var_prefix
	    // 	    + "PORT_"
	    // 	    + portid
	    // 	    + "_INDEX";
	    // else
	    // 	s += "COMPONENT_"
	    // 	    + "COMPONENTPREFIX_VAR_PORT_"
	    // 	    + portid
	    // 	    + "_INDEX";
	    s += "UNKNOWNAT";
	} else if (ctx.varid != null) {
	    String varid = ctx.varid.getText();
	    myassert(varid!=null && !varid.equals("")
		     , "empty name for a variable");
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
		    // var var_prefix = framenow.varPrefix;
		    var var_prefix = getIdInfo(varid).var_prefix;
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


	// System.out.println("Translation is: " + s);
	res.add(s);
	return res;
    }

    @Override
    public LstStr visitUnaryExpression(XCDParser.UnaryExpressionContext ctx) {
	updateln(ctx);
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
	// System.out.println("Translation is: " + s);
	res.add(s);
	return res;
    }

    @Override
    public LstStr visitMultiplicativeExpression(XCDParser.MultiplicativeExpressionContext ctx) {
	updateln(ctx);
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
	updateln(ctx);
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
	else if (ctx.at != null) {
	    /* The @ is used as a nameless parameter when initialising arrays.
	     * E.g., int arr[N] = @+3; means for (int i=0;i<N;++i) arr[i]=i+3;
	     *
	     * So, we need a local symbol to represent this index (or
	     * a function that does this initialisation)
	     *
	     * Composite (component/connector role), port (or
	     * portvar), line, char-of-at
	     */
	    var framenow = env.get(env.size()-1);
	    var compId = framenow.compilationUnitID;
	    var port_name = "UNKNOWN_port";
	    String big_name = compId + "_" + port_name
		+ "_" + ln + "_" + atchar;

	    s = "UNKNOWN"+big_name;
	} else if (ctx.varid != null) {
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
	    myassert(false, "Unknown case of NullaryExpression_index");
	res.add(s);

	//	System.err.println("Translation is: " + s);
	return res;
    }

    @Override
    public LstStr visitIntegerLiteral(XCDParser.IntegerLiteralContext ctx) {
	updateln(ctx);
	int chldNo = ctx.getChildCount();
	String val = ((chldNo==2)? "-" : "")
	    + ctx.getChild(chldNo-1).getText();
	LstStr res = new LstStr(1);
	res.add(val);
	return res;
    }

    //// Misc support methods
    private String component_variable_result(String actionid){
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compilationUnitID;
	var portid = "UNKNOWN_PORTID"; // framenow.portID;
	return "COMPONENT_" + compTypeid + "_VAR_PORT_" + portid + "_ACTION_" + actionid + "_RESULT";
    }
    private String component_variable_exception(String actionid) {
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compilationUnitID;
	var portid = "UNKNOWN_PORTID"; // framenow.portID;
	return "COMPONENT_" + compTypeid + "_VAR_PORT_" + portid + "_ACTION_" + actionid + "_EXCEPTION" ;
    }

    private boolean isVarComponentParamOld(String variable) {
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compilationUnitID;
	var portid = "UNKNOWN_PORTID"; // framenow.portID;
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
	return (idInfo.type == XCD_type.paramt);
    }
    String nameOfVarComponentParam(String id) {
	var framenow = env.get(env.size()-1);
	var compTypeid = framenow.compilationUnitID;
	var idInfo = framenow.map.get(id);
	// var portid = framenow.portID;
	// var var_prefix = framenow.varPrefix;
	String s = ("Component_i_Param_N(CompositeName,CompositeID,"
		    + compTypeid	// compType.id
		    + ",CompInstanceID,Instance,"
		    + id		// varid
		    + ")");
	myassert(s.equals(idInfo.big_name)
		 , "Parameter's \""+id+"\" big name differs: was\n\""+idInfo.big_name+"\"\n\tbut should be\n\""+s+"\"\n");
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
	var compTypeid = framenow.compilationUnitID;
	var portid = "UNKNOWN_PORTID"; // framenow.portID;
	var var_prefix = getIdInfo(id).var_prefix;
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

    int ln=-1;
    int atchar=-1;
    void resetln() {ln=-1; atchar=-1;}
    void updateln(Tree ctx) {
	Token tk = getAtoken(ctx);
	if (tk==null)
	    { resetln(); return; }
	ln = tk.getLine(); atchar = tk.getStartIndex();
    }
    Token getAtoken(Tree tr) {	// took me a while... - simplified
				// version of updateln1 really
	if (null==tr || (tr instanceof Token)) return (Token)tr;
	Object pl = tr;
	do {
	    Tree ch = ((Tree)pl).getChild(0);
	    pl = (null==ch)?null:ch.getPayload();
	} while (null!=pl && !(pl instanceof Token));
	return (Token)pl;
    }
    // /*
    //  * Double dispatch used for getAtoken/getit - got tired in
    //  * makeTop... Which of course begs the question - why do double
    //  * dispatch at all, if you're going to end up with a test using
    //  * instanceof?!?
    //  */
    // static class Top<T> {
    // 	T field;
    // 	Top<Object> makeTop(Object obj) {
    // 	    if (obj instanceof Tree)
    // 		return new Two<Tree>((Tree)obj);
    // 	    else if (obj instanceof Token)
    // 		return new One<Token>((Token)obj);
    // 	    else
    // 		return new Top<Object>(obj);
    // 	}
    // 	Top(T arg) {field=arg;}
    // 	org.antlr.v4.runtime.Token getAtoken() {
    // 	    return this.getit(field);
    // 	}
    // 	org.antlr.v4.runtime.Token getit(org.antlr.v4.runtime.Token x)
    // 	{ return x; }
    // 	org.antlr.v4.runtime.Token getit(org.antlr.v4.runtime.tree.Tree x)
    // 	{ return new Two(x).getAtoken(); }
    // 	org.antlr.v4.runtime.Token getit(Object x) {
    // 	    	var cl = field.getClass();
    // 		myAssert(false
    // 		     , "ParseTree payload: more than a Token or a RuleContext!"
    // 		     + cl.toString());
    // 	    return null;
    // 	}
    // }
    // static class One<T extends org.antlr.v4.runtime.Token> extends Top<Object> {
    // 	One(T tk) {super(tk);}
    // 	@Override org.antlr.v4.runtime.Token getAtoken() {
    // 	    return this.getit(field);
    // 	}
    // 	@Override org.antlr.v4.runtime.Token getit(org.antlr.v4.runtime.Token x)
    // 	{   return (org.antlr.v4.runtime.Token)x; }
    // 	@Override org.antlr.v4.runtime.Token getit(Object x) {
    // 	    return this.getit((org.antlr.v4.runtime.Token)x); } }
    // /* RuleContext & Tree have the same code for finding the payload,
    //  * therefore no need to distinguish between them, esp. given that
    //  * RuleContext inherits from Tree. So, no need for a Three<T
    //  * extends RuleContext> class/extra getit.
    //  */
    // static class Two<T extends org.antlr.v4.runtime.tree.Tree> extends Top<Object> {
    // 	Two(T tree) { super(tree); }
    // 	@Override org.antlr.v4.runtime.Token getAtoken() {
    // 	    return this.getit(field); }
    // 	@Override org.antlr.v4.runtime.Token getit(org.antlr.v4.runtime.tree.Tree x)
    // 	{   var chld = x.getChild(0);
    // 	    if (chld == null) return null;
    // 	    var pld = chld.getPayload();
    // 	    return makeTop(pld).getAtoken(); }
    // 	@Override org.antlr.v4.runtime.Token getit(Object x) {
    // 	    return this.getit((org.antlr.v4.runtime.tree.Tree)x); } }
    // void updateln3(Tree ctx) {
    // 	Token tk = new Two<Tree>(ctx).getAtoken();
    // 	if (tk==null)
    // 	    { resetln(); return; }
    // 	ln = tk.getLine(); atchar = tk.getStartIndex();
    // }
    // // /* Generic types used here (even though not needed - could have
    // //  * used proper types Token,RuleContext,Tree,Object instead)
    // //  * because they *look* like the type case I want to implement (and
    // //  * stand out more).
    // //  */
    // // <T extends Token> Token getAtoken(T tkn) { return (Token)tkn; }
    // // <T extends RuleContext> Token getAtoken(T rc) {
    // // 	var chld = rc.getChild(0);
    // // 	if (chld == null) return null;
    // // 	var pld = chld.getPayload();
    // // 	return getAtoken(pld);
    // // }
    // // <T extends Tree> Token getAtoken(T ptree) {
    // // 	var chld = ptree.getChild(0);
    // // 	if (chld == null) return null;
    // // 	var pld = chld.getPayload();
    // // 	return getAtoken(pld);
    // // }
    // // <T> Token getAtoken(T arg) {
    // // 	if (null==arg) return null; // sanity check
    // // 	if (arg instanceof Token) return getAtoken((Token)arg);
    // // 	else if (arg instanceof RuleContext) return getAtoken((RuleContext)arg);
    // // 	else if (arg instanceof Tree) return getAtoken((Tree)arg);
    // // 	var cl = arg.getClass();
    // // 	// return getAtoken( cl.cast(arg) ); // Doesn't enable dynamic binding
    // // 	myAssert(false
    // // 		 , "ParseTree payload: more than a Token or a RuleContext!"
    // // 		 + cl.toString());
    // // 	return null;
    // // }
    // // void updateln2(Tree ctx) {
    // // 	Token tk = getAtoken(ctx);
    // // 	if (tk==null)
    // // 	    { resetln(); return; }
    // // 	ln = tk.getLine(); atchar = tk.getStartIndex();
    // // }
    // // // void updateln1(Tree ctx) {
    // // // 	Tree tree = ctx.getChild(0);
    // // // 	if (tree == null) {
    // // // 	    resetln(); return; }
    // // // 	Object pld = tree.getPayload();
    // // // 	Token tk = null;
    // // // 	int depth = 3;
    // // // 	while (!(pld instanceof Token) && depth>0) {
    // // // 	    if (pld instanceof RuleContext) {
    // // // 		var rc = (RuleContext) pld;
    // // // 		if (rc == null) {
    // // // 		    resetln(); return; // bailout, reset them
    // // // 		} else {
    // // // 		    Tree tree2 = rc.getChild(0);
    // // // 		    if (tree2 == null) {
    // // // 			resetln(); return; }
    // // // 		    pld = tree2.getPayload();
    // // // 		}
    // // // 	    } // else: just decrement depth, eventually fall through
    // // // 	    --depth;
    // // // 	}
    // // // 	if ((pld instanceof Token) && pld != null) {
    // // // 	    tk = (Token) pld;	// non-null since pld non-null
    // // // 	    ln = tk.getLine(); atchar = tk.getStartIndex();
    // // // 	} else {
    // // // 	    resetln();		// bailout, reset them
    // // // 	}
    // // // }
    static void myAssert(boolean cond, String msg) {
	assert cond : msg ; if (!cond) throw new RuntimeException(msg); }
    void myassert(boolean cond, String msg) {
	msg = "Line: " +ln + " at char: " + atchar + "\n" + msg;
	BasicVisitor.myAssert(cond,msg); }
    static void myWarning(String msg) {
	System.err.println(msg); }
    void mywarning(String msg) {
	msg = "Line: " +ln + " at char: " + atchar + "\n" + msg;
	BasicVisitor.myWarning(msg); resetln(); }

    ArrayList<ContextInfo> env = new ArrayList<ContextInfo>();
    IdInfo addIdInfo(String symbol
		     , XCD_type tp, String stype, boolean is_paramp
		     , boolean is_arrayp, String arraySize
		     , boolean has_initValp, String initVal
		     , String big_name, String var_prefix
		     , String parentId) {
	var newInfo = new IdInfo(tp, stype, is_paramp
				, is_arrayp, arraySize
				, has_initValp, initVal
				, big_name, var_prefix
				, parentId);
	var currentMap = env.get(env.size()-1).map;
	if (currentMap.containsKey(symbol)) {
	    IdInfo info = currentMap.get(symbol);
	    boolean matches = (info.type == newInfo.type)
		&& (info.sType.equals(newInfo.sType))
		&& (info.is_param == newInfo.is_param)
		&& (info.is_array == newInfo.is_array)
		&& (info.arraySz.equals(newInfo.arraySz))
		&& (info.has_initVal == newInfo.has_initVal)
		&& (info.initVal.equals(newInfo.initVal))
		&& (info.big_name.equals(newInfo.big_name))
		&& (info.parent.equals(newInfo.parent));
	    if (!matches) {
		mywarning("Symbol \"" +symbol+"\" already in the map"
			  + "\n" + info.type + " vs " + newInfo.type
			  + "\n" + info.sType + " vs " + newInfo.sType
			  + "\n" + info.is_param + " vs " + newInfo.is_param
			  + "\n" + info.is_array + " vs " + newInfo.is_array
			  + "\n" + info.arraySz + " vs " + newInfo.arraySz
			  + "\n" + info.has_initVal + " vs " + newInfo.has_initVal
			  + "\n" + info.initVal + " vs " + newInfo.initVal
			  + "\n" + info.big_name + " vs " + newInfo.big_name
			  + "\n" + info.parent + " vs " + newInfo.parent);
		myassert(false, "Symbol \""+symbol+"\" already in the map");
	    } else
		mywarning("Symbol \"" +symbol+"\" aldeary in the map - input visited twice!");

	} else {
	    currentMap.put(symbol, newInfo); }
	return newInfo;
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
