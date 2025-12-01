package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Function;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

// Void is final unfortunately :-(
// class MyReturnType extends Void { public Void() {super();} }

/**
 * A visitor with no visit methods, to define fields and methods used by other
 * classes, and to ensure that whoever extends it does not copy behaviour from
 * some other visitor.
 */
 abstract class BaseVisitor<T>
 /* Use the following to ensure that you haven't forgotten any methods */
     // extends AbstractParseTreeVisitor<T> implements XCDVisitor<T>
     extends XCDBaseVisitor<T>
{
    static String getTokenString(Token tk)
    { return getTokenString(tk.getType()); }
    static String getTokenString(int token) {
        Vocabulary voc = XCDParser.VOCABULARY;

        String literalName = voc.getLiteralName(token);
        myAssert(literalName!=null, "Token " + token + " has no literal name");
        literalName = literalName.replaceAll("[']", "");
        // myWarning("Token " + token + " is " + literalName);
        return literalName;     // remove all 's
    }

    static public String keywordResult
        = getTokenString(XCDParser.TK_RESULT);
    static public String keywordException
        = getTokenString(XCDParser.TK_EXCEPTION);
    static public String keywordIn
        = getTokenString(XCDParser.TK_IN);

    static public String keywordVoid
        = getTokenString(XCDParser.TK_VOID);
    static public String keywordBool
        = getTokenString(XCDParser.TK_BOOL);
    static public String keywordByte
        = getTokenString(XCDParser.TK_BYTE);
    static public String keywordShort
        = getTokenString(XCDParser.TK_SHORT);
    static public String keywordInteger
        = getTokenString(XCDParser.TK_INTEGER);

    /**
     * Miscellaneous helper functions
     */
    static protected void updateln(Tree ctx) {Utils.updateln(ctx); }
    static protected String newgensym() { return Utils.newgensym(null); }
    static protected String newgensym(String pref) {return Utils.newgensym(pref);}
    static final String loopGenSym = newgensym("ForLooping");
    static protected void myAssert(boolean cond, String msg) {
        Utils.myAssert(cond, msg); }
    protected void myassert(boolean cond, String msg) {
        Utils.util.myassert(cond,msg); }
    static protected void myWarning(String msg) { Utils.myWarning(msg); }
    protected void mywarning(String msg) { Utils.util.mywarning(msg); }
    protected void mySyntaxCheck(boolean cond, String msg)
    { Utils.util.// mySyntaxCheck
            myassert(cond, msg); }

    private ArrayList<SymbolTable> stbl = new ArrayList<SymbolTable>();
    ArrayList<SymbolTable> getSTbl() { return stbl; }
    public SymbolTable symbolTableNow() {
        int sz = stbl.size();
        myassert(sz > 0
                 , "symbolTableNow called with empty symbol table stack - stbl.size()=" + sz);
        return stbl.get(stbl.size()-1);
    }
    public SymbolTable symbolTableBefore() {
        int sz = stbl.size();
        myassert(sz > 1
                 , "symbolTableBefore called with empty symbol table stack - stbl.size()=" + sz);
        return stbl.get(stbl.size()-2);
    }
    protected void pushSymbolTable(SymbolTable aSymTab) {
        getSTbl().add(aSymTab);
    }
    private void stblNotEmpty() {
        int sz = getSTbl().size();
        myassert(sz > 0
                 , "Popped too many symbol tables - stbl.size()=" + sz);
    }
    private void popLastSymbolTableReal() {
        int sz = getSTbl().size();
        var fr = symbolTableNow();
        // mywarning("Popping symbol table " + fr.compilationUnitID
        //           + " of type " + fr.type
        //           + " who's parent is " + fr.parent.compilationUnitID
        //           + " of type " + fr.parent.type);
        getSTbl().remove(sz-1);
    }
    protected void popLastSymbolTable(SymbolTable aSymtab) {
        int sz = getSTbl().size();
        stblNotEmpty();
        SymbolTable laststbl = getSTbl().get(sz-1);
        myassert(aSymtab == laststbl
                 , "Current symbol table is not the last element!"
                 + ("\n Name: "
                    + aSymtab.compilationUnitID
                    + " vs "
                    + laststbl.compilationUnitID)
                 + ("\n Type: "
                    + aSymtab.type
                    + " vs "
                    + laststbl.type)
                 );
        popLastSymbolTableReal();
    }
    protected void popLastSymbolTable() {
        stblNotEmpty();
        popLastSymbolTableReal();
    }

    final static public SymbolTableRoot rootContext = new SymbolTableRoot();

    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, String varTypeName
                               , boolean is_paramp
                               , ArraySizeContext arraySize
                               , VariableDefaultValueContext initVal
                               , String parentId) {
        IdInfo res
            = addIdInfo(symbol, tp, is_paramp, arraySize
                        , initVal, parentId);
        res.variableTypeName = varTypeName;
        return res; }
    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, boolean is_paramp
                               , ArraySizeContext arraySize
                               , VariableDefaultValueContext initVal
                               , String parentId) {
        var newInfo = new IdInfo(tp, is_paramp
                                 , arraySize
                                 , initVal
                                 , parentId);
        var currentMap = stbl.get(stbl.size()-1).map;
        if (currentMap.containsKey(symbol)) {
            IdInfo info = currentMap.get(symbol);
            boolean matches = (info.type == newInfo.type)
                // && (info.sType.equals(newInfo.sType))
                && (info.is_param == newInfo.is_param)
                && ( (info.arraySz==null && newInfo.arraySz==null)
                     || (info.arraySz!=null && newInfo.arraySz!=null
                         && (info.arraySz.equals(newInfo.arraySz))) )
                && ( (info.initVal==null && newInfo.initVal==null)
                     || (info.initVal!=null && newInfo.initVal!=null
                         && (info.initVal.equals(newInfo.initVal))) )
                && (info.parent.equals(newInfo.parent));
            if (!matches) {
                mywarning("Symbol \"" +symbol+"\" already in the map"
                          + " (was/is)\ntype: " + info.type + " vs " + newInfo.type
                          // + "\n" + info.sType + " vs " + newInfo.sType
                          + "\nis_param: " + info.is_param + " vs " + newInfo.is_param
                          + "\narraySz: " + info.arraySz + " vs " + newInfo.arraySz
                          + "\ninitVal: " + info.initVal + " vs " + newInfo.initVal
                          + "\nparent: " + info.parent + " vs " + newInfo.parent);
                myassert(false, "Symbol \""+symbol+"\" already in the map");
            } else
                myassert(false, "Symbol \"" +symbol+"\" already in the map - input visited twice!");

        } else {
            currentMap.put(symbol, newInfo); }
        return newInfo;
    }

    public IdInfo getIdInfo(String id){return getIdInfo(symbolTableNow(), id);}
    public IdInfo getIdInfo(SymbolTable currEnv, String id) {
        IdInfo res = null;
        while (res == null && currEnv!=null) {
            Map<String,IdInfo> the_map = currEnv.map;
            if (the_map.containsKey(id))
                res=the_map.get(id);
            currEnv=currEnv.parent;
        }
        myassert(res!=null, "getIdInfo: Symbol \"" + id + "\" not found");
        return res;
    }

    public DataTypeContext makeDataType(String dt) {
        org.antlr.v4.runtime.CharStream input
            = org.antlr.v4.runtime.CharStreams.fromString(dt);
        // create a lexer that feeds off of input CharStream
        XCDLexer lexer
            = new XCDLexer(input);
        // create a buffer of tokens pulled from the lexer
        org.antlr.v4.runtime.CommonTokenStream tokens
            = new org.antlr.v4.runtime.CommonTokenStream(lexer);
        // create a parser that feeds off the tokens buffer
        XCDParser parser
            = new XCDParser(tokens);
        // begin parsing at "dataType" parse rule
        DataTypeContext tree
            = (DataTypeContext) parser.dataType();
        return tree;
    }

    String getThisMethodXXXName(Function<String
                                , Function<String
                                , Function<String, String>>> func) {
        SymbolTable framenow=symbolTableNow();
        String methodName = framenow.compilationUnitID;
        String portName = framenow.parent.compilationUnitID;
        String compName = framenow.parent.parent.compilationUnitID;
        // Code assumes that we're in a component. We could be in a role!
        if (framenow.parent.parent.type!=XCD_type.componentt)
            mywarning("LHS: wrong translation for role variables");
        return func.apply(compName).apply(portName).apply(methodName);
    }
    String getThisMethodResultName() {
        Function<String
            , Function<String
            , Function<String, String>>>
            res = c -> p -> m -> Names.varNameRESULT(c, p, m);
        return getThisMethodXXXName(res);
    }
    String getThisMethodExceptionName() {
        Function<String
            , Function<String
            , Function<String, String>>>
            res = c -> p -> m -> Names.varNameEXCEPTION(c, p, m);
        return getThisMethodXXXName(res);
    }
    //

    boolean isComposite(SymbolTableComposite info)
    { return info.subcomponents.size()!=0; }
    boolean hasProvidedPorts(SymbolTableComponent info)
    { return info.compConstructs.providedprts.size()!=0; }
    boolean hasRequiredPorts(SymbolTableComponent info)
    { return info.compConstructs.requiredprts.size()!=0; }
    boolean hasEmitterPorts(SymbolTableComponent info)
    { return info.compConstructs.emitterprts.size()!=0; }
    boolean hasConsumerPorts(SymbolTableComponent info)
    { return info.compConstructs.consumerprts.size()!=0; }
    boolean hasPorts(SymbolTableComponent info)
    { return hasProvidedPorts(info) || hasRequiredPorts(info)
            || hasEmitterPorts(info) || hasConsumerPorts(info); }

    String component_typeof_id(String var) { return Names.typeOfVar(var);
        // "TypeOf("+ var + ")";
    }
    String component_variable_id(String var, String index)
    { return var + "[" + index + "]"; }
    abstract String component_variable_id(String var, ArraySizeContext index);

    boolean is_enumConstant(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.enumt);
    }
    public String component_variable_result(String actionid){
        var framenow = symbolTableNow();
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        return Names.portActionNameRes(compTypeid, portid, actionid);
    }
    public String component_variable_exception(String actionid) {
        var framenow = symbolTableNow();
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        return Names.portActionNameExc(compTypeid, portid, actionid);
    }

    public boolean isVarComponentParam(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.paramt);
    }
    public String nameOfVarComponentParam(String id) {
        var idInfo = getIdInfo(id);
        return Names.varNameComponent(idInfo.parent, id);
    }
    public boolean isVarConnectorParam(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.connectorpart);
    }
    public String nameOfVarConnectorParam(String id) { // supposedly also considers var_prefix
        var framenow = symbolTableNow();
        var idInfo = framenow.map.get(id);
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        String s = Names.xVarName("UNKNOWN_conn", "UNKNOWN_role", id);

        // var String output = "";
        // var String connIns_i = "";
        // for(element: root.elements){
        //     if(element.connector != null ){
        //      if(element.connector.params.par_pre.prim_param != null &&
        //         element.connector.params.par_pre.prim_param.id == variable
        //         ){
        //          connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
        //          output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
        //      }
        //      else{
        //          for(par: element.connector.params.pars){
        //                      if(par.prim_param!= null && par.prim_param.id == variable){
        //                  connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
        //                     output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
        //                      }
        //          }
        //      }
        //     }
        // }

        return s;
    }

    public int getTokenType( Token tk ) {return tk.getType();}
}
